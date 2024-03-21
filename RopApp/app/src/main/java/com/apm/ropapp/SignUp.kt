import android.content.Intent
import com.apm.ropapp.R
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.apm.ropapp.MainActivity
import com.apm.ropapp.databinding.SignUpBinding

class SignUp : AppCompatActivity() {

    private lateinit var binding: SignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Crear un ArrayAdapter utilizando el array de opciones y un diseño predefinido para los elementos de lista
        ArrayAdapter.createFromResource(
            this,
            R.array.genero_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Especificar el diseño a utilizar cuando aparece la lista de opciones
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            // Aplicar el adaptador al Spinner
            binding.spinnerGenero.adapter = adapter
        }

        // Manejar la selección del Spinner
        binding.spinnerGenero.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                val selectedGender = parent.getItemAtPosition(position).toString()
                Log.d("SignUp", "Se seleccionó el género: $selectedGender")
                Toast.makeText(applicationContext, "Selected gender: $selectedGender", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No hacer nada si no se selecciona ningún elemento
            }
        }

        // Configurar el clic del botón para navegar a MainActivity
        binding.createAccountButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}
