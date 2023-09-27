package com.openclassrooms.realestatemanager.application

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.openclassrooms.realestatemanager.database.PropertyDatabase

class RealEstateApplication : Application() {
    companion object {
        @Volatile
        private var INSTANCE: PropertyDatabase? = null

        fun getInstance(context: Context): PropertyDatabase {
            if (INSTANCE == null) {
                synchronized(this) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(
                            context.applicationContext,
                            PropertyDatabase::class.java,
                            "property_database"
                        )
                            .fallbackToDestructiveMigration()
                            .allowMainThreadQueries()
                            .build()
                    }
                }
            }

            return INSTANCE!!
        }
    }
}