(ns #^{:doc "Definitions of models used in app and functions to use with them."}
  gtjbot.models.user
  (:require [appengine-magic.services.datastore :as ds])
  (:use [appengine-magic.services.user :only [current-user]]
        [gtjbot.utils.user :only [get-user-id]]))

(ds/defentity GUser [^:key id, user])

(defn save-user-to-ds
  "Saves current user to Datastore (if it not presented in it already)."
  []
  (let [id (get-user-id)]
    (or (ds/retrieve GUser id)
        (ds/save! (GUser. id (current-user))))))

(defn get-gusers
  "Retrieves a list of all GUsers saved in a DS."
  [] (ds/query :kind GUser))

(defn get-users
  "Retrieves a list of all Users saved as a GUsers in DS."
  [] (let [gusers (get-gusers)] (map #(:user %) gusers)))
  