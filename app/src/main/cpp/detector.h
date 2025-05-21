#ifndef DETECTOR_H
#define DETECTOR_H

#define SECURITY_CLEAN        0x0
#define SECURITY_VPN         0x1
#define SECURITY_TAMPERED    0x2
#define SECURITY_ART_METHOD  0x3
#define SECURITY_QUICK_BRIDGE 0x4
#define SECURITY_MEMORY_PERM  0x5
#define SECURITY_INLINE_HOOK  0x6
#define SECURITY_INTERPRETER  0x7

#include <jni.h>
#include "dev/Qwanwin/implement/antivpn.h"

#ifdef __cplusplus
extern "C" {
#endif


JNIEXPORT jint JNICALL
Java_com_dev_detector_Qwanwin_implement_DetectorVPN_nativeCheckVPN(JNIEnv* env, jobject thiz);


JNIEXPORT jint JNICALL
Java_com_dev_detector_Qwanwin_implement_PineDetector_nativeCheck(JNIEnv* env, jobject thiz);

#ifdef __cplusplus
}
#endif

#endif 