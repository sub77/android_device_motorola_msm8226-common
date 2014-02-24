# Copyright (C) 2012 The Android Open Source Project
# Copyright (C) 2014 The CyanogenMod Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

"""Custom OTA commands for falcon devices"""

def FullOTA_InstallEnd(info):
	info.script.AppendExtra('ifelse(is_substring("0x5", getprop("ro.boot.radio")), run_program("/sbin/sh", "-c", "busybox cp -R /system/xt1033/* /system/"));')
	info.script.AppendExtra('delete_recursive("/system/xt1033");')
	info.script.AppendExtra('package_extract_file("system/bin/gpecheck.sh", "/tmp/gpecheck.sh");')
	info.script.AppendExtra('set_perm(0, 0, 0777, "/tmp/gpecheck.sh");')
	info.script.AppendExtra('run_program("/tmp/gpecheck.sh");')
	info.script.AppendExtra('delete("/system/bin/gpecheck.sh");')
	info.script.AppendExtra('ifelse(is_substring("true", getprop("ro.gpe.device")), package_extract_file("system/gpe-boot.img", "/dev/block/platform/msm_sdcc.1/by-name/boot"));')
	info.script.AppendExtra('delete("/system/gpe-boot.img");')