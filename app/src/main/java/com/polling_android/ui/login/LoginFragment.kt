package com.polling_android.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.polling_android.databinding.FragmentLoginBinding
import com.polling_android.api.RetrofitInstance
import com.polling_android.model.PollingOrder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Spinner

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var spinner: Spinner

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val root: View = binding.root

        spinner = binding.pollingOrdersSpinner
        fetchPollingOrders()

        return root
    }

    private fun fetchPollingOrders() {
        RetrofitInstance.api.getPollingOrders().enqueue(object : Callback<List<PollingOrder>> {
            override fun onResponse(call: Call<List<PollingOrder>>, response: Response<List<PollingOrder>>) {
                if (response.isSuccessful) {
                    val pollingOrders = response.body()
                    val pollingOrderNames = pollingOrders?.map { it.polling_order_name } ?: emptyList()

                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, pollingOrderNames.sorted())
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinner.adapter = adapter

                    Log.d("LoginFragment", "Fetched ${pollingOrders} orders")
                    Toast.makeText(context, "Fetched ${pollingOrders?.size} orders", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<PollingOrder>>, t: Throwable) {
                Toast.makeText(context, "Failed to fetch polling orders", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}