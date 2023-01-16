/* tslint:disable */
/* eslint-disable */
// Generated using typescript-generator version 3.1.1185 on 2023-01-16 14:08:03.

export interface de_bwl_bwfla_api_blobstore_BlobStore {
}

export interface de_bwl_bwfla_api_blobstore_BlobStoreService extends javax_xml_ws_Service {
    blobStorePort: de_bwl_bwfla_api_blobstore_BlobStore;
}

export interface de_bwl_bwfla_api_blobstore_Delete {
    arg0: de_bwl_bwfla_blobstore_api_BlobHandle;
}

export interface de_bwl_bwfla_api_blobstore_DeleteResponse {
}

export interface de_bwl_bwfla_api_blobstore_Get {
    arg0: de_bwl_bwfla_blobstore_api_BlobHandle;
}

export interface de_bwl_bwfla_api_blobstore_GetResponse {
    return: de_bwl_bwfla_blobstore_api_Blob;
}

export interface de_bwl_bwfla_api_blobstore_Put {
    arg0: de_bwl_bwfla_blobstore_api_BlobDescription;
}

export interface de_bwl_bwfla_api_blobstore_PutResponse {
    return: de_bwl_bwfla_blobstore_api_BlobHandle;
}

export interface de_bwl_bwfla_api_eaas_CreateSession {
    arg0: string;
}

export interface de_bwl_bwfla_api_eaas_CreateSessionResponse {
    return: string;
}

export interface de_bwl_bwfla_api_eaas_CreateSessionWithOptions {
    arg0: string;
    arg1: de_bwl_bwfla_api_eaas_SessionOptions;
}

export interface de_bwl_bwfla_api_eaas_CreateSessionWithOptionsResponse {
    return: string;
}

export interface de_bwl_bwfla_api_eaas_EaasWS {
}

export interface de_bwl_bwfla_api_eaas_EaasWSService extends javax_xml_ws_Service {
    eaasWSPort: de_bwl_bwfla_api_eaas_EaasWS;
}

export interface de_bwl_bwfla_api_eaas_OutOfResourcesException {
    message: string;
    messageWithoutSuffix: string;
}

export interface de_bwl_bwfla_api_eaas_OutOfResourcesException_Exception extends java_lang_Exception {
    faultInfo: de_bwl_bwfla_api_eaas_OutOfResourcesException;
}

export interface de_bwl_bwfla_api_eaas_QuotaExceededException {
    message: string;
    messageWithoutSuffix: string;
}

export interface de_bwl_bwfla_api_eaas_QuotaExceededException_Exception extends java_lang_Exception {
    faultInfo: de_bwl_bwfla_api_eaas_QuotaExceededException;
}

export interface de_bwl_bwfla_api_eaas_ReleaseSession {
    arg0: string;
}

export interface de_bwl_bwfla_api_eaas_ReleaseSessionResponse {
}

export interface de_bwl_bwfla_api_eaas_ResourceSpec {
    cpu: number;
    memory: number;
}

export interface de_bwl_bwfla_api_eaas_SessionOptions {
    lockEnvironment: boolean;
    resourceSpec: de_bwl_bwfla_api_eaas_ResourceSpec;
    selectors: string[];
    tenantId: string;
    userId: string;
}

export interface de_bwl_bwfla_api_emucomp_AddActionFinishedMark {
    arg0: string;
}

export interface de_bwl_bwfla_api_emucomp_AddActionFinishedMarkResponse {
}

export interface de_bwl_bwfla_api_emucomp_AddTraceMetadataEntry {
    arg0: string;
    arg1: string;
    arg2: string;
    arg3: string;
}

export interface de_bwl_bwfla_api_emucomp_AddTraceMetadataEntryResponse {
}

export interface de_bwl_bwfla_api_emucomp_AttachMedium {
    arg0: string;
    arg1: javax_activation_DataHandler;
    arg2: string;
}

export interface de_bwl_bwfla_api_emucomp_AttachMediumResponse {
    return: number;
}

export interface de_bwl_bwfla_api_emucomp_ChangeMedium {
    arg0: string;
    arg1: number;
    arg2: string;
}

export interface de_bwl_bwfla_api_emucomp_ChangeMediumResponse {
    return: number;
}

export interface de_bwl_bwfla_api_emucomp_Checkpoint {
    arg0: string;
}

export interface de_bwl_bwfla_api_emucomp_CheckpointResponse {
    return: javax_activation_DataHandler;
}

export interface de_bwl_bwfla_api_emucomp_Component {
}

export interface de_bwl_bwfla_api_emucomp_ComponentService extends javax_xml_ws_Service {
    networkSwitchPort: de_bwl_bwfla_api_emucomp_NetworkSwitch;
    containerPort: de_bwl_bwfla_api_emucomp_Container;
    componentPort: de_bwl_bwfla_api_emucomp_Component;
    dontUseDummyPort: de_bwl_bwfla_api_emucomp_DontUseDummy;
    machinePort: de_bwl_bwfla_api_emucomp_Machine;
}

export interface de_bwl_bwfla_api_emucomp_Connect {
    arg0: string;
    arg1: string;
}

export interface de_bwl_bwfla_api_emucomp_ConnectResponse {
}

export interface de_bwl_bwfla_api_emucomp_Container {
}

export interface de_bwl_bwfla_api_emucomp_DefineTraceMetadataChunk {
    arg0: string;
    arg1: string;
    arg2: string;
}

export interface de_bwl_bwfla_api_emucomp_DefineTraceMetadataChunkResponse {
}

export interface de_bwl_bwfla_api_emucomp_Destroy {
    arg0: string;
}

export interface de_bwl_bwfla_api_emucomp_DestroyResponse {
}

export interface de_bwl_bwfla_api_emucomp_DetachMedium {
    arg0: string;
    arg1: number;
}

export interface de_bwl_bwfla_api_emucomp_DetachMediumResponse {
    return: javax_activation_DataHandler;
}

export interface de_bwl_bwfla_api_emucomp_Disconnect {
    arg0: string;
    arg1: string;
}

export interface de_bwl_bwfla_api_emucomp_DisconnectResponse {
}

export interface de_bwl_bwfla_api_emucomp_DontUseDummy {
}

export interface de_bwl_bwfla_api_emucomp_GetAllMonitorValues {
    arg0: string;
}

export interface de_bwl_bwfla_api_emucomp_GetAllMonitorValuesResponse {
    return: string[];
}

export interface de_bwl_bwfla_api_emucomp_GetColdplugableDrives {
    arg0: string;
}

export interface de_bwl_bwfla_api_emucomp_GetColdplugableDrivesResponse {
    return: string[];
}

export interface de_bwl_bwfla_api_emucomp_GetComponentType {
    arg0: string;
}

export interface de_bwl_bwfla_api_emucomp_GetComponentTypeResponse {
    return: string;
}

export interface de_bwl_bwfla_api_emucomp_GetControlUrls {
    arg0: string;
}

export interface de_bwl_bwfla_api_emucomp_GetControlUrlsResponse {
    return: de_bwl_bwfla_api_emucomp_GetControlUrlsResponse$Return;
}

export interface de_bwl_bwfla_api_emucomp_GetControlUrlsResponse$Return {
    entry: de_bwl_bwfla_api_emucomp_GetControlUrlsResponse$Return$Entry[];
}

export interface de_bwl_bwfla_api_emucomp_GetControlUrlsResponse$Return$Entry {
    key: string;
    value: string;
}

export interface de_bwl_bwfla_api_emucomp_GetEmulatorState {
    arg0: string;
}

export interface de_bwl_bwfla_api_emucomp_GetEmulatorStateResponse {
    return: string;
}

export interface de_bwl_bwfla_api_emucomp_GetEnvironmentId {
    arg0: string;
}

export interface de_bwl_bwfla_api_emucomp_GetEnvironmentIdResponse {
    return: string;
}

export interface de_bwl_bwfla_api_emucomp_GetEventSourceUrl {
    arg0: string;
}

export interface de_bwl_bwfla_api_emucomp_GetEventSourceUrlResponse {
    return: string;
}

export interface de_bwl_bwfla_api_emucomp_GetHotplugableDrives {
    arg0: string;
}

export interface de_bwl_bwfla_api_emucomp_GetHotplugableDrivesResponse {
    return: string[];
}

export interface de_bwl_bwfla_api_emucomp_GetMonitorValue {
    arg0: string;
    arg1: number;
}

export interface de_bwl_bwfla_api_emucomp_GetMonitorValueResponse {
    return: string;
}

export interface de_bwl_bwfla_api_emucomp_GetMonitorValues {
    arg0: string;
    arg1: number[];
}

export interface de_bwl_bwfla_api_emucomp_GetMonitorValuesResponse {
    return: string[];
}

export interface de_bwl_bwfla_api_emucomp_GetNextScreenshot {
    arg0: string;
}

export interface de_bwl_bwfla_api_emucomp_GetNextScreenshotResponse {
    return: javax_activation_DataHandler;
}

export interface de_bwl_bwfla_api_emucomp_GetPrintJobs {
    arg0: string;
}

export interface de_bwl_bwfla_api_emucomp_GetPrintJobsResponse {
    return: de_bwl_bwfla_api_emucomp_PrintJob[];
}

export interface de_bwl_bwfla_api_emucomp_GetResult {
    arg0: string;
}

export interface de_bwl_bwfla_api_emucomp_GetResultResponse {
    return: de_bwl_bwfla_blobstore_api_BlobHandle;
}

export interface de_bwl_bwfla_api_emucomp_GetRuntimeConfiguration {
    arg0: string;
}

export interface de_bwl_bwfla_api_emucomp_GetRuntimeConfigurationResponse {
    return: string;
}

export interface de_bwl_bwfla_api_emucomp_GetSessionPlayerProgress {
    arg0: string;
}

export interface de_bwl_bwfla_api_emucomp_GetSessionPlayerProgressResponse {
    return: number;
}

export interface de_bwl_bwfla_api_emucomp_GetSessionTrace {
    arg0: string;
}

export interface de_bwl_bwfla_api_emucomp_GetSessionTraceResponse {
    return: string;
}

export interface de_bwl_bwfla_api_emucomp_GetState {
    arg0: string;
}

export interface de_bwl_bwfla_api_emucomp_GetStateResponse {
    return: string;
}

export interface de_bwl_bwfla_api_emucomp_Initialize {
    arg0: string;
    arg1: string;
}

export interface de_bwl_bwfla_api_emucomp_InitializeResponse {
    return: string;
}

export interface de_bwl_bwfla_api_emucomp_IsRecordModeEnabled {
    arg0: string;
}

export interface de_bwl_bwfla_api_emucomp_IsRecordModeEnabledResponse {
    return: boolean;
}

export interface de_bwl_bwfla_api_emucomp_IsReplayModeEnabled {
    arg0: string;
}

export interface de_bwl_bwfla_api_emucomp_IsReplayModeEnabledResponse {
    return: boolean;
}

export interface de_bwl_bwfla_api_emucomp_Keepalive {
    arg0: string;
}

export interface de_bwl_bwfla_api_emucomp_KeepaliveResponse {
}

export interface de_bwl_bwfla_api_emucomp_Machine {
}

export interface de_bwl_bwfla_api_emucomp_NetworkSwitch {
}

export interface de_bwl_bwfla_api_emucomp_PrepareSessionPlayer {
    arg0: string;
    arg1: string;
    arg2: boolean;
}

export interface de_bwl_bwfla_api_emucomp_PrepareSessionPlayerResponse {
    return: boolean;
}

export interface de_bwl_bwfla_api_emucomp_PrepareSessionRecorder {
    arg0: string;
}

export interface de_bwl_bwfla_api_emucomp_PrepareSessionRecorderResponse {
    return: boolean;
}

export interface de_bwl_bwfla_api_emucomp_PrintJob {
    label: string;
    dataHandler: javax_activation_DataHandler;
}

export interface de_bwl_bwfla_api_emucomp_Snapshot {
    arg0: string;
}

export interface de_bwl_bwfla_api_emucomp_SnapshotResponse {
    return: de_bwl_bwfla_emucomp_api_BindingDataHandler[];
}

export interface de_bwl_bwfla_api_emucomp_Start {
    arg0: string;
}

export interface de_bwl_bwfla_api_emucomp_StartContainer {
    arg0: string;
}

export interface de_bwl_bwfla_api_emucomp_StartContainerResponse {
}

export interface de_bwl_bwfla_api_emucomp_StartResponse {
}

export interface de_bwl_bwfla_api_emucomp_StartSessionRecording {
    arg0: string;
}

export interface de_bwl_bwfla_api_emucomp_StartSessionRecordingResponse {
}

export interface de_bwl_bwfla_api_emucomp_Stop {
    arg0: string;
}

export interface de_bwl_bwfla_api_emucomp_StopContainer {
    arg0: string;
}

export interface de_bwl_bwfla_api_emucomp_StopContainerResponse {
}

export interface de_bwl_bwfla_api_emucomp_StopResponse {
    return: string;
}

export interface de_bwl_bwfla_api_emucomp_StopSessionRecording {
    arg0: string;
}

export interface de_bwl_bwfla_api_emucomp_StopSessionRecordingResponse {
}

export interface de_bwl_bwfla_api_emucomp_TakeScreenshot {
    arg0: string;
}

export interface de_bwl_bwfla_api_emucomp_TakeScreenshotResponse {
}

export interface de_bwl_bwfla_api_emucomp_UpdateMonitorValues {
    arg0: string;
}

export interface de_bwl_bwfla_api_emucomp_UpdateMonitorValuesResponse {
    return: boolean;
}

export interface de_bwl_bwfla_api_emucomp_WsConnect {
    arg0: string;
}

export interface de_bwl_bwfla_api_emucomp_WsConnectResponse {
    return: string;
}

export interface de_bwl_bwfla_api_imagearchive_AddNameIndexesEntry {
    arg0: string;
    arg1: de_bwl_bwfla_api_imagearchive_ImageMetadata;
    arg2: de_bwl_bwfla_api_imagearchive_Alias;
}

export interface de_bwl_bwfla_api_imagearchive_AddNameIndexesEntryResponse {
}

export interface de_bwl_bwfla_api_imagearchive_AddRecordingFile {
    arg0: string;
    arg1: string;
    arg2: string;
    arg3: string;
}

export interface de_bwl_bwfla_api_imagearchive_AddRecordingFileResponse {
    return: boolean;
}

export interface de_bwl_bwfla_api_imagearchive_Alias {
    name: string;
    version: string;
    alias: string;
}

export interface de_bwl_bwfla_api_imagearchive_CreateImage {
    arg0: string;
    arg1: string;
    arg2: string;
}

export interface de_bwl_bwfla_api_imagearchive_CreateImageAsync {
    arg0: string;
    arg1: string;
    arg2: de_bwl_bwfla_api_imagearchive_ImageType;
    arg3: de_bwl_bwfla_api_imagearchive_ImageMetadata;
}

export interface de_bwl_bwfla_api_imagearchive_CreateImageAsyncResponse {
    return: de_bwl_bwfla_api_imagearchive_TaskState;
}

export interface de_bwl_bwfla_api_imagearchive_CreateImageResponse {
    return: string;
}

export interface de_bwl_bwfla_api_imagearchive_CreatePatchedImage {
    arg0: string;
    arg1: string;
    arg2: de_bwl_bwfla_api_imagearchive_ImageType;
    arg3: string;
}

export interface de_bwl_bwfla_api_imagearchive_CreatePatchedImageResponse {
    return: string;
}

export interface de_bwl_bwfla_api_imagearchive_DefaultEntry {
    key: string;
    value: string;
}

export interface de_bwl_bwfla_api_imagearchive_DefaultEnvironments {
    map: de_bwl_bwfla_api_imagearchive_DefaultEntry[];
}

export interface de_bwl_bwfla_api_imagearchive_DeleteImage {
    arg0: string;
    arg1: string;
    arg2: string;
}

export interface de_bwl_bwfla_api_imagearchive_DeleteImageResponse {
    return: boolean;
}

export interface de_bwl_bwfla_api_imagearchive_DeleteMetadata {
    arg0: string;
    arg1: string;
}

export interface de_bwl_bwfla_api_imagearchive_DeleteMetadataResponse {
    return: boolean;
}

export interface de_bwl_bwfla_api_imagearchive_DeleteNameIndexesEntry {
    arg0: string;
    arg1: string;
    arg2: string;
}

export interface de_bwl_bwfla_api_imagearchive_DeleteNameIndexesEntryResponse {
}

export interface de_bwl_bwfla_api_imagearchive_DeleteTempEnvironments {
    arg0: string;
}

export interface de_bwl_bwfla_api_imagearchive_DeleteTempEnvironmentsResponse {
}

export interface de_bwl_bwfla_api_imagearchive_EmulatorMetadata {
    emulatorVersion: string;
    version: string;
    emulatorType: string;
    containerDigest: string;
    ociSourceUrl: string;
}

export interface de_bwl_bwfla_api_imagearchive_ExtractMetadata {
    arg0: string;
    arg1: string;
}

export interface de_bwl_bwfla_api_imagearchive_ExtractMetadataResponse {
    return: de_bwl_bwfla_api_imagearchive_EmulatorMetadata;
}

export interface de_bwl_bwfla_api_imagearchive_GetDefaultBackendName {
}

export interface de_bwl_bwfla_api_imagearchive_GetDefaultBackendNameResponse {
    return: string;
}

export interface de_bwl_bwfla_api_imagearchive_GetDefaultEnvironment {
    arg0: string;
    arg1: string;
}

export interface de_bwl_bwfla_api_imagearchive_GetDefaultEnvironmentResponse {
    return: string;
}

export interface de_bwl_bwfla_api_imagearchive_GetDefaultEnvironments {
    arg0: string;
}

export interface de_bwl_bwfla_api_imagearchive_GetDefaultEnvironmentsResponse {
    return: de_bwl_bwfla_api_imagearchive_DefaultEnvironments;
}

export interface de_bwl_bwfla_api_imagearchive_GetEnvironmentById {
    arg0: string;
    arg1: string;
}

export interface de_bwl_bwfla_api_imagearchive_GetEnvironmentByIdResponse {
    return: string;
}

export interface de_bwl_bwfla_api_imagearchive_GetEnvironments {
    arg0: string;
    arg1: string;
}

export interface de_bwl_bwfla_api_imagearchive_GetEnvironmentsResponse {
    return: string[];
}

export interface de_bwl_bwfla_api_imagearchive_GetExportPrefix {
    arg0: string;
}

export interface de_bwl_bwfla_api_imagearchive_GetExportPrefixResponse {
    return: string;
}

export interface de_bwl_bwfla_api_imagearchive_GetImageBinding {
    arg0: string;
    arg1: string;
    arg2: string;
}

export interface de_bwl_bwfla_api_imagearchive_GetImageBindingResponse {
    return: string;
}

export interface de_bwl_bwfla_api_imagearchive_GetImageGeneralizationPatches {
}

export interface de_bwl_bwfla_api_imagearchive_GetImageGeneralizationPatchesResponse {
    return: de_bwl_bwfla_api_imagearchive_ImageGeneralizationPatchDescription[];
}

export interface de_bwl_bwfla_api_imagearchive_GetImageImportResult {
    arg0: string;
    arg1: string;
}

export interface de_bwl_bwfla_api_imagearchive_GetImageImportResultResponse {
    return: de_bwl_bwfla_api_imagearchive_ImageImportResult;
}

export interface de_bwl_bwfla_api_imagearchive_GetNameIndexes {
    arg0: string;
}

export interface de_bwl_bwfla_api_imagearchive_GetNameIndexesResponse {
    return: de_bwl_bwfla_api_imagearchive_ImageNameIndex;
}

export interface de_bwl_bwfla_api_imagearchive_GetRecording {
    arg0: string;
    arg1: string;
    arg2: string;
}

export interface de_bwl_bwfla_api_imagearchive_GetRecordingResponse {
    return: string;
}

export interface de_bwl_bwfla_api_imagearchive_GetRecordings {
    arg0: string;
    arg1: string;
}

export interface de_bwl_bwfla_api_imagearchive_GetRecordingsResponse {
    return: de_bwl_bwfla_api_imagearchive_IwdMetaData[];
}

export interface de_bwl_bwfla_api_imagearchive_GetTaskState {
    arg0: string;
}

export interface de_bwl_bwfla_api_imagearchive_GetTaskStateResponse {
    return: de_bwl_bwfla_api_imagearchive_TaskState;
}

export interface de_bwl_bwfla_api_imagearchive_ImageArchiveMetadata {
    type: de_bwl_bwfla_api_imagearchive_ImageType;
    userId: string;
    imageId: string;
    deleteIfExists: boolean;
}

export interface de_bwl_bwfla_api_imagearchive_ImageArchiveWS {
    defaultBackendName: string;
    imageGeneralizationPatches: de_bwl_bwfla_api_imagearchive_ImageGeneralizationPatchDescription[];
}

export interface de_bwl_bwfla_api_imagearchive_ImageArchiveWSService extends javax_xml_ws_Service {
    imageArchiveWSPort: de_bwl_bwfla_api_imagearchive_ImageArchiveWS;
}

export interface de_bwl_bwfla_api_imagearchive_ImageDescription {
    url: string;
    id: string;
    type: string;
    fstype: string;
}

export interface de_bwl_bwfla_api_imagearchive_ImageGeneralizationPatchDescription {
    name: string;
    description: string;
}

export interface de_bwl_bwfla_api_imagearchive_ImageImportResult {
    urlPrefix: string;
    imageId: string;
}

export interface de_bwl_bwfla_api_imagearchive_ImageMetadata {
    name: string;
    version: string;
    image: de_bwl_bwfla_api_imagearchive_ImageDescription;
    provenance: de_bwl_bwfla_api_imagearchive_Provenance;
    digest: string;
    label: string;
}

export interface de_bwl_bwfla_api_imagearchive_ImageNameIndex {
    entries: de_bwl_bwfla_api_imagearchive_ImageNameIndex$Entries;
    aliases: de_bwl_bwfla_api_imagearchive_ImageNameIndex$Aliases;
}

export interface de_bwl_bwfla_api_imagearchive_ImageNameIndex$Aliases {
    entry: de_bwl_bwfla_api_imagearchive_ImageNameIndex$Aliases$Entry[];
}

export interface de_bwl_bwfla_api_imagearchive_ImageNameIndex$Aliases$Entry {
    key: string;
    value: de_bwl_bwfla_api_imagearchive_Alias;
}

export interface de_bwl_bwfla_api_imagearchive_ImageNameIndex$Entries {
    entry: de_bwl_bwfla_api_imagearchive_ImageNameIndex$Entries$Entry[];
}

export interface de_bwl_bwfla_api_imagearchive_ImageNameIndex$Entries$Entry {
    key: string;
    value: de_bwl_bwfla_api_imagearchive_ImageMetadata;
}

export interface de_bwl_bwfla_api_imagearchive_ImportConfiguration {
    arg0: string;
    arg1: string;
    arg2: de_bwl_bwfla_api_imagearchive_ImageArchiveMetadata;
    arg3: boolean;
}

export interface de_bwl_bwfla_api_imagearchive_ImportConfigurationResponse {
}

export interface de_bwl_bwfla_api_imagearchive_ImportImageAsStream {
    arg0: string;
    arg1: javax_activation_DataHandler;
    arg2: de_bwl_bwfla_api_imagearchive_ImageArchiveMetadata;
}

export interface de_bwl_bwfla_api_imagearchive_ImportImageAsStreamResponse {
    return: string;
}

export interface de_bwl_bwfla_api_imagearchive_ImportImageFromUrl {
    arg0: string;
    arg1: string;
    arg2: de_bwl_bwfla_api_imagearchive_ImageArchiveMetadata;
}

export interface de_bwl_bwfla_api_imagearchive_ImportImageFromUrlAsync {
    arg0: string;
    arg1: string;
    arg2: de_bwl_bwfla_api_imagearchive_ImageArchiveMetadata;
}

export interface de_bwl_bwfla_api_imagearchive_ImportImageFromUrlAsyncResponse {
    return: de_bwl_bwfla_api_imagearchive_TaskState;
}

export interface de_bwl_bwfla_api_imagearchive_ImportImageFromUrlResponse {
    return: string;
}

export interface de_bwl_bwfla_api_imagearchive_IwdMetaData {
    description: string;
    title: string;
    uuid: string;
}

export interface de_bwl_bwfla_api_imagearchive_ListBackendNames {
}

export interface de_bwl_bwfla_api_imagearchive_ListBackendNamesResponse {
    return: string[];
}

export interface de_bwl_bwfla_api_imagearchive_Provenance {
    ociSourceUrl: string;
    versionTag: string;
    layers: string[];
}

export interface de_bwl_bwfla_api_imagearchive_Reload {
    arg0: string;
}

export interface de_bwl_bwfla_api_imagearchive_ReloadResponse {
}

export interface de_bwl_bwfla_api_imagearchive_ReplicateImages {
    arg0: string;
    arg1: string[];
}

export interface de_bwl_bwfla_api_imagearchive_ReplicateImagesResponse {
    return: string[];
}

export interface de_bwl_bwfla_api_imagearchive_ResolveImage {
    arg0: string;
    arg1: string;
}

export interface de_bwl_bwfla_api_imagearchive_ResolveImageResponse {
    return: string;
}

export interface de_bwl_bwfla_api_imagearchive_SetDefaultEnvironment {
    arg0: string;
    arg1: string;
    arg2: string;
}

export interface de_bwl_bwfla_api_imagearchive_SetDefaultEnvironmentResponse {
}

export interface de_bwl_bwfla_api_imagearchive_TaskState {
    taskId: string;
    done: boolean;
    failed: boolean;
    result: string;
}

export interface de_bwl_bwfla_api_imagearchive_UpdateConfiguration {
    arg0: string;
    arg1: string;
}

export interface de_bwl_bwfla_api_imagearchive_UpdateConfigurationResponse {
}

export interface de_bwl_bwfla_api_imagearchive_UpdateLatestEmulator {
    arg0: string;
    arg1: string;
    arg2: string;
}

export interface de_bwl_bwfla_api_imagearchive_UpdateLatestEmulatorResponse {
}

export interface de_bwl_bwfla_api_imagebuilder_Build {
    arg0: de_bwl_bwfla_imagebuilder_api_ImageDescription;
}

export interface de_bwl_bwfla_api_imagebuilder_BuildResponse {
    return: de_bwl_bwfla_imagebuilder_api_ImageBuildHandle;
}

export interface de_bwl_bwfla_api_imagebuilder_DockerImport extends de_bwl_bwfla_api_imagebuilder_ImageBuilderMetadata {
    imageRef: string;
    tag: string;
    layers: string[];
    emulatorVersion: string;
    digest: string;
    emulatorType: string;
    entryProcesses: string[];
    envVariables: string[];
    workingDir: string;
}

export interface de_bwl_bwfla_api_imagebuilder_Get {
    arg0: de_bwl_bwfla_imagebuilder_api_ImageBuildHandle;
}

export interface de_bwl_bwfla_api_imagebuilder_GetResponse {
    return: de_bwl_bwfla_api_imagebuilder_ImageBuilderResult;
}

export interface de_bwl_bwfla_api_imagebuilder_ImageBuilder {
}

export interface de_bwl_bwfla_api_imagebuilder_ImageBuilderMetadata {
}

export interface de_bwl_bwfla_api_imagebuilder_ImageBuilderResult {
    blobHandle: de_bwl_bwfla_blobstore_api_BlobHandle;
    metadata: de_bwl_bwfla_api_imagebuilder_ImageBuilderMetadata;
}

export interface de_bwl_bwfla_api_imagebuilder_ImageBuilderService extends javax_xml_ws_Service {
    imageBuilderPort: de_bwl_bwfla_api_imagebuilder_ImageBuilder;
}

export interface de_bwl_bwfla_api_imagebuilder_IsDone {
    arg0: de_bwl_bwfla_imagebuilder_api_ImageBuildHandle;
}

export interface de_bwl_bwfla_api_imagebuilder_IsDoneResponse {
    return: boolean;
}

export interface de_bwl_bwfla_api_objectarchive_Delete {
    arg0: string;
    arg1: string;
}

export interface de_bwl_bwfla_api_objectarchive_DeleteResponse {
}

export interface de_bwl_bwfla_api_objectarchive_GetArchives {
}

export interface de_bwl_bwfla_api_objectarchive_GetArchivesResponse {
    return: string[];
}

export interface de_bwl_bwfla_api_objectarchive_GetNumObjectSeats {
    arg0: string;
    arg1: string;
}

export interface de_bwl_bwfla_api_objectarchive_GetNumObjectSeatsForTenant {
    arg0: string;
    arg1: string;
    arg2: string;
}

export interface de_bwl_bwfla_api_objectarchive_GetNumObjectSeatsForTenantResponse {
    return: number;
}

export interface de_bwl_bwfla_api_objectarchive_GetNumObjectSeatsResponse {
    return: number;
}

export interface de_bwl_bwfla_api_objectarchive_GetObjectIds {
    arg0: string;
}

export interface de_bwl_bwfla_api_objectarchive_GetObjectIdsResponse {
    return: any;
}

export interface de_bwl_bwfla_api_objectarchive_GetObjectMetadata {
    arg0: string;
    arg1: string;
}

export interface de_bwl_bwfla_api_objectarchive_GetObjectMetadataCollection {
    arg0: string;
}

export interface de_bwl_bwfla_api_objectarchive_GetObjectMetadataCollectionResponse {
    return: any;
}

export interface de_bwl_bwfla_api_objectarchive_GetObjectMetadataResponse {
    return: de_bwl_bwfla_common_datatypes_DigitalObjectMetadata;
}

export interface de_bwl_bwfla_api_objectarchive_GetObjectReference {
    arg0: string;
    arg1: string;
}

export interface de_bwl_bwfla_api_objectarchive_GetObjectReferenceResponse {
    return: string;
}

export interface de_bwl_bwfla_api_objectarchive_GetTaskState {
    arg0: string;
}

export interface de_bwl_bwfla_api_objectarchive_GetTaskStateResponse {
    return: de_bwl_bwfla_api_objectarchive_TaskState;
}

export interface de_bwl_bwfla_api_objectarchive_ImportObjectFromMetadata {
    arg0: string;
    arg1: string;
}

export interface de_bwl_bwfla_api_objectarchive_ImportObjectFromMetadataResponse {
}

export interface de_bwl_bwfla_api_objectarchive_ObjectArchiveFacadeWS {
    archives: string[];
}

export interface de_bwl_bwfla_api_objectarchive_ObjectArchiveFacadeWSService extends javax_xml_ws_Service {
    objectArchiveFacadeWSPort: de_bwl_bwfla_api_objectarchive_ObjectArchiveFacadeWS;
}

export interface de_bwl_bwfla_api_objectarchive_RegisterUserArchive {
    arg0: string;
}

export interface de_bwl_bwfla_api_objectarchive_RegisterUserArchiveResponse {
}

export interface de_bwl_bwfla_api_objectarchive_ResetAllObjectSeatsForTenant {
    arg0: string;
}

export interface de_bwl_bwfla_api_objectarchive_ResetAllObjectSeatsForTenantResponse {
}

export interface de_bwl_bwfla_api_objectarchive_ResetNumObjectSeatsForTenant {
    arg0: string;
    arg1: string;
    arg2: string;
}

export interface de_bwl_bwfla_api_objectarchive_ResetNumObjectSeatsForTenantBatched {
    arg0: string;
    arg1: string[];
    arg2: string;
}

export interface de_bwl_bwfla_api_objectarchive_ResetNumObjectSeatsForTenantBatchedResponse {
}

export interface de_bwl_bwfla_api_objectarchive_ResetNumObjectSeatsForTenantResponse {
}

export interface de_bwl_bwfla_api_objectarchive_ResolveObjectResource {
    arg0: string;
    arg1: string;
    arg2: string;
    arg3: string;
}

export interface de_bwl_bwfla_api_objectarchive_ResolveObjectResourceResponse {
    return: string;
}

export interface de_bwl_bwfla_api_objectarchive_SetNumObjectSeatsForTenant {
    arg0: string;
    arg1: string;
    arg2: string;
    arg3: number;
}

export interface de_bwl_bwfla_api_objectarchive_SetNumObjectSeatsForTenantBatched {
    arg0: string;
    arg1: de_bwl_bwfla_objectarchive_api_SeatDescription[];
    arg2: string;
}

export interface de_bwl_bwfla_api_objectarchive_SetNumObjectSeatsForTenantBatchedResponse {
}

export interface de_bwl_bwfla_api_objectarchive_SetNumObjectSeatsForTenantResponse {
}

export interface de_bwl_bwfla_api_objectarchive_Sync {
    arg0: string;
}

export interface de_bwl_bwfla_api_objectarchive_SyncAll {
}

export interface de_bwl_bwfla_api_objectarchive_SyncAllResponse {
}

export interface de_bwl_bwfla_api_objectarchive_SyncObjects {
    arg0: string;
    arg1: string[];
}

export interface de_bwl_bwfla_api_objectarchive_SyncObjectsResponse {
    return: de_bwl_bwfla_api_objectarchive_TaskState;
}

export interface de_bwl_bwfla_api_objectarchive_SyncResponse {
}

export interface de_bwl_bwfla_api_objectarchive_TaskState {
    taskId: string;
    done: boolean;
    failed: boolean;
    result: string;
}

export interface de_bwl_bwfla_api_objectarchive_UpdateLabel {
    arg0: string;
    arg1: string;
    arg2: string;
}

export interface de_bwl_bwfla_api_objectarchive_UpdateLabelResponse {
}

export interface de_bwl_bwfla_api_softwarearchive_AddSoftwarePackage {
    arg0: de_bwl_bwfla_common_datatypes_SoftwarePackage;
}

export interface de_bwl_bwfla_api_softwarearchive_AddSoftwarePackageResponse {
    return: boolean;
}

export interface de_bwl_bwfla_api_softwarearchive_Delete {
    arg0: string;
}

export interface de_bwl_bwfla_api_softwarearchive_DeleteResponse {
}

export interface de_bwl_bwfla_api_softwarearchive_GetName {
}

export interface de_bwl_bwfla_api_softwarearchive_GetNameResponse {
    return: string;
}

export interface de_bwl_bwfla_api_softwarearchive_GetNumSoftwareSeatsById {
    arg0: string;
}

export interface de_bwl_bwfla_api_softwarearchive_GetNumSoftwareSeatsByIdResponse {
    return: number;
}

export interface de_bwl_bwfla_api_softwarearchive_GetNumSoftwareSeatsForTenant {
    arg0: string;
    arg1: string;
}

export interface de_bwl_bwfla_api_softwarearchive_GetNumSoftwareSeatsForTenantResponse {
    return: number;
}

export interface de_bwl_bwfla_api_softwarearchive_GetSoftwareDescriptionById {
    arg0: string;
}

export interface de_bwl_bwfla_api_softwarearchive_GetSoftwareDescriptionByIdResponse {
    return: de_bwl_bwfla_common_datatypes_SoftwareDescription;
}

export interface de_bwl_bwfla_api_softwarearchive_GetSoftwareDescriptions {
}

export interface de_bwl_bwfla_api_softwarearchive_GetSoftwareDescriptionsResponse {
    return: any;
}

export interface de_bwl_bwfla_api_softwarearchive_GetSoftwarePackageById {
    arg0: string;
}

export interface de_bwl_bwfla_api_softwarearchive_GetSoftwarePackageByIdResponse {
    return: de_bwl_bwfla_common_datatypes_SoftwarePackage;
}

export interface de_bwl_bwfla_api_softwarearchive_GetSoftwarePackageIds {
}

export interface de_bwl_bwfla_api_softwarearchive_GetSoftwarePackageIdsResponse {
    return: any;
}

export interface de_bwl_bwfla_api_softwarearchive_GetSoftwarePackages {
}

export interface de_bwl_bwfla_api_softwarearchive_GetSoftwarePackagesResponse {
    return: any;
}

export interface de_bwl_bwfla_api_softwarearchive_HasSoftwarePackage {
    arg0: string;
}

export interface de_bwl_bwfla_api_softwarearchive_HasSoftwarePackageResponse {
    return: boolean;
}

export interface de_bwl_bwfla_api_softwarearchive_ResetAllSoftwareSeatsForTenant {
    arg0: string;
}

export interface de_bwl_bwfla_api_softwarearchive_ResetAllSoftwareSeatsForTenantResponse {
}

export interface de_bwl_bwfla_api_softwarearchive_ResetNumSoftwareSeatsForTenant {
    arg0: string;
    arg1: string;
}

export interface de_bwl_bwfla_api_softwarearchive_ResetNumSoftwareSeatsForTenantResponse {
}

