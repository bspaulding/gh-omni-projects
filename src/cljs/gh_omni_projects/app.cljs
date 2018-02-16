(ns gh-omni-projects.app
  (:refer-clojure :exclude [print])
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [rum.core :as rum]
            [cljs.core.async :refer [<!]]
            [clojure.data :refer [diff]]
            [gh-omni-projects.actions :as actions]
            [gh-omni-projects.components :refer [app]]))

(defn print [& args]
  (.log js/console args))

(defonce state (atom {:projects []
                      :projects/by-id {}
                      :cards/by-id {}
                      :columns/by-id {}
                      :columns/by-project-id {}
                      :columns/by-name {}
                      :loading false}))

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
