(ns #^{:doc "Definitions of pages for basic users."} gtjbot.views.user
  (:require [gtjbot.views.common :as common])
  (:use [noir.core :only [defpage]]
        [gtjbot.models.user :only [save-user-to-ds]]
        [gtjbot.utils.user :only [get-user-nick]]))

;; Main page of site
(defpage "/" []
  (save-user-to-ds)
  (common/gh-layout
   [:h1 (str "Welcome to gtjbot, " (get-user-nick) "!")]
   [:p "App is under construction."]))
