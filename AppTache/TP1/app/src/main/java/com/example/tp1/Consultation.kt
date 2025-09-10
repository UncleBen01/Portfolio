package com.example.tp1

import android.app.AlertDialog
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.text.Editable
import android.view.MenuItem
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tp1.databinding.ActivityAccueilBinding
import com.example.tp1.databinding.ActivityConsultationBinding
import com.example.tp1.databinding.NavHeaderBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale

class Consultation : AppCompatActivity() {
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var binding: ActivityConsultationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConsultationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = intent.getStringExtra("tache_nom")

        setupDrawer()
        getUsername()
        recupererTache()


        binding.btnSave.setOnClickListener {
            binding.llButtons.visibility = Button.GONE
            binding.loadingPanel.visibility = ProgressBar.VISIBLE

            val id = intent.getStringExtra("tache_id")!!
            val valeur = binding.tvProgression.text.toString()
            val service = UtilitaireRetrofit.get()
            val call: Call<Void> = service.updateProgress(id, valeur)
            call.enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    binding.loadingPanel.visibility = ProgressBar.GONE
                    binding.llButtons.visibility = Button.VISIBLE
                    if (response.isSuccessful) {
                        // http 200 http tout s'est bien passé
                        val monIntent = Intent(this@Consultation, Accueil::class.java)
                        startActivity(monIntent)

                    } else {
                        // cas d'erreur http 400 404 etc.
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    // cas d'erreur de connexion
                    binding.loadingPanel.visibility = ProgressBar.GONE
                    binding.llButtons.visibility = Button.VISIBLE
                    val builder = AlertDialog.Builder(this@Consultation)
                    builder.setMessage(getString(R.string.erreur_serveur))
                        .setNegativeButton("Ok") { dialog, id -> }
                    builder.show()
                }
            })
        }

        binding.btnDelete.setOnClickListener{
            binding.llButtons.visibility = Button.GONE
            binding.loadingPanel.visibility = ProgressBar.VISIBLE

            val id = intent.getStringExtra("tache_id")!!
            val service = UtilitaireRetrofit.get()
            val call: Call<Void> = service.deleteTask(id)
            call.enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    binding.loadingPanel.visibility = ProgressBar.GONE
                    binding.llButtons.visibility = Button.VISIBLE
                    if (response.isSuccessful) {
                        // http 200 http tout s'est bien passé
                        val monIntent = Intent(this@Consultation, Accueil::class.java)
                        startActivity(monIntent)

                    } else {
                        // cas d'erreur http 400 404 etc.
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    // cas d'erreur de connexion
                    binding.loadingPanel.visibility = ProgressBar.GONE
                    binding.llButtons.visibility = Button.VISIBLE
                    val builder = AlertDialog.Builder(this@Consultation)
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

    private fun recupererTache(){
        val tachePourcentageAvancement = intent.getStringExtra("tache_pourcentageAvancement")
        val tachePourcentageTempsEcoule = intent.getStringExtra("tache_pourcentageTempsEcoule")
        val tacheDateEcheance = intent.getStringExtra("tache_dateEcheance")
        binding.tvProgression.text = Editable.Factory.getInstance().newEditable(tachePourcentageAvancement)
        binding.tvPourcentageTempsEcoule.text = tachePourcentageTempsEcoule
        binding.tvDateFin.text = tacheDateEcheance

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
                                val monIntent = Intent(this@Consultation, Connexion::class.java)
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