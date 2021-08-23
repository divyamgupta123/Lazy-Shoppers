package com.lazyshoppers.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lazyshoppers.databinding.ItemDashboardLayoutBinding
import com.lazyshoppers.models.Product
import com.lazyshoppers.ui.activities.ProductDetailsActivity
import com.lazyshoppers.utils.Constants
import com.lazyshoppers.utils.GlideLoader

open class DashboardItemsListAdapter(
    val context: Context,
    private var list: ArrayList<Product>
) : RecyclerView.Adapter<DashboardItemsListAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding: ItemDashboardLayoutBinding =
            ItemDashboardLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = list[position]
        GlideLoader(context).loadProductPicture(
            model.image,
            holder.ivItemImage,
        )
        holder.tvItemPrice.text = "₹${model.price}"
        holder.tvItemTitle.text = model.title

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ProductDetailsActivity::class.java)
            intent.putExtra(Constants.EXTRA_PRODUCT_ID, model.product_id)
            intent.putExtra(Constants.EXTRA_PRODUCT_OWNER_ID, model.user_id)
            context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return list.size
    }

    class MyViewHolder(view: ItemDashboardLayoutBinding) : RecyclerView.ViewHolder(view.root) {
        val ivItemImage = view.ivDashboardItemImage
        val tvItemPrice = view.tvDashboardItemPrice
        val tvItemTitle = view.tvDashboardItemTitle
    }
}