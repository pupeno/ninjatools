;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(ns ninjatools.tools
  (:require [clojure.string :as s]
            [clojure.walk :as walk]
            [reagent.ratom :as ratom :include-macros true]
            [re-frame.core :as re-frame]
            [ajax.core :as ajax]
            [ninjatools.layout :as layout]
            [ninjatools.routing :as routing]
            [ninjatools.ui :as ui]
            [ninjatools.util :as util :refer [dissoc-in println-ret]]))

(defmethod routing/display-page :home [_current-route db]
  (when (empty? (get-in db [:tools :by-id]))
    (re-frame/dispatch [:get-tools])
    (re-frame/dispatch [:get-features])
    (re-frame/dispatch [:get-used-tools])
    (re-frame/dispatch [:get-wanted-features]))
  db)

(defmethod routing/display-page :tools [_current-route db]
  (when (empty? (get-in db [:tools :by-id]))
    (re-frame/dispatch [:get-tools]))
  db)


(defn add-tool [db tool]
  (-> db
      (assoc-in [:tools :by-id (:id tool)] tool)
      (assoc-in [:tools :by-slug (:slug tool)] tool)))

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

(re-frame/register-sub
  :tools
  (fn [db _]
    (ratom/reaction (:tools @db))))

(defn add-feature [db feature]
  (-> db
      (assoc-in [:features :by-id (:id feature)] feature)
      (assoc-in [:features :by-slug (:id feature)] feature)))

(re-frame/register-handler
  :get-features
  (fn [db [_]]
    (ajax/GET "/api/v1/features"
              {:handler       #(re-frame/dispatch [:got-features (walk/keywordize-keys %1)])
               :error-handler util/report-unexpected-error})
    db))

(re-frame/register-handler
  :got-features
  (fn [db [_ features]]
    (reduce add-feature db features)))

(re-frame/register-sub
  :features
  (fn [db _]
    (ratom/reaction (:features @db))))

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
    (re-frame/dispatch [:get-suggested-tools])
    (assoc db :used-tools (set used-tools))))

