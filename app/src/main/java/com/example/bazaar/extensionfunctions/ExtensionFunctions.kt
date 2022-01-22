package com.example.bazaar.extensionfunctions

import android.util.Patterns

object ExtensionFunctions {
    fun CharSequence?.isValidEmail() = !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()
}