package xyz.teamgravity.enableordisablebluetooth

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import xyz.teamgravity.enableordisablebluetooth.databinding.CardBluetoothDeviceBinding

class BluetoothDevicesAdapter() : ListAdapter<BluetoothDevice, BluetoothDevicesAdapter.BluetoothDeviceViewHolder>(DIFF) {
    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<BluetoothDevice>() {
            override fun areItemsTheSame(oldItem: BluetoothDevice, newItem: BluetoothDevice) = oldItem.address == newItem.address

            override fun areContentsTheSame(oldItem: BluetoothDevice, newItem: BluetoothDevice) = oldItem == newItem
        }
    }

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
        holder.bind(getItem(position))
    }
}