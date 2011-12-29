(ns #^{:doc "Definitions of pages for admin."} gtjbot.views.admin
    (:require [gtjbot.views.common :as common]
              [gtjbot.models.user :as user]
              [clojure.pprint :as pp])
    (:use [noir.core :only [defpage]]
          [noir.response :only [redirect]]
          [hiccup.core :only [html]]))

;; Main page of admin panel
(defpage "/admin/main" []
  (common/layout
   [:h1 "Welcome to gtjbot admin panel!"]
   [:h2 "Users list:"]
   (common/u-list (user/get-users))))