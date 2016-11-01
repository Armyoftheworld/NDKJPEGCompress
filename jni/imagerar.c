#include "com_army_libjpeg_JpegUtils.h"
#include <string.h>
#include <android/bitmap.h>
#include <jni.h>
#include <stdio.h>
#include <setjmp.h>
#include <math.h>
#include <time.h>
#include "jpeg/imagerar.c"
#include <android/log.h>
typedef uint8_t BYTE;
#define TAG "jpeg"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,TAG ,__VA_ARGS__)
JNIEXPORT jstring JNICALL Java_com_army_libjpeg_JpegUtils_compressBitmap(
		JNIEnv *env, jclass jclass, jobject bitmap, jint w, jint h,
		jint quality, jbyteArray outputfilename, jboolean optimize) {
	AndroidBitmapInfo infocolor;
	int ret;
	BYTE * pixelColor;
	BYTE * data;
	BYTE * tmpData;
	char * filename = jstrinTostring(env,outputfilename);
	if ((ret = AndroidBitmap_getInfo(env, bitmap, &infocolor)) < 0) {
		LOGE("解析错误");
		return (*env)->NewStringUTF(env, "0");
	}
	if ((ret = AndroidBitmap_lockPixels(env, bitmap, &pixelColor)) < 0) {
		LOGE("加载失败");
	}
	BYTE r, g, b;
	int color, i, j;
	data = malloc(w * h * 3);
	tmpData = data;
	for (i = 0; i < h; i++) {
		for (j = 0; j < w; j++) {
			color = *((int*) pixelColor);
			r = ((color & 0x00FF0000) >> 16);
			g = ((color & 0x0000FF00) >> 8);
			b = ((color & 0x000000FF));
			*data = b;
			*(data + 1) = g;
			*(data + 2) = r;
			data += 3;
			pixelColor += 4;
		}
	}
	AndroidBitmap_unlockPixels(env, bitmap);
	int resultCode = generateJPEG(tmpData, w, h, quality, filename, optimize);
	free(tmpData);
	if (resultCode == 0) {
		jstring result = (*env)->NewStringUTF(env, "0");
		return result;
	}
	return (*env)->NewStringUTF(env, "1");
}
