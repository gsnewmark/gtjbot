;; ## Wrappers around Google's User java class
(ns gtjbot.utils.user
  (:use [appengine-magic.services.user :only [current-user]]))


(defn get-user-id
  "Returns ID of a specified user (or current user if no argument
 is supplied)."
  ([] (get-user-id (current-user)))
  ;; TODO remove or (hack to work in repl)
  ([user] (. user getUserId)))

(defn get-user-email
  "Returns email of a specified user (or current user if no argument
 is supplied)."
  ([] (get-user-email (current-user)))
  ([user] (. user getEmail)))

(defn get-user-nick
  "Returns nickname of a specified user (or current user if no argument
 is supplied)."
  ([] (get-user-nick (current-user)))
  ([user] (. user getNickname)))
