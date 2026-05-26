package com.example.util

import android.content.Context
import android.widget.Toast

private var activeToast: Toast? = null

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    if (android.os.Looper.myLooper() == android.os.Looper.getMainLooper()) {
        showToastInternal(message, duration)
    } else {
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            showToastInternal(message, duration)
        }
    }
}

private fun Context.showToastInternal(message: String, duration: Int) {
    activeToast?.cancel()
    val toast = Toast.makeText(this.applicationContext, message, duration)
    activeToast = toast
    toast.show()
}
