(ns #^{:doc "Definitions of common page elements."} gtjbot.views.common
  (:use [noir.core :only [defpartial]]
        [hiccup.page-helpers :only [include-css html5]]
        [appengine-magic.services.user :only [logout-url user-logged-in?]]))

;; Basic layout.
(defpartial layout [& content]
  (html5
   [:head
    [:title "gtjbot"]
    (include-css "/css/reset.css")]
   [:body [:div#wrapper content]]
   (if (user-logged-in?) [:a {:href (logout-url)} "Logout"])))

;; Ribbon for a GitHub page
(defpartial gh-ribbon [] [:a {:href "https://github.com/gsnewmark/gtjbot"}
                          [:image {:style "position: absolute; top: 0; right: 0; border: 0;" :src "https://a248.e.akamai.net/assets.github.com/img/7afbc8b248c68eb468279e8c17986ad46549fb71/687474703a2f2f73332e616d617a6f6e6177732e636f6d2f6769746875622f726962626f6e732f666f726b6d655f72696768745f6461726b626c75655f3132313632312e706e67" :alt "Fork me on GitHub"}]])

;; Basic logged layout with Github Ribbon.
(defpartial gh-layout [& content]
  (layout (gh-ribbon) content))

;; Element of a list.
(defpartial item [e] [:li e])

;; List itself.
(defpartial u-list [elems] [:ul (map item elems)])
