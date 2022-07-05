package com.wonmirzo.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.wonmirzo.databinding.ActivityMainBinding
import com.wonmirzo.utils.Utils.showToast


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                showToast(
                    "${result.data?.getStringExtra("card_number")} - ${
                        result.data?.getStringExtra(
                            "card_expire"
                        )
                    }"
                )
            } else if (result.resultCode == Activity.RESULT_CANCELED) {
                showToast("cancel")
            }
        }

        binding.goBtn.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(this, "android.permission.CAMERA") != 0) {
                ActivityCompat.requestPermissions(
                    this, arrayOf("android.permission.CAMERA"),
                    123
                )
            } else {
                Intent(this, ScanActivity::class.java).also {
                    launcher.launch(it)
                }
            }
        }

    }
}