package com.example.saloris

import android.net.ConnectivityManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.saloris.databinding.FragmentRegisterBinding
import com.example.saloris.util.MakeToast
import com.example.saloris.util.OpenDialog
import com.example.saloris.util.Validator
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase

class RegisterFragment : Fragment() {
    /* View */
    private lateinit var navController: NavController
    private lateinit var binding: FragmentRegisterBinding

    /* User Authentication */
    private lateinit var auth: FirebaseAuth
    private var isExistBlank = false
    private var isPasswordSame = false
    private val validator = Validator()

    private fun isInputDone(id: String, username: String, password: String, passwordRe: String) {
        isExistBlank =
            id.isEmpty() || username.isEmpty() || password.isEmpty() || passwordRe.isEmpty()
        isPasswordSame = password == passwordRe
    }

    private fun addTextChangedListener(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.btnRegister.isEnabled = isAllDone()
            }
        })
    }

    private fun onCheckChanged(compoundButton: CompoundButton) {
        when (compoundButton.id) {
            R.id.check_all -> {
                if (binding.checkAll.isChecked) {
                    binding.checkUse.isChecked = true
                    binding.checkAge.isChecked = true
                    binding.checkPrivate.isChecked = true
                } else {
                    binding.checkUse.isChecked = false
                    binding.checkAge.isChecked = false
                    binding.checkPrivate.isChecked = false
                }
            }
            else -> {
                binding.checkAll.isChecked =
                    binding.checkUse.isChecked && binding.checkAge.isChecked && binding.checkPrivate.isChecked
            }
        }
        binding.btnRegister.isEnabled = isAllDone()
    }

    private fun changeEdit(editText: EditText, textView: TextView, isCorrect: Boolean) {
        if (isCorrect) {
            editText.setBackgroundResource(R.drawable.bg_edittext)
            textView.visibility = View.INVISIBLE
        } else {
            editText.setBackgroundResource(R.drawable.bg_edittext_wrong)
            textView.visibility = View.VISIBLE
        }
    }

    private fun isAllDone(): Boolean {
        isInputDone(
            binding.registerEmail.text.toString(),
            binding.registerUsername.text.toString(),
            binding.registerPassword.text.toString(),
            binding.registerRePassword.text.toString()
        )

        changeEdit(
            binding.registerEmail, binding.emailNotice,
            validator.checkEmail(binding.registerEmail)
        )
        changeEdit(
            binding.registerPassword, binding.passwordNotice,
            validator.checkPassword(binding.registerPassword)
        )
        changeEdit(
            binding.registerRePassword, binding.rePasswordNotice,
            (isPasswordSame || binding.registerRePassword.text.toString() == "")
        )

        return if (!isExistBlank && isPasswordSame) {
            if (validator.checkEmail(binding.registerEmail)) binding.checkAll.isChecked
            else false
        } else false
    }

    /* Toast */
    private val toast = MakeToast()

    /* Dialog */
    private val dialog = OpenDialog()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val connectivityManager =
            ContextCompat.getSystemService(binding.root.context, ConnectivityManager::class.java)

        auth = Firebase.auth

        navController = Navigation.findNavController(view)

        binding.registerEmail.setText(arguments?.getString("id"))

        addTextChangedListener(binding.registerEmail)
        addTextChangedListener(binding.registerPassword)
        addTextChangedListener(binding.registerUsername)
        addTextChangedListener(binding.registerRePassword)

        // 약관 동의
        binding.checkAll.setOnClickListener { onCheckChanged(binding.checkAll) }
        binding.checkUse.setOnClickListener { onCheckChanged(binding.checkUse) }
        binding.checkAge.setOnClickListener { onCheckChanged(binding.checkAge) }
        binding.checkPrivate.setOnClickListener { onCheckChanged(binding.checkPrivate) }

        // 내용 보기
        binding.tacView.setOnClickListener {
            activity?.let { activity -> dialog.openDialog(activity, TermsDialog(), "Terms") }
        }
        binding.privacyView.setOnClickListener {
            activity?.let { activity -> dialog.openDialog(activity, TermsDialog(), "Privacy") }
        }

        // 회원가입
        binding.btnRegister.isEnabled = isAllDone()
        binding.btnRegister.setOnClickListener {
            binding.layoutLoading.root.visibility = View.VISIBLE

            val id = binding.registerEmail.text.toString()
            val username = binding.registerUsername.text.toString()
            val password = binding.registerPassword.text.toString()
            val passwordRe = binding.registerRePassword.text.toString()

            val profileUpdates = userProfileChangeRequest {
                displayName = username
            }

            val currentNetwork = connectivityManager!!.activeNetwork
            if (currentNetwork == null) {
                context?.let { context -> toast.makeToast(context, "네트워크를 확인해 주세요.") }
                binding.layoutLoading.root.visibility = View.GONE
            } else {
                if (!isExistBlank && isPasswordSame) {
                    if (validator.checkEmail(binding.registerEmail)) {
                        auth.createUserWithEmailAndPassword(id, password)
                            .addOnCompleteListener { registerTask ->
                                if (registerTask.isSuccessful) {
                                    if (!isAdded) {
                                        return@addOnCompleteListener
                                    }
                                    auth.currentUser
                                        ?.sendEmailVerification()
                                        ?.addOnCompleteListener { verifyTask ->
                                            if (verifyTask.isSuccessful) {
                                                context?.let { context ->
                                                    toast.makeToast(context, "메일함을 확인해 주세요.")
                                                }
                                                auth.currentUser!!.updateProfile(profileUpdates)
                                                navController.navigate(
                                                    R.id.action_registerFragment_to_registerSuccessFragment,
                                                    bundleOf("id" to id)
                                                )
                                            }
                                            binding.layoutLoading.root.visibility = View.GONE
                                        }
                                } else if (registerTask.exception?.message.isNullOrEmpty()) {
                                    context?.let { context ->
                                        toast.makeToast(context, "회원가입 오류입니다.")
                                    }
                                    binding.layoutLoading.root.visibility = View.GONE
                                } else {
                                    context?.let { context ->
                                        toast.makeToast(
                                            context,
                                            if (password.length < 8 || passwordRe.length < 8)
                                                "비밀번호는 8자리 이상입니다."
                                            else
                                                "이미 존재하는 이메일입니다."
                                        )
                                    }
                                    binding.layoutLoading.root.visibility = View.GONE
                                }
                            }
                    } else {
                        context?.let { context -> toast.makeToast(context, "이메일 형식을 지켜주세요") }
                        binding.layoutLoading.root.visibility = View.GONE
                    }
                } else {
                    if (isExistBlank) {
                        context?.let { context -> toast.makeToast(context, "입력란을 모두 작성해주세요") }
                    } else { // !isPasswordSame
                        context?.let { context -> toast.makeToast(context, "비밀번호가 다릅니다") }
                    }
                    binding.layoutLoading.root.visibility = View.GONE
                }
            }
        }
    }
}