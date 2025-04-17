#include <jni.h>
#include <android/log.h>
#include <android/bitmap.h>


#define  LOG_TAG    "imageModification"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)


extern "C"
JNIEXPORT void JNICALL
Java_com_example_drawingappall_jni_ImageModification_blur(JNIEnv *env, jobject thiz, jobject bitmap) {
    LOGI("Never");
    // TODO: implement blur()
//    AndroidBitmapInfo info;
//    void *pixels;
//    AndroidBitmap_getInfo(env, bitmap, &info);
//    AndroidBitmap_lockPixels(env, bitmap, &pixels);
//
//
//
//    AndroidBitmap_unlockPixels(env, bitmap);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_drawingappall_jni_ImageModification_sharpen(JNIEnv *env, jobject thiz, jobject bitmap) {
    // TODO: implement sharpen()
    LOGI("naa");
//    AndroidBitmapInfo info;
//    void *pixels;
//    AndroidBitmap_getInfo(env, bitmap, &info);
//    AndroidBitmap_lockPixels(env, bitmap, &pixels);
//
//
//
//    AndroidBitmap_unlockPixels(env, bitmap);
}
