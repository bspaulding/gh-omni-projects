(ns gh-omni-projects.actions
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [gh-omni-projects.api :as api]
            [gh-omni-projects.selectors :refer [get-issues]]))

(defn index-by [key coll]
  (reduce
    (fn [v x]
      (assoc v (key x) x))
    {}
    coll))

(defn loadProjects [state]
  (go (swap! state assoc :loading true)
    (let [response (<! (api/getProjectsForRepo "spredfast" "mobile-conversations"))]
      (swap! state assoc
        :loading false
        :projects/by-id (index-by :id (:body response))))))

(defn loadColumnsForProject [state projectId]
  (go (let [response (<! (api/getColumnsForProject projectId))
            columns (:body response)]
        (doall (map (fn [column]
                     (swap! state assoc-in [:columns/by-id (:id column)] column))
                columns))
        (swap! state assoc-in
               [:columns/by-project-id projectId]
               columns))))

(defn loadColumnsForProjects [state]
  (doall (map
           (fn [projectId]
             (loadColumnsForProject state projectId))
           (doall (-> @state :projects/by-id keys)))))

(defn loadCardsForColumn [state columnId]
  (swap! state assoc :loading true)
  (go (let [response (<! (api/getCardsForColumn columnId))
            cards (:body response)
            cards-by-id (index-by :id cards)]
        (swap! state update-in [:cards/by-id]
               (fn [current] (merge current cards-by-id)))
        (swap! state assoc :loading false))))

(defn loadCardsForColumns [state]
  (doall (map
           (partial loadCardsForColumn state)
           (-> @state :columns/by-id keys))))

(defn loadIssueForCard [state cardId]
  (go (let [card (get (:cards/by-id @state) cardId)
            response (<! (api/getIssueForCard card))
            issue (:body response)]
        (js/console.log card)
        (swap! state assoc-in [:cards/by-id cardId :issue] issue))))

(defn loadIssuesForCards [state]
  (doall (map
           (partial loadIssueForCard state)
           (-> @state get-issues keys))))

(defn loadAll [state]
  (loadProjects state)
  (loadColumnsForProjects state)
  (loadCardsForColumns state)
  (loadIssuesForCards state))