export interface de_bwl_bwfla_api_softwarearchive_SetNumSoftwareSeatsForTenant {
    arg0: string;
    arg1: string;
    arg2: number;
}

export interface de_bwl_bwfla_api_softwarearchive_SetNumSoftwareSeatsForTenantResponse {
}

export interface de_bwl_bwfla_api_softwarearchive_SoftwareArchiveWS {
    name: string;
    softwareDescriptions: any;
    softwarePackageIds: any;
    softwarePackages: any;
}

export interface de_bwl_bwfla_api_softwarearchive_SoftwareArchiveWSService extends javax_xml_ws_Service {
    softwareArchiveWSPort: de_bwl_bwfla_api_softwarearchive_SoftwareArchiveWS;
}

export interface de_bwl_bwfla_blobstore_api_Blob extends de_bwl_bwfla_blobstore_api_BlobDescription {
    id: string;
    size: number;
    creationTimestamp: number;
}

export interface de_bwl_bwfla_blobstore_api_BlobDescription {
    namespace: string;
    accessToken: string;
    type: string;
    name: string;
    description: string;
    data: javax_activation_DataHandler;
}

export interface de_bwl_bwfla_blobstore_api_BlobHandle {
    namespace: string;
    id: string;
    accessToken: string;
    valid: boolean;
}

export interface de_bwl_bwfla_blobstore_api_IBlobStore {
}

export interface de_bwl_bwfla_blobstore_client_BlobStoreClient extends de_bwl_bwfla_common_utils_AbstractServiceClient<de_bwl_bwfla_api_blobstore_BlobStoreService> {
}

export interface de_bwl_bwfla_common_concurrent_FirstAccessComputationFuture<T> extends java_util_concurrent_FutureTask<T> {
    exception: java_lang_Throwable;
}

export interface de_bwl_bwfla_common_concurrent_ResettableTimer {
}

export interface de_bwl_bwfla_common_concurrent_SequentialExecutor extends java_util_concurrent_AbstractExecutorService {
    maxBatchExecutionTime: number;
}

export interface de_bwl_bwfla_common_concurrent_SequentialExecutor$BatchTask extends java_lang_Runnable {
}

export interface de_bwl_bwfla_common_concurrent_SequentialExecutor$Task extends java_lang_Runnable, java_lang_Comparable<de_bwl_bwfla_common_concurrent_SequentialExecutor$Task> {
}

/**
 * @deprecated
 */
export interface de_bwl_bwfla_common_database_DocumentCollection<T> {
}

/**
 * @deprecated
 */
export interface de_bwl_bwfla_common_database_MongodbEaasConnector {
}

/**
 * @deprecated
 */
export interface de_bwl_bwfla_common_database_MongodbEaasConnector$DatabaseInstance {
    collections: string[];
}

export interface de_bwl_bwfla_common_database_MongodbEaasConnector$FilterBuilder {
}

export interface de_bwl_bwfla_common_database_document_DocumentCollection<T> {
}

export interface de_bwl_bwfla_common_database_document_DocumentCollection$Batch {
}

export interface de_bwl_bwfla_common_database_document_DocumentCollection$BinaryOperator<T> extends java_util_function_BiFunction<string, T, org_bson_conversions_Bson> {
}

export interface de_bwl_bwfla_common_database_document_DocumentCollection$Filter {
}

export interface de_bwl_bwfla_common_database_document_DocumentCollection$FindResult<T> extends java_lang_Iterable<T>, java_lang_AutoCloseable {
}

export interface de_bwl_bwfla_common_database_document_DocumentCollection$Update {
}

export interface de_bwl_bwfla_common_database_document_DocumentDatabase {
}

export interface de_bwl_bwfla_common_database_document_DocumentDatabaseConnector {
}

export interface de_bwl_bwfla_common_database_document_DocumentUtils {
}

export interface de_bwl_bwfla_common_datatypes_AbstractCredentials {
}

export interface de_bwl_bwfla_common_datatypes_AccessSession extends java_io_Serializable {
    cdLocation: any;
    metadata: any;
}

export interface de_bwl_bwfla_common_datatypes_BWFLAIdentifiers {
}

export interface de_bwl_bwfla_common_datatypes_DigitalObjectMetadata extends de_bwl_bwfla_common_utils_jaxb_JaxbType {
    id: string;
    title: string;
    description: string;
    thumbnail: string;
    summary: string;
    wikiDataId: string;
    customData: { [index: string]: string };
    metsData: string;
}

export interface de_bwl_bwfla_common_datatypes_EnvironmentDescription {
    title: string;
    os: string;
}

export interface de_bwl_bwfla_common_datatypes_GenericId extends de_bwl_bwfla_common_utils_jaxb_JaxbType {
}

export interface de_bwl_bwfla_common_datatypes_IngestSession extends java_io_Serializable {
    cdLocation: any;
}

export interface de_bwl_bwfla_common_datatypes_MonitorValueMap<K> {
}

export interface de_bwl_bwfla_common_datatypes_MonitorValueMap$Adapter extends javax_xml_bind_annotation_adapters_XmlAdapter<string[], de_bwl_bwfla_common_datatypes_MonitorValueMap<K>> {
}

export interface de_bwl_bwfla_common_datatypes_QemuImage extends de_bwl_bwfla_common_utils_jaxb_JaxbType {
    "virtual-size": string;
    filename: string;
    format: string;
    "backing-filename": string;
    "full-backing-filename": string;
}

export interface de_bwl_bwfla_common_datatypes_SoftwareDescription extends de_bwl_bwfla_common_utils_jaxb_JaxbType {
    softwareId: string;
    label: string;
    isOperatingSystem: boolean;
    archiveId: string;
    public: boolean;
}

export interface de_bwl_bwfla_common_datatypes_SoftwarePackage extends de_bwl_bwfla_common_utils_jaxb_JaxbType {
    name: string;
    description: string;
    releaseDate: Date;
    infoSource: string;
    location: string;
    licence: string;
    numSeats: number;
    language: string;
    documentation: string;
    isOperatingSystem: boolean;
    deleted: boolean;
    archive: string;
    objectId: string;
    supportedFileFormats: string[];
    timestamp: string;
    public: boolean;
    id: string;
    qid: string;
}

export interface de_bwl_bwfla_common_datatypes_identification_Content {
    wikidata: string;
    type: string;
}

export interface de_bwl_bwfla_common_datatypes_identification_DiskType extends de_bwl_bwfla_common_utils_jaxb_JaxbType {
    type: string;
    path: string;
    size: string;
    localAlias: string;
    content: de_bwl_bwfla_common_datatypes_identification_Content[];
}

export interface de_bwl_bwfla_common_datatypes_identification_OperatingSystemInformation {
    id: string;
    label: string;
    puids: string[];
    extensions: string[];
}

export interface de_bwl_bwfla_common_datatypes_identification_OperatingSystems extends de_bwl_bwfla_common_utils_jaxb_JaxbType {
    operatingSystemInformations: de_bwl_bwfla_common_datatypes_identification_OperatingSystemInformation[];
}

export interface de_bwl_bwfla_common_exceptions_BWFLAException extends java_lang_Exception {
    messageWithoutSuffix: string;
    id: string;
}

export interface de_bwl_bwfla_common_exceptions_ConcurrentAccessException extends de_bwl_bwfla_common_exceptions_BWFLAException {
}

export interface de_bwl_bwfla_common_exceptions_IllegalEmulatorStateException extends de_bwl_bwfla_common_exceptions_BWFLAException {
    emuCompState: de_bwl_bwfla_common_datatypes_EmuCompState;
}

export interface de_bwl_bwfla_common_interfaces_SoftwareArchiveWSRemote {
    name: string;
    softwareDescriptions: javax_activation_DataHandler;
    softwarePackageIds: javax_activation_DataHandler;
    softwarePackages: javax_activation_DataHandler;
}

export interface de_bwl_bwfla_common_logging_LoggerFactory {
}

export interface de_bwl_bwfla_common_logging_PrefixLogger extends java_util_logging_Logger {
    context: de_bwl_bwfla_common_logging_PrefixLoggerContext;
    callerModuleRef: java_lang_Module;
    logManager: java_util_logging_LogManager;
}

export interface de_bwl_bwfla_common_logging_PrefixLoggerContext {
}

export interface de_bwl_bwfla_common_logging_PrefixLoggerContext$Entry {
}

export interface de_bwl_bwfla_common_services_container_helpers_CdromIsoHelper extends de_bwl_bwfla_common_services_container_helpers_ContainerHelper {
}

export interface de_bwl_bwfla_common_services_container_helpers_ContainerHelper {
}

export interface de_bwl_bwfla_common_services_container_helpers_ContainerHelperFactory {
}

export interface de_bwl_bwfla_common_services_container_helpers_FloppyFat12Helper extends de_bwl_bwfla_common_services_container_helpers_ContainerHelper {
}

export interface de_bwl_bwfla_common_services_container_helpers_HddFat16Helper extends de_bwl_bwfla_common_services_container_helpers_ContainerHelper {
}

export interface de_bwl_bwfla_common_services_container_helpers_HddHfsHelper extends de_bwl_bwfla_common_services_container_helpers_ContainerHelper {
}

export interface de_bwl_bwfla_common_services_container_helpers_HddZipHelper extends de_bwl_bwfla_common_services_container_helpers_ContainerHelper {
}

export interface de_bwl_bwfla_common_services_container_helpers_ImageFileHelper extends de_bwl_bwfla_common_services_container_helpers_ContainerHelper {
}

export interface de_bwl_bwfla_common_services_container_types_CdromContainer extends de_bwl_bwfla_common_services_container_types_Container {
}

export interface de_bwl_bwfla_common_services_container_types_Container {
    file: any;
}

export interface de_bwl_bwfla_common_services_container_types_FloppyContainer extends de_bwl_bwfla_common_services_container_types_Container {
}

export interface de_bwl_bwfla_common_services_container_types_HddContainer extends de_bwl_bwfla_common_services_container_types_Container {
}

export interface de_bwl_bwfla_common_services_container_types_HddHfsContainer extends de_bwl_bwfla_common_services_container_types_Container {
}

export interface de_bwl_bwfla_common_services_container_types_HddZipContainer extends de_bwl_bwfla_common_services_container_types_Container {
}

export interface de_bwl_bwfla_common_services_container_types_ImageFileContainer extends de_bwl_bwfla_common_services_container_types_Container {
}

export interface de_bwl_bwfla_common_services_guacplay_GuacDefs {
}

export interface de_bwl_bwfla_common_services_guacplay_GuacDefs$CompositeMode {
}

export interface de_bwl_bwfla_common_services_guacplay_GuacDefs$EventType {
}

export interface de_bwl_bwfla_common_services_guacplay_GuacDefs$ExtOpCode {
}

export interface de_bwl_bwfla_common_services_guacplay_GuacDefs$KeyCode {
}

export interface de_bwl_bwfla_common_services_guacplay_GuacDefs$KeyState {
}

export interface de_bwl_bwfla_common_services_guacplay_GuacDefs$LineCapStyle {
}

export interface de_bwl_bwfla_common_services_guacplay_GuacDefs$LineJoinStyle {
}

export interface de_bwl_bwfla_common_services_guacplay_GuacDefs$MetadataTag {
}

export interface de_bwl_bwfla_common_services_guacplay_GuacDefs$MouseButton {
}

export interface de_bwl_bwfla_common_services_guacplay_GuacDefs$OpCode {
}

export interface de_bwl_bwfla_common_services_guacplay_GuacDefs$VSyncType {
}

export interface de_bwl_bwfla_common_services_guacplay_GuacSessionLogger extends de_bwl_bwfla_common_services_guacplay_net_IGuacInterceptor {
}

export interface de_bwl_bwfla_common_services_guacplay_capture_ScrShotInstrHandler extends de_bwl_bwfla_common_services_guacplay_protocol_InstructionHandler {
    nextScreenshot: any;
}

export interface de_bwl_bwfla_common_services_guacplay_capture_ScreenShooter extends de_bwl_bwfla_common_services_guacplay_net_IGuacInterceptor {
    nextScreenshot: any;
    finished: boolean;
}

export interface de_bwl_bwfla_common_services_guacplay_capture_ServerMessageProcessor extends de_bwl_bwfla_common_services_guacplay_protocol_AsyncWorker {
}

export interface de_bwl_bwfla_common_services_guacplay_events_EventSink extends de_bwl_bwfla_common_services_guacplay_util_AbstractSink<de_bwl_bwfla_common_services_guacplay_events_IGuacEventListener> {
}

export interface de_bwl_bwfla_common_services_guacplay_events_GuacEvent {
    source: any;
    type: number;
    processed: boolean;
}

export interface de_bwl_bwfla_common_services_guacplay_events_IGuacEventListener {
}

export interface de_bwl_bwfla_common_services_guacplay_events_SessionBeginEvent extends de_bwl_bwfla_common_services_guacplay_events_GuacEvent {
    timestamp: number;
}

export interface de_bwl_bwfla_common_services_guacplay_events_VisualSyncBeginEvent extends de_bwl_bwfla_common_services_guacplay_events_GuacEvent {
    instruction: de_bwl_bwfla_common_services_guacplay_protocol_Instruction;
    timestamp: number;
    intruction: de_bwl_bwfla_common_services_guacplay_protocol_Instruction;
}

export interface de_bwl_bwfla_common_services_guacplay_graphics_AwtUtils {
}

export interface de_bwl_bwfla_common_services_guacplay_graphics_CanvasLayer {
}

export interface de_bwl_bwfla_common_services_guacplay_graphics_OffscreenCanvas extends de_bwl_bwfla_common_services_guacplay_protocol_handler_ISizeInstrListener {
    bufferedImage: java_awt_image_BufferedImage;
    width: number;
    height: number;
}

export interface de_bwl_bwfla_common_services_guacplay_graphics_ScreenObserver {
    enabled: boolean;
}

export interface de_bwl_bwfla_common_services_guacplay_graphics_ScreenRegion {
}

export interface de_bwl_bwfla_common_services_guacplay_graphics_ScreenRegionList {
}

export interface de_bwl_bwfla_common_services_guacplay_io_BlockIndex {
}

export interface de_bwl_bwfla_common_services_guacplay_io_BlockIndexEntry {
    blockName: string;
    blockOffset: number;
    blockLength: number;
}

export interface de_bwl_bwfla_common_services_guacplay_io_BlockLocator {
    valid: boolean;
    offset: number;
    length: number;
}

export interface de_bwl_bwfla_common_services_guacplay_io_BlockReader {
    blockName: string;
}

export interface de_bwl_bwfla_common_services_guacplay_io_BlockReaderException extends java_io_IOException {
}

export interface de_bwl_bwfla_common_services_guacplay_io_BlockWriter {
    blockName: string;
    blockOffset: number;
    blockLength: number;
}

export interface de_bwl_bwfla_common_services_guacplay_io_FileReader extends java_io_Closeable {
}

export interface de_bwl_bwfla_common_services_guacplay_io_FileReaderException extends java_io_IOException {
}

export interface de_bwl_bwfla_common_services_guacplay_io_FileWriter extends java_io_Closeable, java_io_Flushable {
    numBytesFlushed: number;
    state: de_bwl_bwfla_common_services_guacplay_io_FileWriter$State;
    closed: boolean;
    numBytesWritten: number;
}

export interface de_bwl_bwfla_common_services_guacplay_io_FileWriterException extends java_io_IOException {
}

export interface de_bwl_bwfla_common_services_guacplay_io_IndexBlockReader extends de_bwl_bwfla_common_services_guacplay_io_BlockReader {
}

export interface de_bwl_bwfla_common_services_guacplay_io_IndexBlockWriter extends de_bwl_bwfla_common_services_guacplay_io_BlockWriter {
}

export interface de_bwl_bwfla_common_services_guacplay_io_Metadata {
    chunks: { [index: string]: string }[];
    empty: boolean;
}

export interface de_bwl_bwfla_common_services_guacplay_io_MetadataBlockReader extends de_bwl_bwfla_common_services_guacplay_io_BlockReader {
}

export interface de_bwl_bwfla_common_services_guacplay_io_MetadataBlockWriter extends de_bwl_bwfla_common_services_guacplay_io_BlockWriter {
}

export interface de_bwl_bwfla_common_services_guacplay_io_TraceBlockReader extends de_bwl_bwfla_common_services_guacplay_io_BlockReader {
}

export interface de_bwl_bwfla_common_services_guacplay_io_TraceBlockWriter extends de_bwl_bwfla_common_services_guacplay_io_BlockWriter {
    numEntriesWritten: number;
}

export interface de_bwl_bwfla_common_services_guacplay_io_TraceFile {
    metadata: de_bwl_bwfla_common_services_guacplay_io_Metadata;
    charset: java_nio_charset_Charset;
    path: java_nio_file_Path;
    version: de_bwl_bwfla_common_services_guacplay_io_Version;
}

export interface de_bwl_bwfla_common_services_guacplay_io_TraceFileDefs {
}

export interface de_bwl_bwfla_common_services_guacplay_io_TraceFileHeader {
    version: de_bwl_bwfla_common_services_guacplay_io_Version;
}

export interface de_bwl_bwfla_common_services_guacplay_io_TraceFileReader extends de_bwl_bwfla_common_services_guacplay_io_FileReader {
}

export interface de_bwl_bwfla_common_services_guacplay_io_TraceFileWriter extends de_bwl_bwfla_common_services_guacplay_io_FileWriter {
    insideBlock: boolean;
}

export interface de_bwl_bwfla_common_services_guacplay_io_Version {
    major: number;
    minor: number;
    valid: boolean;
}

export interface de_bwl_bwfla_common_services_guacplay_net_DisabledWriter extends org_glyptodon_guacamole_io_GuacamoleWriter {
}

export interface de_bwl_bwfla_common_services_guacplay_net_GuacClientInformationWrapper extends org_glyptodon_guacamole_protocol_GuacamoleClientInformation {
}

export interface de_bwl_bwfla_common_services_guacplay_net_GuacConfigurationWrapper extends org_glyptodon_guacamole_protocol_GuacamoleConfiguration {
    parameterList: de_bwl_bwfla_common_services_guacplay_net_GuacConfigurationWrapper$ParamEntry[];
}

export interface de_bwl_bwfla_common_services_guacplay_net_GuacConfigurationWrapper$ParamEntry {
    name: string;
    value: string;
}

export interface de_bwl_bwfla_common_services_guacplay_net_GuacInterceptorChain extends de_bwl_bwfla_common_services_guacplay_net_IGuacInterceptor {
}

export interface de_bwl_bwfla_common_services_guacplay_net_GuacReader extends org_glyptodon_guacamole_io_ReaderGuacamoleReader, de_bwl_bwfla_common_services_guacplay_net_IGuacReader {
    numBytesRead: number;
    numMsgsRead: number;
}

export interface de_bwl_bwfla_common_services_guacplay_net_GuacReaderWrapper extends de_bwl_bwfla_common_services_guacplay_net_IGuacReader {
}

export interface de_bwl_bwfla_common_services_guacplay_net_GuacSession extends org_glyptodon_guacamole_servlet_GuacamoleSession {
}

export interface de_bwl_bwfla_common_services_guacplay_net_GuacSocket extends org_glyptodon_guacamole_net_GuacamoleSocket {
}

export interface de_bwl_bwfla_common_services_guacplay_net_GuacTunnel extends org_glyptodon_guacamole_net_GuacamoleTunnel {
    cookie: string;
    guacWriter: de_bwl_bwfla_common_services_guacplay_net_GuacWriter;
    guacReader: de_bwl_bwfla_common_services_guacplay_net_GuacReader;
}

export interface de_bwl_bwfla_common_services_guacplay_net_GuacWriter extends org_glyptodon_guacamole_io_GuacamoleWriter {
    numBytesWritten: number;
    numMsgsWritten: number;
}

export interface de_bwl_bwfla_common_services_guacplay_net_IGuacInterceptor {
}

export interface de_bwl_bwfla_common_services_guacplay_net_IGuacReader extends org_glyptodon_guacamole_io_GuacamoleReader {
    interceptor: de_bwl_bwfla_common_services_guacplay_net_IGuacInterceptor;
}

export interface de_bwl_bwfla_common_services_guacplay_net_InterceptorRegistry extends de_bwl_bwfla_common_services_guacplay_util_ObjectRegistry<string, de_bwl_bwfla_common_services_guacplay_net_IGuacInterceptor> {
}

export interface de_bwl_bwfla_common_services_guacplay_net_PlayerSocket extends org_glyptodon_guacamole_net_GuacamoleSocket {
}

export interface de_bwl_bwfla_common_services_guacplay_net_PlayerTunnel extends de_bwl_bwfla_common_services_guacplay_net_GuacTunnel {
}

export interface de_bwl_bwfla_common_services_guacplay_net_TunnelConfig {
    interceptor: de_bwl_bwfla_common_services_guacplay_net_IGuacInterceptor;
    guacdHostname: string;
    guacdPort: number;
    guacamoleConfiguration: org_glyptodon_guacamole_protocol_GuacamoleConfiguration;
    guacamoleClientInformation: org_glyptodon_guacamole_protocol_GuacamoleClientInformation;
}

export interface de_bwl_bwfla_common_services_guacplay_net_TunnelConfigRegistry extends de_bwl_bwfla_common_services_guacplay_util_ObjectRegistry<string, de_bwl_bwfla_common_services_guacplay_net_TunnelConfig> {
}

export interface de_bwl_bwfla_common_services_guacplay_net_TunnelRegistry extends de_bwl_bwfla_common_services_guacplay_util_ObjectRegistry<string, org_glyptodon_guacamole_net_GuacamoleTunnel> {
}

export interface de_bwl_bwfla_common_services_guacplay_png_AlphaPalette extends de_bwl_bwfla_common_services_guacplay_png_Palette {
}

export interface de_bwl_bwfla_common_services_guacplay_png_BufferedImageWrapper {
    bufferedImage: java_awt_image_BufferedImage;
    nextPixel: number;
}

export interface de_bwl_bwfla_common_services_guacplay_png_ColorPalette extends de_bwl_bwfla_common_services_guacplay_png_Palette {
}

export interface de_bwl_bwfla_common_services_guacplay_png_ConverterGreyAlphaToArgb extends de_bwl_bwfla_common_services_guacplay_png_ScanlineConverter {
}

export interface de_bwl_bwfla_common_services_guacplay_png_ConverterGreyToArgb extends de_bwl_bwfla_common_services_guacplay_png_ScanlineConverter {
}

export interface de_bwl_bwfla_common_services_guacplay_png_ConverterIndexedToArgb extends de_bwl_bwfla_common_services_guacplay_png_ScanlineConverter {
}

export interface de_bwl_bwfla_common_services_guacplay_png_ConverterRgbToArgb extends de_bwl_bwfla_common_services_guacplay_png_ScanlineConverter {
}

export interface de_bwl_bwfla_common_services_guacplay_png_ConverterRgbaToArgb extends de_bwl_bwfla_common_services_guacplay_png_ScanlineConverter {
}

export interface de_bwl_bwfla_common_services_guacplay_png_IScanlineFilter {
}

export interface de_bwl_bwfla_common_services_guacplay_png_Palette {
    valid: boolean;
}

export interface de_bwl_bwfla_common_services_guacplay_png_PngDecoder {
}

export interface de_bwl_bwfla_common_services_guacplay_png_PngDefs {
}

export interface de_bwl_bwfla_common_services_guacplay_png_SampleReader16Bit extends de_bwl_bwfla_common_services_guacplay_png_ScanlineWrapper {
}

export interface de_bwl_bwfla_common_services_guacplay_png_SampleReader8Bit extends de_bwl_bwfla_common_services_guacplay_png_ScanlineWrapper {
}

export interface de_bwl_bwfla_common_services_guacplay_png_SampleReaderPacked extends de_bwl_bwfla_common_services_guacplay_png_ScanlineWrapper {
}

export interface de_bwl_bwfla_common_services_guacplay_png_ScanlineConverter {
}

export interface de_bwl_bwfla_common_services_guacplay_png_ScanlineFilterAverage extends de_bwl_bwfla_common_services_guacplay_png_IScanlineFilter {
}

export interface de_bwl_bwfla_common_services_guacplay_png_ScanlineFilterNone extends de_bwl_bwfla_common_services_guacplay_png_IScanlineFilter {
}

export interface de_bwl_bwfla_common_services_guacplay_png_ScanlineFilterPaeth extends de_bwl_bwfla_common_services_guacplay_png_IScanlineFilter {
}

export interface de_bwl_bwfla_common_services_guacplay_png_ScanlineFilterSub extends de_bwl_bwfla_common_services_guacplay_png_IScanlineFilter {
}

export interface de_bwl_bwfla_common_services_guacplay_png_ScanlineFilterUp extends de_bwl_bwfla_common_services_guacplay_png_IScanlineFilter {
}

export interface de_bwl_bwfla_common_services_guacplay_png_ScanlineReader {
    numScanlinesRead: number;
}

export interface de_bwl_bwfla_common_services_guacplay_png_ScanlineWrapper {
    width: number;
    nextSample: number;
}

export interface de_bwl_bwfla_common_services_guacplay_protocol_AsyncWorker extends java_lang_Runnable {
    running: boolean;
}

export interface de_bwl_bwfla_common_services_guacplay_protocol_AverageColorVSyncInstrGenerator extends de_bwl_bwfla_common_services_guacplay_protocol_VSyncInstrGenerator {
}

export interface de_bwl_bwfla_common_services_guacplay_protocol_AverageColorVSyncInstrGenerator$VSyncData {
}

export interface de_bwl_bwfla_common_services_guacplay_protocol_BufferedMessageProcessor extends de_bwl_bwfla_common_services_guacplay_protocol_MessageProcessor {
    messages: de_bwl_bwfla_common_services_guacplay_util_RingBufferSPSC<de_bwl_bwfla_common_services_guacplay_protocol_Message>;
}

export interface de_bwl_bwfla_common_services_guacplay_protocol_EqualPixelsVSyncInstrGenerator extends de_bwl_bwfla_common_services_guacplay_protocol_VSyncInstrGenerator {
}

export interface de_bwl_bwfla_common_services_guacplay_protocol_EqualPixelsVSyncInstrGenerator$VSyncData {
}

export interface de_bwl_bwfla_common_services_guacplay_protocol_IGuacInstructionConsumer {
}

export interface de_bwl_bwfla_common_services_guacplay_protocol_Instruction extends java_lang_Cloneable {
    opcode: string;
    numArguments: number;
    offset: number;
    length: number;
    array: string[];
}

export interface de_bwl_bwfla_common_services_guacplay_protocol_InstructionBuilder {
}

export interface de_bwl_bwfla_common_services_guacplay_protocol_InstructionDescription {
    timestamp: number;
    sourceType: de_bwl_bwfla_common_services_guacplay_GuacDefs$SourceType;
}

export interface de_bwl_bwfla_common_services_guacplay_protocol_InstructionHandler {
    opcode: string;
}

export interface de_bwl_bwfla_common_services_guacplay_protocol_InstructionParser {
    dataArray: string[];
    dataLength: number;
    currentPosition: number;
    instrOffset: number;
    instrLength: number;
    dataOffset: number;
}

export interface de_bwl_bwfla_common_services_guacplay_protocol_InstructionParserException extends de_bwl_bwfla_common_exceptions_BWFLAException {
}

export interface de_bwl_bwfla_common_services_guacplay_protocol_InstructionSink extends de_bwl_bwfla_common_services_guacplay_util_AbstractSink<de_bwl_bwfla_common_services_guacplay_protocol_IGuacInstructionConsumer> {
}

export interface de_bwl_bwfla_common_services_guacplay_protocol_Message {
    timestamp: number;
    offset: number;
    length: number;
    sourceType: de_bwl_bwfla_common_services_guacplay_GuacDefs$SourceType;
    dataArray: string[];
}

export interface de_bwl_bwfla_common_services_guacplay_protocol_MessageProcessor {
    name: string;
    numProcessedMessages: number;
    numSkippedMessages: number;
}

export interface de_bwl_bwfla_common_services_guacplay_protocol_VSyncInstrGenerator {
}

export interface de_bwl_bwfla_common_services_guacplay_protocol_handler_ActfinInstrHandler extends de_bwl_bwfla_common_services_guacplay_protocol_InstructionHandler {
}

export interface de_bwl_bwfla_common_services_guacplay_protocol_handler_ArcInstrHandler extends de_bwl_bwfla_common_services_guacplay_protocol_handler_DrawingInstrHandler {
}

export interface de_bwl_bwfla_common_services_guacplay_protocol_handler_AverageColorVSyncInstrParser extends de_bwl_bwfla_common_services_guacplay_protocol_handler_VSyncInstrParser {
}

export interface de_bwl_bwfla_common_services_guacplay_protocol_handler_CFillInstrHandler extends de_bwl_bwfla_common_services_guacplay_protocol_handler_DrawingInstrHandler {
}

export interface de_bwl_bwfla_common_services_guacplay_protocol_handler_CStrokeInstrHandler extends de_bwl_bwfla_common_services_guacplay_protocol_handler_DrawingInstrHandler {
}

export interface de_bwl_bwfla_common_services_guacplay_protocol_handler_CloseInstrHandler extends de_bwl_bwfla_common_services_guacplay_protocol_handler_DrawingInstrHandler {
}

export interface de_bwl_bwfla_common_services_guacplay_protocol_handler_CursorInstrHandler extends de_bwl_bwfla_common_services_guacplay_protocol_handler_DrawingInstrHandler {
}

export interface de_bwl_bwfla_common_services_guacplay_protocol_handler_CurveInstrHandler extends de_bwl_bwfla_common_services_guacplay_protocol_handler_DrawingInstrHandler {
}

export interface de_bwl_bwfla_common_services_guacplay_protocol_handler_DisposeInstrHandler extends de_bwl_bwfla_common_services_guacplay_protocol_handler_DrawingInstrHandler {
}

export interface de_bwl_bwfla_common_services_guacplay_protocol_handler_DrawingInstrHandler extends de_bwl_bwfla_common_services_guacplay_protocol_InstructionHandler {
}

export interface de_bwl_bwfla_common_services_guacplay_protocol_handler_EqualPixelsVSyncInstrParser extends de_bwl_bwfla_common_services_guacplay_protocol_handler_VSyncInstrParser {
}

export interface de_bwl_bwfla_common_services_guacplay_protocol_handler_ISizeInstrListener {
}

export interface de_bwl_bwfla_common_services_guacplay_protocol_handler_InstructionSkipper extends de_bwl_bwfla_common_services_guacplay_protocol_InstructionHandler {
}

export interface de_bwl_bwfla_common_services_guacplay_protocol_handler_InstructionTrap extends de_bwl_bwfla_common_services_guacplay_protocol_InstructionHandler {
}

export interface de_bwl_bwfla_common_services_guacplay_protocol_handler_KeyInstrHandlerPLAY extends de_bwl_bwfla_common_services_guacplay_protocol_InstructionHandler {
}

export interface de_bwl_bwfla_common_services_guacplay_protocol_handler_LineInstrHandler extends de_bwl_bwfla_common_services_guacplay_protocol_handler_DrawingInstrHandler {
}

export interface de_bwl_bwfla_common_services_guacplay_protocol_handler_MouseInstrHandlerPLAY extends de_bwl_bwfla_common_services_guacplay_protocol_InstructionHandler {
}

export interface de_bwl_bwfla_common_services_guacplay_protocol_handler_MouseInstrHandlerREC extends de_bwl_bwfla_common_services_guacplay_protocol_InstructionHandler {
}

export interface de_bwl_bwfla_common_services_guacplay_protocol_handler_PngInstrDebugHandler extends de_bwl_bwfla_common_services_guacplay_protocol_handler_PngInstrHandler {
    outputDirectory: java_nio_file_Path;
}

export interface de_bwl_bwfla_common_services_guacplay_protocol_handler_PngInstrHandler extends de_bwl_bwfla_common_services_guacplay_protocol_handler_DrawingInstrHandler, de_bwl_bwfla_common_services_guacplay_protocol_handler_ISizeInstrListener {
}

export interface de_bwl_bwfla_common_services_guacplay_protocol_handler_PngInstrHandlerPLAY extends de_bwl_bwfla_common_services_guacplay_protocol_handler_PngInstrHandler, de_bwl_bwfla_common_services_guacplay_events_IGuacEventListener {
}

export interface de_bwl_bwfla_common_services_guacplay_protocol_handler_PngInstrHandlerREC extends de_bwl_bwfla_common_services_guacplay_protocol_handler_PngInstrHandler {
}

export interface de_bwl_bwfla_common_services_guacplay_protocol_handler_RectInstrHandler extends de_bwl_bwfla_common_services_guacplay_protocol_handler_DrawingInstrHandler {
}

export interface de_bwl_bwfla_common_services_guacplay_protocol_handler_SizeInstrHandler extends de_bwl_bwfla_common_services_guacplay_protocol_InstructionHandler {
}

export interface de_bwl_bwfla_common_services_guacplay_protocol_handler_StartInstrHandler extends de_bwl_bwfla_common_services_guacplay_protocol_handler_DrawingInstrHandler {
}

export interface de_bwl_bwfla_common_services_guacplay_protocol_handler_SupdInstrHandler extends de_bwl_bwfla_common_services_guacplay_protocol_InstructionHandler, de_bwl_bwfla_common_services_guacplay_events_IGuacEventListener {
    minTimestamp: number;
}

export interface de_bwl_bwfla_common_services_guacplay_protocol_handler_VSyncInstrHandler extends de_bwl_bwfla_common_services_guacplay_protocol_InstructionHandler, de_bwl_bwfla_common_services_guacplay_events_IGuacEventListener {
}

export interface de_bwl_bwfla_common_services_guacplay_protocol_handler_VSyncInstrParser {
    syncRectPosX: number;
    syncRectPosY: number;
    syncRectWidth: number;
    syncRectHeight: number;
    typeName: string;
    typeId: number;
}

export interface de_bwl_bwfla_common_services_guacplay_record_BufferedTraceWriter extends de_bwl_bwfla_common_services_guacplay_protocol_AsyncWorker {
}

export interface de_bwl_bwfla_common_services_guacplay_record_InstructionForwarder extends de_bwl_bwfla_common_services_guacplay_protocol_InstructionHandler {
}

export interface de_bwl_bwfla_common_services_guacplay_record_SessionRecorder extends de_bwl_bwfla_common_services_guacplay_net_IGuacInterceptor, de_bwl_bwfla_common_services_guacplay_protocol_IGuacInstructionConsumer {
    finished: boolean;
    recording: boolean;
}

export interface de_bwl_bwfla_common_services_guacplay_replay_EntryBasedProgress extends de_bwl_bwfla_common_services_guacplay_replay_IReplayProgress {
}

export interface de_bwl_bwfla_common_services_guacplay_replay_IReplayProgress {
    currentValue: number;
}

export interface de_bwl_bwfla_common_services_guacplay_replay_IWDMetaData {
    uuid: string;
    title: string;
    description: string;
}

export interface de_bwl_bwfla_common_services_guacplay_replay_InstructionForwarder extends de_bwl_bwfla_common_services_guacplay_protocol_InstructionHandler {
}

export interface de_bwl_bwfla_common_services_guacplay_replay_ServerMessageProcessor extends de_bwl_bwfla_common_services_guacplay_protocol_AsyncWorker {
}

export interface de_bwl_bwfla_common_services_guacplay_replay_ServerMessageReader extends de_bwl_bwfla_common_services_guacplay_protocol_AsyncWorker {
}

export interface de_bwl_bwfla_common_services_guacplay_replay_SessionPlayer extends de_bwl_bwfla_common_services_guacplay_util_ICharArrayConsumer, de_bwl_bwfla_common_services_guacplay_events_IGuacEventListener {
    progress: number;
    finished: boolean;
    playerTunnel: de_bwl_bwfla_common_services_guacplay_net_PlayerTunnel;
    traceFilePath: java_nio_file_Path;
    traceMetadata: de_bwl_bwfla_common_services_guacplay_io_Metadata;
    playing: boolean;
}

export interface de_bwl_bwfla_common_services_guacplay_replay_SizeBasedProgress extends de_bwl_bwfla_common_services_guacplay_replay_IReplayProgress {
}

export interface de_bwl_bwfla_common_services_guacplay_replay_TraceFileProcessor extends de_bwl_bwfla_common_services_guacplay_protocol_AsyncWorker {
    numEntriesRead: number;
}

export interface de_bwl_bwfla_common_services_guacplay_tools_Base64Benchmark {
}

export interface de_bwl_bwfla_common_services_guacplay_tools_Base64Benchmark$Timer {
}

export interface de_bwl_bwfla_common_services_guacplay_tools_BusyWorker extends java_lang_Runnable {
}

