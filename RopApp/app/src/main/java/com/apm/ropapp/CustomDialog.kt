import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.widget.Button
import com.apm.ropapp.AddClothes
import com.apm.ropapp.R

class CustomDialog(context: Context) : Dialog(context) {

    private lateinit var addPrendaButton: Button
    private lateinit var addConjuntoButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.addmenu_layout)

        addPrendaButton = findViewById(R.id.btn_add_prenda)
        addConjuntoButton = findViewById(R.id.btn_add_conjunto)

        addPrendaButton.setOnClickListener {
            // Acción cuando se hace clic en el botón añadir prenda
            dismiss() // Cierra el diálogo

            // Iniciar la actividad AddClothes
            val intent = Intent(context, AddClothes::class.java)
            context.startActivity(intent)
        }

        addConjuntoButton.setOnClickListener {
            // Acción cuando se hace clic en el botón añadir conjunto
            dismiss() // Cierra el diálogo
            // Agrega aquí la lógica para añadir un conjunto
        }
    }
}
