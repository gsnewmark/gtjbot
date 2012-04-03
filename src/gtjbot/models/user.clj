;; ## GoogleUser entity
;; Definition of user model used in app and functions to use with it.
(ns gtjbot.models.user
  (:require [appengine-magic.services.datastore :as ds])
  (:use [appengine-magic.services.user :only [current-user]]
        [gtjbot.utils.user :only [get-user-id get-user-email]]))

;; String with all existing handlers (default setting for new users).
(def default-handlers (str "HTTP Status Code - httpsc; "
                           "Current Weather - weather"))

;; Message sent to unsubscribed users who try to use a bot.
(def message-to-unsubscribed (str "You need to subscribe to an app in order to use this"
" feature. More details could be found at https://gtjbot.appspot.com"))

;; Main entity of an app - subscribed user. Consists of user's unique
;; identifier, user itself (instance of Google's User class), user's
;; mail (for easier filtering) and string with message handlers list.
(ds/defentity GoogleUser [^:key id, user, mail, handlers])

(defn save-user-to-ds
  "Saves current user to a DataStore (if it not presented in it already).
Returns user just created from a current user's ID or retrieves user saved
in a DataStore earlier."
  []
  (let [id (get-user-id)]
    (or (ds/retrieve GoogleUser id)
        (ds/save! (GoogleUser. id (current-user) (get-user-email)
                               default-handlers)))))

(defn get-gusers
  "Retrieves a list of all GoogleUsers saved in a DS."
  [] (ds/query :kind GoogleUser))

(defn get-users
  "Retrieves a list of all Users (User class instances) saved as a
GoogleUsers in a DS."
  [] (let [gusers (get-gusers)] (map #(:user %) gusers)))
  
(defn check-user-by-id
  "Checks whether the user specified by an ID exists in a DataStore."
  ([] (check-user-by-id (current-user)))
  ([user] (if (nil? (ds/retrieve GoogleUser (get-user-id user)))
            false
            true)))

(defn check-user-by-mail
  "Checks whether the user specified by a mail exists in a DataStore."
  ([] (check-user-by-mail (get-user-email)))
  ([mail] (if (empty? (ds/query :kind GoogleUser :filter (= :mail mail)))
            false
            true)))

;; TODO maybe use user not guser (test on dev_server)
(defn get-guser-by-mail
  "Returns user with a given mail."
  [mail]
  (let [guser (ds/query :kind GoogleUser :filter (= :mail mail))]
    (do
      (if (empty? guser)
       nil
       (first guser)))))

(defn get-guser-handlers
  "Returns list of handlers user wishes to use for a given user."
  ([] (get-guser-handlers (get-guser-by-mail (get-user-email (current-user)))))
  ([user]
     (if (nil? user)
       nil
       (:handlers user))))

(defn get-guser-handlers-for-mail
  "Returns list of handlers for a user with a given mail."
  [mail]
  (get-guser-handlers (get-guser-by-mail mail)))

(defn update-guser-handlers
  "Saves a given string as a user's new handlers."
  ([handlers-string] (update-guser-handlers
                      (get-guser-by-mail (get-user-email (current-user)))
                      handlers-string))
  ([user handlers-string]
     (if (nil? user)
       nil
       (ds/save! (assoc user :handlers handlers-string)))))

(defn update-guser-handlers-for-mail
  "Saves a given string as a new handlers for a user with a given mail."
  [mail handlers-string]
  (update-guser-handlers (get-guser-by-mail mail) handlers-string))