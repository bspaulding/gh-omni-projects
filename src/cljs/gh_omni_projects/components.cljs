(ns gh-omni-projects.components
	(:require [rum.core :as rum]
						[gh-omni-projects.selectors :refer [get-projects-list
																								get-columns-list
																								get-cards-list]]))

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
(rum/defc column-list-item [column]
	[:li (:name column)])
(rum/defc columns [state]
	[:div
		[:h1 "Columns"]
		[:ul (map column-list-item (get-columns-list state))]])
(rum/defc card-list-item < {:key-fn :id} [card]
	[:li (or (:note card)
					 (-> card :issue :title))])
(rum/defc cards [state]
	[:div
		[:h1 "Cards"]
		[:ul (map card-list-item (get-cards-list state))]])
(rum/defc app [state]
  [:div
	 [:p (if (:loading state) "Loading...")]
	 (cards state)
	 (columns state)
	 (projects state)])

