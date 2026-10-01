# CMakeLists.txt for WebToApp native components

cmake_minimum_required(VERSION 3.22.1)

project("webtoapp_native" C)

# 查找 Android 日志库
find_library(
    log-lib
    log
)

# User-mode exec loader for static ELFs under SELinux W^X — host-side only;
# a real JNI shared library, loaded lazily by StaticExecProcess when plain
# execve of downloaded toolchains (e.g. esbuild) is unavailable. AArch64
# only: the child trampoline is AArch64 asm, and the loader parses AArch64 ELFs.
if (ANDROID_ABI STREQUAL "arm64-v8a")
    add_library(
        static_exec
        SHARED
        static_exec_loader.c
    )

    target_link_libraries(
        static_exec
        ${log-lib}
    )

    target_compile_options(static_exec PRIVATE
        -ffunction-sections
        -fdata-sections
        -O2
        -DNDEBUG
    )

    set_target_properties(static_exec PROPERTIES
        LINK_FLAGS "-Wl,--gc-sections -Wl,-z,max-page-size=16384"
    )
endif()

# ==================== Performance Engine (极致性能优化引擎) ====================
# C 级热路径加速：URL 解析、MIME 查找、mmap I/O、性能 JS 注入
add_library(
    perf_engine
    SHARED
    perf_engine.c
)

set_property(TARGET perf_engine PROPERTY C_STANDARD 11)

# -O3 最大速度优化 (性能引擎不需要体积优化)
target_compile_options(perf_engine PRIVATE
    -fvisibility=hidden
    -ffunction-sections
    -fdata-sections
    -O3
    -DNDEBUG
    -Wall
    -Wextra
)

target_link_libraries(
    perf_engine
    ${log-lib}
)

set_target_properties(perf_engine PROPERTIES
    LINK_FLAGS "-Wl,--gc-sections -Wl,--strip-all -Wl,-z,max-page-size=16384"
)

# ==================== System Optimizer (系统层极致性能优化) ====================
# C 级系统调优：CPU 亲和性、线程优先级、OOM 评分、I/O 预读、温度监控
add_library(
    sys_optimizer
    SHARED
    sys_optimizer.c
)

set_property(TARGET sys_optimizer PROPERTY C_STANDARD 11)

# -O3 最大速度优化
target_compile_options(sys_optimizer PRIVATE
    -fvisibility=hidden
    -ffunction-sections
    -fdata-sections
    -O3
    -DNDEBUG
    -Wall
    -Wextra
)

target_link_libraries(
    sys_optimizer
    ${log-lib}
)

set_target_properties(sys_optimizer PROPERTIES
    LINK_FLAGS "-Wl,--gc-sections -Wl,--strip-all -Wl,-z,max-page-size=16384"
)

# ==================== Browser Kernel (浏览器内核伪装引擎) ====================
# 反 WebView 检测: navigator/chrome/WebGL/Permissions/Error stack 全面伪装
add_library(
    browser_kernel
    SHARED
    browser_kernel.c
)

set_property(TARGET browser_kernel PROPERTY C_STANDARD 11)

# -Os 体积优化 (此库主要是静态字符串, 不需要 -O3 速度优化)
target_compile_options(browser_kernel PRIVATE
    -fvisibility=hidden
    -ffunction-sections
    -fdata-sections
    -Os
    -DNDEBUG
    -Wall
    -Wextra
)

target_link_libraries(
    browser_kernel
    ${log-lib}
)

set_target_properties(browser_kernel PROPERTIES
    LINK_FLAGS "-Wl,--gc-sections -Wl,--strip-all -Wl,-z,max-page-size=16384"
)
