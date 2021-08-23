package com.lazyshoppers.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.recyclerview.widget.GridLayoutManager
import com.lazyshoppers.R
import com.lazyshoppers.databinding.FragmentDashboardBinding
import com.lazyshoppers.firestore.FirestoreClass
import com.lazyshoppers.models.Product
import com.lazyshoppers.ui.activities.CartListActivity
import com.lazyshoppers.ui.activities.SettingsActivity
import com.lazyshoppers.ui.adapters.DashboardItemsListAdapter

class DashboardFragment : BaseFragment() {

    //  private lateinit var dashboardViewModel: DashboardViewModel
    private var _binding: FragmentDashboardBinding? = null

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
//    dashboardViewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root


        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.dashboard_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id = item.itemId
        when (id) {
            R.id.action_settings -> {
                startActivity(Intent(activity, SettingsActivity::class.java))
                return true
            }
            R.id.action_go_to_cart->{
                startActivity(Intent(activity, CartListActivity::class.java))
                return true
            }
        }


        return super.onOptionsItemSelected(item)
    }


    private fun getDashboardItemsList() {
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getDashboardItemsList(this)
    }

    fun successDashBoardItemsList(dashboardItemsList: ArrayList<Product>) {
        hideProgressDialog()
        if (dashboardItemsList.size > 0) {
            binding.rvDashboardItems.visibility = View.VISIBLE
            binding.tvNoDashboardItemsFound.visibility = View.GONE
            binding.rvDashboardItems.layoutManager = GridLayoutManager(activity, 2)
            binding.rvDashboardItems.setHasFixedSize(true)
            val adapter = DashboardItemsListAdapter(requireActivity(), dashboardItemsList)
            binding.rvDashboardItems.adapter = adapter
        } else {
            binding.rvDashboardItems.visibility = View.GONE
            binding.tvNoDashboardItemsFound.visibility = View.VISIBLE
        }

    }

    override fun onResume() {
        super.onResume()
        getDashboardItemsList()
    }
}