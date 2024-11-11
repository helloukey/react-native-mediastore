import ReactNativeMediaStore from "./NativeDeleteMedia";

export type ErrorCodes =
  | "ERROR_WRITE_EXTERNAL_STORAGE_PERMISSION_NEEDED"
  | "ERROR_URIS_NOT_FOUND"
  | "ERROR_USER_REJECTED"
  | "ERROR_URIS_PARAMETER_NULL"
  | "ERROR_URIS_PARAMETER_INVALID"
  | "ERROR_MODULE_NOT_INITIALIZED"
  | "ERROR_UNEXPECTED";

export class DeleteMedia {
  static deletePhotos(uris: Array<string>): Promise<void> {
    return ReactNativeMediaStore.deletePhotos(uris);
  }
  static deleteVideos(uris: Array<string>): Promise<void> {
    return ReactNativeMediaStore.deleteVideos(uris);
  }
  static renameVideo(uri: string, newName: string): Promise<void> {
    return ReactNativeMediaStore.renameVideo(uri, newName);
  }
}
