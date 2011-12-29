(ns #^{:doc "Wrappers around Google's User java class."}
  gtjbot.utils.user
  (:use [appengine-magic.services.user :only [current-user]]))

(defn get-user-id
  "Returns ID of a specified user (or current user)."
  ([] (get-user-id (current-user)))
  ([user] (. user getUserId)))

(defn get-user-email
  "Returns email of a specified user (or current user)."
  ([] (get-user-email (current-user)))
  ([user] (. user getEmail)))

(defn get-user-nick
  "Returns nickname of a specified user (or current user)."
  ([] (get-user-nick (current-user)))
  ([user] (. user getNickname)))