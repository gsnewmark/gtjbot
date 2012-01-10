(ns #^{:doc "Definitions of pages for basic users."} gtjbot.views.user
  (:require [gtjbot.views.common :as common])
  (:use [noir.core :only [defpage url-for pre-route]]
        [noir.response :only [redirect]]
        [gtjbot.models.user :only [save-user-to-ds]]
        [gtjbot.utils.user :only [get-user-nick]]
        [appengine-magic.services.user :only [login-url user-logged-in?]]))

;; Main page of a site for logged user.
(defpage user-main "/user/" []
  (save-user-to-ds)
  (common/gh-layout
   [:h1 (str "Welcome to gtjbot, " (get-user-nick) "!")]
   [:p "App is under construction."]
   [:a {:href "/xmpp/register/"} "Subscribe to a bot"]
   [:br]
   [:a {:href (url-for index)} "Main Page"]
   [:br]
   ))

;; Index page of a site.
(defpage index "/" []
  (common/gh-layout
   [:h1 "App is under construction."]
   (if (user-logged-in?)
     [:a {:href (url-for user-main)} "Profile"]
     [:a {:href (login-url :destination (url-for user-main))} "Login"])))