package com.example.saloris.util

import android.widget.EditText
import java.util.regex.Pattern

class Validator {
    private val emailValidation =
        "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"

    fun checkEmail(email: EditText): Boolean {
        if (email.text.toString() == "") return true
        return Pattern.matches(emailValidation, email.text.toString())
    }

    private val passwordValidation =
        "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[!@#\$%^+\\-=])(?=\\S+\$).{8,}\$"

    fun checkPassword(password: EditText): Boolean {
        if (password.text.toString() == "") return true
        return Pattern.matches(passwordValidation, password.text.toString())
    }
}
