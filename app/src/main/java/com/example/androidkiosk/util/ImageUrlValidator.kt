package com.example.androidkiosk.util

import java.net.URI

/** Allows HTTPS image objects from exactly one configured Firebase Storage bucket. */
object ImageUrlValidator {
    fun sanitize(url: String?, storageBucket: String): String? {
        if (url.isNullOrBlank() || storageBucket.isBlank()) return null

        return try {
            val uri = URI(url)
            if (!uri.scheme.equals("https", ignoreCase = true)) return null
            if (uri.userInfo != null || uri.fragment != null || uri.port !in listOf(-1, 443)) return null

            val host = uri.host?.lowercase() ?: return null
            val bucket = storageBucket.lowercase()
            val path = uri.path ?: return null
            if (path.split('/').any { it == "." || it == ".." }) return null

            val isAllowed = when (host) {
                FIREBASE_STORAGE_HOST -> path.startsWith("/v0/b/$bucket/o/")
                GOOGLE_STORAGE_HOST -> path.startsWith("/$bucket/")
                "$bucket.$GOOGLE_STORAGE_HOST" -> path.startsWith("/") && path.length > 1
                else -> false
            }
            url.takeIf { isAllowed }
        } catch (_: Exception) {
            null
        }
    }

    private const val FIREBASE_STORAGE_HOST = "firebasestorage.googleapis.com"
    private const val GOOGLE_STORAGE_HOST = "storage.googleapis.com"
}
