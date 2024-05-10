package com.apm.ropapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import com.apm.ropapp.databinding.AddchoicecategoryBinding

class AddCategories : AppCompatActivity() {

    private lateinit var binding: AddchoicecategoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = AddchoicecategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val layout = binding.root.getChildAt(0)
        val checkedList = intent.extras?.getStringArrayList("checked")

        if (checkedList != null && layout is ConstraintLayout) {
            for (checkBox in layout.children)
                if (checkBox is CheckBox && checkedList.contains(checkBox.text))
                    checkBox.toggle()
        }

        binding.backButtonChoice.setOnClickListener {
            Log.d("AddCategories", "Back to Add Clothes")
            finish()
        }

        binding.confirm.setOnClickListener {
            val result = arrayListOf<String>()

            if (layout is ConstraintLayout) {
                for (checkBox in layout.children)
                    if (checkBox is CheckBox && checkBox.isChecked)
                        result.add(checkBox.text.toString())
            }

            val intent = Intent()
            intent.putExtra("category", result)
            setResult(RESULT_OK, intent)
            Log.d("AddCategories", "Categories Added")
            finish()
        }
    }
}