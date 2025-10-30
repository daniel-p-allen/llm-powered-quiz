// app/src/main/java/com/example/quizapp/model/User.java
package com.example.quizapp.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {
    @PrimaryKey(autoGenerate = true) public long id;
    public String username;
    public String email;
}
