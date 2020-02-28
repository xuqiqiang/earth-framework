#include <stdio.h>
#include <stdlib.h>
#include <math.h>
//#include <sys/timeb.h>
#include "platform.h"
#include "parabola.h"

double MAX_DURATION = 10 * 1000;
double MIN_DURATION = 2 * 1000;
double STEP_POW = 1.3;
double STEEP_MULTIPLIER = 1.6;

// 抛物线最大海拔高度
double getSteepFactor(double s) {
    return (STEEP_MULTIPLIER * 10 / pow(s, STEP_POW));
}

double getCountByDistance(double s) {
    double percent = s / 201.0;
    double count = percent * MAX_DURATION * 2;
    double result = count <= MAX_DURATION ? MAX_DURATION : (count <= MIN_DURATION ? MIN_DURATION : count);
    return result / 10;
}

int eq0(double num) {
    if(num >= -0.001 && num <= 0.001) return 1;
    return 0;
}

void fixArcGISAccuracyBug(double point[]) {
    if(eq0(point[3]) == 1 || eq0(fabs(360-point[3])) == 1) point[3] = 0;
    if(eq0(point[4]) == 1 || eq0(fabs(360-point[4])) == 1) point[4] = 0;
    if(eq0(point[5]) == 1 || eq0(fabs(360-point[5])) == 1) point[5] = 0;
}

void fixArcGISAccuracyNumber(double startPoint[], double endPoint[]) {
    if(startPoint[3] >= 180) endPoint[3] = 360;
    if(startPoint[4] >= 180) endPoint[4] = 360;
    if(startPoint[5] >= 180) endPoint[5] = 360;
}

char * parabola(double startPoint[], double endPoint[]) {
    // 解决arcGIS精度问题
    fixArcGISAccuracyBug(startPoint);
    fixArcGISAccuracyBug(endPoint);
    fixArcGISAccuracyNumber(startPoint, endPoint);
    // 海拔单位转换
    double startAltitude = startPoint[2] / (100000);
    double endAltitude = endPoint[2] / (100000);
    // 两点间最大距离
    double s = sqrt(pow(startPoint[0] - endPoint[0], 2) + pow(startPoint[1] - endPoint[1], 2));
    // 最大高度
    double maxH = (-pow(s / 2 - sqrt(pow(s / 2, 2)), 2) + pow(s / 2, 2)) * (STEEP_MULTIPLIER * 10 / pow(s, STEP_POW));
    // 点数
    double count = getCountByDistance(s);
    // 海拔高度因数
    double steepFactor = getSteepFactor(s);
    // 偏移x
    double startX = sqrt(pow(s / 2, 2)) - sqrt(fabs(pow(s / 2, 2) - startAltitude / steepFactor));
    // 结束点x
    double endX = s - (sqrt(pow(s / 2, 2)) - sqrt(fabs(pow(s / 2, 2) - endAltitude / steepFactor)) + startX);

    //printf("parabola:%lf, %lf\n", startX, endX);

    //double** result;
    int i_count = (int) count + 1;

    char (*str)[60];
    str = (char **) malloc(i_count * 60);
    long long length = 0;

    // 点分布结果
    //double (*result)[6];
    //result = (double (*)[6]) malloc(count * sizeof(double) * 6);
    // x = 1 / 2 * a * t * t
    // 加速度
    double a = (2 * s) / (double) pow(count - 1, 2);
    
    //printf("count:%d\n", count);
    
    LOGD("startAltitude:%.16lf\n", startAltitude);
    LOGD("endAltitude:%.16lf\n", endAltitude);
    LOGD("s:%.16lf\n", s);
    LOGD("maxH:%.16lf\n", maxH);
    LOGD("count:%lf\n", count);
    LOGD("steepFactor:%.16lf\n", steepFactor);
    LOGD("startX:%.16lf\n", startX);
    LOGD("endX:%.16lf\n", endX);
    LOGD("a:%.16lf\n", a);
    int i;
    for (i = 0; i < count; i++) {
        // 计算距离
        double x = 0;
        if (i < (count - 1) / 2) {
            // 加加速度
            double r = i == 0 ? 0 : (2 * i / (count - 1)) * a;
            x = r * pow(i, 2);
        } else {
            // 加加速度
            double r = 2 * a - (2 * i / (count - 1)) * a;
            // x = (s) - (a * Math.pow((count - 1 - i), 2));
            x = (s) - (r * pow((count - 1 - i), 2));
        }

        // 相对位置
        double abX = x * endX / s;
        // 比例
        double percent = x / s;
        // 计算海拔
        double h = 0;
        if (startAltitude > maxH) {
            h = startAltitude - (startAltitude - endAltitude) * percent;
        } else {
            h = (-pow((abX + startX) - sqrt(pow(s / 2, 2)), 2) + pow(s / 2, 2)) * steepFactor;
        }
        //double h = (-pow((abX + startX) - sqrt(pow(s / 2, 2)), 2) + pow(s / 2, 2)) * steepFactor;
/*
        result[i][0] = startPoint[0] + (endPoint[0] - startPoint[0]) * percent;
        result[i][1] = startPoint[1] + (endPoint[1] - startPoint[1]) * percent;
        result[i][2] = h * 100000;
        result[i][3] = 0;
        result[i][4] = 0;
        result[i][5] = 0;
        printf("item:%lf, %lf, %lf\n",
            startPoint[0] + (endPoint[0] - startPoint[0]) * percent,
            startPoint[1] + (endPoint[1] - startPoint[1]) * percent,
            h * 100000);
*/

LOGD("percent:%.16lf,%d\n", percent, i);
LOGD("h:%.16lf,%d\n", h, i);

//LOGD("head:%.16lf,%.16lf,%.16lf\n", startPoint[3], endPoint[3], (startPoint[3] + (endPoint[3] - startPoint[3]) * percent));
        //char str[50];
        sprintf(str[i],"[%.6lf,%.6lf,%.2lf,%.3lf,%.3lf,%.3lf],",
            startPoint[0] + (endPoint[0] - startPoint[0]) * percent,
            startPoint[1] + (endPoint[1] - startPoint[1]) * percent,
            h * 100000,
            (startPoint[3] + (endPoint[3] - startPoint[3]) * percent),
            (startPoint[4] + (endPoint[4] - startPoint[4]) * percent),
            (startPoint[5] + (endPoint[5] - startPoint[5]) * percent));
        //printf("item:%s\n", str[i]);

        length += strlen(str[i]);
    }
    printf("length:%d,%d\n", count, length);
    char *result = (char *) malloc(length + 3);
    strcpy(result, "[");
    for (i = 0; i < count; i++) {
        strcat(result, str[i]);
    }

    strcat(result, "]");
    free(str);

    //printf("result:%s\n", result);
    return result;
}

