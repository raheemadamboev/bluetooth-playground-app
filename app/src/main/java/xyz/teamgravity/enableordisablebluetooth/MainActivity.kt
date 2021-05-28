package xyz.teamgravity.enableordisablebluetooth

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import xyz.teamgravity.enableordisablebluetooth.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var bluetoothAdapter: BluetoothAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        button()
    }

    private fun button() {
        onTurnOnOff()
    }

    // turn on off button
    private fun onTurnOnOff() {
        binding.apply {

            // device has no bt capabilities
            if (bluetoothAdapter == null) {
                onOffB.text = getString(R.string.not_available_bluetooth)
                statusT.text = getString(R.string.not_available_bluetooth)
            } else {

                // update ui
                updateUI()

                // register receiver
                val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
                registerReceiver(receiver, filter)

                // button
                onOffB.setOnClickListener {
                    if (bluetoothAdapter!!.isEnabled) {
                        bluetoothAdapter!!.disable()
                    } else {
                        startActivity(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                    }
                }
            }
        }
    }

    private fun updateUI() {
        if (bluetoothAdapter!!.isEnabled) {
            updateBluetoothOn()
        } else {
            updateBluetoothOff()
        }
    }

    private fun updateBluetoothOn() {
        binding.apply {
            statusT.text = getString(R.string.state_on)
            onOffB.text = getString(R.string.turn_off_bluetooth)
        }
    }

    private fun updateBluetoothOff() {
        binding.apply {
            statusT.text = getString(R.string.state_off)
            onOffB.text = getString(R.string.turn_on_bluetooth)
        }
    }

    // respond to bluetooth changes
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                binding.apply {
                    when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {
                        BluetoothAdapter.STATE_OFF -> {
                            updateBluetoothOff()
                        }

                        BluetoothAdapter.STATE_TURNING_OFF -> {
                            statusT.text = getString(R.string.turning_off)
                        }

                        BluetoothAdapter.STATE_ON -> {
                            updateBluetoothOn()
                        }

                        BluetoothAdapter.STATE_TURNING_ON -> {
                            statusT.text = getString(R.string.turning_on)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }
}