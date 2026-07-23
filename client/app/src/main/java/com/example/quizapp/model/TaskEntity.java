package com.example.quizapp.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tasks")
public class TaskEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    // 2. Scope to user
    public String username;

    public String title;
    public String description;
    public String questionJson;

    public TaskEntity() {}

    public TaskEntity(String username, String title, String description, String questionJson) {
        this.username     = username;
        this.title        = title;
        this.description  = description;
        this.questionJson = questionJson;
    }
}