(ns gtjbot.core
  (:require [appengine-magic.core :as gae]
            [noir.util.gae :as noir-gae]))

;; load existing routes
(require 'gtjbot.views.admin)
(require 'gtjbot.views.user)
(require 'gtjbot.views.xmpp)

;; define the appengine app
(gae/def-appengine-app gtjbot-app (noir-gae/gae-handler nil))
