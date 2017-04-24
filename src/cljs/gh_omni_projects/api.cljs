(ns gh-omni-projects.api
		(:require-macros [cljs.core.async.macros :refer [go]])
    (:require [cljs-http.client :as http]
							[cljs.core.async :refer [<!]]))

(def clientId "65af6d23b5e26f4347a9")
(def clientSecret "fcc223fc39cdcf1d8dc75af4eba6ba55260da9e6")
(def token "2154838dcc41d5d7b529e3d8f97c46e70dd0abfc")

(defn getUsers []
	(http/get "https://api.github.com/users"
						{:with-credentials? false}))

(defn getProjectsForRepo [owner repo]
	(http/get (str "https://api.github.com/repos/" owner "/" repo "/projects")
						{:with-credentials? false
						 :basic-auth {:username "bspaulding" :password token}
						 :headers {"Accept" "application/vnd.github.inertia-preview+json" }}))

(defn getColumnsForProject [projectId]
	(http/get (str "https://api.github.com/projects/" projectId "/columns")
						{:with-credentials? false
						 :basic-auth {:username "bspaulding" :password token}
						 :headers {"Accept" "application/vnd.github.inertia-preview+json"}}))

(defn getCardsForColumn [columnId]
	(http/get (str "https://api.github.com/projects/columns/" columnId "/cards")
						{:with-credentials? false
						 :basic-auth {:username "bspaulding" :password token}
						 :headers {"Accept" "application/vnd.github.inertia-preview+json"}}))

(defn getIssueForCard [card]
	(http/get (:content_url card)
						{:with-credentials? false
						 :basic-auth {:username "bspaulding" :password token}
						 :headers {"Accept" "application/vnd.github.inertia-preview+json"}}))

;; (go (let [response (<! (http/get "https://api.github.com/users"
;; 																 {:with-credentials? false
;; 																	:query-params {"since" 135}}))]
;; 			(print (:status response))
;; 			(print (map :login (:body response)))))
