(ns #^{:doc "Definitions of handlers for XMPP operations."} gtjbot.views.xmpp
    (:use [noir.core :only [defpage url-for compojure-route]]
          [noir.response :only [redirect status]]
          [compojure.core]
          [gtjbot.utils.user :only [get-user-email]]
          [gtjbot.utils.xmpp :only [send-invite send-message]]
          [gtjbot.views.user :only [user-main]]))

;; Sends an invite to current user to participate in a chat with a bot.
(defpage subscribe "/xmpp/register/" []
  (send-invite (get-user-email))
  (redirect (url-for user-main)))

;; Main handler for incoming XMPP messages (sends an answer and returns OK status).
(defpage [:post "/_ah/xmpp/message/chat/"] {:keys [body from]}
  (send-message [from] body)
  (status 200 nil))
