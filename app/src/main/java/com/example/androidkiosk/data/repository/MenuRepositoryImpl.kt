package com.example.androidkiosk.data.repository

import com.example.androidkiosk.data.local.dao.MenuItemDao
import com.example.androidkiosk.data.local.entity.MenuItemEntity
import com.example.androidkiosk.domain.repository.MenuRepository
import com.example.androidkiosk.model.CategoryWithItems
import com.example.androidkiosk.model.MenuItem
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MenuRepositoryImpl @Inject constructor(
    private val database: FirebaseDatabase,
    private val menuItemDao: MenuItemDao
) : MenuRepository {

    override fun observeCategories(): Flow<List<CategoryWithItems>> {
        syncFirebaseToRoom()

        return menuItemDao.getAllMenuItems().map { entities ->
            entities.groupBy { it.categoryName }
                .map { (categoryName, items) ->
                    CategoryWithItems(
                        categoryName = categoryName,
                        items = items.map { it.toDomainModel() }
                    )
                }
        }
    }

    override fun observeBestSellers(): Flow<List<MenuItem>> {
        return menuItemDao.getBestSellers().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun refresh() {
        menuItemDao.clearAll()
    }

    
    @OptIn(DelicateCoroutinesApi::class)
    private fun syncFirebaseToRoom() {
        val categoriesRef = database.getReference("categories")

        categoriesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val entities = mutableListOf<MenuItemEntity>()

                for (categorySnapshot in snapshot.children) {
                    val categoryName = categorySnapshot.key ?: continue

                    for (itemSnapshot in categorySnapshot.children) {
                        try {
                            val item = itemSnapshot.getValue(MenuItem::class.java) ?: continue

                            if (item.name.isNotBlank()) {
                                val id = item.id.ifEmpty { itemSnapshot.key ?: "" }
                                entities.add(
                                    MenuItemEntity(
                                        id = id,
                                        name = item.name,
                                        price = item.price,
                                        imageUrl = item.imageUrl,
                                        available = item.available,
                                        categoryName = categoryName,
                                        isBestSeller = item.isBestSeller
                                    )
                                )
                            }
                        } catch (e: Exception) {
                            Timber.w(e, "Skipping malformed item in category: $categoryName")
                        }
                    }
                }
                kotlinx.coroutines.GlobalScope.launch {
                    try {
                        menuItemDao.clearAll()
                        menuItemDao.insertAll(entities)
                        Timber.d("Synced ${entities.size} menu items to local cache")
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to sync menu items to Room")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.e("Firebase listener cancelled: ${error.message}")
            }
        })
    }

    private fun MenuItemEntity.toDomainModel() = MenuItem(
        id = id,
        name = name,
        price = price,
        imageUrl = imageUrl,
        available = available,
        isBestSeller = isBestSeller
    )
}
