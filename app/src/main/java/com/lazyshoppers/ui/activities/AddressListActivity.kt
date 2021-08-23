package com.lazyshoppers.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lazyshoppers.R
import com.lazyshoppers.databinding.ActivityAddressListBinding
import com.lazyshoppers.firestore.FirestoreClass
import com.lazyshoppers.models.Address
import com.lazyshoppers.ui.adapters.AddressListAdapter
import com.lazyshoppers.utils.Constants
import com.lazyshoppers.utils.SwipeToDeleteCallback
import com.lazyshoppers.utils.SwipeToEditCallback

class AddressListActivity : BaseActivity() {
    lateinit var binding: ActivityAddressListBinding

    private var mSelectedAddress: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddressListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpActionBar()
        getAddressList()
        if (intent.hasExtra(Constants.EXTRA_SELECT_ADDRESSES)) {
            mSelectedAddress = intent.getBooleanExtra(Constants.EXTRA_SELECT_ADDRESSES, false)
        }

        if (mSelectedAddress) {
            binding.tvTitle.text = resources.getString(R.string.title_select_address)
        }

        binding.tvAddAddress.setOnClickListener {
            startActivityForResult(
                Intent(this, AddEditAddressActivity::class.java),
                Constants.ADD_ADDRESSES_REQUEST_CODE
            )
        }
    }

    private fun setUpActionBar() {
        setSupportActionBar(binding.toolbarAddressListActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        }
        binding.toolbarAddressListActivity.setNavigationOnClickListener { onBackPressed() }
    }

    fun successAddressListFromFireStore(addressList: ArrayList<Address>) {
        hideProgressDialog()
        if (addressList.size > 0) {
            binding.rvAddressList.visibility = View.VISIBLE
            binding.tvNoAddressFound.visibility = View.GONE

            binding.rvAddressList.layoutManager = LinearLayoutManager(this)
            binding.rvAddressList.setHasFixedSize(true)
            val adapter = AddressListAdapter(this, addressList, mSelectedAddress)
            binding.rvAddressList.adapter = adapter

            if (!mSelectedAddress) {

                val editSwipeHandler = object : SwipeToEditCallback(this) {
                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                        val adapter = binding.rvAddressList.adapter as AddressListAdapter
                        adapter.notifyEditItem(
                            this@AddressListActivity,
                            viewHolder.adapterPosition
                        )
                    }
                }
                val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
                editItemTouchHelper.attachToRecyclerView(binding.rvAddressList)

                val deleteSwipeHandler = object : SwipeToDeleteCallback(this) {
                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                        showProgressDialog(resources.getString(R.string.please_wait))
                        FirestoreClass().deleteAddress(
                            this@AddressListActivity,
                            addressList[viewHolder.adapterPosition].id
                        )
                    }
                }
                val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
                deleteItemTouchHelper.attachToRecyclerView(binding.rvAddressList)
            }


        } else {
            binding.rvAddressList.visibility = View.VISIBLE
            binding.tvNoAddressFound.visibility = View.GONE
        }
    }

    private fun getAddressList() {
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getAddressList(this)
    }

    fun deleteAddressSuccess() {
        hideProgressDialog()
        Toast.makeText(
            this,
            resources.getString(R.string.err_your_address_deleted_successfully),
            Toast.LENGTH_SHORT
        ).show()
        getAddressList()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            getAddressList()
        }
    }

}