(ns #^{:doc "Wrappers around Google's XMPP API."} gtjbot.utils.xmpp
    (:use [gtjbot.models.user :only [check-user-by-mail message-to-unsubscribed]])
    (:import [com.google.appengine.api.xmpp JID Message MessageBuilder MessageType
              XMPPService XMPPServiceFactory]))

;;; XMPP functions 

;; Returns an instance of XMPPService - basic interface for XMPP interaction.
(def xmpp-service (XMPPServiceFactory/getXMPPService))

(defn send-invite
  "Sends an invite to a specified user to chat with an app."
  [jid] (. xmpp-service sendInvitation (JID. jid)))

(defn form-message
  "Forms an instance of a Message class from jids list and message string."
  [jids message] (. (. (. (. (MessageBuilder.) withRecipientJids
                            (into-array JID (map #(JID. %) jids)))
                         withBody message)
                      withMessageType (. MessageType NORMAL))
                   build))

(defn send-message
  "Sends a message over XMPP to a specified jids list."
  [jids message] (if (and (not (empty? jids)) (not (nil? message)))
                   (. xmpp-service sendMessage
                      (form-message jids message))))

;;; App specific functions

(defn jid-to-mail
  "Cuts redundant info if JID is in a full form to make an 'email' from it."
  [jid] (if-let [search-results (re-matches #"(.*)/.*" jid)]
          (last search-results)
          jid))

(defn send-message-to-subscribed
  "Sends a message to users subscribed for the app. If the user is unsubscribed app they will receive an offer to subscribe."
  [jids message] (let [predicate #(check-user-by-mail (jid-to-mail %))
                       subscribed-jids (filter predicate jids)
                       unsubscribed-jids (remove predicate jids)]
                   (send-message subscribed-jids message)
                   (send-message unsubscribed-jids message-to-unsubscribed)))