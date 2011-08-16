LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE    := mixed_sample
LOCAL_SRC_FILES := libmixed_sample.so
include $(PREBUILT_SHARED_LIBRARY)