export interface de_bwl_bwfla_common_services_guacplay_tools_BusyWorkerRunner {
}

export interface de_bwl_bwfla_common_services_guacplay_tools_TraceStats {
}

export interface de_bwl_bwfla_common_services_guacplay_tools_TraceStats$Counter {
}

export interface de_bwl_bwfla_common_services_guacplay_util_AbstractSink<T> {
}

export interface de_bwl_bwfla_common_services_guacplay_util_ArrayBuffer {
    position: number;
}

export interface de_bwl_bwfla_common_services_guacplay_util_ArrayWrapper {
}

export interface de_bwl_bwfla_common_services_guacplay_util_Barrier {
    runnable: java_lang_Runnable;
}

export interface de_bwl_bwfla_common_services_guacplay_util_Base64 {
}

export interface de_bwl_bwfla_common_services_guacplay_util_CharArrayBuffer extends de_bwl_bwfla_common_services_guacplay_util_ArrayBuffer {
    checked: string;
}

export interface de_bwl_bwfla_common_services_guacplay_util_CharArrayWrapper extends de_bwl_bwfla_common_services_guacplay_util_ArrayWrapper {
}

export interface de_bwl_bwfla_common_services_guacplay_util_CharToken {
}

export interface de_bwl_bwfla_common_services_guacplay_util_CharUtils {
}

export interface de_bwl_bwfla_common_services_guacplay_util_ConditionVariable {
}

export interface de_bwl_bwfla_common_services_guacplay_util_FlagSet {
}

export interface de_bwl_bwfla_common_services_guacplay_util_ICharArrayConsumer {
}

export interface de_bwl_bwfla_common_services_guacplay_util_ImageSize {
    width: number;
    height: number;
}

export interface de_bwl_bwfla_common_services_guacplay_util_IntegerToken {
}

export interface de_bwl_bwfla_common_services_guacplay_util_IntegerUtils {
}

export interface de_bwl_bwfla_common_services_guacplay_util_LongToken {
}

export interface de_bwl_bwfla_common_services_guacplay_util_LongUtils {
}

export interface de_bwl_bwfla_common_services_guacplay_util_MathUtils {
}

export interface de_bwl_bwfla_common_services_guacplay_util_NotImplementedException extends java_lang_RuntimeException {
}

export interface de_bwl_bwfla_common_services_guacplay_util_ObjectRegistry<K, V> {
}

export interface de_bwl_bwfla_common_services_guacplay_util_PaddedInteger {
}

export interface de_bwl_bwfla_common_services_guacplay_util_RingBufferSPSC<T> {
    size: number;
    empty: boolean;
}

export interface de_bwl_bwfla_common_services_guacplay_util_StopWatch {
}

export interface de_bwl_bwfla_common_services_guacplay_util_StringBuffer {
}

export interface de_bwl_bwfla_common_services_guacplay_util_TimeUtils {
}

export interface de_bwl_bwfla_common_services_handle_HandleClient {
}

export interface de_bwl_bwfla_common_services_handle_HandleClient$UrlEntry {
}

export interface de_bwl_bwfla_common_services_handle_HandleException extends de_bwl_bwfla_common_exceptions_BWFLAException {
    responseCodeMessage: string;
    errorCode: number;
}

export interface de_bwl_bwfla_common_services_handle_HandleException$ErrorCode {
}

export interface de_bwl_bwfla_common_services_handle_HandleUtils {
}

export interface de_bwl_bwfla_common_services_net_HttpExportServlet extends javax_servlet_http_HttpServlet {
}

export interface de_bwl_bwfla_common_services_net_HttpExportServlet$FileCacheCleanupTask extends java_lang_Runnable {
}

export interface de_bwl_bwfla_common_services_net_HttpExportServlet$FileCacheEntry {
}

export interface de_bwl_bwfla_common_services_net_HttpUtils {
}

export interface de_bwl_bwfla_common_services_rest_ErrorInformation extends de_bwl_bwfla_common_utils_jaxb_JaxbType {
}

export interface de_bwl_bwfla_common_services_rest_ResponseUtils {
}

export interface de_bwl_bwfla_common_services_rest_filters_ErrorResponseJsonFilter extends javax_ws_rs_container_ContainerResponseFilter {
}

export interface de_bwl_bwfla_common_services_security_AbstractAuthenticationFilter extends javax_ws_rs_container_ContainerRequestFilter {
}

export interface de_bwl_bwfla_common_services_security_AuthenticatedUserProducer {
}

export interface de_bwl_bwfla_common_services_security_AuthenticationFilter extends de_bwl_bwfla_common_services_security_AbstractAuthenticationFilter {
}

export interface de_bwl_bwfla_common_services_security_AuthenticationFilter$JwtLoginEvent {
    jwt: com_auth0_jwt_interfaces_DecodedJWT;
}

export interface de_bwl_bwfla_common_services_security_AuthenticationFilterApi extends de_bwl_bwfla_common_services_security_AbstractAuthenticationFilter {
}

export interface de_bwl_bwfla_common_services_security_AuthenticationFilterInternal extends de_bwl_bwfla_common_services_security_AbstractAuthenticationFilter {
}

export interface de_bwl_bwfla_common_services_security_AuthorizationFilter extends javax_ws_rs_container_ContainerRequestFilter {
}

export interface de_bwl_bwfla_common_services_security_EmilEnvironmentOwner {
    username: string;
    usergroup: string;
}

export interface de_bwl_bwfla_common_services_security_EmilEnvironmentPermissions {
    user: de_bwl_bwfla_common_services_security_EmilEnvironmentPermissions$Permissions;
    group: de_bwl_bwfla_common_services_security_EmilEnvironmentPermissions$Permissions;
}

export interface de_bwl_bwfla_common_services_security_EmilEnvironmentPermissions$PermissionAdapter extends javax_xml_bind_annotation_adapters_XmlAdapter<string, de_bwl_bwfla_common_services_security_EmilEnvironmentPermissions$Permissions> {
}

export interface de_bwl_bwfla_common_services_security_MachineToken extends java_util_function_Supplier<string> {
}

export interface de_bwl_bwfla_common_services_security_MachineTokenProvider {
}

export interface de_bwl_bwfla_common_services_security_SOAPClientAuthenticationHandler extends javax_xml_ws_handler_soap_SOAPHandler<{ [index: string]: any }> {
}

export interface de_bwl_bwfla_common_services_security_SOAPClientAuthenticationHandlerResolver extends javax_xml_ws_handler_HandlerResolver {
}

export interface de_bwl_bwfla_common_services_security_ServletAuthenticationFilter extends javax_servlet_Filter {
}

export interface de_bwl_bwfla_common_services_security_UserContext {
    token: string;
    userId: string;
    tenantId: string;
    username: string;
    name: string;
    role: de_bwl_bwfla_common_services_security_Role;
    available: boolean;
}

export interface de_bwl_bwfla_common_services_sse_EventSink {
    closed: boolean;
}

export interface de_bwl_bwfla_common_taskmanager_AbstractTask<R> extends java_lang_Runnable {
    taskId: string;
    taskGroup: de_bwl_bwfla_common_taskmanager_TaskGroup;
    taskResult: java_util_concurrent_CompletableFuture<R>;
}

export interface de_bwl_bwfla_common_taskmanager_BlockingTask<R> extends de_bwl_bwfla_common_taskmanager_AbstractTask<R> {
}

export interface de_bwl_bwfla_common_taskmanager_CompletableTask<R> extends de_bwl_bwfla_common_taskmanager_AbstractTask<R> {
}

export interface de_bwl_bwfla_common_taskmanager_TaskGroup {
    pendingTasks: string[];
    doneTasks: string[];
    done: boolean;
}

export interface de_bwl_bwfla_common_taskmanager_TaskInfo<R> {
    accessTimestamp: number;
    userData: any;
}

export interface de_bwl_bwfla_common_taskmanager_TaskManager<R> {
    taskExpirationTimeout: java_time_Duration;
    garbageCollectionInterval: java_time_Duration;
}

export interface de_bwl_bwfla_common_taskmanager_TaskManager$Groups {
}

export interface de_bwl_bwfla_common_taskmanager_TaskState extends de_bwl_bwfla_common_utils_jaxb_JaxbType {
    taskId: string;
    done: boolean;
    failed: boolean;
    result: string;
}

export interface de_bwl_bwfla_common_utils_AbstractServiceClient<T> {
}

export interface de_bwl_bwfla_common_utils_BwflaFileUtils {
}

export interface de_bwl_bwfla_common_utils_ByteRange {
    length: number;
    startOffset: number;
    endOffset: number;
}

export interface de_bwl_bwfla_common_utils_ByteRangeChannel extends java_nio_channels_ReadableByteChannel {
    range: de_bwl_bwfla_common_utils_ByteRange;
    length: number;
    startOffset: number;
    endOffset: number;
    numBytesRemaining: number;
    dataChannel: java_nio_channels_ReadableByteChannel;
}

export interface de_bwl_bwfla_common_utils_ByteRangeIterator extends java_util_Iterator<de_bwl_bwfla_common_utils_ByteRangeChannel>, java_lang_AutoCloseable {
}

export interface de_bwl_bwfla_common_utils_ConcurrentAccessGuard {
}

export interface de_bwl_bwfla_common_utils_ConfigChangeListener<C> {
}

export interface de_bwl_bwfla_common_utils_ConfigHelpers {
}

/**
 * @deprecated
 */
export interface de_bwl_bwfla_common_utils_DeprecatedProcessRunner {
    command: string[];
    envVariables: { [index: string]: string };
    commandString: string;
    envString: string;
    commandStringWithEnv: string;
    processMonitor: de_bwl_bwfla_common_utils_ProcessMonitor;
    stdInStream: java_io_OutputStream;
    stdInWriter: java_io_Writer;
    stdOutStream: any;
    stdOutReader: any;
    stdOutString: string;
    stdOutPath: java_nio_file_Path;
    stdErrStream: any;
    stdErrReader: any;
    stdErrString: string;
    stdErrPath: java_nio_file_Path;
    processId: number;
    processValid: boolean;
    processRunning: boolean;
    processFinished: boolean;
    returnCode: number;
    logger: java_util_logging_Logger;
    workingDirectory: java_nio_file_Path;
}

export interface de_bwl_bwfla_common_utils_DeprecatedProcessRunner$Result {
}

export interface de_bwl_bwfla_common_utils_DiskDescription {
    transport: de_bwl_bwfla_common_utils_DiskDescription$Transport;
    partitionTableType: de_bwl_bwfla_common_utils_DiskDescription$PartitionTableType;
    modelName: string;
    logicalSectorSize: number;
    physicalSectorSize: number;
    size: number;
    partitions: de_bwl_bwfla_common_utils_DiskDescription$Partition[];
    devicePath: string;
    diskSize: number;
}

export interface de_bwl_bwfla_common_utils_DiskDescription$FieldReader<T> {
}

export interface de_bwl_bwfla_common_utils_DiskDescription$Parser extends java_util_function_Consumer<string>, java_io_Closeable {
}

export interface de_bwl_bwfla_common_utils_DiskDescription$Partition {
    index: number;
    size: number;
    flags: string;
    startOffset: number;
    endOffset: number;
    fileSystemType: string;
    partitionName: string;
    name: string;
}

export interface de_bwl_bwfla_common_utils_EaasBuildInfo {
}

export interface de_bwl_bwfla_common_utils_EaasFileUtils {
}

export interface de_bwl_bwfla_common_utils_ExceptionUtils {
}

export interface de_bwl_bwfla_common_utils_FileRangeIterator extends de_bwl_bwfla_common_utils_ByteRangeIterator {
}

export interface de_bwl_bwfla_common_utils_IReadListener {
}

export interface de_bwl_bwfla_common_utils_ImageInformation {
    backingFile: string;
    fileFormat: de_bwl_bwfla_common_utils_ImageInformation$QemuImageFormat;
}

export interface de_bwl_bwfla_common_utils_Iso9660Utils {
}

export interface de_bwl_bwfla_common_utils_JsonUtils {
}

export interface de_bwl_bwfla_common_utils_MARC21Xml {
}

export interface de_bwl_bwfla_common_utils_METS_MetsUtil {
}

export interface de_bwl_bwfla_common_utils_METS_MetsUtil$FileTypeProperties {
    fileSize: number;
    checksum: string;
    deviceId: string;
    fileFmt: string;
    filename: string;
}

export interface de_bwl_bwfla_common_utils_NetworkUtils {
}

export interface de_bwl_bwfla_common_utils_Pair<A, B> extends java_io_Serializable {
    a: A;
    b: B;
    hashCode: number;
}

export interface de_bwl_bwfla_common_utils_ProcessMonitor {
    values: string[];
}

export interface de_bwl_bwfla_common_utils_ProcessOutput {
}

export interface de_bwl_bwfla_common_utils_ProcessRunner extends java_lang_Process, java_lang_AutoCloseable {
    commandString: string;
    processId: number;
    stderrStream: any;
    stdoutStream: any;
    stdinStream: java_io_OutputStream;
    stdoutString: string;
    stderrString: string;
    workingDirectory: java_nio_file_Path;
    environmentVariables: { [index: string]: string };
}

export interface de_bwl_bwfla_common_utils_StringUtils {
}

export interface de_bwl_bwfla_common_utils_SystemMonitor {
    values: string[];
}

export interface de_bwl_bwfla_common_utils_TaskStack {
    empty: boolean;
}

export interface de_bwl_bwfla_common_utils_TaskStack$IRunnable {
}

export interface de_bwl_bwfla_common_utils_TaskStack$Task {
}

export interface de_bwl_bwfla_common_utils_VncUtils {
}

export interface de_bwl_bwfla_common_utils_WebsocketClient extends javax_websocket_Endpoint {
}

export interface de_bwl_bwfla_common_utils_WebsocketClient$CloseListener extends java_util_EventListener {
}

export interface de_bwl_bwfla_common_utils_WebsocketClient$ErrorListener extends java_util_EventListener {
}

export interface de_bwl_bwfla_common_utils_Zip32Utils {
}

export interface de_bwl_bwfla_common_utils_jaxb_JaxbCollectionReader<T> extends java_util_Iterator<T>, java_lang_AutoCloseable {
}

export interface de_bwl_bwfla_common_utils_jaxb_JaxbCollectionWriter<T> extends java_lang_Runnable {
    dataHandler: javax_activation_DataHandler;
}

export interface de_bwl_bwfla_common_utils_jaxb_JaxbCollectionWriter$ItemMarshaller<I> extends java_util_function_Consumer<I> {
}

export interface de_bwl_bwfla_common_utils_jaxb_JaxbNames {
}

export interface de_bwl_bwfla_common_utils_jaxb_JaxbType {
}

export interface de_bwl_bwfla_common_utils_jaxb_JaxbValidator {
}

export interface de_bwl_bwfla_common_utils_net_PortRangeProvider {
}

export interface de_bwl_bwfla_common_utils_net_PortRangeProvider$Port {
}

export interface de_bwl_bwfla_common_utils_net_PortRangeProvider$PortRange {
    nextAvailablePort: number;
}

export interface de_bwl_bwfla_common_utils_net_SSLUtilities {
}

export interface de_bwl_bwfla_common_utils_net_SSLUtilities$FakeHostnameVerifier extends javax_net_ssl_HostnameVerifier {
}

export interface de_bwl_bwfla_common_utils_net_SSLUtilities$FakeX509TrustManager extends javax_net_ssl_X509TrustManager {
}

export interface de_bwl_bwfla_common_utils_net_SSLUtilities$_FakeHostnameVerifier extends javax_net_ssl_HostnameVerifier {
}

export interface de_bwl_bwfla_common_utils_net_SSLUtilities$_FakeX509TrustManager extends javax_net_ssl_X509TrustManager {
}

export interface de_bwl_bwfla_conf_CommonConf {
    keyfile: string;
    authIndex: string;
    authHandle: string;
    serverdatadir: string;
}

export interface de_bwl_bwfla_conf_CommonSingleton {
}

export interface de_bwl_bwfla_conf_HelpersConf {
    hddFat16Create: string;
    hddFat16Io: string;
    hddHfsCreate: string;
    hddHfsIo: string;
    floppyFat12Create: string;
    floppyFat12Io: string;
}

export interface de_bwl_bwfla_conf_RunnerConf {
    tmpBaseDir: string;
    tmpdirPrefix: string;
    stdoutFilename: string;
    stderrFilename: string;
}

export interface de_bwl_bwfla_configuration_BaseConfigurationPropertySourceProvider extends org_apache_tamaya_spi_PropertySourceProvider {
    defaultOrdinal: number;
}

export interface de_bwl_bwfla_configuration_CustomYamlFormat extends org_apache_tamaya_yaml_YAMLFormat {
}

export interface de_bwl_bwfla_configuration_DefaultConfigurationPropertySourceProvider extends de_bwl_bwfla_configuration_BaseConfigurationPropertySourceProvider {
}

export interface de_bwl_bwfla_configuration_DropInUserConfigurationPropertySourceProvider extends de_bwl_bwfla_configuration_BaseConfigurationPropertySourceProvider {
}

export interface de_bwl_bwfla_configuration_UserConfigurationPropertySourceProvider extends de_bwl_bwfla_configuration_BaseConfigurationPropertySourceProvider {
}

export interface de_bwl_bwfla_configuration_converters_DurationPropertyConverter extends org_apache_tamaya_spi_PropertyConverter<java_time_Duration> {
}

export interface de_bwl_bwfla_configuration_converters_DurationPropertyConverter$DurationParser {
}

export interface de_bwl_bwfla_configuration_converters_PathPropertyConverter extends org_apache_tamaya_spi_PropertyConverter<java_nio_file_Path> {
}

export interface de_bwl_bwfla_eaas_client_EaasClient extends de_bwl_bwfla_common_utils_AbstractServiceClient<de_bwl_bwfla_api_eaas_EaasWSService> {
}

export interface de_bwl_bwfla_eaas_cluster_IClusterManager extends de_bwl_bwfla_eaas_cluster_dump_IDumpable, de_bwl_bwfla_eaas_cluster_IDescribable<de_bwl_bwfla_eaas_cluster_rest_ClusterDescription> {
    name: string;
    resourceProviderComparator: java_util_Comparator<de_bwl_bwfla_eaas_cluster_provider_IResourceProvider>;
    providerNames: string[];
}

export interface de_bwl_bwfla_eaas_cluster_IDescribable<T> {
}

export interface de_bwl_bwfla_eaas_cluster_MutableResourceSpec extends de_bwl_bwfla_eaas_cluster_ResourceSpec {
}

export interface de_bwl_bwfla_eaas_cluster_NodeID extends java_lang_Comparable<de_bwl_bwfla_eaas_cluster_NodeID> {
    ipAddress: string;
    domainName: string;
    nodeAddress: string;
    subDomainName: string;
    protocol: string;
}

export interface de_bwl_bwfla_eaas_cluster_ResourceDiff {
}

export interface de_bwl_bwfla_eaas_cluster_ResourceHandle extends java_lang_Comparable<de_bwl_bwfla_eaas_cluster_ResourceHandle> {
    tenantId: string;
    nodeID: de_bwl_bwfla_eaas_cluster_NodeID;
    allocationID: string;
    providerName: string;
}

export interface de_bwl_bwfla_eaas_cluster_ResourceSpec {
    cpu: number;
    memory: number;
}

export interface de_bwl_bwfla_eaas_cluster_config_BaseConfig extends de_bwl_bwfla_eaas_cluster_dump_IDumpable {
    valid: boolean;
}

export interface de_bwl_bwfla_eaas_cluster_config_ClusterManagerConfig extends de_bwl_bwfla_eaas_cluster_config_BaseConfig {
    name: string;
    resourceProviderConfigs: de_bwl_bwfla_eaas_cluster_config_ResourceProviderConfig[];
}

export interface de_bwl_bwfla_eaas_cluster_config_ClusterManagerConfig$DumpFields {
}

export interface de_bwl_bwfla_eaas_cluster_config_HeterogeneousNodePoolScalerConfig extends de_bwl_bwfla_eaas_cluster_config_NodePoolScalerConfig {
    minPoolSize: de_bwl_bwfla_eaas_cluster_ResourceSpec;
    maxPoolSize: de_bwl_bwfla_eaas_cluster_ResourceSpec;
    maxPoolSizeScaleUpAdjustment: de_bwl_bwfla_eaas_cluster_ResourceSpec;
    maxPoolSizeScaleDownAdjustment: de_bwl_bwfla_eaas_cluster_ResourceSpec;
}

export interface de_bwl_bwfla_eaas_cluster_config_HomogeneousNodePoolScalerConfig extends de_bwl_bwfla_eaas_cluster_config_NodePoolScalerConfig {
    minPoolSize: number;
    maxPoolSize: number;
    maxPoolSizeScaleUpAdjustment: number;
    maxPoolSizeScaleDownAdjustment: number;
}

export interface de_bwl_bwfla_eaas_cluster_config_NodeAllocatorConfig extends de_bwl_bwfla_eaas_cluster_config_BaseConfig {
    healthCheckUrl: string;
    healthCheckConnectTimeout: number;
    healthCheckReadTimeout: number;
    healthCheckFailureTimeout: number;
    healthCheckInterval: number;
    numParallelHealthChecks: number;
    subDomainPrefix: string;
}

export interface de_bwl_bwfla_eaas_cluster_config_NodeAllocatorConfigBLADES extends de_bwl_bwfla_eaas_cluster_config_NodeAllocatorConfig {
    nodeCapacity: de_bwl_bwfla_eaas_cluster_ResourceSpec;
    nodeAddresses: string[];
}

export interface de_bwl_bwfla_eaas_cluster_config_NodeAllocatorConfigBLADES$DumpFields {
}

export interface de_bwl_bwfla_eaas_cluster_config_NodeAllocatorConfigGCE extends de_bwl_bwfla_eaas_cluster_config_NodeAllocatorConfig {
    appName: string;
    projectId: string;
    zoneName: string;
    networkName: string;
    nodeNamePrefix: string;
    serviceAccountCredentialsFile: string;
    vmType: string;
    vmMinCpuPlatform: string;
    vmPersistentDiskType: string;
    vmPersistentDiskSize: number;
    vmPersistentDiskImageUrl: string;
    vmBootPollInterval: number;
    vmBootPollIntervalDelta: number;
    vmMaxNumBootPolls: number;
    vmAccelerators: de_bwl_bwfla_eaas_cluster_config_NodeAllocatorConfigGCE$AcceleratorConfig[];
    apiPollInterval: number;
    apiPollIntervalDelta: number;
    apiRetryInterval: number;
    apiRetryIntervalDelta: number;
    apiMaxNumRetries: number;
}

export interface de_bwl_bwfla_eaas_cluster_config_NodeAllocatorConfigGCE$AcceleratorConfig extends de_bwl_bwfla_eaas_cluster_config_BaseConfig {
    type: string;
    count: number;
}

export interface de_bwl_bwfla_eaas_cluster_config_NodeAllocatorConfigGCE$DumpFields {
}

export interface de_bwl_bwfla_eaas_cluster_config_NodeAllocatorConfigJCLOUDS extends de_bwl_bwfla_eaas_cluster_config_NodeAllocatorConfig {
    securityGroupName: string;
    nodeGroupName: string;
    nodeNamePrefix: string;
    vmNetworkId: string;
    vmHardwareId: string;
    vmImageId: string;
    vmImageSourceType: string;
    vmBootPollInterval: number;
    vmBootPollIntervalDelta: number;
    vmMaxNumBootPolls: number;
    providerType: string;
    providerConfig: de_bwl_bwfla_eaas_cluster_config_NodeAllocatorConfigJCLOUDS$ProviderConfig;
}

export interface de_bwl_bwfla_eaas_cluster_config_NodeAllocatorConfigJCLOUDS$DumpFields {
}

export interface de_bwl_bwfla_eaas_cluster_config_NodeAllocatorConfigJCLOUDS$ImageSourceType {
}

export interface de_bwl_bwfla_eaas_cluster_config_NodeAllocatorConfigJCLOUDS$ProviderConfig {
    type: string;
}

export interface de_bwl_bwfla_eaas_cluster_config_NodeAllocatorConfigJCLOUDS$ProviderConfigOPENSTACK extends de_bwl_bwfla_eaas_cluster_config_NodeAllocatorConfigJCLOUDS$ProviderConfig {
    authEndpoint: string;
    authApiVersion: string;
    authProjectName: string;
    authUser: string;
    authPassword: string;
    keyPairName: string;
}

export interface de_bwl_bwfla_eaas_cluster_config_NodeAllocatorConfigJCLOUDS$ProviderConfigOPENSTACK$DumpFields {
}

export interface de_bwl_bwfla_eaas_cluster_config_NodePoolScalerConfig extends de_bwl_bwfla_eaas_cluster_config_BaseConfig {
    poolScalingInterval: number;
    nodeWarmUpPeriod: number;
    nodeCoolDownPeriod: number;
}

export interface de_bwl_bwfla_eaas_cluster_config_NodePoolScalerConfig$DumpFields {
}

export interface de_bwl_bwfla_eaas_cluster_config_ResourceProviderConfig extends de_bwl_bwfla_eaas_cluster_config_BaseConfig {
    name: string;
    type: string;
    domain: string;
    protocol: string;
    deferredAllocationsGcInterval: number;
    preAllocationRequestHistoryMultiplier: number;
    preAllocationMinBound: de_bwl_bwfla_eaas_cluster_ResourceSpec;
    preAllocationMaxBound: de_bwl_bwfla_eaas_cluster_ResourceSpec;
    labels: de_bwl_bwfla_eaas_cluster_metadata_Label[];
    nodeAllocatorConfig: de_bwl_bwfla_eaas_cluster_config_NodeAllocatorConfig;
    poolScalerConfig: de_bwl_bwfla_eaas_cluster_config_NodePoolScalerConfig;
    requestHistoryUpdateInterval: number;
    requestHistoryMaxRequestAge: number;
    requestHistoryMaxNumRequests: number;
}

export interface de_bwl_bwfla_eaas_cluster_config_ResourceProviderConfig$DumpFields {
}

export interface de_bwl_bwfla_eaas_cluster_config_util_ConfigHelpers extends de_bwl_bwfla_common_utils_ConfigHelpers {
}

export interface de_bwl_bwfla_eaas_cluster_config_util_CpuUnitParser {
}

export interface de_bwl_bwfla_eaas_cluster_config_util_DurationParser {
}

export interface de_bwl_bwfla_eaas_cluster_config_util_DurationPropertyConverter extends org_apache_tamaya_spi_PropertyConverter<number> {
}

export interface de_bwl_bwfla_eaas_cluster_config_util_MemoryUnitParser {
}

export interface de_bwl_bwfla_eaas_cluster_dump_DumpConfig {
}

export interface de_bwl_bwfla_eaas_cluster_dump_DumpFlags {
}

export interface de_bwl_bwfla_eaas_cluster_dump_DumpHelpers {
}

export interface de_bwl_bwfla_eaas_cluster_dump_DumpTrigger extends java_lang_Runnable {
    subResourceDumpHandler: java_lang_Runnable;
    resourceDumpHandler: java_lang_Runnable;
}

export interface de_bwl_bwfla_eaas_cluster_dump_IDumpable {
}

export interface de_bwl_bwfla_eaas_cluster_dump_ObjectDumper {
}

export interface de_bwl_bwfla_eaas_cluster_dump_ObjectDumper$Handler {
}

export interface de_bwl_bwfla_eaas_cluster_exception_AllocationFailureException extends de_bwl_bwfla_common_exceptions_BWFLAException {
}

export interface de_bwl_bwfla_eaas_cluster_exception_MalformedLabelSelectorException extends de_bwl_bwfla_common_exceptions_BWFLAException {
    selector: string;
}

export interface de_bwl_bwfla_eaas_cluster_exception_OutOfResourcesException extends de_bwl_bwfla_eaas_cluster_exception_AllocationFailureException {
}

export interface de_bwl_bwfla_eaas_cluster_exception_QuotaExceededException extends de_bwl_bwfla_eaas_cluster_exception_AllocationFailureException {
}

export interface de_bwl_bwfla_eaas_cluster_metadata_Label {
    key: string;
    value: string;
}

export interface de_bwl_bwfla_eaas_cluster_metadata_LabelIndex {
}

export interface de_bwl_bwfla_eaas_cluster_metadata_LabelSelector {
    negated: boolean;
    key: string;
}

export interface de_bwl_bwfla_eaas_cluster_metadata_LabelSelectorEQ extends de_bwl_bwfla_eaas_cluster_metadata_LabelSelector {
    value: string;
    operator: string;
}

export interface de_bwl_bwfla_eaas_cluster_metadata_LabelSelectorIN extends de_bwl_bwfla_eaas_cluster_metadata_LabelSelector {
    values: string[];
    operator: string;
}

export interface de_bwl_bwfla_eaas_cluster_metadata_LabelSelectorParser {
}

export interface de_bwl_bwfla_eaas_cluster_metadata_LabelSelectorParser$SelectorCtor {
}

export interface de_bwl_bwfla_eaas_cluster_metadata_LabelSelectorParser$SelectorEqCtor extends de_bwl_bwfla_eaas_cluster_metadata_LabelSelectorParser$SelectorCtor {
}

export interface de_bwl_bwfla_eaas_cluster_metadata_LabelSelectorParser$SelectorInCtor extends de_bwl_bwfla_eaas_cluster_metadata_LabelSelectorParser$SelectorCtor {
}

export interface de_bwl_bwfla_eaas_cluster_metadata_LabelSelectorWITH extends de_bwl_bwfla_eaas_cluster_metadata_LabelSelector {
}

export interface de_bwl_bwfla_eaas_cluster_metadata_Labels {
}

export interface de_bwl_bwfla_eaas_cluster_provider_IResourceProvider extends de_bwl_bwfla_eaas_cluster_dump_IDumpable, de_bwl_bwfla_eaas_cluster_IDescribable<de_bwl_bwfla_eaas_cluster_rest_ResourceProviderDescription> {
    name: string;
    labelIndex: de_bwl_bwfla_eaas_cluster_metadata_LabelIndex;
}

export interface de_bwl_bwfla_eaas_cluster_provider_ResourceProviderComparators {
}

export interface de_bwl_bwfla_eaas_cluster_rest_AllocationDescription {
    id: string;
    spec: de_bwl_bwfla_eaas_cluster_ResourceSpec;
}

export interface de_bwl_bwfla_eaas_cluster_rest_ClusterDescription {
    name: string;
    resource_providers: de_bwl_bwfla_eaas_cluster_rest_ResourceProviderDescription[];
    num_resource_providers: number;
}

export interface de_bwl_bwfla_eaas_cluster_rest_NodeDescription {
    id: string;
    is_used: boolean;
    is_healthy: boolean;
    capacity: de_bwl_bwfla_eaas_cluster_ResourceSpec;
    utilization: de_bwl_bwfla_eaas_cluster_ResourceSpec;
    allocations: de_bwl_bwfla_eaas_cluster_rest_AllocationDescription[];
    num_allocations: number;
    usedFlag: boolean;
    healthyFlag: boolean;
}

export interface de_bwl_bwfla_eaas_cluster_rest_NodePoolDescription {
    num_nodes_unused: number;
    num_nodes_unhealthy: number;
    capacity: de_bwl_bwfla_eaas_cluster_ResourceSpec;
    nodes: de_bwl_bwfla_eaas_cluster_rest_NodeDescription[];
    resources_free: de_bwl_bwfla_eaas_cluster_ResourceSpec;
    resources_pending: de_bwl_bwfla_eaas_cluster_ResourceSpec;
    num_nodes: number;
    resources_allocated: de_bwl_bwfla_eaas_cluster_ResourceSpec;
}

export interface de_bwl_bwfla_eaas_cluster_rest_ResourceProviderDescription {
    name: string;
    type: string;
    num_requests_total: number;
    num_requests_deferred: number;
    num_requests_expired: number;
    num_requests_failed: number;
    node_pool: de_bwl_bwfla_eaas_cluster_rest_NodePoolDescription;
}

export interface de_bwl_bwfla_emil_Components$ComponentSession {
    keepaliveTimestamp: number;
    startTimestamp: number;
    id: string;
    request: de_bwl_bwfla_emil_datatypes_rest_ComponentRequest;
    cleanupTasks: de_bwl_bwfla_common_utils_TaskStack;
    eventSink: de_bwl_bwfla_common_services_sse_EventSink;
}

export interface de_bwl_bwfla_emil_Components$ComponentSessionCleanupTrigger extends java_lang_Runnable {
}

export interface de_bwl_bwfla_emil_Components$ComponentSessionStatsWriter extends java_lang_Runnable {
    closed: boolean;
}

export interface de_bwl_bwfla_emil_Components$IResolver {
}

export interface de_bwl_bwfla_emil_Components$SessionDurationNotification {
    duration: string;
    maxDuration: string;
}

export interface de_bwl_bwfla_emil_Components$SessionExpiredNotification extends de_bwl_bwfla_emil_Components$SessionDurationNotification {
    message: string;
}

export interface de_bwl_bwfla_emil_Components$SessionWillExpireNotification extends de_bwl_bwfla_emil_Components$SessionDurationNotification {
    message: string;
}

export interface de_bwl_bwfla_emil_DatabaseEnvironmentsAdapter {
    defaultEnvironments: de_bwl_bwfla_api_imagearchive_DefaultEntry[];
    imageGeneralizationPatches: de_bwl_bwfla_api_imagearchive_ImageGeneralizationPatchDescription[];
    /**
     * @deprecated
     */
    nameIndexes: de_bwl_bwfla_api_imagearchive_ImageNameIndex;
    /**
     * @deprecated
     */
    imagesIndex: de_bwl_bwfla_api_imagearchive_ImageNameIndex;
}

export interface de_bwl_bwfla_emil_EmilDataExport {
}

export interface de_bwl_bwfla_emil_EmilDataImport {
}

export interface de_bwl_bwfla_emil_EmilEnvironmentRepository extends com_openslx_eaas_migration_IMigratable {
    /**
     * @deprecated
     */
    emilEnvironments: java_util_stream_Stream<de_bwl_bwfla_emil_datatypes_EmilEnvironmentUnion>;
    networkEnvironments: java_util_stream_Stream<de_bwl_bwfla_emil_datatypes_NetworkEnvironment>;
    imageArchive: com_openslx_eaas_imagearchive_ImageArchiveClient;
    initialized: boolean;
}

export interface de_bwl_bwfla_emil_EmilEnvironmentRepository$Filter {
    offset: number;
    limit: number;
    fromTime: number;
    untilTime: number;
}

export interface de_bwl_bwfla_emil_EmilEnvironmentRepository$MetadataCollection {
}

export interface de_bwl_bwfla_emil_EmilEvents extends de_bwl_bwfla_emil_EmilRest {
}

export interface de_bwl_bwfla_emil_EmilRest {
}

export interface de_bwl_bwfla_emil_EmulatorRepository$Emulators {
}

export interface de_bwl_bwfla_emil_EmulatorRepository$Images {
}

export interface de_bwl_bwfla_emil_EnvironmentRepository$Actions {
}

export interface de_bwl_bwfla_emil_EnvironmentRepository$DefaultEnvironments {
}

export interface de_bwl_bwfla_emil_EnvironmentRepository$Environments {
}

export interface de_bwl_bwfla_emil_EnvironmentRepository$Images {
}

export interface de_bwl_bwfla_emil_EnvironmentRepository$Patches {
}

export interface de_bwl_bwfla_emil_EnvironmentRepository$Revisions {
}

export interface de_bwl_bwfla_emil_EnvironmentRepository$Templates {
}

export interface de_bwl_bwfla_emil_MetaDataRepositories$AccessMode {
}

export interface de_bwl_bwfla_emil_MetaDataRepositories$RepoType {
}

export interface de_bwl_bwfla_emil_MetaDataSinks {
}

export interface de_bwl_bwfla_emil_MetaDataSinks$EmilEnvironmentSink extends de_bwl_bwfla_metadata_repository_sink_ItemSink {
}

export interface de_bwl_bwfla_emil_MetaDataSinks$EnvironmentSink extends de_bwl_bwfla_metadata_repository_sink_ItemSink {
}

export interface de_bwl_bwfla_emil_MetaDataSinks$SoftwareSink extends de_bwl_bwfla_metadata_repository_sink_ItemSink {
}

export interface de_bwl_bwfla_emil_MetaDataSources {
}

export interface de_bwl_bwfla_emil_MetaDataSources$AbstractEmilEnvironmentSource {
}

export interface de_bwl_bwfla_emil_MetaDataSources$AbstractEnvironmentSource {
}

export interface de_bwl_bwfla_emil_MetaDataSources$AbstractSoftwareSource {
}

