package com.webtoapp.core.aicoding.tool

class ToolRegistry(private val tools: List<Tool>) {
    val all: List<Tool> = tools.toList()
    private val byName: Map<String, Tool> = tools.associateBy { it.name }

    operator fun get(name: String): Tool? = byName[name]
    fun contains(name: String): Boolean = name in byName
    fun names(): List<String> = byName.keys.toList()

    fun filter(allowed: Set<String>): ToolRegistry =
        ToolRegistry(tools.filter { it.name in allowed })

    fun plus(extra: List<Tool>): ToolRegistry {
        val existing = byName.keys
        return ToolRegistry(tools + extra.filter { it.name !in existing })
    }
}
