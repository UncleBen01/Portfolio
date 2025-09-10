package com.example.tp1

import android.app.AlertDialog
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.tp1.adapters.TacheAdapter
import com.example.tp1.databinding.ActivityAccueilBinding
import com.example.tp1.databinding.NavHeaderBinding
import com.example.tp1.models.Tache
import com.google.android.material.snackbar.Snackbar
import org.kickmyb.transfer.HomeItemResponse
import org.kickmyb.transfer.SigninResponse
import org.kickmyb.transfer.SignupRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Date
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class Accueil : AppCompatActivity() {
    private lateinit var binding: ActivityAccueilBinding
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var adapter: TacheAdapter

    var swipeRefresher: SwipeRefreshLayout? = null
    var loading: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccueilBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = getString(R.string.accueil)

        setupRecycler()
        fillRecycler()
        setupDrawer()

        getUsername()
        loading = findViewById(R.id.loadingPanel)

        swipeRefresher = findViewById(R.id.swiperefresh)
        swipeRefresher?.setOnRefreshListener {
            fillRecycler()
            swipeRefresher?.isRefreshing = false
        }


        binding.fab.setOnClickListener {
            val monIntent = Intent(this, Creation::class.java)
            startActivity(monIntent)
        }
    }

    private fun getUsername(){
        val username = UserSingleton.username
        val navigationView = binding.nvTiroir
        val headerView = navigationView.getHeaderView(0)
        val usernameTextView = headerView.findViewById<TextView>(R.id.tvNavHeader)
        usernameTextView.text = username
    }

    private fun setupDrawer() {
        setupDrawerApplicationBar()
        setupDrawerItemSelected()
        setupDrawerHeader()
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
                                    val monIntent = Intent(this@Accueil, Connexion::class.java)
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

    private fun setupRecycler() {
        adapter = TacheAdapter() // Créer l'adapteur
        binding.rvTacheAdapter.adapter = adapter // Assigner l'adapteur au RecyclerView
        binding.rvTacheAdapter.setHasFixedSize(true) // Option pour améliorer les performances
        binding.rvTacheAdapter.addItemDecoration( // Ajouter un séparateur entre chaque élément
            DividerItemDecoration(
                binding.rvTacheAdapter.context, DividerItemDecoration.VERTICAL
            )
        )
    }

    private fun fillRecycler(){
        binding.rvTacheAdapter.visibility = View.GONE
        loading?.visibility = View.VISIBLE
        val service = UtilitaireRetrofit.get()
        val call: Call<List<HomeItemResponse>> = service.getTasks()
        swipeRefresher?.isRefreshing = true
        call.enqueue(object : Callback<List<HomeItemResponse>> {
            override fun onResponse(call: Call<List<HomeItemResponse>>, response: Response<List<HomeItemResponse>>) {
                swipeRefresher?.isRefreshing = false
                loading?.visibility = View.GONE
                if (response.isSuccessful) {
                    // http 200 http tout s'est bien passé
                    val items = response.body()!!.map {
                        Tache(it.id.toString(), it.name, it.percentageDone.toString(), it.percentageTimeSpent.toString() + "%", it.deadline, Date())
                    }

                    adapter.submitList(items)
                    binding.rvTacheAdapter.visibility = View.VISIBLE
                } else {
                    // cas d'erreur http 400 404 etc.
                }
            }

            override fun onFailure(call: Call<List<HomeItemResponse>>, t: Throwable) {
                // cas d'erreur de connexion
                val builder = AlertDialog.Builder(this@Accueil)
                builder.setMessage(getString(R.string.erreur_serveur))
                    .setNegativeButton(getString(R.string.btn_reessayer)) { dialog, id -> }
                builder.show()
            }
        })
    }
}