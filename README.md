
# `@kunalukey/react-native-mediastore`

## NOTE: This module extends the [react-native-delete-media](https://www.npmjs.com/package/react-native-delete-media) module to support deleting videos as well

**_react-native-mediastore_** is a react native module used to delete media on android using the new scoped storage. **_It only supports Android_**.

It uses the native `MediaStore.createDeleteRequest` function to launch the android activity responsible for asking the user permission to delete a media file.

## Getting started

### Installing

`$ npm install @kunalukey/react-native-mediastore`

### Library configuration

1. Append the following lines to `android/settings.gradle`:

   ```gradle
   include 'kunalukey_react-native-mediastore'
   project(':kunalukey_react-native-mediastore').projectDir = new File(rootProject.projectDir, '../node_modules/@kunalukey/react-native-mediastore/android')
   ```

2. Insert the following lines inside the dependencies block in `android/app/build.gradle`:

   ```gradle
     implementation project(':kunalukey_react-native-mediastore')
   ```

3. Add the following lines to your imports in `android/app/src/main/java/com/[projectname]/MainActivity.java`:

   ```java
       import com.kunalukey.mediastore.DeleteMediaModule;
       import android.os.Bundle;
   ```

4. Create the `OnCreate` method in `MainActivity.java`, if you already have an `OnCreate` function just add the init line:

   ```java
   @Override
   public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       DeleteMediaModule.init(this);
   }
   ```

    Your `MainActivity.java` file should look like this:

    ```java
    import com.facebook.react.ReactActivity;
    import com.facebook.react.ReactActivityDelegate;
    import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint;
    import com.facebook.react.defaults.DefaultReactActivityDelegate;

    import com.rtndeletemedia.DeleteMediaModule;
    import android.os.Bundle;

    public class MainActivity extends ReactActivity {

    @Override
      public void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);
          DeleteMediaModule.init(this);
      }
    ...
    ...
    }

    ```

5. Make sure to rebuild using `yarn android`.

### Linking

This module was created using the new Turbo Native Modules.
It needs to have the New Architecture enabled in the `android/gradle.properties` file.

If you're project is not using New Architecture, you'll need to link the library manually.

To do so:

1. Follow the steps in the [`Library configuration`](#library-configuration) section.

2. In the `android/app/src/main/java/com/[projectname]/MainApplication.java` file, add:

    * `import com.kunalukey.mediastore.DeleteMediaPackage;` to the list of imports at the top of the file.

    * `packages.add(new DeleteMediaPackage());` to the `getPackages()` method.

**Note:** If you the this error while trying to use the module `[TypeError: Cannot read property 'deletePhotos' / 'deleteVideos' of null]` you probably need to manually link it.

### Permissions

You will need to add the `WRITE_EXTERNAL_STORAGE` permission to your android manifest

```manifest
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>

    <application
      ...
    </application>
</manifest>
```

And make sure to request it before calling any functions from this module.

## Usage

Here is a simple example of using the library.

```typescript
import { PermissionsAndroid, Platform } from "react-native";
import { DeleteMedia, ErrorCodes } from "@kunalukey/react-native-mediastore";

async function hasAndroidPermission() {
  const permission = PermissionsAndroid.PERMISSIONS.WRITE_EXTERNAL_STORAGE;

  const hasPermission = await PermissionsAndroid.check(permission);
  if (hasPermission) {
    return true;
  }

  const status = await PermissionsAndroid.request(permission);
  return status === "granted";
}

async function performDelete() {
  if (Platform.OS !== "android") {
    return;
  }

  if (!(await hasAndroidPermission())) {
    return;
  }

  const photoURI = "file:///storage/emulated/0/DCIM/Camera/photo.jpg";
  const videoURI = "file:///storage/emulated/0/DCIM/Camera/video.mp4";

  // Delete photo
  DeleteMedia.deletePhotos([uri])
    .then(() => {
      console.log("Image deleted");
    })
    .catch((e) => {
      const message = e.message;
      const code: ErrorCodes = e.code;

      switch (code) {
        case "ERROR_USER_REJECTED":
          console.log("Image deletion denied by user");
          break;
        default:
          console.log(message);
          break;
      }
    });

  // Delete video  
  DeleteMedia.deleteVideos([uri])
    .then(() => {
      console.log("Video deleted");
    })
    .catch((e) => {
      const message = e.message;
      const code: ErrorCodes = e.code;

      switch (code) {
        case "ERROR_USER_REJECTED":
          console.log("Video deletion denied by user");
          break;
        default:
          console.log(message);
          break;
      }
    });
}
```

## Methods

### `deletePhotos()`

```typescript
    DeleteMedia.deletePhotos(Array<string>);
```

This function asks the android OS to delete a list of photos.

It takes in an array of uris strings. Theses strings must be valid
uris to a local images, such as `file:///storage/emulated/0/DCIM/Camera/photo.jpg`

The function will fail if at least one of the uris is not valid or is not found.

It returns a promise that resolves if all the photos was deleted and rejects otherwise.

When rejected it returns an error object of type `{code: ErrorCodes, message: string}`.

The list of possible error codes is:

* `"ERROR_WRITE_EXTERNAL_STORAGE_PERMISSION_NEEDED"`

  Permission to write to external storage is not granted.

* `"ERROR_URIS_NOT_FOUND"`

  One or more uris are invalid or not found.

* `"ERROR_USER_REJECTED"`

  User rejected the delete operation.

* `"ERROR_URIS_PARAMETER_NULL"`

  List of uris passed to `deletePhotos` is `undefined` or `null`

* `"ERROR_URIS_PARAMETER_INVALID"`

  List of uris passed to `deletePhotos` is not valid, it should be an array of strings.

* `"ERROR_MODULE_NOT_INITIALIZED"`

  The module was not initialized correctly, please follow the steps in [`Library configuration`](#library-configuration)

* `"ERROR_UNEXPECTED"`

  Unexpected error. Report it if encountered in **[`Issues`](https://github.com/helloukey/react-native-mediastore/issues)**.

### `deleteVideos()`

```typescript
    DeleteMedia.deleteVideos(Array<string>);
```

This function asks the android OS to delete a list of videos.

It takes in an array of uris strings. Theses strings must be valid
uris to a local videos, such as `file:///storage/emulated/0/DCIM/Camera/video.mp4`

The function will fail if at least one of the uris is not valid or is not found.

It returns a promise that resolves if all the videos was deleted and rejects otherwise.

When rejected it returns an error object of type `{code: ErrorCodes, message: string}`.

The list of possible error codes is:

* `"ERROR_WRITE_EXTERNAL_STORAGE_PERMISSION_NEEDED"`

  Permission to write to external storage is not granted.

* `"ERROR_URIS_NOT_FOUND"`

  One or more uris are invalid or not found.

* `"ERROR_USER_REJECTED"`

  User rejected the delete operation.

* `"ERROR_URIS_PARAMETER_NULL"`

  List of uris passed to `deleteVideos` is `undefined` or `null`

* `"ERROR_URIS_PARAMETER_INVALID"`

  List of uris passed to `deleteVideos` is not valid, it should be an array of strings.

* `"ERROR_MODULE_NOT_INITIALIZED"`

  The module was not initialized correctly, please follow the steps in [`Library configuration`](#library-configuration)

* `"ERROR_UNEXPECTED"`

  Unexpected error. Report it if encountered in **[`Issues`](https://github.com/helloukey/react-native-mediastore/issues)**.
