# inherit from the proprietary version
-include vendor/motorola/msm8226-common/BoardConfigVendor.mk

COMMON_PATH := device/motorola/msm8226-common

TARGET_NO_BOOTLOADER := true

BOARD_VENDOR := motorola-qcom

# ANT+
BOARD_ANT_WIRELESS_DEVICE := "qualcomm-smd"

# Architecture
TARGET_CPU_VARIANT := krait
TARGET_CPU_ABI := armeabi-v7a
TARGET_CPU_ABI2 := armeabi
TARGET_ARCH := arm
TARGET_ARCH_VARIANT := armv7-a-neon
TARGET_CPU_SMP := true

BOARD_DTBTOOL_ARGS := --force-v2
BOARD_KERNEL_CMDLINE := console=ttyHSL0,115200,n8 androidboot.hardware=qcom user_debug=31 msm_rtb.filter=0x37 ehci-hcd.park=3 androidboot.bootdevice=msm_sdcc.1
BOARD_KERNEL_CMDLINE += androidboot.selinux=permissive
BOARD_KERNEL_BASE := 0x00000000
BOARD_KERNEL_PAGESIZE := 2048
BOARD_KERNEL_SEPARATED_DT := true
BOARD_MKBOOTIMG_ARGS := --ramdisk_offset 0x01000000 --tags_offset 0x00000100
TARGET_KERNEL_CROSS_COMPILE_PREFIX := arm-linux-androideabi-

# Architecture / Platform
TARGET_BOARD_PLATFORM := msm8226
TARGET_BOARD_PLATFORM_GPU := qcom-adreno305

# Audio/media
TARGET_QCOM_AUDIO_VARIANT := caf-msm8974
TARGET_QCOM_DISPLAY_VARIANT := caf-msm8974
TARGET_QCOM_MEDIA_VARIANT := caf-msm8974
TARGET_ENABLE_QC_AV_ENHANCEMENTS := true

# Audio
AUDIO_FEATURE_ENABLED_MULTI_VOICE_SESSIONS := true
AUDIO_FEATURE_ENABLED_NEW_SAMPLE_RATE := true
BOARD_USES_ALSA_AUDIO := true

# Bluetooth
BOARD_HAVE_BLUETOOTH := true
BOARD_HAVE_BLUETOOTH_QCOM := true
BLUETOOTH_HCI_USE_MCT := true

# Camera
TARGET_HAS_LEGACY_CAMERA_HAL1 := true
USE_DEVICE_SPECIFIC_CAMERA := true

# Charger
BOARD_CHARGING_MODE_BOOTING_LPM := /sys/mmi_lpm/lpm_mode
BACKLIGHT_PATH := /sys/class/leds/lcd-backlight/brightness
BOARD_CHARGER_ENABLE_SUSPEND := true
BOARD_NO_CHARGER_LED := true

# Crypto
TARGET_HW_DISK_ENCRYPTION := true
TARGET_CRYPTFS_HW_PATH := device/motorola/msm8226-common/cryptfs_hw

# Disable secure discard because it's SLOW
BOARD_SUPPRESS_SECURE_ERASE := true

# Display
USE_OPENGL_RENDERER := true
TARGET_USES_ION := true
TARGET_USES_C2D_COMPOSITION := true
OVERRIDE_RS_DRIVER := libRSDriver_adreno.so
MAX_EGL_CACHE_KEY_SIZE := 12*1024
MAX_EGL_CACHE_SIZE := 2048*1024

# FM Radio
BOARD_HAVE_FM_RADIO := true
BOARD_DISABLE_FMRADIO_LIBJNI := true
BOARD_HAVE_QCOM_FM := true
TARGET_QCOM_NO_FM_FIRMWARE := true

# Global
TARGET_SPECIFIC_HEADER_PATH := $(COMMON_PATH)/include
BOARD_USES_QCOM_HARDWARE := true
TARGET_USES_QCOM_BSP := true

# Keymaster
TARGET_KEYMASTER_WAIT_FOR_QSEE := true

# Lights
TARGET_PROVIDES_LIBLIGHT := true

# Power
TARGET_PROVIDES_POWERHAL := true

# Time Daemon
BOARD_USES_QC_TIME_SERVICES := true

# Wifi
BOARD_HAS_QCOM_WLAN := true
BOARD_HAS_QCOM_WLAN_SDK := true
BOARD_WLAN_DEVICE := qcwcn
BOARD_HOSTAPD_DRIVER := NL80211
BOARD_HOSTAPD_PRIVATE_LIB := lib_driver_cmd_$(BOARD_WLAN_DEVICE)
BOARD_WPA_SUPPLICANT_DRIVER := NL80211
BOARD_WPA_SUPPLICANT_PRIVATE_LIB := lib_driver_cmd_$(BOARD_WLAN_DEVICE)
TARGET_USES_WCNSS_CTRL := true
WPA_SUPPLICANT_VERSION := VER_0_8_X
WIFI_DRIVER_FW_PATH_STA   := "sta"
WIFI_DRIVER_FW_PATH_AP    := "ap"
TARGET_WLAN_VARIANT := caf

# Selinux
include device/qcom/sepolicy/sepolicy.mk

#BOARD_SEPOLICY_DIRS += \
    $(COMMON_PATH)/sepolicy

# Recovery:Start
TW_THEME := portrait_hdpi
# Use this flag if the board has a ext4 partition larger than 2gb
BOARD_HAS_LARGE_FILESYSTEM := true
BOARD_HAS_NO_REAL_SDCARD := true
TARGET_USERIMAGES_USE_EXT4 := true
TARGET_USERIMAGES_USE_F2FS := true

# Properties
TARGET_SYSTEM_PROP += $(COMMON_PATH)/system.prop

DEVICE_MANIFEST_FILE := device/motorola/msm8226-common/manifest.xml
