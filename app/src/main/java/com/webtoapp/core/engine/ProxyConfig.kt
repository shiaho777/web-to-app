package com.webtoapp.core.engine

data class ProxyConfig(
    val mode: String = "NONE",
    val host: String = "",
    val port: Int = 0,
    val type: String = "HTTP",
    val pacUrl: String = "",
    val username: String = "",
    val password: String = ""
)
