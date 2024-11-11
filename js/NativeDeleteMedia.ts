import type { TurboModule } from "react-native/Libraries/TurboModule/RCTExport";
import { TurboModuleRegistry } from "react-native";

export interface Spec extends TurboModule {
  deletePhotos(uris: Array<string>): Promise<void>;
  deleteVideos(uris: Array<string>): Promise<void>;
  renameVideo(uri: string, newName: string): Promise<void>;
}

export default TurboModuleRegistry.get<Spec>("ReactNativeMediaStore") as Spec;