export interface de_bwl_bwfla_emil_MetaDataSources$EmilEnvironmentIdentifierSource extends de_bwl_bwfla_emil_MetaDataSources$AbstractEmilEnvironmentSource, de_bwl_bwfla_metadata_repository_source_ItemIdentifierSource {
}

export interface de_bwl_bwfla_emil_MetaDataSources$EmilEnvironmentSource extends de_bwl_bwfla_emil_MetaDataSources$AbstractEmilEnvironmentSource, de_bwl_bwfla_metadata_repository_source_ItemSource {
}

export interface de_bwl_bwfla_emil_MetaDataSources$EnvironmentIdentifierSource extends de_bwl_bwfla_emil_MetaDataSources$AbstractEnvironmentSource, de_bwl_bwfla_metadata_repository_source_ItemIdentifierSource {
}

export interface de_bwl_bwfla_emil_MetaDataSources$EnvironmentSource extends de_bwl_bwfla_emil_MetaDataSources$AbstractEnvironmentSource, de_bwl_bwfla_metadata_repository_source_ItemSource {
}

export interface de_bwl_bwfla_emil_MetaDataSources$SoftwareIdentifierSource extends de_bwl_bwfla_emil_MetaDataSources$AbstractSoftwareSource, de_bwl_bwfla_metadata_repository_source_ItemIdentifierSource {
}

export interface de_bwl_bwfla_emil_MetaDataSources$SoftwareSource extends de_bwl_bwfla_emil_MetaDataSources$AbstractSoftwareSource, de_bwl_bwfla_metadata_repository_source_ItemSource {
}

export interface de_bwl_bwfla_emil_ObjectRepository$Actions {
}

export interface de_bwl_bwfla_emil_ObjectRepository$Archives {
}

export interface de_bwl_bwfla_emil_ObjectRepository$Objects {
}

export interface de_bwl_bwfla_emil_ObjectRepository$Tasks {
}

export interface de_bwl_bwfla_emil_ResourceProviderSelection {
    name: string;
}

export interface de_bwl_bwfla_emil_ResourceProviderSelection$ResourceProviderSelector extends de_bwl_bwfla_eaas_cluster_config_BaseConfig {
    selectors: string[];
    environmentId: string;
}

export interface de_bwl_bwfla_emil_ServerLifecycleHooks {
}

export interface de_bwl_bwfla_emil_SoftwareRepository$SoftwareDescriptions {
}

export interface de_bwl_bwfla_emil_SoftwareRepository$SoftwarePackages {
}

export interface de_bwl_bwfla_emil_datatypes_CheckpointResponse extends de_bwl_bwfla_common_utils_jaxb_JaxbType {
    environmentId: string;
}

export interface de_bwl_bwfla_emil_datatypes_ComputeRequest extends de_bwl_bwfla_common_utils_jaxb_JaxbType {
    components: de_bwl_bwfla_emil_datatypes_ComputeRequest$ComponentSpec[];
    timeout: number;
}

export interface de_bwl_bwfla_emil_datatypes_ComputeRequest$ComponentSpec {
    componentId: string;
    environmentId: string;
    saveEnvironmentLabel: string;
    shouldSaveEnvironment: boolean;
}

export interface de_bwl_bwfla_emil_datatypes_ComputeResponse extends de_bwl_bwfla_common_utils_jaxb_JaxbType {
    id: string;
    result: de_bwl_bwfla_emil_datatypes_ComputeResponse$ComputeResult[];
}

export interface de_bwl_bwfla_emil_datatypes_ComputeResponse$ComputeResult {
    componentId: string;
    state: string;
    environmentId: string;
    resultBlob: string;
}

export interface de_bwl_bwfla_emil_datatypes_ContainerMetadata extends de_bwl_bwfla_common_utils_jaxb_JaxbType {
    dhcp: boolean;
    telnet: boolean;
    process: string;
    args: string[];
}

export interface de_bwl_bwfla_emil_datatypes_CreateNewEnvironmentDesc {
    label: string;
    templateId: string;
    imageId: string;
    objectId: string;
    nativeConfig: string;
}

export interface de_bwl_bwfla_emil_datatypes_CreateSessionResponse extends de_bwl_bwfla_common_utils_jaxb_JaxbType {
    id: string;
}

export interface de_bwl_bwfla_emil_datatypes_DefaultEnvironmentResponse extends de_bwl_bwfla_emil_datatypes_rest_EmilResponseType {
    envId: string;
}

export interface de_bwl_bwfla_emil_datatypes_DigitalObjectMetadataResponse extends de_bwl_bwfla_emil_datatypes_rest_EmilResponseType {
    description: string;
    summary: string;
    thumbnail: string;
    title: string;
    customData: { [index: string]: string };
}

export interface de_bwl_bwfla_emil_datatypes_EaasiSoftwareObject extends de_bwl_bwfla_common_utils_jaxb_JaxbType {
    metsData: string;
    softwarePackage: de_bwl_bwfla_common_datatypes_SoftwarePackage;
}

export interface de_bwl_bwfla_emil_datatypes_EmilContainerEnvironment extends de_bwl_bwfla_emil_datatypes_EmilEnvironment {
    type: "de.bwl.bwfla.emil.datatypes.EmilContainerEnvironment";
    networking: de_bwl_bwfla_emil_datatypes_rest_ContainerNetworkingType;
    input: string;
    output: string;
    args: string[];
    env: string[];
    runtimeId: string;
    serviceContainer: boolean;
    digest: string;
}

export interface de_bwl_bwfla_emil_datatypes_EmilEnvironment extends de_bwl_bwfla_common_utils_jaxb_JaxbType, java_lang_Comparable<de_bwl_bwfla_emil_datatypes_EmilEnvironment> {
    type: "de.bwl.bwfla.emil.datatypes.EmilEnvironment" | "de.bwl.bwfla.emil.datatypes.EmilContainerEnvironment" | "de.bwl.bwfla.emil.datatypes.EmilObjectEnvironment" | "de.bwl.bwfla.emil.datatypes.EmilSessionEnvironment";
    envId: string;
    archive: string;
    os: string;
    title: string;
    description: string;
    version: string;
    emulator: string;
    timeContext: string;
    author: string;
    linuxRuntime: boolean;
    enableRelativeMouse: boolean;
    enablePrinting: boolean;
    shutdownByOs: boolean;
    owner: de_bwl_bwfla_common_services_security_EmilEnvironmentOwner;
    permissions: de_bwl_bwfla_common_services_security_EmilEnvironmentPermissions;
    canProcessAdditionalFiles: boolean;
    xpraEncoding: string;
    networking: de_bwl_bwfla_emil_datatypes_rest_NetworkingType;
    helpText: string;
    parentEnvId: string;
    childrenEnvIds: string[];
    branches: string[];
    timestamp: string;
    deleted: boolean;
    visible: boolean;
}

export interface de_bwl_bwfla_emil_datatypes_EmilEnvironment$Names {
}

export interface de_bwl_bwfla_emil_datatypes_EmilObjectEnvironment extends de_bwl_bwfla_emil_datatypes_EmilEnvironment {
    type: "de.bwl.bwfla.emil.datatypes.EmilObjectEnvironment" | "de.bwl.bwfla.emil.datatypes.EmilSessionEnvironment";
    driveId: number;
    objectId: string;
    objectArchiveId: string;
}

export interface de_bwl_bwfla_emil_datatypes_EmilSessionEnvironment extends de_bwl_bwfla_emil_datatypes_EmilObjectEnvironment {
    type: "de.bwl.bwfla.emil.datatypes.EmilSessionEnvironment";
    baseEnv: string;
    userId: string;
    creationDate: number;
    lastAccess: number;
}

export interface de_bwl_bwfla_emil_datatypes_EmilSoftwareObject {
    id: string;
    objectId: string;
    label: string;
    licenseInformation: string;
    allowedInstances: number;
    nativeFMTs: string[];
    importFMTs: string[];
    exportFMTs: string[];
    archiveId: string;
    isPublic: boolean;
    isOperatingSystem: boolean;
    qid: string;
}

export interface de_bwl_bwfla_emil_datatypes_EnvironmentCreateRequest extends de_bwl_bwfla_common_utils_jaxb_JaxbType {
    templateId: string;
    label: string;
    nativeConfig: string;
    driveSettings: de_bwl_bwfla_emil_datatypes_EnvironmentCreateRequest$DriveSetting[];
    romId: string;
    romLabel: string;
    enablePrinting: boolean;
    enableRelativeMouse: boolean;
    useWebRTC: boolean;
    useXpra: boolean;
    xpraEncoding: string;
    shutdownByOs: boolean;
    operatingSystemId: string;
    enableNetwork: boolean;
    enableInternet: boolean;
}

export interface de_bwl_bwfla_emil_datatypes_EnvironmentCreateRequest$DriveSetting extends de_bwl_bwfla_common_utils_jaxb_JaxbType {
    drive: de_bwl_bwfla_emucomp_api_Drive;
    driveIndex: number;
    operatingSystem: string;
    imageId: string;
    imageArchive: string;
    objectId: string;
    objectArchive: string;
}

export interface de_bwl_bwfla_emil_datatypes_EnvironmentDeleteRequest {
    envId: string;
    deleteMetaData: boolean;
    deleteImage: boolean;
    force: boolean;
}

export interface de_bwl_bwfla_emil_datatypes_EnvironmentInfo extends de_bwl_bwfla_common_utils_jaxb_JaxbType {
    id: string;
    label: string;
    objectEnvironment: boolean;
}

export interface de_bwl_bwfla_emil_datatypes_ForkRevisionRequest extends de_bwl_bwfla_common_utils_jaxb_JaxbType {
    id: string;
}

export interface de_bwl_bwfla_emil_datatypes_ImageGeneralizationPatchRequest extends de_bwl_bwfla_common_utils_jaxb_JaxbType {
    archive: string;
    imageId: string;
    imageType: de_bwl_bwfla_api_imagearchive_ImageType;
}

export interface de_bwl_bwfla_emil_datatypes_ImageGeneralizationPatchResponse extends de_bwl_bwfla_common_utils_jaxb_JaxbType {
    status: string;
    imageId: string;
}

export interface de_bwl_bwfla_emil_datatypes_ImportImageRequest extends de_bwl_bwfla_common_utils_jaxb_JaxbType {
    url: string;
    label: string;
    imageType: string;
}

export interface de_bwl_bwfla_emil_datatypes_MediaDescriptionItem extends de_bwl_bwfla_common_utils_jaxb_JaxbType {
    id: string;
    label: string;
}

export interface de_bwl_bwfla_emil_datatypes_NetworkEnvironment extends de_bwl_bwfla_common_utils_jaxb_JaxbType {
    emilEnvironments: de_bwl_bwfla_emil_datatypes_NetworkEnvironmentElement[];
    title: string;
    envId: string;
    description: string;
    network: string;
    gateway: string;
    upstream_dns: string;
    dnsServiceEnvId: string;
    smbServiceEnvId: string;
    linuxArchiveProxyEnvId: string;
    startupEnvId: string;
    networking: de_bwl_bwfla_emil_datatypes_rest_NetworkEnvironmentNetworkingType;
    type: string;
}

export interface de_bwl_bwfla_emil_datatypes_NetworkEnvironmentCreateReq {
}

export interface de_bwl_bwfla_emil_datatypes_NetworkEnvironmentElement {
    envId: string;
    macAddress: string;
    serverPorts: number[];
    serverIp: string;
    wildcard: boolean;
    label: string;
    title: string;
    /**
     * @deprecated
     */
    fqdn: string;
    fqdnList: string[];
}

export interface de_bwl_bwfla_emil_datatypes_NetworkRequest extends de_bwl_bwfla_common_utils_jaxb_JaxbType {
    components: de_bwl_bwfla_emil_datatypes_NetworkRequest$ComponentSpec[];
    networkEnvironmentId: string;
    dhcp: boolean;
    dhcpNetworkAddress: string;
    dhcpNetworkMask: string;
    tcpGateway: boolean;
    gateway: string;
    network: string;
    tcpGatewayConfig: de_bwl_bwfla_emil_datatypes_NetworkRequest$TcpGatewayConfig;
    internet: boolean;
}

export interface de_bwl_bwfla_emil_datatypes_NetworkRequest$ComponentSpec {
    componentId: string;
    networkLabel: string;
    serverPorts: number[];
    serverIp: string;
    fqdn: string;
    hwAddress: string;
}

export interface de_bwl_bwfla_emil_datatypes_NetworkRequest$TcpGatewayConfig {
    socks: boolean;
    serverPort: string;
    serverIp: string;
}

export interface de_bwl_bwfla_emil_datatypes_NetworkResponse extends de_bwl_bwfla_common_utils_jaxb_JaxbType {
    id: string;
    networkUrls: { [index: string]: java_net_URI };
    localMode: boolean;
}

export interface de_bwl_bwfla_emil_datatypes_ObjectListItem {
    id: string;
    title: string;
    archiveId: string;
    description: string;
    thumbnail: string;
    summary: string;
}

export interface de_bwl_bwfla_emil_datatypes_OverrideCharacterizationRequest {
    objectId: string;
    description: string;
    objectArchive: string;
    environments: de_bwl_bwfla_emil_datatypes_EnvironmentInfo[];
}

export interface de_bwl_bwfla_emil_datatypes_RuntimeListItem {
    id: string;
    name: string;
    description: string;
}

export interface de_bwl_bwfla_emil_datatypes_RuntimeListResponse extends de_bwl_bwfla_common_utils_jaxb_JaxbType {
    status: string;
    runtimes: de_bwl_bwfla_emil_datatypes_RuntimeListItem[];
}

export interface de_bwl_bwfla_emil_datatypes_SaveImportedContainerRequest {
}

export interface de_bwl_bwfla_emil_datatypes_SessionResource extends de_bwl_bwfla_common_utils_jaxb_JaxbType {
    id: string;
    type: de_bwl_bwfla_emil_datatypes_SessionResource$Type;
    keepaliveUrl: string;
    failed: boolean;
}

export interface de_bwl_bwfla_emil_datatypes_SoftwareCollection extends java_lang_Iterable<de_bwl_bwfla_emil_datatypes_EaasiSoftwareObject> {
}

export interface de_bwl_bwfla_emil_datatypes_SoftwareCollection$SoftwareObjectsIterator extends java_util_Iterator<de_bwl_bwfla_emil_datatypes_EaasiSoftwareObject> {
}

export interface de_bwl_bwfla_emil_datatypes_UserSessionResponse extends de_bwl_bwfla_emil_datatypes_rest_EmilResponseType {
    envId: string;
}

export interface de_bwl_bwfla_emil_datatypes_UserSessions {
}

export interface de_bwl_bwfla_emil_datatypes_UserSessions$Monitor extends java_lang_Runnable {
}

export interface de_bwl_bwfla_emil_datatypes_rest_ChangeObjectLabelRequest extends de_bwl_bwfla_common_utils_jaxb_JaxbType {
    label: string;
}

export interface de_bwl_bwfla_emil_datatypes_rest_ClassificationResult extends de_bwl_bwfla_emil_datatypes_rest_EmilResponseType {
    environmentList: de_bwl_bwfla_emil_datatypes_EnvironmentInfo[];
    suggested: de_bwl_bwfla_emil_datatypes_rest_ClassificationResult$OperatingSystem[];
    fileFormatMap: { [index: string]: de_bwl_bwfla_emil_datatypes_rest_ClassificationResult$IdentificationData };
    mediaFormats: { [index: string]: de_bwl_bwfla_common_datatypes_identification_DiskType };
    objectId: string;
    userDescription: string;
}

export interface de_bwl_bwfla_emil_datatypes_rest_ClassificationResult$FileFormat extends de_bwl_bwfla_common_utils_jaxb_JaxbType {
    puid: string;
    name: string;
    count: number;
    fromDate: number;
    toDate: number;
}

export interface de_bwl_bwfla_emil_datatypes_rest_ClassificationResult$IdentificationData extends de_bwl_bwfla_common_utils_jaxb_JaxbType {
    fileFormats: de_bwl_bwfla_emil_datatypes_rest_ClassificationResult$FileFormat[];
}

export interface de_bwl_bwfla_emil_datatypes_rest_ClassificationResult$OperatingSystem {
    defaultEnvironment: de_bwl_bwfla_emil_datatypes_EnvironmentInfo;
}

export interface de_bwl_bwfla_emil_datatypes_rest_ClientClassificationRequest extends de_bwl_bwfla_common_utils_jaxb_JaxbType {
    archiveId: string;
    objectId: string;
    updateClassification: boolean;
    updateProposal: boolean;
    noUpdate: boolean;
    url: string;
    filename: string;
}

export interface de_bwl_bwfla_emil_datatypes_rest_ComponentRequest extends de_bwl_bwfla_emil_datatypes_rest_EmilRequestType {
    type: "ContainerComponentRequest" | "MachineComponentRequest" | "UviComponentRequest" | "NodeTcpComponentRequest" | "SlirpComponentRequest" | "SwitchComponentRequest";
}

export interface de_bwl_bwfla_emil_datatypes_rest_ComponentResponse extends de_bwl_bwfla_common_utils_jaxb_JaxbType {
    id: string;
}

export interface de_bwl_bwfla_emil_datatypes_rest_ComponentStateResponse extends de_bwl_bwfla_emil_datatypes_rest_ComponentResponse {
    state: string;
}

export interface de_bwl_bwfla_emil_datatypes_rest_ComponentWithExternalFilesRequest extends de_bwl_bwfla_emil_datatypes_rest_ComponentRequest {
    type: "ContainerComponentRequest" | "MachineComponentRequest" | "UviComponentRequest";
    inputMedia: de_bwl_bwfla_emil_datatypes_rest_ComponentWithExternalFilesRequest$InputMedium[];
}

export interface de_bwl_bwfla_emil_datatypes_rest_ComponentWithExternalFilesRequest$BaseFileSource extends de_bwl_bwfla_common_utils_jaxb_JaxbType {
    action: de_bwl_bwfla_imagebuilder_api_ImageContentDescription$Action;
    compressionFormat: de_bwl_bwfla_imagebuilder_api_ImageContentDescription$ArchiveFormat;
    name: string;
}

export interface de_bwl_bwfla_emil_datatypes_rest_ComponentWithExternalFilesRequest$FileData extends de_bwl_bwfla_emil_datatypes_rest_ComponentWithExternalFilesRequest$BaseFileSource {
    data: any;
}

export interface de_bwl_bwfla_emil_datatypes_rest_ComponentWithExternalFilesRequest$FileURL extends de_bwl_bwfla_emil_datatypes_rest_ComponentWithExternalFilesRequest$BaseFileSource {
    url: string;
}

export interface de_bwl_bwfla_emil_datatypes_rest_ComponentWithExternalFilesRequest$InputMedium extends de_bwl_bwfla_common_utils_jaxb_JaxbType {
    fileSystemType: de_bwl_bwfla_emucomp_api_FileSystemType;
    sizeInMb: number;
    destination: string;
    mediumType: de_bwl_bwfla_emucomp_api_MediumType;
    extFiles: de_bwl_bwfla_emil_datatypes_rest_ComponentWithExternalFilesRequest$FileURL[];
    inlineFiles: de_bwl_bwfla_emil_datatypes_rest_ComponentWithExternalFilesRequest$FileData[];
    partitiionTableType: de_bwl_bwfla_emucomp_api_PartitionTableType;
    partitionTableType: string;
}

export interface de_bwl_bwfla_emil_datatypes_rest_ContainerComponentRequest extends de_bwl_bwfla_emil_datatypes_rest_ComponentWithExternalFilesRequest {
    type: "ContainerComponentRequest";
    environment: string;
    archive: string;
}

export interface de_bwl_bwfla_emil_datatypes_rest_ContainerNetworkingType extends de_bwl_bwfla_emil_datatypes_rest_NetworkingType {
    dhcpenabled: boolean;
    telnetEnabled: boolean;
}

export interface de_bwl_bwfla_emil_datatypes_rest_CreateContainerImageRequest {
    tag: string;
    digest: string;
    containerType: de_bwl_bwfla_emil_datatypes_rest_CreateContainerImageRequest$ContainerType;
    urlString: string;
    checkForExistingDigest: boolean;
}

export interface de_bwl_bwfla_emil_datatypes_rest_CreateContainerImageResult extends de_bwl_bwfla_common_utils_jaxb_JaxbType {
    containerUrl: string;
    metadata: de_bwl_bwfla_emil_datatypes_rest_CreateContainerImageResult$ContainerImageMetadata;
}

export interface de_bwl_bwfla_emil_datatypes_rest_CreateContainerImageResult$ContainerImageMetadata {
    containerSourceUrl: string;
    entryProcesses: string[];
    envVariables: string[];
    workingDir: string;
    containerDigest: string;
    tag: string;
    emulatorType: string;
    emulatorVersion: string;
}

export interface de_bwl_bwfla_emil_datatypes_rest_DeleteImageRequest {
    imageId: string;
    imageArchive: string;
}

export interface de_bwl_bwfla_emil_datatypes_rest_EmilRequestType extends de_bwl_bwfla_common_utils_jaxb_JaxbType {
    userId: string;
    connectEnvs: boolean;
    networking: de_bwl_bwfla_emil_datatypes_rest_NetworkingType;
}

export interface de_bwl_bwfla_emil_datatypes_rest_EmilResponseType extends de_bwl_bwfla_common_utils_jaxb_JaxbType {
}

export interface de_bwl_bwfla_emil_datatypes_rest_EnvironmentDetails {
    networking: de_bwl_bwfla_emil_datatypes_rest_NetworkingType;
    parentEnvId: string;
    envId: string;
    title: string;
    description: string;
    version: string;
    emulator: string;
    helpText: string;
    enableRelativeMouse: boolean;
    enablePrinting: boolean;
    shutdownByOs: boolean;
    timeContext: string;
    author: string;
    canProcessAdditionalFiles: boolean;
    archive: string;
    xpraEncoding: string;
    owner: string;
    envType: string;
    revisions: de_bwl_bwfla_emil_datatypes_rest_EnvironmentDetails$ParentEnvironment[];
    installedSoftwareIds: de_bwl_bwfla_emil_datatypes_rest_EnvironmentDetails$SoftwareInfo[];
    userTag: string;
    os: string;
    nativeConfig: string;
    useXpra: boolean;
    useWebRTC: boolean;
    containerName: string;
    containerVersion: string;
    drives: de_bwl_bwfla_emucomp_api_Drive[];
    timestamp: string;
    objectId: string;
    objectArchive: string;
    input: string;
    output: string;
    processArgs: string[];
    processEnvs: string[];
    runtimeId: string;
    linuxRuntime: boolean;
    digest: string;
    serviceContainer: boolean;
}

export interface de_bwl_bwfla_emil_datatypes_rest_EnvironmentDetails$ParentEnvironment {
    id: string;
    text: string;
    archive: string;
}

export interface de_bwl_bwfla_emil_datatypes_rest_EnvironmentDetails$SoftwareInfo {
    id: string;
    label: string;
    archive: string;
}

export interface de_bwl_bwfla_emil_datatypes_rest_EnvironmentListItem {
    envId: string;
    title: string;
    archive: string;
    owner: string;
    objectId: string;
    objectArchive: string;
    envType: string;
    operatingSystem: string;
    timestamp: string;
    description: string;
    linuxRuntime: boolean;
    networkEnabled: boolean;
    internetEnabled: boolean;
    serviceContainer: boolean;
}

export interface de_bwl_bwfla_emil_datatypes_rest_EnvironmentMetaData extends de_bwl_bwfla_emil_datatypes_rest_EmilResponseType {
    mediaChangeSupport: boolean;
}

export interface de_bwl_bwfla_emil_datatypes_rest_ExportRequest extends de_bwl_bwfla_common_utils_jaxb_JaxbType {
    envId: string;
    archive: string;
    standalone: boolean;
    deleteAfterExport: boolean;
}

export interface de_bwl_bwfla_emil_datatypes_rest_HandleListResponse extends de_bwl_bwfla_common_utils_jaxb_JaxbType {
}

export interface de_bwl_bwfla_emil_datatypes_rest_HandleRequest extends de_bwl_bwfla_common_utils_jaxb_JaxbType {
    handle: string;
    valueIndex: number;
    handleValue: string;
}

export interface de_bwl_bwfla_emil_datatypes_rest_HandleValueResponse {
    handleValues: string[];
}

export interface de_bwl_bwfla_emil_datatypes_rest_ImageCreateRequest extends de_bwl_bwfla_common_utils_jaxb_JaxbType {
    size: number;
}

export interface de_bwl_bwfla_emil_datatypes_rest_ImportContainerRequest extends de_bwl_bwfla_emil_datatypes_rest_EmilRequestType {
    name: string;
    imageType: de_bwl_bwfla_emil_datatypes_rest_CreateContainerImageRequest$ContainerType;
    imageUrl: string;
    runtimeId: string;
    outputFolder: string;
    inputFolder: string;
    title: string;
    processArgs: string[];
    processEnvs: string[];
    containerDigest: string;
    description: string;
    author: string;
    workingDir: string;
    enableNetwork: boolean;
    customSubdir: string;
    serviceContainer: boolean;
    serviceContainerId: string;
    archive: string;
    guiRequired: boolean;
}

export interface de_bwl_bwfla_emil_datatypes_rest_ImportEmulatorRequest extends de_bwl_bwfla_emil_datatypes_rest_EmilRequestType {
    imageUrl: string;
    metadata: de_bwl_bwfla_emil_datatypes_rest_CreateContainerImageResult$ContainerImageMetadata;
}

export interface de_bwl_bwfla_emil_datatypes_rest_ImportObjectRequest {
    label: string;
    objectArchive: string;
    files: de_bwl_bwfla_emil_datatypes_rest_ImportObjectRequest$ImportFileInfo[];
}

export interface de_bwl_bwfla_emil_datatypes_rest_ImportObjectRequest$ImportFileInfo {
    url: string;
    deviceId: string;
    fileFmt: string;
    filename: string;
}

export interface de_bwl_bwfla_emil_datatypes_rest_LinuxRuntimeContainerReq {
    userContainerEnvironment: string;
    userContainerArchive: string;
    userEnvironment: string[];
    dhcpenabled: boolean;
    telnetEnabled: boolean;
}

export interface de_bwl_bwfla_emil_datatypes_rest_MachineComponentRequest extends de_bwl_bwfla_emil_datatypes_rest_ComponentWithExternalFilesRequest {
    type: "MachineComponentRequest" | "UviComponentRequest";
    environment: string;
    keyboardLayout: string;
    keyboardModel: string;
    object: string;
    archive: string;
    objectArchive: string;
    software: string;
    lockEnvironment: boolean;
    emulatorVersion: string;
    nic: string;
    headless: boolean;
    sessionLifetime: number;
    linuxRuntimeData: de_bwl_bwfla_emil_datatypes_rest_LinuxRuntimeContainerReq;
    outputDriveId: string;
    /**
     * @deprecated
     */
    userMedia: de_bwl_bwfla_emil_datatypes_rest_MachineComponentRequest$UserMedium[];
    drives: de_bwl_bwfla_emil_datatypes_rest_MachineComponentRequest$Drive[];
    hasOutput: boolean;
}

export interface de_bwl_bwfla_emil_datatypes_rest_MachineComponentRequest$Drive extends de_bwl_bwfla_common_utils_jaxb_JaxbType {
    id: string;
    data: de_bwl_bwfla_emil_datatypes_rest_MachineComponentRequest$DriveDataSource;
    bootable: boolean;
}

export interface de_bwl_bwfla_emil_datatypes_rest_MachineComponentRequest$DriveDataSource extends de_bwl_bwfla_common_utils_jaxb_JaxbType {
    kind: string;
}

export interface de_bwl_bwfla_emil_datatypes_rest_MachineComponentRequest$ImageDataSource extends de_bwl_bwfla_emil_datatypes_rest_MachineComponentRequest$DriveDataSource {
    id: string;
}

export interface de_bwl_bwfla_emil_datatypes_rest_MachineComponentRequest$ObjectDataSource extends de_bwl_bwfla_emil_datatypes_rest_MachineComponentRequest$DriveDataSource {
    id: string;
    archive: string;
}

export interface de_bwl_bwfla_emil_datatypes_rest_MachineComponentRequest$SoftwareDataSource extends de_bwl_bwfla_emil_datatypes_rest_MachineComponentRequest$DriveDataSource {
    id: string;
}

export interface de_bwl_bwfla_emil_datatypes_rest_MachineComponentRequest$UserMedium extends de_bwl_bwfla_emil_datatypes_rest_MachineComponentRequest$DriveDataSource {
    mediumType: de_bwl_bwfla_emucomp_api_MediumType;
    url: string;
    name: string;
}

export interface de_bwl_bwfla_emil_datatypes_rest_MachineComponentResponse extends de_bwl_bwfla_emil_datatypes_rest_ComponentResponse {
    driveId: number;
    removbleMediaList: de_bwl_bwfla_emil_datatypes_rest_MachineComponentResponse$RemovableMedia[];
    removableMediaList: de_bwl_bwfla_emil_datatypes_rest_MachineComponentResponse$RemovableMedia[];
}

export interface de_bwl_bwfla_emil_datatypes_rest_MachineComponentResponse$RemovableMedia {
    id: string;
    archive: string;
    driveIndex: string;
}

export interface de_bwl_bwfla_emil_datatypes_rest_MediaChangeRequest extends de_bwl_bwfla_common_utils_jaxb_JaxbType {
    objectId: string;
    driveId: string;
    label: string;
    archiveId: string;
}

export interface de_bwl_bwfla_emil_datatypes_rest_MediaDescriptionResponse extends de_bwl_bwfla_emil_datatypes_rest_EmilResponseType {
    mediaItems: de_bwl_bwfla_emucomp_api_FileCollection;
    metadata: de_bwl_bwfla_common_datatypes_DigitalObjectMetadata;
    objectEnvironments: de_bwl_bwfla_emil_datatypes_rest_ClassificationResult;
}

export interface de_bwl_bwfla_emil_datatypes_rest_NetworkEnvironmentNetworkingType extends de_bwl_bwfla_emil_datatypes_rest_ContainerNetworkingType {
    archiveInternetDate: string;
    allowExternalConnections: boolean;
    dhcpNetworkAddress: string;
    dhcpNetworkMask: string;
    archivedInternetEnabled: boolean;
}

export interface de_bwl_bwfla_emil_datatypes_rest_NetworkingType {
    enableInternet: boolean;
    serverMode: boolean;
    localServerMode: boolean;
    enableSocks: boolean;
    serverPort: string;
    serverIp: string;
    gwPrivateIp: string;
    gwPrivateMask: string;
    connectEnvs: boolean;
    helpText: string;
}

export interface de_bwl_bwfla_emil_datatypes_rest_NodeTcpComponentRequest extends de_bwl_bwfla_emil_datatypes_rest_ComponentRequest {
    type: "NodeTcpComponentRequest";
    config: de_bwl_bwfla_emucomp_api_NodeTcpConfiguration;
}

export interface de_bwl_bwfla_emil_datatypes_rest_ObjectArchivesResponse extends de_bwl_bwfla_emil_datatypes_rest_EmilResponseType {
    archives: string[];
}

export interface de_bwl_bwfla_emil_datatypes_rest_ProcessResultUrl extends de_bwl_bwfla_common_utils_jaxb_JaxbType {
    url: string;
}

export interface de_bwl_bwfla_emil_datatypes_rest_RemoteExportRequest extends de_bwl_bwfla_emil_datatypes_rest_EmilRequestType {
    wsHost: string;
    envId: string[];
    exportObjectEmbedded: boolean;
    objectArchiveHost: string;
}

export interface de_bwl_bwfla_emil_datatypes_rest_RemoteExportResponse extends de_bwl_bwfla_emil_datatypes_rest_TaskStateResponse {
}

export interface de_bwl_bwfla_emil_datatypes_rest_ReplicateImagesRequest extends de_bwl_bwfla_emil_datatypes_rest_EmilRequestType {
    replicateList: string[];
    destArchive: string;
}

export interface de_bwl_bwfla_emil_datatypes_rest_ReplicateImagesResponse extends de_bwl_bwfla_emil_datatypes_rest_EmilResponseType {
    taskList: string[];
}

export interface de_bwl_bwfla_emil_datatypes_rest_RevertRevisionRequest extends de_bwl_bwfla_common_utils_jaxb_JaxbType {
    currentId: string;
    revId: string;
}

export interface de_bwl_bwfla_emil_datatypes_rest_SaveContainerResponse extends de_bwl_bwfla_emil_datatypes_rest_EmilResponseType {
    id: string;
}

export interface de_bwl_bwfla_emil_datatypes_rest_SlirpComponentRequest extends de_bwl_bwfla_emil_datatypes_rest_ComponentRequest {
    type: "SlirpComponentRequest";
    hwAddress: string;
    network: string;
    gateway: string;
    netmask: string;
    dhcp: boolean;
    dnsServer: string;
}

export interface de_bwl_bwfla_emil_datatypes_rest_SnapshotResponse extends de_bwl_bwfla_emil_datatypes_rest_EmilResponseType {
    envId: string;
}

export interface de_bwl_bwfla_emil_datatypes_rest_SwitchComponentRequest extends de_bwl_bwfla_emil_datatypes_rest_ComponentRequest {
    type: "SwitchComponentRequest";
    config: de_bwl_bwfla_emucomp_api_NetworkSwitchConfiguration;
}

export interface de_bwl_bwfla_emil_datatypes_rest_SyncObjectRequest extends de_bwl_bwfla_emil_datatypes_rest_EmilRequestType {
    archive: string;
    objectIDs: string[];
}

export interface de_bwl_bwfla_emil_datatypes_rest_TaskStateResponse extends de_bwl_bwfla_emil_datatypes_rest_EmilResponseType {
    taskId: string;
    userData: { [index: string]: string };
    object: string;
    done: boolean;
}

export interface de_bwl_bwfla_emil_datatypes_rest_UpdateContainerRequest extends de_bwl_bwfla_emil_datatypes_rest_EmilRequestType {
    networking: de_bwl_bwfla_emil_datatypes_rest_ContainerNetworkingType;
    id: string;
    title: string;
    description: string;
    outputFolder: string;
    inputFolder: string;
    author: string;
    containerRuntimeId: string;
    processArgs: string[];
    processEnvs: string[];
}

export interface de_bwl_bwfla_emil_datatypes_rest_UpdateEnvironmentDescriptionRequest extends de_bwl_bwfla_emil_datatypes_rest_EmilRequestType {
    envId: string;
    title: string;
    author: string;
    description: string;
    helpText: string;
    time: string;
    userTag: string;
    os: string;
    nativeConfig: string;
    containerEmulatorVersion: string;
    containerEmulatorName: string;
    enablePrinting: boolean;
    enableRelativeMouse: boolean;
    shutdownByOs: boolean;
    useXpra: boolean;
    useWebRTC: boolean;
    xpraEncoding: string;
    drives: de_bwl_bwfla_emucomp_api_Drive[];
    linuxRuntime: boolean;
    driveSettings: de_bwl_bwfla_emil_datatypes_EnvironmentCreateRequest$DriveSetting[];
    processAdditionalFiles: boolean;
}

export interface de_bwl_bwfla_emil_datatypes_rest_UpdateLatestEmulatorRequest extends de_bwl_bwfla_emil_datatypes_rest_EmilRequestType {
    version: string;
    emulatorName: string;
}

export interface de_bwl_bwfla_emil_datatypes_rest_UploadObjectRequest extends de_bwl_bwfla_emil_datatypes_rest_EmilRequestType {
    objectId: string;
    archive: string;
}

export interface de_bwl_bwfla_emil_datatypes_rest_UploadResponse extends de_bwl_bwfla_emil_datatypes_rest_EmilResponseType {
    /**
     * @deprecated
     */
    uploads: string[];
    uploadedItemList: de_bwl_bwfla_emil_datatypes_rest_UploadResponse$UploadedItem[];
}

export interface de_bwl_bwfla_emil_datatypes_rest_UploadResponse$UploadedItem {
    url: java_net_URL;
    filename: string;
}

export interface de_bwl_bwfla_emil_datatypes_rest_UserInfoResponse extends de_bwl_bwfla_emil_datatypes_rest_EmilResponseType {
    userId: string;
    fullName: string;
    role: string;
    username: string;
}

export interface de_bwl_bwfla_emil_datatypes_rest_UviComponentRequest extends de_bwl_bwfla_emil_datatypes_rest_MachineComponentRequest {
    type: "UviComponentRequest";
    uviUrl: string;
    uviFilename: string;
    uviWriteable: boolean;
    auxFiles: de_bwl_bwfla_emil_datatypes_rest_UviComponentRequest$UviFile[];
}

