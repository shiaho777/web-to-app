#include "crypto_engine.h"
#include <android/log.h>
#include <unistd.h>
#include <sys/ptrace.h>
#include <sys/stat.h>
#include <fstream>
#include <cstring>
#include <dirent.h>
#include <dlfcn.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <fcntl.h>
#include <sys/inotify.h>
#include <thread>
#include <atomic>

#define LOG_TAG "AntiDebug"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)

static std::atomic<bool> g_strictMode(false);

void AntiDebug::setStrictMode(bool enabled) {
    g_strictMode.store(enabled);
}

bool AntiDebug::isDebuggerAttached() {

    std::ifstream status("/proc/self/status");
    if (status.is_open()) {
        std::string line;
        while (std::getline(status, line)) {
            if (line.find("TracerPid:") != std::string::npos) {
                int tracer_pid = 0;
                sscanf(line.c_str(), "TracerPid: %d", &tracer_pid);
                if (tracer_pid != 0) {
                    LOGW("TracerPid detected: %d", tracer_pid);
                    return true;
                }
                break;
            }
        }
    }

    return false;
}

bool AntiDebug::isTracerAttached() {
    char path[256];
    snprintf(path, sizeof(path), "/proc/%d/status", getpid());

    std::ifstream status(path);
    if (!status.is_open()) return false;

    std::string line;
    while (std::getline(status, line)) {
        if (line.find("TracerPid:") != std::string::npos) {
            int tracer_pid = 0;
            sscanf(line.c_str(), "TracerPid: %d", &tracer_pid);
            return tracer_pid != 0;
        }
    }

    return false;
}

bool AntiDebug::detectFrida() {

    const int frida_ports[] = {27042, 27043, 27044, 27045, 0};

    for (int i = 0; frida_ports[i] != 0; i++) {
        int sock = socket(AF_INET, SOCK_STREAM, 0);
        if (sock < 0) continue;

        struct sockaddr_in addr;
        addr.sin_family = AF_INET;
        addr.sin_port = htons(frida_ports[i]);
        inet_pton(AF_INET, "127.0.0.1", &addr.sin_addr);

        int flags = fcntl(sock, F_GETFL, 0);
        fcntl(sock, F_SETFL, flags | O_NONBLOCK);

        struct timeval timeout;
        timeout.tv_sec = 0;
        timeout.tv_usec = 50000;
        setsockopt(sock, SOL_SOCKET, SO_RCVTIMEO, &timeout, sizeof(timeout));
        setsockopt(sock, SOL_SOCKET, SO_SNDTIMEO, &timeout, sizeof(timeout));

        int result = connect(sock, (struct sockaddr*)&addr, sizeof(addr));
        close(sock);

        if (result == 0) {
            LOGW("Frida port %d detected", frida_ports[i]);
            return true;
        }
    }

    std::ifstream maps("/proc/self/maps");
    if (maps.is_open()) {
        std::string line;
        while (std::getline(maps, line)) {

            if (line.find("frida") != std::string::npos ||
                line.find("gadget") != std::string::npos ||
                line.find("agent") != std::string::npos && line.find(".so") != std::string::npos) {
                LOGW("Frida library detected in maps: %s", line.c_str());
                return true;
            }
        }
    }

    DIR* dir = opendir("/proc");
    if (dir) {
        struct dirent* entry;
        while ((entry = readdir(dir)) != nullptr) {
            if (entry->d_type == DT_DIR) {
                char cmdline_path[256];
                snprintf(cmdline_path, sizeof(cmdline_path), "/proc/%s/cmdline", entry->d_name);

                std::ifstream cmdline(cmdline_path);
                if (cmdline.is_open()) {
                    std::string cmd;
                    std::getline(cmdline, cmd);
                    if (cmd.find("frida") != std::string::npos ||
                        cmd.find("gum-js-loop") != std::string::npos) {
                        closedir(dir);
                        LOGW("Frida process detected: %s", cmd.c_str());
                        return true;
                    }
                }
            }
        }
        closedir(dir);
    }

    const char* frida_files[] = {
        "/data/local/tmp/frida-server",
        "/data/local/tmp/re.frida.server",
        "/data/local/tmp/frida-agent.so",
        nullptr
    };

    for (int i = 0; frida_files[i] != nullptr; i++) {
        struct stat st;
        if (stat(frida_files[i], &st) == 0) {
            LOGW("Frida file detected: %s", frida_files[i]);
            return true;
        }
    }

    return false;
}

