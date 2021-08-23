package com.lazyshoppers.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.lazyshoppers.R
import com.lazyshoppers.databinding.ActivityProductDetailsBinding
import com.lazyshoppers.firestore.FirestoreClass
import com.lazyshoppers.models.CartItem
import com.lazyshoppers.models.Product
import com.lazyshoppers.utils.Constants
import com.lazyshoppers.utils.GlideLoader

class ProductDetailsActivity : BaseActivity(), View.OnClickListener {
    lateinit var binding: ActivityProductDetailsBinding
    private lateinit var mProductDetails: Product

    private var mProductId: String = ""
    private var mProductOwnerId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpActionBar()

        if (intent.hasExtra(Constants.EXTRA_PRODUCT_ID)) {
            mProductId = intent.getStringExtra(Constants.EXTRA_PRODUCT_ID)!!
        }

        if (intent.hasExtra(Constants.EXTRA_PRODUCT_OWNER_ID)) {
            mProductOwnerId = intent.getStringExtra(Constants.EXTRA_PRODUCT_OWNER_ID)!!
        }

        if (FirestoreClass().getCurrentUserId() == mProductOwnerId) {
            binding.btnAddToCart.visibility = View.GONE
            binding.btnGoToCart.visibility = View.GONE
        } else {
            binding.btnAddToCart.visibility = View.VISIBLE
        }

        binding.btnAddToCart.setOnClickListener(this)
        binding.btnGoToCart.setOnClickListener(this)

        getProductDetails()
    }

    private fun setUpActionBar() {
        setSupportActionBar(binding.toolbarProductDetailsActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        }
        binding.toolbarProductDetailsActivity.setNavigationOnClickListener { onBackPressed() }
    }

    fun getProductDetails() {
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getProductDetails(this, mProductId)
    }

    @SuppressLint("SetTextI18n")
    fun productDetailsSuccess(product: Product) {
        mProductDetails = product
        GlideLoader(this).loadUserPicture(
            product.image,
            binding.ivProductDetailImage
        )

        binding.tvProductDetailsTitle.text = product.title
        binding.tvProductDetailsDescription.text = product.description
        binding.tvProductDetailsPrice.text = "\u20B9${product.price}"
        binding.tvProductDetailsAvailableQuantity.text = product.stock_quantity

        if (product.stock_quantity.toInt() == 0) {
            hideProgressDialog()
            binding.btnAddToCart.visibility = View.GONE
            binding.tvProductDetailsAvailableQuantity.text =
                resources.getString(R.string.lbl_out_of_stock)

            binding.tvProductDetailsAvailableQuantity.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.colorSnackBarError
                )
            )
        } else {
            if (FirestoreClass().getCurrentUserId() == product.user_id) {
                hideProgressDialog()
            } else {
                FirestoreClass().checkIfProductExistInCart(this, mProductId)
            }
        }


    }

    fun addToCart() {
        val cartItem = CartItem(
            FirestoreClass().getCurrentUserId(),
            mProductOwnerId,
            mProductId,
            mProductDetails.title,
            mProductDetails.price,
            mProductDetails.image,
            Constants.DEFAULT_CART_QUANTITY
        )
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addCartItems(this, cartItem)
    }

    fun addToCartSuccess() {
        hideProgressDialog()
        Toast.makeText(
            this,
            resources.getString(R.string.success_msg_item_added_to_cart),
            Toast.LENGTH_SHORT
        ).show()

        binding.btnAddToCart.visibility = View.GONE
        binding.btnGoToCart.visibility = View.VISIBLE
    }

    fun productExistInCart() {
        hideProgressDialog()
        binding.btnAddToCart.visibility = View.GONE
        binding.btnGoToCart.visibility = View.VISIBLE
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v) {
                binding.btnAddToCart -> {
                    addToCart()
                }
                binding.btnGoToCart -> {
                    startActivity(Intent(this, CartListActivity::class.java))
                }
            }
        }
    }
}