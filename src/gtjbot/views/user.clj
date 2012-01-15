;; ## Definitions of the pages for basic users
(ns gtjbot.views.user
    (:require [gtjbot.views.common :as common])
    (:use [noir.core :only [defpage url-for]]
          [hiccup.page-helpers :only [link-to]]
          [gtjbot.models.user :only [save-user-to-ds]]
          [gtjbot.utils.user :only [get-user-nick]]
          [appengine-magic.services.user :only [user-logged-in? user-admin?]]))


;; Main page of a site for the logged user.
(defpage user-main [:get "/user/"] []
  (save-user-to-ds)
  (let [links [(link-to (url-for index) "Main")]
        links (if (user-admin?)
                (conj links (link-to "/admin/main" "Admin Panel"))
                links)]
    (common/layout
     :content [[:h1 (str "Welcome to gtjbot, " (get-user-nick) "!")]
               [:p "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."]
               (common/button "/xmpp/register/" "Subscribe to a bot")]
     :links links)))

;; Index page of a site.
(defpage index [:get "/"] []
  (let [links (if (user-logged-in?)
                [(link-to (url-for user-main) "Profile")]
                [])
        links (if (and (user-logged-in?) (user-admin?))
                (conj links (link-to "/admin/main" "Admin Panel"))
                links)]
    (common/layout
     :content [[:h1 "App is under construction."]
               [:p "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."]]
     :links links)))