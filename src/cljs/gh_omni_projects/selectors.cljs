(ns gh-omni-projects.selectors)

(defn get-projects-list [state]
  (-> state :projects/by-id vals))

(defn get-columns-list [state]
  (-> state :columns/by-id vals))

(defn get-cards-list [state]
  (-> state :cards/by-id vals))

(defn get-notes [state]
  (->> state :cards/by-id (filter #(->> % second :note))))

(defn get-issues [state]
  (->> state :cards/by-id (filter #(->> % second :note not))))
