(ns #^{:doc "Definitions of handlers for XMPP operations."} gtjbot.views.xmpp
    (:use [noir.core :only [defpage]]
          [noir.response :only [redirect]]
          [gtjbot.utils.user :only [get-user-email]]
          [gtjbot.utils.xmpp :only [send-invite]]))

;; Sends an invite to current user to participate in a chat with a bot.
(defpage "/xmpp/register/" []
  (send-invite (get-user-email))
  (redirect "/"))