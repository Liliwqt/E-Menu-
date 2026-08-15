package com.example.androidkiosk.data.repository

import com.example.androidkiosk.data.local.entity.MenuItemEntity
import com.example.androidkiosk.model.MenuItem
import com.example.androidkiosk.model.SizeOption
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Pure Firebase/Room mapping kept separate so compatibility formats are unit-testable. */
internal object FirebaseMenuMapper {
    private val json = Json { ignoreUnknownKeys = true }

    fun parseSizes(raw: Any?): Map<String, SizeOption> = buildMap {
        for ((name, value) in raw.asStringMap()) {
            if (name.isBlank()) continue
            val modifier = value.asFiniteDouble()
                ?: value.asStringMap()["priceModifier"].asFiniteDouble()
                ?: 0.0
            put(name, SizeOption(modifier))
        }
    }

    fun parseInventory(raw: Any?): Map<String, Map<String, Int>> = buildMap {
        for ((categoryName, categoryValue) in raw.asStringMap()) {
            for ((itemId, itemValue) in categoryValue.asStringMap()) {
                val sizes = buildMap {
                    for ((sizeName, sizeValue) in itemValue.asStringMap()["sizes"].asStringMap()) {
                        val stock = sizeValue.asWholeInt()
                            ?: sizeValue.asStringMap()["stock"].asWholeInt()
                            ?: continue
                        put(sizeName, stock.coerceAtLeast(0))
                    }
                }
                put("$categoryName/$itemId", sizes)
            }
        }
    }

    fun toEntity(
        item: MenuItem,
        categoryName: String,
        snapshotKey: String,
        sizes: Map<String, SizeOption>
    ): MenuItemEntity? {
        val id = item.id.ifBlank { snapshotKey }
        if (id.isBlank() || categoryName.isBlank() || item.name.isBlank()) return null
        if (!item.price.isFinite() || item.price < 0.0) return null

        val safeSizes = sizes.filterValues { option ->
            option.priceModifier.isFinite() && item.price + option.priceModifier >= 0.0
        }
        return MenuItemEntity(
            id = id,
            categoryName = categoryName,
            name = item.name,
            price = item.price,
            imageUrl = item.imageUrl,
            available = item.available,
            isBestSeller = item.isBestSeller,
            sizesJson = json.encodeToString(safeSizes)
        )
    }

    fun toDomain(entity: MenuItemEntity): MenuItem = MenuItem(
        id = entity.id,
        categoryName = entity.categoryName,
        name = entity.name,
        price = entity.price,
        imageUrl = entity.imageUrl,
        available = entity.available,
        isBestSeller = entity.isBestSeller,
        sizes = runCatching {
            json.decodeFromString<Map<String, SizeOption>>(entity.sizesJson)
        }.getOrDefault(emptyMap())
    )

    private fun Any?.asStringMap(): Map<String, Any?> = (this as? Map<*, *>)
        ?.mapNotNull { (key, value) -> (key as? String)?.let { it to value } }
        ?.toMap()
        .orEmpty()

    private fun Any?.asFiniteDouble(): Double? = when (this) {
        is Number -> toDouble()
        is String -> toDoubleOrNull()
        else -> null
    }?.takeIf(Double::isFinite)

    private fun Any?.asWholeInt(): Int? {
        val number = asFiniteDouble() ?: return null
        if (number % 1.0 != 0.0 || number > Int.MAX_VALUE || number < Int.MIN_VALUE) return null
        return number.toInt()
    }
}
