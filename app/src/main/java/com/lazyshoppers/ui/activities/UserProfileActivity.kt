package com.lazyshoppers.ui.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.lazyshoppers.R
import com.lazyshoppers.databinding.ActivityUserProfileBinding
import com.lazyshoppers.firestore.FirestoreClass
import com.lazyshoppers.models.User
import com.lazyshoppers.utils.Constants
import com.lazyshoppers.utils.GlideLoader
import java.io.IOException

class UserProfileActivity : BaseActivity(), View.OnClickListener {
    lateinit var binding: ActivityUserProfileBinding
    lateinit var mUserDetails: User

    private var mSelectedImageFileUri: Uri? = null
    private var mUserProfileImageUrL: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent.hasExtra(Constants.EXTRA_USER_DETAILS)) {
            mUserDetails = intent.getParcelableExtra(Constants.EXTRA_USER_DETAILS)!!
        }

        binding.etFirstName.setText(mUserDetails.firstName)
        binding.etLastName.setText(mUserDetails.lastName)
        binding.etEmail.isEnabled = false
        binding.etEmail.setText(mUserDetails.email)

        if(mUserDetails.profileCompleted==0){
            binding.tvTitle.text = resources.getString(R.string.title_complete_profile)
            binding.etFirstName.isEnabled = false

            binding.etLastName.isEnabled = false


        }else{
            setUpActionBar()
            binding.tvTitle.text = resources.getString(R.string.title_edit_profile)
            GlideLoader(this).loadUserPicture(mUserDetails.image,binding.ivUserPhoto)

            if(mUserDetails.mobile!=0L){
                binding.etMobileNumber.setText(mUserDetails.mobile.toString())
            }
            if (mUserDetails.gender==Constants.MALE){
                binding.rbMale.isChecked = true
            }else{
                binding.rbFemale.isChecked = true
            }
        }



        binding.ivUserPhoto.setOnClickListener(this)
        binding.btnSave.setOnClickListener(this)
    }
    private fun setUpActionBar() {
        setSupportActionBar(binding.toolbarUserProfileActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        }
        binding.toolbarUserProfileActivity.setNavigationOnClickListener { onBackPressed() }
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                R.id.iv_user_photo -> {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        Constants.showImageChoser(this@UserProfileActivity)
                    } else {
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                            Constants.READ_STORAGE_PERMISSION_CODE
                        )
                    }
                }
                R.id.btn_save -> {
                    if (validateUderProfileDetails()) {
                        showProgressDialog(resources.getString(R.string.please_wait))
                        if (mSelectedImageFileUri != null) {
                            FirestoreClass().uploadImageToCloudStorage(this, mSelectedImageFileUri,Constants.USER_PROFILE_IMAGE)
                        } else {
                            updateUserProfileDetails()
                        }

                    }
                }
            }
        }
    }

    fun updateUserProfileDetails() {
        val userHashMap = HashMap<String, Any>()
        val mobileNumber = binding.etMobileNumber.text.toString().trim { it <= ' ' }
        val firstName = binding.etFirstName.text.toString().trim { it <= ' ' }
        val lastName = binding.etLastName.text.toString().trim { it <= ' ' }

        if(firstName!=mUserDetails.firstName){
            userHashMap[Constants.FIRST_NAME] = firstName
        }
        if(lastName!=mUserDetails.lastName){
            userHashMap[Constants.LAST_NAME] = lastName
        }

        val gender = if (binding.rbMale.isChecked) {
            Constants.MALE
        } else {
            Constants.FEMALE
        }
        if (mobileNumber.isNotEmpty()&& mobileNumber!=mUserDetails.mobile.toString()) {
            userHashMap[Constants.MOBILE] = mobileNumber.toLong()
        }
        if (gender.isNotEmpty()&& gender!=mUserDetails.gender) {
            userHashMap[Constants.GENDER] = gender
        }

        if (mUserProfileImageUrL.isNotEmpty()) {
            userHashMap[Constants.IMAGE] = mUserProfileImageUrL
        }

        userHashMap[Constants.GENDER] = gender
        userHashMap[Constants.COMPLETE_PROFILE] = 1

        FirestoreClass().updateUserProfileDetails(this, userHashMap)
    }


    fun userProfileUpdateSuccess() {
        hideProgressDialog()
        Toast.makeText(
            this@UserProfileActivity,
            resources.getString(R.string.msg_profile_update_success),
            Toast.LENGTH_SHORT
        ).show()
        startActivity(Intent(this, DashboardActivity::class.java))
        finish()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE) {
            //If permission is granted
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Constants.showImageChoser(this@UserProfileActivity)
            } else {
                //Displaying another toast if permission is not granted
                Toast.makeText(
                    this,
                    resources.getString(R.string.read_storage_permission_denied),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Constants.IMAGE_REQUEST_CODE) {
                if (data != null) {
                    try {
                        mSelectedImageFileUri = data.data!!

                        GlideLoader(this@UserProfileActivity).loadUserPicture(
                            mSelectedImageFileUri!!,
                            binding.ivUserPhoto
                        )

                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(
                            this@UserProfileActivity,
                            resources.getString(R.string.image_selection_failed),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            // A log is printed when user close or cancel the image selection.
            Log.e("Request Cancelled", "Image selection cancelled")
        }
    }


    private fun validateUderProfileDetails(): Boolean {
        return when {
            TextUtils.isEmpty(binding.etMobileNumber.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_mobile_number), true)
                false
            }
            else -> {
                true
            }
        }
    }

    fun imageUploadSuccess(imageURL: String) {
        mUserProfileImageUrL = imageURL
        updateUserProfileDetails()
    }

}