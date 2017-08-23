ifneq ($(filter falcon peregrine thea,$(TARGET_DEVICE)),)
LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_C_INCLUDES := \
    system/media/camera/include

LOCAL_SRC_FILES := \
    CameraWrapper.cpp

LOCAL_STATIC_LIBRARIES := libbase libarect
LOCAL_SHARED_LIBRARIES := \
    libhardware liblog libcamera_client libutils libdl \
    android.hidl.token@1.0-utils \
    android.hardware.graphics.bufferqueue@1.0

LOCAL_MODULE_PATH := $(TARGET_OUT_SHARED_LIBRARIES)/hw
# workaround for hardware/qcom/camera already defining camera.board
LOCAL_MODULE := camera-wrapper.msm8974


LOCAL_MODULE_TAGS := optional

include $(BUILD_SHARED_LIBRARY)
#include $(BUILD_HEAPTRACKED_SHARED_LIBRARY)

$(LOCAL_INSTALLED_MODULE): $(LOCAL_BUILT_MODULE) | $(ACP)
	@echo "Install (overridden): $@"
	$(hide) $(ACP) -fp $< $(dir $@)/camera.msm8226$(TARGET_SHLIB_SUFFIX)

endif
