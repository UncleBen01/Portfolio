package com.example.tp1.models

import java.util.Date


data class Tache(val id: String,
                 val nom: String,
                 val pourcentageAvancement: String,
                 val pourcentageTempsEcoule: String,
                 val dateEchance:  Date,
                 val dateCreation:  Date)