export interface de_bwl_bwfla_emil_datatypes_rest_UviComponentRequest$UviFile {
    url: string;
    filename: string;
}

export interface de_bwl_bwfla_emil_datatypes_snapshot_SaveCreatedEnvironmentRequest extends de_bwl_bwfla_emil_datatypes_snapshot_SaveDerivateRequest {
    type: "SaveCreatedEnvironmentRequest";
    title: string;
}

export interface de_bwl_bwfla_emil_datatypes_snapshot_SaveDerivateRequest extends de_bwl_bwfla_emil_datatypes_snapshot_SnapshotRequest {
    type: "SaveDerivateRequest" | "SaveCreatedEnvironmentRequest" | "SaveNewEnvironmentRequest";
    softwareId: string;
}

export interface de_bwl_bwfla_emil_datatypes_snapshot_SaveImportRequest extends de_bwl_bwfla_emil_datatypes_snapshot_SnapshotRequest {
    type: "SaveImportRequest";
    title: string;
}

export interface de_bwl_bwfla_emil_datatypes_snapshot_SaveNewEnvironmentRequest extends de_bwl_bwfla_emil_datatypes_snapshot_SaveDerivateRequest {
    type: "SaveNewEnvironmentRequest";
    title: string;
}

export interface de_bwl_bwfla_emil_datatypes_snapshot_SaveObjectEnvironmentRequest extends de_bwl_bwfla_emil_datatypes_snapshot_SnapshotRequest {
    type: "SaveObjectEnvironmentRequest";
    objectId: string;
    title: string;
    objectArchiveId: string;
    driveId: number;
}

export interface de_bwl_bwfla_emil_datatypes_snapshot_SaveUserSessionRequest extends de_bwl_bwfla_emil_datatypes_snapshot_SnapshotRequest {
    type: "SaveUserSessionRequest";
    objectId: string;
    archiveId: string;
}

export interface de_bwl_bwfla_emil_datatypes_snapshot_SnapshotRequest extends de_bwl_bwfla_emil_datatypes_rest_EmilRequestType {
    type: "SaveDerivateRequest" | "SaveCreatedEnvironmentRequest" | "SaveNewEnvironmentRequest" | "SaveImportRequest" | "SaveObjectEnvironmentRequest" | "SaveUserSessionRequest";
    envId: string;
    archive: string;
    message: string;
    author: string;
    cleanRemovableDrives: boolean;
    relativeMouse: boolean;
}

export interface de_bwl_bwfla_emil_filters_CORSResponseFilter extends javax_ws_rs_container_ContainerResponseFilter {
}

export interface de_bwl_bwfla_emil_filters_NoContentFirefoxBugFilter extends javax_ws_rs_container_ContainerResponseFilter {
}

export interface de_bwl_bwfla_emil_session_HeadlessSession extends de_bwl_bwfla_emil_session_Session {
}

export interface de_bwl_bwfla_emil_session_NetworkSession extends de_bwl_bwfla_emil_session_Session {
    switchId: string;
    networkRequest: de_bwl_bwfla_emil_datatypes_NetworkRequest;
}

export interface de_bwl_bwfla_emil_session_Session extends de_bwl_bwfla_common_utils_jaxb_JaxbType {
    name: string;
    lifetime: number;
    detached: boolean;
    failed: boolean;
    expirationTimestamp: number;
}

export interface de_bwl_bwfla_emil_session_SessionComponent extends java_lang_Comparable<de_bwl_bwfla_emil_session_SessionComponent> {
    networkInfo: string;
    customName: string;
}

export interface de_bwl_bwfla_emil_session_SessionManager {
}

export interface de_bwl_bwfla_emil_session_SessionManager$SessionKeepAliveTask extends java_lang_Runnable {
}

export interface de_bwl_bwfla_emil_session_rest_DetachRequest extends de_bwl_bwfla_common_utils_jaxb_JaxbType {
    lifetime: number;
    componentTitle: de_bwl_bwfla_emil_session_rest_DetachRequest$ComponentTitleCreator;
    sessionName: string;
    lifetimeUnit: java_util_concurrent_TimeUnit;
}

export interface de_bwl_bwfla_emil_session_rest_DetachRequest$ComponentTitleCreator extends de_bwl_bwfla_common_utils_jaxb_JaxbType {
    componentName: string;
    componentId: string;
}

export interface de_bwl_bwfla_emil_session_rest_RunningNetworkEnvironmentResponse extends de_bwl_bwfla_common_utils_jaxb_JaxbType {
}

export interface de_bwl_bwfla_emil_session_rest_SessionComponent extends de_bwl_bwfla_common_utils_jaxb_JaxbType {
}

export interface de_bwl_bwfla_emil_session_rest_SessionRequest extends de_bwl_bwfla_emil_session_rest_DetachRequest {
    resources: de_bwl_bwfla_emil_datatypes_SessionResource[];
}

export interface de_bwl_bwfla_emil_session_rest_SessionResponse extends de_bwl_bwfla_common_utils_jaxb_JaxbType {
    components: de_bwl_bwfla_emil_session_rest_SessionComponent[];
    network: de_bwl_bwfla_emil_datatypes_NetworkRequest;
}

export interface de_bwl_bwfla_emil_tasks_BuildContainerImageTask extends de_bwl_bwfla_common_taskmanager_BlockingTask<any> {
    taskResult: java_util_concurrent_CompletableFuture<any>;
}

export interface de_bwl_bwfla_emil_tasks_ClassificationTask extends de_bwl_bwfla_common_taskmanager_BlockingTask<any> {
    taskResult: java_util_concurrent_CompletableFuture<any>;
}

export interface de_bwl_bwfla_emil_tasks_ClassificationTask$ClassifyObjectRequest {
    classification: de_bwl_bwfla_emil_ObjectClassification;
    /**
     * @deprecated
     */
    environments: de_bwl_bwfla_emil_DatabaseEnvironmentsAdapter;
    imagearchive: com_openslx_eaas_imagearchive_ImageArchiveClient;
    metadata: de_bwl_bwfla_emil_EmilEnvironmentRepository;
    input: de_bwl_bwfla_emil_datatypes_rest_ClassificationResult;
    fileCollection: de_bwl_bwfla_emucomp_api_FileCollection;
    url: string;
    filename: string;
    noUpdate: boolean;
    forceProposal: boolean;
    userCtx: de_bwl_bwfla_common_services_security_UserContext;
}

export interface de_bwl_bwfla_emil_tasks_CreateEmptyImageTask extends de_bwl_bwfla_common_taskmanager_BlockingTask<any> {
    taskResult: java_util_concurrent_CompletableFuture<any>;
}

export interface de_bwl_bwfla_emil_tasks_CreateSnapshotTask extends de_bwl_bwfla_common_taskmanager_BlockingTask<any> {
    taskResult: java_util_concurrent_CompletableFuture<any>;
}

export interface de_bwl_bwfla_emil_tasks_ExportEnvironmentTask extends de_bwl_bwfla_common_taskmanager_BlockingTask<any> {
    taskResult: java_util_concurrent_CompletableFuture<any>;
}

export interface de_bwl_bwfla_emil_tasks_ExportEnvironmentTask$ExportEnvironmentRequest {
    exportFilePath: string;
    envHelper: de_bwl_bwfla_emil_DatabaseEnvironmentsAdapter;
    envId: string;
    archive: string;
    environmentRepository: de_bwl_bwfla_emil_EmilEnvironmentRepository;
    userCtx: de_bwl_bwfla_common_services_security_UserContext;
}

export interface de_bwl_bwfla_emil_tasks_ImportContainerTask extends de_bwl_bwfla_common_taskmanager_BlockingTask<any> {
    taskResult: java_util_concurrent_CompletableFuture<any>;
}

export interface de_bwl_bwfla_emil_tasks_ImportEmulatorTask extends de_bwl_bwfla_common_taskmanager_BlockingTask<any> {
    taskResult: java_util_concurrent_CompletableFuture<any>;
}

export interface de_bwl_bwfla_emil_tasks_ImportImageTask extends de_bwl_bwfla_common_taskmanager_CompletableTask<any> {
    taskResult: java_util_concurrent_CompletableFuture<any>;
}

export interface de_bwl_bwfla_emil_tasks_ImportImageTask$ImportImageTaskRequest {
    label: string;
    url: java_net_URL;
    type: de_bwl_bwfla_api_imagearchive_ImageType;
    /**
     * @deprecated
     */
    environmentHelper: de_bwl_bwfla_emil_DatabaseEnvironmentsAdapter;
    imagearchive: com_openslx_eaas_imagearchive_ImageArchiveClient;
    destArchive: string;
}

export interface de_bwl_bwfla_emil_tasks_ImportObjectTask extends de_bwl_bwfla_common_taskmanager_BlockingTask<any> {
    taskResult: java_util_concurrent_CompletableFuture<any>;
}

export interface de_bwl_bwfla_emil_tasks_ReplicateImageTask extends de_bwl_bwfla_common_taskmanager_BlockingTask<any> {
    taskResult: java_util_concurrent_CompletableFuture<any>;
}

export interface de_bwl_bwfla_emil_tasks_ReplicateImageTask$ReplicateImageTaskRequest {
    /**
     * @deprecated
     */
    environmentHelper: de_bwl_bwfla_emil_DatabaseEnvironmentsAdapter;
    imagearchive: com_openslx_eaas_imagearchive_ImageArchiveClient;
    imageProposer: de_bwl_bwfla_imageproposer_client_ImageProposer;
    destArchive: string;
    env: de_bwl_bwfla_emucomp_api_Environment;
    emilEnvironment: de_bwl_bwfla_emil_datatypes_EmilEnvironmentUnion;
    repository: de_bwl_bwfla_emil_EmilEnvironmentRepository;
    userctx: de_bwl_bwfla_common_services_security_UserContext;
}

export interface de_bwl_bwfla_emil_utils_AutoRunScripts {
}

export interface de_bwl_bwfla_emil_utils_AutoRunScripts$CompiledTemplate extends de_bwl_bwfla_emil_utils_AutoRunScripts$Template {
}

export interface de_bwl_bwfla_emil_utils_AutoRunScripts$Template {
    fileName: string;
    targetEncoding: java_nio_charset_Charset;
}

export interface de_bwl_bwfla_emil_utils_AutoRunScripts$TemplateDescription {
    filename: string;
    medium_type: de_bwl_bwfla_emucomp_api_MediumType;
    os_ids: string[];
    encoding: java_nio_charset_Charset;
    template: string;
}

export interface de_bwl_bwfla_emil_utils_AutoRunScripts$Variables {
}

export interface de_bwl_bwfla_emil_utils_BuildContainerUtil {
}

export interface de_bwl_bwfla_emil_utils_EventObserver {
}

export interface de_bwl_bwfla_emil_utils_ImportEmulatorUtil {
}

export interface de_bwl_bwfla_emil_utils_JacksonConfig extends org_jboss_resteasy_plugins_providers_jackson_ResteasyJackson2Provider {
}

export interface de_bwl_bwfla_emil_utils_Snapshot {
}

export interface de_bwl_bwfla_emil_utils_TaskManager$AsyncIoTaskManager extends de_bwl_bwfla_common_taskmanager_TaskManager<any> {
}

export interface de_bwl_bwfla_emil_utils_components_ContainerComponent {
}

export interface de_bwl_bwfla_emil_utils_components_UviComponent {
}

export interface de_bwl_bwfla_emucomp_api_AbstractDataResource extends de_bwl_bwfla_common_utils_jaxb_JaxbType {
    dataResourceType: "Binding" | "BlobStoreBinding" | "FileCollectionEntry" | "ImageArchiveBinding" | "VolatileResource" | "ObjectArchiveBinding";
    id: string;
}

export interface de_bwl_bwfla_emucomp_api_Binding extends de_bwl_bwfla_emucomp_api_AbstractDataResource {
    dataResourceType: "Binding" | "BlobStoreBinding" | "FileCollectionEntry" | "ImageArchiveBinding" | "VolatileResource";
    url: string;
    transport: de_bwl_bwfla_emucomp_api_Binding$TransportType;
    access: de_bwl_bwfla_emucomp_api_Binding$AccessType;
    localAlias: string;
    username: string;
    password: string;
    imagetype: string;
    fileSize: number;
}

export interface de_bwl_bwfla_emucomp_api_BindingDataHandler {
    id: string;
    data: javax_activation_DataHandler;
    url: string;
}

export interface de_bwl_bwfla_emucomp_api_BlobStoreBinding extends de_bwl_bwfla_emucomp_api_Binding {
    dataResourceType: "BlobStoreBinding";
    fileSystemType: de_bwl_bwfla_emucomp_api_FileSystemType;
    resourceType: de_bwl_bwfla_emucomp_api_Binding$ResourceType;
    mountFS: boolean;
    partitionOffset: number;
}

export interface de_bwl_bwfla_emucomp_api_ComponentConfiguration extends de_bwl_bwfla_common_utils_jaxb_JaxbType {
}

export interface de_bwl_bwfla_emucomp_api_ContainerConfiguration extends de_bwl_bwfla_emucomp_api_Environment {
    configurationType: "ContainerConfiguration" | "de.bwl.bwfla.emucomp.api.DockerContainerConfiguration" | "de.bwl.bwfla.emucomp.api.OciContainerConfiguration";
    inputs: de_bwl_bwfla_emucomp_api_ContainerConfiguration$Input[];
    digest: string;
    dataResources: de_bwl_bwfla_emucomp_api_AbstractDataResourceUnion[];
    input: string;
    outputPath: string;
    inputPath: string;
}

export interface de_bwl_bwfla_emucomp_api_ContainerConfiguration$Input {
    binding: string;
    destination: string;
}

export interface de_bwl_bwfla_emucomp_api_ContainerConfiguration$Names {
}

export interface de_bwl_bwfla_emucomp_api_Device {
}

export interface de_bwl_bwfla_emucomp_api_DockerContainerConfiguration extends de_bwl_bwfla_emucomp_api_ContainerConfiguration {
    configurationType: "de.bwl.bwfla.emucomp.api.DockerContainerConfiguration";
    image: string;
}

export interface de_bwl_bwfla_emucomp_api_Drive extends de_bwl_bwfla_emucomp_api_Device {
    data: string;
    iface: string;
    bus: string;
    unit: string;
    filesystem: string;
    type: de_bwl_bwfla_emucomp_api_Drive$DriveType;
    boot: boolean;
    plugged: boolean;
}

export interface de_bwl_bwfla_emucomp_api_EmulationEnvironmentHelper {
}

export interface de_bwl_bwfla_emucomp_api_EmulatorSpec {
    machine: de_bwl_bwfla_emucomp_api_EmulatorSpec$Machine;
    bean: string;
    version: string;
    containerName: string;
    containerVersion: string;
    ociSourceUrl: string;
    digest: string;
}

export interface de_bwl_bwfla_emucomp_api_EmulatorSpec$Architecture {
    id: string;
    name: string;
}

export interface de_bwl_bwfla_emucomp_api_EmulatorSpec$Machine {
    value: string;
    base: string;
}

export interface de_bwl_bwfla_emucomp_api_EmulatorUtils {
}

export interface de_bwl_bwfla_emucomp_api_Environment extends de_bwl_bwfla_emucomp_api_ComponentConfiguration {
    id: string;
    timestamp: string;
    description: de_bwl_bwfla_common_datatypes_EnvironmentDescription;
    configurationType: string;
    metaDataVersion: string;
    userTag: string;
    deleted: boolean;
}

export interface de_bwl_bwfla_emucomp_api_Environment$Fields {
}

export interface de_bwl_bwfla_emucomp_api_FileCollection extends de_bwl_bwfla_common_utils_jaxb_JaxbType {
    files: de_bwl_bwfla_emucomp_api_FileCollectionEntry[];
    id: string;
    archive: string;
    label: string;
    defaultEntry: de_bwl_bwfla_emucomp_api_FileCollectionEntry;
}

export interface de_bwl_bwfla_emucomp_api_FileCollectionEntry extends de_bwl_bwfla_emucomp_api_Binding, java_lang_Comparable<de_bwl_bwfla_emucomp_api_FileCollectionEntry> {
    dataResourceType: "FileCollectionEntry";
    type: de_bwl_bwfla_emucomp_api_Drive$DriveType;
    resourceType: de_bwl_bwfla_emucomp_api_Binding$ResourceType;
    order: string;
    label: string;
    archive: string;
    objectId: string;
    default: boolean;
}

export interface de_bwl_bwfla_emucomp_api_Html5Options {
    pointerLock: boolean;
    crt: string;
}

export interface de_bwl_bwfla_emucomp_api_ImageArchiveBinding extends de_bwl_bwfla_emucomp_api_Binding {
    dataResourceType: "ImageArchiveBinding";
    backendName: string;
    imageId: string;
    type: string;
    fileSystemType: string;
}

export interface de_bwl_bwfla_emucomp_api_ImageMounter extends java_lang_AutoCloseable {
}

export interface de_bwl_bwfla_emucomp_api_ImageMounter$Mount {
    process: de_bwl_bwfla_common_utils_DeprecatedProcessRunner;
    mountPoint: java_nio_file_Path;
    targetImage: string;
    sourceImage: string;
}

export interface de_bwl_bwfla_emucomp_api_InputOptions {
    required: boolean;
    clientKbdLayout: string;
    clientKbdModel: string;
    emulatorKbdLayout: string;
    emulatorKbdModel: string;
}

export interface de_bwl_bwfla_emucomp_api_LoopDeviceManager {
}

export interface de_bwl_bwfla_emucomp_api_MachineConfiguration extends de_bwl_bwfla_emucomp_api_Environment {
    arch: string;
    emulator: de_bwl_bwfla_emucomp_api_EmulatorSpec;
    model: string;
    uiOptions: de_bwl_bwfla_emucomp_api_UiOptions;
    drive: de_bwl_bwfla_emucomp_api_Drive[];
    nic: de_bwl_bwfla_emucomp_api_Nic[];
    outputBindingId: string;
    abstractDataResource: de_bwl_bwfla_emucomp_api_AbstractDataResourceUnion[];
    nativeConfig: de_bwl_bwfla_emucomp_api_MachineConfiguration$NativeConfig;
    operatingSystemId: string;
    checkpointBindingId: string;
    installedSoftwareIds: string[];
    linuxRuntime: boolean;
}

export interface de_bwl_bwfla_emucomp_api_MachineConfiguration$NativeConfig {
    value: string;
    linebreak: string;
}

export interface de_bwl_bwfla_emucomp_api_MachineConfigurationTemplate extends de_bwl_bwfla_emucomp_api_MachineConfiguration {
}

export interface de_bwl_bwfla_emucomp_api_MountOptions {
    readonly: boolean;
    userOptions: { [index: string]: string };
    args: string[];
    inFmt: de_bwl_bwfla_emucomp_api_EmulatorUtils$XmountInputFormat;
    offset: number;
    size: number;
}

export interface de_bwl_bwfla_emucomp_api_NetworkConfiguration extends de_bwl_bwfla_common_utils_jaxb_JaxbType {
    network: string;
    gateway: string;
    upstream_dns: string;
    archived_internet_date: string;
    dhcp: de_bwl_bwfla_emucomp_api_NetworkConfiguration$DHCPConfiguration;
    environments: de_bwl_bwfla_emucomp_api_NetworkConfiguration$EnvironmentNetworkConfiguration[];
}

export interface de_bwl_bwfla_emucomp_api_NetworkConfiguration$DHCPConfiguration {
    ip: string;
}

export interface de_bwl_bwfla_emucomp_api_NetworkConfiguration$EnvironmentNetworkConfiguration {
    mac: string;
    ip: string;
    wildcard: boolean;
    hostnames: string[];
}

export interface de_bwl_bwfla_emucomp_api_NetworkSwitchConfiguration extends de_bwl_bwfla_emucomp_api_ComponentConfiguration {
}

export interface de_bwl_bwfla_emucomp_api_Nic extends de_bwl_bwfla_emucomp_api_Device {
    hwaddress: string;
}

export interface de_bwl_bwfla_emucomp_api_NodeTcpConfiguration extends de_bwl_bwfla_emucomp_api_ComponentConfiguration {
    dhcp: boolean;
    dhcpNetworkAddress: string;
    dhcpNetworkMask: string;
    destIp: string;
    destPort: string;
    hwAddress: string;
    socksUser: string;
    socksPasswd: string;
    socksMode: boolean;
}

export interface de_bwl_bwfla_emucomp_api_ObjectArchiveBinding extends de_bwl_bwfla_emucomp_api_AbstractDataResource {
    dataResourceType: "ObjectArchiveBinding";
    archiveHost: string;
    objectId: string;
    archive: string;
}

export interface de_bwl_bwfla_emucomp_api_OciContainerConfiguration extends de_bwl_bwfla_emucomp_api_ContainerConfiguration {
    configurationType: "de.bwl.bwfla.emucomp.api.OciContainerConfiguration";
    process: de_bwl_bwfla_emucomp_api_OciContainerConfiguration$Process;
    customSubdir: string;
    gui: boolean;
    rootFilesystem: string;
}

export interface de_bwl_bwfla_emucomp_api_OciContainerConfiguration$Process {
    workingDir: string;
    environmentVariables: string[];
    arguments: string[];
}

export interface de_bwl_bwfla_emucomp_api_PrintJob extends de_bwl_bwfla_common_utils_jaxb_JaxbType {
    label: string;
    dataHandler: javax_activation_DataHandler;
}

export interface de_bwl_bwfla_emucomp_api_QcowOptions {
    backingFile: string;
    size: string;
    proxyAuth: string;
}

export interface de_bwl_bwfla_emucomp_api_TimeOptions {
    offset: string;
    epoch: string;
}

export interface de_bwl_bwfla_emucomp_api_UiOptions {
    html5: de_bwl_bwfla_emucomp_api_Html5Options;
    input: de_bwl_bwfla_emucomp_api_InputOptions;
    time: de_bwl_bwfla_emucomp_api_TimeOptions;
    forwarding_system: string;
    audio_system: string;
}

export interface de_bwl_bwfla_emucomp_api_VdeSlirpConfiguration extends de_bwl_bwfla_emucomp_api_ComponentConfiguration {
    hwAddress: string;
    network: string;
    gateway: string;
    netmask: string;
    dnsServer: string;
    dhcpEnabled: boolean;
}

export interface de_bwl_bwfla_emucomp_api_VolatileResource extends de_bwl_bwfla_emucomp_api_Binding {
    dataResourceType: "VolatileResource";
}

export interface de_bwl_bwfla_emucomp_client_ComponentClient extends de_bwl_bwfla_common_utils_AbstractServiceClient<de_bwl_bwfla_api_emucomp_ComponentService> {
}

export interface de_bwl_bwfla_envproposer_EnvironmentProposer$TaskManager extends de_bwl_bwfla_common_taskmanager_TaskManager<any> {
}

export interface de_bwl_bwfla_envproposer_api_Proposal {
    result: de_bwl_bwfla_emil_datatypes_rest_ClassificationResult;
}

export interface de_bwl_bwfla_envproposer_api_ProposalRequest {
    dataUrl: string;
    dataType: de_bwl_bwfla_envproposer_api_ProposalRequest$DataType;
}

export interface de_bwl_bwfla_envproposer_api_ProposalResponse {
    id: string;
    message: string;
}

export interface de_bwl_bwfla_envproposer_impl_FileArchiveUtils {
}

export interface de_bwl_bwfla_envproposer_impl_ProposalTask extends de_bwl_bwfla_common_taskmanager_BlockingTask<any> {
    taskResult: java_util_concurrent_CompletableFuture<any>;
}

export interface de_bwl_bwfla_envproposer_impl_UserData {
    waitLocation: string;
    resultLocation: string;
}

/**
 * @deprecated
 */
export interface de_bwl_bwfla_imagearchive_util_EmulatorRegistryUtil {
}

export interface de_bwl_bwfla_imagearchive_util_EnvironmentsAdapter extends de_bwl_bwfla_imagearchive_util_ImageArchiveWSClient {
    imageGeneralizationPatches: de_bwl_bwfla_api_imagearchive_ImageGeneralizationPatchDescription[];
    /**
     * @deprecated
     */
    nameIndexes: de_bwl_bwfla_api_imagearchive_ImageNameIndex;
}

/**
 * @deprecated
 */
export interface de_bwl_bwfla_imagearchive_util_EnvironmentsAdapter$ImportImageHandle {
    binding: de_bwl_bwfla_emucomp_api_ImageArchiveBinding;
}

export interface de_bwl_bwfla_imagearchive_util_EnvironmentsAdapter$ImportNoFinishedException extends java_lang_Exception {
}

export interface de_bwl_bwfla_imagearchive_util_ImageArchiveWSClient extends java_io_Serializable {
    defaultBackendName: string;
}

export interface de_bwl_bwfla_imagearchive_util_RecordingsAdapter extends de_bwl_bwfla_imagearchive_util_ImageArchiveWSClient {
}

export interface de_bwl_bwfla_imagebuilder_api_IImageBuilder {
}

export interface de_bwl_bwfla_imagebuilder_api_ImageBuildHandle {
    id: string;
}

export interface de_bwl_bwfla_imagebuilder_api_ImageBuilderResult {
    blobHandle: de_bwl_bwfla_blobstore_api_BlobHandle;
    metadata: de_bwl_bwfla_imagebuilder_api_metadata_ImageBuilderMetadata;
}

export interface de_bwl_bwfla_imagebuilder_api_ImageContentDescription {
    action: de_bwl_bwfla_imagebuilder_api_ImageContentDescription$Action;
    name: string;
    subdir: string;
    archiveFormat: de_bwl_bwfla_imagebuilder_api_ImageContentDescription$ArchiveFormat;
    dataSource: de_bwl_bwfla_imagebuilder_api_ImageContentDescription$DataSource;
    streamableDataSource: de_bwl_bwfla_imagebuilder_api_ImageContentDescription$StreamableDataSource;
    dockerDataSource: de_bwl_bwfla_imagebuilder_api_ImageContentDescription$DockerDataSource;
    urlDataSource: java_net_URL;
    byteArrayDataSource: any;
    /**
     * @deprecated
     */
    fileDataSource: java_nio_file_Path;
}

export interface de_bwl_bwfla_imagebuilder_api_ImageContentDescription$ByteArrayDataSource extends de_bwl_bwfla_imagebuilder_api_ImageContentDescription$StreamableDataSource {
    bytes: any;
}

export interface de_bwl_bwfla_imagebuilder_api_ImageContentDescription$DataSource {
    streamable: boolean;
}

export interface de_bwl_bwfla_imagebuilder_api_ImageContentDescription$DockerDataSource extends de_bwl_bwfla_imagebuilder_api_ImageContentDescription$DataSource {
    imageRef: string;
    tag: string;
    digest: string;
    imageArchiveHost: string;
    entryProcesses: string[];
    envVariables: string[];
    workingDir: string;
    version: string;
    emulatorType: string;
}

/**
 * @deprecated
 */
export interface de_bwl_bwfla_imagebuilder_api_ImageContentDescription$FileDataSource extends de_bwl_bwfla_imagebuilder_api_ImageContentDescription$StreamableDataSource {
    path: java_nio_file_Path;
    pathAsString: string;
}

export interface de_bwl_bwfla_imagebuilder_api_ImageContentDescription$StreamableDataSource extends de_bwl_bwfla_imagebuilder_api_ImageContentDescription$DataSource {
}

export interface de_bwl_bwfla_imagebuilder_api_ImageContentDescription$UrlDataSource extends de_bwl_bwfla_imagebuilder_api_ImageContentDescription$StreamableDataSource {
    url: java_net_URL;
}

export interface de_bwl_bwfla_imagebuilder_api_ImageDescription {
    sizeInMb: number;
    label: string;
    partitionTableType: de_bwl_bwfla_emucomp_api_PartitionTableType;
    fileSystemType: de_bwl_bwfla_emucomp_api_FileSystemType;
    mediumType: de_bwl_bwfla_emucomp_api_MediumType;
    partitionStartBlock: number;
    partitionOffset: number;
    contentEntries: de_bwl_bwfla_imagebuilder_api_ImageContentDescription[];
    blockSize: number;
}

export interface de_bwl_bwfla_imagebuilder_api_metadata_DockerImport extends de_bwl_bwfla_imagebuilder_api_metadata_ImageBuilderMetadata {
    imageRef: string;
    tag: string;
    layers: string[];
    emulatorVersion: string;
    digest: string;
    emulatorType: string;
    entryProcesses: any[];
    envVariables: any[];
    workingDir: string;
}

export interface de_bwl_bwfla_imagebuilder_api_metadata_ImageBuilderMetadata extends de_bwl_bwfla_common_utils_jaxb_JaxbType {
}

export interface de_bwl_bwfla_imagebuilder_client_ImageBuilderClient extends de_bwl_bwfla_common_utils_AbstractServiceClient<de_bwl_bwfla_api_imagebuilder_ImageBuilderService> {
}

export interface de_bwl_bwfla_imageclassifier_client_ClassificationEntry extends de_bwl_bwfla_imageclassifier_client_HistogramEntry {
    typeName: string;
    files: string[];
    readQID: string[];
    writeQID: string[];
    fromDate: number;
    toDate: number;
}

export interface de_bwl_bwfla_imageclassifier_client_HistogramEntry {
    value: string;
    type: string;
    count: number;
}

export interface de_bwl_bwfla_imageclassifier_client_Identification<T> {
    fileCollection: de_bwl_bwfla_emucomp_api_FileCollection;
    identificationData: { [index: string]: de_bwl_bwfla_imageclassifier_client_Identification$IdentificationDetails<T> };
    url: string;
    filename: string;
}

export interface de_bwl_bwfla_imageclassifier_client_Identification$IdentificationDetails<T> {
    entries: T[];
    diskType: de_bwl_bwfla_common_datatypes_identification_DiskType;
}

export interface de_bwl_bwfla_imageclassifier_client_IdentificationRequest {
    fileCollection: de_bwl_bwfla_emucomp_api_FileCollection;
    fileUrl: string;
    fileName: string;
    policyUrl: string;
}

export interface de_bwl_bwfla_imageclassifier_client_IdentificationResponse {
    message: string;
}

export interface de_bwl_bwfla_imageclassifier_client_ImageClassifier {
}

export interface de_bwl_bwfla_imageproposer_client_ImageProposer {
}

export interface de_bwl_bwfla_imageproposer_client_Proposal {
    images: string[];
    suggested: { [index: string]: string };
}

export interface de_bwl_bwfla_imageproposer_client_Proposal$OperatingSystem {
}

export interface de_bwl_bwfla_imageproposer_client_ProposalRequest {
    fileFormats: { [index: string]: de_bwl_bwfla_imageproposer_client_ProposalRequest$Entry[] };
    mediaFormats: { [index: string]: de_bwl_bwfla_common_datatypes_identification_DiskType };
}

export interface de_bwl_bwfla_imageproposer_client_ProposalRequest$Entry {
    type: string;
    count: number;
}

export interface de_bwl_bwfla_imageproposer_client_ProposalResponse {
    message: string;
}

export interface de_bwl_bwfla_metadata_repository_GenericRegistry<T> {
}

export interface de_bwl_bwfla_metadata_repository_IMetaDataRepositoryAPI {
}

export interface de_bwl_bwfla_metadata_repository_MetaDataRepositoryAPI extends de_bwl_bwfla_metadata_repository_IMetaDataRepositoryAPI {
    s: string;
}

export interface de_bwl_bwfla_metadata_repository_MetaDataRepositoryAPI$ItemIdentifiers {
}

export interface de_bwl_bwfla_metadata_repository_MetaDataRepositoryAPI$Items {
}

export interface de_bwl_bwfla_metadata_repository_MetaDataRepositoryAPI$Sets {
}

export interface de_bwl_bwfla_metadata_repository_MetaDataSinkRegistry extends de_bwl_bwfla_metadata_repository_GenericRegistry<de_bwl_bwfla_metadata_repository_sink_MetaDataSink> {
}

export interface de_bwl_bwfla_metadata_repository_MetaDataSourceRegistry extends de_bwl_bwfla_metadata_repository_GenericRegistry<de_bwl_bwfla_metadata_repository_source_MetaDataSource> {
}

export interface de_bwl_bwfla_metadata_repository_api_HttpDefs {
}

export interface de_bwl_bwfla_metadata_repository_api_HttpDefs$MediaTypes {
}

export interface de_bwl_bwfla_metadata_repository_api_HttpDefs$Paths {
}

export interface de_bwl_bwfla_metadata_repository_api_HttpDefs$QueryParams {
}

export interface de_bwl_bwfla_metadata_repository_api_ItemDescription extends de_bwl_bwfla_metadata_repository_json_IJsonStreamable {
    identifier: de_bwl_bwfla_metadata_repository_api_ItemIdentifierDescription;
    metaData: string;
}

export interface de_bwl_bwfla_metadata_repository_api_ItemDescription$Fields {
}

export interface de_bwl_bwfla_metadata_repository_api_ItemDescriptionStream extends de_bwl_bwfla_metadata_repository_json_JsonReader {
    header: de_bwl_bwfla_metadata_repository_api_ItemDescriptionStream$Header;
    itemStream: java_util_stream_Stream<de_bwl_bwfla_metadata_repository_api_ItemDescription>;
}

export interface de_bwl_bwfla_metadata_repository_api_ItemDescriptionStream$Fields {
}

export interface de_bwl_bwfla_metadata_repository_api_ItemDescriptionStream$Header extends de_bwl_bwfla_metadata_repository_json_IJsonStreamable {
    totalCount: number;
}

export interface de_bwl_bwfla_metadata_repository_api_ItemDescriptionStream$Parts {
}

export interface de_bwl_bwfla_metadata_repository_api_ItemDescriptionStream$Writer extends de_bwl_bwfla_metadata_repository_json_JsonWriter {
}

export interface de_bwl_bwfla_metadata_repository_api_ItemIdentifierDescription extends de_bwl_bwfla_metadata_repository_json_IJsonStreamable {
    id: string;
    timestamp: string;
    deleted: boolean;
    timestampAsInstant: Date;
    timestampAsLong: number;
    timestampAsDate: Date;
    sets: string[];
}

export interface de_bwl_bwfla_metadata_repository_api_ItemIdentifierDescription$Fields {
}

export interface de_bwl_bwfla_metadata_repository_api_ItemIdentifierDescriptionStream extends de_bwl_bwfla_metadata_repository_json_JsonReader {
    header: de_bwl_bwfla_metadata_repository_api_ItemIdentifierDescriptionStream$Header;
    itemIdentifierStream: java_util_stream_Stream<de_bwl_bwfla_metadata_repository_api_ItemIdentifierDescription>;
}

export interface de_bwl_bwfla_metadata_repository_api_ItemIdentifierDescriptionStream$Fields {
}

export interface de_bwl_bwfla_metadata_repository_api_ItemIdentifierDescriptionStream$Header extends de_bwl_bwfla_metadata_repository_json_IJsonStreamable {
    totalCount: number;
}

export interface de_bwl_bwfla_metadata_repository_api_ItemIdentifierDescriptionStream$Parts {
}

export interface de_bwl_bwfla_metadata_repository_api_ItemIdentifierDescriptionStream$Writer extends de_bwl_bwfla_metadata_repository_json_JsonWriter {
}

export interface de_bwl_bwfla_metadata_repository_api_SetDescription extends de_bwl_bwfla_metadata_repository_json_IJsonStreamable {
    spec: string;
    name: string;
}

export interface de_bwl_bwfla_metadata_repository_api_SetDescription$Fields {
}

export interface de_bwl_bwfla_metadata_repository_api_SetDescriptionStream extends de_bwl_bwfla_metadata_repository_json_JsonReader {
    header: de_bwl_bwfla_metadata_repository_api_SetDescriptionStream$Header;
    setStream: java_util_stream_Stream<de_bwl_bwfla_metadata_repository_api_SetDescription>;
}

export interface de_bwl_bwfla_metadata_repository_api_SetDescriptionStream$Fields {
}

export interface de_bwl_bwfla_metadata_repository_api_SetDescriptionStream$Header extends de_bwl_bwfla_metadata_repository_json_IJsonStreamable {
    totalCount: number;
}

export interface de_bwl_bwfla_metadata_repository_api_SetDescriptionStream$Parts {
}

export interface de_bwl_bwfla_metadata_repository_api_SetDescriptionStream$Writer extends de_bwl_bwfla_metadata_repository_json_JsonWriter {
}

export interface de_bwl_bwfla_metadata_repository_json_IJsonStreamable {
}

export interface de_bwl_bwfla_metadata_repository_json_JsonReader extends java_lang_Iterable<string>, java_lang_AutoCloseable {
}

