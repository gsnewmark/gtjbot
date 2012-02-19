;; ## Definition of an app
(ns gtjbot.core
  (:require [appengine-magic.core :as gae]
            [noir.util.gae :as noir-gae])
  (:use [ring.middleware params]
        [appengine-magic.multipart-params :only [wrap-multipart-params]]))


;; Prelooads existing routes (pages).
(require 'gtjbot.views.admin)
(require 'gtjbot.views.user)
(require 'gtjbot.views.xmpp)

;; Define the appengine app from noir handler (GAE-compatible one).
(gae/def-appengine-app gtjbot-app
  (wrap-multipart-params (wrap-params (noir-gae/gae-handler nil))))
