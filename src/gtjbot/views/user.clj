;; ## Definitions of the pages for basic users
(ns gtjbot.views.user
  (:require [gtjbot.views.common :as common]
            [noir.validation :as vali])
  (:use [clojure.string :only [join]]
        [noir.core :only [defpage url-for defpartial render]]
        [noir.response :only [redirect]]
        [hiccup.page-helpers :only [link-to]]
        [hiccup.form-helpers]
        [gtjbot.models.user :only [save-user-to-ds get-user-handlers
                                   update-user-handlers]]
        [gtjbot.utils.user :only [get-user-nick]]
        [gtjbot.utils.parser :only [generate-user-handlers handlers-list]]
        [appengine-magic.services.user :only [user-logged-in? user-admin?]]))


;; Index page of a site.
(defpage index [:get "/"] []
  (let [links (if (user-logged-in?)
                (let [links [(link-to (url-for user-main) "Profile")]]
                  (if (user-admin?)
                    (conj links (link-to "/admin/main" "Admin Panel"))
                    links))
                [])
        links (concat [(link-to (url-for index) "Main")] links)]
    (common/layout
     :content [[:h1 "App is still under construction."]
               [:h2 "App Overview"]
               [:p [:b "gtjbot"] " is a handy little information bot that "
                "you can use with your XMPP (e. g. Google Talk) client. "
                "It's main feature is going to be a customizability - "
                "you can choose which modules bot uses to 'talk' with you. "]
               (when-not (user-logged-in?)
                 [:p "More details could be found after "
                  (common/login-link "logging in") "."])]
     :links links)))

;; TODO add styling
;; Error message.
(defpartial error-item [[first-error]]
  [:div.error first-error])

;; Element in a customization menu.
(defpartial handlers-edit-menu-element [handler selected]
  (let [handler-name (:name (meta handler))]
    [:div#customization-menu
     (check-box handler-name selected "on")
     [:b handler-name]
     [:br]
     (vali/on-error (str handler-name " command") error-item)
     (label (str handler-name " command") "Command ")
     (text-field (str handler-name " command") (:command-word handler))]))

;; TODO style tweaks needed: remove bullets, add tint to a textfield,
;; center submit
;; Customization menu: list with editable user's handlers.
(defpartial handlers-edit-menu []
  (let [user-handlers (generate-user-handlers (get-user-handlers))
        handler-name (fn [h] (:name (meta h)))
        ;; Creates a map with pairs <Handler name> - <Actual handler>
        create-handlers-map (fn [hl] (apply hash-map
                                 (flatten
                                  (map #(conj (conj [] (handler-name %)) %) hl))))
        user-hm (create-handlers-map user-handlers)
        all-hm (create-handlers-map handlers-list)
        ;; Use command words from user preferences
        customized-handlers (vec (merge all-hm user-hm))
        ;; Names of currently activated handlers
        selected-handlers-names (set
                           (map first (filter #(contains? user-hm (first %))
                                              customized-handlers)))
        handlers-states
        (map #(contains? selected-handlers-names (first %)) customized-handlers)]
    (common/u-list
     (map handlers-edit-menu-element
          (map second customized-handlers) handlers-states))))

;; Main page of a site for the logged user.
(defpage user-main [:get "/user/profile"] {:as prefs}
  (when (empty? prefs) (save-user-to-ds))
  (let [links [(link-to (url-for index) "Main")
               (link-to (url-for user-main) "Profile")]
        links (if (user-admin?)
                (conj links (link-to "/admin/main" "Admin Panel"))
                links)]
    (common/layout
     :content [[:h1 (str "Welcome to gtjbot customization page, "
                         (get-user-nick) "!")]
               [:h2 "Invitation"]
               [:p "In order to use this bot, you have to accept a chat "
                "invitation from it. This invitation could be send by "
                "clicking the button below: "]
               (common/button "/xmpp/register/" "Subscribe to a bot")
               [:p "After this procedure you can start chatting with a "
                "bot. You can find it in your XMPP client's contact list "
                "under a nickname " [:b "gtjbot@appspot.com"]]
               [:h2 "Bot Modules"]
               [:p "Here you can choose which modules the bot uses:"]
               (form-to [:post "/user/profile"]
                        (handlers-edit-menu)
                        (submit-button "Save preferences"))]
     :links links)))

(defn- get-handlers-names
  "Returns a set of names of a given handlers."
  [handlers-list] (set (map #(:name (meta %)) handlers-list)))

(defn- get-intersected-names
  "Returns names presented in both lists"
  [names-list-1 names-list-2]
  (filter #(contains? names-list-1 %) names-list-2))

(defn- valid?
  "Checks whether the user's input was correct (commands consist of one word, each symbol of which is an alphanumeric character)."
  [{:as prefs}]
  (let [handlers-names (get-handlers-names handlers-list)
        selected-handlers-names (get-intersected-names handlers-names
                                                       (map first prefs))]
    (doall (map #(vali/rule (vali/min-length? (prefs (str % " command")) 1) [(str % " command") "Command word must have 1 or more alphanumeric symbols."]) selected-handlers-names))
    (not (apply vali/errors? (keys prefs)))))

(defn- generate-user-prefs-string 
  "Generates string with user's preferences used in a DataStore from a submitted form."
  [{:as prefs}]
  (let [handlers-names (get-handlers-names handlers-list)
        selected-handlers-names (get-intersected-names handlers-names
                                                       (map first prefs))
        selected-prefs-list (map #(str % " - " (prefs (str % " command")))
                                 selected-handlers-names)
        selected-prefs-string (join "; " selected-prefs-list)]
    selected-prefs-string))

;; Page that saves submitted preferences and redirects back to profile page.
(defpage user-save [:post "/user/profile"] {:as prefs}
  (do
    (if (valid? prefs)
      (do (update-user-handlers (generate-user-prefs-string prefs))
          (redirect (url-for user-main)))
      (render user-main prefs))))