export interface de_bwl_bwfla_metadata_repository_json_JsonReader$BlockIterator extends java_util_Iterator<string> {
}

export interface de_bwl_bwfla_metadata_repository_json_JsonUtils {
}

export interface de_bwl_bwfla_metadata_repository_json_JsonWriter extends java_lang_AutoCloseable {
}

export interface de_bwl_bwfla_metadata_repository_json_JsonWriter$ValueWriter<T> extends java_util_function_Consumer<T> {
}

export interface de_bwl_bwfla_metadata_repository_sink_ItemSink {
}

export interface de_bwl_bwfla_metadata_repository_sink_MetaDataSink {
}

export interface de_bwl_bwfla_metadata_repository_source_ItemIdentifierSource {
}

export interface de_bwl_bwfla_metadata_repository_source_ItemSource {
}

export interface de_bwl_bwfla_metadata_repository_source_MetaDataSource {
}

export interface de_bwl_bwfla_metadata_repository_source_QueryOptions {
}

export interface de_bwl_bwfla_metadata_repository_source_QueryOptions$Defaults {
}

export interface de_bwl_bwfla_metadata_repository_source_SetSource {
}

export interface de_bwl_bwfla_objectarchive_api_SeatDescription extends de_bwl_bwfla_common_utils_jaxb_JaxbType {
    resource: string;
    numSeats: number;
}

export interface de_bwl_bwfla_objectarchive_api_SeatDescription$Fields {
}

export interface de_bwl_bwfla_objectarchive_util_ObjectArchiveHelper {
    host: string;
    archives: string[];
}

export interface de_bwl_bwfla_restutils_ResponseUtils {
}

export interface de_bwl_bwfla_restutils_ResponseUtils$InternalErrorMessage extends de_bwl_bwfla_restutils_ResponseUtils$Message {
    cause: string;
}

export interface de_bwl_bwfla_restutils_ResponseUtils$Message {
    message: string;
}

export interface de_bwl_bwfla_softwarearchive_util_SoftwareArchiveHelper {
    name: string;
    host: string;
    softwareDescriptions: java_util_stream_Stream<de_bwl_bwfla_common_datatypes_SoftwareDescription>;
    softwarePackageIds: java_util_stream_Stream<string>;
    softwarePackages: java_util_stream_Stream<de_bwl_bwfla_common_datatypes_SoftwarePackage>;
}

export interface de_bwl_bwfla_wikidata_reader_QIDComparator {
}

export interface de_bwl_bwfla_wikidata_reader_QIDDateResolver {
}

export interface de_bwl_bwfla_wikidata_reader_QIDsFinder {
}

export interface de_bwl_bwfla_wikidata_reader_SparqlQueries {
}

export interface de_bwl_bwfla_wikidata_reader_Utils {
}

export interface de_bwl_bwfla_wikidata_reader_WikiRead {
}

export interface de_bwl_bwfla_wikidata_reader_config_Config {
}

export interface de_bwl_bwfla_wikidata_reader_entities_FileFormats {
    readFormats: string[];
    writeFormats: string[];
}

export interface de_bwl_bwfla_wikidata_reader_entities_RelatedQIDS {
    qid: string;
    following: string[];
    followedBy: string[];
}

export interface de_bwl_bwfla_wikidata_reader_entities_SoftwareQIDs {
    readQIDs: string[];
    writeQIDs: string[];
}

export interface de_bwl_bwfla_wikidata_writer_CallClass {
}

export interface de_bwl_bwfla_wikidata_writer_SetValues {
}

export interface de_bwl_bwfla_wikidata_writer_SparqlQueries {
}

export interface de_bwl_bwfla_wikidata_writer_WikiWrite {
}

export interface java_util_concurrent_Executor {
}

export interface java_util_Iterator<E> {
}

export interface javax_xml_namespace_QName extends java_io_Serializable {
}

export interface java_net_URL extends java_io_Serializable {
}

export interface javax_xml_ws_handler_HandlerResolver {
}

export interface javax_xml_ws_Service {
    executor: java_util_concurrent_Executor;
    ports: java_util_Iterator<javax_xml_namespace_QName>;
    wsdldocumentLocation: java_net_URL;
    handlerResolver: javax_xml_ws_handler_HandlerResolver;
    serviceName: javax_xml_namespace_QName;
}

export interface java_lang_Throwable extends java_io_Serializable {
    cause: java_lang_Throwable;
    stackTrace: java_lang_StackTraceElement[];
    message: string;
    suppressed: java_lang_Throwable[];
    localizedMessage: string;
}

export interface java_lang_StackTraceElement extends java_io_Serializable {
    classLoaderName: string;
    moduleName: string;
    moduleVersion: string;
    methodName: string;
    fileName: string;
    lineNumber: number;
    className: string;
    nativeMethod: boolean;
}

export interface java_lang_Exception extends java_lang_Throwable {
}

export interface javax_activation_DataHandler extends java_awt_datatransfer_Transferable {
    dataSource: any;
    name: string;
    content: any;
    inputStream: any;
    preferredCommands: javax_activation_CommandInfo[];
    allCommands: javax_activation_CommandInfo[];
    contentType: string;
    outputStream: java_io_OutputStream;
    commandMap: javax_activation_CommandMap;
}

export interface java_util_concurrent_AbstractExecutorService extends java_util_concurrent_ExecutorService {
}

export interface java_lang_Runnable {
}

export interface java_lang_AutoCloseable {
}

export interface java_io_Serializable {
}

export interface java_util_logging_Logger {
    name: string;
    parent: java_util_logging_Logger;
    resourceBundle: java_util_ResourceBundle;
    resourceBundleName: string;
    filter: java_util_logging_Filter;
    level: java_util_logging_Level;
    handlers: java_util_logging_Handler[];
    useParentHandlers: boolean;
}

export interface java_util_ResourceBundle {
    locale: java_util_Locale;
    keys: java_util_Enumeration<string>;
    baseBundleName: string;
}

export interface java_util_logging_Filter {
}

export interface java_util_logging_Level extends java_io_Serializable {
    name: string;
    resourceBundleName: string;
    localizedName: string;
}

export interface java_util_logging_Handler {
    filter: java_util_logging_Filter;
    formatter: java_util_logging_Formatter;
    errorManager: java_util_logging_ErrorManager;
    encoding: string;
    level: java_util_logging_Level;
}

export interface java_lang_Module extends java_lang_reflect_AnnotatedElement {
    layer: java_lang_ModuleLayer;
    name: string;
    descriptor: java_lang_module_ModuleDescriptor;
    classLoader: java_lang_ClassLoader;
    named: boolean;
    packages: string[];
}

export interface java_util_logging_LogManager {
    loggerNames: java_util_Enumeration<string>;
}

export interface java_awt_image_BufferedImage extends java_awt_Image, java_awt_image_WritableRenderedImage, java_awt_Transparency {
    raster: java_awt_image_WritableRaster;
    type: number;
    alphaRaster: java_awt_image_WritableRaster;
    alphaPremultiplied: boolean;
}

export interface java_io_IOException extends java_lang_Exception {
}

export interface java_io_Closeable extends java_lang_AutoCloseable {
}

export interface java_io_Flushable {
}

export interface java_nio_charset_Charset extends java_lang_Comparable<java_nio_charset_Charset> {
}

export interface java_nio_file_Path extends java_lang_Comparable<java_nio_file_Path>, java_lang_Iterable<java_nio_file_Path>, java_nio_file_Watchable {
}

export interface org_glyptodon_guacamole_io_GuacamoleWriter {
}

export interface org_glyptodon_guacamole_protocol_GuacamoleClientInformation {
    optimalScreenWidth: number;
    optimalScreenHeight: number;
    optimalResolution: number;
    audioMimetypes: string[];
    videoMimetypes: string[];
}

export interface org_glyptodon_guacamole_protocol_GuacamoleConfiguration extends java_io_Serializable {
    connectionID: string;
    protocol: string;
    parameters: { [index: string]: string };
    parameterNames: string[];
}

export interface org_glyptodon_guacamole_io_ReaderGuacamoleReader extends org_glyptodon_guacamole_io_GuacamoleReader {
}

export interface org_glyptodon_guacamole_servlet_GuacamoleSession {
}

export interface org_glyptodon_guacamole_io_GuacamoleReader {
}

export interface org_glyptodon_guacamole_net_GuacamoleSocket {
    open: boolean;
    writer: org_glyptodon_guacamole_io_GuacamoleWriter;
    reader: org_glyptodon_guacamole_io_GuacamoleReader;
}

export interface org_glyptodon_guacamole_net_GuacamoleTunnel {
    uuid: string;
    socket: org_glyptodon_guacamole_net_GuacamoleSocket;
    open: boolean;
}

export interface java_lang_Cloneable {
}

export interface java_lang_RuntimeException extends java_lang_Exception {
}

export interface java_util_Enumeration<E> {
}

export interface javax_servlet_ServletConfig {
    initParameterNames: java_util_Enumeration<string>;
    servletContext: javax_servlet_ServletContext;
    servletName: string;
}

export interface javax_servlet_ServletContext {
    classLoader: java_lang_ClassLoader;
    majorVersion: number;
    minorVersion: number;
    effectiveMajorVersion: number;
    effectiveMinorVersion: number;
    /**
     * @deprecated
     */
    servlets: java_util_Enumeration<javax_servlet_Servlet>;
    /**
     * @deprecated
     */
    servletNames: java_util_Enumeration<string>;
    serverInfo: string;
    servletContextName: string;
    servletRegistrations: { [index: string]: javax_servlet_ServletRegistration };
    filterRegistrations: { [index: string]: javax_servlet_FilterRegistration };
    sessionCookieConfig: javax_servlet_SessionCookieConfig;
    defaultSessionTrackingModes: javax_servlet_SessionTrackingMode[];
    effectiveSessionTrackingModes: javax_servlet_SessionTrackingMode[];
    jspConfigDescriptor: javax_servlet_descriptor_JspConfigDescriptor;
    virtualServerName: string;
    requestCharacterEncoding: string;
    responseCharacterEncoding: string;
    initParameterNames: java_util_Enumeration<string>;
    contextPath: string;
    sessionTimeout: number;
    attributeNames: java_util_Enumeration<string>;
}

export interface javax_servlet_http_HttpServlet extends javax_servlet_GenericServlet {
}

export interface javax_ws_rs_container_ContainerResponseFilter {
}

export interface javax_ws_rs_container_ContainerRequestFilter {
}

export interface com_auth0_jwt_interfaces_DecodedJWT extends com_auth0_jwt_interfaces_Payload, com_auth0_jwt_interfaces_Header {
    signature: string;
    header: string;
    payload: string;
    token: string;
}

export interface javax_servlet_Filter {
}

export interface java_util_concurrent_CompletableFuture<T> extends java_util_concurrent_Future<T>, java_util_concurrent_CompletionStage<T> {
    completedExceptionally: boolean;
    numberOfDependents: number;
}

export interface java_time_Duration extends java_time_temporal_TemporalAmount, java_lang_Comparable<java_time_Duration>, java_io_Serializable {
}

export interface java_nio_channels_ReadableByteChannel extends java_nio_channels_Channel {
}

export interface java_io_OutputStream extends java_io_Closeable, java_io_Flushable {
}

export interface java_io_Writer extends java_lang_Appendable, java_io_Closeable, java_io_Flushable {
}

export interface java_lang_Process {
    alive: boolean;
    inputStream: any;
    errorStream: any;
    outputStream: java_io_OutputStream;
}

export interface javax_websocket_Endpoint {
}

export interface java_util_EventListener {
}

export interface javax_net_ssl_HostnameVerifier {
}

export interface java_security_cert_X509Certificate extends java_security_cert_Certificate, java_security_cert_X509Extension {
    subjectX500Principal: javax_security_auth_x500_X500Principal;
    issuerX500Principal: javax_security_auth_x500_X500Principal;
    signature: any;
    basicConstraints: number;
    version: number;
    issuerDN: java_security_Principal;
    subjectDN: java_security_Principal;
    serialNumber: number;
    tbscertificate: any;
    extendedKeyUsage: string[];
    sigAlgName: string;
    notBefore: Date;
    notAfter: Date;
    sigAlgOID: string;
    sigAlgParams: any;
    issuerUniqueID: boolean[];
    subjectUniqueID: boolean[];
    keyUsage: boolean[];
    subjectAlternativeNames: any[][];
    issuerAlternativeNames: any[][];
}

export interface javax_net_ssl_X509TrustManager extends javax_net_ssl_TrustManager {
    acceptedIssuers: java_security_cert_X509Certificate[];
}

export interface org_apache_tamaya_spi_PropertySource {
    name: string;
    properties: { [index: string]: org_apache_tamaya_spi_PropertyValue };
    scannable: boolean;
    ordinal: number;
}

export interface org_apache_tamaya_spi_PropertySourceProvider {
    propertySources: org_apache_tamaya_spi_PropertySource[];
}

export interface org_apache_tamaya_yaml_YAMLFormat extends org_apache_tamaya_format_ConfigurationFormat {
}

export interface java_util_Comparator<T> {
}

export interface java_util_stream_Stream<T> extends java_util_stream_BaseStream<T, java_util_stream_Stream<T>> {
}

export interface com_openslx_eaas_imagearchive_ImageArchiveClient extends java_io_Closeable {
}

export interface com_openslx_eaas_migration_IMigratable {
}

export interface com_openslx_eaas_imagearchive_databind_ImageMetaData extends com_openslx_eaas_imagearchive_databind_AbstractMetaData {
    id: string;
    category: string;
    label: string;
    fstype: string;
}

export interface java_net_URI extends java_lang_Comparable<java_net_URI>, java_io_Serializable {
}

export interface com_fasterxml_jackson_databind_ObjectMapper extends com_fasterxml_jackson_core_ObjectCodec, com_fasterxml_jackson_core_Versioned, java_io_Serializable {
}

export interface java_lang_Class<T> extends java_io_Serializable, java_lang_reflect_GenericDeclaration, java_lang_reflect_Type, java_lang_reflect_AnnotatedElement {
}

export interface org_jboss_resteasy_plugins_providers_jackson_ResteasyJackson2Provider extends com_fasterxml_jackson_jaxrs_json_JacksonJaxbJsonProvider {
}

export interface org_jboss_resteasy_plugins_providers_multipart_MultipartFormDataInput extends org_jboss_resteasy_plugins_providers_multipart_MultipartInput {
    formDataMap: { [index: string]: org_jboss_resteasy_plugins_providers_multipart_InputPart[] };
    /**
     * @deprecated
     */
    formData: { [index: string]: org_jboss_resteasy_plugins_providers_multipart_InputPart };
}

export interface java_awt_datatransfer_DataFlavor extends java_io_Externalizable, java_lang_Cloneable {
    mimeType: string;
    humanPresentableName: string;
    representationClass: java_lang_Class<any>;
    primaryType: string;
    subType: string;
    mimeTypeSerializedObject: boolean;
    defaultRepresentationClass: java_lang_Class<any>;
    defaultRepresentationClassAsString: string;
    representationClassInputStream: boolean;
    representationClassReader: boolean;
    representationClassCharBuffer: boolean;
    representationClassByteBuffer: boolean;
    representationClassSerializable: boolean;
    representationClassRemote: boolean;
    flavorSerializedObjectType: boolean;
    flavorRemoteObjectType: boolean;
    flavorJavaFileListType: boolean;
    flavorTextType: boolean;
}

export interface javax_activation_CommandInfo {
    commandName: string;
    commandClass: string;
}

export interface javax_activation_CommandMap {
    mimeTypes: string[];
}

export interface java_awt_datatransfer_Transferable {
    transferDataFlavors: java_awt_datatransfer_DataFlavor[];
}

export interface java_util_concurrent_FutureTask<V> extends java_util_concurrent_RunnableFuture<V> {
}

export interface java_util_concurrent_ExecutorService extends java_util_concurrent_Executor {
    shutdown: boolean;
    terminated: boolean;
}

export interface java_lang_Comparable<T> {
}

export interface java_util_function_BiFunction<T, U, R> {
}

export interface org_bson_conversions_Bson {
}

export interface java_lang_Iterable<T> {
}

export interface javax_xml_bind_annotation_adapters_XmlAdapter<ValueType, BoundType> {
}

export interface java_util_Locale extends java_lang_Cloneable, java_io_Serializable {
}

export interface java_util_logging_Formatter {
}

export interface java_util_logging_ErrorManager {
}

export interface java_lang_ModuleLayer {
}

export interface java_lang_module_ModuleDescriptor extends java_lang_Comparable<java_lang_module_ModuleDescriptor> {
    open: boolean;
    automatic: boolean;
}

export interface java_lang_ClassLoader {
}

export interface java_lang_annotation_Annotation {
}

export interface java_lang_reflect_AnnotatedElement {
    annotations: java_lang_annotation_Annotation[];
    declaredAnnotations: java_lang_annotation_Annotation[];
}

export interface java_awt_image_ColorModel extends java_awt_Transparency {
    numComponents: number;
    numColorComponents: number;
    colorSpace: java_awt_color_ColorSpace;
    transferType: number;
    alphaPremultiplied: boolean;
    componentSize: number[];
    pixelSize: number;
}

export interface java_awt_image_WritableRaster extends java_awt_image_Raster {
    writableParent: java_awt_image_WritableRaster;
    rect: java_awt_image_Raster;
}

export interface java_awt_image_SampleModel {
    width: number;
    height: number;
    numBands: number;
    dataType: number;
    transferType: number;
    numDataElements: number;
    sampleSize: number[];
}

export interface java_awt_Graphics {
    clip: java_awt_Shape;
    color: java_awt_Color;
    font: java_awt_Font;
    fontMetrics: java_awt_FontMetrics;
    clipBounds: java_awt_Rectangle;
    /**
     * @deprecated
     */
    clipRect: java_awt_Rectangle;
}

export interface java_awt_Point extends java_awt_geom_Point2D, java_io_Serializable {
}

export interface java_awt_image_Raster {
    sampleModel: java_awt_image_SampleModel;
    dataBuffer: java_awt_image_DataBuffer;
    minX: number;
    minY: number;
    width: number;
    height: number;
    sampleModelTranslateX: number;
    sampleModelTranslateY: number;
    numBands: number;
    numDataElements: number;
    parent: java_awt_image_Raster;
    transferType: number;
    bounds: java_awt_Rectangle;
}

export interface java_awt_image_ImageProducer {
}

export interface java_awt_image_RenderedImage {
    sampleModel: java_awt_image_SampleModel;
    propertyNames: string[];
    colorModel: java_awt_image_ColorModel;
    minX: number;
    minY: number;
    numXTiles: number;
    numYTiles: number;
    minTileX: number;
    minTileY: number;
    tileWidth: number;
    tileHeight: number;
    tileGridXOffset: number;
    tileGridYOffset: number;
    width: number;
    data: java_awt_image_Raster;
    sources: java_awt_image_RenderedImage[];
    height: number;
}

export interface java_awt_Image {
    accelerationPriority: number;
    graphics: java_awt_Graphics;
    source: java_awt_image_ImageProducer;
}

export interface java_awt_image_WritableRenderedImage extends java_awt_image_RenderedImage {
    writableTileIndices: java_awt_Point[];
}

export interface java_awt_Transparency {
    transparency: number;
}

export interface java_nio_file_Watchable {
}

export interface javax_servlet_Servlet {
    servletConfig: javax_servlet_ServletConfig;
    servletInfo: string;
}

export interface javax_servlet_ServletRegistration extends javax_servlet_Registration {
    mappings: string[];
    runAsRole: string;
}

export interface javax_servlet_FilterRegistration extends javax_servlet_Registration {
    servletNameMappings: string[];
    urlPatternMappings: string[];
}

export interface javax_servlet_SessionCookieConfig {
    name: string;
    path: string;
    comment: string;
    secure: boolean;
    domain: string;
    httpOnly: boolean;
    maxAge: number;
}

export interface javax_servlet_descriptor_JspConfigDescriptor {
    taglibs: javax_servlet_descriptor_TaglibDescriptor[];
    jspPropertyGroups: javax_servlet_descriptor_JspPropertyGroupDescriptor[];
}

export interface javax_servlet_GenericServlet extends javax_servlet_Servlet, javax_servlet_ServletConfig, java_io_Serializable {
}

export interface com_auth0_jwt_interfaces_Claim {
    null: boolean;
}

export interface com_auth0_jwt_interfaces_Payload {
    id: string;
    issuedAt: Date;
    claims: { [index: string]: com_auth0_jwt_interfaces_Claim };
    expiresAt: Date;
    audience: string[];
    subject: string;
    notBefore: Date;
    issuer: string;
}

export interface com_auth0_jwt_interfaces_Header {
    type: string;
    keyId: string;
    contentType: string;
    algorithm: string;
}

export interface java_util_function_Supplier<T> {
}

export interface javax_xml_ws_handler_soap_SOAPHandler<T> extends javax_xml_ws_handler_Handler<T> {
    headers: javax_xml_namespace_QName[];
}

export interface java_time_temporal_TemporalAmount {
    units: java_time_temporal_TemporalUnit[];
}

export interface java_nio_channels_Channel extends java_io_Closeable {
    open: boolean;
}

export interface java_lang_Appendable {
}

export interface java_util_function_Consumer<T> {
}

export interface javax_security_auth_x500_X500Principal extends java_security_Principal, java_io_Serializable {
    encoded: any;
}

export interface java_security_Principal {
    name: string;
}

export interface java_security_PublicKey extends java_security_Key {
}

export interface java_security_cert_Certificate extends java_io_Serializable {
    type: string;
    encoded: any;
    publicKey: java_security_PublicKey;
}

export interface java_security_cert_X509Extension {
    criticalExtensionOIDs: string[];
    nonCriticalExtensionOIDs: string[];
}

export interface javax_net_ssl_TrustManager {
}

export interface org_apache_tamaya_spi_PropertyValue extends java_io_Serializable {
    key: string;
    value: string;
    source: string;
    metaEntries: { [index: string]: string };
}

export interface org_apache_tamaya_format_ConfigurationFormat {
    name: string;
}

export interface org_apache_tamaya_spi_PropertyConverter<T> {
}

export interface com_openslx_eaas_common_databind_Streamable<T> extends java_lang_AutoCloseable {
}

export interface com_openslx_eaas_imagearchive_databind_EmulatorMetaData extends com_openslx_eaas_imagearchive_databind_AbstractMetaData {
    id: string;
    name: string;
    version: string;
    digest: string;
    tags: string[];
    provenance: com_openslx_eaas_imagearchive_databind_EmulatorMetaData$Provenance;
    image: com_openslx_eaas_imagearchive_databind_ImageMetaData;
}

export interface com_openslx_eaas_imagearchive_databind_AbstractMetaData {
    kind: string;
}

export interface java_util_concurrent_CompletionStage<T> {
}

export interface javax_ws_rs_core_Response extends java_lang_AutoCloseable {
    length: number;
    location: java_net_URI;
    language: java_util_Locale;
    lastModified: Date;
    date: Date;
    statusInfo: javax_ws_rs_core_Response$StatusType;
    mediaType: javax_ws_rs_core_MediaType;
    allowedMethods: string[];
    entityTag: javax_ws_rs_core_EntityTag;
    stringHeaders: any;
    metadata: any;
    status: number;
    entity: any;
    cookies: { [index: string]: javax_ws_rs_core_NewCookie };
    links: javax_ws_rs_core_Link[];
    headers: any;
}

export interface com_fasterxml_jackson_core_ObjectCodec extends com_fasterxml_jackson_core_TreeCodec, com_fasterxml_jackson_core_Versioned {
    factory: com_fasterxml_jackson_core_JsonFactory;
    /**
     * @deprecated
     */
    jsonFactory: com_fasterxml_jackson_core_JsonFactory;
}

export interface com_fasterxml_jackson_core_Versioned {
}

export interface java_lang_reflect_GenericDeclaration extends java_lang_reflect_AnnotatedElement {
    typeParameters: java_lang_reflect_TypeVariable<any>[];
}

export interface java_lang_reflect_Type {
    typeName: string;
}

export interface com_fasterxml_jackson_jaxrs_json_JacksonJaxbJsonProvider extends com_fasterxml_jackson_jaxrs_json_JacksonJsonProvider {
}

export interface org_jboss_resteasy_plugins_providers_multipart_InputPart {
    mediaType: javax_ws_rs_core_MediaType;
    contentTypeFromMessage: boolean;
    bodyAsString: string;
    headers: any;
}

export interface org_jboss_resteasy_plugins_providers_multipart_MultipartInput {
    parts: org_jboss_resteasy_plugins_providers_multipart_InputPart[];
    preamble: string;
}

export interface java_io_Externalizable extends java_io_Serializable {
}

export interface java_awt_color_ColorSpace extends java_io_Serializable {
    type: number;
    numComponents: number;
    cs_sRGB: boolean;
}

export interface java_awt_image_DataBuffer {
    dataType: number;
    offset: number;
    size: number;
    offsets: number[];
    numBanks: number;
}

export interface java_awt_Rectangle extends java_awt_geom_Rectangle2D, java_awt_Shape, java_io_Serializable {
    location: java_awt_Point;
    size: java_awt_Dimension;
    rect: java_awt_geom_Rectangle2D;
}

export interface java_awt_Shape {
    bounds2D: java_awt_geom_Rectangle2D;
    bounds: java_awt_Rectangle;
}

export interface java_awt_Color extends java_awt_Paint, java_io_Serializable {
    red: number;
    green: number;
    blue: number;
    alpha: number;
    rgb: number;
    colorSpace: java_awt_color_ColorSpace;
}

export interface java_awt_Font extends java_io_Serializable {
    name: string;
    style: number;
    size: number;
    attributes: { [index: string]: any };
    family: string;
    transform: java_awt_geom_AffineTransform;
    psname: string;
    fontName: string;
    size2D: number;
    plain: boolean;
    bold: boolean;
    italic: boolean;
    transformed: boolean;
    numGlyphs: number;
    missingGlyphCode: number;
    availableAttributes: java_text_AttributedCharacterIterator$Attribute[];
    italicAngle: number;
}

export interface java_awt_FontMetrics extends java_io_Serializable {
    font: java_awt_Font;
    ascent: number;
    descent: number;
    leading: number;
    maxAscent: number;
    maxDescent: number;
    /**
     * @deprecated
     */
    maxDecent: number;
    maxAdvance: number;
    widths: number[];
    height: number;
    fontRenderContext: java_awt_font_FontRenderContext;
}

export interface java_awt_geom_Point2D extends java_lang_Cloneable {
    y: number;
    x: number;
}

export interface javax_servlet_Registration {
    name: string;
    className: string;
    initParameters: { [index: string]: string };
}

export interface javax_servlet_descriptor_TaglibDescriptor {
    taglibURI: string;
    taglibLocation: string;
}

export interface javax_servlet_descriptor_JspPropertyGroupDescriptor {
    buffer: string;
    urlPatterns: string[];
    elIgnored: string;
    pageEncoding: string;
    scriptingInvalid: string;
    isXml: string;
    includePreludes: string[];
    includeCodas: string[];
    deferredSyntaxAllowedAsLiteral: string;
    trimDirectiveWhitespaces: string;
    defaultContentType: string;
    errorOnUndeclaredNamespace: string;
}

export interface java_util_concurrent_Future<V> {
    done: boolean;
    cancelled: boolean;
}

export interface java_time_temporal_TemporalUnit {
    dateBased: boolean;
    timeBased: boolean;
    duration: java_time_Duration;
    durationEstimated: boolean;
}

export interface java_security_Key extends java_io_Serializable {
    encoded: any;
    format: string;
    algorithm: string;
}

export interface java_util_stream_BaseStream<T, S> extends java_lang_AutoCloseable {
    parallel: boolean;
}

export interface com_openslx_eaas_imagearchive_databind_EmulatorMetaData$Provenance {
    url: string;
    tag: string;
    layers: string[];
}

export interface javax_ws_rs_core_Response$StatusType {
    family: javax_ws_rs_core_Response$Status$Family;
    reasonPhrase: string;
    statusCode: number;
}

export interface javax_ws_rs_core_MediaType {
    type: string;
    subtype: string;
    parameters: { [index: string]: string };
    wildcardType: boolean;
    wildcardSubtype: boolean;
}

export interface javax_ws_rs_core_EntityTag {
    value: string;
    weak: boolean;
}

export interface javax_ws_rs_core_NewCookie extends javax_ws_rs_core_Cookie {
    comment: string;
    maxAge: number;
    expiry: Date;
    secure: boolean;
    httpOnly: boolean;
}

export interface javax_ws_rs_core_Link {
    type: string;
    rel: string;
    rels: string[];
    params: { [index: string]: string };
    title: string;
    uri: java_net_URI;
    uriBuilder: javax_ws_rs_core_UriBuilder;
}

export interface com_fasterxml_jackson_core_JsonFactory extends com_fasterxml_jackson_core_TokenStreamFactory, com_fasterxml_jackson_core_Versioned, java_io_Serializable {
    codec: com_fasterxml_jackson_core_ObjectCodec;
    /**
     * @deprecated
     */
    inputDecorator: com_fasterxml_jackson_core_io_InputDecorator;
    characterEscapes: com_fasterxml_jackson_core_io_CharacterEscapes;
    /**
     * @deprecated
     */
    outputDecorator: com_fasterxml_jackson_core_io_OutputDecorator;
    rootValueSeparator: string;
}

export interface com_fasterxml_jackson_core_TreeCodec {
}

export interface java_lang_reflect_TypeVariable<D> extends java_lang_reflect_Type, java_lang_reflect_AnnotatedElement {
    name: string;
    genericDeclaration: D;
    bounds: java_lang_reflect_Type[];
    annotatedBounds: java_lang_reflect_AnnotatedType[];
}

export interface com_fasterxml_jackson_jaxrs_json_JacksonJsonProvider extends com_fasterxml_jackson_jaxrs_base_ProviderBase<com_fasterxml_jackson_jaxrs_json_JacksonJsonProvider, com_fasterxml_jackson_databind_ObjectMapper, com_fasterxml_jackson_jaxrs_json_JsonEndpointConfig, com_fasterxml_jackson_jaxrs_json_JsonMapperConfigurator> {
    jsonpfunctionName: string;
    annotationsToUse: com_fasterxml_jackson_jaxrs_cfg_Annotations[];
    mapper: com_fasterxml_jackson_databind_ObjectMapper;
    defaultReadView: java_lang_Class<any>;
    defaultWriteView: java_lang_Class<any>;
    defaultView: java_lang_Class<any>;
}

export interface java_util_concurrent_RunnableFuture<V> extends java_lang_Runnable, java_util_concurrent_Future<V> {
}

export interface java_awt_Dimension extends java_awt_geom_Dimension2D, java_io_Serializable {
}

export interface java_awt_geom_Rectangle2D extends java_awt_geom_RectangularShape {
}

export interface java_awt_Paint extends java_awt_Transparency {
}

export interface java_awt_geom_AffineTransform extends java_lang_Cloneable, java_io_Serializable {
    scaleX: number;
    shearY: number;
    shearX: number;
    scaleY: number;
    translateX: number;
    translateY: number;
    type: number;
    identity: boolean;
    determinant: number;
    transform: java_awt_geom_AffineTransform;
    toRotation: number;
    toQuadrantRotation: number;
}

export interface java_text_AttributedCharacterIterator$Attribute extends java_io_Serializable {
}

export interface java_awt_font_FontRenderContext {
    transform: java_awt_geom_AffineTransform;
    transformed: boolean;
    antiAliasingHint: any;
    fractionalMetricsHint: any;
    antiAliased: boolean;
    transformType: number;
}

export interface javax_xml_ws_handler_Handler<C> {
}

export interface javax_ws_rs_core_Cookie {
    name: string;
    value: string;
    version: number;
    path: string;
    domain: string;
}

export interface javax_ws_rs_core_UriBuilder {
}

export interface com_fasterxml_jackson_core_io_InputDecorator extends java_io_Serializable {
}

export interface com_fasterxml_jackson_core_io_CharacterEscapes extends java_io_Serializable {
    escapeCodesForAscii: number[];
}

export interface com_fasterxml_jackson_core_io_OutputDecorator extends java_io_Serializable {
}

export interface com_fasterxml_jackson_core_TokenStreamFactory extends com_fasterxml_jackson_core_Versioned, java_io_Serializable {
}

export interface java_lang_reflect_AnnotatedType extends java_lang_reflect_AnnotatedElement {
    type: java_lang_reflect_Type;
    annotatedOwnerType: java_lang_reflect_AnnotatedType;
}

export interface java_awt_geom_Dimension2D extends java_lang_Cloneable {
    width: number;
    height: number;
}

export interface java_awt_geom_RectangularShape extends java_awt_Shape, java_lang_Cloneable {
    empty: boolean;
    minX: number;
    minY: number;
    maxX: number;
    maxY: number;
    centerX: number;
    centerY: number;
    width: number;
    y: number;
    x: number;
    height: number;
}

export interface com_fasterxml_jackson_jaxrs_base_ProviderBase<THIS, MAPPER, EP_CONFIG, MAPPER_CONFIG> extends javax_ws_rs_ext_MessageBodyReader<any>, javax_ws_rs_ext_MessageBodyWriter<any>, com_fasterxml_jackson_core_Versioned {
}

export interface com_fasterxml_jackson_jaxrs_json_JsonEndpointConfig extends com_fasterxml_jackson_jaxrs_cfg_EndpointConfigBase<com_fasterxml_jackson_jaxrs_json_JsonEndpointConfig> {
}

export interface com_fasterxml_jackson_jaxrs_json_JsonMapperConfigurator extends com_fasterxml_jackson_jaxrs_cfg_MapperConfiguratorBase<com_fasterxml_jackson_jaxrs_json_JsonMapperConfigurator, com_fasterxml_jackson_databind_ObjectMapper> {
    configuredMapper: com_fasterxml_jackson_databind_ObjectMapper;
    defaultMapper: com_fasterxml_jackson_databind_ObjectMapper;
    annotationsToUse: com_fasterxml_jackson_jaxrs_cfg_Annotations[];
    mapper: com_fasterxml_jackson_databind_ObjectMapper;
}

export interface com_fasterxml_jackson_databind_ObjectWriter extends com_fasterxml_jackson_core_Versioned, java_io_Serializable {
}

export interface com_fasterxml_jackson_databind_ObjectReader extends com_fasterxml_jackson_core_ObjectCodec, com_fasterxml_jackson_core_Versioned, java_io_Serializable {
}

export interface javax_ws_rs_ext_MessageBodyReader<T> {
}

export interface javax_ws_rs_ext_MessageBodyWriter<T> {
}

export interface com_fasterxml_jackson_jaxrs_cfg_EndpointConfigBase<THIS> {
    activeView: java_lang_Class<any>;
    rootName: string;
    writer: com_fasterxml_jackson_databind_ObjectWriter;
    reader: com_fasterxml_jackson_databind_ObjectReader;
}

export interface com_fasterxml_jackson_jaxrs_cfg_MapperConfiguratorBase<IMPL, MAPPER> {
    configuredMapper: MAPPER;
    defaultMapper: MAPPER;
}

export interface HttpClient<O> {

    request<R>(requestConfig: { method: string; url: string; queryParams?: any; data?: any; copyFn?: (data: R) => R; options?: O; }): RestResponse<R>;
}

export class RestApplicationClient<O> {

    constructor(protected httpClient: HttpClient<O>) {
    }

