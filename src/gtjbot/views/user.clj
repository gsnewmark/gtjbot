;; ## Definitions of the pages for basic users
(ns gtjbot.views.user
    (:require [gtjbot.views.common :as common])
    (:use [noir.core :only [defpage url-for]]
          [hiccup.page-helpers :only [link-to]]
          [gtjbot.models.user :only [save-user-to-ds]]
          [gtjbot.utils.user :only [get-user-nick]]
          [appengine-magic.services.user :only [user-logged-in? user-admin?]]))

;; Index page of a site.
(defpage index [:get "/"] []
  (let [links (if (user-logged-in?)
                (let [links [(link-to (url-for user-main) "Profile")]]
                  (if (user-admin?)
                    (conj links (link-to "/admin/main" "Admin Panel"))
                    links))
                [])
        links (concat [(link-to (url-for index) "Main")] links)]
    (common/layout
     :content [[:h1 "App is still under construction."]
               [:h2 "App Overview"]
               [:p [:b "gtjbot"] " is a handy little information bot that "
                "you can use with your XMPP (e. g. Googtle Talk) client. "
                "It's main feature is going to be a customizability - "
                "you can choose which modules bot uses to 'talk' with you. "]
               (when-not (user-logged-in?)
                 [:p "More details could be found after "
                  (common/login-link "logging in") "."])]
     :links links)))

;; Main page of a site for the logged user.
(defpage user-main [:get "/user/profile"] []
  (save-user-to-ds)
  (let [links [(link-to (url-for index) "Main")
               (link-to (url-for user-main) "Profile")]
        links (if (user-admin?)
                (conj links (link-to "/admin/main" "Admin Panel"))
                links)]
    (common/layout
     :content [[:h1 (str "Welcome to gtjbot customization page, "
                         (get-user-nick) "!")]
               [:h2 "Invitation"]
               [:p "In order to use this bot, you have to accept a chat "
                "invitation from it. This invitation could be send by "
                "clicking the button below: "]
               (common/button "/xmpp/register/" "Subscribe to a bot")
               [:p "After this procedure you can start chatting with a "
                "bot. You can find it in your XMPP client's contact list "
                "under a nickname " [:b "gtjbot@appspot.com"]]
               [:h2 "Bot Modules"]
               [:p "Here you can choose wich modules the bot uses."]
               [:h3 "Not yet available."]]
     :links links)))

