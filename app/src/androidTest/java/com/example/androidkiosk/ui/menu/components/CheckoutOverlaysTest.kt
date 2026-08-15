package com.example.androidkiosk.ui.menu.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.androidkiosk.model.MenuItem
import com.example.androidkiosk.model.Order
import com.example.androidkiosk.model.SizeOption
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class CheckoutOverlaysTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val order = Order(
        id = "8fb46cb1-52a7-4e77-9030-b58be89b742f",
        orderNumber = "8FB46CB1",
        customerName = "Guest",
        items = emptyList(),
        total = 125.0
    )

    @Test
    fun qrRendersBundledMerchantCodeAndPaidActionIsSingleFire() {
        var reports = 0
        composeRule.setContent {
            MaterialTheme {
                QRPaymentOverlay(
                    order = order,
                    isSubmitting = false,
                    isComplete = false,
                    onPaid = { reports++ },
                    onDismiss = {}
                )
            }
        }

        composeRule.onNodeWithContentDescription("Merchant GCash QR code").assertExists()
        composeRule.onNodeWithText("I'VE PAID").performClick().assertIsNotEnabled()
        composeRule.runOnIdle { assertEquals(1, reports) }
    }

    @Test
    fun counterSubmissionIsSingleFire() {
        var submissions = 0
        composeRule.setContent {
            MaterialTheme {
                CounterPaymentOverlay(
                    order = order,
                    isSubmitting = false,
                    isComplete = false,
                    onSubmit = { submissions++ },
                    onDismiss = {}
                )
            }
        }

        composeRule.onNodeWithText("SUBMIT ORDER").performClick().assertIsNotEnabled()
        composeRule.runOnIdle { assertEquals(1, submissions) }
    }

    @Test
    fun itemDetailSelectsSizeAndDisablesOutOfStockSelection() {
        val item = MenuItem(
            id = "coffee",
            categoryName = "Drinks",
            name = "Coffee",
            price = 100.0,
            sizes = linkedMapOf(
                "Medium" to SizeOption(),
                "Large" to SizeOption(25.0)
            )
        )
        var selectedSize: String? = null
        composeRule.setContent {
            MaterialTheme {
                ItemDetailOverlay(
                    item = item,
                    stockBySize = mapOf("Medium" to 0, "Large" to 1),
                    onDismiss = {},
                    onAddToCart = { _, _, size -> selectedSize = size }
                )
            }
        }

        composeRule.onNodeWithText("Out of stock").assertExists()
        composeRule.onNodeWithText("Add to Cart").assertIsNotEnabled()
        composeRule.onNodeWithText("Large +₱25.00").performClick()
        composeRule.onNodeWithText("₱125.00").assertExists()
        composeRule.onNodeWithText("Add to Cart").performClick()
        composeRule.runOnIdle { assertEquals("Large", selectedSize) }
    }

    @Test
    fun firebaseFailureShowsRetryAction() {
        composeRule.setContent {
            MaterialTheme {
                QRPaymentOverlay(
                    order = order,
                    isSubmitting = false,
                    isComplete = false,
                    errorMessage = "Check the connection and try again.",
                    onPaid = {},
                    onDismiss = {}
                )
            }
        }

        composeRule.onNodeWithText("Unable to submit order").assertExists()
        composeRule.onNodeWithText("TRY AGAIN").assertExists()
    }

    @Test
    fun registrationScreenDisplaysAnonymousUidAndRetries() {
        var retries = 0
        composeRule.setContent {
            MaterialTheme {
                KioskAuthorizationScreen(
                    uid = "new-anonymous-kiosk-uid",
                    message = null,
                    onRetry = { retries++ }
                )
            }
        }

        composeRule.onNodeWithText("new-anonymous-kiosk-uid").assertExists()
        composeRule.onNodeWithText("Check registration").performClick()
        composeRule.runOnIdle { assertEquals(1, retries) }
    }
}
