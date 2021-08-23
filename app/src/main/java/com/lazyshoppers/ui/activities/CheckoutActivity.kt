package com.lazyshoppers.ui.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.lazyshoppers.R
import com.lazyshoppers.databinding.ActivityCheckoutBinding
import com.lazyshoppers.firestore.FirestoreClass
import com.lazyshoppers.models.Address
import com.lazyshoppers.models.CartItem
import com.lazyshoppers.models.Order
import com.lazyshoppers.models.Product
import com.lazyshoppers.ui.adapters.CartItemsListAdapter
import com.lazyshoppers.utils.Constants
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject

class CheckoutActivity : BaseActivity(), PaymentResultListener, AdapterView.OnItemSelectedListener {
    lateinit var binding: ActivityCheckoutBinding
    lateinit var mProductsList: ArrayList<Product>
    lateinit var mCartItemsList: ArrayList<CartItem>
    lateinit var mOrderDetails: Order
    private var mAddressDetails: Address? = null
    private var mSubTotal: Double = 0.0
    private var mTotalAmount: Double = 0.0

    private var paymentMethod: String = "Cash on Delivery"

    var payment_methods = arrayOf<String>("Cash on Delivery", "Online Payment")

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpActionBar()

        if (intent.hasExtra(Constants.EXTRA_CHECKOUT_ADDRESS)) {
            mAddressDetails = intent.getParcelableExtra(Constants.EXTRA_CHECKOUT_ADDRESS)
        }
        if (mAddressDetails != null) {
            binding.tvCheckoutAddressType.text = mAddressDetails?.type
            binding.tvCheckoutFullName.text = mAddressDetails?.name
            binding.tvCheckoutAddress.text =
                "${mAddressDetails?.address}, ${mAddressDetails?.zipCode}"
            binding.tvCheckoutAdditionalNote.text = mAddressDetails?.additionalNote

            if (mAddressDetails?.otherDetails!!.isNotEmpty()) {
                binding.tvCheckoutOtherDetails.text = mAddressDetails?.otherDetails
            }

            binding.tvMobileNumber.text = mAddressDetails?.mobileNumber
        }

        getProductsList()

        binding.btnPlaceOrder.setOnClickListener {
            placeAnOrder()
        }


        // Take the instance of Spinner and
        // apply OnItemSelectedListener on it which
        // tells which item of spinner is clicked
        val spin = binding.paymentMode
        spin.onItemSelectedListener = this

        // Create the instance of ArrayAdapter
        // having the list of courses
        val ad: ArrayAdapter<*> = ArrayAdapter<Any?>(
            this,
            android.R.layout.simple_spinner_item,
            payment_methods
        )

        // set simple layout resource file
        // for each item of spinner
        ad.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        // Set the ArrayAdapter (ad) data on the
        // Spinner which binds data to spinner
        spin.adapter = ad

    }

    private fun setUpActionBar() {
        setSupportActionBar(binding.toolbarCheckoutActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        }
        binding.toolbarCheckoutActivity.setNavigationOnClickListener { onBackPressed() }
    }

    private fun getProductsList() {
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getAllProductsList(this)
    }

    fun successProductListFromFirestore(productList: ArrayList<Product>) {
        mProductsList = productList
        getCartItemsList()
    }

    fun getCartItemsList() {
        FirestoreClass().getCartList(this)
    }

    @SuppressLint("SetTextI18n")
    fun successCartItemList(cartItems: ArrayList<CartItem>) {
        hideProgressDialog()

        for (product in mProductsList) {
            for (cartItem in cartItems) {
                if (product.product_id == cartItem.product_id) {
                    cartItem.stock_quantity = product.stock_quantity
                }
            }
        }

        mCartItemsList = cartItems

        binding.rvCartListItems.layoutManager = LinearLayoutManager(this)
        binding.rvCartListItems.setHasFixedSize(true)

        val cartListAdapter = CartItemsListAdapter(this, mCartItemsList, false)
        binding.rvCartListItems.adapter = cartListAdapter

        for (item in mCartItemsList) {
            val availableQuantity = item.stock_quantity.toInt()
            if (availableQuantity > 0) {
                val price = item.price.toDouble()
                val quantity = item.cart_quantity.toInt()
                mSubTotal += (price * quantity)
            }
        }

        binding.tvCheckoutSubTotal.text = "₹$mSubTotal"
        binding.tvCheckoutShippingCharge.text = "₹10.0"

        if (mSubTotal > 0) {
            binding.llCheckoutPlaceOrder.visibility = View.VISIBLE
            mTotalAmount = mSubTotal + 10.0
            binding.tvCheckoutTotalAmount.text = "₹$mTotalAmount"
        } else {
            binding.llCheckoutPlaceOrder.visibility = View.GONE
        }
    }

    fun placeAnOrder() {
        showProgressDialog(resources.getString(R.string.please_wait))
        mOrderDetails = Order(
            FirestoreClass().getCurrentUserId(),
            mCartItemsList,
            mAddressDetails!!,
            "My Order ${System.currentTimeMillis()}",
            mCartItemsList[0].image,
            mSubTotal.toString(),
            "₹10.0",
            mTotalAmount.toString(),
            System.currentTimeMillis(),
            paymentMethod
        )
        FirestoreClass().placeOrder(this, mOrderDetails)
    }

    fun orderPlacedSuccess() {
        FirestoreClass().updateAllDetails(this, mCartItemsList, mOrderDetails)
    }

    fun allDetailsSuccess() {
        hideProgressDialog()
        Toast.makeText(this, "Order Placed Successfully", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onPaymentSuccess(s: String) {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Payment Id")
        alertDialog.setMessage(s)
        alertDialog.show()
        binding.tvPaymentStatus.text = "Payment Done"
        binding.tvPaymentStatus.setTextColor(Color.parseColor("#7CFC00"))
        binding.paymentMode.isEnabled = false
        binding.paymentMode.isClickable = false

        binding.btnPlaceOrder.performClick()
    }

    override fun onPaymentError(p0: Int, s: String) {
        Toast.makeText(this, "Some Error Occurred", Toast.LENGTH_SHORT).show()
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (position) {
            0 -> {
                paymentMethod = payment_methods[0]
                binding.tvPaymentStatus.text = "Payment Pending"
                binding.tvPaymentStatus.setTextColor(Color.parseColor("#DC143C"))
            }
            1 -> {
                paymentMethod = payment_methods[1]
                val activity: Activity = this
                val checkout: Checkout = Checkout()
                checkout.setKeyID("rzp_test_zPdl7HeEyFaebv")

                try {
                    val options = JSONObject()
                    options.put("name", "Lazy Shoppers")
                    options.put("description", "Online Payment")
                    //You can omit the image option to fetch the image from dashboard
                    options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png")
                    options.put("theme.color", "#3399cc")
                    options.put("currency", "INR")
                    options.put("amount", (mTotalAmount * 100).toString())

                    checkout.open(activity, options)
                } catch (e: Exception) {
                    Toast.makeText(activity, "Error in payment",Toast.LENGTH_SHORT)
                        .show()
                    e.printStackTrace()
                }
            }
        }
    }
    override fun onNothingSelected(parent: AdapterView<*>?) {}
}