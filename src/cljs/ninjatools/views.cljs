;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.views
  (:require [re-frame.core :as re-frame]
            [ninjatools.routes :as routes]
            [ninjatools.util :refer [log]]))

(defn loading
  "Display a loading panel"
  []
  [:div "Loading..."])

(defn tools-panel []
  (let [tools (re-frame/subscribe [:tools])]
    (fn []
      (if (empty? (:data @tools))
        [loading]
        [:div
         [:ul (for [tool (vals (:data @tools))]
                ^{:key (:id tool)} [:li [:a {:href (routes/url-for :tool {:slug (:slug tool)})} (:name tool)]])]
         [:div [:a {:on-click #(re-frame/dispatch [:get-tools])}
                "Refresh tools"]]]))))

(defn tool-panel []
  (let [current-tool (re-frame/subscribe [:current-tool])
        tools (re-frame/subscribe [:tools])]
    (fn []
      (if @current-tool
        [:div
         [:h1 (:name @current-tool)]
         [:ul (for [integrated-tool (vals (select-keys (:data @tools) (:integration-ids @current-tool)))]
                ^{:key (:id integrated-tool)} [:li [:a {:href (routes/url-for :tool {:slug (:slug integrated-tool)})} (:name integrated-tool)]])]]
        [loading]))))

(defn about-panel []
  (fn []
    [:div "This is the About Page."]))

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
          [:a.navbar-brand {:href "/"} "Ninja Tools"]]
         [:div#navbar.collapse.navbar-collapse
          [:ul.nav.navbar-nav
           #_[:li {:class "active"}
              [:a {:href "#/"} "Tools"]]
           [:li
              [:a {:href (routes/url-for :about)} "About"]]]]]]
       [:main.container
        (panels @active-panel)]])))