    /**
     * HTTP GET /Emil/buildInfo
     * Java method: de.bwl.bwfla.emil.Emil.buildInfo
     */
    buildInfo(options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`Emil/buildInfo`, options: options });
    }

    /**
     * HTTP GET /Emil/exportMetadata
     * Java method: de.bwl.bwfla.emil.Emil.exportMetadata
     */
    exportMetadata$GET$Emil_exportMetadata(options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`Emil/exportMetadata`, options: options });
    }

    /**
     * HTTP GET /Emil/resetUsageLog
     * Java method: de.bwl.bwfla.emil.Emil.resetUsageLog
     */
    resetUsageLog$GET$Emil_resetUsageLog(options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`Emil/resetUsageLog`, options: options });
    }

    /**
     * HTTP GET /Emil/usageLog
     * Java method: de.bwl.bwfla.emil.Emil.usageLog
     */
    usageLog(options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`Emil/usageLog`, options: options });
    }

    /**
     * HTTP GET /Emil/userInfo
     * Java method: de.bwl.bwfla.emil.Emil.userInfo
     */
    userInfo(options?: O): RestResponse<de_bwl_bwfla_emil_datatypes_rest_UserInfoResponse> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`Emil/userInfo`, options: options });
    }

    /**
     * HTTP POST /EmilContainerData/buildContainerImage
     * Java method: de.bwl.bwfla.emil.EmilContainerData.saveContainerImage
     */
    saveContainerImage(arg0: de_bwl_bwfla_emil_datatypes_rest_CreateContainerImageRequest, options?: O): RestResponse<de_bwl_bwfla_emil_datatypes_rest_TaskStateResponse> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`EmilContainerData/buildContainerImage`, data: arg0, options: options });
    }

    /**
     * HTTP POST /EmilContainerData/delete
     * Java method: de.bwl.bwfla.emil.EmilContainerData.delete
     */
    delete$POST$EmilContainerData_delete(arg0: de_bwl_bwfla_emil_datatypes_EnvironmentDeleteRequest, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`EmilContainerData/delete`, data: arg0, options: options });
    }

    /**
     * HTTP GET /EmilContainerData/getOriginRuntimeList
     * Java method: de.bwl.bwfla.emil.EmilContainerData.getOriginRuntimeList
     */
    getOriginRuntimeList(options?: O): RestResponse<de_bwl_bwfla_emil_datatypes_RuntimeListResponse> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`EmilContainerData/getOriginRuntimeList`, options: options });
    }

    /**
     * HTTP POST /EmilContainerData/importContainer
     * Java method: de.bwl.bwfla.emil.EmilContainerData.importContainer
     */
    importContainer(arg0: de_bwl_bwfla_emil_datatypes_rest_ImportContainerRequest, options?: O): RestResponse<de_bwl_bwfla_emil_datatypes_rest_TaskStateResponse> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`EmilContainerData/importContainer`, data: arg0, options: options });
    }

    /**
     * HTTP POST /EmilContainerData/importEmulator
     * Java method: de.bwl.bwfla.emil.EmilContainerData.importEmulator
     */
    importEmulator(arg0: de_bwl_bwfla_emil_datatypes_rest_ImportEmulatorRequest, options?: O): RestResponse<de_bwl_bwfla_emil_datatypes_rest_TaskStateResponse> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`EmilContainerData/importEmulator`, data: arg0, options: options });
    }

    /**
     * HTTP POST /EmilContainerData/updateContainer
     * Java method: de.bwl.bwfla.emil.EmilContainerData.updateContainer
     */
    updateContainer(arg0: de_bwl_bwfla_emil_datatypes_rest_UpdateContainerRequest, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`EmilContainerData/updateContainer`, data: arg0, options: options });
    }

    /**
     * @deprecated
     * HTTP POST /EmilContainerData/updateLatestEmulator
     * Java method: de.bwl.bwfla.emil.EmilContainerData.updateLatestEmulator
     */
    updateLatestEmulator(arg0: de_bwl_bwfla_emil_datatypes_rest_UpdateLatestEmulatorRequest, options?: O): RestResponse<void> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`EmilContainerData/updateLatestEmulator`, data: arg0, options: options });
    }

    /**
     * HTTP GET /EmilEnvironmentData/
     * Java method: de.bwl.bwfla.emil.EmilEnvironmentData.getEnvironments
     */
    getEnvironments(options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`EmilEnvironmentData/`, options: options });
    }

    /**
     * HTTP POST /EmilEnvironmentData/createEnvironment
     * Java method: de.bwl.bwfla.emil.EmilEnvironmentData.createEnvironment
     */
    createEnvironment(arg0: de_bwl_bwfla_emil_datatypes_EnvironmentCreateRequest, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`EmilEnvironmentData/createEnvironment`, data: arg0, options: options });
    }

    /**
     * HTTP GET /EmilEnvironmentData/defaultEnvironment
     * Java method: de.bwl.bwfla.emil.EmilEnvironmentData.defaultEnvironment
     */
    defaultEnvironment(queryParams?: { osId?: string; }, options?: O): RestResponse<de_bwl_bwfla_emil_datatypes_DefaultEnvironmentResponse> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`EmilEnvironmentData/defaultEnvironment`, queryParams: queryParams, options: options });
    }

    /**
     * HTTP GET /EmilEnvironmentData/defaultEnvironments
     * Java method: de.bwl.bwfla.emil.EmilEnvironmentData.defaultEnvironments
     */
    defaultEnvironments(options?: O): RestResponse<{ [index: string]: string }> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`EmilEnvironmentData/defaultEnvironments`, options: options });
    }

    /**
     * HTTP POST /EmilEnvironmentData/delete
     * Java method: de.bwl.bwfla.emil.EmilEnvironmentData.delete
     */
    delete$POST$EmilEnvironmentData_delete(arg0: de_bwl_bwfla_emil_datatypes_EnvironmentDeleteRequest, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`EmilEnvironmentData/delete`, data: arg0, options: options });
    }

    /**
     * HTTP POST /EmilEnvironmentData/export
     * Java method: de.bwl.bwfla.emil.EmilEnvironmentData.export
     */
    export$POST$EmilEnvironmentData_export(arg0: de_bwl_bwfla_emil_datatypes_rest_ExportRequest, options?: O): RestResponse<de_bwl_bwfla_emil_datatypes_rest_TaskStateResponse> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`EmilEnvironmentData/export`, data: arg0, options: options });
    }

    /**
     * HTTP POST /EmilEnvironmentData/forkRevision
     * Java method: de.bwl.bwfla.emil.EmilEnvironmentData.forkRevision
     */
    forkRevision(arg0: de_bwl_bwfla_emil_datatypes_ForkRevisionRequest, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`EmilEnvironmentData/forkRevision`, data: arg0, options: options });
    }

    /**
     * HTTP GET /EmilEnvironmentData/getEnvironmentTemplates
     * Java method: de.bwl.bwfla.emil.EmilEnvironmentData.getEnvironmentTemplates
     */
    getEnvironmentTemplates(options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`EmilEnvironmentData/getEnvironmentTemplates`, options: options });
    }

    /**
     * HTTP GET /EmilEnvironmentData/getNameIndexes
     * Java method: de.bwl.bwfla.emil.EmilEnvironmentData.getNameIndexes
     */
    getNameIndexes$GET$EmilEnvironmentData_getNameIndexes(options?: O): RestResponse<de_bwl_bwfla_api_imagearchive_ImageNameIndex> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`EmilEnvironmentData/getNameIndexes`, options: options });
    }

    /**
     * HTTP GET /EmilEnvironmentData/getPatches
     * Java method: de.bwl.bwfla.emil.EmilEnvironmentData.getPatches
     */
    getPatches(options?: O): RestResponse<de_bwl_bwfla_api_imagearchive_ImageGeneralizationPatchDescription[]> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`EmilEnvironmentData/getPatches`, options: options });
    }

    /**
     * HTTP POST /EmilEnvironmentData/importImage
     * Java method: de.bwl.bwfla.emil.EmilEnvironmentData.importImage
     */
    importImage$POST$EmilEnvironmentData_importImage(arg0: de_bwl_bwfla_emil_datatypes_ImportImageRequest, options?: O): RestResponse<de_bwl_bwfla_emil_datatypes_rest_TaskStateResponse> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`EmilEnvironmentData/importImage`, data: arg0, options: options });
    }

    /**
     * HTTP GET /EmilEnvironmentData/init
     * Java method: de.bwl.bwfla.emil.EmilEnvironmentData.init
     */
    init$GET$EmilEnvironmentData_init(options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`EmilEnvironmentData/init`, options: options });
    }

    /**
     * HTTP GET /EmilEnvironmentData/objectDependencies
     * Java method: de.bwl.bwfla.emil.EmilEnvironmentData.getObjectDependencies
     */
    getObjectDependencies$GET$EmilEnvironmentData_objectDependencies(queryParams?: { envId?: string; }, options?: O): RestResponse<string[]> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`EmilEnvironmentData/objectDependencies`, queryParams: queryParams, options: options });
    }

    /**
     * HTTP GET /EmilEnvironmentData/operatingSystemMetadata
     * Java method: de.bwl.bwfla.emil.EmilEnvironmentData.getOperatingSystemMetadata
     */
    getOperatingSystemMetadata$GET$EmilEnvironmentData_operatingSystemMetadata(options?: O): RestResponse<de_bwl_bwfla_common_datatypes_identification_OperatingSystems> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`EmilEnvironmentData/operatingSystemMetadata`, options: options });
    }

    /**
     * HTTP POST /EmilEnvironmentData/replicateImage
     * Java method: de.bwl.bwfla.emil.EmilEnvironmentData.replicateImage
     */
    replicateImage$POST$EmilEnvironmentData_replicateImage(arg0: de_bwl_bwfla_emil_datatypes_rest_ReplicateImagesRequest, options?: O): RestResponse<de_bwl_bwfla_emil_datatypes_rest_ReplicateImagesResponse> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`EmilEnvironmentData/replicateImage`, data: arg0, options: options });
    }

    /**
     * HTTP POST /EmilEnvironmentData/revertRevision
     * Java method: de.bwl.bwfla.emil.EmilEnvironmentData.revertRevision
     */
    revertRevision(arg0: de_bwl_bwfla_emil_datatypes_rest_RevertRevisionRequest, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`EmilEnvironmentData/revertRevision`, data: arg0, options: options });
    }

    /**
     * HTTP GET /EmilEnvironmentData/setDefaultEnvironment
     * Java method: de.bwl.bwfla.emil.EmilEnvironmentData.setDefaultEnvironment
     */
    setDefaultEnvironment(queryParams?: { osId?: string; envId?: string; }, options?: O): RestResponse<de_bwl_bwfla_emil_datatypes_rest_EmilResponseType> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`EmilEnvironmentData/setDefaultEnvironment`, queryParams: queryParams, options: options });
    }

    /**
     * HTTP GET /EmilEnvironmentData/sync
     * Java method: de.bwl.bwfla.emil.EmilEnvironmentData.sync
     */
    sync$GET$EmilEnvironmentData_sync(options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`EmilEnvironmentData/sync`, options: options });
    }

    /**
     * HTTP POST /EmilEnvironmentData/updateDescription
     * Java method: de.bwl.bwfla.emil.EmilEnvironmentData.updateDescription
     */
    updateDescription(arg0: de_bwl_bwfla_emil_datatypes_rest_UpdateEnvironmentDescriptionRequest, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`EmilEnvironmentData/updateDescription`, data: arg0, options: options });
    }

    /**
     * HTTP GET /EmilEnvironmentData/{envId}
     * Java method: de.bwl.bwfla.emil.EmilEnvironmentData.getEnvironment
     */
    getEnvironment(envId: string, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`EmilEnvironmentData/${envId}`, options: options });
    }

    /**
     * HTTP GET /EmilSoftwareData/getSoftwareObject
     * Java method: de.bwl.bwfla.emil.EmilSoftwareData.getSoftwareObject
     */
    getSoftwareObject(queryParams?: { softwareId?: string; }, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`EmilSoftwareData/getSoftwareObject`, queryParams: queryParams, options: options });
    }

    /**
     * HTTP GET /EmilSoftwareData/getSoftwarePackageDescription
     * Java method: de.bwl.bwfla.emil.EmilSoftwareData.getSoftwarePackageDescription
     */
    getSoftwarePackageDescription(queryParams?: { softwareId?: string; }, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`EmilSoftwareData/getSoftwarePackageDescription`, queryParams: queryParams, options: options });
    }

    /**
     * HTTP GET /EmilSoftwareData/getSoftwarePackageDescriptions
     * Java method: de.bwl.bwfla.emil.EmilSoftwareData.getSoftwarePackageDescriptions
     */
    getSoftwarePackageDescriptions(options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`EmilSoftwareData/getSoftwarePackageDescriptions`, options: options });
    }

    /**
     * HTTP POST /EmilSoftwareData/saveSoftwareObject
     * Java method: de.bwl.bwfla.emil.EmilSoftwareData.saveSoftwareObject
     */
    saveSoftwareObject(arg0: de_bwl_bwfla_emil_datatypes_EmilSoftwareObject, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`EmilSoftwareData/saveSoftwareObject`, data: arg0, options: options });
    }

    /**
     * HTTP GET /EmilUserSession/delete
     * Java method: de.bwl.bwfla.emil.EmilUserSession.delete
     */
    delete$GET$EmilUserSession_delete(queryParams?: { sessionId?: string; }, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`EmilUserSession/delete`, queryParams: queryParams, options: options });
    }

    /**
     * HTTP GET /EmilUserSession/list
     * Java method: de.bwl.bwfla.emil.EmilUserSession.userSessionList
     */
    userSessionList(options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`EmilUserSession/list`, options: options });
    }

    /**
     * HTTP GET /EmilUserSession/session
     * Java method: de.bwl.bwfla.emil.EmilUserSession.getUserSession
     */
    getUserSession(queryParams?: { userId?: string; objectId?: string; }, options?: O): RestResponse<de_bwl_bwfla_emil_datatypes_UserSessionResponse> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`EmilUserSession/session`, queryParams: queryParams, options: options });
    }

    /**
     * HTTP GET /admin/apikey
     * Java method: de.bwl.bwfla.emil.Admin.getApiKey
     */
    getApiKey(options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`admin/apikey`, options: options });
    }

    /**
     * HTTP GET /admin/build-info
     * Java method: de.bwl.bwfla.emil.Admin.getBuildInfo
     */
    getBuildInfo(options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`admin/build-info`, options: options });
    }

    /**
     * HTTP GET /admin/init
     * Java method: de.bwl.bwfla.emil.Admin.init
     */
    init$GET$admin_init(options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`admin/init`, options: options });
    }

    /**
     * HTTP POST /admin/metadata-export
     * Java method: de.bwl.bwfla.emil.Admin.exportMetadata
     */
    exportMetadata$POST$admin_metadataexport(options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`admin/metadata-export`, options: options });
    }

    /**
     * HTTP GET /admin/usage-log
     * Java method: de.bwl.bwfla.emil.Admin.getUsageLog
     */
    getUsageLog(options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`admin/usage-log`, options: options });
    }

    /**
     * HTTP DELETE /admin/usage-log
     * Java method: de.bwl.bwfla.emil.Admin.resetUsageLog
     */
    resetUsageLog$DELETE$admin_usagelog(options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "DELETE", url: uriEncoding`admin/usage-log`, options: options });
    }

    /**
     * HTTP GET /admin/user-info
     * Java method: de.bwl.bwfla.emil.Admin.getUserInfo
     */
    getUserInfo(options?: O): RestResponse<de_bwl_bwfla_emil_datatypes_rest_UserInfoResponse> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`admin/user-info`, options: options });
    }

    /**
     * HTTP POST /classification/
     * Java method: de.bwl.bwfla.emil.ObjectClassification.classify
     */
    classify(arg0: de_bwl_bwfla_emil_datatypes_rest_ClientClassificationRequest, options?: O): RestResponse<de_bwl_bwfla_emil_datatypes_rest_TaskStateResponse> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`classification/`, data: arg0, options: options });
    }

    /**
     * HTTP POST /classification/overrideObjectCharacterization
     * Java method: de.bwl.bwfla.emil.ObjectClassification.overrideObjectCharacterization
     */
    overrideObjectCharacterization(arg0: de_bwl_bwfla_emil_datatypes_OverrideCharacterizationRequest, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`classification/overrideObjectCharacterization`, data: arg0, options: options });
    }

    /**
     * HTTP POST /components
     * Java method: de.bwl.bwfla.emil.Components.createComponent
     */
    createComponent(arg0: de_bwl_bwfla_emil_datatypes_rest_ComponentRequest, options?: O): RestResponse<de_bwl_bwfla_emil_datatypes_rest_ComponentResponse> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`components`, data: arg0, options: options });
    }

    /**
     * HTTP GET /components/{compid}/{kind}/{resource: .+}/url
     * Java method: de.bwl.bwfla.emil.Components.resolveResourceGET
     */
    resolveResourceGET(compid: string, kind: string, resource: string, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`components/${compid}/${kind}/${resource}/url`, options: options });
    }

    /**
     * HTTP HEAD /components/{compid}/{kind}/{resource: .+}/url
     * Java method: de.bwl.bwfla.emil.Components.resolveResourceHEAD
     */
    resolveResourceHEAD(compid: string, kind: string, resource: string, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "HEAD", url: uriEncoding`components/${compid}/${kind}/${resource}/url`, options: options });
    }

    /**
     * HTTP GET /components/{componentId}
     * Java method: de.bwl.bwfla.emil.Components.getComponent
     */
    getComponent(componentId: string, options?: O): RestResponse<de_bwl_bwfla_emil_datatypes_rest_ComponentResponse> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`components/${componentId}`, options: options });
    }

    /**
     * HTTP DELETE /components/{componentId}
     * Java method: de.bwl.bwfla.emil.Components.releaseComponent
     */
    releaseComponent(componentId: string, options?: O): RestResponse<void> {
        return this.httpClient.request({ method: "DELETE", url: uriEncoding`components/${componentId}`, options: options });
    }

    /**
     * HTTP POST /components/{componentId}/async/checkpoint
     * Java method: de.bwl.bwfla.emil.Components.checkpointAsync
     */
    checkpointAsync(componentId: string, arg1: de_bwl_bwfla_emil_datatypes_snapshot_SnapshotRequest, options?: O): RestResponse<de_bwl_bwfla_emil_datatypes_rest_TaskStateResponse> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`components/${componentId}/async/checkpoint`, data: arg1, options: options });
    }

    /**
     * HTTP POST /components/{componentId}/async/snapshot
     * Java method: de.bwl.bwfla.emil.Components.snapshotAsync
     */
    snapshotAsync(componentId: string, arg1: de_bwl_bwfla_emil_datatypes_snapshot_SnapshotRequest, options?: O): RestResponse<de_bwl_bwfla_emil_datatypes_rest_TaskStateResponse> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`components/${componentId}/async/snapshot`, data: arg1, options: options });
    }

    /**
     * HTTP POST /components/{componentId}/changeMedia
     * Java method: de.bwl.bwfla.emil.Components.changeMedia
     */
    changeMedia(componentId: string, arg1: de_bwl_bwfla_emil_datatypes_rest_MediaChangeRequest, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`components/${componentId}/changeMedia`, data: arg1, options: options });
    }

    /**
     * HTTP POST /components/{componentId}/checkpoint
     * Java method: de.bwl.bwfla.emil.Components.checkpoint
     */
    checkpoint(componentId: string, arg1: de_bwl_bwfla_emil_datatypes_snapshot_SnapshotRequest, options?: O): RestResponse<de_bwl_bwfla_emil_datatypes_rest_SnapshotResponse> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`components/${componentId}/checkpoint`, data: arg1, options: options });
    }

    /**
     * HTTP GET /components/{componentId}/controlurls
     * Java method: de.bwl.bwfla.emil.Components.getControlUrls
     */
    getControlUrls(componentId: string, options?: O): RestResponse<{ [index: string]: java_net_URI }> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`components/${componentId}/controlurls`, options: options });
    }

    /**
     * HTTP GET /components/{componentId}/downloadPrintJob
     * Java method: de.bwl.bwfla.emil.Components.downloadPrintJob
     */
    downloadPrintJob(componentId: string, queryParams?: { label?: string; }, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`components/${componentId}/downloadPrintJob`, queryParams: queryParams, options: options });
    }

    /**
     * HTTP GET /components/{componentId}/events
     * Java method: de.bwl.bwfla.emil.Components.events
     */
    events(componentId: string, options?: O): RestResponse<void> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`components/${componentId}/events`, options: options });
    }

    /**
     * HTTP POST /components/{componentId}/keepalive
     * Java method: de.bwl.bwfla.emil.Components.keepalive
     */
    keepalive$POST$components_componentId_keepalive(componentId: string, options?: O): RestResponse<void> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`components/${componentId}/keepalive`, options: options });
    }

    /**
     * HTTP GET /components/{componentId}/printJobs
     * Java method: de.bwl.bwfla.emil.Components.printJobs
     */
    printJobs(componentId: string, options?: O): RestResponse<string[]> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`components/${componentId}/printJobs`, options: options });
    }

    /**
     * HTTP GET /components/{componentId}/result
     * Java method: de.bwl.bwfla.emil.Components.getResult
     */
    getResult(componentId: string, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`components/${componentId}/result`, options: options });
    }

    /**
     * HTTP GET /components/{componentId}/screenshot
     * Java method: de.bwl.bwfla.emil.Components.screenshot
     */
    screenshot(componentId: string, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`components/${componentId}/screenshot`, options: options });
    }

    /**
     * HTTP POST /components/{componentId}/snapshot
     * Java method: de.bwl.bwfla.emil.Components.snapshot
     */
    snapshot(componentId: string, arg1: de_bwl_bwfla_emil_datatypes_snapshot_SnapshotRequest, options?: O): RestResponse<de_bwl_bwfla_emil_datatypes_rest_SnapshotResponse> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`components/${componentId}/snapshot`, data: arg1, options: options });
    }

    /**
     * HTTP GET /components/{componentId}/state
     * Java method: de.bwl.bwfla.emil.Components.getState
     */
    getState(componentId: string, options?: O): RestResponse<de_bwl_bwfla_emil_datatypes_rest_ComponentResponse> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`components/${componentId}/state`, options: options });
    }

    /**
     * HTTP GET /components/{componentId}/stop
     * Java method: de.bwl.bwfla.emil.Components.stop
     */
    stop(componentId: string, options?: O): RestResponse<de_bwl_bwfla_emil_datatypes_rest_ProcessResultUrl> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`components/${componentId}/stop`, options: options });
    }

    /**
     * HTTP POST /compute
     * Java method: de.bwl.bwfla.emil.utils.Compute.create
     */
    create$POST$compute(arg0: de_bwl_bwfla_emil_datatypes_ComputeRequest, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`compute`, data: arg0, options: options });
    }

    /**
     * HTTP GET /compute/{sessionId}
     * Java method: de.bwl.bwfla.emil.utils.Compute.state
     */
    state(sessionId: string, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`compute/${sessionId}`, options: options });
    }

    /**
     * HTTP GET /emulator-repository/emulators
     * Java method: de.bwl.bwfla.emil.EmulatorRepository$Emulators.list
     */
    list$GET$emulatorrepository_emulators(options?: O): RestResponse<com_openslx_eaas_common_databind_Streamable<com_openslx_eaas_imagearchive_databind_EmulatorMetaData>> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`emulator-repository/emulators`, options: options });
    }

    /**
     * HTTP POST /emulator-repository/emulators/{emuid}/default
     * Java method: de.bwl.bwfla.emil.EmulatorRepository$Emulators.makrAsDefault
     */
    makrAsDefault(emuid: string, options?: O): RestResponse<void> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`emulator-repository/emulators/${emuid}/default`, options: options });
    }

    /**
     * HTTP GET /emulator-repository/images/{imgid}/url
     * Java method: de.bwl.bwfla.emil.EmulatorRepository$Images.resolveGET
     */
    resolveGET$GET$emulatorrepository_images_imgid_url(imgid: string, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`emulator-repository/images/${imgid}/url`, options: options });
    }

    /**
     * HTTP HEAD /emulator-repository/images/{imgid}/url
     * Java method: de.bwl.bwfla.emil.EmulatorRepository$Images.resolveHEAD
     */
    resolveHEAD$HEAD$emulatorrepository_images_imgid_url(imgid: string, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "HEAD", url: uriEncoding`emulator-repository/images/${imgid}/url`, options: options });
    }

    /**
     * HTTP POST /environment-proposer/api/v2/proposals
     * Java method: de.bwl.bwfla.envproposer.EnvironmentProposer.postProposal
     */
    postProposal(arg0: de_bwl_bwfla_envproposer_api_ProposalRequest, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`environment-proposer/api/v2/proposals`, data: arg0, options: options });
    }

    /**
     * HTTP GET /environment-proposer/api/v2/proposals/{id}
     * Java method: de.bwl.bwfla.envproposer.EnvironmentProposer.getProposal
     */
    getProposal(id: string, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`environment-proposer/api/v2/proposals/${id}`, options: options });
    }

    /**
     * HTTP GET /environment-proposer/api/v2/waitqueue/{id}
     * Java method: de.bwl.bwfla.envproposer.EnvironmentProposer.poll
     */
    poll(id: string, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`environment-proposer/api/v2/waitqueue/${id}`, options: options });
    }

    /**
     * HTTP POST /environment-repository/actions/create-image
     * Java method: de.bwl.bwfla.emil.EnvironmentRepository$Actions.createImage
     */
    createImage(arg0: de_bwl_bwfla_emil_datatypes_rest_ImageCreateRequest, options?: O): RestResponse<de_bwl_bwfla_emil_datatypes_rest_TaskStateResponse> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`environment-repository/actions/create-image`, data: arg0, options: options });
    }

    /**
     * HTTP POST /environment-repository/actions/delete-image
     * Java method: de.bwl.bwfla.emil.EnvironmentRepository$Actions.deleteImage
     */
    deleteImage(arg0: de_bwl_bwfla_emil_datatypes_rest_DeleteImageRequest, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`environment-repository/actions/delete-image`, data: arg0, options: options });
    }

    /**
     * HTTP POST /environment-repository/actions/import-image
     * Java method: de.bwl.bwfla.emil.EnvironmentRepository$Actions.importImage
     */
    importImage$POST$environmentrepository_actions_importimage(arg0: de_bwl_bwfla_emil_datatypes_ImportImageRequest, options?: O): RestResponse<de_bwl_bwfla_emil_datatypes_rest_TaskStateResponse> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`environment-repository/actions/import-image`, data: arg0, options: options });
    }

    /**
     * HTTP POST /environment-repository/actions/prepare
     * Java method: de.bwl.bwfla.emil.EnvironmentRepository$Actions.prepare
     */
    prepare(options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`environment-repository/actions/prepare`, options: options });
    }

    /**
     * HTTP POST /environment-repository/actions/replicate-image
     * Java method: de.bwl.bwfla.emil.EnvironmentRepository$Actions.replicateImage
     */
    replicateImage$POST$environmentrepository_actions_replicateimage(arg0: de_bwl_bwfla_emil_datatypes_rest_ReplicateImagesRequest, options?: O): RestResponse<de_bwl_bwfla_emil_datatypes_rest_ReplicateImagesResponse> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`environment-repository/actions/replicate-image`, data: arg0, options: options });
    }

    /**
     * HTTP POST /environment-repository/actions/sync
     * Java method: de.bwl.bwfla.emil.EnvironmentRepository$Actions.sync
     */
    sync$POST$environmentrepository_actions_sync(options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`environment-repository/actions/sync`, options: options });
    }

    /**
     * HTTP GET /environment-repository/db-migration
     * Java method: de.bwl.bwfla.emil.EnvironmentRepository.migrateDb
     */
    migrateDb(options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`environment-repository/db-migration`, options: options });
    }

    /**
     * HTTP GET /environment-repository/default-environments
     * Java method: de.bwl.bwfla.emil.EnvironmentRepository$DefaultEnvironments.list
     */
    list$GET$environmentrepository_defaultenvironments(options?: O): RestResponse<{ [index: string]: string }> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`environment-repository/default-environments`, options: options });
    }

    /**
     * HTTP GET /environment-repository/default-environments/{osId}
     * Java method: de.bwl.bwfla.emil.EnvironmentRepository$DefaultEnvironments.get
     */
    get$GET$environmentrepository_defaultenvironments_osId(osId: string, options?: O): RestResponse<de_bwl_bwfla_emil_datatypes_DefaultEnvironmentResponse> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`environment-repository/default-environments/${osId}`, options: options });
    }

    /**
     * HTTP PATCH /environment-repository/default-environments/{osId}
     * Java method: de.bwl.bwfla.emil.EnvironmentRepository$DefaultEnvironments.set
     */
    set(osId: string, queryParams?: { envId?: string; }, options?: O): RestResponse<de_bwl_bwfla_emil_datatypes_rest_EmilResponseType> {
        return this.httpClient.request({ method: "PATCH", url: uriEncoding`environment-repository/default-environments/${osId}`, queryParams: queryParams, options: options });
    }

    /**
     * HTTP POST /environment-repository/environments
     * Java method: de.bwl.bwfla.emil.EnvironmentRepository$Environments.create
     */
    create$POST$environmentrepository_environments(arg0: de_bwl_bwfla_emil_datatypes_EnvironmentCreateRequest, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`environment-repository/environments`, data: arg0, options: options });
    }

    /**
     * HTTP GET /environment-repository/environments
     * Java method: de.bwl.bwfla.emil.EnvironmentRepository$Environments.list
     */
    list$GET$environmentrepository_environments(queryParams?: { detailed?: boolean; localOnly?: boolean; }, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`environment-repository/environments`, queryParams: queryParams, options: options });
    }

    /**
     * HTTP DELETE /environment-repository/environments/{envId}
     * Java method: de.bwl.bwfla.emil.EnvironmentRepository$Environments.delete
     */
    delete$DELETE$environmentrepository_environments_envId(envId: string, arg1: de_bwl_bwfla_emil_datatypes_EnvironmentDeleteRequest, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "DELETE", url: uriEncoding`environment-repository/environments/${envId}`, data: arg1, options: options });
    }

    /**
     * HTTP GET /environment-repository/environments/{envId}
     * Java method: de.bwl.bwfla.emil.EnvironmentRepository$Environments.get
     */
    get$GET$environmentrepository_environments_envId(envId: string, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`environment-repository/environments/${envId}`, options: options });
    }

    /**
     * HTTP PATCH /environment-repository/environments/{envId}
     * Java method: de.bwl.bwfla.emil.EnvironmentRepository$Environments.update
     */
    update$PATCH$environmentrepository_environments_envId(envId: string, arg1: de_bwl_bwfla_emil_datatypes_rest_UpdateEnvironmentDescriptionRequest, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "PATCH", url: uriEncoding`environment-repository/environments/${envId}`, data: arg1, options: options });
    }

    /**
     * HTTP POST /environment-repository/environments/{envId}/export
     * Java method: de.bwl.bwfla.emil.EnvironmentRepository$Environments.export
     */
    export$POST$environmentrepository_environments_envId_export(envId: string, arg1: de_bwl_bwfla_emil_datatypes_rest_ExportRequest, options?: O): RestResponse<de_bwl_bwfla_emil_datatypes_rest_TaskStateResponse> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`environment-repository/environments/${envId}/export`, data: arg1, options: options });
    }

    /**
     * HTTP GET /environment-repository/environments/{envId}/object-deps
     * Java method: de.bwl.bwfla.emil.EnvironmentRepository$Environments.getObjectDependencies
     */
    getObjectDependencies$GET$environmentrepository_environments_envId_objectdeps(envId: string, options?: O): RestResponse<string[]> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`environment-repository/environments/${envId}/object-deps`, options: options });
    }

    /**
     * HTTP POST /environment-repository/environments/{envId}/revisions
     * Java method: de.bwl.bwfla.emil.EnvironmentRepository$Revisions.create
     */
    create$POST$environmentrepository_environments_envId_revisions(envId: string, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`environment-repository/environments/${envId}/revisions`, options: options });
    }

    /**
     * HTTP POST /environment-repository/environments/{envId}/revisions/{revId}
     * Java method: de.bwl.bwfla.emil.EnvironmentRepository$Revisions.revert
     */
    revert(envId: string, revId: string, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`environment-repository/environments/${envId}/revisions/${revId}`, options: options });
    }

    /**
     * @deprecated
     * HTTP GET /environment-repository/image-name-index
     * Java method: de.bwl.bwfla.emil.EnvironmentRepository.getNameIndexes
     */
    getNameIndexes$GET$environmentrepository_imagenameindex(options?: O): RestResponse<de_bwl_bwfla_api_imagearchive_ImageNameIndex> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`environment-repository/image-name-index`, options: options });
    }

    /**
     * HTTP GET /environment-repository/images
     * Java method: de.bwl.bwfla.emil.EnvironmentRepository$Images.list
     */
    list$GET$environmentrepository_images(options?: O): RestResponse<com_openslx_eaas_common_databind_Streamable<com_openslx_eaas_imagearchive_databind_ImageMetaData>> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`environment-repository/images`, options: options });
    }

    /**
     * HTTP POST /environment-repository/images
     * Java method: de.bwl.bwfla.emil.EnvironmentRepository$Images.update
     */
    update$POST$environmentrepository_images(arg0: com_openslx_eaas_imagearchive_databind_ImageMetaData, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`environment-repository/images`, data: arg0, options: options });
    }

    /**
     * @deprecated
     * HTTP GET /environment-repository/images-index
     * Java method: de.bwl.bwfla.emil.EnvironmentRepository.getImagesIndex
     */
    getImagesIndex(options?: O): RestResponse<de_bwl_bwfla_api_imagearchive_ImageNameIndex> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`environment-repository/images-index`, options: options });
    }

    /**
     * HTTP GET /environment-repository/images/{imgid}/url
     * Java method: de.bwl.bwfla.emil.EnvironmentRepository$Images.resolveGET
     */
    resolveGET$GET$environmentrepository_images_imgid_url(imgid: string, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`environment-repository/images/${imgid}/url`, options: options });
    }

    /**
     * HTTP HEAD /environment-repository/images/{imgid}/url
     * Java method: de.bwl.bwfla.emil.EnvironmentRepository$Images.resolveHEAD
     */
    resolveHEAD$HEAD$environmentrepository_images_imgid_url(imgid: string, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "HEAD", url: uriEncoding`environment-repository/images/${imgid}/url`, options: options });
    }

    /**
     * HTTP GET /environment-repository/os-metadata
     * Java method: de.bwl.bwfla.emil.EnvironmentRepository.getOperatingSystemMetadata
     */
    getOperatingSystemMetadata$GET$environmentrepository_osmetadata(options?: O): RestResponse<de_bwl_bwfla_common_datatypes_identification_OperatingSystems> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`environment-repository/os-metadata`, options: options });
    }

    /**
     * HTTP GET /environment-repository/patches
     * Java method: de.bwl.bwfla.emil.EnvironmentRepository$Patches.list
     */
    list$GET$environmentrepository_patches(options?: O): RestResponse<de_bwl_bwfla_api_imagearchive_ImageGeneralizationPatchDescription[]> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`environment-repository/patches`, options: options });
    }

    /**
     * HTTP POST /environment-repository/patches/{patchId}
     * Java method: de.bwl.bwfla.emil.EnvironmentRepository$Patches.apply
     */
    apply(patchId: string, arg1: de_bwl_bwfla_emil_datatypes_ImageGeneralizationPatchRequest, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`environment-repository/patches/${patchId}`, data: arg1, options: options });
    }

    /**
     * HTTP GET /environment-repository/templates
     * Java method: de.bwl.bwfla.emil.EnvironmentRepository$Templates.list
     */
    list$GET$environmentrepository_templates(queryParams?: { compat?: string; }, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`environment-repository/templates`, queryParams: queryParams, options: options });
    }

    /**
     * HTTP GET /error-report
     * Java method: de.bwl.bwfla.emil.ErrorReport.getErrorReport
     */
    getErrorReport(options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`error-report`, options: options });
    }

    /**
     * HTTP POST /handles
     * Java method: de.bwl.bwfla.emil.Handles.createHandle
     */
    createHandle(arg0: de_bwl_bwfla_emil_datatypes_rest_HandleRequest, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`handles`, data: arg0, options: options });
    }

    /**
     * HTTP GET /handles
     * Java method: de.bwl.bwfla.emil.Handles.getHandleList
     */
    getHandleList(options?: O): RestResponse<de_bwl_bwfla_emil_datatypes_rest_HandleListResponse> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`handles`, options: options });
    }

    /**
     * HTTP DELETE /handles/{handle}
     * Java method: de.bwl.bwfla.emil.Handles.deleteHandle
     */
    deleteHandle(handle: string, options?: O): RestResponse<void> {
        return this.httpClient.request({ method: "DELETE", url: uriEncoding`handles/${handle}`, options: options });
    }

    /**
     * HTTP GET /handles/{handle}
     * Java method: de.bwl.bwfla.emil.Handles.getHandleValue
     */
    getHandleValue(handle: string, options?: O): RestResponse<de_bwl_bwfla_emil_datatypes_rest_HandleValueResponse> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`handles/${handle}`, options: options });
    }

    /**
     * HTTP POST /handles/{handle}
     * Java method: de.bwl.bwfla.emil.Handles.updateHandleValue
     */
    updateHandleValue(handle: string, arg0: de_bwl_bwfla_emil_datatypes_rest_HandleRequest, options?: O): RestResponse<void> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`handles/${handle}`, data: arg0, options: options });
    }

    /**
     * HTTP DELETE /handles/{prefix}/{handle}
     * Java method: de.bwl.bwfla.emil.Handles.deleteHandleWithPrefix
     */
    deleteHandleWithPrefix(prefix: string, handle: string, options?: O): RestResponse<void> {
        return this.httpClient.request({ method: "DELETE", url: uriEncoding`handles/${prefix}/${handle}`, options: options });
    }

    /**
     * HTTP GET /handles/{prefix}/{handle}
     * Java method: de.bwl.bwfla.emil.Handles.getHandleValueWithPrefix
     */
    getHandleValueWithPrefix(prefix: string, handle: string, options?: O): RestResponse<de_bwl_bwfla_emil_datatypes_rest_HandleValueResponse> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`handles/${prefix}/${handle}`, options: options });
    }

    /**
     * HTTP POST /handles/{prefix}/{handle}
     * Java method: de.bwl.bwfla.emil.Handles.updateHandleValueWithPrefix
     */
    updateHandleValueWithPrefix(prefix: string, handle: string, arg0: de_bwl_bwfla_emil_datatypes_rest_HandleRequest, options?: O): RestResponse<void> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`handles/${prefix}/${handle}`, data: arg0, options: options });
    }

    /**
     * HTTP GET /metadata-repositories/{name}/item-identifiers
     * Java method: de.bwl.bwfla.metadata.repository.MetaDataRepositoryAPI$ItemIdentifiers.list
     */
    list$GET$metadatarepositories_name_itemidentifiers(name: string, options?: O): RestResponse<java_util_concurrent_CompletionStage<javax_ws_rs_core_Response>> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`metadata-repositories/${name}/item-identifiers`, options: options });
    }

    /**
     * HTTP POST /metadata-repositories/{name}/items
     * Java method: de.bwl.bwfla.metadata.repository.MetaDataRepositoryAPI$Items.insert
     */
    insert(name: string, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`metadata-repositories/${name}/items`, options: options });
    }

    /**
     * HTTP GET /metadata-repositories/{name}/items
     * Java method: de.bwl.bwfla.metadata.repository.MetaDataRepositoryAPI$Items.list
     */
    list$GET$metadatarepositories_name_items(name: string, options?: O): RestResponse<java_util_concurrent_CompletionStage<javax_ws_rs_core_Response>> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`metadata-repositories/${name}/items`, options: options });
    }

    /**
     * HTTP GET /metadata-repositories/{name}/sets
     * Java method: de.bwl.bwfla.metadata.repository.MetaDataRepositoryAPI$Sets.list
     */
    list$GET$metadatarepositories_name_sets(name: string, options?: O): RestResponse<java_util_concurrent_CompletionStage<javax_ws_rs_core_Response>> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`metadata-repositories/${name}/sets`, options: options });
    }

    /**
     * HTTP HEAD /metadata-repositories/{name}/sets
     * Java method: de.bwl.bwfla.metadata.repository.MetaDataRepositoryAPI$Sets.supported
     */
    supported(name: string, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "HEAD", url: uriEncoding`metadata-repositories/${name}/sets`, options: options });
    }

    /**
     * HTTP HEAD /metadata-repositories/{name}/sets/{setspec}
     * Java method: de.bwl.bwfla.metadata.repository.MetaDataRepositoryAPI$Sets.exists
     */
    exists(name: string, setspec: string, options?: O): RestResponse<java_util_concurrent_CompletionStage<javax_ws_rs_core_Response>> {
        return this.httpClient.request({ method: "HEAD", url: uriEncoding`metadata-repositories/${name}/sets/${setspec}`, options: options });
    }

    /**
     * HTTP PUT /network-environments/
     * Java method: de.bwl.bwfla.emil.NetworkEnvironments.createNetworkEnvironment
     */
    createNetworkEnvironment(arg0: de_bwl_bwfla_emil_datatypes_NetworkEnvironment, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "PUT", url: uriEncoding`network-environments/`, data: arg0, options: options });
    }

    /**
     * HTTP GET /network-environments/
     * Java method: de.bwl.bwfla.emil.NetworkEnvironments.getNetworkEnvironments
     */
    getNetworkEnvironments(options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`network-environments/`, options: options });
    }

    /**
     * HTTP POST /network-environments/
     * Java method: de.bwl.bwfla.emil.NetworkEnvironments.updateNetworkEnvironment
     */
    updateNetworkEnvironment(arg0: de_bwl_bwfla_emil_datatypes_NetworkEnvironment, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`network-environments/`, data: arg0, options: options });
    }

    /**
     * HTTP DELETE /network-environments/{envId}
     * Java method: de.bwl.bwfla.emil.NetworkEnvironments.deleteNetworkEnvironment
     */
    deleteNetworkEnvironment(envId: string, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "DELETE", url: uriEncoding`network-environments/${envId}`, options: options });
    }

    /**
     * HTTP GET /network-environments/{envId}
     * Java method: de.bwl.bwfla.emil.NetworkEnvironments.getNetworkEnvironment
     */
    getNetworkEnvironment(envId: string, queryParams?: { jsonUrl?: boolean; json?: boolean; }, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`network-environments/${envId}`, queryParams: queryParams, options: options });
    }

    /**
     * HTTP POST /networks
     * Java method: de.bwl.bwfla.emil.Networks.createNetwork
     */
    createNetwork(arg0: de_bwl_bwfla_emil_datatypes_NetworkRequest, options?: O): RestResponse<de_bwl_bwfla_emil_datatypes_NetworkResponse> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`networks`, data: arg0, options: options });
    }

    /**
     * HTTP POST /networks/{id}/addComponentToSwitch
     * Java method: de.bwl.bwfla.emil.Networks.addComponentToSwitch
     */
    addComponentToSwitch(id: string, arg1: de_bwl_bwfla_emil_datatypes_NetworkRequest$ComponentSpec, options?: O): RestResponse<void> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`networks/${id}/addComponentToSwitch`, data: arg1, options: options });
    }

    /**
     * HTTP POST /networks/{id}/components
     * Java method: de.bwl.bwfla.emil.Networks.addComponent
     */
    addComponent(id: string, arg1: de_bwl_bwfla_emil_datatypes_NetworkRequest$ComponentSpec, options?: O): RestResponse<void> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`networks/${id}/components`, data: arg1, options: options });
    }

    /**
     * HTTP DELETE /networks/{id}/components/{componentId}
     * Java method: de.bwl.bwfla.emil.Networks.removeComponent
     */
    removeComponent(id: string, componentId: string, options?: O): RestResponse<void> {
        return this.httpClient.request({ method: "DELETE", url: uriEncoding`networks/${id}/components/${componentId}`, options: options });
    }

    /**
     * HTTP POST /networks/{id}/components/{componentId}/disconnect
     * Java method: de.bwl.bwfla.emil.Networks.disconnectComponent
     */
    disconnectComponent(id: string, componentId: string, options?: O): RestResponse<void> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`networks/${id}/components/${componentId}/disconnect`, options: options });
    }

    /**
     * HTTP GET /networks/{id}/wsConnection
     * Java method: de.bwl.bwfla.emil.Networks.wsConnection
     */
    wsConnection(id: string, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`networks/${id}/wsConnection`, options: options });
    }

    /**
     * HTTP GET /object-repository/actions/sync
     * Java method: de.bwl.bwfla.emil.ObjectRepository$Actions.sync
     */
    sync$GET$objectrepository_actions_sync(options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`object-repository/actions/sync`, options: options });
    }

    /**
     * HTTP GET /object-repository/archives
     * Java method: de.bwl.bwfla.emil.ObjectRepository$Archives.list
     */
    list$GET$objectrepository_archives(options?: O): RestResponse<de_bwl_bwfla_emil_datatypes_rest_ObjectArchivesResponse> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`object-repository/archives`, options: options });
    }

    /**
     * HTTP POST /object-repository/archives/{archiveId}/actions/sync
     * Java method: de.bwl.bwfla.emil.ObjectRepository$Archives.sync
     */
    sync$POST$objectrepository_archives_archiveId_actions_sync(archiveId: string, arg1: de_bwl_bwfla_emil_datatypes_rest_SyncObjectRequest, options?: O): RestResponse<de_bwl_bwfla_emil_datatypes_rest_TaskStateResponse> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`object-repository/archives/${archiveId}/actions/sync`, data: arg1, options: options });
    }

    /**
     * HTTP POST /object-repository/archives/{archiveId}/objects
     * Java method: de.bwl.bwfla.emil.ObjectRepository$Objects.importObject
     */
    importObject$POST$objectrepository_archives_archiveId_objects(archiveId: string, arg0: de_bwl_bwfla_emil_datatypes_rest_ImportObjectRequest, options?: O): RestResponse<de_bwl_bwfla_emil_datatypes_rest_TaskStateResponse> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`object-repository/archives/${archiveId}/objects`, data: arg0, options: options });
    }

    /**
     * HTTP GET /object-repository/archives/{archiveId}/objects
     * Java method: de.bwl.bwfla.emil.ObjectRepository$Objects.list
     */
    list$GET$objectrepository_archives_archiveId_objects(archiveId: string, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`object-repository/archives/${archiveId}/objects`, options: options });
    }

    /**
     * HTTP DELETE /object-repository/archives/{archiveId}/objects/{objectId}
     * Java method: de.bwl.bwfla.emil.ObjectRepository$Objects.delete
     */
    delete$DELETE$objectrepository_archives_archiveId_objects_objectId(archiveId: string, objectId: string, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "DELETE", url: uriEncoding`object-repository/archives/${archiveId}/objects/${objectId}`, options: options });
    }

    /**
     * HTTP GET /object-repository/archives/{archiveId}/objects/{objectId}
     * Java method: de.bwl.bwfla.emil.ObjectRepository$Objects.get
     */
    get$GET$objectrepository_archives_archiveId_objects_objectId(archiveId: string, objectId: string, options?: O): RestResponse<de_bwl_bwfla_emil_datatypes_rest_MediaDescriptionResponse> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`object-repository/archives/${archiveId}/objects/${objectId}`, options: options });
    }

    /**
     * HTTP PUT /object-repository/archives/{archiveId}/objects/{objectId}/label
     * Java method: de.bwl.bwfla.emil.ObjectRepository$Objects.putChangeLabel
     */
    putChangeLabel(archiveId: string, objectId: string, arg0: de_bwl_bwfla_emil_datatypes_rest_ChangeObjectLabelRequest, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "PUT", url: uriEncoding`object-repository/archives/${archiveId}/objects/${objectId}/label`, data: arg0, options: options });
    }

    /**
     * HTTP GET /object-repository/archives/{archiveId}/objects/{objectId}/resources/{resourceId}/url
     * Java method: de.bwl.bwfla.emil.ObjectRepository$Objects.resolveGET
     */
    resolveGET$GET$objectrepository_archives_archiveId_objects_objectId_resources_resourceId_url(archiveId: string, objectId: string, resourceId: string, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`object-repository/archives/${archiveId}/objects/${objectId}/resources/${resourceId}/url`, options: options });
    }

    /**
     * HTTP HEAD /object-repository/archives/{archiveId}/objects/{objectId}/resources/{resourceId}/url
     * Java method: de.bwl.bwfla.emil.ObjectRepository$Objects.resolveHEAD
     */
    resolveHEAD$HEAD$objectrepository_archives_archiveId_objects_objectId_resources_resourceId_url(archiveId: string, objectId: string, resourceId: string, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "HEAD", url: uriEncoding`object-repository/archives/${archiveId}/objects/${objectId}/resources/${resourceId}/url`, options: options });
    }

    /**
     * HTTP GET /object-repository/tasks/{taskId}
     * Java method: de.bwl.bwfla.emil.ObjectRepository$Tasks.get
     */
    get$GET$objectrepository_tasks_taskId(taskId: string, options?: O): RestResponse<de_bwl_bwfla_emil_datatypes_rest_TaskStateResponse> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`object-repository/tasks/${taskId}`, options: options });
    }

    /**
     * HTTP GET /objects/archives
     * Java method: de.bwl.bwfla.emil.EmilObjectData.getArchives
     */
    getArchives(options?: O): RestResponse<de_bwl_bwfla_emil_datatypes_rest_ObjectArchivesResponse> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`objects/archives`, options: options });
    }

    /**
     * HTTP POST /objects/import
     * Java method: de.bwl.bwfla.emil.EmilObjectData.importObject
     */
    importObject$POST$objects_import(arg0: de_bwl_bwfla_emil_datatypes_rest_ImportObjectRequest, options?: O): RestResponse<de_bwl_bwfla_emil_datatypes_rest_TaskStateResponse> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`objects/import`, data: arg0, options: options });
    }

    /**
     * HTTP GET /objects/objectImportTaskState
     * Java method: de.bwl.bwfla.emil.EmilObjectData.getObjectImportTaskState
     */
    getObjectImportTaskState(queryParams?: { taskId?: string; }, options?: O): RestResponse<de_bwl_bwfla_emil_datatypes_rest_TaskStateResponse> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`objects/objectImportTaskState`, queryParams: queryParams, options: options });
    }

    /**
     * HTTP GET /objects/sync
     * Java method: de.bwl.bwfla.emil.EmilObjectData.sync
     */
    sync$GET$objects_sync(options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`objects/sync`, options: options });
    }

    /**
     * HTTP POST /objects/syncObjects
     * Java method: de.bwl.bwfla.emil.EmilObjectData.syncObjects
     */
    syncObjects(arg0: de_bwl_bwfla_emil_datatypes_rest_SyncObjectRequest, options?: O): RestResponse<de_bwl_bwfla_emil_datatypes_rest_TaskStateResponse> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`objects/syncObjects`, data: arg0, options: options });
    }

    /**
     * HTTP GET /objects/{objectArchive}
     * Java method: de.bwl.bwfla.emil.EmilObjectData.list
     */
    list$GET$objects_objectArchive(objectArchive: string, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`objects/${objectArchive}`, options: options });
    }

    /**
     * HTTP DELETE /objects/{objectArchive}/{objectId}
     * Java method: de.bwl.bwfla.emil.EmilObjectData.delete
     */
    delete$DELETE$objects_objectArchive_objectId(objectArchive: string, objectId: string, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "DELETE", url: uriEncoding`objects/${objectArchive}/${objectId}`, options: options });
    }

    /**
     * HTTP GET /objects/{objectArchive}/{objectId}
     * Java method: de.bwl.bwfla.emil.EmilObjectData.mediaDescription
     */
    mediaDescription$GET$objects_objectArchive_objectId(objectArchive: string, objectId: string, options?: O): RestResponse<de_bwl_bwfla_emil_datatypes_rest_MediaDescriptionResponse> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`objects/${objectArchive}/${objectId}`, options: options });
    }

    /**
     * HTTP PUT /objects/{objectArchive}/{objectId}/label
     * Java method: de.bwl.bwfla.emil.EmilObjectData.mediaDescription
     */
    mediaDescription$PUT$objects_objectArchive_objectId_label(objectArchive: string, objectId: string, arg0: de_bwl_bwfla_emil_datatypes_rest_ChangeObjectLabelRequest, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "PUT", url: uriEncoding`objects/${objectArchive}/${objectId}/label`, data: arg0, options: options });
    }

    /**
     * HTTP GET /operator/api/v1/channels
     * Java method: de.bwl.bwfla.emil.Operator.getChannels
     */
    getChannels$GET$operator_api_v1_channels(options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`operator/api/v1/channels`, options: options });
    }

    /**
     * HTTP GET /operator/api/v1/channels
     * Java method: de.bwl.bwfla.emil.OperatorProxy.getChannels
     */
    getChannels$GET$operator_api_v1_channels(options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`operator/api/v1/channels`, options: options });
    }

    /**
     * HTTP POST /operator/api/v1/channels/{channel}
     * Java method: de.bwl.bwfla.emil.Operator.reload
     */
    reload$POST$operator_api_v1_channels_channel(channel: string, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`operator/api/v1/channels/${channel}`, options: options });
    }

    /**
     * HTTP POST /operator/api/v1/channels/{channel}
     * Java method: de.bwl.bwfla.emil.OperatorProxy.reload
     */
    reload$POST$operator_api_v1_channels_channel(channel: string, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`operator/api/v1/channels/${channel}`, options: options });
    }

    /**
     * HTTP GET /operator/api/v1/channels/{channel}/releases
     * Java method: de.bwl.bwfla.emil.Operator.getReleases
     */
    getReleases$GET$operator_api_v1_channels_channel_releases(channel: string, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`operator/api/v1/channels/${channel}/releases`, options: options });
    }

    /**
     * HTTP GET /operator/api/v1/channels/{channel}/releases
     * Java method: de.bwl.bwfla.emil.OperatorProxy.getReleases
     */
    getReleases$GET$operator_api_v1_channels_channel_releases(channel: string, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`operator/api/v1/channels/${channel}/releases`, options: options });
    }

    /**
     * HTTP GET /operator/api/v1/channels/{channel}/releases/latest
     * Java method: de.bwl.bwfla.emil.Operator.getLatest
     */
    getLatest$GET$operator_api_v1_channels_channel_releases_latest(channel: string, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`operator/api/v1/channels/${channel}/releases/latest`, options: options });
    }

    /**
     * HTTP GET /operator/api/v1/channels/{channel}/releases/latest
     * Java method: de.bwl.bwfla.emil.OperatorProxy.getLatest
     */
    getLatest$GET$operator_api_v1_channels_channel_releases_latest(channel: string, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`operator/api/v1/channels/${channel}/releases/latest`, options: options });
    }

    /**
     * HTTP POST /operator/api/v1/channels/{channel}/releases/{version}
     * Java method: de.bwl.bwfla.emil.Operator.update
     */
    update$POST$operator_api_v1_channels_channel_releases_version(channel: string, version: string, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`operator/api/v1/channels/${channel}/releases/${version}`, options: options });
    }

    /**
     * HTTP POST /operator/api/v1/channels/{channel}/releases/{version}
     * Java method: de.bwl.bwfla.emil.OperatorProxy.update
     */
    update$POST$operator_api_v1_channels_channel_releases_version(channel: string, version: string, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`operator/api/v1/channels/${channel}/releases/${version}`, options: options });
    }

    /**
     * HTTP GET /operator/api/v1/deployment/current
     * Java method: de.bwl.bwfla.emil.Operator.getCurrent
     */
    getCurrent$GET$operator_api_v1_deployment_current(options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`operator/api/v1/deployment/current`, options: options });
    }

    /**
     * HTTP POST /operator/api/v1/deployment/current
     * Java method: de.bwl.bwfla.emil.Operator.redeployCurrent
     */
    redeployCurrent$POST$operator_api_v1_deployment_current(options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`operator/api/v1/deployment/current`, options: options });
    }

    /**
     * HTTP GET /operator/api/v1/deployment/current
     * Java method: de.bwl.bwfla.emil.OperatorProxy.getCurrent
     */
    getCurrent$GET$operator_api_v1_deployment_current(options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`operator/api/v1/deployment/current`, options: options });
    }

    /**
     * HTTP POST /operator/api/v1/deployment/current
     * Java method: de.bwl.bwfla.emil.OperatorProxy.redeployCurrent
     */
    redeployCurrent$POST$operator_api_v1_deployment_current(options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`operator/api/v1/deployment/current`, options: options });
    }

    /**
     * HTTP GET /operator/api/v1/deployment/previous
     * Java method: de.bwl.bwfla.emil.Operator.getPrevious
     */
    getPrevious$GET$operator_api_v1_deployment_previous(options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`operator/api/v1/deployment/previous`, options: options });
    }

    /**
     * HTTP POST /operator/api/v1/deployment/previous
     * Java method: de.bwl.bwfla.emil.Operator.redeployPrevious
     */
    redeployPrevious$POST$operator_api_v1_deployment_previous(options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`operator/api/v1/deployment/previous`, options: options });
    }

    /**
     * HTTP GET /operator/api/v1/deployment/previous
     * Java method: de.bwl.bwfla.emil.OperatorProxy.getPrevious
     */
    getPrevious$GET$operator_api_v1_deployment_previous(options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`operator/api/v1/deployment/previous`, options: options });
    }

    /**
     * HTTP POST /operator/api/v1/deployment/previous
     * Java method: de.bwl.bwfla.emil.OperatorProxy.redeployPrevious
     */
    redeployPrevious$POST$operator_api_v1_deployment_previous(options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`operator/api/v1/deployment/previous`, options: options });
    }

    /**
     * HTTP GET /sessions
     * Java method: de.bwl.bwfla.emil.session.Sessions.list
     */
    list$GET$sessions(options?: O): RestResponse<de_bwl_bwfla_emil_session_Session[]> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`sessions`, options: options });
    }

    /**
     * HTTP GET /sessions/network-environments
     * Java method: de.bwl.bwfla.emil.session.Sessions.getSessionsWithNetworkEnvID
     */
    getSessionsWithNetworkEnvID(options?: O): RestResponse<de_bwl_bwfla_emil_session_rest_RunningNetworkEnvironmentResponse[]> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`sessions/network-environments`, options: options });
    }

    /**
     * HTTP DELETE /sessions/{id}
     * Java method: de.bwl.bwfla.emil.session.Sessions.delete
     */
    delete$DELETE$sessions_id(id: string, options?: O): RestResponse<void> {
        return this.httpClient.request({ method: "DELETE", url: uriEncoding`sessions/${id}`, options: options });
    }

    /**
     * HTTP GET /sessions/{id}
     * Java method: de.bwl.bwfla.emil.session.Sessions.listComponents
     */
    listComponents(id: string, options?: O): RestResponse<de_bwl_bwfla_emil_session_rest_SessionResponse> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`sessions/${id}`, options: options });
    }

    /**
     * HTTP POST /sessions/{id}/detach
     * Java method: de.bwl.bwfla.emil.session.Sessions.setLifetime
     */
    setLifetime(id: string, arg1: de_bwl_bwfla_emil_session_rest_DetachRequest, options?: O): RestResponse<void> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`sessions/${id}/detach`, data: arg1, options: options });
    }

    /**
     * HTTP POST /sessions/{id}/keepalive
     * Java method: de.bwl.bwfla.emil.session.Sessions.keepalive
     */
    keepalive$POST$sessions_id_keepalive(id: string, options?: O): RestResponse<void> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`sessions/${id}/keepalive`, options: options });
    }

    /**
     * HTTP DELETE /sessions/{id}/resources
     * Java method: de.bwl.bwfla.emil.session.Sessions.removeResources
     */
    removeResources(id: string, arg1: string[], options?: O): RestResponse<void> {
        return this.httpClient.request({ method: "DELETE", url: uriEncoding`sessions/${id}/resources`, data: arg1, options: options });
    }

    /**
     * HTTP GET /software-repository/descriptions
     * Java method: de.bwl.bwfla.emil.SoftwareRepository$SoftwareDescriptions.list
     */
    list$GET$softwarerepository_descriptions(options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`software-repository/descriptions`, options: options });
    }

    /**
     * HTTP GET /software-repository/descriptions/{softwareId}
     * Java method: de.bwl.bwfla.emil.SoftwareRepository$SoftwareDescriptions.get
     */
    get$GET$softwarerepository_descriptions_softwareId(softwareId: string, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`software-repository/descriptions/${softwareId}`, options: options });
    }

    /**
     * HTTP POST /software-repository/packages
     * Java method: de.bwl.bwfla.emil.SoftwareRepository$SoftwarePackages.create
     */
    create$POST$softwarerepository_packages(arg0: de_bwl_bwfla_emil_datatypes_EmilSoftwareObject, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`software-repository/packages`, data: arg0, options: options });
    }

    /**
     * HTTP GET /software-repository/packages
     * Java method: de.bwl.bwfla.emil.SoftwareRepository$SoftwarePackages.list
     */
    list$GET$softwarerepository_packages(options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`software-repository/packages`, options: options });
    }

    /**
     * HTTP DELETE /software-repository/packages/{softwareId}
     * Java method: de.bwl.bwfla.emil.SoftwareRepository$SoftwarePackages.deleteSoftware
     */
    deleteSoftware(softwareId: string, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "DELETE", url: uriEncoding`software-repository/packages/${softwareId}`, options: options });
    }

    /**
     * HTTP GET /software-repository/packages/{softwareId}
     * Java method: de.bwl.bwfla.emil.SoftwareRepository$SoftwarePackages.get
     */
    get$GET$softwarerepository_packages_softwareId(softwareId: string, options?: O): RestResponse<any> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`software-repository/packages/${softwareId}`, options: options });
    }

    /**
     * HTTP GET /tasks/{id}
     * Java method: de.bwl.bwfla.emil.utils.TaskManager.lookup
     */
    lookup(id: string, queryParams?: { cleanup?: boolean; }, options?: O): RestResponse<de_bwl_bwfla_emil_datatypes_rest_TaskStateResponse> {
        return this.httpClient.request({ method: "GET", url: uriEncoding`tasks/${id}`, queryParams: queryParams, options: options });
    }

    /**
     * HTTP DELETE /tasks/{id}
     * Java method: de.bwl.bwfla.emil.utils.TaskManager.remove
     */
    remove(id: string, options?: O): RestResponse<void> {
        return this.httpClient.request({ method: "DELETE", url: uriEncoding`tasks/${id}`, options: options });
    }

    /**
     * HTTP POST /upload/
     * Java method: de.bwl.bwfla.emil.utils.Upload.upload
     */
    upload(arg0: org_jboss_resteasy_plugins_providers_multipart_MultipartFormDataInput, options?: O): RestResponse<de_bwl_bwfla_emil_datatypes_rest_UploadResponse> {
        return this.httpClient.request({ method: "POST", url: uriEncoding`upload/`, data: arg0, options: options });
    }
}

