package com.webtoapp.core.aicoding.skill

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SkillRegistry {

    private val byName = LinkedHashMap<String, Skill>()
    private val _skills = MutableStateFlow<List<Skill>>(emptyList())
    val skills: StateFlow<List<Skill>> = _skills.asStateFlow()

    @Synchronized
    fun replaceLayer(source: Skill.Source, items: List<Skill>) {

        val toRemove = byName.values.filter { it.source == source }.map { it.name }
        toRemove.forEach { byName.remove(it) }

        items.forEach { byName[it.name] = it }
        publish()
    }

    @Synchronized
    fun upsert(skill: Skill) {
        byName[skill.name] = skill
        publish()
    }

    @Synchronized
    fun remove(name: String) {
        if (byName.remove(name) != null) publish()
    }

    @Synchronized
    fun get(name: String): Skill? = byName[name]

    @Synchronized
    fun all(): List<Skill> = byName.values.toList()

    @Synchronized
    fun search(query: String, limit: Int = 8): List<Skill> {
        if (query.isBlank()) return byName.values.toList().take(limit)
        val q = query.lowercase()
        return byName.values
            .asSequence()
            .filter { !it.hidden && it.userInvokable }
            .map { it to score(it, q) }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .take(limit)
            .map { it.first }
            .toList()
    }

    private fun score(skill: Skill, q: String): Int {
        val name = skill.name.lowercase()
        if (name == q) return 1000
        if (name.startsWith(q)) return 500 + (skill.pinned.compareTo(false) * 50)
        if (name.contains(q)) return 200
        if (skill.description.lowercase().contains(q)) return 100
        if (skill.whenToUse.lowercase().contains(q)) return 50
        return 0
    }

    private fun publish() {
        _skills.value = byName.values.toList()
            .sortedWith(
                compareByDescending<Skill> { it.pinned }
                    .thenBy { it.source.ordinal }
                    .thenBy { it.category.ordinal }
                    .thenBy { it.name }
            )
    }
}
