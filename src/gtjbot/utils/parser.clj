;; ## XMPP message parsing
;; A set of functions to process incoming XMPP messages (requests) and
;; generate answers for them.
;; In order to be processed by a bot, messages should follow a basic
;; pattern: [command] [optional arguments].
;; If bot receives empty or malformed (incorrect command or arguments)
;; message it will send a help message to user.
(ns gtjbot.utils.parser
  [:require [clojure.string :as cs]
            [appengine-magic.services.memcache :as memcache]]
  [:use [appengine-magic.services.url-fetch :only [fetch]]
        [gtjbot.models.user :only [get-guser-handlers-for-mail]]]
  [:import [com.google.appengine.api.memcache Expiration]])


;; ## Service functions

;; Adds meta-data name and help to an object.
(defmacro create-object-with-help [obj name help]
  `(with-meta ~obj {:name ~name :help ~help}))

(defn- retrieve-command
  "Retrieves 'command' word from the incoming message. 'Command' is a first word in a message."
  [message]
  (let [results (re-matches #"^([a-zA-Z0-9]+)\s?.*$" message)]
    (if (nil? results)
      ""
      (last results))))

(defn- retrieve-arguments
  "Retrieves list of command's arguments from the incoming message. Arguments are all words in a message except first."
  [message]
  (let [results (re-matches #"[a-zA-Z0-9]+\s?(.*)" message)
        args-string (if (nil? results) "" (results 1))]
    (filter #(not= % "") (seq (.split args-string ", ")))))

;; ## Message handlers protocol
;; Message handler is a function that generates answer based on the
;; incoming message.
;; Protocol defines basic handler interface.

(defprotocol MessageHandler
  "Interface for message handlers."
  ;; Checks whether the handler can generate an answer for a given
  ;; message (i. e. whether handler's commad word is equal to a
  ;; message's one).
  (processable? [self message])
  ;; Generates answer based on an incoming message. Returns nil if a
  ;; given handler can't process message.
  (generate-answer [self message]))

;; ## Helper functions for reply building

(defn- check-command-word
  "Checks whether the command word contains the given word."
  [command-word message]
  (not (nil? (re-matches (re-pattern (str "^" command-word "$"))
                         (retrieve-command message)))))

(defn- sanitize-html-and-brackets
  "Removes all occurrences of a HTML tag or a [...] from a given string."
  [string] (cs/replace string #"<.*?>|\[.*?\]" ""))

(defn- generate-answer-using-function
  "Generates answer for a given message using a given function."
  [message answer-generator]
  (let [arguments (retrieve-arguments message)]
    (if (empty? arguments)
      "No argument is supplied."
      (cs/join "\n\n"
               (map
                #(let [arg (cs/trim %)]
                   (if (empty? arg)
                     "Empty argument."
                     (answer-generator arg)))
                arguments)))))

;; TODO maybe try-catch possible exceptions
(defn- fetch-page-content
  "Fetches a given link and returns it's content."
  [link] (String. (:content (fetch link :headers {"User-Agent" "gtjbot"}))))

(defn- retrieve-contents-of-page
  "Retrieves content of a given HTTP page from cache or Internet. Optional argument says for how much seconds store the retrieved content in a cache."
  ([link] (retrieve-contents-of-page link (* 60 60 24 30)))
  ([link seconds]
     (let [content
           (or (memcache/get link) (fetch-page-content link))]
       (do
         (memcache/put! link content
                        :expiration (Expiration/byDeltaSeconds seconds))
         content))))

(defn- get-first-matched
  "Retrieves first match (if any) of a given pattern (as string) on a given page."
  [pattern-string page]
  (first (re-seq (re-pattern pattern-string) page)))

;; ## Hello Reply

;; Sends greetings in return to user's ones.
(defrecord HiMessage []
  MessageHandler
  (processable? [self message]
    (check-command-word
     #"[hH]ello[\.\s\?\!]*|[Hh]i[\.\s\?\!]*|[Gg]reetings[\.\s\?\!]*|[Hh]ail[\.\s\?\!]*"
     message))
  (generate-answer [self _] "Hello!"))

;; ## HTTP Status Code Reply

;; Helper functions for HttpStatusCodeMessage

(defn- get-http-status-code-definitions
  "Retrieves page with HTTP status codes definitions."
  []
  (retrieve-contents-of-page
   (str "https://secure.wikimedia.org/wikipedia/en/w/index.php?"
        "title=List_of_HTTP_status_codes&printable=yes")))

(defn- get-http-status-code-meaning
  "Returns meaning of a given numeric HTTP status code."
  [code] (let [source-page (get-http-status-code-definitions)
               definition-html
               (get-first-matched
                (str "<dt><span id=\""
                     code
                     "\"></span>(.*?)</dt>\n<dd>(.*?)</dd>")
                source-page)]
           (if (nil? definition-html)
             (str code " - No such code.")
             (sanitize-html-and-brackets
              (str (definition-html 1) "\n" (definition-html 2))))))

;; 'Replier' itself

;; Gives a description of a given numeric HTTP status code.
(defrecord HttpStatusCodeMessage [command-word]
  MessageHandler
  (processable? [self message]
    (check-command-word command-word message))
  (generate-answer [self message]
    (generate-answer-using-function message get-http-status-code-meaning)))

;; ## Current Weather Reply

;; Helper functions for a CurrentWeatherMessage

(defn- get-page-with-city-woeid
  "Returns a HTML page with given city's WOEID on it."
  [city]
  (if (empty? city)
    "error"
    (retrieve-contents-of-page
     (str "http://sigizmund.info/woeidinfo/?woeid="
          (cs/replace (sanitize-html-and-brackets city) " " "+")))))

(defn- get-woeid-for-city
  "Returns WOEID for a given city."
  [city]
  (let [page-with-woeid (get-page-with-city-woeid city)
        woeid-search-result
        (get-first-matched "<h3>.*?</h3>WOEID: (\\d+)" page-with-woeid)]
    (if (nil? woeid-search-result)
      0
      (woeid-search-result 1))))

(defn- get-weather-page-for-city
  "Returns a XML page with a forecast for a given WOEID (optional second argument specifies whether temperature must be in a Fahrenheits)."
  ([woeid] (get-weather-page-for-city woeid false))
  ([woeid isFahrenheit]
     (if-not (zero? woeid)
       (let [units (if (true? isFahrenheit) "f" "c")
             link (str "http://weather.yahooapis.com/forecastrss?w="
                       woeid "&u=" units)]
         (retrieve-contents-of-page link 3600))
       "error")))

(defn- extract-forecast-string
  "Extracts string with a forecast from a source page."
  [source]
  (let [forecast-html
        (get-first-matched "(Current Conditions:.*?)<br /><br /><a"
                           (cs/replace source "\n" ""))]
    (if (nil? forecast-html)
      "City not found."
      (sanitize-html-and-brackets
       (cs/replace
        (cs/replace (forecast-html 1) #"(<[bB][rR]\s?/>){2}" "\n")
        #"<[bB][rR]\s?/>" "\n")))))

(defn- get-weather-for-city
  "Returns a weather report summary for a given city."
  [city]
  (let [woeid (get-woeid-for-city city)
        forecast-source (get-weather-page-for-city woeid)
        forecast (extract-forecast-string forecast-source)
        proper-city-name (cs/join " " (map cs/capitalize (cs/split city #"\s")))]
    (str "Weather for " proper-city-name " from Yahoo!\n" forecast)))

;; 'Replier' itself

;; Gives a current weather for a specified city.
(defrecord CurrentWeatherMessage [command-word]
  MessageHandler
  (processable? [self message]
    (check-command-word command-word message))
  (generate-answer [self message]
    (generate-answer-using-function message get-weather-for-city)))

;; ## Currently enabled reply plugins

;; List with all 'main' message handlers instances.
(def handlers-list [(create-object-with-help
                      (HttpStatusCodeMessage. "httpsc")
                      "HTTP Status Code"
                      (str "prints a description of a given HTTP status code. "
                           "Arguments are either a one numeric status code "
                           "or a list of the numeric status codes "
                           "(separated by commas)."))
                    (create-object-with-help
                      (CurrentWeatherMessage. "weather")
                      "Current Weather"
                      (str " prints a short weather report for a given "
                           "area/areas. Arguments are either a one "
                           "geographic are or a bunch of such "
                           "(separated by commas). Provide as much "
                           "info about small area as possible for most "
                           "accurate results (e.g., "
                           "\"kiyevka russia novosibirsk oblast\" for small "
                           "town near Novosibirsk, but for capital of Ukraine "
                           "simply \"Kyiv\" is enough)."))])

(defn generate-user-handlers
  "Removes handlers which names aren't present in a handlers-string from a handlers-list, sets handler's command word to one specified in a handlers-string."
  ([handlers-string] (generate-user-handlers handlers-string handlers-list))
  ([handlers-string handlers-list]
     (if (nil? handlers-string)
       handlers-list
       (if (empty? handlers-string)
         []
         (let [user-prefs (apply hash-map
                                 (flatten
                                  (map #(cs/split % #" \- ")
                                       (cs/split handlers-string #"; "))))]
           (map #(assoc % :command-word (user-prefs (:name (meta %))))
                (filter #(contains? user-prefs (:name (meta %)))
                        handlers-list)))))))

;; ## Help message generator

(defn get-help-message
  "Generates help information for enabled plugins."
  [handlers]
  (str "Please use one of those commands:\n"
       (cs/trim-newline
        (cs/join "\n\n"
                 (remove nil?
                         (map
                          (fn [o] (let [meta-data (meta o)]
                                   (when-not (nil? meta-data)
                                     (str (:command-word o)
                                          " <args> - "
                                          (meta-data :help)))))
                          handlers))))
       "\n\nCommands could be customized at https://gtjbot.appspot.com."))

;; ## Actual answer generation

(defn- generate-answer-for-handlers
  "Actually creates an answer for a given message using a given handlers."
  [handlers message]
  (let [user-handlers (conj handlers (HiMessage.))
        applicable-handlers (filter #(processable? % message) user-handlers)
        answers (map #(generate-answer % message) applicable-handlers)]
    (if (empty? answers)
      (get-help-message user-handlers)
      (apply str answers))))

(defn generate-answer
  "Generates answer to an incoming message using applicable message handlers. If none of the handlers could produce a message then help is generated. If mail is given uses a handlers of a user with this mail, otherwise uses all handlers."
  ([message] (generate-answer-for-handlers handlers-list message))
  ([mail message]
     (let [user-handlers (generate-user-handlers
                          (get-guser-handlers-for-mail mail))]
       (generate-answer-for-handlers user-handlers message))))

