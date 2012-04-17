;; ## Definitions of the elements common to all pages
(ns gtjbot.views.common
  (:use [noir.core :only [defpartial]]
        [hiccup.page-helpers :only [include-css include-js html5 link-to]]
        [appengine-magic.services.user :only [logout-url login-url
                                              user-logged-in?]]))


;; Ribbon for a GitHub page
(defpartial gh-ribbon []
  [:a {:href "https://github.com/gsnewmark/gtjbot"}
   [:image
    {:style "position: absolute; top: 0; right: 0; border: 0;"
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

;; Fancy button to use for a link.
(defpartial button [link text]
  [:a {:href link} [:button text]])

;; Basic header.
(defpartial header [text] [:header [:h1 text]])

;; Login link.
(defpartial login-link [text]
  (link-to (login-url :destination "/user/profile") text))

;; Logout link.
(defpartial logout-link [text] (link-to (logout-url) text))

;; Basic bar with logo and links (elems - list of links to show in menu).
(defpartial links-menu [& elems]
  [:ul#menu
   (map item elems)
   [:li (link-to {:target "_blank"} "/doc.html" "Documentation")]
   [:li (if (user-logged-in?)
          (logout-link "Logout")
          (login-link "Login"))]])

;; Basic block with main page content.
(defpartial main-block [& content]
  [:div#content content])

;; Basic footer.
(defpartial footer []
  [:footer
   (link-to "mailto:gildraug@gmail.com?subject=gtjbot" "gsnewmark")
   " | Color palette from "
   (link-to "http://ethanschoonover.com/solarized" "Solarized")])

;; Basic page layout.
;; :links argument - links to put in a sidebar
;; :content argument - content to put in a main block
(defpartial layout [& {:keys [links content]}]
  (html5
   [:head
    [:title "gtjbot"]
    [:link {:rel "stylesheet/less" :type "text/css" :href "/css/gtjbot.less"}]
    (include-js "/js/less-1.2.1.min.js")]
   [:body
    (header "GTJBot - useful bot for your GoogleTalk")
    [:div#container
     (apply links-menu links)
     (apply main-block content)]
    (footer)
    (gh-ribbon)]))
