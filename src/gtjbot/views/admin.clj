;; ## Definitions of the admin panel pages
(ns gtjbot.views.admin
  (:require [gtjbot.views.common :as common]
            [gtjbot.models.user :as user]
            [gtjbot.views.user :as user-views])
  (:use [noir.core :only [defpage url-for]]
        [hiccup.page-helpers :only [link-to]]))


;; Main page of an admin panel.
(defpage admin-panel [:get "/admin/main"] []
  (common/layout
   :content [[:h1 "Welcome to gtjbot admin panel!"]
             [:h2 "Users list:"]
             (common/u-list (user/get-users))]
   :links [(link-to (url-for user-views/index) "Main")
           (link-to (url-for user-views/user-main) "Profile")]))
