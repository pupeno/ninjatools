;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.views
  (:require [re-frame.core :as re-frame]))

(defn tools-panel []
  (let [tools (re-frame/subscribe [:tools])]
    (fn []
      [:div
       [:ul (for [tool (vals (:all @tools))]
              ^{:key (tool "id")} [:li [:a {:href (str "/#/tool/" (tool "slug"))} (tool "name")]])]
       [:div [:a {:on-click #(re-frame/dispatch [:get-tools])}
              "Refresh tools"]]])))

(defn tool-panel []
  (let [current-tool (re-frame/subscribe [:current-tool])]
    (fn []
      [:h1 (@current-tool "name")])))

(defn about-panel []
  (fn []
    [:div "This is the About Page."
     [:div [:a {:href "#/"} "go to Home Page"]]]))

;; --------------------
(defmulti panels identity)
(defmethod panels :tools-panel [] [tools-panel])
(defmethod panels :tool-panel [] [tool-panel])
(defmethod panels :about-panel [] [about-panel])
(defmethod panels :default [] [:div])

(defn main-panel []
  (let [active-panel (re-frame/subscribe [:active-panel])]
    (fn []
      [:div
       [:nav.navbar.navbar-inverse.navbar-fixed-top
        [:div.container
         [:div.navbar-header
          [:button.navbar-toggle.collapsed {:type "button", :data-toggle "collapse", :data-target "#navbar", :aria-expanded "false", :aria-controls "navbar"}
           [:span.sr-only "Toggle navigation"]
           [:span.icon-bar]
           [:span.icon-bar]
           [:span.icon-bar]]
          [:a.navbar-brand {:href "#"} "Ninja Tools"]]
         [:div#navbar.collapse.navbar-collapse
          [:ul.nav.navbar-nav
           #_[:li {:class "active"}
              [:a {:href "#/"} "Tools"]]
           #_[:li
              [:a {:href "#/about"} "About"]]]]]]
       [:main.container
        (panels @active-panel)]])))