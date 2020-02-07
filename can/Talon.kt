package org.team5419.fault.can

import com.ctre.phoenix.motorcontrol.NeutralMode
import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.InvertType
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import com.ctre.phoenix.ErrorCode


private interface TalonInterface {
    fun set(controlMode: ControlMode, value: Double)

    fun getSelectedSensorPosition(id: Int=0): Int

    fun getSelectedSensorVelocity(id: Int=0): Int

    fun setSelectedSensorPosition(value: Int=0, id: Int=0, timeout: Int=0): ErrorCode

    fun setNeutralMode(mode: NeutralMode)

    fun setInverted(mode: InvertType)

    fun config_kP(id: Int, value: Double, timeout: Int=0): ErrorCode

    fun config_kI(id: Int, value: Double, timeout: Int=0): ErrorCode

    fun config_kD(id: Int, value: Double, timeout: Int=0): ErrorCode

    fun config_kF(id: Int, value: Double, timeout: Int=0): ErrorCode
}

private open class NoTalon : TalonInterface {
    override fun set(controlMode: ControlMode, value: Double) {}

    override fun getSelectedSensorPosition(id: Int): Int = 0

    override fun getSelectedSensorVelocity(id: Int): Int = 0

    override fun setSelectedSensorPosition(value: Int, id: Int, timeout: Int): ErrorCode = ErrorCode.OK

    override fun setNeutralMode(mode: NeutralMode) {}

    override fun setInverted(mode: InvertType) {}

    override fun config_kP(id: Int, value: Double, timeout: Int) = ErrorCode.OK

    override fun config_kI(id: Int, value: Double, timeout: Int) = ErrorCode.OK

    override fun config_kD(id: Int, value: Double, timeout: Int) = ErrorCode.OK

    override fun config_kF(id: Int, value: Double, timeout: Int) = ErrorCode.OK
}

private open class MockTalon : NoTalon() {}

private open class RealTalon(id: Int) : TalonSRX(id), TalonInterface {}

data class PID (
    val P: Double = 0.0,
    val I: Double = 0.0,
    val D: Double = 0.0,
    val F: Double = 0.0
)

data class Slot (
    val pid: PID = PID()
)

class Talon(
    id: Int,

    val enabled: Boolean = true,

    val maxVelocity: Int = -1,
    val maxAcceleration: Int = -1,

    val invert: Boolean? = null,
    val invertType: InvertType =
        if (invert == null) InvertType.None else
        if (invert == true) InvertType.None else
                            InvertType.InvertMotorOutput,

    val neutralMode: NeutralMode = NeutralMode.Coast,

    // slot 0 configs
    val pid: PID = PID(),

    // PID slots
    val slot0: Slot = Slot(pid=pid),
    val slot1: Slot = Slot(),
    val slot2: Slot = Slot(),
    val slot3: Slot = Slot()
) : CanDevice(id), TalonInterface {
    private val talon: TalonInterface

    init {
        if ( CanManager.testMode ) {
            talon = MockTalon()
        } else if ( enabled && id >= 0 ) {
            talon = RealTalon(id)
        } else {
            talon = NoTalon()
        }
    }

    init {
        talon.setInverted(invertType)
        talon.setNeutralMode(neutralMode)

        configSlot(0, slot0)
        configSlot(1, slot1)
        configSlot(2, slot2)
        configSlot(3, slot3)
    }

    // config

    fun configSlot(id: Int, slot: Slot) {
        talon.config_kP(id, slot.pid.P)
        talon.config_kP(id, slot.pid.I)
        talon.config_kP(id, slot.pid.D)
        talon.config_kP(id, slot.pid.F)
    }

    override fun config_kP(id: Int, value: Double, timeout: Int)
        = talon.config_kP(id, value, timeout)

    override fun config_kI(id: Int, value: Double, timeout: Int)
        = talon.config_kI(id, value, timeout)
    
    override fun config_kD(id: Int, value: Double, timeout: Int)
        = talon.config_kD(id, value, timeout)

    override fun config_kF(id: Int, value: Double, timeout: Int)
        = talon.config_kF(id, value, timeout)

    // set

    override fun set(controlMode: ControlMode, value: Double)
        = talon.set(controlMode, value)

    override fun setNeutralMode(mode: NeutralMode)
        = talon.setNeutralMode(mode)

    override fun setInverted(mode: InvertType)
        = talon.setInverted(mode)

    // sensor functions

    fun position(id: Int=0) = talon.getSelectedSensorPosition(id)

    override fun getSelectedSensorPosition(id: Int): Int
        = talon.getSelectedSensorPosition(id)

    fun velocity(id: Int=0) = talon.getSelectedSensorPosition(id)

    override fun getSelectedSensorVelocity(id: Int): Int
        = talon.getSelectedSensorVelocity(id)

    fun clearPosition(id: Int=0, timeout: Int=0)
        = talon.setSelectedSensorPosition(0, id, timeout)

    fun setPosition(value: Int, id: Int=0, timeout: Int=0)
        = talon.setSelectedSensorPosition(value, id, timeout)

    override fun setSelectedSensorPosition(value: Int, id: Int, timeout: Int) =
        talon.setSelectedSensorPosition(value, id, timeout)

    //

    fun test() : Talon {
        val talon = Talon(
            id = 1,

            pid = PID(1.0, 0.0, 0.5),

            slot3 = Slot()
        )

        talon.clearPosition()

        return talon
    }
}