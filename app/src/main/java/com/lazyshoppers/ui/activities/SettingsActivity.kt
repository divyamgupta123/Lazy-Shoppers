package com.lazyshoppers.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.lazyshoppers.R
import com.lazyshoppers.databinding.ActivitySettingsBinding
import com.lazyshoppers.firestore.FirestoreClass
import com.lazyshoppers.models.User
import com.lazyshoppers.utils.Constants
import com.lazyshoppers.utils.GlideLoader

class SettingsActivity : BaseActivity(), View.OnClickListener {
    lateinit var binding: ActivitySettingsBinding
    private lateinit var mUserDetails: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpActionBar()
        binding.btnLogout.setOnClickListener(this)
        binding.tvEdit.setOnClickListener(this)
        binding.llAddress.setOnClickListener(this)
    }

    private fun setUpActionBar() {
        setSupportActionBar(binding.toolbarSettingsActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        }
        binding.toolbarSettingsActivity.setNavigationOnClickListener { onBackPressed() }
    }

    private fun getUserDetails() {
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getUserDetails(this)
    }

    @SuppressLint("SetTextI18n")
    fun userDetailsSuccess(user: User) {
        mUserDetails = user
        hideProgressDialog()
        GlideLoader(this).loadUserPicture(user.image, binding.ivUserPhoto)

        binding.tvTitle.text = "${user.firstName} ${user.lastName}"
        binding.tvGender.text = user.gender
        binding.tvEmail.text = user.email
        binding.tvMobileNumber.text = user.mobile.toString()
    }

    override fun onResume() {
        super.onResume()
        getUserDetails()
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                R.id.btn_logout -> {
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                R.id.tv_edit -> {
                    val intent = Intent(this, UserProfileActivity::class.java)
                    intent.putExtra(Constants.EXTRA_USER_DETAILS, mUserDetails)
                    startActivity(intent)
                }
                R.id.ll_address -> {
                    startActivity(Intent(this, AddressListActivity::class.java))
                }
            }
        }
    }
}