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

    private val _items = MutableStateFlow<List<Item>>(emptyList())
    val items: StateFlow<List<Item>> = _items.asStateFlow()

    private var counter = 0

    @Synchronized
    fun replaceAll(subjects: List<Pair<String, Item.Status>>) {
        counter = 0
        _items.value = subjects.map { (subject, status) ->
            counter++
            Item(id = counter.toString(), subject = subject, status = status)
        }
    }

    @Synchronized
    fun update(id: String, subject: String? = null, status: Item.Status? = null): Item? {
        val current = _items.value.toMutableList()
        val idx = current.indexOfFirst { it.id == id }
        if (idx < 0) return null
        val updated = current[idx].copy(
            subject = subject ?: current[idx].subject,
            status = status ?: current[idx].status,
            updatedAt = System.currentTimeMillis()
        )
        current[idx] = updated
        _items.value = current
        return updated
    }

    @Synchronized
    fun get(id: String): Item? = _items.value.firstOrNull { it.id == id }

    @Synchronized
    fun clear() { _items.value = emptyList(); counter = 0 }
}
