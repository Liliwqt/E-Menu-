package com.example.androidkiosk.data.repository

import com.example.androidkiosk.model.MenuItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class FirebaseMenuMapperTest {
    @Test
    fun `maps numeric and object size formats through Room`() {
        val sizes = FirebaseMenuMapper.parseSizes(
            linkedMapOf(
                "Medium" to 0L,
                "Large" to mapOf("priceModifier" to "25.5")
            )
        )
        val entity = FirebaseMenuMapper.toEntity(
            item = MenuItem(id = "coffee", name = "Coffee", price = 100.0),
            categoryName = "Drinks",
            snapshotKey = "ignored",
            sizes = sizes
        )!!

        val restored = FirebaseMenuMapper.toDomain(entity)

        assertEquals("Drinks", restored.categoryName)
        assertEquals(0.0, restored.sizes.getValue("Medium").priceModifier, 0.0)
        assertEquals(25.5, restored.sizes.getValue("Large").priceModifier, 0.0)
    }

    @Test
    fun `maps stock and keeps known items with missing sizes`() {
        val inventory = FirebaseMenuMapper.parseInventory(
            mapOf(
                "Drinks" to mapOf(
                    "coffee" to mapOf(
                        "sizes" to mapOf(
                            "Medium" to mapOf("stock" to 3L),
                            "Large" to mapOf("stock" to -2L)
                        )
                    ),
                    "tea" to mapOf("sizes" to emptyMap<String, Any>())
                )
            )
        )

        assertEquals(3, inventory.getValue("Drinks/coffee").getValue("Medium"))
        assertEquals(0, inventory.getValue("Drinks/coffee").getValue("Large"))
        assertEquals(emptyMap<String, Int>(), inventory.getValue("Drinks/tea"))
    }

    @Test
    fun `rejects invalid identity and unsafe negative pricing`() {
        assertEquals(
            null,
            FirebaseMenuMapper.toEntity(MenuItem(name = "Coffee"), "Drinks", "", emptyMap())
        )

        val sizes = FirebaseMenuMapper.parseSizes(mapOf("Broken" to -200.0))
        val entity = FirebaseMenuMapper.toEntity(
            MenuItem(id = "coffee", name = "Coffee", price = 100.0),
            "Drinks",
            "coffee",
            sizes
        )!!
        assertFalse(FirebaseMenuMapper.toDomain(entity).sizes.containsKey("Broken"))
    }
}
