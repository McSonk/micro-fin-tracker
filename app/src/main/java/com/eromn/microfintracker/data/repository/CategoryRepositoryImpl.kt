package com.eromn.microfintracker.data.repository

import com.eromn.microfintracker.data.Category
import com.eromn.microfintracker.domain.repository.CategoryRepository

class CategoryRepositoryImpl : CategoryRepository {
    override suspend fun getCategories(): List<Category> {
        return Category.entries.sortedBy { it.displayName }
    }
}
