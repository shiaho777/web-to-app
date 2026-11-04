package com.webtoapp.core.apkbuilder

internal fun requiredNativeLib(libName: String, appType: String): Boolean {
    if (libName == "libperf_engine.so" || libName == "libbrowser_kernel.so") return false

    if (libName == "libphp.so") return appType in setOf("WORDPRESS", "PHP_APP")

    if (libName == "libnode_bridge.so" ||
        libName == "libnode.so" ||
        libName == "libnode_launcher.so"
    ) return appType == "NODEJS_APP"

    if (libName == "libgo_exec_loader.so") return appType == "GO_APP"

    if (libName == "libpython3.so" || libName == "libmusl-linker.so") return appType == "PYTHON_APP"

    val geckoViewLibs = setOf(
        "libcrashhelper.so",
        "libgkcodecs.so",
        "libminidump_analyzer.so",
        "libnss3.so",
        "libfreebl3.so",
        "libsoftokn3.so",
        "liblgpllibs.so",
        "libplugin-container.so"
    )
    if (libName in geckoViewLibs) return false

    return true
}

internal fun editorOnlyAsset(
    entryName: String,
    appType: String,
    engineType: String,
    browserDisguiseEnabled: Boolean = true
): Boolean {
    if (entryName.startsWith("assets/template/")) return true

    if (entryName.startsWith("assets/sample_projects/")) return true

    if (entryName.startsWith("assets/extensions/")) return true

    if (entryName == "assets/omni.ja") return true

    if (entryName == "assets/php_router_server.php" && appType !in setOf("WORDPRESS", "PHP_APP")) return true

    if (entryName.startsWith("assets/python_runtime/") && appType != "PYTHON_APP") return true

    if (entryName.startsWith("assets/go_runtime/") && appType != "GO_APP") return true

    if (entryName.startsWith("assets/help/")) return true

    if (entryName.startsWith("assets/schemas/")) return true

    if (entryName == "assets/default_config.json") return true

    if (entryName.startsWith("assets/frontend_tools/") && appType != "FRONTEND") return true

    if (entryName.startsWith("assets/nodejs_runtime/") && appType != "NODEJS_APP") return true

    if (entryName.startsWith("assets/web_extensions/") && engineType != "GECKOVIEW") return true

    if (entryName.startsWith("res/") && entryName.endsWith(".js") && !browserDisguiseEnabled) return true

    return false
}
