;; ## XMPP message parsing
;; A set of functions to process incoming XMPP messages (requests) and
;; generate answers for them.
;; In order to be processed by a bot, messages should follow a basic
;; pattern: [command] [optional arguments].
;; If bot receives empty or malformed (incorrect command or arguments)
;; message it will send a help message to user.
(ns gtjbot.utils.parser
  [:require [clojure.string :as cs]]
  [:use [appengine-magic.services.url-fetch :only [fetch]]])


;; ## Service functions

;; Adds meta-data name and help to an object.
(defmacro create-object-with-help [obj name help]
  `(with-meta ~obj {:name ~name :help ~help}))

(defn- retrieve-command
  "Retrieves 'command' word from the incoming message. 'Command' is a first word in a message."
  [message] (let [results (re-matches #"^([a-zA-Z]+)\s?.*$" message)]
              (if (nil? results)
                ""
                (last results))))

(defn- retrieve-arguments
  "Retrieves list of command's arguments from the incoming message. Arguments are all words in a message except first."
  [message] (let [results (re-matches #"[a-zA-Z]+\s?(.*)" message)
                  args-string (if (nil? results) "" (results 1))]
              (filter #(not (= % "")) (seq (. args-string split " ")))))

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
  "Checks whether the command word contains the given word(s) (i. e. matcher)."
  [matcher message]
  (not (nil? (re-matches matcher (retrieve-command message)))))

(defn- sanitize-html-and-brackets
  "Removes all occurrences of a HTML tag or a [...] from a given string."
  [string] (cs/replace string #"<.*?>|\[.*?\]" ""))

(defn- generate-answer-using-function
  "Generates answer for a given message using a given function."
  [message answer-generator]
  (let [arguments (retrieve-arguments message)]
    (if (empty? arguments)
      "No argument is supplied."
      (apply str (interpose "\n\n" (map answer-generator arguments))))))

(defn- retrieve-contents-of-page
  "Retrieves content of a given HTTP page."
  [link]
  (String. (:content (fetch link :headers {"User-Agent" "gtjbot"}))))

(defn- find-first-matched
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
  (generate-answer [self message] "Hello!"))

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
               (find-first-matched
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
  (processable? [self message] (check-command-word (re-pattern command-word) message))
  (generate-answer [self message]
    (generate-answer-using-function message get-http-status-code-meaning)))

;; ## Current Weather Reply

;; Helper functions for a CurrentWeatherMessage

(defn- get-page-with-city-woeid
  "Returns a HTML page with given city's WOEID on it."
  (retrieve-contents-of-page
   (str "http://sigizmund.info/woeidinfo/?woeid="
        (sanitize-html-and-brackets city))))
;;<h3>Texas (Town)</h3>WOEID: 1105939
(defn- get-woeid-for-city
  "Returns WOEID for a given city."
  [city]
  (let [page-with-woeid (get-page-with-city-woeid city)]
    (get-first-matched "<h3>.*?</h3>WOEID: (d+?)" page-with-woeid)))

(defn- get-weather-for-city
  "Returns a weather report summary for a given city."
  [city]
  (let [woeid (get-woeid-for-city city)]
    woeid))

;; 'Replier' itself

;; Gives a current weather for a specified city.
(defrecord CurrentWeatherMessage [command-word]
  MessageHandler
  (processable? [self message] (check-command-word (re-pattern command-word) message))
  (generate-answer [self message]
    (generate-answer-using-function message get-weather-for-city)))

;; ## Currently enabled reply plugins

;; List with all 'main' message handlers instances.
(def handlers-list [(HiMessage.)
                    (create-object-with-help
                      (HttpStatusCodeMessage. "httpsc")
                      "HTTP Status Code"
                      (str "prints a description of a given HTTP status code. "
                           "Arguments are either a one numeric status code "
                           "or a list of the numeric status codes."))])

;; ## Help message generator

(defn get-help-message
  "Generates help information for enabled plugins."
  []
  (str "Please use one of those commands:\n"
       (apply str
              (map
               (fn [o] (let [meta-data (meta o)]
                        (when (not (nil? meta-data))
                          (str (:command-word o)
                               " <args> - "
                               (meta-data :help)))))
               handlers-list))))

;; Basic handler which generates a help message. Used when no other
;; handler could produce an answer.
(defrecord HelpMessage []
  MessageHandler
  (processable? [self message] true)
  (generate-answer [self message] (get-help-message)))

;; ## Actual answer generation

(defn generate-answer
  "Generates answer to an incoming message using existing message handlers. If none of the 'main' handlers could produce a message then help is generated."
  [message]
  (let [applicable-handlers (filter #(processable? % message) handlers-list)
        answers (map #(generate-answer % message) applicable-handlers)]
    (if (empty? answers)
      (generate-answer (HelpMessage.) message)
      (apply str answers))))

