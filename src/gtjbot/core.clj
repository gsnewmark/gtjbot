(ns gtjbot.core
  (:require [appengine-magic.core :as gae]
            [noir.util.gae :as noir-gae])
  (:use [ring.middleware params]
        [appengine-magic.multipart-params :only [wrap-multipart-params]]))

;; load existing routes
(require 'gtjbot.views.admin)
(require 'gtjbot.views.user)
(require 'gtjbot.views.xmpp)

;; define the appengine app
(gae/def-appengine-app gtjbot-app (wrap-multipart-params (wrap-params (noir-gae/gae-handler nil))))


