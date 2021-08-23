package com.lazyshoppers.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.lazyshoppers.R
import com.lazyshoppers.databinding.ItemCartLayoutBinding
import com.lazyshoppers.firestore.FirestoreClass
import com.lazyshoppers.models.CartItem
import com.lazyshoppers.ui.activities.CartListActivity
import com.lazyshoppers.utils.Constants
import com.lazyshoppers.utils.GlideLoader

open class CartItemsListAdapter(
    private val context: Context,
    private var list: ArrayList<CartItem>,
    private val updateCartItems: Boolean
) : RecyclerView.Adapter<CartItemsListAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding: ItemCartLayoutBinding =
            ItemCartLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = list[position]
        GlideLoader(context).loadUserPicture(model.image, holder.ivCartImage)
        holder.tvCartTitle.text = model.title
        holder.tvCartPrice.text = "₹${model.price}"
        holder.tvCartQuantity.text = model.cart_quantity
        if (model.cart_quantity == "0") {
            holder.ibRemoveCartItem.visibility = View.GONE
            holder.ibAddCartItem.visibility = View.GONE

            if (updateCartItems) {
                holder.ibDeleteCartItem.visibility = View.VISIBLE
            } else {
                holder.ibDeleteCartItem.visibility = View.GONE
            }

            holder.tvCartQuantity.text =
                context.resources.getString(R.string.lbl_out_of_stock)

            holder.tvCartQuantity.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.colorSnackBarError
                )
            )
        } else {
            if (updateCartItems) {
                holder.ibRemoveCartItem.visibility = View.VISIBLE
                holder.ibAddCartItem.visibility = View.VISIBLE
                holder.ibDeleteCartItem.visibility = View.VISIBLE
            } else {
                holder.ibRemoveCartItem.visibility = View.GONE
                holder.ibAddCartItem.visibility = View.GONE
                holder.ibDeleteCartItem.visibility = View.GONE
            }

            holder.tvCartQuantity.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.colorSecondaryText
                )
            )
        }

        holder.ibDeleteCartItem.setOnClickListener {
            when (context) {
                is CartListActivity -> {
                    context.showProgressDialog(context.resources.getString(R.string.please_wait))
                }
            }
            FirestoreClass().removeItemFromCart(context, model.id)
        }

        holder.ibRemoveCartItem.setOnClickListener {
            if (model.cart_quantity.toInt() == 1) {
                FirestoreClass().removeItemFromCart(context, model.id)
            } else {

                val cartQuantity: Int = model.cart_quantity.toInt()

                val itemHashMap = HashMap<String, Any>()

                itemHashMap[Constants.CART_QUANTITY] = (cartQuantity - 1).toString()

                if (context is CartListActivity) {
                    context.showProgressDialog(context.resources.getString(R.string.please_wait))
                }

                FirestoreClass().updateMyCart(context, model.id, itemHashMap)
            }
        }
        holder.ibAddCartItem.setOnClickListener {
            val cartQuantity: Int = model.cart_quantity.toInt()

            if (cartQuantity < model.stock_quantity.toInt()) {

                val itemHashMap = HashMap<String, Any>()

                itemHashMap[Constants.CART_QUANTITY] = (cartQuantity + 1).toString()

                if (context is CartListActivity) {
                    context.showProgressDialog(context.resources.getString(R.string.please_wait))
                }

                FirestoreClass().updateMyCart(context, model.id, itemHashMap)
            } else {
                if (context is CartListActivity) {
                    context.showErrorSnackBar(
                        context.resources.getString(
                            R.string.msg_for_available_stock,
                            model.stock_quantity
                        ),
                        true
                    )
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class MyViewHolder(view: ItemCartLayoutBinding) : RecyclerView.ViewHolder(view.root) {
        val ivCartImage = view.ivCartItemImage
        val tvCartTitle = view.tvCartItemTitle
        val tvCartPrice = view.tvCartItemPrice
        val tvCartQuantity = view.tvCartQuantity
        val ibRemoveCartItem = view.ibRemoveCartItem
        val ibAddCartItem = view.ibAddCartItem
        val ibDeleteCartItem = view.ibDeleteCartItem
    }
}