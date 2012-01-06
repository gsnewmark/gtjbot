(ns #^{:doc "Wrappers around Google's XMPP API."} gtjbot.utils.xmpp
    (:import [com.google.appengine.api.xmpp JID XMPPService XMPPServiceFactory]))

(defn get-xmpp-service
  "Returns an instance of XMPPService - basic interface for XMPP interaction."
  [] (XMPPServiceFactory/getXMPPService))

(defn send-invite
  "Sends an invite to a specified user to chat with an app."
  [jid] (. (get-xmpp-service) sendInvitation (JID. jid)))