(re-frame/register-handler
  :mark-tool-as-used
  (fn [db [_ tool-id]]
    (let [db (update-in db [:used-tools] #(conj (or %1 #{}) %2) tool-id)]
      (ajax/PUT "/api/v1/used-tools"
                {:params        tool-id
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

(re-frame/register-sub
  :used-tools
  (fn [db _]
    (ratom/reaction (:used-tools @db))))

(re-frame/register-handler
  :get-wanted-features
  (fn [db [_]]
    (ajax/GET "/api/v1/wanted-features"
              {:handler       #(re-frame/dispatch [:got-wanted-features %1])
               :error-handler util/report-unexpected-error})
    db))

(re-frame/register-handler
  :got-wanted-features
  (fn [db [_ wanted-features]]
    (re-frame/dispatch [:get-suggested-tools])
    (assoc db :wanted-features (set wanted-features))))

(re-frame/register-handler
  :mark-feature-as-wanted
  (fn [db [_ feature-id]]
    (let [db (update-in db [:wanted-features] #(conj (or %1 #{}) %2) feature-id)]
      (ajax/PUT "/api/v1/wanted-features"
                {:params        feature-id
                 :handler       #(re-frame/dispatch [:got-wanted-features %1])
                 :error-handler util/report-unexpected-error})
      db)))

(re-frame/register-handler
  :mark-feature-as-unwanted
  (fn [db [_ feature-id]]
    (let [db (update-in db [:wanted-features] disj feature-id)]
      (ajax/DELETE (str "/api/v1/wanted-features/" feature-id)
                   {:handler       #(re-frame/dispatch [:got-wanted-features %1])
                    :error-handler util/report-unexpected-error})
      db)))

(re-frame/register-sub
  :wanted-features
  (fn [db _]
    (ratom/reaction (:wanted-features @db))))

(re-frame/register-sub
  :current-available-features
  (fn [db _]
    (ratom/reaction
      (if (:features @db)
        (let [features-not-wanted (filter #(not (contains? (:wanted-features @db) (:id %)))
                                          (vals (:by-id (:features @db))))
              features-per-page 10
              number-of-pages (Math.ceil (/ (count features-not-wanted) features-per-page))
              page-number (if-let [raw-page-number ((:query (:url (:current-route @db))) "p")]
                            (js/parseInt raw-page-number)
                            1)]
          {:features        (doall (take features-per-page (drop (* features-per-page (dec page-number))
                                                                 features-not-wanted)))
           :page-number     page-number
           :number-of-pages number-of-pages})
        nil))))

(defmethod layout/pages :home [_]
  (let [tools (re-frame/subscribe [:tools])
        features (re-frame/subscribe [:features])
        current-available-tools (re-frame/subscribe [:current-available-tools])
        used-tools (re-frame/subscribe [:used-tools])
        wanted-features (re-frame/subscribe [:wanted-features])
        suggested-tools (re-frame/subscribe [:suggested-tools])]
    (fn [_]
      [:div
       (if (nil? (:tools @current-available-tools))
         [ui/loading]
         [:div
          [:h1 "What tools do you use?"]
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
                     [:a {:on-click #(ui/dispatch % [:mark-tool-as-unused (:id tool)])} "x"]])]])

          [:h1 "What do you need?"]
          [:ul (for [feature (vals (:by-id @features))]
                 ^{:key (:id feature)}
                 [:li [:a {:on-click #(ui/dispatch % [:mark-feature-as-wanted (:id feature)])} (:name feature)]])]
          (when (and #_(:features @current-available-features) (not (empty? @wanted-features)))
            [:div
             [:div "Your desires"]
             [:ul (for [feature (doall (filter identity (map #(get-in @features [:by-id %]) @wanted-features)))]
                    ^{:key (:id feature)}
                    [:li (:name feature) " "
                     [:a {:on-click #(ui/dispatch % [:mark-feature-as-unwanted (:id feature)])} "x"]])]])
          [:h1 "Suggested tools"]
          (when (not (empty? @suggested-tools))
            [:div
             [:ul (for [[feature suggested-tools] (group-by :feature @suggested-tools)]
                    ^{:key feature}
                    [:div
                      [:span feature]
                      [:ul (for [tool suggested-tools]
                           ^{:key (:id tool)}
                           [:li (:name tool) ": " (s/join ", " (map :name (vals (select-keys (:by-id @tools) (:integration-ids tool)))))])]])]
             ])
          ])])))

(defmethod layout/pages :tools [_]
  (let [tools (re-frame/subscribe [:tools])]
    (fn [_]
      (if (nil? (:by-id @tools))
        [ui/loading]
        [:div
         [:ul (for [tool (vals (:by-id @tools))]
                ^{:key (:id tool)} [:li [:a {:href (routing/url-for :tool {:slug (:slug tool)})} (:name tool)]])]
         [:div [:a {:on-click #(ui/dispatch % [:get-tools])}
                "Refresh tools"]]]))))

(re-frame/register-sub
  :current-tool
  (fn [db _]
    (ratom/reaction (get-in @db [:tools :by-slug (get @db :current-tool-slug)]))))

(defmethod routing/display-page :tool [current-route db]
  (re-frame/dispatch [:get-tool-with-integrations (:slug current-route)])
  (assoc db :current-tool-slug (:slug current-route)))

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

(defmethod layout/pages :tool [_]
  (let [current-tool (re-frame/subscribe [:current-tool])
        tools (re-frame/subscribe [:tools])]
    (fn [_]
      (if @current-tool
        [:div
         [:h1 (:name @current-tool)]
         [:ul (for [integrated-tool (vals (select-keys (:by-id @tools) (:integration-ids @current-tool)))]
                ^{:key (:id integrated-tool)} [:li [:a {:href (routing/url-for :tool {:slug (:slug integrated-tool)})} (:name integrated-tool)]])]]
        [ui/loading]))))

(re-frame/register-sub
  :suggested-tools
  (fn [db _]
    (ratom/reaction (:suggested-tools @db))))

(re-frame/register-handler
  :get-suggested-tools
  (fn [db [_ feature_id tool_ids]]
    (ajax/GET "/api/v1/suggested-tools"
              {:handler       #(re-frame/dispatch [:got-suggested-tools %1])
               :error-handler util/report-unexpected-error})
    db))

(re-frame/register-handler
  :got-suggested-tools
  (fn [db [_ suggested-tools]]
    (assoc db :suggested-tools (set (map walk/keywordize-keys suggested-tools)))))
