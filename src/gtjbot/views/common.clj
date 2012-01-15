;; ## Definitions of the elements common to all pages
(ns gtjbot.views.common
  (:use [noir.core :only [defpartial]]
        [hiccup.page-helpers :only [include-css html5 link-to]]
        [appengine-magic.services.user :only [logout-url login-url
                                              user-logged-in?]]))


;; Ribbon for a GitHub page
(defpartial gh-ribbon [] [:a {:href "https://github.com/gsnewmark/gtjbot"}
                          [:image {:style "position: absolute; top: 0; right: 0; border: 0;"
                                   :src (str "https://a248.e.akamai.net/assets.github.com/img/71"
                                             "eeaab9d563c2b3c590319b398dd35683265e85/687474703a2"
                                             "f2f73332e616d617a6f6e6177732e636f6d2f6769746875622"
                                             "f726962626f6e732f666f726b6d655f72696768745f6772617"
                                             "95f3664366436642e706e67")
                                   :alt "Fork me on GitHub"}]])

;; Element of a HTML list.
(defpartial item [e] [:li e])

;; HTML unordered list.
(defpartial u-list [elems] [:ul (map item elems)])

;; Basic bar with logo and links (elems - list of links to show in menu).
(defpartial header [& elems]
  [:div#header
   [:img {:src "/img/logo.png"}]
   [:ul#menu
    (map item elems)
    [:li (if (user-logged-in?)
           (link-to (logout-url) "Logout")
           (link-to (login-url :destination "/user/") "Login"))]
    [:li (link-to "/doc.html" "Documentation")]]])

;; Basic footer.
(defpartial footer []
  [:div#footer
   (link-to "https://twitter.com/#!/gsnewmark" "gsnewmark")
   " | Color palette from "
   (link-to "http://ethanschoonover.com/solarized" "Solarized")])

;; Basic block with main page content.
(defpartial main-block [& content]
  [:div#content content])

;; Basic page layout.
;; :links argument - links to put in a sidebar
;; :content argument - content to put in a main block
(defpartial layout [& {:keys [links content]}]
  (html5
   [:head
    [:title "gtjbot"]
    (include-css "/css/my.css")]
   [:body [:div#container
           (apply header links)
           (apply main-block content)
           (footer)]
    (gh-ribbon)]))


