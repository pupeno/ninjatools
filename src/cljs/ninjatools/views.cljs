;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.views
  (:require [re-frame.core :as re-frame]
            [ninjatools.routes :as routes]
            [ninjatools.auth :as auth]
            [ninjatools.models.user-schema :as user-schema]
            [ninjatools.util :refer [log]]))

(defn dispatch [& args]
  (re-frame/dispatch [:human-interaction])
  (apply re-frame/dispatch args))

(defn loading
  "Display a loading panel"
  []
  [:div "Loading..."])

(defn about-panel []
  (fn []
    [:div "This is the About Page."]))

;; --------------------
(defmulti panels :name)
(defmethod panels :about [] [about-panel])
(defmethod panels :default [] [:div])

(defn nav-bar []
  (let [current-route (re-frame/subscribe [:current-route])
        current-user (re-frame/subscribe [:current-user])]
    (fn []
      [:nav.navbar.navbar-inverse.navbar-fixed-top
       [:div.container
        [:div.navbar-header
         [:button.navbar-toggle.collapsed {:type "button", :data-toggle "collapse", :data-target "#navbar", :aria-expanded "false", :aria-controls "navbar"}
          [:span.sr-only "Toggle navigation"]
          [:span.icon-bar]
          [:span.icon-bar]
          [:span.icon-bar]]
         [:a.navbar-brand {:href (routes/url-for :home)} "Ninja Tools"]]
        [:div#navbar.collapse.navbar-collapse
         [:ul.nav.navbar-nav
          [:li {:class (when (some #{(:name @current-route)} [:tools :tool]) "active")}
           [:a {:href (routes/url-for :tools)} "Tools"]]
          [:li {:class (when (= :about (:name @current-route)) "active")}
           [:a {:href (routes/url-for :about)} "About"]]]
         [:ul.nav.navbar-nav.navbar-right
          [:li.dropdown {:class (when (contains? #{:login :register} (:name @current-route)) "active")}
           [:a.dropdown-toggle {:href "#" :data-toggle "dropdown" :role "button" :aria-haspopup "true" :aria-expanded "false"}
            [:i.fa.fa-user]]
           (if @current-user
             [:ul.dropdown-menu
              [:li [:a (user-schema/display-name @current-user)]]
              [:li [:a {:on-click #(dispatch [:log-out])} "Log out"]]]
             [:ul.dropdown-menu
              [:li {:class (when (= :log-in (:name @current-route)) "active")}
               [:a {:href (routes/url-for :log-in)} "Log in"]]
              [:li {:class (when (= :register (:name @current-route)) "active")}
               [:a {:href (routes/url-for :register)} "Register"]]])]]]]])))

(defn main-panel []
  (let [current-route (re-frame/subscribe [:current-route])
        alerts (re-frame/subscribe [:alerts])]
    (fn []
      [:div
       [nav-bar]
       [:main.container
        (if (not (empty? @alerts))
          [:div.alerts
           (map (fn [[id alert]]
                  [:div {:key   id
                         :class (str "alert alert-" (name (:type alert)))
                         :role  "alert"}
                   (:message alert)
                   [:button {:type       "button"
                             :class      "close"
                             :aria-label "Close"
                             :on-click   #(dispatch [:remove-alert id])}
                    [:span {:aria-hidden true} [:i.fa.fa-times]]]])
                @alerts)])
        (panels @current-route)]])))
