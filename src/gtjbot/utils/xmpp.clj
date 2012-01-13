;; ## Wrappers around Google's XMPP API
(ns gtjbot.utils.xmpp
  (:use [gtjbot.models.user :only [check-user-by-mail message-to-unsubscribed]]
        [gtjbot.utils.parser :only [generate-answer]])
  (:import [com.google.appengine.api.xmpp JID Message MessageBuilder MessageType
            XMPPService XMPPServiceFactory]))


;; ## General XMPP functions 

;; Returns an instance of XMPPService - basic interface for XMPP interaction.
(def xmpp-service (XMPPServiceFactory/getXMPPService))

(defn send-invite
  "Sends an invite (to a chat with an app) to a specified user."
  [jid] (. xmpp-service sendInvitation (JID. jid)))

(defn form-message
  "Forms an instance of a Message class from a jids list (jids as strings)
and a message string."
  [jids message] (. (. (. (. (MessageBuilder.) withRecipientJids
                            (into-array JID (map #(JID. %) jids)))
                         withBody message)
                      withMessageType (. MessageType NORMAL))
                   build))

(defn send-message
  "Sends a message over XMPP to a specified jids list (jids as strings)."
  [jids message] (if (and (not (empty? jids)) (not (nil? message)))
                   (. xmpp-service sendMessage
                      (form-message jids message))))

;; ## App specific XMPP functions

(defn jid-to-mail
  "Cuts redundant info if jid is in a full form to make an 'email'
(bare form) from it."
  [jid] (if-let [search-results (re-matches #"(.*)/.*" jid)]
          (last search-results)
          jid))

(defn send-message-to-subscribed
  "Sends a message to the users subscribed for the app. If the user
 is unsubscribed to an app they will receive an offer to subscribe."
  [jids message] (let [predicate #(check-user-by-mail (jid-to-mail %))
                       subscribed-jids (filter predicate jids)
                       unsubscribed-jids (remove predicate jids)]
                   (send-message subscribed-jids (generate-answer message))
                   (send-message unsubscribed-jids message-to-unsubscribed)))