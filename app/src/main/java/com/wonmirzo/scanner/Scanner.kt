package com.wonmirzo.scanner

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.Detector.Detections
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import java.io.IOException

class Scanner(private var activity: Activity, surfaceView: SurfaceView, listener: ScannerListener) {
    private lateinit var camera: SurfaceView
    private var showToasts = true
        set(scanning) {
            var scanning = scanning
            scanning = if (scanning) {
                prepareScanning()
                true
            } else {
                camera.destroyDrawingCache()
                false
            }
            field = scanning
        }
    private lateinit var listener: ScannerListener

    init {
        setSurfaceView(surfaceView)
        setListener(listener)
        this.scan()
    }

    private fun setSurfaceView(surfaceView: SurfaceView) {
        camera = surfaceView
    }

    private fun setListener(listener: ScannerListener) {
        this.listener = listener
    }

    fun scan() {
        prepareScanning()
    }

    private fun prepareScanning() {
        val textRecognizer = TextRecognizer.Builder(activity).build()
        if (!textRecognizer.isOperational) {
            val lowStorageFilter = IntentFilter("android.intent.action.DEVICE_STORAGE_LOW")
            val lowStorage =
                activity.registerReceiver(null as BroadcastReceiver?, lowStorageFilter) != null
            if (lowStorage) {
                Companion.state = "You have low storage"
                if (showToasts) {
                    Toast.makeText(activity, Companion.state, Toast.LENGTH_LONG).show()
                }
                Log.e(LOG_TEXT, Companion.state)
                listener.onStateChanged(Companion.state, 2)
            } else {
                Companion.state = "OCR not ready"
                if (showToasts) {
                    Toast.makeText(activity, Companion.state, Toast.LENGTH_LONG).show()
                }
                Log.e(LOG_TEXT, Companion.state)
                listener.onStateChanged(Companion.state, 3)
            }
        } else {
            val cameraSource = CameraSource.Builder(activity, textRecognizer).setFacing(0)
                .setRequestedPreviewSize(1280, 1024).setRequestedFps(2.0f).setAutoFocusEnabled(true)
                .build()
            camera.holder.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
                    try {
                        if (ActivityCompat.checkSelfPermission(
                                activity,
                                "android.permission.CAMERA"
                            ) != 0
                        ) {
                            ActivityCompat.requestPermissions(
                                activity,
                                arrayOf("android.permission.CAMERA"),
                                REQUEST_CAMERA
                            )
                            return
                        }
                        cameraSource.start(camera.holder)
                    } catch (var3: IOException) {
                        var3.printStackTrace()
                    }
                }

                override fun surfaceChanged(
                    surfaceHolder: SurfaceHolder,
                    i: Int,
                    i1: Int,
                    i2: Int
                ) {
                }

                override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {
                    cameraSource.stop()
                }
            })
            textRecognizer.setProcessor(object : Detector.Processor<TextBlock> {
                override fun release() {}
                override fun receiveDetections(detections: Detections<TextBlock>) {
                    Companion.state = "running"
                    listener.onStateChanged(Companion.state, 1)
                    val items = detections.detectedItems
                    if (items.size() != 0) {
                        val stringBuilder = StringBuilder()
                        for (i in 0 until items.size()) {
                            val item = items.valueAt(i) as TextBlock
                            stringBuilder.append(item.value)
                            stringBuilder.append("\n")
                        }
                        activity.runOnUiThread { listener.onDetected(stringBuilder.toString()) }
                        Log.d(LOG_TEXT, stringBuilder.toString())
                    }
                }
            })
        }

    }

    fun showToasts(show: Boolean) {
        showToasts = show
    }

    val state: String
        get() = Companion.state

    companion object {
        private const val REQUEST_CAMERA = 12
        private const val LOG_TEXT = "SCANNER"
        private var state = "loading"
    }
}