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

(defn home-panel []
  (let [tools (re-frame/subscribe [:tools])
        current-available-tools (re-frame/subscribe [:current-available-tools])
        tools-in-use (re-frame/subscribe [:tools-in-use])]
    (fn []
      [:div
       [:div "Select the tools you use"]
       (if (empty? (:tools @current-available-tools))
         [loading]
         [:div
          [:ul (for [tool (:tools @current-available-tools)]
                 ^{:key (:id tool)}
                 [:li [:a {:on-click #(dispatch [:mark-tool-as-used (:id tool)])} (:name tool)]])]
          [:div [:a {:href (str (routes/url-for :home) "?p=" (inc (:page-number @current-available-tools)))} "more tools"]]
          (if (not (empty? @tools-in-use))
            [:div
             [:div "Your tools"]
             [:ul (for [tool (doall (map #(get-in @tools [:by-id %]) @tools-in-use))]
                    ^{:key (:id tool)}
                    [:li (:name tool)])]])])])))

(defn tools-panel []
  (let [tools (re-frame/subscribe [:tools])]
    (fn []
      (if (empty? (:by-id @tools))
        [loading]
        [:div
         [:ul (for [tool (vals (:by-id @tools))]
                ^{:key (:id tool)} [:li [:a {:href (routes/url-for :tool {:slug (:slug tool)})} (:name tool)]])]
         [:div [:a {:on-click #(dispatch [:get-tools])}
                "Refresh tools"]]]))))

(defn tool-panel []
  (let [current-tool (re-frame/subscribe [:current-tool])
        tools (re-frame/subscribe [:tools])]
    (fn []
      (if @current-tool
        [:div
         [:h1 (:name @current-tool)]
         [:ul (for [integrated-tool (vals (select-keys (:by-id @tools) (:integration-ids @current-tool)))]
                ^{:key (:id integrated-tool)} [:li [:a {:href (routes/url-for :tool {:slug (:slug integrated-tool)})} (:name integrated-tool)]])]]
        [loading]))))

(defn about-panel []
  (fn []
    [:div "This is the About Page."]))

;; --------------------
(defmulti panels :name)
(defmethod panels :home [] [home-panel])
(defmethod panels :tools [] [tools-panel])
(defmethod panels :tool [] [tool-panel])
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
