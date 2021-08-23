package com.lazyshoppers.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.lazyshoppers.R
import com.lazyshoppers.databinding.ActivityCartListBinding
import com.lazyshoppers.firestore.FirestoreClass
import com.lazyshoppers.models.CartItem
import com.lazyshoppers.models.Product
import com.lazyshoppers.ui.adapters.CartItemsListAdapter
import com.lazyshoppers.utils.Constants

class CartListActivity : BaseActivity() {
    lateinit var binding: ActivityCartListBinding
    private lateinit var mProductsList: ArrayList<Product>
    private lateinit var mCartListItems: ArrayList<CartItem>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpActionBar()

        binding.btnCheckout.setOnClickListener {
            val intent = Intent(this, AddressListActivity::class.java)
            intent.putExtra(Constants.EXTRA_SELECT_ADDRESSES, true)
            startActivity(intent)
        }
    }

    private fun setUpActionBar() {
        setSupportActionBar(binding.toolbarCartListActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        }
        binding.toolbarCartListActivity.setNavigationOnClickListener { onBackPressed() }
    }

    private fun getCartItem() {
        FirestoreClass().getCartList(this)
    }

    @SuppressLint("SetTextI18n")
    fun successCartItemsList(cartList: ArrayList<CartItem>) {
        hideProgressDialog()

        for (product in mProductsList) {
            for (cartItem in cartList) {
                if (product.product_id == cartItem.product_id) {
                    cartItem.stock_quantity = product.stock_quantity
                    if (product.stock_quantity.toInt() == 0) {
                        cartItem.cart_quantity = product.stock_quantity
                    }
                }
            }
        }

        mCartListItems = cartList

        if (mCartListItems.size > 0) {
            binding.rvCartItemsList.visibility = View.VISIBLE
            binding.llCheckout.visibility = View.VISIBLE
            binding.tvNoCartItemFound.visibility = View.GONE

            binding.rvCartItemsList.layoutManager = LinearLayoutManager(this)
            binding.rvCartItemsList.setHasFixedSize(true)
            val cartListAdapter = CartItemsListAdapter(this, mCartListItems, true)
            binding.rvCartItemsList.adapter = cartListAdapter
            var subTotal = 0.0
            for (item in mCartListItems) {

                val availableQuantity = item.stock_quantity.toInt()
                if (availableQuantity > 0) {
                    val price = item.price.toDouble()
                    val quantity = item.cart_quantity.toInt()
                    subTotal += (price * quantity)
                }
            }
            binding.tvSubTotal.text = "\u20B9${subTotal}"
            binding.tvShippingCharge.text = "₹10.0"

            if (subTotal > 0) {
                binding.llCheckout.visibility = View.VISIBLE
                val total = subTotal + 10
                binding.tvTotalAmount.text = "₹$total"
            } else {
                binding.llCheckout.visibility = View.GONE
            }
        } else {
            binding.rvCartItemsList.visibility = View.GONE
            binding.llCheckout.visibility = View.GONE
            binding.tvNoCartItemFound.visibility = View.VISIBLE
        }
    }

    private fun getProductsList() {
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getAllProductsList(this)
    }

    fun successProductsListFromFireStore(productsList: ArrayList<Product>) {
        hideProgressDialog()
        mProductsList = productsList
        getCartItem()
    }

    fun itemRemovedSuccess() {
        hideProgressDialog()
        Toast.makeText(
            this,
            resources.getString(R.string.msg_item_removed_successfully),
            Toast.LENGTH_SHORT
        ).show()
        getCartItem()
    }

    fun itemUpdateSuccess() {
        hideProgressDialog()
        getCartItem()
    }

    override fun onResume() {
        super.onResume()
        getProductsList()
    }

}