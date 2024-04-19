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

        val checkedList = intent.extras?.getStringArrayList("checked")
        if (checkedList != null) {
            if (checkedList.contains(binding.top.text)) binding.top.toggle()
            if (checkedList.contains(binding.accessories.text)) binding.accessories.toggle()
            if (checkedList.contains(binding.bottom.text)) binding.bottom.toggle()
            if (checkedList.contains(binding.shoe.text)) binding.shoe.toggle()
            if (checkedList.contains(binding.dress.text)) binding.dress.toggle()
            if (checkedList.contains(binding.bag.text)) binding.bag.toggle()
            if (checkedList.contains(binding.outerwear.text)) binding.outerwear.toggle()
            if (checkedList.contains(binding.hat.text)) binding.hat.toggle()
            if (checkedList.contains(binding.activewear.text)) binding.activewear.toggle()
            if (checkedList.contains(binding.scarf.text)) binding.scarf.toggle()
            if (checkedList.contains(binding.formalwear.text)) binding.formalwear.toggle()
            if (checkedList.contains(binding.gloves.text)) binding.gloves.toggle()
            if (checkedList.contains(binding.loungewear.text)) binding.loungewear.toggle()
            if (checkedList.contains(binding.socks.text)) binding.socks.toggle()
            if (checkedList.contains(binding.belts.text)) binding.belts.toggle()
            if (checkedList.contains(binding.socks.text)) binding.socks.toggle()
            if (checkedList.contains(binding.sunglasses.text)) binding.sunglasses.toggle()
        }

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
            intent.putExtra("category", result)
            setResult(RESULT_OK, intent)
            Log.d("AddCategories", "Categories Added")
            finish()
        }
    }
}