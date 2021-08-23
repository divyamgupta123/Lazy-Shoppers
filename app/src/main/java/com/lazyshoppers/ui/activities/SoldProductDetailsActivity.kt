package com.lazyshoppers.ui.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.lazyshoppers.R
import com.lazyshoppers.databinding.ActivitySoldProductDetailsBinding
import com.lazyshoppers.models.SoldProduct
import com.lazyshoppers.utils.Constants
import com.lazyshoppers.utils.GlideLoader
import java.text.SimpleDateFormat
import java.util.*

class SoldProductDetailsActivity : AppCompatActivity() {
    lateinit var binding: ActivitySoldProductDetailsBinding
    private var mSoldProduct: SoldProduct? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySoldProductDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpActionBar()
        if (intent.hasExtra(Constants.EXTRA_SOLD_PRODUCT_DETAILS)) {
            mSoldProduct = intent.getParcelableExtra(Constants.EXTRA_SOLD_PRODUCT_DETAILS)!!
        }
        setupUI(mSoldProduct!!)
    }

    private fun setUpActionBar() {
        setSupportActionBar(binding.toolbarSoldProductDetailsActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        }
        binding.toolbarSoldProductDetailsActivity.setNavigationOnClickListener { onBackPressed() }
    }

    @SuppressLint("SetTextI18n")
    private fun setupUI(productDetails: SoldProduct) {

        binding.tvOrderDetailsId.text = productDetails.order_id

        val dateFormat = "dd MMM yyyy HH:mm"
        val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())

        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = productDetails.order_date
        binding.tvOrderDetailsDate.text = formatter.format(calendar.time)

        GlideLoader(this@SoldProductDetailsActivity).loadProductPicture(
            productDetails.image,
            binding.ivProductItemImage
        )
        binding.tvProductItemName.text = productDetails.title
        binding.tvProductItemPrice.text = "₹${productDetails.price}"
        binding.tvSoldProductQuantity.text = productDetails.sold_quantity

        binding.tvSoldDetailsAddressType.text = productDetails.address.type
        binding.tvSoldDetailsFullName.text = productDetails.address.name
        binding.tvSoldDetailsAddress.text =
            "${productDetails.address.address}, ${productDetails.address.zipCode}"
        binding.tvSoldDetailsAdditionalNote.text = productDetails.address.additionalNote

        if (productDetails.address.otherDetails.isNotEmpty()) {
            binding.tvSoldDetailsOtherDetails.visibility = View.VISIBLE
            binding.tvSoldDetailsOtherDetails.text = productDetails.address.otherDetails
        } else {
            binding.tvSoldDetailsOtherDetails.visibility = View.GONE
        }
        binding.tvSoldDetailsMobileNumber.text = productDetails.address.mobileNumber

        binding.tvSoldProductSubTotal.text = "₹${productDetails.sub_total_amount}"
        binding.tvSoldProductShippingCharge.text = productDetails.shipping_charge
        binding.tvSoldProductTotalAmount.text = "₹${productDetails.total_amount}"
    }
}