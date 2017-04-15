(ns gh-omni-projects.actions
	(:require-macros [cljs.core.async.macros :refer [go]])
	(:require [gh-omni-projects.api :as api]))

(defn index-by [key coll]
	(reduce
		(fn [v x]
			(assoc v (key x) x))
		{}
		coll))

(defn loadProjects [state]
	(go (swap! state assoc :loading true)
			(let [response (<! (api/getProjectsForRepo "spredfast" "mobile-conversations"))]
				(swap! state assoc :loading false)
				(swap! state assoc :projects/by-id (index-by :id (:body response))))))

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
						 (print projectId)
						 (loadColumnsForProject state projectId))
					 (doall (-> @state :projects/by-id keys)))))
;; (doall
;; 	(map
;; 		(fn [project]
;; 			(let [projectId (:id project)]
;; 				(print "getting columns for " projectId)
;; 				(go (let [response (<! (api/getColumnsForProject projectId))]
;; 							 (swap! state assoc-in [:columns/by-project-id projectId] (:body response))))))
;; 		(:projects @state)))
;; (print (:columns/by-project-id @state))

;; (def columns-v (reduce into [] (map second (:columns/by-project-id @state))))
;; (def columns-by-name
;; 	(reduce
;; 		(fn [by-name column]
;; 			(let [name (:name column)]
;; 				(update by-name name (fn [cs] (conj (or cs []) (:id column))))))
;; 		(array-map)
;; 		columns-v))
;; (print columns-by-name)
;; (swap! state assoc :columns/by-name columns-by-name)

;; (def columnId 822867)
;; (go (let [response (<! (api/getCardsForColumn columnId))]
;; 			(print (:body response))
;; 			(def cards-by-id
;; 				(reduce
;; 					(fn [by-id card] (assoc by-id (:id card) card))
;; 					{}
;; 					(:body response)))
;; 			()
;; 			(swap! state assoc :cards/by-id cards-by-id)))

