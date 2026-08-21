package com.eromn.microfintracker.ui.extensions

import androidx.annotation.DrawableRes
import com.eromn.microfintracker.R
import com.eromn.microfintracker.data.Category

/**
 * Maps a business logic Category to a UI-specific drawable resource.
 * This keeps the Android framework dependency (R.drawable) out of the data layer.
 */
@get:DrawableRes
val Category.iconRes: Int
    get() = when (this) {
        Category.TRANSPORT -> R.drawable.ic_directions_bus_24 // Replace with your actual drawable
        Category.RESTAURANTS -> R.drawable.ic_restaurant_24
        Category.FOOD -> R.drawable.ic_breakfast_dining_24
        Category.ENTERTAINMENT -> R.drawable.ic_theater_comedy_24
        Category.HEALTH -> R.drawable.ic_local_hospital_24
        Category.MAINTENANCE -> R.drawable.ic_build_24
        Category.CLOTHING -> R.drawable.ic_checkroom_24
        Category.TRAVEL -> R.drawable.ic_flight_takeoff_24
        Category.EDUCATION -> R.drawable.ic_school_24
        Category.UNFORESEEN -> R.drawable.ic_balance_24
        Category.PERSONAL_CARE -> R.drawable.ic_content_cut_24
        Category.TECHNOLOGY -> R.drawable.ic_computer_sound_24
        Category.DANCE_SPORTS -> R.drawable.ic_sports_gymnastics_24
        Category.SERVICES -> R.drawable.ic_water_drop_24
        Category.DRINKS -> R.drawable.ic_local_cafe_24
        Category.SNACKS -> R.drawable.ic_cake_24
        Category.STATIONERY -> R.drawable.ic_stylus_note_24
        Category.PANTRY -> R.drawable.ic_shopping_bag_24
        Category.DONATIONS -> R.drawable.ic_mood_heart_24
        Category.CONVENIENCE -> R.drawable.ic_local_convenience_store_24
        else -> R.drawable.ic_question_mark_24
    }