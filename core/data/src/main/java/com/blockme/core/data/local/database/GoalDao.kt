package com.blockme.core.data.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.blockme.core.data.local.entity.GoalEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for focus goals.
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
@Dao
interface GoalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: GoalEntity): Long

    @Query("SELECT * FROM goals ORDER BY lastUsed DESC LIMIT :limit")
    fun getRecent(limit: Int): Flow<List<GoalEntity>>

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM goals WHERE text = :text LIMIT 1")
    suspend fun getByText(text: String): GoalEntity?
}