export type RestResponse<R> = Promise<Axios.GenericAxiosResponse<R>>;

export type de_bwl_bwfla_api_imagearchive_ImageType = "BASE" | "OBJECT" | "USER" | "DERIVATE" | "SYSTEM" | "TEMPLATE" | "TMP" | "SESSIONS" | "ROMS" | "CONTAINERS" | "CHECKPOINTS" | "RUNTIME";

export type de_bwl_bwfla_common_datatypes_ConnectionType = "HTTP" | "HTTPS" | "VNC" | "RDP";

export type de_bwl_bwfla_common_datatypes_EaasState = "SESSION_UNDEFINED" | "SESSION_ALLOCATING" | "SESSION_READY" | "SESSION_BUSY" | "SESSION_RUNNING" | "SESSION_INACTIVE" | "SESSION_STOPPED" | "SESSION_OUT_OF_RESOURCES" | "SESSION_FAILED" | "SESSION_CLIENT_FAULT";

export type de_bwl_bwfla_common_datatypes_EmuCompState = "EMULATOR_UNDEFINED" | "EMULATOR_BUSY" | "EMULATOR_READY" | "EMULATOR_RUNNING" | "EMULATOR_INACTIVE" | "EMULATOR_STOPPED" | "EMULATOR_FAILED";

export type de_bwl_bwfla_common_datatypes_ProcessMonitorVID = "STATE" | "USER_MODE_TIME" | "KERNEL_MODE_TIME" | "VIRTUAL_MEMORY_SIZE" | "INSTRUCTION_POINTER";

export type de_bwl_bwfla_common_datatypes_ResourceState = "RESOURCE_ALLOCATING" | "RESOURCE_READY" | "RESOURCE_RELEASED" | "OUT_OF_RESOURCES" | "CLIENT_FAULT";

export type de_bwl_bwfla_common_services_container_types_Container$Filesystem = "FAT12" | "FAT16" | "HFS" | "ISO" | "ZIP";

export type de_bwl_bwfla_common_services_guacplay_GuacDefs$SourceType = "CLIENT" | "SERVER" | "INTERNAL" | "UNKNOWN";

export type de_bwl_bwfla_common_services_guacplay_capture_ScreenShooter$State = "READY" | "PREPARED" | "FINISHED";

export type de_bwl_bwfla_common_services_guacplay_io_FileReader$State = "READY" | "READING" | "CLOSED";

export type de_bwl_bwfla_common_services_guacplay_io_FileWriter$State = "READY" | "WRITING" | "CLOSED";

export type de_bwl_bwfla_common_services_guacplay_record_SessionRecorder$State = "READY" | "PREPARED" | "FINISHED";

export type de_bwl_bwfla_common_services_guacplay_replay_SessionPlayer$State = "READY" | "PREPARED" | "FINISHED";

export type de_bwl_bwfla_common_services_security_EmilEnvironmentPermissions$Permissions = "NONE" | "READ" | "WRITE";

export type de_bwl_bwfla_common_services_security_Role = "PUBLIC" | "RESTRICTED" | "ADMIN";

export type de_bwl_bwfla_common_utils_DeprecatedProcessRunner$State = "INVALID" | "READY" | "STARTED" | "STOPPED";

export type de_bwl_bwfla_common_utils_DiskDescription$Parser$DiskFields = "PATH" | "SIZE" | "TRANSPORT" | "LOGICAL_SECTOR_SIZE" | "PHYSICAL_SECTOR_SIZE" | "PARTITION_TABLE_TYPE" | "MODEL" | "FLAGS" | "__LAST_FIELD__";

export type de_bwl_bwfla_common_utils_DiskDescription$Parser$PartitionFields = "INDEX" | "START" | "END" | "SIZE" | "FILESYSTEM_TYPE" | "NAME" | "FLAGS" | "__LAST_FIELD__";

export type de_bwl_bwfla_common_utils_DiskDescription$Parser$State = "EXPECTING_HEADER" | "EXPECTING_DISK_DESCRIPTION" | "EXPECTING_PART_DESCRIPTION";

export type de_bwl_bwfla_common_utils_DiskDescription$PartitionTableType = "BSD" | "LOOP" | "GPT" | "MAC" | "MSDOS" | "PC98" | "SUN" | "UNKNOWN";

export type de_bwl_bwfla_common_utils_DiskDescription$Transport = "SD_MMC" | "IDE" | "SCSI" | "NVME" | "VIRTBLK" | "LOOPBACK" | "FILE" | "UNKNOWN";

export type de_bwl_bwfla_common_utils_ImageInformation$QemuImageFormat = "QCOW2" | "RAW" | "VDI" | "VHD" | "VMDK" | "EWF" | "VHDX";

export type de_bwl_bwfla_common_utils_METS_MetsUtil$MetsEaasConstant = "FILE_GROUP_OBJECTS";

export type de_bwl_bwfla_common_utils_METS_MetsUtil$MetsEaasContext = "INSTALLATION" | "USAGE";

export type de_bwl_bwfla_common_utils_SystemMonitor$ValueID = "LOAD_AVERAGE_1MIN" | "LOAD_AVERAGE_5MIN" | "LOAD_AVERAGE_15MIN" | "MEMORY_TOTAL" | "MEMORY_FREE" | "MEMORY_BUFFERS" | "MEMORY_CACHED" | "MEMORY_ACTIVE";

export type de_bwl_bwfla_eaas_cluster_ResourceSpec$CpuUnit = "CORES" | "MILLICORES";

export type de_bwl_bwfla_eaas_cluster_ResourceSpec$MemoryUnit = "MEGABYTES" | "GIGABYTES";

export type de_bwl_bwfla_emil_datatypes_SessionResource$Type = "COMPONENT" | "NETWORK";

export type de_bwl_bwfla_emil_datatypes_rest_CreateContainerImageRequest$ContainerType = "ROOTFS" | "SIMG" | "DOCKERHUB" | "READYMADE";

export type de_bwl_bwfla_emil_tasks_ExportEnvironmentTask$ExportEnvironmentRequest$ExportTarget = "FILEPATH";

export type de_bwl_bwfla_emil_utils_ImportCounts = "IMPORTED" | "FAILED" | "__LAST";

export type de_bwl_bwfla_emucomp_api_Binding$AccessType = "COW" | "COPY";

export type de_bwl_bwfla_emucomp_api_Binding$ResourceType = "ISO" | "DISK" | "FLOPPY" | "CART" | "ZIP" | "TAR" | "FILE";

export type de_bwl_bwfla_emucomp_api_Binding$TransportType = "FILE" | "AUTO";

export type de_bwl_bwfla_emucomp_api_ComponentState = "INITIALIZING" | "INACTIVE" | "STOPPED" | "FAILED" | "RUNNING";

export type de_bwl_bwfla_emucomp_api_Drive$DriveType = "CDROM" | "DISK" | "FLOPPY" | "CART";

export type de_bwl_bwfla_emucomp_api_EmulatorUtils$XmountInputFormat = "RAW" | "QEMU";

export type de_bwl_bwfla_emucomp_api_EmulatorUtils$XmountOutputFormat = "RAW" | "VDI" | "VHD" | "VMDK";

export type de_bwl_bwfla_emucomp_api_FileSystemType = "RAW" | "FAT16" | "FAT32" | "VFAT" | "NTFS" | "EXT2" | "EXT3" | "EXT4" | "HFS" | "ISO9660";

export type de_bwl_bwfla_emucomp_api_MediumType = "HDD" | "CDROM" | "FLOPPY";

export type de_bwl_bwfla_emucomp_api_PartitionTableType = "MBR" | "GPT" | "NONE";

export type de_bwl_bwfla_envproposer_api_ProposalRequest$DataType = "ZIP" | "TAR" | "BAGIT_ZIP" | "BAGIT_TAR";

export type de_bwl_bwfla_imagebuilder_api_ImageContentDescription$Action = "COPY" | "EXTRACT" | "RSYNC";

export type de_bwl_bwfla_imagebuilder_api_ImageContentDescription$ArchiveFormat = "ZIP" | "TAR" | "SIMG" | "DOCKER";

export type java_util_concurrent_TimeUnit = "NANOSECONDS" | "MICROSECONDS" | "MILLISECONDS" | "SECONDS" | "MINUTES" | "HOURS" | "DAYS";

export type com_fasterxml_jackson_jaxrs_cfg_Annotations = "JACKSON" | "JAXB";

export type javax_servlet_SessionTrackingMode = "COOKIE" | "URL" | "SSL";

export type javax_ws_rs_core_Response$Status$Family = "INFORMATIONAL" | "SUCCESSFUL" | "REDIRECTION" | "CLIENT_ERROR" | "SERVER_ERROR" | "OTHER";

export type de_bwl_bwfla_emil_datatypes_EmilEnvironmentUnion = de_bwl_bwfla_emil_datatypes_EmilEnvironment | de_bwl_bwfla_emil_datatypes_EmilObjectEnvironment | de_bwl_bwfla_emil_datatypes_EmilContainerEnvironment | de_bwl_bwfla_emil_datatypes_EmilSessionEnvironment;

export type de_bwl_bwfla_emucomp_api_AbstractDataResourceUnion = de_bwl_bwfla_emucomp_api_ObjectArchiveBinding | de_bwl_bwfla_emucomp_api_ImageArchiveBinding | de_bwl_bwfla_emucomp_api_BlobStoreBinding | de_bwl_bwfla_emucomp_api_Binding;

export type de_bwl_bwfla_emucomp_api_ContainerConfigurationUnion = de_bwl_bwfla_emucomp_api_DockerContainerConfiguration | de_bwl_bwfla_emucomp_api_OciContainerConfiguration;

function uriEncoding(template: TemplateStringsArray, ...substitutions: any[]): string {
    let result = "";
    for (let i = 0; i < substitutions.length; i++) {
        result += template[i];
        result += encodeURIComponent(substitutions[i]);
    }
    result += template[template.length - 1];
    return result;
}


// Added by 'AxiosClientExtension' extension

import axios from "axios";
import * as Axios from "axios";

declare module "axios" {
    export interface GenericAxiosResponse<R> extends Axios.AxiosResponse {
        data: R;
    }
}

class AxiosHttpClient implements HttpClient<Axios.AxiosRequestConfig> {

    constructor(private axios: Axios.AxiosInstance) {
    }

    request<R>(requestConfig: { method: string; url: string; queryParams?: any; data?: any; copyFn?: (data: R) => R; options?: Axios.AxiosRequestConfig; }): RestResponse<R> {
        function assign(target: any, source?: any) {
            if (source != undefined) {
                for (const key in source) {
                    if (source.hasOwnProperty(key)) {
                        target[key] = source[key];
                    }
                }
            }
            return target;
        }

        const config: Axios.AxiosRequestConfig = {};
        config.method = requestConfig.method as typeof config.method;  // `string` in axios 0.16.0, `Method` in axios 0.19.0
        config.url = requestConfig.url;
        config.params = requestConfig.queryParams;
        config.data = requestConfig.data;
        assign(config, requestConfig.options);
        const copyFn = requestConfig.copyFn;

        const axiosResponse = this.axios.request(config);
        return axiosResponse.then(axiosResponse => {
            if (copyFn && axiosResponse.data) {
                (axiosResponse as any).originalData = axiosResponse.data;
                axiosResponse.data = copyFn(axiosResponse.data);
            }
            return axiosResponse;
        });
    }
}

export class AxiosRestApplicationClient extends RestApplicationClient<Axios.AxiosRequestConfig> {

    constructor(baseURL: string, axiosInstance: Axios.AxiosInstance = axios.create()) {
        axiosInstance.defaults.baseURL = baseURL;
        super(new AxiosHttpClient(axiosInstance));
    }
}
