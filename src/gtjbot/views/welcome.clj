(ns gtjbot.views.welcome
  (:require [gtjbot.views.common :as common])
  (:use [noir.core :only [defpage]]
        [hiccup.core :only [html]]))

(defpage "/" []
         (common/layout
           [:h1 "Welcome to gtjbot!"]
           [:p "App is under construction."]))
