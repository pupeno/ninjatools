;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.tools
  (:require [clojure.walk :as walk]
            [reagent.ratom :as ratom :include-macros true]
            [re-frame.core :as re-frame]
            [ajax.core :as ajax]
            [ninjatools.layout :as layout]
            [ninjatools.routing :as routing]
            [ninjatools.ui :as ui]
            [ninjatools.util :as util :refer [dissoc-in]]))

(defn add-tool [db tool]
  (-> db
      (assoc-in [:tools :by-id (:id tool)] tool)
      (assoc-in [:tools :by-slug (:slug tool)] tool)))

(defmethod routing/display-page :home [_current-route db]
  (when (empty? (get-in db [:tools :by-id]))
    (re-frame/dispatch [:get-tools])
    (re-frame/dispatch [:get-used-tools]))
  db)

(defmethod routing/display-page :tools [_current-route db]
  (when (empty? (get-in db [:tools :by-id]))
    (re-frame/dispatch [:get-tools]))
  db)

(defmethod routing/display-page :tool [current-route db]
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
    (let [tools (map walk/keywordize-keys tools)]
      (reduce add-tool db tools))))

(re-frame/register-handler
  :get-used-tools
  (fn [db [_]]
    (ajax/GET "/api/v1/used-tools"
              {:handler       #(re-frame/dispatch [:got-used-tools %1])
               :error-handler util/report-unexpected-error})
    db))

(re-frame/register-handler
  :got-used-tools
  (fn [db [_ used-tools]]
    (assoc db :used-tools (set used-tools))))

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
    (let [db (update-in db [:used-tools] conj tool-id)]
      (ajax/PUT "/api/v1/used-tools"
                {:params        (:used-tools db)
                 :handler       #(re-frame/dispatch [:got-used-tools %1])
                 :error-handler util/report-unexpected-error})
      db)))

(re-frame/register-handler
  :mark-tool-as-unused
  (fn [db [_ tool-id]]
    (let [db (update-in db [:used-tools] disj tool-id)]
      (ajax/DELETE (str "/api/v1/used-tools/" tool-id)
                   {:handler       #(re-frame/dispatch [:got-used-tools %1])
                    :error-handler util/report-unexpected-error})
      db)))

(re-frame/register-sub
  :tools
  (fn [db _]
    (ratom/reaction (:tools @db))))

(re-frame/register-sub
  :used-tools
  (fn [db _]
    (ratom/reaction (:used-tools @db))))

(re-frame/register-sub
  :current-tool
  (fn [db _]
    (ratom/reaction (get-in @db [:tools :by-slug (get @db :current-tool-slug)]))))

(re-frame/register-sub
  :current-available-tools
  (fn [db _]
    (ratom/reaction
      (if (:tools @db)
        (let [tools-not-in-use (filter #(not (contains? (:used-tools @db) (:id %)))
                                       (vals (:by-id (:tools @db))))
              tools-per-page 10
              number-of-pages (Math.ceil (/ (count tools-not-in-use) tools-per-page))
              page-number (if-let [raw-page-number ((:query (:url (:current-route @db))) "p")]
                            (js/parseInt raw-page-number)
                            1)]
          {:tools           (doall (take tools-per-page (drop (* tools-per-page (dec page-number))
                                                              tools-not-in-use)))
           :page-number     page-number
           :number-of-pages number-of-pages})
        nil))))

(defn home-page []
  (let [tools (re-frame/subscribe [:tools])
        current-available-tools (re-frame/subscribe [:current-available-tools])
        used-tools (re-frame/subscribe [:used-tools])]
    (fn []
      [:div
       (if (nil? (:tools @current-available-tools))
         [ui/loading]
         [:div
          [:div "Select the tools you use"]
          [:ul (for [tool (:tools @current-available-tools)]
                 ^{:key (:id tool)}
                 [:li [:a {:on-click #(ui/dispatch % [:mark-tool-as-used (:id tool)])} (:name tool)]])]
          [:div [:a {:href (str (routing/url-for :home) "?p=" (if (= (:page-number @current-available-tools) (:number-of-pages @current-available-tools))
                                                                1
                                                                (inc (:page-number @current-available-tools))))}
                 "more tools"]]
          (when (and (:tools @current-available-tools) (not (empty? @used-tools)))
            [:div
             [:div "Your tools"]
             [:ul (for [tool (doall (filter identity (map #(get-in @tools [:by-id %]) @used-tools)))]
                    ^{:key (:id tool)}
                    [:li (:name tool) " "
                     [:a {:on-click #(ui/dispatch % [:mark-tool-as-unused (:id tool)])} "x"]])]])])])))

(defmethod layout/pages :home [] [home-page])

(defn tools-page []
  (let [tools (re-frame/subscribe [:tools])]
    (fn []
      (if (nil? (:by-id @tools))
        [ui/loading]
        [:div
         [:ul (for [tool (vals (:by-id @tools))]
                ^{:key (:id tool)} [:li [:a {:href (routing/url-for :tool {:slug (:slug tool)})} (:name tool)]])]
         [:div [:a {:on-click #(ui/dispatch % [:get-tools])}
                "Refresh tools"]]]))))

(defmethod layout/pages :tools [] [tools-page])

(defn tool-page []
  (let [current-tool (re-frame/subscribe [:current-tool])
        tools (re-frame/subscribe [:tools])]
    (fn []
      (if @current-tool
        [:div
         [:h1 (:name @current-tool)]
         [:ul (for [integrated-tool (vals (select-keys (:by-id @tools) (:integration-ids @current-tool)))]
                ^{:key (:id integrated-tool)} [:li [:a {:href (routing/url-for :tool {:slug (:slug integrated-tool)})} (:name integrated-tool)]])]]
        [ui/loading]))))

(defmethod layout/pages :tool [] [tool-page])
