(ns org.lotuc.durabletask.message
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]
   [pronto.core :as p]
   [pronto.utils :as u]))

(set! *warn-on-reflection* true)

(declare mapper)

(defn- message-names [] (str/split-lines (slurp (io/resource "durabletask_messages.txt"))))
(def ^:private java-package "com.microsoft.durabletask.implementation.protobuf")
(def ^:private outer-classname "OrchestratorService")

(defmacro import-durabletask-messages []
  (let [classes (map (fn [n] (symbol (str java-package "."  outer-classname "$" n))) (message-names))]
    `(import ~@classes)))

(defmacro def-durabletask-mapper []
  (let [classes (map (fn [n] (symbol (str outer-classname "$" n))) (message-names))]
    `(p/defmapper ~'mapper [~@classes]
       :key-name-fn u/->kebab-case
       :enum-value-fn u/->kebab-case)))

(defn def-clj-map->proto-map [clazz-name]
  (let [n (symbol (str "clj-map->proto-map-" clazz-name))
        c (symbol (str "OrchestratorService$" clazz-name))]
    `(def ~n (fn [m#] (p/clj-map->proto-map mapper ~c m#)))))

(defn def-bytes->proto-map [clazz-name]
  (let [n (symbol (str "bytes->proto-map-" clazz-name))
        c (symbol (str "OrchestratorService$" clazz-name))]
    `(def ~n (fn [b#] (p/bytes->proto-map mapper ~c b#)))))

(defmacro def-conversions []
  (let [names (message-names)
        d0 (map def-clj-map->proto-map names)
        d1 (map def-bytes->proto-map names)]
    `(do ~@d0 ~@d1)))

(import-durabletask-messages)
(def-durabletask-mapper)
(def-conversions)

(def proto->proto-map (partial p/proto->proto-map mapper))
(def clj-map->proto-map (fn [clazz m] (p/clj-map->proto-map mapper clazz m)))
(def bytes->proto-map (fn [clazz bytes] (p/bytes->proto-map mapper clazz bytes)))
