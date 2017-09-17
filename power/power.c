/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
#include <errno.h>
#include <string.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <fcntl.h>
#include <dlfcn.h>
#include <cutils/uevent.h>
#include <errno.h>
#include <sys/poll.h>
#include <pthread.h>
#include <linux/netlink.h>
#include <stdlib.h>
#include <stdbool.h>

#define LOG_TAG "PowerHAL"
#include <utils/Log.h>

#include <hardware/hardware.h>
#include <hardware/power.h>

#define STATE_ON "state=1"
#define STATE_OFF "state=0"
#define STATE_HDR_ON "state=2"
#define STATE_HDR_OFF "state=3"

#define MAX_LENGTH         50

#define debug 0

#define UEVENT_MSG_LEN 1024
#define TOTAL_CPUS 4
#define RETRY_TIME_CHANGING_FREQ 20
#define SLEEP_USEC_BETWN_RETRY 200
#define LOW_POWER_MAX_FREQ "729600"
#define LOW_POWER_MIN_FREQ "300000"
#define NORMAL_MAX_FREQ "1190400"
#define UEVENT_STRING "online@/devices/system/cpu/"

static int client_sockfd;
static struct sockaddr_un client_addr;
static int last_state = -1;

static struct pollfd pfd;
static char *cpu_path_min[] = {
    "/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq",
    "/sys/devices/system/cpu/cpu1/cpufreq/scaling_min_freq",
    "/sys/devices/system/cpu/cpu2/cpufreq/scaling_min_freq",
    "/sys/devices/system/cpu/cpu3/cpufreq/scaling_min_freq",
};
static char *cpu_path_max[] = {
    "/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq",
    "/sys/devices/system/cpu/cpu1/cpufreq/scaling_max_freq",
    "/sys/devices/system/cpu/cpu2/cpufreq/scaling_max_freq",
    "/sys/devices/system/cpu/cpu3/cpufreq/scaling_max_freq",
};
static bool freq_set[TOTAL_CPUS];
static bool low_power_mode = false;
static pthread_mutex_t low_power_mode_lock = PTHREAD_MUTEX_INITIALIZER;

bool display_boost = false;

int sysfs_write(char *path, char *s)
{
    char buf[80];
    int len;
    int ret = 0;
    int fd = open(path, O_WRONLY);

    if (fd < 0) {
        strerror_r(errno, buf, sizeof(buf));
        ALOGE("Error opening %s: %s\n", path, buf);
        return -1 ;
    }

    len = write(fd, s, strlen(s));
    if (len < 0) {
        strerror_r(errno, buf, sizeof(buf));
        ALOGE("Error writing to %s: %s\n", path, buf);

        ret = -1;
    }

    close(fd);

    return ret;
}

void set_feature(__attribute__((unused))struct power_module *module, feature_t feature, int state)
{
#ifdef TAP_TO_WAKE_NODE
    if (feature == POWER_FEATURE_DOUBLE_TAP_TO_WAKE) {
            ALOGI("Double tap to wake is %s.", state ? "enabled" : "disabled");
            sysfs_write(TAP_TO_WAKE_NODE, state ? "1" : "0");
        return;
    }
#endif
}

static void power_set_interactive(__attribute__((unused)) struct power_module *module, int on)
{
    if (last_state == -1) {
        last_state = on;
    } else {
        if (last_state == on)
            return;
        else
            last_state = on;
    }

    ALOGV("%s %s", __func__, (on ? "ON" : "OFF"));
#if (debug)
	ALOGE("%s TODO", __func__);
#endif

}

static void process_video_encode_hint(void *metadata)
{
#if (debug)
	ALOGE("%s TODO", __func__);
#endif
}

static void power_hint( __attribute__((unused)) struct power_module *module,
                      power_hint_t hint, __attribute__((unused)) void *data)
{
    int cpu, ret;

    switch (hint) {
        case POWER_HINT_INTERACTION:
#if (debug)
            ALOGV("POWER_HINT_INTERACTION");
			ALOGE("%s TODO POWER_HINT_INTERACTION ", __func__);
#endif
            break;
        case POWER_HINT_VIDEO_ENCODE:
            process_video_encode_hint(data);
            break;

        case POWER_HINT_LOW_POWER:
             pthread_mutex_lock(&low_power_mode_lock);
             if (data) {
                 low_power_mode = true;
                 for (cpu = 0; cpu < TOTAL_CPUS; cpu++) {
                     sysfs_write(cpu_path_min[cpu], LOW_POWER_MIN_FREQ);
                     ret = sysfs_write(cpu_path_max[cpu], LOW_POWER_MAX_FREQ);
                     if (!ret) {
                         freq_set[cpu] = true;
                     }
                 }
                 // reduces the refresh rate
                 system("service call SurfaceFlinger 1016");
             } else {
                 low_power_mode = false;
                 for (cpu = 0; cpu < TOTAL_CPUS; cpu++) {
                     ret = sysfs_write(cpu_path_max[cpu], NORMAL_MAX_FREQ);
                     if (!ret) {
                         freq_set[cpu] = false;
                     }
                 }
                 // restores the refresh rate
                 system("service call SurfaceFlinger 1017");
             }
             pthread_mutex_unlock(&low_power_mode_lock);
             break;
        case POWER_HINT_VSYNC:
#if (debug)
             ALOGE("%s TODO: POWER_HINT_VSYNC", __func__);
#endif
            break;
        case POWER_HINT_DISABLE_TOUCH:
#if (debug)
             ALOGE("%s TODO: POWER_HINT_DISABLE_TOUCH", __func__);
#endif
             break;
        case POWER_HINT_LAUNCH:
#if (debug)
             ALOGE("%s TODO: POWER_HINT_LAUNCH", __func__);
#endif
             break;
        default:
#if (debug)
			 ALOGE("%s TODO: hint id: %i", __func__, hint);
#endif
             break;
    }
}

static void power_init(__attribute__((unused)) struct power_module *module)
{
    ALOGI("%s", __func__);

    int fd;
    char buf[10] = {0};

    fd = open("/sys/devices/soc0/soc_id", O_RDONLY);
    if (fd >= 0) {
        if (read(fd, buf, sizeof(buf) - 1) == -1) {
            ALOGW("Unable to read soc_id");
        } else {
            int soc_id = atoi(buf);
            if (soc_id == 194 || (soc_id >= 208 && soc_id <= 218) || soc_id == 178) {
                display_boost = true;
				ALOGI("%s: Enabling display boost", __func__);
            } else {
                ALOGI("%s: Soc %d not in list, disabling display boost", __func__, soc_id);
            }
        }
        close(fd);
    }
}


static struct hw_module_methods_t power_module_methods = {
    .open = NULL,
};

struct power_module HAL_MODULE_INFO_SYM = {
    .common = {
        .tag = HARDWARE_MODULE_TAG,
        .module_api_version = POWER_MODULE_API_VERSION_0_2,
        .hal_api_version = HARDWARE_HAL_API_VERSION,
        .id = POWER_HARDWARE_MODULE_ID,
        .name = "Sony 8974 Power HAL",
        .author = "The Android Open Source Project",
        .methods = &power_module_methods,
    },

    .init = power_init,
    .setInteractive = power_set_interactive,
    .powerHint = power_hint,
    .setFeature = set_feature,
};

