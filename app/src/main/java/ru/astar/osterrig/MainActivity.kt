package ru.astar.osterrig

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.astar.osterrig.databinding.ActivityMainBinding
import ru.astar.osterrig.entities.RgbColor
import ru.astar.osterrig.lamps.Lamp
import ru.astar.osterrig.lamps.Sirius
import java.nio.ByteBuffer
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "MainActivity"

        const val TESTING_DEVICE_ADDRESS1 = "78:21:84:CE:F6:22"
        const val TESTING_DEVICE_ADDRESS2 = "94:3C:C6:29:C6:9E"
    }

    private val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        listOf(Manifest.permission.BLUETOOTH_CONNECT)
    } else {
        listOf()
    }

    private lateinit var binding: ActivityMainBinding

    private lateinit var sirius: Lamp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val device1 = createDevice(TESTING_DEVICE_ADDRESS1)
        sirius = Sirius(context = this, device = device1)

        binding.apply {
            buttonConnect.setOnClickListener {
                if (checkAllPermissions()) {
                    lifecycleScope.launch {
                        sirius.connect()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Нет разрешений", Toast.LENGTH_SHORT).show()
                }
            }

            buttonSend.setOnClickListener {
                lifecycleScope.launch {
                    sirius.setColor(RgbColor(255, 0, 0))
                    delay(300)
                    sirius.setColor(RgbColor(0, 255, 0))
                    delay(300)
                    sirius.setColor(RgbColor(255, 255, 0))
                    delay(300)
                    sirius.setColor(RgbColor(0, 0, 255))
                    delay(300)
                    sirius.setColor(RgbColor(255, 0, 255))
                    delay(300)
                }
            }

            buttonReadState.setOnClickListener {
                val sb = StringBuilder()
                sb.append(sirius.state.color).append("\n")
                sb.append(sirius.state.brightness).append("\n")
                showAlert(sb.toString())
            }

            sbHue.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean,
                ) {
                    val hsv = FloatArray(3)
                    hsv[0] = progress.toFloat()
                    hsv[1] = 1f
                    hsv[2] = 1f

                    val color = Color.HSVToColor(hsv)
                    val rgbColor = RgbColor(Color.red(color), Color.green(color), Color.blue(color))

                    lifecycleScope.launch {
                        sirius.setColor(rgbColor)
                    }

                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }
            })
        }
    }

    private fun showAlert(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Сообщение")
            .setMessage(message)
            .setPositiveButton("OK") { d, _ ->
                d.dismiss()
            }
            .show()
    }

    private fun createDevice(address: String): BluetoothDevice {
        val manager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        return manager.adapter.getRemoteDevice(address)
    }

    private fun checkAllPermissions(): Boolean {
        val deniedPermissions = mutableListOf<String>()
        for (permission in permissions) {
            if (!checkPermission(permission)) {
                deniedPermissions.add(permission)
            }
        }
        return deniedPermissions.isEmpty()
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }


    private fun generateColor(): ByteArray {
        val buffer = ByteBuffer.allocate(4)
        buffer.put(0x16)
        repeat(3) {
            buffer.put(Random.nextInt(255).toByte())
        }
        return buffer.array()
    }

}