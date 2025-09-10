package com.example.tp1.adapters

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tp1.Connexion
import com.example.tp1.Consultation
import com.example.tp1.R
import com.example.tp1.UtilitaireRetrofit
import com.example.tp1.databinding.TacheItemBinding
import com.example.tp1.models.Tache
import org.kickmyb.transfer.TaskDetailResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale

class TacheAdapter : ListAdapter<Tache, TacheAdapter.TacheItemViewHolder>(TacheItemDiffCallback) {

    // binding nous permet d'accéder à tout le champs de notre layout tache_item.xml
    inner class TacheItemViewHolder(private val binding: TacheItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(tache: Tache) {
            binding.tvNom.text = tache.nom
            binding.tvProgression.text = tache.pourcentageAvancement
            binding.tvPourcentageTempsEcoule.text = tache.pourcentageTempsEcoule

            val inputFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.getDefault())
            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = inputFormat.parse(tache.dateEchance.toString())
            val formattedDate = outputFormat.format(date)
            binding.tvDateFin.text = formattedDate

            binding.tacheItem.setOnClickListener {
                val context = binding.root.context

                val builder = AlertDialog.Builder(context)
                builder.setMessage(context.getString(R.string.chargement))
                builder.setCancelable(false)
                val progressDialog = builder.show()

                val service = UtilitaireRetrofit.get()
                val call: Call<TaskDetailResponse> = service.getDetailTask(tache.id)
                call.enqueue(object : Callback<TaskDetailResponse> {
                    override fun onResponse(call: Call<TaskDetailResponse>, response: Response<TaskDetailResponse>) {
                        progressDialog.dismiss()
                        if (response.isSuccessful) {
                            // http 200 http tout s'est bien passé
                            val monIntent = Intent(context, Consultation::class.java)
                            monIntent.putExtra("tache_id", tache.id)
                            monIntent.putExtra("tache_nom", tache.nom)
                            monIntent.putExtra("tache_pourcentageAvancement", tache.pourcentageAvancement)
                            monIntent.putExtra("tache_pourcentageTempsEcoule", tache.pourcentageTempsEcoule)
                            monIntent.putExtra("tache_dateEcheance", formattedDate)
                            context.startActivity(monIntent)

                        } else {
                            // cas d'erreur http 400 404 etc.
                        }
                    }

                    override fun onFailure(call: Call<TaskDetailResponse>, t: Throwable) {
                        // cas d'erreur de connexion
                        progressDialog.dismiss()
                    }
                })
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TacheItemViewHolder {
        val binding: TacheItemBinding = TacheItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TacheItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TacheItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

object TacheItemDiffCallback : DiffUtil.ItemCallback<Tache>() {
    override fun areItemsTheSame(oldItem: Tache, newItem: Tache): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Tache, newItem: Tache): Boolean {
        return oldItem.nom == newItem.nom
    }
}



