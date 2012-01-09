(ns #^{:doc "Wrappers around Google's XMPP API."} gtjbot.utils.xmpp
    (:use [gtjbot.models.user :only [check-user-by-mail]])
    (:import [com.google.appengine.api.xmpp JID Message MessageBuilder MessageType
              XMPPService XMPPServiceFactory]))

(def xmpp-service
  "Returns an instance of XMPPService - basic interface for XMPP interaction."
  (XMPPServiceFactory/getXMPPService))

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
  [jids message] (let [signed-jids (filter #(check-user-by-mail %) jids)]
                   (if (and (not (empty? signed-jids)) (not (nil? message)))
                     (. xmpp-service sendMessage
                        (form-message signed-jids message)))))