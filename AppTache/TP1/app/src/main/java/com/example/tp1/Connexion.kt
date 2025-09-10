package com.example.tp1

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tp1.databinding.ActivityConnexionBinding
import org.kickmyb.transfer.SigninRequest
import org.kickmyb.transfer.SigninResponse
import org.kickmyb.transfer.SignupRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Connexion : AppCompatActivity() {

    private lateinit var binding: ActivityConnexionBinding

    var loading: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_connexion)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding = ActivityConnexionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loading = findViewById(R.id.loadingPanel)

        binding.btnInscription.setOnClickListener {
            val monIntent = Intent(this, Inscription::class.java)
            startActivity(monIntent)
        }

        binding.btnConnexion.setOnClickListener {
            loading?.visibility = ProgressBar.VISIBLE
            binding.btnConnexion.visibility = Button.GONE
            binding.btnInscription.isEnabled = false
            val service = UtilitaireRetrofit.get()
            val request = SigninRequest()
            request.username = binding.etUsername.text.toString()
            request.password = binding.etPassword.text.toString()
            val call: Call<SigninResponse> = service.signin(request)
            call.enqueue(object : Callback<SigninResponse> {
                override fun onResponse(call: Call<SigninResponse>, response: Response<SigninResponse>) {
                    binding.btnInscription.isEnabled = true
                    if (response.isSuccessful) {
                        // http 200 http tout s'est bien passé
                        UserSingleton.username = request.username
                        val monIntent = Intent(this@Connexion, Accueil::class.java)
                        startActivity(monIntent)

                    } else {
                        // cas d'erreur http 400 404 etc.
                        val builder = AlertDialog.Builder(this@Connexion)
                        builder.setMessage(getString(R.string.erreur_connexion)).setNegativeButton(getString(R.string.btn_modifier)){ dialog, id ->}
                        builder.show()
                        binding.btnConnexion.visibility = Button.VISIBLE
                        loading?.visibility = ProgressBar.GONE
                    }
                }

                override fun onFailure(call: Call<SigninResponse>, t: Throwable) {
                    // cas d'erreur de connexion
                    binding.btnInscription.isEnabled = true
                    binding.btnConnexion.visibility = Button.GONE
                    val builder = AlertDialog.Builder(this@Connexion)
                    builder.setMessage(getString(R.string.erreur_serveur)).setNegativeButton("Ok"){dialog, id ->}
                    builder.show()
                }
            })
        }
    }


}