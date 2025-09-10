package com.example.tp1

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tp1.databinding.ActivityConnexionBinding
import com.example.tp1.databinding.ActivityInscriptionBinding
import org.kickmyb.transfer.SigninResponse
import org.kickmyb.transfer.SignupRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Inscription : AppCompatActivity() {

    private lateinit var binding: ActivityInscriptionBinding



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_inscription)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding = ActivityInscriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnInscription.setOnClickListener {
            binding.btnInscription.visibility = Button.GONE
            binding.loadingPanel.visibility = ProgressBar.VISIBLE

            val service = UtilitaireRetrofit.get()
            val request = SignupRequest()
            request.username = binding.etUsername.text.toString()
            request.password = binding.etPassword.text.toString()
            val call: Call<SigninResponse> = service.signup(request)
            if(binding.etPassword.text.toString() == binding.etConfirmPassword.text.toString()){
                call.enqueue(object : Callback<SigninResponse> {
                    override fun onResponse(call: Call<SigninResponse>, response: Response<SigninResponse>) {
                        binding.btnInscription.visibility = Button.GONE
                        binding.loadingPanel.visibility = ProgressBar.VISIBLE
                        if (response.isSuccessful) {
                            // http 200 http tout s'est bien passé
                            UserSingleton.username = request.username
                            val monIntent = Intent(this@Inscription, Accueil::class.java)
                            startActivity(monIntent)

                        } else {
                            // cas d'erreur http 400 404 etc.
                            val erreur = response.errorBody()!!.string()
                            if (erreur.contains("UsernameAlreadyTaken")) {
                                binding.etUsername.error = getString(R.string.erreur_nomUtilisateur)
                                val builder = AlertDialog.Builder(this@Inscription)
                                builder.setMessage(getString(R.string.erreur_nomUtilisateur)).setNegativeButton(getString(R.string.btn_modifier)) { dialog, id -> }
                                builder.show()
                                binding.etUsername.requestFocus()
                            }

                            if(erreur.contains("UsernameTooShort")){
                                binding.etUsername.error = getString(R.string.minimum_caractere_nom)
                                val builder = AlertDialog.Builder(this@Inscription)
                                builder.setMessage(getString(R.string.erreur_nomUtilisateur_trop_court)).setNegativeButton(getString(R.string.btn_modifier)) { dialog, id -> }
                                builder.show()
                                binding.etUsername.requestFocus()
                            }

                            if(erreur.contains("PasswordTooShort")){
                                binding.etPassword.error =
                                    getString(R.string.minimum_caractere_password)
                                val builder = AlertDialog.Builder(this@Inscription)
                                builder.setMessage(getString(R.string.erreur_password_court)).setNegativeButton(getString(R.string.btn_modifier)) { dialog, id -> }
                                builder.show()
                                binding.etPassword.requestFocus()
                            }
                            binding.loadingPanel.visibility = ProgressBar.GONE
                            binding.btnInscription.visibility = Button.VISIBLE
                        }
                    }

                    override fun onFailure(call: Call<SigninResponse>, t: Throwable) {
                        // cas d'erreur de connexion
                        binding.btnInscription.visibility = Button.VISIBLE
                        binding.loadingPanel.visibility = ProgressBar.GONE
                        val builder = AlertDialog.Builder(this@Inscription)
                        builder.setMessage(getString(R.string.erreur_serveur)).setNegativeButton("Ok") { dialog, id -> }
                        builder.show()
                    }
                })
            }else{
                binding.etConfirmPassword.error = getString(R.string.erreur_password_different)
                val builder = AlertDialog.Builder(this@Inscription)
                builder.setMessage(getString(R.string.erreur_password_different)).setNegativeButton(getString(R.string.btn_modifier)) { dialog, id -> }
                builder.show()
                binding.etConfirmPassword.requestFocus()
                binding.loadingPanel.visibility = ProgressBar.GONE
                binding.btnInscription.visibility = Button.VISIBLE
            }
        }

    }
}