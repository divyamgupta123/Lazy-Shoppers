package com.lazyshoppers.ui.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.lazyshoppers.R
import com.lazyshoppers.databinding.ActivityMyOrdersDetailsBinding
import com.lazyshoppers.models.Order
import com.lazyshoppers.ui.adapters.CartItemsListAdapter
import com.lazyshoppers.utils.Constants
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MyOrdersDetailsActivity : AppCompatActivity() {
    lateinit var binding: ActivityMyOrdersDetailsBinding
    private var mMyOrderDetails: Order? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyOrdersDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpActionBar()

        if (intent.hasExtra(Constants.EXTRA_MY_ORDER_DETAILS)) {
            mMyOrderDetails = intent.getParcelableExtra(Constants.EXTRA_MY_ORDER_DETAILS)!!
        }

        setupUI(mMyOrderDetails!!)
    }

    private fun setUpActionBar() {
        setSupportActionBar(binding.toolbarMyOrderDetailsActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        }
        binding.toolbarMyOrderDetailsActivity.setNavigationOnClickListener { onBackPressed() }
    }


    @SuppressLint("SetTextI18n")
    fun setupUI(orderDetails: Order) {
        binding.tvOrderDetailsId.text = orderDetails.title
        val dateFormat = "dd MMM yyy HH:mm"
        val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = orderDetails.order_datetime
        val orderDateTime = formatter.format(calendar.time)
        binding.tvOrderDetailsDate.text = orderDateTime

        val diffInMilliSeconds: Long = System.currentTimeMillis() - orderDetails.order_datetime
        val diffInHours: Long = TimeUnit.MILLISECONDS.toHours(diffInMilliSeconds)
        Log.e("Difference in Hours", "$diffInHours")

        when {
            diffInHours < 1 -> {
                binding.tvOrderStatus.text = resources.getString(R.string.order_status_pending)
                binding.tvOrderStatus.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.colorAccent
                    )
                )
            }
            diffInHours < 2 -> {
                binding.tvOrderStatus.text = resources.getString(R.string.order_status_in_process)
                binding.tvOrderStatus.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.colorOrderStatusInProcess
                    )
                )
            }
            else -> {
                binding.tvOrderStatus.text = resources.getString(R.string.order_status_delivered)
                binding.tvOrderStatus.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.colorOrderStatusDelivered
                    )
                )
            }
        }

        binding.rvMyOrderItemsList.layoutManager = LinearLayoutManager(this)
        binding.rvMyOrderItemsList.setHasFixedSize(true)

        val cartListAdapter =
            CartItemsListAdapter(this, orderDetails.items, false)
        binding.rvMyOrderItemsList.adapter = cartListAdapter

        binding.tvMyOrderDetailsAddressType.text = orderDetails.address.type
        binding.tvMyOrderDetailsFullName.text = orderDetails.address.name
        binding.tvMyOrderDetailsAddress.text =
            "${orderDetails.address.address}, ${orderDetails.address.zipCode}"
        binding.tvMyOrderDetailsAdditionalNote.text = orderDetails.address.additionalNote

        if (orderDetails.address.otherDetails.isNotEmpty()) {
            binding.tvMyOrderDetailsOtherDetails.visibility = View.VISIBLE
            binding.tvMyOrderDetailsOtherDetails.text = orderDetails.address.otherDetails
        } else {
            binding.tvMyOrderDetailsOtherDetails.visibility = View.GONE
        }
        binding.tvMyOrderDetailsMobileNumber.text = orderDetails.address.mobileNumber

        binding.tvOrderDetailsSubTotal.text = "₹${orderDetails.sub_total_amount}"
        binding.tvOrderDetailsShippingCharge.text = orderDetails.shipping_charges
        binding.tvOrderDetailsTotalAmount.text = "₹${orderDetails.total_amount}"

        binding.paymentMode.text = orderDetails.payment_method

    }
}
