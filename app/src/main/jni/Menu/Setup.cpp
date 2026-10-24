//
// Created by Nik2143 on 11/05/2025.
// Template by Wxrnin
//

#include "Menu/Setup.hpp"
#include <sstream>
#include "../Includes/obfuscate.h"
#include "../Includes/Logger.h"
#include "../Includes/Utils.hpp"
#include "Menu/Menu.hpp"
#include "../Includes/Logo.h"

/* Target game shown in the menu header and in the Settings tab.
 * These are display only: the library to hook is set in Main.cpp (IL2CPP_MODULE).
 */
#define GAME_NAME    "Example Game"
#define GAME_VERSION "1.0.0"
#define GAME_PACKAGE "com.example.game"

void Init(JNIEnv *env, jobject thiz, jobject ctx, jobject title, jobject bottomTitle){
    Menu::setText(env, title, OBFUSCATE("Wxrnin"));
    Menu::setText(env, bottomTitle, OBFUSCATE("Modded by <b><font color=#38BDF8>Wxrnin</font></b>"));

    // Every Menu::showToast is drawn with an animated rainbow, see components/RainbowToast.java
    Menu::showToast(env, ctx, OBFUSCATE("Modded by Wxrnin"), ToastLength::LENGTH_LONG);
}

// { name, version, package } of the target game
jobjectArray GameInfo(JNIEnv *env, jobject thiz) {
    const char *info[] = {OBFUSCATE(GAME_NAME), OBFUSCATE(GAME_VERSION), OBFUSCATE(GAME_PACKAGE)};
    jclass stringClass = env->FindClass(OBFUSCATE("java/lang/String"));
    jobjectArray array = env->NewObjectArray(3, stringClass, nullptr);
    for (int i = 0; i < 3; i++) {
        jstring value = env->NewStringUTF(info[i]);
        env->SetObjectArrayElement(array, i, value);
        env->DeleteLocalRef(value);
    }
    env->DeleteLocalRef(stringClass);
    return array;
}

/* Logo shown on the floating bubble, the header and the toast, as a base64 encoded PNG/JPG/GIF, defined in Includes/Logo.h
 * (a "data:image/...;base64," prefix is fine).
 * It is drawn cropped to a circle inside the accent ring, a square image of about 128x128 looks best.
 * An animated GIF plays in a loop. Keep it small (crop, resize, few colors): it is stored inside the .so.
 * Transparent PNGs work, the bubble background shows through.
 * Leave it empty to draw Theme.BUBBLE_TEXT instead.
 * Use https://www.base64encode.org/ or `base64 -w0 icon.gif` to encode your image.
 */
jstring Icon(JNIEnv *env, jobject thiz) {
    return env->NewStringUTF(LOGO_BASE64);
}

int RegisterMenu(JNIEnv *env) {
    jclass clazz = env->FindClass(OBFUSCATE("com/android/support/Menu"));
    if (!clazz) {
        LOGE(OBFUSCATE("Menu class not found"));
        return JNI_ERR; // Class not found
    }

    static const JNINativeMethod methods[] = {
            {OBFUSCATE("Init")              , OBFUSCATE("(Landroid/content/Context;Landroid/widget/TextView;Landroid/widget/TextView;)V")   , reinterpret_cast<void *>(Init)},
            {OBFUSCATE("Icon")              , OBFUSCATE("()Ljava/lang/String;")                                                              , reinterpret_cast<void *>(Icon)},
            {OBFUSCATE("GameInfo")          , OBFUSCATE("()[Ljava/lang/String;")                                                             , reinterpret_cast<void *>(GameInfo)},
    };

    jint ret = env->RegisterNatives(clazz, methods, sizeof(methods) / sizeof(JNINativeMethod));
    if (ret != JNI_OK) {
        return ret; // Failed to register natives
    }

    LOGI(OBFUSCATE("Menu registered"));
    return JNI_OK;
}