/*
void parabola1(double startPoint[], double endPoint[]) {
    // 两点间最大距离
    double s = sqrt(pow(startPoint[0] - endPoint[0], 2) + pow(startPoint[1] - endPoint[1], 2));
    // 点数
    int count = getCountByDistance(s);
    // 海拔高度因数
    double steepFactor = getSteepFactor(s);

    double startAltitude = startPoint[2] / (100000);

    double endAltitude = endPoint[2] / (100000);

    // 偏移x
    double startX = sqrt(pow(s / 2, 2)) - sqrt(abs(pow(s / 2, 2) - startAltitude / steepFactor));
    // 结束点x
    double endX = s - (sqrt(pow(s / 2, 2)) - sqrt(abs(pow(s / 2, 2) - endAltitude / steepFactor)) + startX);

    char result[100000];
    strcpy(result, "[");

    //printf("count:%d\n", count);
    // 点分布结果
    //double (*result)[6];
    //result = (double (*)[6]) malloc(count * sizeof(double) * 6);
    // x = 1 / 2 * a * t * t
    // 加速度
    double a = (2 * s) / pow(count - 1, 2);
    int i;
    for (i = 0; i < count; i++) {
        // 计算距离
        double x = 0;
        if (i < (count - 1) / 2) {
            // 加加速度
            double r = i == 0 ? 0 : (2 * i / (count - 1)) * a;
            x = r * pow(i, 2);
        } else {
            // 加加速度
            double r = 2 * a - (2 * i / (count - 1)) * a;
            // x = (s) - (a * Math.pow((count - 1 - i), 2));
            x = (s) - (r * pow((count - 1 - i), 2));
        }

        // 相对位置
        double abX = x * endX / s;
        // 比例
        double percent = x / s;
        // 计算海拔
        double h = (-pow((abX + startX) - sqrt(pow(s / 2, 2)), 2) + pow(s / 2, 2)) * steepFactor;

        char str[50];
        sprintf(str,"[%.6lf,%.6lf,%.2lf],",
            startPoint[0] + (endPoint[0] - startPoint[0]) * percent,
            startPoint[1] + (endPoint[1] - startPoint[1]) * percent,
            h * 100000);
        //printf("item:%s\n", str);

        strcat(result, str);

        //length += strlen(str[i]);
    }

    strcat(result, "]");
    //free(str);
    printf("result:%s\n", result);

}
*/
/*
long long getSystemTime() {
    struct timeb t;
    ftime(&t);
    return 1000 * t.time + t.millitm;
}
*/
int main() {

    //time_t t_start, t_end;
    //t_start = time(NULL) ;

    //long long start=getSystemTime();


    //printf("test:%lf\n", getCountByDistance(100));

    double startPoint[6] = {34.454004, 89.9021, 20000, 10, 20, 30};
    double endPoint[6] = {42.644483, -109.084758, 20000, 0, 0, 0};

    char *result = parabola(startPoint, endPoint);

    //t_end = time(NULL) ;
    //printf("time: %.0f s\n", difftime(t_end,t_start)) ;

    //long long end=getSystemTime();
    //printf("time: %lld ms\n", end-start);

    printf("result:%s\n", result);

    return 0;
}

/*
JavaVM *javaVm;

JavaVM* g_jvm = 0;
jclass strClass;


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
jstring Java_com_snailstudio2010_earthframework_utils_Utils_parabola(JNIEnv *env,
                                                            jobject thiz,
                                                            jdoubleArray jstartPoint,
                                                            jdoubleArray jendPoint) {


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

*/