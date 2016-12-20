LOCAL_PATH := $(call my-dir)

## Overload bootimg generation: Same as the original
$(INSTALLED_BOOTIMAGE_TARGET): $(MKBOOTIMG) $(INTERNAL_BOOTIMAGE_FILES) $(PRODUCT_OUT)/utilities/busybox
	$(call pretty,"Target boot image: $@")
	@echo -e ${CL_CYN}"----- Copying static busybox to ramdisk ------"${CL_RST}
	$(hide) mkdir -p $(PRODUCT_OUT)/root/sbin/static
	$(hide) cp $(PRODUCT_OUT)/utilities/busybox $(PRODUCT_OUT)/root/sbin/static/busybox
	@echo -e ${CL_CYN}"----- Making boot ramdisk ------"${CL_RST}
	$(hide) rm -f $(INSTALLED_RAMDISK_TARGET)
	$(hide) $(MKBOOTFS) $(TARGET_ROOT_OUT) | $(MINIGZIP) > $(INSTALLED_RAMDISK_TARGET)
	@echo -e ${CL_CYN}"----- Making boot image ------"${CL_RST}
	$(hide) $(MKBOOTIMG) $(INTERNAL_BOOTIMAGE_ARGS) $(BOARD_MKBOOTIMG_ARGS) --output $@
	$(hide) $(call assert-max-image-size,$@,$(BOARD_BOOTIMAGE_PARTITION_SIZE),raw)
	@echo -e ${CL_CYN}"Made boot image: $@"${CL_RST}

## Overload recoveryimg generation: Same as the original
$(INSTALLED_RECOVERYIMAGE_TARGET): $(MKBOOTIMG) \
		$(recovery_uncompressed_ramdisk) \
		$(recovery_kernel) \
		$(MINIGZIP)
	@echo -e ${CL_CYN}"----- Making recovery ramdisk ------"${CL_RST}
	$(hide) rm -f $(recovery_uncompressed_ramdisk)
	$(MKBOOTFS) $(TARGET_RECOVERY_ROOT_OUT) > $(recovery_uncompressed_ramdisk)
	$(MINIGZIP) < $(recovery_uncompressed_ramdisk) > $(recovery_ramdisk)
	@echo -e ${CL_CYN}"----- Making recovery image ------"${CL_RST}
	$(hide) $(MKBOOTIMG) $(INTERNAL_RECOVERYIMAGE_ARGS) $(BOARD_MKBOOTIMG_ARGS) --output $@
	$(hide) $(call assert-max-image-size,$@,$(BOARD_RECOVERYIMAGE_PARTITION_SIZE),raw)
	@echo -e ${CL_CYN}"Made recovery image: $@"${CL_RST}
