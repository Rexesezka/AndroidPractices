package com.example.myapp.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteGameDao {

    @Query("SELECT * FROM favorite_games ORDER BY savedAt DESC")
    fun observeAll(): Flow<List<FavoriteGameEntity>>

    @Query("SELECT id FROM favorite_games")
    fun observeIds(): Flow<List<Int>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_games WHERE id = :gameId)")
    fun observeIsFavorite(gameId: Int): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_games WHERE id = :gameId)")
    suspend fun isFavorite(gameId: Int): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FavoriteGameEntity)

    @Query("DELETE FROM favorite_games WHERE id = :gameId")
    suspend fun deleteById(gameId: Int)
}
