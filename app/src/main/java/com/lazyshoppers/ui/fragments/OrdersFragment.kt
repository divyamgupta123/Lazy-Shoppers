package com.lazyshoppers.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.lazyshoppers.R
import com.lazyshoppers.databinding.FragmentOrdersBinding
import com.lazyshoppers.firestore.FirestoreClass
import com.lazyshoppers.models.Order
import com.lazyshoppers.ui.adapters.MyOrdersListAdapter

class OrdersFragment : BaseFragment() {

    //  private lateinit var notificationsViewModel: NotificationsViewModel
    private var _binding: FragmentOrdersBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentOrdersBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    private fun getMyOrdersList() {
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getMyOrderList(this)
    }

    fun populateOrdersListinUI(list: ArrayList<Order>) {
        hideProgressDialog()

        if (list.size > 0) {
            binding.rvMyOrderItems.visibility = View.VISIBLE
            binding.tvNoOrdersFound.visibility = View.GONE

            binding.rvMyOrderItems.layoutManager = LinearLayoutManager(activity)
            binding.rvMyOrderItems.setHasFixedSize(true)

            val adapter = MyOrdersListAdapter(requireActivity(), list)
            binding.rvMyOrderItems.adapter = adapter
        }else{
            binding.rvMyOrderItems.visibility = View.GONE
            binding.tvNoOrdersFound.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        getMyOrdersList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}