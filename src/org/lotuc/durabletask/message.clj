(ns org.lotuc.durabletask.message
  (:require
   [pronto.core :as p]))

(set! *warn-on-reflection* true)

;;; orchestrator_service.proto
;;; cat orchestrator_service.proto | grep message | awk '{print $2}'
(def ^:private durabletask-message-names ["OrchestrationInstance"
                                          "ActivityRequest"
                                          "ActivityResponse"
                                          "TaskFailureDetails"
                                          "ParentInstanceInfo"
                                          "ExecutionStartedEvent"
                                          "ExecutionCompletedEvent"
                                          "ExecutionTerminatedEvent"
                                          "TaskScheduledEvent"
                                          "TaskCompletedEvent"
                                          "TaskFailedEvent"
                                          "SubOrchestrationInstanceCreatedEvent"
                                          "SubOrchestrationInstanceCompletedEvent"
                                          "SubOrchestrationInstanceFailedEvent"
                                          "TimerCreatedEvent"
                                          "TimerFiredEvent"
                                          "OrchestratorStartedEvent"
                                          "OrchestratorCompletedEvent"
                                          "EventSentEvent"
                                          "EventRaisedEvent"
                                          "GenericEvent"
                                          "HistoryStateEvent"
                                          "ContinueAsNewEvent"
                                          "ExecutionSuspendedEvent"
                                          "ExecutionResumedEvent"
                                          "HistoryEvent"
                                          "ScheduleTaskAction"
                                          "CreateSubOrchestrationAction"
                                          "CreateTimerAction"
                                          "SendEventAction"
                                          "CompleteOrchestrationAction"
                                          "TerminateOrchestrationAction"
                                          "OrchestratorAction"
                                          "OrchestratorRequest"
                                          "OrchestratorResponse"
                                          "CreateInstanceRequest"
                                          "CreateInstanceResponse"
                                          "GetInstanceRequest"
                                          "GetInstanceResponse"
                                          "RewindInstanceRequest"
                                          "RewindInstanceResponse"
                                          "OrchestrationState"
                                          "RaiseEventRequest"
                                          "RaiseEventResponse"
                                          "TerminateRequest"
                                          "TerminateResponse"
                                          "SuspendRequest"
                                          "SuspendResponse"
                                          "ResumeRequest"
                                          "ResumeResponse"
                                          "QueryInstancesRequest"
                                          "InstanceQuery"
                                          "QueryInstancesResponse"
                                          "PurgeInstancesRequest"
                                          "PurgeInstanceFilter"
                                          "PurgeInstancesResponse"
                                          "CreateTaskHubRequest"
                                          "CreateTaskHubResponse"
                                          "DeleteTaskHubRequest"
                                          "DeleteTaskHubResponse"
                                          "SignalEntityRequest"
                                          "SignalEntityResponse"
                                          "GetEntityRequest"
                                          "GetEntityResponse"
                                          "EntityQuery"
                                          "QueryEntitiesRequest"
                                          "QueryEntitiesResponse"
                                          "EntityMetadata"
                                          "CleanEntityStorageRequest"
                                          "CleanEntityStorageResponse"
                                          "OrchestratorEntityParameters"
                                          "EntityBatchRequest"
                                          "EntityBatchResult"
                                          "OperationRequest"
                                          "OperationResult"
                                          "OperationResultSuccess"
                                          "OperationResultFailure"
                                          "OperationAction"
                                          "SendSignalAction"
                                          "StartNewOrchestrationAction"
                                          "GetWorkItemsRequest"
                                          "WorkItem"
                                          "CompleteTaskResponse"])
(def ^:private durabletask-java-package "com.microsoft.durabletask.implementation.protobuf")
(def ^:private durabletask-java-outer-name "OrchestratorService")

;;; google protobuf messages used in orchestrator_service.proto
(def ^:private protobuf-java-package "com.google.protobuf")
(def ^:private protobuf-message-names ["Empty" "Duration" "Int32Value" "StringValue" "Timestamp"])

(defmacro ^:private import-classes []
  (letfn [(make-fqn [package-name outer-name class-name]
            (symbol (str package-name "." (if outer-name (str outer-name "$" class-name) class-name))))]
    `(import ~@(->> protobuf-message-names
                    (map (partial make-fqn protobuf-java-package nil)))
             ~@(->> durabletask-message-names
                    (map (partial make-fqn durabletask-java-package durabletask-java-outer-name))))))

(defn- make-class-symbol [outer-name class-name]
  (symbol (if outer-name (str outer-name "$" class-name) class-name)))

(defmacro ^:private def-mapper []
  `(p/defmapper ~'mapper
     [~@(->> protobuf-message-names
             (map (partial make-class-symbol nil)))
      ~@(->> durabletask-message-names
             (map (partial make-class-symbol durabletask-java-outer-name)))]))

(defn- build-convertions [outer-name clazz-name]
  (let [clj-map->proto-map-clazz (symbol (str "clj-map->" clazz-name "-proto-map"))
        clj-map->clazz           (symbol (str "clj-map->" clazz-name))
        bytes->proto-map-clazz   (symbol (str "bytes->" clazz-name "-proto-map"))
        clazz                    (make-class-symbol outer-name clazz-name)]
    [`(def ~clj-map->proto-map-clazz (fn [m#] (p/clj-map->proto-map mapper ~clazz m#)))
     `(def ~clj-map->clazz           (fn [m#] (p/proto-map->proto (p/clj-map->proto-map mapper ~clazz m#))))
     `(def ~bytes->proto-map-clazz   (fn [b#] (p/bytes->proto-map mapper ~clazz b#)))]))

(defmacro ^:private def-conversions []
  `(do ~@(->> protobuf-message-names
              (map (partial build-convertions nil))
              (reduce concat))
       ~@(->> durabletask-message-names
              (map (partial build-convertions durabletask-java-outer-name))
              (reduce concat))))

#_{:clj-kondo/ignore [:unused-import]}
(import-classes)

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(do
  (def-mapper)
  (def-conversions)
  (def proto-empty (Empty/getDefaultInstance))
  (def proto->proto-map (partial p/proto->proto-map mapper))
  (def clj-map->proto-map (fn [clazz m] (p/clj-map->proto-map mapper clazz m)))
  (def bytes->proto-map (fn [clazz bytes] (p/bytes->proto-map mapper clazz bytes))))
