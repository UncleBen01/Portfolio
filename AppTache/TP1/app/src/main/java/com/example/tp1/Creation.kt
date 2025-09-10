package com.example.tp1

import android.app.AlertDialog
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.CalendarView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tp1.databinding.ActivityAccueilBinding
import com.example.tp1.databinding.ActivityCreationBinding
import com.example.tp1.databinding.NavHeaderBinding
import org.kickmyb.transfer.AddTaskRequest
import org.kickmyb.transfer.SigninRequest
import org.kickmyb.transfer.SigninResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class Creation : AppCompatActivity() {
    private lateinit var binding: ActivityCreationBinding
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var selectedDate: Date

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = getString(R.string.nouvelle_tache)
        setupDrawer()
        getUsername()

        selectedDate = Date()
        binding.calendrier.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(year, month, dayOfMonth)
            selectedDate = selectedCalendar.time
        }

        binding.btnCreation.setOnClickListener {
            binding.btnCreation.visibility = Button.GONE
            binding.loadingPanel.visibility = ProgressBar.VISIBLE

            val service = UtilitaireRetrofit.get()
            val request = AddTaskRequest()
            request.name = binding.etNomTache.text.toString()
            request.deadline = selectedDate
            val call = service.addTask(request)
            call.enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    binding.btnCreation.visibility = Button.GONE
                    binding.loadingPanel.visibility = ProgressBar.VISIBLE
                    if (response.isSuccessful) {
                        // http 200 http tout s'est bien passé
                        val monIntent = Intent(this@Creation, Accueil::class.java)
                        startActivity(monIntent)

                    } else {
                        // cas d'erreur http 400 404 etc.
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    // cas d'erreur de connexion
                    binding.loadingPanel.visibility = ProgressBar.GONE
                    binding.btnCreation.visibility = Button.VISIBLE
                    val builder = AlertDialog.Builder(this@Creation)
                    builder.setMessage(getString(R.string.erreur_serveur))
                        .setNegativeButton("Ok") { dialog, id -> }
                    builder.show()
                }
            })
        }

    }

    private fun setupDrawer() {
        setupDrawerApplicationBar()
        setupDrawerItemSelected()
        setupDrawerHeader()
    }

    private fun getUsername(){
        val username = UserSingleton.username
        val navigationView = binding.nvTiroir
        val headerView = navigationView.getHeaderView(0)
        val usernameTextView = headerView.findViewById<TextView>(R.id.tvNavHeader)
        usernameTextView.text = username
    }

    private fun setupDrawerApplicationBar() {
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        // Lier le tiroir de navigation à l'activité
        // R.string.ouvert et R.string.ferme sont des strings de ressource.
        // Référez-vous à la recette pour les strings de ressource pour voir comment les ajouter :
        // https://info.cegepmontpetit.ca/3N5-Prog3/recettes/ressources-string
        actionBarDrawerToggle = ActionBarDrawerToggle(this, binding.dlTiroir, R.string.ouvert, R.string.ferme)

        // Faire en sorte que le menu hamburger se transforme en flèche au clic, et vis versa
        binding.dlTiroir.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()
    }

    private fun setupDrawerItemSelected() {
        binding.nvTiroir.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.accueil_item -> {
                    val monIntent = Intent(this, Accueil::class.java)
                    startActivity(monIntent)
                }
                R.id.ajouter_item -> {
                    val monIntent = Intent(this, Creation::class.java)
                    startActivity(monIntent)
                }
                R.id.deconnexion_item -> {
                    val builder = AlertDialog.Builder(this)
                    builder.setMessage(this.getString(R.string.chargement))
                    builder.setCancelable(false)
                    val progressDialog = builder.show()

                    val service = UtilitaireRetrofit.get()
                    val call: Call<Void> = service.signout()
                    call.enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            progressDialog.dismiss()
                            if (response.isSuccessful) {
                                // http 200 http tout s'est bien passé
                                val monIntent = Intent(this@Creation, Connexion::class.java)
                                startActivity(monIntent)

                            } else {
                                // cas d'erreur http 400 404 etc.
                            }
                        }

                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            // cas d'erreur de connexion
                            progressDialog.dismiss()
                        }
                    })
                }
            }
            false
        }
    }

    private fun setupDrawerHeader() {
        // Si on veut avoir du contenu dynamique dans l'en-tête,
        val headerBinding: NavHeaderBinding = NavHeaderBinding.bind(binding.nvTiroir.getHeaderView(0))
    }

    // Se déclenche lorsqu'un élément de la barre d'application est sélectionné
    // Exemple : lorsqu'on clique sur le menu hamburger
    // Peut aussi se combiner avec les autres options de menu qui se retrouvent dans la barre d'application
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Réagir au clic sur le menu hamburger
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        actionBarDrawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        actionBarDrawerToggle.onConfigurationChanged(newConfig)
        super.onConfigurationChanged(newConfig)
    }
}