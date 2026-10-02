# ObjectLens

An Android app (Kotlin + Jetpack Compose) that identifies what is in a photo using **MobileNet V2**
(quantized, TensorFlow Lite, ImageNet 1000 classes) fully on-device.

## Features
- Pick an image from your gallery and get the best match plus the top 5 predictions with confidence bars
- Category badges (Dog, Cat, Vehicle, Animal, Object)
- History of every scan (thumbnail, label, confidence, time), with detail view, delete and clear-all
- Light/dark theme, Material 3 UI

## Build
Open the folder in Android Studio (Koala or newer, JDK 17) and run the `app` configuration
(minSdk 28). Android Studio will generate the Gradle wrapper on first sync.

## Notes
- MobileNet V2 is an image **classifier**: it names the dominant object in the photo and does not draw
  bounding boxes or find several objects at once. Boxes would need a detector such as SSD MobileNet.
- Model: `mobilenet_v2_1.0_224_quant.tflite` from the TensorFlow Lite hosted models, plus ImageNet labels in `app/src/main/assets`.
