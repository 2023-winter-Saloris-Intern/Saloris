package com.example.saloris

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.saloris.databinding.DialogTermsViewBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class TermsDialog : DialogFragment() {
    private lateinit var binding: DialogTermsViewBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DialogTermsViewBinding.inflate(layoutInflater, container, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        /* Bottom Menu */
        val bottomMenu = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomMenu.visibility = View.GONE
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /* After 서버를 통해 약관 가져오기 */
        when (arguments?.getString("contents")) {
            "Terms" -> {
                binding.titleAgreement.text = getString(R.string.terms_title)
                binding.contentAgreement.text = getString(R.string.terms)
            }
            "Privacy" -> {
                binding.titleAgreement.text = getString(R.string.privacy_title)
                binding.contentAgreement.text = getString(R.string.privacy)
            }
        }
    }
}