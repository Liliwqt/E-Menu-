package com.example.androidkiosk.data.repository

import com.example.androidkiosk.admin.AuthManager
import com.example.androidkiosk.data.local.dao.MenuItemDao
import com.example.androidkiosk.data.local.entity.MenuItemEntity
import com.example.androidkiosk.domain.repository.MenuRepository
import com.example.androidkiosk.model.CategoryWithItems
import com.example.androidkiosk.model.MenuItem
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MenuRepositoryImpl @Inject constructor(
    private val database: FirebaseDatabase,
    private val menuItemDao: MenuItemDao,
    private val externalScope: CoroutineScope,
    private val authManager: AuthManager
) : MenuRepository {

    @Volatile
    private var firebaseListenerAttached = false

    override fun observeCategories(): Flow<List<CategoryWithItems>> {
        // Only attach Firebase listener once auth is confirmed
        ensureFirebaseSync()

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

    /** Waits for Firebase anonymous auth before attaching the ValueEventListener. */
    private fun ensureFirebaseSync() {
        if (firebaseListenerAttached) return

        externalScope.launch {
            // Wait until authenticated
            authManager.isAuthenticated.collect { authenticated ->
                if (authenticated && !firebaseListenerAttached) {
                    firebaseListenerAttached = true
                    attachFirebaseListener()
                    return@collect
                }
            }
        }
    }

    private fun attachFirebaseListener() {
        val categoriesRef = database.getReference("branch2/categories") // Branch Switch

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
                externalScope.launch {
                    try {
                        menuItemDao.replaceAll(entities)
                        Timber.d("Synced ${entities.size} menu items to local cache")
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to sync menu items to Room")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.e(error.toException(), "Firebase listener cancelled")
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
