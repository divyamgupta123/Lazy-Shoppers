package com.lazyshoppers.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lazyshoppers.databinding.ItemListLayoutBinding
import com.lazyshoppers.models.Order
import com.lazyshoppers.ui.activities.MyOrdersDetailsActivity
import com.lazyshoppers.utils.Constants
import com.lazyshoppers.utils.GlideLoader

open class MyOrdersListAdapter(
    private val context: Context,
    private val list: ArrayList<Order>
) : RecyclerView.Adapter<MyOrdersListAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemListLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = list[position]

        GlideLoader(context)
            .loadUserPicture(model.image, holder.ivItemImage)

        holder.tvItemName.text = model.title
        holder.tvItemPrice.text = "₹${model.total_amount}"

        holder.ibDeleteItem.visibility = View.GONE

        holder.itemView.setOnClickListener {
            val intent = Intent(context, MyOrdersDetailsActivity::class.java)
            intent.putExtra(Constants.EXTRA_MY_ORDER_DETAILS, model)
            context.startActivity(intent)
        }


    }

    override fun getItemCount(): Int {
        return list.size
    }

    class MyViewHolder(view: ItemListLayoutBinding) : RecyclerView.ViewHolder(view.root) {
        val ivItemImage = view.ivItemImage
        val tvItemName = view.tvItemName
        val tvItemPrice = view.tvItemPrice
        val ibDeleteItem = view.ibDeleteProduct
    }
}