package com.polling_android.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.polling_android.databinding.FragmentLoginBinding
import com.polling_android.api.RetrofitInstance
import com.polling_android.model.PollingOrder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.polling_android.R

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var spinner: Spinner
    private lateinit var loginHandler: LoginHandler
    private var pollingOrders: List<PollingOrder> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val root: View = binding.root

        loginHandler = LoginHandler(requireContext())

        spinner = binding.pollingOrdersSpinner
        fetchPollingOrders()
        binding.loginButton.setOnClickListener {
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()
            val selectedPollingOrder = spinner.selectedItem as PollingOrder
            val pollingOrderId = selectedPollingOrder.polling_order_id
            toggleLoading(true)
            loginHandler.handleLogin(email, password, pollingOrderId) { logSuccess ->
                if (logSuccess) {
                    findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                }
                else {
                    toggleLoading(false)
                }
            }
        }

        return root
    }

    private fun fetchPollingOrders() {
        RetrofitInstance.api.getPollingOrders().enqueue(object : Callback<List<PollingOrder>> {
            override fun onResponse(call: Call<List<PollingOrder>>, response: Response<List<PollingOrder>>) {
                if (response.isSuccessful) {
                    pollingOrders = response.body() ?: emptyList()
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, pollingOrders)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinner.adapter = adapter
                }
            }

            override fun onFailure(call: Call<List<PollingOrder>>, t: Throwable) {
                Toast.makeText(context, "Failed to fetch polling orders", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun toggleLoading(isLoading: Boolean) {
        binding.loginButton.visibility = if (isLoading) View.GONE else View.VISIBLE
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}