bool AntiDebug::detectXposed() {

    const char* xposed_paths[] = {
        "/system/framework/XposedBridge.jar",
        "/system/bin/app_process.orig",
        "/system/lib/libxposed_art.so",
        "/system/lib64/libxposed_art.so",
        "/data/data/de.robv.android.xposed.installer",
        "/data/user/0/de.robv.android.xposed.installer",
        nullptr
    };

    for (int i = 0; xposed_paths[i] != nullptr; i++) {
        struct stat st;
        if (stat(xposed_paths[i], &st) == 0) {
            LOGW("Xposed path detected: %s", xposed_paths[i]);
            return true;
        }
    }

    const char* lsposed_paths[] = {
        "/data/adb/lspd",
        "/data/adb/modules/zygisk_lsposed",
        "/data/adb/modules/riru_lsposed",
        "/data/adb/modules/edxposed",
        "/data/adb/modules/riru_edxposed",
        "/data/data/org.lsposed.manager",
        "/data/user/0/org.lsposed.manager",
        nullptr
    };

    for (int i = 0; lsposed_paths[i] != nullptr; i++) {
        struct stat st;
        if (stat(lsposed_paths[i], &st) == 0) {
            LOGW("LSPosed/EdXposed path detected: %s", lsposed_paths[i]);
            return true;
        }
    }

    std::ifstream maps("/proc/self/maps");
    if (maps.is_open()) {
        std::string line;
        while (std::getline(maps, line)) {
            if (line.find("XposedBridge") != std::string::npos ||
                line.find("libxposed") != std::string::npos ||
                line.find("lspd") != std::string::npos ||
                line.find("edxposed") != std::string::npos) {
                LOGW("Xposed/LSPosed library detected in maps");
                return true;
            }
        }
    }

    return false;
}

bool AntiDebug::isRunningInEmulator() {

    if (!g_strictMode.load()) {
        LOGD("模拟器检测已跳过（非严格模式）");
        return false;
    }

    int score = 0;

    const char* emulator_files[] = {
        "/dev/socket/qemud",
        "/dev/qemu_pipe",
        "/system/lib/libc_malloc_debug_qemu.so",
        "/sys/qemu_trace",
        "/system/bin/qemu-props",
        "/dev/goldfish_pipe",
        nullptr
    };

    for (int i = 0; emulator_files[i] != nullptr; i++) {
        struct stat st;
        if (stat(emulator_files[i], &st) == 0) {
            score++;
        }
    }

    std::ifstream cpuinfo("/proc/cpuinfo");
    if (cpuinfo.is_open()) {
        std::string line;
        while (std::getline(cpuinfo, line)) {
            if (line.find("goldfish") != std::string::npos ||
                line.find("ranchu") != std::string::npos) {
                score += 2;
                break;
            }
        }
    }

    std::ifstream buildprop("/system/build.prop");
    if (buildprop.is_open()) {
        std::string line;
        while (std::getline(buildprop, line)) {
            if (line.find("generic") != std::string::npos ||
                line.find("sdk_gphone") != std::string::npos ||
                line.find("vbox86") != std::string::npos ||
                line.find("nox") != std::string::npos) {
                score++;
            }
        }
    }

    bool isEmulator = score >= 3;
    if (isEmulator) {
        LOGW("Emulator detected with score: %d", score);
    }

    return isEmulator;
}

bool AntiDebug::isRooted() {
    int score = 0;

    const char* root_paths[] = {
        "/system/app/Superuser.apk",
        "/sbin/su",
        "/system/bin/su",
        "/system/xbin/su",
        "/data/local/xbin/su",
        "/data/local/bin/su",
        "/system/sd/xbin/su",
        "/system/bin/failsafe/su",
        "/data/local/su",
        "/su/bin/su",
        nullptr
    };

    for (int i = 0; root_paths[i] != nullptr; i++) {
        struct stat st;
        if (stat(root_paths[i], &st) == 0) {
            score++;
        }
    }

    const char* magisk_paths[] = {
        "/magisk/.core",
        "/sbin/.magisk",
        "/data/adb/magisk",
        "/data/adb/modules",
        nullptr
    };

    for (int i = 0; magisk_paths[i] != nullptr; i++) {
        struct stat st;
        if (stat(magisk_paths[i], &st) == 0) {
            score++;
        }
    }

    if (access("/system/xbin/su", X_OK) == 0 ||
        access("/system/bin/su", X_OK) == 0 ||
        access("/sbin/su", X_OK) == 0) {
        score += 2;
    }

    std::ifstream mountinfo("/proc/self/mountinfo");
    if (mountinfo.is_open()) {
        std::string line;
        while (std::getline(mountinfo, line)) {
            if (line.find("magisk") != std::string::npos ||
                line.find("core/mirror") != std::string::npos) {
                score++;
                break;
            }
        }
    }

    bool rooted = score >= 1;
    if (rooted) {
        LOGD("Root detected with score: %d", score);
    }

    return rooted;
}

int AntiDebug::getSecurityThreatLevel() {
    int threatLevel = 0;

    if (isDebuggerAttached()) {
        threatLevel += 40;
    }

    if (detectFrida()) {
        threatLevel += 35;
    }

    if (detectXposed()) {
        threatLevel += 20;
    }

    if (isRooted()) {
        threatLevel += 5;
    }

    if (g_strictMode.load() && isRunningInEmulator()) {
        threatLevel += 10;
    }

    return threatLevel > 100 ? 100 : threatLevel;
}

bool AntiDebug::shouldBlockSensitiveOperation() {

    return isDebuggerAttached() || detectFrida();
}
