(ns org.lotuc.durabletask.client-grpc
  (:require
   [org.lotuc.durabletask.message :as message]
   [org.lotuc.durabletask.protocol :as protocol])
  (:import
   com.microsoft.durabletask.implementation.protobuf.TaskHubSidecarServiceGrpc
   com.microsoft.durabletask.implementation.protobuf.TaskHubSidecarServiceGrpc$TaskHubSidecarServiceBlockingStub
   io.grpc.Grpc
   io.grpc.InsecureChannelCredentials))

(defn- call* [->proto f req]
  (-> (into {} (filter second req)) ->proto f message/proto->proto-map))

(defrecord TaskHubGrpcClient [^TaskHubSidecarServiceGrpc$TaskHubSidecarServiceBlockingStub client]
  protocol/DurableTaskClient
  (hello [_] (.hello client message/proto-empty) nil)
  (startInstance [_ req]
    (call* message/clj-map->CreateInstanceRequest #(.startInstance client %) req))
  (getInstance [_ req]
    (call* message/clj-map->GetInstanceRequest #(.getInstance client %) req))
  (rewindInstance [_ req]
    (call* message/clj-map->RewindInstanceRequest #(.rewindInstance client %) req))
  (waitForInstanceStart [_ req]
    (call* message/clj-map->GetInstanceRequest #(.waitForInstanceStart client %) req))
  (waitForInstanceCompletion [_ req]
    (call* message/clj-map->GetInstanceRequest #(.waitForInstanceCompletion client %) req))
  (raiseEvent [_ req]
    (call* message/clj-map->RaiseEventRequest #(.raiseEvent client %) req))
  (terminateInstance [_ req]
    (call* message/clj-map->TerminateRequest #(.terminateInstance client %) req))
  (suspendInstance [_ req]
    (call* message/clj-map->SuspendRequest #(.suspendInstance client %) req))
  (resumeInstance [_ req]
    (call* message/clj-map->ResumeRequest #(.resumeInstance client %) req))
  (queryInstances [_ req]
    (call* message/clj-map->QueryInstancesRequest #(.queryInstances client %) req))
  (purgeInstances [_ req]
    (call* message/clj-map->PurgeInstancesRequest #(.purgeInstances client %) req))
  (getWorkItems [_ req]
    (call* message/clj-map->GetWorkItemsRequest #(.getWorkItems client %) req))
  (completeActivityTask [_ req]
    (call* message/clj-map->ActivityResponse #(.completeActivityTask client %) req))
  (completeOrchestratorTask [_ req]
    (call* message/clj-map->OrchestratorResponse #(.completeOrchestratorTask client %) req))
  (completeEntityTask [_ req]
    (call* message/clj-map->EntityBatchResult #(.completeEntityTask client %) req))
  (createTaskHub [_ req]
    (call* message/clj-map->CreateTaskHubRequest #(.createTaskHub client %) req))
  (deleteTaskHub [_ req]
    (call* message/clj-map->DeleteTaskHubRequest #(.deleteTaskHub client %) req))
  (signalEntity [_ req]
    (call* message/clj-map->SignalEntityRequest #(.signalEntity client %) req))
  (getEntity [_ req]
    (call* message/clj-map->GetEntityRequest #(.getEntity client %) req))
  (queryEntities [_ req]
    (call* message/clj-map->QueryEntitiesRequest #(.queryEntities client %) req))
  (cleanEntityStorage [_ req]
    (call* message/clj-map->CleanEntityStorageRequest #(.cleanEntityStorage client %) req)))

(comment
  (def ch (.build (Grpc/newChannelBuilder "localhost:4001" (InsecureChannelCredentials/create))))
  (def blocking-stub (TaskHubSidecarServiceGrpc/newBlockingStub ch))

  (def c (TaskHubGrpcClient. blocking-stub))
  (.hello c)
  (.startInstance c {:instanceId "hello"})
  (.getInstance c {:instanceId "hello"})
  (.rewindInstance c {:instanceId "hello" :reason "42"})

  (protocol/hello c))
