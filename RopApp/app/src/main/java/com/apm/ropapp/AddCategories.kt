package com.apm.ropapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.apm.ropapp.databinding.AddchoicecategoryBinding

class AddCategories : AppCompatActivity() {

    private lateinit var binding: AddchoicecategoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = AddchoicecategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButtonChoice.setOnClickListener {
            Log.d("AddCategories", "Back to Add Clothes")
            finish()
        }

        binding.confirm.setOnClickListener {
            val result = arrayListOf<String>()
            if (binding.top.isChecked) result.add(binding.top.text.toString())
            if (binding.accessories.isChecked) result.add(binding.accessories.text.toString())
            if (binding.bottom.isChecked) result.add(binding.bottom.text.toString())
            if (binding.shoe.isChecked) result.add(binding.shoe.text.toString())
            if (binding.dress.isChecked) result.add(binding.dress.text.toString())
            if (binding.bag.isChecked) result.add(binding.bag.text.toString())
            if (binding.outerwear.isChecked) result.add(binding.outerwear.text.toString())
            if (binding.hat.isChecked) result.add(binding.hat.text.toString())
            if (binding.activewear.isChecked) result.add(binding.activewear.text.toString())
            if (binding.scarf.isChecked) result.add(binding.scarf.text.toString())
            if (binding.formalwear.isChecked) result.add(binding.formalwear.text.toString())
            if (binding.gloves.isChecked) result.add(binding.gloves.text.toString())
            if (binding.loungewear.isChecked) result.add(binding.loungewear.text.toString())
            if (binding.socks.isChecked) result.add(binding.socks.text.toString())
            if (binding.belts.isChecked) result.add(binding.belts.text.toString())
            if (binding.sunglasses.isChecked) result.add(binding.sunglasses.text.toString())

            val intent = Intent()
            intent.putExtra("result", result)
            setResult(RESULT_OK, intent)
            Log.d("AddCategories", "Categories Added")
            finish()
        }
    }
}