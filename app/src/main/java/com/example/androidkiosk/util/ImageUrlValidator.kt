package com.example.androidkiosk.util

/** Validates image URLs against an allowlist of approved domains */
object ImageUrlValidator {

    private val ALLOWED_DOMAINS = setOf(
        "firebasestorage.googleapis.com",
        "storage.googleapis.com",
        "lh3.googleusercontent.com",
        "via.placeholder.com",
        "i.imgur.com",
        "imgur.com",
    )

    /** Maximum allowed length for data: URIs (~500 KB base64). */
    private const val MAX_DATA_URI_LENGTH = 680_000

    /** Safe raster image MIME types allowed in data: URIs. SVG is blocked (can contain scripts). */
    private val ALLOWED_DATA_MIME_PREFIXES = listOf(
        "data:image/png",
        "data:image/jpeg",
        "data:image/jpg",
        "data:image/webp",
        "data:image/gif",
    )

    /** Returns the URL if it belongs to an allowed domain, or null otherwise. */
    fun sanitize(url: String?): String? {
        if (url.isNullOrBlank()) return null

        // Validate data: URIs — allow only safe raster types, block SVG (XSS risk)
        if (url.startsWith("data:image/")) {
            val isSafeType = ALLOWED_DATA_MIME_PREFIXES.any { url.startsWith(it) }
            return if (isSafeType && url.length <= MAX_DATA_URI_LENGTH) url else null
        }

        return try {
            val host = java.net.URI(url).host?.lowercase() ?: return null
            if (ALLOWED_DOMAINS.any { host == it || host.endsWith(".$it") }) url else null
        } catch (_: Exception) {
            null
        }
    }
}
