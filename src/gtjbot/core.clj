(ns gtjbot.core
  (:require [appengine-magic.core :as gae]
            [noir.util.gae :as noir-gae]))

(require 'gtjbot.views.welcome)

;; def the appengine app
(gae/def-appengine-app gtjbot-app (noir-gae/gae-handler nil))
