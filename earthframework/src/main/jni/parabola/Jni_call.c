#include <stdio.h>
#include <stdlib.h>
#include "platform.h"
#include "parabola.h"

JavaVM *javaVm;

JavaVM* g_jvm = 0;
jclass strClass;

/*
// 由于jvm和c++对中文的编码不一样，因此需要转码。gb2312转换成utf8/16
jstring charTojstring(JNIEnv* env, const char* str) {
    jstring rtn = 0;
    int slen = strlen(str);
    unsigned short * buffer = 0;
    if (slen == 0)
        rtn = (*env)->NewStringUTF(env, str);
    else {
        int length = MultiByteToWideChar( CP_ACP, 0, (LPCSTR) str, slen, NULL, 0);
        buffer = (unsigned short *) malloc(length * 2 + 1);
        if (MultiByteToWideChar( CP_ACP, 0, (LPCSTR) str, slen, (LPWSTR) buffer, length) > 0)
            rtn = (*env)->NewString(env, (jchar*) buffer, length);
        // 释放内存
        free(buffer);
    }
    return rtn;
}
*/

jstring charTojstring(JNIEnv* env, const char* pat) {

    //jclass tmp = (*env)->FindClass(env, XMPP_JAVA_PATH);
	//jclass strClass = (jclass)(*env)->NewGlobalRef(env, tmp);
LOGD("5");
    //定义java String类 strClass
    jclass strClass = (*env)->FindClass(env, "Ljava/lang/String;");
    LOGD("5.1");
    //获取String(byte[],String)的构造器,用于将本地byte[]数组转换为一个新String
    jmethodID ctorID = (*env)->GetMethodID(env, strClass, "<init>", "([BLjava/lang/String;)V");
    LOGD("6");
    //建立byte数组
    jbyteArray bytes = (*env)->NewByteArray(env, strlen(pat));
    LOGD("7");
    //将char* 转换为byte数组
    (*env)->SetByteArrayRegion(env, bytes, 0, strlen(pat), (jbyte*) pat);
    LOGD("8");
    // 设置String, 保存语言类型,用于byte数组转换至String时的参数
    jstring encoding = (*env)->NewStringUTF(env, "GB2312");
    LOGD("9");
    //将byte数组转换为java String,并输出
    return (jstring) (*env)->NewObject(env, strClass, ctorID, bytes, encoding);
}

//JNIEXPORT jstring JNICALL
jstring Java_com_snailstudio2010_earthframework_utils_EarthUtils_parabola(JNIEnv *env,
                                                            jobject thiz,
                                                            jdoubleArray jstartPoint,
                                                            jdoubleArray jendPoint) {

             //LOGD("0");
 //jstring str = (*env)->NewStringUTF(env, "test123");
/*
(*env)->GetJavaVM(env, &g_jvm);
JNIEnv *env_now = NULL;
	if (g_jvm == NULL) {
		return NULL;
	}
	if ((*g_jvm)->AttachCurrentThread(g_jvm, &env_now, 0) != JNI_OK) {
		return NULL;
	}
	if (env_now == NULL) {
		LOGD("env_now NULL");
		return NULL;
	}


jclass tmp = (*env_now)->FindClass(env, "Ljava/lang/String;");
strClass = (jclass)(*env_now)->NewGlobalRef(env, tmp);
//(*env)->GetJavaVM(env, &javaVm);
*/



LOGD("1");
    jboolean b;
    double* startPoint= (double*) (*env)->GetPrimitiveArrayCritical(env, jstartPoint, &b);
    //jsize size= (*env)->GetArrayLength(env, jstartPoint);
LOGD("2");
    jboolean bendPoint;
    double* endPoint= (double*) (*env)->GetPrimitiveArrayCritical(env, jendPoint, &bendPoint);
    //jsize sizeEndPoint= (*env)->GetArrayLength(env, jendPoint);
LOGD("3");
    char *result = parabola(startPoint, endPoint);
    //LOGD("4:%s", result);

(*env)->ReleasePrimitiveArrayCritical(env, jstartPoint, startPoint, 0);
(*env)->ReleasePrimitiveArrayCritical(env, jendPoint, endPoint, 0);


    LOGD("4");

    jstring str = (*env)->NewStringUTF(env, result);
    free(result);
    //return charTojstring(env, result);
    return str;//(*env)->NewStringUTF(env, result);
}