;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.tools
  (:require [re-frame.core :as re-frame]
            [ninjatools.handlers :as handlers]
            [ninjatools.views :as views]
            [ajax.core :as ajax]
            [ninjatools.routing :as routing]
            [reagent.ratom :as ratom :include-macros true]
            clojure.walk
            [ninjatools.human :as human]
            [ninjatools.util :as util]))

(defn add-tool [db tool]
  (-> db
      (assoc-in [:tools :by-id (:id tool)] tool)
      (assoc-in [:tools :by-slug (:slug tool)] tool)))

(defmethod handlers/display-page :home [_current-route db]
  (when (empty? (get-in db [:tools :by-id]))
    (re-frame/dispatch [:get-tools]))
  db)

(defmethod handlers/display-page :tools [_current-route db]
  (when (empty? (get-in db [:tools :by-id]))
    (re-frame/dispatch [:get-tools]))
  db)

(defmethod handlers/display-page :tool [current-route db]
  (re-frame/dispatch [:get-tool-with-integrations (:slug current-route)])
  (assoc db :current-tool-slug (:slug current-route)))

(re-frame/register-handler
  :get-tools
  (fn [db [_]]
    (ajax/GET "/api/v1/tools"
              {:handler       #(re-frame/dispatch [:got-tools %1])
               :error-handler util/report-unexpected-error})
    db))

(re-frame/register-handler
  :got-tools
  (fn [db [_ tools]]
    (let [tools (map clojure.walk/keywordize-keys tools)]
      (reduce add-tool db tools))))

(re-frame/register-handler
  :get-tool-with-integrations
  (fn [db [_ tool-slug tool-requested]]
    (if-let [tool (get-in db [:tools :by-slug tool-slug])]
      (when (empty? (:integration-ids tool))
        (ajax/GET (str "/api/v1/tools/" (:id tool) "/integrations")
                  {:handler       #(re-frame/dispatch [:got-integrations (:id tool) %1])
                   :error-handler util/report-unexpected-error}))
      (do (when (not tool-requested)
            (re-frame/dispatch [:get-tools]))               ; TODO: only get the tool we want, by slug.
          (re-frame/dispatch [:get-tool-with-integrations tool-slug true])))
    db))

(re-frame/register-handler
  :got-integrations
  (fn [db [_ tool-id integration-ids]]
    (let [tool (assoc (get-in db [:tools :by-id tool-id]) :integration-ids integration-ids)] ; TODO: get the tools that we have integration ids for when we stop getting all the tools all the time.
      (add-tool db tool))))

(re-frame/register-handler
  :mark-tool-as-used
  (fn [db [_ tool-id]]
    (update-in db [:tools-in-use] conj tool-id)))

(re-frame/register-sub
  :tools
  (fn [db _]
    (ratom/reaction (:tools @db))))

(re-frame/register-sub
  :tools-in-use
  (fn [db _]
    (ratom/reaction (:tools-in-use @db))))

(re-frame/register-sub
  :current-tool
  (fn [db _]
    (ratom/reaction (get-in @db [:tools :by-slug (get @db :current-tool-slug)]))))

(re-frame/register-sub
  :current-available-tools
  (fn [db _]
    (ratom/reaction
      (let [page-number (if-let [raw-page-number ((:query (:url (:current-route @db))) "p")]
                          (js/parseInt raw-page-number)
                          0)]
        {:tools       (doall (take 10 (drop (* 10 page-number)
                                            (filter #(not (contains? (:tools-in-use @db) (:id %)))
                                                    (vals (:by-id (:tools @db)))))))
         :page-number page-number}))))

(defn home-page []
  (let [tools (re-frame/subscribe [:tools])
        current-available-tools (re-frame/subscribe [:current-available-tools])
        tools-in-use (re-frame/subscribe [:tools-in-use])]
    (fn []
      [:div
       [:div "Select the tools you use"]
       (if (empty? (:tools @current-available-tools))
         [views/loading]
         [:div
          [:ul (for [tool (:tools @current-available-tools)]
                 ^{:key (:id tool)}
                 [:li [:a {:on-click #(human/dispatch [:mark-tool-as-used (:id tool)])} (:name tool)]])]
          [:div [:a {:href (str (routing/url-for :home) "?p=" (inc (:page-number @current-available-tools)))} "more tools"]]
          (if (not (empty? @tools-in-use))
            [:div
             [:div "Your tools"]
             [:ul (for [tool (doall (map #(get-in @tools [:by-id %]) @tools-in-use))]
                    ^{:key (:id tool)}
                    [:li (:name tool)])]])])])))

(defmethod views/pages :home [] [home-page])

(defn tools-page []
  (let [tools (re-frame/subscribe [:tools])]
    (fn []
      (if (empty? (:by-id @tools))
        [views/loading]
        [:div
         [:ul (for [tool (vals (:by-id @tools))]
                ^{:key (:id tool)} [:li [:a {:href (routing/url-for :tool {:slug (:slug tool)})} (:name tool)]])]
         [:div [:a {:on-click #(human/dispatch [:get-tools])}
                "Refresh tools"]]]))))

(defmethod views/pages :tools [] [tools-page])

(defn tool-page []
  (let [current-tool (re-frame/subscribe [:current-tool])
        tools (re-frame/subscribe [:tools])]
    (fn []
      (if @current-tool
        [:div
         [:h1 (:name @current-tool)]
         [:ul (for [integrated-tool (vals (select-keys (:by-id @tools) (:integration-ids @current-tool)))]
                ^{:key (:id integrated-tool)} [:li [:a {:href (routing/url-for :tool {:slug (:slug integrated-tool)})} (:name integrated-tool)]])]]
        [views/loading]))))

(defmethod views/pages :tool [] [tool-page])
