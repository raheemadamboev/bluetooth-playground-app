package xyz.teamgravity.enableordisablebluetooth

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import xyz.teamgravity.enableordisablebluetooth.databinding.CardBluetoothDeviceBinding

class BluetoothDevicesAdapter(private val devices: List<BluetoothDevice>) :
    RecyclerView.Adapter<BluetoothDevicesAdapter.BluetoothDeviceViewHolder>() {

    inner class BluetoothDeviceViewHolder(private val binding: CardBluetoothDeviceBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(model: BluetoothDevice) {
            binding.apply {
                deviceNameT.text = model.name
                deviceAddressT.text = model.address
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        BluetoothDeviceViewHolder(CardBluetoothDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: BluetoothDeviceViewHolder, position: Int) {
        holder.bind(devices[position])
    }

    override fun getItemCount() = devices.size
}