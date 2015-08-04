;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.views
  (:require [re-frame.core :as re-frame]))

;; --------------------
(defn tools-panel []
  (let [tools (re-frame/subscribe [:tools])]
    (fn []
      [:div
       [:ul (for [tool @tools]
              ^{:key (tool "id")} [:li (tool "name")])]
       [:div [:a {:on-click #(re-frame/dispatch [:get-tools])}
              "Refresh tools"]]])))

(defn about-panel []
  (fn []
    [:div "This is the About Page."
     [:div [:a {:href "#/"} "go to Home Page"]]]))

;; --------------------
(defmulti panels identity)
(defmethod panels :tools-panel [] [tools-panel])
(defmethod panels :about-panel [] [about-panel])
(defmethod panels :default [] [:div])

(defn main-panel []
  (let [active-panel (re-frame/subscribe [:active-panel])]
    (fn []
      (panels @active-panel))))
