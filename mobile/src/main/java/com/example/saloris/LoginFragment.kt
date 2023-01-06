package com.example.saloris

import android.app.Activity
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.saloris.databinding.FragmentLoginBinding
import com.example.saloris.util.MakeToast
import com.example.saloris.util.Validator
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginFragment : Fragment() {
    /* View */
    private lateinit var navController: NavController
    private lateinit var binding: FragmentLoginBinding

    /* User Authentication */
    private lateinit var auth: FirebaseAuth
    private val validator = Validator()

    private fun saveAutoLoginInfo(username: String, password: String) {
        val autoLoginPref =
            requireContext().getSharedPreferences("autoLogin", Activity.MODE_PRIVATE)
        val autoLoginEdit = autoLoginPref.edit()
        autoLoginEdit.putString("username", username)
        autoLoginEdit.putString("password", password)
        autoLoginEdit.apply()
    }

    /* Toast */
    private val toast = MakeToast()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(layoutInflater, container, false)
        /* Bottom Menu */
        val bottomMenu = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomMenu.visibility = View.GONE

        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val connectivityManager =
            ContextCompat.getSystemService(binding.root.context, ConnectivityManager::class.java)
        auth = Firebase.auth

        navController = Navigation.findNavController(view)

        binding.loginEmail.setText(arguments?.getString("id"))

        binding.loginPassword.setOnEditorActionListener { textView, action, _ ->
            if (action == EditorInfo.IME_ACTION_DONE) {
                // Hide Keyboard
                val inputMethodManager =
                    context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(textView.windowToken, 0)

                // Click Button
                binding.btnLogin.performClick()
                return@setOnEditorActionListener true
            }
            false
        }
        binding.btnLogin.setOnClickListener {
            binding.layoutLoading.root.visibility = View.VISIBLE
            context?.let { context -> toast.makeToast(context, "로그인 버튼") }
            val username = binding.loginEmail.text.toString().trim()
            val password = binding.loginPassword.text.toString().trim()

            val currentNetwork = connectivityManager!!.activeNetwork

            if (currentNetwork == null) {
                context?.let { context -> toast.makeToast(context, "네트워크를 확인해 주세요.") }
                binding.layoutLoading.root.visibility = View.GONE
            } else {
                if (username.isEmpty() || password.isEmpty()) {
                    context?.let { context -> toast.makeToast(context, "입력란을 모두 작성해주세요") }
                    binding.layoutLoading.root.visibility = View.GONE
                } else {
                    if (!validator.checkEmail(binding.loginEmail)) {
                        context?.let { context -> toast.makeToast(context, "이메일 형식을 지켜주세요") }
                        binding.layoutLoading.root.visibility = View.GONE
                    } else {
                        auth.signInWithEmailAndPassword(username, password)
                            .addOnCompleteListener { loginTask ->
                                if (loginTask.isSuccessful) {
                                    if (!isAdded) {
                                        return@addOnCompleteListener
                                    }
                                    if (auth.currentUser?.isEmailVerified!!) {
                                        saveAutoLoginInfo(username, password)
                                        navController.navigate(R.id.action_loginFragment_to_homeFragment)
                                    } else {
                                        Firebase.auth.currentUser!!.sendEmailVerification()
                                            .addOnCompleteListener { verifyTask ->
                                                context?.let { context ->
                                                    toast.makeToast(
                                                        context,
                                                        if (verifyTask.isSuccessful)
                                                            "인증 이메일을 다시 전송했습니다. 메일함에서 인증해주세요"
                                                        else
                                                            "메일함을 확인해주세요"
                                                    )
                                                }
                                            }
                                    }
                                } else {
                                    context?.let { context ->
                                        toast.makeToast(context, "이메일과 비밀번호를 확인해주세요")
                                    }
                                }
                                binding.layoutLoading.root.visibility = View.GONE
                            }
                    }
                }
            }
        }

        binding.btnRegister.setOnClickListener {
            val username = binding.loginEmail.text.toString().trim()
            toast.makeToast(requireContext(), "회원가입 버튼")
                    navController.navigate(
                R.id.action_loginFragment_to_registerFragment,
                bundleOf("id" to username)
            )
        }

        binding.btnFindPassword.setOnClickListener {
            toast.makeToast(requireContext(), "비밀번호 찾기 버튼")
            navController.navigate(R.id.action_loginFragment_to_findPasswordFragment)
        }
    }

}