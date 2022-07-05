package com.wonmirzo.utils

import android.app.Activity
import android.widget.Toast

object Utils {
    fun Activity.showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}