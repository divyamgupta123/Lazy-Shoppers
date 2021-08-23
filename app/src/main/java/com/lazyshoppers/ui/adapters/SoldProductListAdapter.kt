package com.lazyshoppers.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lazyshoppers.databinding.ItemListLayoutBinding
import com.lazyshoppers.models.SoldProduct
import com.lazyshoppers.ui.activities.SoldProductDetailsActivity
import com.lazyshoppers.utils.Constants
import com.lazyshoppers.utils.GlideLoader

open class SoldProductsListAdapter(
    private val context: Context,
    private var list: ArrayList<SoldProduct>
) : RecyclerView.Adapter<SoldProductsListAdapter.MyViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding: ItemListLayoutBinding =
            ItemListLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = list[position]

        GlideLoader(context).loadProductPicture(
            model.image,
            holder.ivItemImage
        )

        holder.tvItemName.text = model.title
        holder.tvItemPrice.text = "₹${model.price}"

        holder.ibDeleteItem.visibility = View.GONE

        holder.itemView.setOnClickListener {
            val intent = Intent(context, SoldProductDetailsActivity::class.java)
            intent.putExtra(Constants.EXTRA_SOLD_PRODUCT_DETAILS, model)
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