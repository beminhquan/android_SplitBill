package com.example.splitbill.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.splitbill.data.model.DebtSettlement

object ShareReminderUtils {

    const val PACKAGE_ZALO = "com.zing.zalo"
    const val PACKAGE_MESSENGER = "com.facebook.orca"

    fun generateReminderMessage(debt: DebtSettlement): String {
        val groupText = if (debt.groupName.isNotBlank()) "nhóm \"${debt.groupName}\"" else "nhóm chi tiêu"
        return """
            💸 [SplitBill] Nhắc nợ thân thương 💸
            
            Chào ${debt.fromMemberName} ơi!
            Trong $groupText, bạn có khoản chi tiêu cần chuyển lại cho ${debt.toMemberName} nhé:
            
            👉 Số tiền: ${CurrencyUtils.formatVnd(debt.amount)}
            👉 Nội dung: Quyết toán chi tiêu ${if (debt.groupName.isNotBlank()) debt.groupName else "SplitBill"}
            
            Bạn kiểm tra và gửi lại cho ${debt.toMemberName} sớm nha. Cảm ơn bạn nhiều nhiều! ❤️✨
        """.trimIndent()
    }

    /**
     * Sends message via specific app if installed, or falls back to system Chooser
     */
    fun sendReminderViaPackage(
        context: Context,
        debt: DebtSettlement,
        targetPackage: String? = null
    ) {
        val message = generateReminderMessage(debt)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Nhắc nợ chi tiêu ${debt.groupName}")
            putExtra(Intent.EXTRA_TEXT, message)
            if (targetPackage != null) {
                setPackage(targetPackage)
            }
        }

        try {
            if (targetPackage != null) {
                val packageManager = context.packageManager
                val isInstalled = try {
                    packageManager.getPackageInfo(targetPackage, 0)
                    true
                } catch (e: Exception) {
                    false
                }

                if (isInstalled) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    return
                } else {
                    val appName = if (targetPackage == PACKAGE_ZALO) "Zalo" else "Messenger"
                    Toast.makeText(context, "$appName chưa được cài đặt, đang mở ứng dụng khác...", Toast.LENGTH_SHORT).show()
                }
            }

            // Fallback to Chooser
            val chooser = Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "Nhắc nợ chi tiêu ${debt.groupName}")
                    putExtra(Intent.EXTRA_TEXT, message)
                },
                "Gửi nhắc nợ cho ${debt.fromMemberName} qua:"
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Không thể mở ứng dụng gửi tin nhắn: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Copies reminder text to clipboard
     */
    fun copyToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("SplitBill Reminder", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Đã sao chép nội dung nhắc nợ vào bộ nhớ tạm!", Toast.LENGTH_SHORT).show()
    }
}
