;; ## Definitions of the handlers for XMPP operations
(ns gtjbot.views.xmpp
  (:use [noir.core :only [defpage url-for]]
        [noir.response :only [redirect status]]
        [gtjbot.utils.user :only [get-user-email]]
        [gtjbot.utils.xmpp :only [send-invite send-message-to-subscribed]]
        [gtjbot.views.user :only [user-main]]))


;; Sends an invite to current user to participate in a chat with a bot.
(defpage subscribe [:get "/xmpp/register/"] []
  (send-invite (get-user-email))
  (redirect (url-for user-main)))

;; Main handler for incoming XMPP messages (sends an answer and returns OK status).
(defpage [:post "/_ah/xmpp/message/chat/"] {:keys [body from]}
  (send-message-to-subscribed [from] body)
  (status 200 nil))
