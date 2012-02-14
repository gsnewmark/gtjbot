;; ## XMPP message parsing
;; A set of functions to process incoming XMPP messages (requests) and
;; generate answers for them.
;; In order to be processed by a bot, messages should follow a basic
;; pattern: [command] [optional arguments].
;; If bot receives empty or malformed (incorrect command or arguments)
;; message it will send a help message to user.
(ns gtjbot.utils.parser
  [:require [clojure.string :as cs]])


;; ## Service functions

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

;; ## Help message generator

;; Help information.
(def help-message (str "Please use one of those commands:\n"))

;; Basic handler which generates a help message. Used when no other
;; handler could produce an answer.
(defrecord HelpMessage []
  MessageHandler
  (processable? [self message] true)
  (generate-answer [self message] help-message))

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

;; Helper functions fot HttpStatusCodeMessage

;; TODO implement this method (retrieve http status codes page from wiki)
(defn- get-http-status-code-definitions
  "Retrieves page with HTTP status codes definitions."
  [] "<dt><span id=\"100\"></span>100 Continue</dt><dd>This means that the server has received the request headers, and that the client should proceed to send the request body (in the case of a request for which a body needs to be sent; for example, a <a href=\"/wikipedia/en/wiki/POST_(HTTP)\" title=\"POST (HTTP)\">POST</a> request). If the request body is large, sending it to a server when a request has already been rejected based upon inappropriate headers is inefficient. To have a server check if the request could be accepted based on the request's headers alone, a client must send <code>Expect: 100-continue</code> as a header in its initial request<sup id=\"cite_ref-RFC_2616_1-1\" class=\"reference\"><a href=\"#cite_note-RFC_2616-1\"><span>[</span>2<span>]</span></a></sup> and check if a <code>100 Continue</code> status code is received in response before continuing (or receive <code>417 Expectation Failed</code> and not continue).<sup id=\"cite_ref-RFC_2616_1-2\" class=\"reference\"><a href=\"#cite_note-RFC_2616-1\"><span>[</span>2<span>]</span></a></sup></dd><dt><span id=\"509\"></span>509 Bandwidth Limit Exceeded (Apache bw/limited extension)</dt><dd>This status code, while used by many servers, is not specified in any RFCs.</dd>")

(defn- get-http-status-code-meaning
  "Returns meaning of a given numeric HTTP status code."
  [code] (let [source-page (get-http-status-code-definitions)
               definition-html
               (first (re-seq
                 (re-pattern
                  (str "<dt><span id=\""
                       code
                       "\"></span>(.*?)</dt><dd>(.*?)</dd>"))
                 source-page))]
           (if (nil? definition-html)
             (str code " - No such code.")
             (sanitize-html-and-brackets
              (str (definition-html 1) "\n" (definition-html 2))))))

;; Gives a description of a given numeric HTTP status code.
(defrecord HttpStatusCodeMessage [command-word]
  MessageHandler
  (processable? [self message] (check-command-word (re-pattern command-word) message))
  (generate-answer [self message]
    (apply str (interpose "\n\n"
                          (map get-http-status-code-meaning
                               (retrieve-arguments message))))))

;; List with all 'main' message handlers instances.
(def handlers-list [(HiMessage.) (HttpStatusCodeMessage. "httpsc")])

;; ## Actual answer generation

(defn generate-answer
  "Generates answer to an incoming message using existing message handlers. If none of the 'main' handlers could produce a message then help is generated."
  [message]
  (let [applicable-handlers (filter #(processable? % message) handlers-list)
        answers (map #(generate-answer % message) applicable-handlers)]
    (if (empty? answers)
      (generate-answer (HelpMessage.) message)
      (apply str answers))))


