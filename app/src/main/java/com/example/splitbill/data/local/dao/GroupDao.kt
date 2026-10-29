package com.example.splitbill.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.splitbill.data.local.entity.GroupEntity
import com.example.splitbill.data.model.GroupSummary
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {
    @Query("""
        SELECT g.*, 
            (SELECT COUNT(*) FROM members WHERE groupId = g.id) AS memberCount,
            (SELECT COALESCE(SUM(amount), 0.0) FROM expenses WHERE groupId = g.id) AS totalExpense
        FROM groups g 
        ORDER BY g.createdAt DESC
    """)
    fun getAllGroupSummaries(): Flow<List<GroupSummary>>

    @Query("SELECT * FROM groups ORDER BY createdAt DESC")
    fun getAllGroups(): Flow<List<GroupEntity>>

    @Query("SELECT * FROM groups WHERE id = :groupId LIMIT 1")
    fun getGroupById(groupId: Long): Flow<GroupEntity?>

    @Query("SELECT * FROM groups WHERE id = :groupId LIMIT 1")
    suspend fun getGroupByIdSync(groupId: Long): GroupEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: GroupEntity): Long

    @Update
    suspend fun updateGroup(group: GroupEntity)

    @Delete
    suspend fun deleteGroup(group: GroupEntity)

    @Query("DELETE FROM groups WHERE id = :groupId")
    suspend fun deleteGroupById(groupId: Long)
}
