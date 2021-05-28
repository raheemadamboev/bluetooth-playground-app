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

        bluetoothAdapter()
    }

    private fun bluetoothAdapter() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        binding.apply {
            // device has no bt capabilities
            if (bluetoothAdapter == null) {
                onOffB.text = getString(R.string.not_available_bluetooth)
                statusT.text = getString(R.string.not_available_bluetooth)
            } else {
                updateUI()
                button()
            }
        }
    }

    private fun updateUI() {
        if (bluetoothAdapter!!.isEnabled) {
            updateBluetoothOn()
        } else {
            updateBluetoothOff()
        }

        updateDiscoverability()
    }

    private fun button() {
        onTurnOnOff()
        onDiscoverability()
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

    private fun updateDiscoverability() {
        binding.discoverabilityT.text =
            getString(if (bluetoothAdapter!!.isDiscovering) R.string.discoverability_enabled else R.string.discoverability_disabled)
    }

    // turn on off button
    private fun onTurnOnOff() {
        binding.apply {

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

    // on off discoverability
    private fun onDiscoverability() {
        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 10)

        val filter = IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)
        registerReceiver(receiver, filter)

        binding.discoverabilityB.setOnClickListener {
            startActivity(intent)
        }
    }

    // respond to bluetooth changes
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            binding.apply {
                when (intent?.action) {

                    // turn off on state
                    BluetoothAdapter.ACTION_STATE_CHANGED -> {
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

                    // discoverability
                    BluetoothAdapter.ACTION_SCAN_MODE_CHANGED -> {
                        when (intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR)) {

                            BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE -> {
                                discoverabilityT.text = getString(R.string.discoverability_enabled)
                            }

                            BluetoothAdapter.SCAN_MODE_CONNECTABLE -> {
                                discoverabilityT.text = getString(R.string.connectable)
                            }

                            BluetoothAdapter.SCAN_MODE_NONE -> {
                                discoverabilityT.text = getString(R.string.discoverability_disabled_none)
                            }

                            BluetoothAdapter.STATE_CONNECTING -> {
                                discoverabilityT.text = getString(R.string.connecting)
                            }

                            BluetoothAdapter.STATE_CONNECTED -> {
                                discoverabilityT.text = getString(R.string.connected)
                            }
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