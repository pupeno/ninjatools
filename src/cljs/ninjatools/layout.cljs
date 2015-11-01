;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.layout
  (:require [re-frame.core :as re-frame]
            [ninjatools.models.user-schema :as user-schema]
            [ninjatools.routing :as routing]
            [ninjatools.alerts :as alerts]
            [ninjatools.util :refer [log]]
            [ninjatools.ui :as ui]))


;; --------------------
(defmulti pages :name)
(defmethod pages :default [_]
  [:div [:h1 "Page not found"]])

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
         [:a.navbar-brand {:href (routing/url-for :home)} "Ninja Tools"]]
        [:div#navbar.collapse.navbar-collapse
         [:ul.nav.navbar-nav
          [:li {:class (when (some #{(:name @current-route)} [:tools :tool]) "active")}
           [:a {:href (routing/url-for :tools)} "Tools"]]
          [:li {:class (when (= :about (:name @current-route)) "active")}
           [:a {:href (routing/url-for :about)} "About"]]]
         [:ul.nav.navbar-nav.navbar-right
          [:li.dropdown {:class (when (contains? #{:log-in :register :reset-password :change-password} (:name @current-route)) "active")}
           [:a.dropdown-toggle {:href "#" :data-toggle "dropdown" :role "button" :aria-haspopup "true" :aria-expanded "false"}
            [:i.fa.fa-user]]
           (if @current-user
             [:ul.dropdown-menu
              [:li [:a (user-schema/display-name @current-user)]]
              [:li [:a {:href (routing/url-for :change-password)} "Change Password"]]
              [:li [:a {:on-click #(ui/dispatch % [:log-out])} "Log out"]]]
             [:ul.dropdown-menu
              [:li {:class (when (= :log-in (:name @current-route)) "active")}
               [:a {:href (routing/url-for :log-in)} "Log in"]]
              [:li {:class (when (= :register (:name @current-route)) "active")}
               [:a {:href (routing/url-for :register)} "Register"]]
              [:li {:class (when (= :reset-password (:name @current-route)) "active")}
               [:a {:href (routing/url-for :reset-password)} "Reset Password"]]])]]]]])))

(defn main-panel []
  (let [current-route (re-frame/subscribe [:current-route])]
    (fn []
      [:div
       [nav-bar]
       [:main.container
        [alerts/view]
        ^{:key @current-route} [pages @current-route]]])))
