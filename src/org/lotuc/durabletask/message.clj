(ns org.lotuc.durabletask.message
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]
   [pronto.core :as p]))

(set! *warn-on-reflection* true)

(declare mapper)

;;; orchestrator_service.proto
(defn- durabletask-message-names [] (str/split-lines (slurp (io/resource "durabletask_messages.txt"))))
(def ^:private durabletask-java-package "com.microsoft.durabletask.implementation.protobuf")
(def ^:private durabletask-java-outer-name "OrchestratorService")

;;; google protobuf messages used in orchestrator_service.proto
(def ^:private protobuf-java-package "com.google.protobuf")
(def ^:private protobuf-message-names ["Empty" "Duration" "Int32Value" "StringValue" "Timestamp"])

(defn- make-class-symbol [outer-name class-name]
  (symbol (if outer-name (str outer-name "$" class-name) class-name)))

(defn- make-fqn [package-name outer-name class-name]
  (symbol (str package-name "." (if outer-name (str outer-name "$" class-name) class-name))))

(defmacro import-classes []
  (let [protobuf-classes (->> protobuf-message-names
                              (map (partial make-fqn protobuf-java-package nil)))
        durabletask-classes (->> (durabletask-message-names)
                                 (map (partial make-fqn durabletask-java-package durabletask-java-outer-name)))]
    `(import ~@protobuf-classes
             ~@durabletask-classes)))

(defmacro def-mapper []
  (let [google-protobuf-classes (->> protobuf-message-names
                                     (map (partial make-class-symbol nil)))
        durabletask-classes (->> (durabletask-message-names)
                                 (map (partial make-class-symbol durabletask-java-outer-name)))]
    `(p/defmapper ~'mapper [~@google-protobuf-classes
                            ~@durabletask-classes])))

(defn- build-convertions [outer-name clazz-name]
  (let [clj-map->proto-map-clazz (symbol (str "clj-map->" clazz-name "-proto-map"))
        clj-map->clazz (symbol (str "clj-map->" clazz-name))
        bytes->proto-map-clazz (symbol (str "bytes->" clazz-name "-proto-map"))
        clazz (make-class-symbol outer-name clazz-name)]
    [`(def ~clj-map->proto-map-clazz (fn [m#] (p/clj-map->proto-map mapper ~clazz m#)))
     `(def ~clj-map->clazz (fn [m#] (p/proto-map->proto (p/clj-map->proto-map mapper ~clazz m#))))
     `(def ~bytes->proto-map-clazz (fn [b#] (p/bytes->proto-map mapper ~clazz b#)))]))

(defmacro def-conversions []
  (let [google-protobuf-convertions (->> protobuf-message-names
                                         (map (partial build-convertions nil))
                                         (reduce concat))
        durabletask-convertions (->> (durabletask-message-names)
                                     (map (partial build-convertions durabletask-java-outer-name))
                                     (reduce concat))]
    `(do ~@google-protobuf-convertions
         ~@durabletask-convertions)))

(import-classes)
(def-mapper)
(def-conversions)

(def proto-empty (Empty/getDefaultInstance))

(def proto->proto-map (partial p/proto->proto-map mapper))
(def clj-map->proto-map (fn [clazz m] (p/clj-map->proto-map mapper clazz m)))
(def bytes->proto-map (fn [clazz bytes] (p/bytes->proto-map mapper clazz bytes)))
