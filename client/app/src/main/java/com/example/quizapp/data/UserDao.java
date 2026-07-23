//package com.example.quizapp.data;
//
//import androidx.room.Dao;
//import androidx.room.Insert;
//import androidx.room.Query;
//
//import com.example.quizapp.model.User;
//
//@Dao
//public interface UserDao {
//    /** Insert a new user and return its generated ID */
//    @Insert
//    long insert(User user);
//
//    /** Look up a user by username */
//    @Query("SELECT * FROM users WHERE username = :name LIMIT 1")
//    User findByUsername(String name);
//
//    /** Delete a user by ID (if you ever need it) */
//    @Query("DELETE FROM users WHERE id = :id")
//    void deleteById(long id);
//}
