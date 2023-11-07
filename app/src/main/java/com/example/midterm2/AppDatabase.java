package com.example.midterm2;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities={Contact.class},version = 1, exportSchema = false)
public abstract class AppDatabase  extends RoomDatabase {

    //name of the database
    private static final String DATABASE_NAME="midtermDb.db";
    private static AppDatabase appDatabase;

    //Singleton Patter
    //Database to be Thread-safe
    public static synchronized AppDatabase getInstance(Context context){
        //check if the database is null, if it is create a new one
        if(appDatabase==null){
            appDatabase= Room.databaseBuilder(context.getApplicationContext(),AppDatabase.class,DATABASE_NAME).build();
        }

        return appDatabase;
    }

    public abstract ContactDao contactDao();

}
