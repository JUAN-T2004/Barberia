package com.example.barberia

import android.os.Bundle
import androidx.activity.ComponentActivity
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.EditText;
import android.widget.Toast;
import android.view.View
import java.util.Calendar
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.database.SQLException
import android.util.Log
import android.widget.ImageView


class MainActivity : ComponentActivity() {

    private var currentView: View? = null
    private var selectedRadioButton: RadioButton? = null
    private var selectedDate: String? = null
    private var selectedTime: String? = null
    private var connectSql = ConnectSql()

    // Datos de ejemplo para los cortes
    private val cortes = listOf(
        Corte(R.drawable.corte1, "Degradado", "$ 15.000"),
        Corte(R.drawable.corte2, "Aleman", "$ 13.000"),
        Corte(R.drawable.corte3, "Frances", "$ 18.000"),
        Corte(R.drawable.corte4, "El 7", "$ 10.000"),
        Corte(R.drawable.corte5, "Degradado punta", "$ 16.000")
    )

    data class Corte(val imageResId: Int, val nombre: String, val precio: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pantalla_inicio)

        currentView = findViewById(R.id.pantalla_inicio_layout)
        setupButtonRegister()
        setupButtonLogin()
    }

    private fun setupEditTextFechaHora() {
        val editTextFechaHora = findViewById<EditText>(R.id.editTextFechaHora)

        // Configurar el OnClickListener para mostrar el DatePickerDialog
        editTextFechaHora.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

            // Crear un DatePickerDialog
            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, dayOfMonth ->
                    // Mostrar un TimePickerDialog después de seleccionar la fecha
                    val hour = calendar.get(Calendar.HOUR_OF_DAY)
                    val minute = calendar.get(Calendar.MINUTE)

                    val timePickerDialog = TimePickerDialog(
                        this,
                        { _, selectedHour, selectedMinute ->
                            selectedDate = "$selectedYear-${selectedMonth + 1}-$dayOfMonth"
                            selectedTime = "$selectedHour:$selectedMinute"
                            editTextFechaHora.setText("$selectedDate $selectedTime")
                        },
                        hour,
                        minute,
                        true
                    )

                    // Mostrar el TimePickerDialog
                    timePickerDialog.show()
                },
                year,
                month,
                dayOfMonth
            )

            // Mostrar el DatePickerDialog
            datePickerDialog.show()
        }
    }


    private fun ModificarRadioButtons() {
        val radioButtonOption1 = findViewById<RadioButton>(R.id.chequeoCorte1)
        val radioButtonOption2 = findViewById<RadioButton>(R.id.chequeoCorte2)
        val radioButtonOption3 = findViewById<RadioButton>(R.id.chequeoCorte3)
        val radioButtonOption4 = findViewById<RadioButton>(R.id.chequeoCorte4)
        val radioButtonOption5 = findViewById<RadioButton>(R.id.chequeoCorte5)

        val radioButtons = listOf(radioButtonOption1, radioButtonOption2, radioButtonOption3, radioButtonOption4, radioButtonOption5)

        for (radioButton in radioButtons) {
            radioButton.setOnClickListener {
                selectedRadioButton = radioButton
                radioButtons.forEach { if (it != radioButton) it.isChecked = false }
            }
        }
    }

    private fun setupButtonRegister() {
        val botonRegistrarse = currentView?.findViewById<Button>(R.id.buttonRegister)
        botonRegistrarse?.setOnClickListener {
            replaceView(R.layout.pantalla_registro)

            setupButtonRegister2()

        }
    }

    private fun guardarDatosEnBaseDeDatos(nombre: String, apellido: String, correo: String, contraseña: String) {
        val conn = connectSql.dbConn()

        conn?.let { connection ->
            try {
                val statement = connection.createStatement()

                // Crear la consulta SQL para insertar los datos en la tabla correspondiente
                val query = "INSERT INTO Datos (nombre, apellido, correo, contraseña) VALUES ('$nombre', '$apellido', '$correo', '$contraseña')"

                // Ejecutar la consulta SQL
                statement.executeUpdate(query)

                // Cerrar la conexión
                connection.close()


            } catch (ex: SQLException) {
                Log.e("Error SQL", ex.message ?: "Error al ejecutar la consulta SQL")
            }
        } ?: run {
            Log.e("Error de conexión", "No se pudo establecer una conexión con la base de datos")
        }
    }

    private fun setupButtonRegister2() {
        val botonRegistrarse2 = currentView?.findViewById<Button>(R.id.buttonRegister2)
        botonRegistrarse2?.setOnClickListener {
            val editTextNombre = findViewById<EditText>(R.id.Nombre)
            val editTextApellido = findViewById<EditText>(R.id.Apellido)
            val editTextCorreo = findViewById<EditText>(R.id.Email)
            val editTextContraseña = findViewById<EditText>(R.id.Contraseña)
            val nombre = editTextNombre.text.toString()
            val apellido = editTextApellido.text.toString()
            val correo = editTextCorreo.text.toString()
            val contraseña = editTextContraseña.text.toString()

            // Verificar si los campos están llenos y si el correo tiene el sufijo "@gmail.com"
            if (nombre.isNotEmpty() && apellido.isNotEmpty() && correo.isNotEmpty() && contraseña.isNotEmpty()) {
                // Verificar si el correo tiene el sufijo "@gmail.com"
                if (isValidGmail(correo)) {
                    guardarDatosEnBaseDeDatos(nombre, apellido, correo, contraseña)
                    // Mostrar mensaje de éxito y llevar al usuario a la pantalla de servicios
                    Toast.makeText(this, "Se ha registrado con éxito en BarberShop", Toast.LENGTH_SHORT).show()
                } else {
                    // Mostrar mensaje de advertencia si el correo electrónico no tiene el sufijo "@gmail.com"
                    editTextCorreo.error = "El correo electrónico debe terminar en @gmail.com"
                }
            } else {
                // Mostrar mensaje de advertencia si los campos están vacíos
                Toast.makeText(this, "Por favor llene todos los campos", Toast.LENGTH_SHORT).show()
            }

        }
    }


    private fun setupButtonLogin() {
        val botonIniciarSesion = currentView?.findViewById<Button>(R.id.buttonLogin)
        botonIniciarSesion?.setOnClickListener {
            replaceView(R.layout.pantalla_inicio_sesion)

            setupButtonLogin2()
        }
    }

    private fun verificarCredenciales(correo: String, contraseña: String): Boolean {
        val conn = connectSql.dbConn()
        var resultado = false

        conn?.let { connection ->
            try {
                val query = "SELECT nombre, apellido FROM Datos WHERE correo = ? AND contraseña = ?"
                val preparedStatement = connection.prepareStatement(query)
                preparedStatement.setString(1, correo)
                preparedStatement.setString(2, contraseña)

                val resultSet = preparedStatement.executeQuery()
                if (resultSet.next()) {
                    val nombrePersona = resultSet.getString("nombre")
                    resultado = true
                    val apellidoPersona = resultSet.getString("apellido")

                    // Guardar correo y nombrePersona en SharedPreferences
                    val sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                    with(sharedPref.edit()) {
                        putString("nombrePersona", nombrePersona)
                        putString("apellidoPersona", apellidoPersona)
                        apply()
                    }
                }

                resultSet.close()
                preparedStatement.close()
                connection.close()
            } catch (ex: SQLException) {
                Log.e("Error SQL", ex.message ?: "Error al ejecutar la consulta SQL")
            }
        } ?: run {
            Log.e("Error de conexión", "No se pudo establecer una conexión con la base de datos")
        }

        return resultado
    }


    private fun setupButtonLogin2() {
        val botonContinuar = currentView?.findViewById<Button>(R.id.buttonContinuar)
        botonContinuar?.setOnClickListener {
            val editTextCorreo = findViewById<EditText>(R.id.editTextEmail)
            val editTextContraseña = findViewById<EditText>(R.id.editTextPassword)
            val correo = editTextCorreo.text.toString()
            val contraseña = editTextContraseña.text.toString()

            // Verificar si los campos de correo y contraseña están llenos
            if (correo.isNotEmpty() && contraseña.isNotEmpty()) {
                // Verificar si el correo tiene el sufijo "@gmail.com"
                if (isValidGmail(correo)) {
                    // Mostrar mensaje de éxito y llevar al usuario a la pantalla de servicios
                    if (verificarCredenciales(correo, contraseña)) {
                        // Mostrar mensaje de éxito y llevar al usuario a la pantalla de servicios
                        Toast.makeText(this, "Iniciaste sesión con éxito en BarberShop", Toast.LENGTH_SHORT).show()
                        replaceView(R.layout.pantalla_servicios)
                        ModificarRadioButtons()
                        setupEditTextFechaHora()
                        setupButtonConfirmarServicio()
                    } else {
                        // Mostrar mensaje de error si las credenciales no son válidas
                        Toast.makeText(this, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Mostrar mensaje de advertencia si el correo electrónico no tiene el sufijo "@gmail.com"
                    editTextCorreo.error = "El correo electrónico debe terminar en @gmail.com"
                }
            } else {
                // Mostrar mensaje de advertencia si los campos están vacíos
                Toast.makeText(this, "Por favor ingresa tu correo y contraseña", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun setupButtonConfirmarServicio() {
        val botonConfirmarServicio = findViewById<Button>(R.id.buttonConfirmarServicio)
        botonConfirmarServicio.setOnClickListener {
            if (selectedRadioButton == null) {
                Toast.makeText(this, "Selecciona tu corte", Toast.LENGTH_SHORT).show()
            } else if (selectedDate == null || selectedTime == null) {
                Toast.makeText(this, "Selecciona fecha y hora para tu corte", Toast.LENGTH_SHORT).show()
            } else {
                replaceView(R.layout.pantalla_confirmacion)
                setupButtonRegresarServicios()
                mostrarDatosConfirmacion()

                // Obtener correo y nombrePersona de SharedPreferences
                val sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                val nombrePersona = sharedPref.getString("nombrePersona", "") ?: ""
                val apellidoPersona = sharedPref.getString("apellidoPersona", "") ?: ""

                // Obtener los datos de los EditText en pantalla_confirmacion
                val editTextNombreCorte = findViewById<EditText>(R.id.editTextNombreCorte)
                val editTextPrecioCorte = findViewById<EditText>(R.id.editTextPrecioCorte)
                val editTextFechaHora = findViewById<EditText>(R.id.editTextFechahoraCorte)

                val corteNombre = editTextNombreCorte.text.toString()
                val cortePrecio = editTextPrecioCorte.text.toString()
                val corteFechaHora = editTextFechaHora.text.toString()

                // Guardar los datos en la base de datos
                guardarCorte(nombrePersona, apellidoPersona, corteNombre, cortePrecio, corteFechaHora)
            }
        }
    }


    private fun guardarCorte(nombrePersona: String, apellidoPersona: String, corteNombre: String, cortePrecio: String, corteFechaHora: String) {
        val conn = connectSql.dbConn()

        conn?.let { connection ->
            try {
                val query = "INSERT INTO Cortes (nombrePersona, apellidoPersona, nombrecorte, preciocorte, fechahoracorte) VALUES (?, ?, ?, ?, ?)"
                val preparedStatement = connection.prepareStatement(query)
                preparedStatement.setString(1, nombrePersona)
                preparedStatement.setString(2, apellidoPersona)
                preparedStatement.setString(3, corteNombre)
                preparedStatement.setString(4, cortePrecio)
                preparedStatement.setString(5, corteFechaHora)

                preparedStatement.executeUpdate()

                preparedStatement.close()
                connection.close()
                Log.d("DB Insert", "Datos del corte guardados correctamente")
            } catch (ex: SQLException) {
                Log.e("Error SQL", ex.message ?: "Error al ejecutar la consulta SQL")
            }
        } ?: run {
            Log.e("Error de conexión", "No se pudo establecer una conexión con la base de datos")
        }
    }


    private fun mostrarDatosConfirmacion() {
        val imageView = findViewById<ImageView>(R.id.imageViewCorteConfirmacion)
        val editTextNombreCorte = findViewById<EditText>(R.id.editTextNombreCorte)
        val editTextPrecioCorte = findViewById<EditText>(R.id.editTextPrecioCorte)
        val editTextFechaHora = findViewById<EditText>(R.id.editTextFechahoraCorte)

        val selectedCorteIndex = when (selectedRadioButton?.id) {
            R.id.chequeoCorte1 -> 0
            R.id.chequeoCorte2 -> 1
            R.id.chequeoCorte3 -> 2
            R.id.chequeoCorte4 -> 3
            R.id.chequeoCorte5 -> 4
            else -> -1
        }

        if (selectedCorteIndex != -1) {
            val corte = cortes[selectedCorteIndex]

            editTextNombreCorte.setText(corte.nombre)
            editTextPrecioCorte.setText(corte.precio)
            editTextFechaHora.setText("$selectedDate $selectedTime")

            imageView.setImageResource(corte.imageResId)

        } else {
            Toast.makeText(this, "No se pudo encontrar el corte seleccionado", Toast.LENGTH_SHORT).show()
        }

    }

    // Función para verificar si el correo electrónico tiene el sufijo "@gmail.com"
    private fun isValidGmail(email: String): Boolean {
        return email.endsWith("@gmail.com")
    }

    private fun setupButtonCerrarSesion() {
        val botonCerrarSesion = currentView?.findViewById<Button>(R.id.buttonCerrarSesion)
        botonCerrarSesion?.setOnClickListener {
            setContentView(R.layout.pantalla_inicio) // Volver a la pantalla de inicio
            currentView = findViewById(R.id.pantalla_inicio_layout) // Actualizar la referencia de la vista actual
            setupButtonRegister() // Configurar nuevamente el botón "Registrarse"
            setupButtonLogin() // Configurar nuevamente el botón "Iniciar Sesión"
            setupButtonLogin2() // Configurar nuevamente el botón "Continuar"

            Toast.makeText(this, "Su sesion se cerro exitosamente", Toast.LENGTH_SHORT).show()
        }
    }

    private fun replaceView(layoutId: Int) {
        val newView = layoutInflater.inflate(layoutId, null)
        setContentView(newView)
        currentView = newView
        setupButtonExitLogin() // Solo configurar el botón "Regresar" en la nueva vista
        setupButtonCerrarSesion()
        setupButtonRegresarServicios()
    }

    private fun setupButtonExitLogin() {
        val botonRegresa = currentView?.findViewById<Button>(R.id.buttonRegresar)
        botonRegresa?.setOnClickListener {
            setContentView(R.layout.pantalla_inicio) // Volver a la pantalla de inicio
            currentView = findViewById(R.id.pantalla_inicio_layout) // Actualizar la referencia de la vista actual
            setupButtonRegister() // Configurar nuevamente el botón "Registrarse"
            setupButtonLogin() // Configurar nuevamente el botón "Iniciar Sesión"
            setupButtonLogin2() // Configurar nuevamente el botón "Continuar"

        }
    }
    private fun setupButtonRegresarServicios() {
        val botonRegresarServicios = currentView?.findViewById<Button>(R.id.buttonRegresar2)
        botonRegresarServicios?.setOnClickListener {
            // Navegar a la pantalla de servicios
            setContentView(R.layout.pantalla_servicios)
            currentView = findViewById(R.id.pantalla_servicios_layout)
            setupButtonConfirmarServicio() //configura nuevamente el boton de confirmar servicio
            setupButtonCerrarSesion() //configura nuevamente el boton de cerrar sesion
            ModificarRadioButtons() //configura nuevamente la funcionalidad de los radioButton
            setupEditTextFechaHora() //configura nuevamente la funcionalidad del seleccionador de fecha y hora
        }
    }
}







