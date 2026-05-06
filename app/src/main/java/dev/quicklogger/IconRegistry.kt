package dev.quicklogger

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Sick
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.ui.graphics.vector.ImageVector

data class LogIcon(
    val key: String,
    val label: String,
    val vector: ImageVector,
)

fun iconFor(key: String): LogIcon =
    StandardIcons.firstOrNull { it.key == key } ?: StandardIcons.first()

val StandardIcons = listOf(
    LogIcon("edit", "Edit", Icons.Filled.Edit),
    LogIcon("medication", "Medication", Icons.Filled.Medication),
    LogIcon("coffee", "Coffee", Icons.Filled.LocalCafe),
    LogIcon("meal", "Meal", Icons.Filled.Restaurant),
    LogIcon("fitness", "Fitness", Icons.Filled.FitnessCenter),
    LogIcon("walk", "Walk", Icons.AutoMirrored.Filled.DirectionsWalk),
    LogIcon("sleep", "Sleep", Icons.Filled.Bedtime),
    LogIcon("sick", "Sick", Icons.Filled.Sick),
    LogIcon("healing", "Healing", Icons.Filled.Healing),
    LogIcon("mind", "Mind", Icons.Filled.Psychology),
    LogIcon("book", "Book", Icons.AutoMirrored.Filled.MenuBook),
    LogIcon("audio", "Audio", Icons.Filled.Headphones),
    LogIcon("movie", "Movie", Icons.Filled.Movie),
    LogIcon("tv", "TV", Icons.Filled.Tv),
    LogIcon("game", "Game", Icons.Filled.SportsEsports),
    LogIcon("code", "Code", Icons.Filled.Code),
    LogIcon("check", "Check", Icons.Filled.CheckCircle),
    LogIcon("warning", "Warning", Icons.Filled.Warning),
    LogIcon("fire", "Fire", Icons.Filled.Whatshot),
    LogIcon("star", "Star", Icons.Filled.Star),
    LogIcon("sun", "Sun", Icons.Filled.WbSunny),
    LogIcon("car", "Car", Icons.Filled.DirectionsCar),
    LogIcon("flight", "Flight", Icons.Filled.Flight),
    LogIcon("home", "Home", Icons.Filled.Home),
    LogIcon("clean", "Clean", Icons.Filled.CleaningServices),
    LogIcon("laundry", "Laundry", Icons.Filled.LocalLaundryService),
    LogIcon("money", "Money", Icons.Filled.Payments),
    LogIcon("receipt", "Receipt", Icons.AutoMirrored.Filled.ReceiptLong),
    LogIcon("target", "Target", Icons.Filled.AdsClick),
    LogIcon("notes", "Notes", Icons.AutoMirrored.Filled.Notes),
    LogIcon("pin", "Pin", Icons.Filled.PushPin),
    LogIcon("timer", "Timer", Icons.Filled.Timer),
    LogIcon("science", "Science", Icons.Filled.Science),
    LogIcon("heart", "Heart", Icons.Filled.Favorite),
    LogIcon("visible", "Visible", Icons.Filled.Visibility),
)
