package com.webtoapp.core.aicoding.todo

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TodoManager {

    data class Item(
        val id: String,
        val subject: String,
        val status: Status = Status.Pending,
        val updatedAt: Long = System.currentTimeMillis()
    ) {
        enum class Status { Pending, InProgress, Completed }
    }

    data class Draft(
        val id: String? = null,
        val subject: String,
        val status: Item.Status = Item.Status.Pending
    )

    private val _items = MutableStateFlow<List<Item>>(emptyList())
    val items: StateFlow<List<Item>> = _items.asStateFlow()

    private var counter = 0

    @Synchronized
    fun replaceAll(subjects: List<Pair<String, Item.Status>>): List<Item> {
        return replaceDrafts(subjects.map { (subject, status) -> Draft(subject = subject, status = status) })
    }

    @Synchronized
    fun replaceDrafts(drafts: List<Draft>): List<Item> {
        counter = 0
        val used = linkedSetOf<String>()
        val next = drafts.map { draft ->
            val preferred = draft.id?.trim().orEmpty()
            val id = when {
                preferred.isNotEmpty() && preferred !in used -> preferred
                else -> nextNumericId(used)
            }
            used += id
            Item(id = id, subject = draft.subject.trim(), status = draft.status)
        }
        _items.value = next
        return next
    }

    @Synchronized
    fun update(id: String, subject: String? = null, status: Item.Status? = null): Item? {
        return updateFlexible(id = id, matchSubject = null, subject = subject, status = status)
    }

    @Synchronized
    fun updateFlexible(
        id: String?,
        matchSubject: String?,
        subject: String? = null,
        status: Item.Status? = null
    ): Item? {
        val current = _items.value.toMutableList()
        if (current.isEmpty()) return null
        val idx = when {
            !id.isNullOrBlank() -> {
                val byId = current.indexOfFirst { it.id == id }
                if (byId >= 0) byId
                else current.indexOfFirst { it.id.equals(id, ignoreCase = true) }
            }
            !matchSubject.isNullOrBlank() -> current.indexOfFirst {
                it.subject.equals(matchSubject, ignoreCase = true)
            }
            else -> -1
        }.takeIf { it >= 0 }
            ?: when {
                !matchSubject.isNullOrBlank() -> current.indexOfFirst {
                    it.subject.contains(matchSubject, ignoreCase = true)
                }
                status == Item.Status.InProgress -> current.indexOfFirst {
                    it.status == Item.Status.Pending || it.status == Item.Status.InProgress
                }
                status == Item.Status.Completed -> current.indexOfFirst {
                    it.status == Item.Status.InProgress
                }.takeIf { it >= 0 } ?: current.indexOfFirst { it.status != Item.Status.Completed }
                else -> -1
            }
        if (idx < 0) return null
        val updated = current[idx].copy(
            subject = subject?.takeIf { it.isNotBlank() } ?: current[idx].subject,
            status = status ?: current[idx].status,
            updatedAt = System.currentTimeMillis()
        )
        current[idx] = updated
        if (updated.status == Item.Status.InProgress) {
            for (i in current.indices) {
                if (i != idx && current[i].status == Item.Status.InProgress) {
                    current[i] = current[i].copy(
                        status = Item.Status.Pending,
                        updatedAt = System.currentTimeMillis()
                    )
                }
            }
        }
        _items.value = current
        return updated
    }

    @Synchronized
    fun get(id: String): Item? = _items.value.firstOrNull { it.id == id }

    @Synchronized
    fun snapshotSummary(): String {
        val items = _items.value
        if (items.isEmpty()) return "(empty)"
        return items.joinToString("; ") { "#${it.id}[${it.status.name.lowercase()}] ${it.subject}" }
    }

    @Synchronized
    fun clear() {
        _items.value = emptyList()
        counter = 0
    }

    private fun nextNumericId(used: Set<String>): String {
        while (true) {
            counter++
            val id = counter.toString()
            if (id !in used) return id
        }
    }
}
