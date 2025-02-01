package com.polling_android.ui.signout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.polling_android.R
import com.polling_android.databinding.FragmentSignOutBinding
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class SignOutFragment : Fragment() {

    private var _binding: FragmentSignOutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignOutBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.signOutButton.setOnClickListener {
            signOut()
        }

        return root
    }

    private fun signOut() {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val sharedPreferences = EncryptedSharedPreferences.create(
            "secure_prefs",
            masterKeyAlias,
            requireContext(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        sharedPreferences.edit().clear().apply()
        findNavController().navigate(R.id.nav_login)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}