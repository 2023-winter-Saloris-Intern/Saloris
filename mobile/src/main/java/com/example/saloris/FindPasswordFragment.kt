package com.example.saloris

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.saloris.databinding.FragmentFindPasswordBinding
import com.example.saloris.util.MakeToast
import com.example.saloris.util.OpenDialog
import com.example.saloris.util.Validator
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class FindPasswordFragment : Fragment() {
    /* View */
    private lateinit var binding: FragmentFindPasswordBinding
    private lateinit var navController: NavController

    /* User Authentication */
    private lateinit var auth: FirebaseAuth
    private val validator = Validator()

    /* Toast */
    private val toast = MakeToast()

    /* Dialog */
    private val dialog = OpenDialog()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentFindPasswordBinding.inflate(layoutInflater, container, false)

        /* Bottom Menu */
        val bottomMenu = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomMenu.visibility = View.GONE

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        auth = Firebase.auth

        binding.editEmail.setOnEditorActionListener { textView, action, _ ->
            if (action == EditorInfo.IME_ACTION_DONE) {
                // Hide Keyboard
                val inputMethodManager =
                    context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(textView.windowToken, 0)

                // Click Button
                binding.btnSubmit.performClick()
                return@setOnEditorActionListener true
            }
            false
        }

        binding.btnCancel.setOnClickListener {
            navController.popBackStack()
        }

        binding.btnSubmit.setOnClickListener {
            binding.layoutLoading.root.visibility = View.VISIBLE

            val id = binding.editEmail.text.toString().trim()

            if (id.isEmpty()) {
                context?.let { context -> toast.makeToast(context, "입력칸을 채워주세요") }
                binding.layoutLoading.root.visibility = View.GONE
            } else {
                if (!validator.checkEmail(binding.editEmail)) {
                    context?.let { context -> toast.makeToast(context, "이메일 형식을 지켜주세요") }
                    binding.layoutLoading.root.visibility = View.GONE
                } else {
                    auth.sendPasswordResetEmail(id).addOnCompleteListener { resetTask ->
                        if (resetTask.isSuccessful) {
                            activity?.let { activity ->
                                dialog.openDialog(activity, FindPasswordDialog(), "FindPassword")
                            }
                            navController.popBackStack()
                        } else {
                            context?.let { context -> toast.makeToast(context, "존재하지 않는 계정입니다.") }
                        }
                        binding.layoutLoading.root.visibility = View.GONE
                    }
                }
            }
        }
    }
}