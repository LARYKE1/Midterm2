package com.example.midterm2;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ContactDao {

    @Query("SELECT * FROM Contact")
    List<Contact> getAll();
    @Query("SELECT * FROM Contact WHERE Contact_Name=:nameWanted")
    Contact getByName(String nameWanted);
    @Insert
    void insertData (Contact...contacts);

    @Query("DELETE FROM contact WHERE id=:contactId")
    void delete(int contactId);

    @Update
    void update(Contact... contact);
}

