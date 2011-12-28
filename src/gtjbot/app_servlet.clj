(ns gtjbot.app_servlet
  (:gen-class :extends javax.servlet.http.HttpServlet)
  (:use gtjbot.core)
  (:use [appengine-magic.servlet :only [make-servlet-service-method]]))


(defn -service [this request response]
  ((make-servlet-service-method gtjbot-app) this request response))
