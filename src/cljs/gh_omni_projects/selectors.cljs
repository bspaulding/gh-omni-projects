(ns gh-omni-projects.selectors)

(defn get-projects-list [state]
	(-> state :projects/by-id vals))

(defn get-columns-list [state]
	(-> state :columns/by-id vals))
