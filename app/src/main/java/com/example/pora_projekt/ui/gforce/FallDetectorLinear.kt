import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

class FallDetectorLinear(
    context: Context,
    private val onFallDetected: () -> Unit,
    private val onAccelerationChanged: (Float, Float, Float, Float) -> Unit
) : SensorEventListener {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val linearAccelSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

    private val freeFallUpperThreshold = 10f
    private val impactThreshold = 28f

    private val freeFallMinDuration = 80L
    private val impactMaxDelay = 600L
    private val cooldownTime = 2500L

    private var freeFallStartTime: Long? = null
    private var lastFallTime = 0L

    fun start() {
        linearAccelSensor?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_GAME
            )
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_LINEAR_ACCELERATION) return

        val now = System.currentTimeMillis()

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val magnitude = sqrt(x * x + y * y + z * z)
        onAccelerationChanged(x, y, z, magnitude)

        if (now - lastFallTime < cooldownTime) return

        // padanje
        if (magnitude < freeFallUpperThreshold) {
            if (freeFallStartTime == null) {
                freeFallStartTime = now
            }
            return
        }

        // udarec v tla
        freeFallStartTime?.let { start ->
            val freeFallDuration = now - start

            if (
                freeFallDuration in freeFallMinDuration..impactMaxDelay &&
                magnitude > impactThreshold
            ) {
                lastFallTime = now
                freeFallStartTime = null
                onFallDetected()
            } else if (freeFallDuration > impactMaxDelay) {
                freeFallStartTime = null
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
