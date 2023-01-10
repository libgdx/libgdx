# lwjgl3-angle

This extension adds angle support to the lwjgl3 backend.

## Usage

To use it, add the `gdx-lwjgl3-angle` extension to your gdx-lwjgl3 desktop project.
Then call `config.setOpenGLEmulation(GLEmulation.ANGLE_GLES20, 0, 0)` on your `Lwjgl3ApplicationConfiguration` instance
before creating your `Lwjgl3Application`. 

Check out [#6672](https://github.com/libgdx/libgdx/pull/6672) for more
information.

### OSX App Bundles using jpackage

Due to the way the libraries are loaded, special care is required when
distributing your app as OSX App Bundle:

1. Grab the `libEGL.dylib` and `libGLESv2.dylib` files from the `macosxarm64` and `macosx64` folder (inside the jar, or
   download from [here](https://github.com/libgdx/gdx-angle-natives))
2. Merge them into a single universal library:
   ```
   lipo macosxarm64/libEGL.dylib macosx64/libEGL.dylib -output libEGL.dylib -create
   lipo macosxarm64/libGLESv2.dylib macosx64/libGLESv2.dylib -output libGLESv2.dylib -create
    ```
3. Place the two libraries into your app bundle: `your-app.app/Contents/Frameworks/`
4. Add the folder to the loader rpath of the launcher so the dynamic linker can find the libraries
   ```
   install_name_tool -add_rpath @executable_path/../Frameworks/ your-app./Contents/MacOS/launcher-binary
   ```
5. Sign, notarize and stamp your app as usual


See [#7058](https://github.com/libgdx/libgdx/issues/7058) for more details.