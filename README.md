# java_compressor_example
Java way of using compressor 3.0
All You need to do:
1. Copy JavaCompressor.kt to project
2. Add kotlin dependencies to ${PROJECT_DIR}/app/build.gradle
3. Add kotlin dependencies to ${PROJECT_DIR}/build.gradle
4. Start using compressor in your activity.

**1. Copy JavaCompressor.kt to project**
Copy JavaCompressor.kt from this [gist](https://gist.github.com/nareshkatta99/5fbe8a37799d5a48ea9de32af4a69ea9)
 to your package.

**2. Add kotlin dependencies to ${PROJECT_DIR}/app/build.gradle**
Make sure you applied the kotlin-android plugin
```
    apply plugin: 'kotlin-android'
```

You have to add 
    org.jetbrains.kotlin:kotlin-stdlib
    org.jetbrains.kotlinx:kotlinx-coroutines-core
    org.jetbrains.kotlinx:kotlinx-coroutines-android
    androidx.lifecycle:lifecycle-extensions
    androidx.lifecycle:lifecycle-runtime-ktx
libraries to dependencies

```
    implementation "org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version"
    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlin_coroutines_version"
    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:$kotlin_coroutines_version"
    implementation 'androidx.lifecycle:lifecycle-extensions:2.2.0'
    implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.2.0'
```
You have to add id.zelory:compressor any way
```
    implementation 'id.zelory:compressor:3.0.0'
```
**3. Add kotlin dependencies to ${PROJECT_DIR}/build.gradle**
Make sure you add kotlin to your project level gradle file.
add these 2 lines to buildscript
```
    ext.kotlin_version = "1.4.0"
    ext.kotlin_coroutines_version = '1.3.8'
```
and 

```
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
```
to dependencies 

Thats it. That's all you need to do.

**4. Example usage**
Simple
```
            JavaCompressor.compress(this, selectedFile, new Callback(){
                @Override
                public void onComplete(boolean status, @Nullable File file) {
                    //check if compression successful and use file if status is true
                }
            })
```
With constraints:
```
            JavaCompressor.compress(this, actual, new Callback() {
                @Override
                public void onComplete(boolean status, @Nullable File file) {
                    //check if compression successful and use file if status is true
                }
            }, new QualityConstraint(80), new ResolutionConstraint(612, 816), new DestinationConstraint(new File(getCacheDir(), "compressed.jpeg")));
```