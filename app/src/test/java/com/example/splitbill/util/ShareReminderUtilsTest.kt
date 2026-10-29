package com.example.splitbill.util

import com.example.splitbill.data.model.DebtSettlement
import org.junit.Assert.assertTrue
import org.junit.Test

class ShareReminderUtilsTest {

    @Test
    fun testGenerateReminderMessage() {
        val debt = DebtSettlement(
            fromMemberId = 2,
            fromMemberName = "Lan",
            fromMemberPhone = "0912345678",
            toMemberId = 1,
            toMemberName = "Hoàng",
            toMemberPhone = "0987654321",
            amount = 150000.0,
            groupName = "Du lịch Đà Lạt"
        )

        val message = ShareReminderUtils.generateReminderMessage(debt)

        assertTrue("Message should contain debtor name", message.contains("Lan"))
        assertTrue("Message should contain creditor name", message.contains("Hoàng"))
        assertTrue("Message should contain group name", message.contains("Du lịch Đà Lạt"))
        assertTrue("Message should contain amount", message.contains("150.000"))
        assertTrue("Message should contain friendly polite phrases", message.contains("Nhắc nợ thân thương"))
    }
}
