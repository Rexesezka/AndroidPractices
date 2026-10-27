package com.example.myapp.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [FavoriteGameEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun favoriteGameDao(): FavoriteGameDao

    companion object {
        private const val DB_NAME = "myapp.db"

        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME).build()
    }
}
