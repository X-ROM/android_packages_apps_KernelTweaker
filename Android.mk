LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_STATIC_JAVA_LIBRARIES := android-support-v4 roottools renderscript google

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_PRIVILEGED_MODULE := true
LOCAL_PACKAGE_NAME := KernelTweaker


include $(BUILD_PACKAGE)
##################################################
include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := roottools:RootTools.jar renderscript:renderscript-v8.jar google:libGoogleAnalyticsServices.jar

include $(BUILD_MULTI_PREBUILT)

# Use the folloing include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))
