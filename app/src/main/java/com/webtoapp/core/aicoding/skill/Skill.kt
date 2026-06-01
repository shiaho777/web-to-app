package com.webtoapp.core.aicoding.skill

data class Skill(
    val name: String,
    val description: String,
    val whenToUse: String = "",

    val icon: String = "auto_awesome",

    val iconColor: String = "9CA3AF",

    val allowedTools: List<String> = emptyList(),

    val implicitlyActiveFor: List<String> = emptyList(),
    val context: Context = Context.Inline,

    val argumentHint: String = "",

    val promptText: String,

    val source: Source = Source.Bundled,

    val hidden: Boolean = false,

    val pinned: Boolean = false,

    val rootDir: String? = null,

    val modelInvokable: Boolean = true,

    val userInvokable: Boolean = true,

    val starterDir: String? = null,

    val starterAssetDir: String? = null,

    val category: Category = Category.Tool
) {
    enum class Context { Inline, Fork }
    enum class Source { Bundled, Market, User }
    enum class Category { App, Module, Tool, Custom }

    fun resolvePromptText(arguments: String): String {
        var text = promptText
        text = text.replace("\${ARGUMENTS}", arguments)
        text = text.replace("\$ARGUMENTS", arguments)
        if (rootDir != null) {
            text = text.replace("\${SKILL_DIR}", rootDir)
        }
        return text
    }
}
