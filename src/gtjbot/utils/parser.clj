;; ## XMPP message parsing
;; A set of functions to process incoming XMPP messages (requests) and
;; generate answers for them.
;; In order to be processed by a bot, messages should follow a basic
;; pattern: [command] [optional arguments].
;; If bot receives empty or malformed (incorrect command or arguments)
;; message it will send a help message to user.
(ns gtjbot.utils.parser)


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
                  args-string (if (nil? results) "" results)]
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

;; ## Help message generator

;; Help information.
(def help-message (str "Please use one of those commands:\n"))

;; Basic handler which generates a help message. Used when no other
;; handler could produce an answer.
(defrecord HelpMessage []
  MessageHandler
  (processable? [self message] true)
  (generate-answer [self message] help-message))

;; ## 'Main' message handlers
;; Functions that actually parse incoming messages to produce some answer.

;; Sends greetings in return to user's ones.
(defrecord HiMessage []
  MessageHandler
  (processable? [self message]
    (not (nil?
          (re-matches
           #"[hH]ello[\.\s\?\!]*|[Hh]i[\.\s\?\!]*|[Gg]reetings[\.\s\?\!]*|[Hh]ail[\.\s\?\!]*"
           (retrieve-command message)))))
  (generate-answer [self message] "Hello!"))

;; List with all 'main' message handlers instances.
(def handlers-list [(HiMessage.)])

;; ## Actual answer generation

(defn generate-answer
  "Generates answer to an incoming message using existing message handlers. If none of the 'main' handlers could produce a message then help is generated."
  [message]
  (let [applicable-handlers (filter #(processable? % message) handlers-list)
        answers (map #(generate-answer % message) applicable-handlers)]
    (if (empty? answers)
      (generate-answer (HelpMessage.) message)
      (apply str answers))))