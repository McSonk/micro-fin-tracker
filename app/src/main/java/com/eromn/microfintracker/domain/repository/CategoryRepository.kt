package com.eromn.microfintracker.domain.repository

import com.eromn.microfintracker.data.Category

interface CategoryRepository {
    suspend fun getCategories(): List<Category>
}
