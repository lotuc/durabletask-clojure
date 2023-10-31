(ns build
  (:require [clojure.java.io :as io]
            [clojure.string :as s]
            [clojure.tools.build.api :as b]))

(def basis (b/create-basis {:project "deps.edn"}))
(def java-src-dir "target/src-java")
(def class-dir "target/classes")

(def proto-gen-grpc-java-plugin
  (or (System/getenv "PROTOC_GEN_GRPC_JAVA")
      "proto-plugins/protoc-gen-grpc-java"))

(defn clean []
  (b/delete {:path java-src-dir})
  (b/delete {:path "target"}))

(defn- sh-cmd! [command-args]
  (when-not (-> {:command-args command-args} b/process :exit zero?)
    (throw (Exception. (str "error compile: " command-args)))))

(defn compile-protos []
  (let [f (io/file proto-gen-grpc-java-plugin)]
    (when-not (.exists f)
      (println "proto-gen-grpc-java plugin does not exists:")
      (println "  1. setup env PROTOC_GEN_GRPC_JAVA, or")
      (println "  2. download and put it at: " (.getAbsolutePath f))
      (println "     check out the following link for details:\n"
               "        https://github.com/grpc/grpc-java/tree/master/compiler")
      (System/exit 1)))

  (println "\nCompiling proto...")
  (.mkdirs (io/file java-src-dir))
  (let [proto-dir (io/file "./durabletask-protobuf/protos")]
    (doseq [proto-file (->> (file-seq proto-dir)
                            (map str)
                            (filter #(s/ends-with? % ".proto")))
            :let [cmd0 ["protoc"
                        "--java_out" java-src-dir
                        "--proto_path" (str proto-dir)
                        proto-file]
                  cmd1 ["protoc"
                        "--plugin" (str "protoc-gen-grpc-java="
                                        proto-gen-grpc-java-plugin)
                        "--grpc-java_out" java-src-dir
                        "--proto_path"  (str proto-dir)
                        proto-file]]]
      (doto cmd0 println sh-cmd!)
      (doto cmd1 println sh-cmd!))))

(defn compile-java []
  (b/javac {:src-dirs [java-src-dir]
            :class-dir class-dir
            :basis basis}))

(defn prep-lib [_]
  (clean)
  (compile-protos)
  (compile-java))
