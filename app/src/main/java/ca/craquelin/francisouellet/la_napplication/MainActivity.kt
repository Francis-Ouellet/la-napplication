package ca.craquelin.francisouellet.la_napplication

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), SensorEventListener, View.OnClickListener {

    private val FROM_RADS_TO_DEGS = -57

    private var synvie = true

    private lateinit var rotationSensor: Sensor
    private lateinit var mSensorManager: SensorManager

    private lateinit var croche: MediaPlayer
    private lateinit var drette: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        this.croche = MediaPlayer.create(this,  R.raw.cest_croche)
        this.drette = MediaPlayer.create(this,  R.raw.cest_drette)

        this.btn_mute.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)?.let{
            this.rotationSensor = it
        }.also {
            mSensorManager.registerListener(
                this,
                rotationSensor,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        this.mSensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        Log.i("sensorChanged",event.values.joinToString())
        update(event.values)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.i("accuracyChanged", accuracy.toString());
    }

    override fun onClick(v: View?) {
        synvie = !synvie
    }

    private fun update(vectors: FloatArray) {
        val rotationMatrix = FloatArray(9)
        SensorManager.getRotationMatrixFromVector(rotationMatrix, vectors)
        val worldAxisX = SensorManager.AXIS_X
        val worldAxisZ = SensorManager.AXIS_Z
        val adjustedRotationMatrix = FloatArray(9)
        SensorManager.remapCoordinateSystem(rotationMatrix, worldAxisX, worldAxisZ, adjustedRotationMatrix)
        val orientation = FloatArray(3)
        SensorManager.getOrientation(adjustedRotationMatrix, orientation)
        val pitch = orientation[1] * FROM_RADS_TO_DEGS
        val roll = orientation[2] * FROM_RADS_TO_DEGS

        Log.i("Pitch", pitch.toString())
        Log.i("Roll", roll.toString())

        if (roll > 85 && roll < 95)  {
            this.roll.text = "C'est drette !"
            if(canSynvieTalk()) drette.start()
        } else {
            this.roll.text = "C'est croche !"
            if(canSynvieTalk()) croche.start()
        }


    }

    private fun canSynvieTalk(): Boolean {
        return synvie && (!drette.isPlaying && !croche.isPlaying)
    }
}
