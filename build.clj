(ns build
  (:require
   [clojure.tools.build.api :as b]))

(defn- p [& args]
  (println)
  (apply println args))

(defn clean [_]
  (p "Cleanup target directory...")
  (b/delete {:path "target"}))

(defn prep [{:keys [basis] :as opts}]
  (p "Preparing deps...")
  (when-not (-> {:command-args ["clojure" "-X:deps" "prep"]}
                b/process :exit zero?)
    (throw (Exception. "error prepare deps"))))
