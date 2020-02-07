package org.team5419.fault.can

open class CanDevice(val id: Int) {
    init {
        CanManager.addDevice(id)
    }
}