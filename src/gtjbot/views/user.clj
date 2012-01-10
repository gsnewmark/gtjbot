(ns #^{:doc "Definitions of pages for basic users."} gtjbot.views.user
    (:require [gtjbot.views.common :as common])
    (:use [noir.core :only [defpage url-for]]
          [hiccup.page-helpers :only [link-to]]
          [gtjbot.models.user :only [save-user-to-ds]]
          [gtjbot.utils.user :only [get-user-nick]]
          [appengine-magic.services.user :only [user-logged-in? user-admin?]]))

;; Main page of a site for logged user.
(defpage user-main "/user/" []
  (save-user-to-ds)
  (let [links [(link-to "/xmpp/register/" "Subscribe to a bot")
               (link-to (url-for index) "Main")]
        links (if (user-admin?)
                (conj links (link-to "/admin/main" "Admin Panel"))
                links)]
    (common/layout
     :content [[:h1 (str "Welcome to gtjbot, " (get-user-nick) "!")]
               [:p "App is under construction."]]
     :links links)))

;; Index page of a site.
(defpage index "/" []
  (let [links (if (user-logged-in?)
                [(link-to (url-for user-main) "Profile")]
                [])
        links (if (and (user-logged-in?) (user-admin?))
                (conj links (link-to "/admin/main" "Admin Panel"))
                links)]
    (common/layout
     :content [[:h1 "App is under construction."]]
     :links links)))