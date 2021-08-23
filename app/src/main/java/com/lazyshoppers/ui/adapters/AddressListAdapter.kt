package com.lazyshoppers.ui.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lazyshoppers.databinding.ItemAddressLayoutBinding
import com.lazyshoppers.models.Address
import com.lazyshoppers.ui.activities.AddEditAddressActivity
import com.lazyshoppers.ui.activities.CheckoutActivity
import com.lazyshoppers.utils.Constants

open class AddressListAdapter(
    private val context: Context,
    private val list: ArrayList<Address>,
    private val selectAddress: Boolean
) : RecyclerView.Adapter<AddressListAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemAddressLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = list[position]
        holder.tvFullName.text = model.name
        holder.tvAddressType.text = model.type
        holder.tvMobileNumber.text = model.mobileNumber
        holder.tvAddressDetails.text = "${model.address},${model.zipCode}"

        if (selectAddress) {
            holder.itemView.setOnClickListener {

                val intent = Intent(context, CheckoutActivity::class.java)
                intent.putExtra(Constants.EXTRA_CHECKOUT_ADDRESS, model)
                context.startActivity(intent)
            }

        }

    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun notifyEditItem(activity: Activity, position: Int) {
        val intent = Intent(context, AddEditAddressActivity::class.java)
        intent.putExtra(Constants.EXTRA_ADDRESSES_DETAILS, list[position])
        activity.startActivityForResult(intent, Constants.ADD_ADDRESSES_REQUEST_CODE)
        notifyItemChanged(position)
    }

    class MyViewHolder(view: ItemAddressLayoutBinding) : RecyclerView.ViewHolder(view.root) {
        val tvFullName = view.tvAddressFullName
        val tvAddressType = view.tvAddressType
        val tvMobileNumber = view.tvAddressMobileNumber
        val tvAddressDetails = view.tvAddressDetails
    }
}