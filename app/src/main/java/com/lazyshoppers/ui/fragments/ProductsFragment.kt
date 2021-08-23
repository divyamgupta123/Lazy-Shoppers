package com.lazyshoppers.ui.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.lazyshoppers.R
import com.lazyshoppers.databinding.FragmentProductsBinding
import com.lazyshoppers.firestore.FirestoreClass
import com.lazyshoppers.models.Product
import com.lazyshoppers.ui.activities.AddProductActivity
import com.lazyshoppers.ui.adapters.MyProductsListAdapter

class ProductsFragment : BaseFragment() {
    private var _binding: FragmentProductsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentProductsBinding.inflate(inflater, container, false)


        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add_product_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add_product -> {
                startActivity(Intent(activity, AddProductActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getProductListFromFireStore() {
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getProductDetails(this)
    }


    fun successProductsListFromFireStore(productList: ArrayList<Product>) {
        hideProgressDialog()
        if (productList.size > 0) {
            binding.rvMyProductItems.visibility = View.VISIBLE
            binding.tvNoProductsFound.visibility = View.GONE
            binding.rvMyProductItems.layoutManager = LinearLayoutManager(activity)
            binding.rvMyProductItems.setHasFixedSize(true)
            val adapterProducts = MyProductsListAdapter(requireActivity(), productList, this)
            binding.rvMyProductItems.adapter = adapterProducts
        } else {
            binding.rvMyProductItems.visibility = View.GONE
            binding.tvNoProductsFound.visibility = View.VISIBLE
        }
    }

    fun deleteProduct(productID: String) {
        showAlertDialogToDeleteProduct(productID)
    }

    fun productDeleteSuccess() {
        hideProgressDialog()
        Toast.makeText(
            requireActivity(),
            resources.getString(R.string.product_delete_success_message),
            Toast.LENGTH_SHORT
        ).show()
        getProductListFromFireStore()
    }

    private fun showAlertDialogToDeleteProduct(productID: String) {

        val builder = AlertDialog.Builder(requireActivity())
        //set title for alert dialog
        builder.setTitle(resources.getString(R.string.delete_dialog_title))
        //set message for alert dialog
        builder.setMessage(resources.getString(R.string.delete_dialog_message))
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        //performing positive action
        builder.setPositiveButton(resources.getString(R.string.yes)) { dialogInterface, _ ->

            // Show the progress dialog.
            showProgressDialog(resources.getString(R.string.please_wait))

            // Call the function of Firestore class.
            FirestoreClass().deleteProduct(this@ProductsFragment, productID)

            dialogInterface.dismiss()
        }

        //performing negative action
        builder.setNegativeButton(resources.getString(R.string.no)) { dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    override fun onResume() {
        super.onResume()
        getProductListFromFireStore()
    }
}