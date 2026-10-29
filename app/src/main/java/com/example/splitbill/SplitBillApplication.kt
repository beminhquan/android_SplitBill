package com.example.splitbill

import android.app.Application
import com.example.splitbill.data.local.AppDatabase
import com.example.splitbill.data.repository.SplitBillRepository
import com.example.splitbill.data.repository.SplitBillRepositoryImpl

class SplitBillApplication : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }

    val repository: SplitBillRepository by lazy {
        SplitBillRepositoryImpl(
            groupDao = database.groupDao(),
            memberDao = database.memberDao(),
            expenseDao = database.expenseDao()
        )
    }
}
