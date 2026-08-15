package com.example.androidkiosk.admin

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KioskReleaseGateTest {
    @Test
    fun `release blocks ordering until device-owner kiosk is active`() {
        assertTrue(KioskReleaseGate.isOrderingBlocked(false, false, KioskEnforcementStatus.NOT_DEVICE_OWNER))
        assertTrue(KioskReleaseGate.isOrderingBlocked(false, false, KioskEnforcementStatus.ERROR))
        assertFalse(KioskReleaseGate.isOrderingBlocked(false, false, KioskEnforcementStatus.ACTIVE))
    }

    @Test
    fun `debug and authenticated maintenance remain usable`() {
        assertFalse(KioskReleaseGate.isOrderingBlocked(true, false, KioskEnforcementStatus.NOT_DEVICE_OWNER))
        assertFalse(KioskReleaseGate.isOrderingBlocked(false, true, KioskEnforcementStatus.ADMIN_UNLOCKED))
    }
}
