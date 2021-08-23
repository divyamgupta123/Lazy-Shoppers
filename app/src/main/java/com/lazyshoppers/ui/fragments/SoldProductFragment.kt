package com.lazyshoppers.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.lazyshoppers.R
import com.lazyshoppers.databinding.FragmentSoldProductBinding
import com.lazyshoppers.firestore.FirestoreClass
import com.lazyshoppers.models.SoldProduct
import com.lazyshoppers.ui.adapters.SoldProductsListAdapter


class SoldProductFragment : BaseFragment() {
    private var _binding: FragmentSoldProductBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        // Inflate the layout for this fragment

        _binding = FragmentSoldProductBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    fun getSoldProductsList(){
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getSoldProductsList(this)
    }

    fun successSoldProductList(soldProductsList:ArrayList<SoldProduct>){
        hideProgressDialog()
        if(soldProductsList.size>0){
            binding.rvSoldProductItems.visibility = View.VISIBLE
            binding.tvNoSoldProductsFound.visibility = View.GONE

            binding.rvSoldProductItems.layoutManager = LinearLayoutManager(activity)
            binding.rvSoldProductItems.setHasFixedSize(true)

            val soldProductsListAdapter =
                SoldProductsListAdapter(requireActivity(), soldProductsList)
            binding.rvSoldProductItems.adapter = soldProductsListAdapter
        } else {
            binding.rvSoldProductItems.visibility = View.GONE
            binding.tvNoSoldProductsFound.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        getSoldProductsList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}