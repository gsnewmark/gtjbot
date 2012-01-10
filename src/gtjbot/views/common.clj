(ns #^{:doc "Definitions of common page elements."} gtjbot.views.common
  (:use [noir.core :only [defpartial]]
        [hiccup.page-helpers :only [include-css html5 link-to]]
        [appengine-magic.services.user :only [logout-url login-url
                                              user-logged-in?]]))

;; Ribbon for a GitHub page
(defpartial gh-ribbon [] [:a {:href "https://github.com/gsnewmark/gtjbot"}
                          [:image {:style "position: absolute; top: 0; right: 0; border: 0;" :src "https://a248.e.akamai.net/assets.github.com/img/4c7dc970b89fd04b81c8e221ba88ff99a06c6b61/687474703a2f2f73332e616d617a6f6e6177732e636f6d2f6769746875622f726962626f6e732f666f726b6d655f72696768745f77686974655f6666666666662e706e67" :alt "Fork me on GitHub"}]])

;; Element of a list.
(defpartial item [e] [:li e])

;; List itself.
(defpartial u-list [elems] [:ul (map item elems)])

;; Basic sidebar with links (elems - links to show in menu).
(defpartial links-sidebar [& elems]
  [:div#templatemo_sidebar
   [:div#templatemo_header [:img {:src "/img/logo.png"}]]
   [:ul.links
    (map item elems)
    [:li (if (user-logged-in?)
           (link-to (logout-url) "Logout")
           (link-to (login-url :destination "/user/") "Login"))]]])

;; Basic footer for pages.
(defpartial footer []
  [:div#templatemo_footer
   (link-to "https://twitter.com/#!/gsnewmark" "gsnewmark")
   " | "
   (link-to "http://www.templatemo.com" "Free CSS Templates")])

;; Basic block with main page content.
(defpartial main-block [& content]
  [:div#templatemo_main
   [:div#content [:div.scroll [:div.scrollContainer [:div.panel content]]]]
   (footer)])

;; Basic layout.
(defpartial layout [& {:keys [links content]}]
  (html5
   [:head
    [:title "gtjbot"]
    (include-css "/css/templatemo_style.css")
    (include-css "/css/coda-slider.css")]
   [:body [:div#slider
           (apply links-sidebar links)
           (apply main-block content)]
    (gh-ribbon)]))


