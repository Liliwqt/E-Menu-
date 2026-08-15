package com.example.androidkiosk.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ImageUrlValidatorTest {
    private val bucket = "menu-app.appspot.com"

    @Test
    fun `allows exact Firebase bucket URL shapes`() {
        val firebaseUrl = "https://firebasestorage.googleapis.com/v0/b/$bucket/o/image.png?alt=media"
        val pathUrl = "https://storage.googleapis.com/$bucket/images/item.png"
        val virtualHostUrl = "https://$bucket.storage.googleapis.com/images/item.png"
        assertEquals(firebaseUrl, ImageUrlValidator.sanitize(firebaseUrl, bucket))
        assertEquals(pathUrl, ImageUrlValidator.sanitize(pathUrl, bucket))
        assertEquals(virtualHostUrl, ImageUrlValidator.sanitize(virtualHostUrl, bucket))
    }

    @Test
    fun `rejects cleartext lookalike and unsafe data URI`() {
        assertNull(ImageUrlValidator.sanitize("http://firebasestorage.googleapis.com/v0/b/$bucket/o/a", bucket))
        assertNull(ImageUrlValidator.sanitize("https://firebasestorage.googleapis.com.evil.test/v0/b/$bucket/o/a", bucket))
        assertNull(ImageUrlValidator.sanitize("https://firebasestorage.googleapis.com/v0/b/other.appspot.com/o/a", bucket))
        assertNull(ImageUrlValidator.sanitize("https://storage.googleapis.com/other.appspot.com/a", bucket))
        assertNull(ImageUrlValidator.sanitize("data:image/png;base64,AAAA", bucket))
    }
}
