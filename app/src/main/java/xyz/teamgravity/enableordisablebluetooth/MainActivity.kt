package xyz.teamgravity.enableordisablebluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import xyz.teamgravity.enableordisablebluetooth.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), BluetoothDevicesAdapter.OnBluetoothDeviceListener {
    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 1
        private val PERMISSIONS = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

        @RequiresApi(Build.VERSION_CODES.Q)
        private val PERMISSIONS_Q = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )
    }

    private lateinit var binding: ActivityMainBinding

    private lateinit var devicesAdapter: BluetoothDevicesAdapter
    private lateinit var locationManager: LocationManager

    private val devices = mutableListOf<BluetoothDevice>()

    private var bluetoothAdapter: BluetoothAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        recyclerView()
        bluetoothAdapter()
    }

    private fun recyclerView() {
        devicesAdapter = BluetoothDevicesAdapter(this)
        binding.recyclerView.adapter = devicesAdapter
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
                registerBondReceiver()
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

    private fun button() {
        onTurnOnOff()
        onDiscoverability()
        onDiscover()
    }

    private fun registerBondReceiver() {
        val filter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        filter.priority = 1000000
        registerReceiver(receiver, filter)
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

    // check permissions
    private fun hasPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            PERMISSIONS.all {
                ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }
        } else {
            PERMISSIONS_Q.all {
                ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }
        }
    }

    // request permissions
    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSIONS_REQUEST_CODE)
        } else {
            ActivityCompat.requestPermissions(this, PERMISSIONS_Q, PERMISSIONS_REQUEST_CODE)
        }
    }

    private fun hasGPSEnabled() = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

    // start discovery
    private fun discoverDevices() {
        devices.clear()
        devicesAdapter.submitList(devices)
        println("debug: discovery started")
        bluetoothAdapter?.startDiscovery()
    }

    // turn on off button
    private fun onTurnOnOff() {
        binding.apply {

            // register receiver
            val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
            filter.priority = 1000000
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

    // on off discoverability button
    private fun onDiscoverability() {
        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)

        val filter = IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)
        filter.priority = 1000000
        registerReceiver(receiver, filter)

        binding.discoverabilityB.setOnClickListener {
            startActivity(intent)
        }
    }

    // discover devices button
    private fun onDiscover() {
        val filter = IntentFilter()
        filter.addAction(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        filter.priority = 1000000
        registerReceiver(receiver, filter)

        binding.discoverB.setOnClickListener {
            if (!hasPermissions()) {
                requestPermissions()
                return@setOnClickListener
            }

            if (!hasGPSEnabled()) {
                startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                return@setOnClickListener
            }

            println("debug: button clicked")
            discoverDevices()
        }
    }

    // bluetooth device click
    override fun onBluetoothDeviceClick(device: BluetoothDevice) {
        device.createBond()
    }

    // respond to bluetooth changes
    private val receiver = object : BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
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

                    // discovery started
                    BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                        binding.apply {
                            discoverB.isEnabled = false
                            progressBar.isVisible = true
                        }
                    }

                    // discovery stopped
                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                        binding.apply {
                            discoverB.isEnabled = true
                            progressBar.isVisible = false
                        }
                    }

                    // devices found
                    BluetoothDevice.ACTION_FOUND -> {
                        println("debug: device found")
                        val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE) ?: return
                        devices.add(device)
                        devicesAdapter.submitList(devices)
                    }

                    // device bond state
                    BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                        binding.apply {
                            val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE) ?: return
                            when (device.bondState) {

                                // device bonded
                                BluetoothDevice.BOND_BONDED -> {
                                    bondT.text = "Bonded to ${device.name}"
                                }

                                // creating bond
                                BluetoothDevice.BOND_BONDING -> {
                                    bondT.text = "Bonding to ${device.name}"
                                }

                                // breaking bond
                                BluetoothDevice.BOND_NONE -> {
                                    bondT.text = "Breaking bond from ${device.address}"
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                discoverDevices()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }
}