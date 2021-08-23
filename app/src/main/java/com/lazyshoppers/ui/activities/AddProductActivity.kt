package com.lazyshoppers.ui.activities

import android.Manifest
import android.app.Activity
import android.content.Context
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
import com.lazyshoppers.databinding.ActivityAddProductBinding
import com.lazyshoppers.firestore.FirestoreClass
import com.lazyshoppers.models.Product
import com.lazyshoppers.utils.Constants
import com.lazyshoppers.utils.GlideLoader
import java.io.IOException

class AddProductActivity : BaseActivity(), View.OnClickListener {

    private var mSelectedImageFileUri: Uri? = null
    private var mProductImageURL: String = ""

    lateinit var binding: ActivityAddProductBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpActionBar()
        binding.ivAddUpdateProduct.setOnClickListener(this)
        binding.btnSubmit.setOnClickListener(this)
    }

    private fun setUpActionBar() {
        setSupportActionBar(binding.toolbarAddProductActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        }
        binding.toolbarAddProductActivity.setNavigationOnClickListener { onBackPressed() }
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v) {
                binding.ivAddUpdateProduct -> {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        Constants.showImageChoser(this)
                    } else {
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                            Constants.READ_STORAGE_PERMISSION_CODE
                        )
                    }
                }
                binding.btnSubmit -> {
                    if (validateProductDetails()) {
                        uploadProductImage()
                    }
                }
            }
        }
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
                Constants.showImageChoser(this)
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
                    binding.ivAddUpdateProduct.setImageDrawable(
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.ic_vector_edit
                        )
                    )
                    try {
                        mSelectedImageFileUri = data.data!!

                        GlideLoader(this).loadUserPicture(
                            mSelectedImageFileUri!!,
                            binding.ivProductImage
                        )

                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(
                            this,
                            resources.getString(R.string.image_selection_failed),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Log.e("Request Cancelled", "Image selection cancelled")
        }
    }

    private fun validateProductDetails(): Boolean {
        return when {
            mSelectedImageFileUri == null -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_select_product_image), true)
                false
            }
            TextUtils.isEmpty(binding.etProductTitle.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_product_title), true)
                false
            }
            TextUtils.isEmpty(binding.etProductPrice.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_product_price), true)
                false
            }
            TextUtils.isEmpty(binding.etProductDescription.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(
                    resources.getString(R.string.err_msg_enter_product_description),
                    true
                )
                false
            }
            else -> {
                true
            }
        }
    }


    private fun uploadProductImage() {

        Log.e("AddProductAct","uploadProductImage called")

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().uploadImageToCloudStorage(
            this,
            mSelectedImageFileUri,
            Constants.PRODUCT_IMAGE
        )
    }


    fun imageUploadSuccess(imageURL: String) {
        Log.e("AddProductAct","image Upload Success called")

        mProductImageURL = imageURL
        uploadProductDetails()
    }

    private fun uploadProductDetails() {
        Log.e("AddProductAct","uploadProductDetails called")

        val username = this.getSharedPreferences(
            Constants.LS_PREFERENCE,
            Context.MODE_PRIVATE
        ).getString(Constants.LOGGED_IN_USERNAME, "")!!

        val product = Product(
            FirestoreClass().getCurrentUserId(),
            username,
            binding.etProductTitle.text.toString().trim { it <= ' ' },
            binding.etProductPrice.text.toString().trim { it <= ' ' },
            binding.etProductDescription.text.toString().trim { it <= ' ' },
            binding.etProductQuantity.text.toString().trim { it <= ' ' },
            mProductImageURL
        )
        FirestoreClass().uploadProductDetails(this, product)
    }


    fun productUploadSuccess() {
        Log.e("AddProductAct","uploadProductSuccess called")

        hideProgressDialog()
        Toast.makeText(this, "Product Uploaded", Toast.LENGTH_SHORT).show()
        finish()
    }
}