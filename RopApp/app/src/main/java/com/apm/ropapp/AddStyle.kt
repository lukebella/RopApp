package com.apm.ropapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.apm.ropapp.databinding.AddchoicestyleBinding

class AddStyle : AppCompatActivity() {

    private lateinit var binding: AddchoicestyleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = AddchoicestyleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val checkedList = intent.extras?.getStringArrayList("checked")
        if (checkedList != null) {
            if (checkedList.contains(binding.classic.text)) binding.classic.toggle()
            if (checkedList.contains(binding.bohemian.text)) binding.bohemian.toggle()
            if (checkedList.contains(binding.preppy.text)) binding.preppy.toggle()
            if (checkedList.contains(binding.streetwear.text)) binding.streetwear.toggle()
            if (checkedList.contains(binding.streetwear.text)) binding.streetwear.toggle()
            if (checkedList.contains(binding.romantic.text)) binding.romantic.toggle()
            if (checkedList.contains(binding.minimalist.text)) binding.minimalist.toggle()
            if (checkedList.contains(binding.vintage.text)) binding.vintage.toggle()
            if (checkedList.contains(binding.sporty.text)) binding.sporty.toggle()
            if (checkedList.contains(binding.glamorous.text)) binding.glamorous.toggle()
            if (checkedList.contains(binding.eclectic.text)) binding.eclectic.toggle()
        }

        binding.backButtonChoice.setOnClickListener {
            Log.d("AddStyle","Back to Add Clothes")
            finish()
        }

        binding.confirm.setOnClickListener {
            val result = arrayListOf<String>()
            if (binding.classic.isChecked) result.add(binding.classic.text.toString())
            if (binding.bohemian.isChecked) result.add(binding.bohemian.text.toString())
            if (binding.preppy.isChecked) result.add(binding.preppy.text.toString())
            if (binding.streetwear.isChecked) result.add(binding.streetwear.text.toString())
            if (binding.romantic.isChecked) result.add(binding.romantic.text.toString())
            if (binding.minimalist.isChecked) result.add(binding.minimalist.text.toString())
            if (binding.vintage.isChecked) result.add(binding.vintage.text.toString())
            if (binding.sporty.isChecked) result.add(binding.sporty.text.toString())
            if (binding.glamorous.isChecked) result.add(binding.glamorous.text.toString())
            if (binding.eclectic.isChecked) result.add(binding.eclectic.text.toString())

            val intent = Intent()
            intent.putExtra("style", result)
            setResult(RESULT_OK, intent)
            Log.d("AddStyle", "Style Added")
            finish()
        }
    }
}