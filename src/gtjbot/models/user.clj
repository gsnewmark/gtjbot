(ns #^{:doc "Definitions of models used in app and functions to use with them."}
  gtjbot.models.user
  (:require [appengine-magic.services.datastore :as ds])
  (:use [appengine-magic.services.user :only [current-user]]
        [gtjbot.utils.user :only [get-user-id get-user-email]]))

;; Message sent to unsubscribed users who try to use bot.
(def message-to-unsubscribed "You need to subscribe to an app in order to use this feature. More details could be found at http://gtjbot.appspot.com")

;; Main entity of app - subscribed user.
(ds/defentity GoogleUser [^:key id, user, mail])

(defn save-user-to-ds
  "Saves current user to a DataStore (if it not presented in it already)."
  []
  (let [id (get-user-id)]
    (or (ds/retrieve GoogleUser id)
        (ds/save! (GoogleUser. id (current-user) (get-user-email))))))

(defn get-gusers
  "Retrieves a list of all GoogleUsers saved in a DS."
  [] (ds/query :kind GoogleUser))

(defn get-users
  "Retrieves a list of all Users saved as a GoogleUsers in a DS."
  [] (let [gusers (get-gusers)] (map #(:user %) gusers)))
  
(defn check-user-by-id
  "Checks whether the user specified by ID is in a DS."
  ([] (check-user-by-id (current-user)))
  ([user] (if (nil? (ds/retrieve GoogleUser (get-user-id user)))
            false
            true)))

(defn check-user-by-mail
  "Checks whether the user specified by mail is in a DS."
  ([] (check-user-by-mail (get-user-email)))
  ([mail] (if (empty? (ds/query :kind GoogleUser :filter (= :mail mail)))
            false
            true)))