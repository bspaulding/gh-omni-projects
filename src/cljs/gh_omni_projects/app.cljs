(ns gh-omni-projects.app
	(:refer-clojure :exclude [print])
	(:require-macros [cljs.core.async.macros :refer [go]])
	(:require [rum.core :as rum]
						[cljs.core.async :refer [<!]]
						[clojure.data :refer [diff]]
						[gh-omni-projects.api :as api]))

(defn print [& args]
	(.log js/console args))

(defonce state (atom {:projects []
											:projects/by-id {}
											:columns/by-id {}
											:columns/by-project-id {}
											:columns/by-name {}
											:loading false}))
;; selectors
(defn get-projects-list [state]
	(-> state :projects/by-id vals))

(rum/defc columns-list-item [column]
	[:li (:name column)])
(rum/defc project-columns-list [columns]
	[:ul (map columns-list-item columns)])
(rum/defc projects-list-item < {:key-fn :id} [project columns]
	[:li (:name project)
	 (project-columns-list columns)])
(rum/defc projects-list [projects columnsByProjectId]
	[:ul (map
				 (fn [project]
					 (projects-list-item project (get columnsByProjectId (:id project))))
				 projects)])
(rum/defc projects [state]
	[:div
   [:h1 "Projects"]
	 (projects-list (get-projects-list state) (:columns/by-project-id state))])
(rum/defc column-list-item [by-name]
	(print "column-list-item: " by-name)
	[:li (first by-name)])
(rum/defc columns [state]
	[:div
		[:h1 "Columns"]
		[:ul (map column-list-item (:columns/by-name state))]])
(rum/defc app [state]
  [:div
	 [:p (if (:loading state) "Loading...")]
	 (columns state)
	 (projects state)])

(defn render [state]
	(rum/mount (app state) (. js/document (getElementById "container"))))

(defn init [] (render @state))
(add-watch state :rerender
					 (fn [_ _ old-state new-state]
						 (let [diff (diff old-state new-state)
									 lost (first diff)
									 add  (second diff)]
							 (if lost (print  "- " (first diff)))
							 (if add (print  "+ " (second diff))))
						 (render new-state)))

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

(swap! state assoc :count 3)
