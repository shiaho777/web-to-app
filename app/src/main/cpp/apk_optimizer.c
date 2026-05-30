#include <jni.h>
#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include <stdint.h>
#include <android/log.h>
#include <sys/stat.h>
#include <unistd.h>
#include <fcntl.h>
#include <zlib.h>
#include <errno.h>

#define TAG "ApkOptimizer"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

#define JNI_FUNC(name) Java_com_webtoapp_core_apkbuilder_NativeApkOptimizer_##name

#define ZIP_LOCAL_MAGIC        0x04034b50
#define ZIP_LOCAL_HEADER_SIZE  30

#define ZIP_CENTRAL_MAGIC      0x02014b50
#define ZIP_CENTRAL_HEADER_SIZE 46

#define ZIP_EOCD_MAGIC         0x06054b50
#define ZIP_EOCD_MIN_SIZE      22

#define ZIP_METHOD_STORED      0
#define ZIP_METHOD_DEFLATED    8

#define DEX_MAGIC_SIZE         8

static inline uint16_t read_u16_le(const uint8_t *p) {
    return (uint16_t)p[0] | ((uint16_t)p[1] << 8);
}

static inline uint32_t read_u32_le(const uint8_t *p) {
    return (uint32_t)p[0] | ((uint32_t)p[1] << 8) |
           ((uint32_t)p[2] << 16) | ((uint32_t)p[3] << 24);
}

static inline void write_u16_le(uint8_t *p, uint16_t v) {
    p[0] = (uint8_t)(v & 0xFF);
    p[1] = (uint8_t)((v >> 8) & 0xFF);
}

static inline void write_u32_le(uint8_t *p, uint32_t v) {
    p[0] = (uint8_t)(v & 0xFF);
    p[1] = (uint8_t)((v >> 8) & 0xFF);
    p[2] = (uint8_t)((v >> 16) & 0xFF);
    p[3] = (uint8_t)((v >> 24) & 0xFF);
}

typedef struct {
    int64_t original_size;
    int64_t optimized_size;
    int     entries_total;
    int     entries_stripped;
    int     entries_recompressed;
    int     entries_deduplicated;
    int     dex_files_kept;
    int     dex_files_stripped;
    int64_t native_lib_savings;
    int64_t dex_savings;
    int64_t resource_savings;
    int64_t recompression_savings;
    int64_t dedup_savings;
    int64_t unused_res_savings;
} optimize_stats_t;

static int is_framework_resource(const char *filename) {

    if (strncmp(filename, "abc_", 4) == 0) return 1;

    if (strncmp(filename, "mtrl_", 5) == 0) return 1;

    if (strncmp(filename, "design_", 7) == 0) return 1;

    if (strncmp(filename, "m3_", 3) == 0) return 1;

    if (strncmp(filename, "preference_", 11) == 0) return 1;

    if (strncmp(filename, "notification_", 13) == 0) return 1;

    if (strncmp(filename, "tooltip_", 8) == 0) return 1;
    if (strncmp(filename, "custom_dialog", 13) == 0) return 1;

    if (strncmp(filename, "compat_", 7) == 0) return 1;
    if (strncmp(filename, "support_", 8) == 0) return 1;

    return 0;
}

static const char *get_res_filename(const char *path) {
    const char *last_slash = strrchr(path, '/');
    return last_slash ? last_slash + 1 : path;
}
