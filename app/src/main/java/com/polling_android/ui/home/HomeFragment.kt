package com.polling_android.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.polling_android.databinding.FragmentHomeBinding
import com.polling_android.R

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (!isUserLoggedIn()) {
            findNavController().navigate(R.id.nav_login)
            return View(context)
        }

        val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    private fun isUserLoggedIn(): Boolean {
        // Replace this with your actual logic to check if the user is logged in
        return false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}