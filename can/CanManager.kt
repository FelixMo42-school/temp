package org.team5419.fault.can

object CanManager {
    var testMode = false

    val ids = mutableSetOf<Int>()

    fun addDevice(id: Int) {
        assert( !has(id) )

        ids.add(id)
    }

    fun has(id: Int): Boolean {
        return ids.contains(id)
    }
}