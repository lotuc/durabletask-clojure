(ns build
  (:require [clojure.java.io :as io]
            [clojure.string :as s]
            [clojure.tools.build.api :as b]))

(def basis (b/create-basis {:project "deps.edn"}))
(def java-src-dir "java-src")
(def class-dir "target/classes")

(defn clean []
  (b/delete {:path "target"}))

(defn compile-protos []
  (.mkdirs (io/file java-src-dir))
  (doseq [proto-file (->> (file-seq (io/file "./durabletask-protobuf/protos"))
                          (map str)
                          (filter #(s/ends-with? % ".proto")))]
    (when-not (-> {:command-args ["protoc" proto-file "--java_out" java-src-dir]}
                  b/process :exit zero?)
      (throw (Exception. (str "error compile: " proto-file))))))

(defn compile-java []
  (b/javac {:src-dirs [java-src-dir]
            :class-dir class-dir
            :basis basis}))

(defn prep-lib [_]
  (clean)
  (compile-protos)
  (compile-java))
