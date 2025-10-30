package com.example.quizapp.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.List;

@Entity(tableName = "quiz_sessions")
public class QuizSession {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long timestamp;
    public int totalQuestions;
    public int correctCount;
    public int incorrectCount;
    /** NEW: who this session belongs to */
    public String username;
    // Store the list of incorrect answers as JSON
    public String incorrectAnswersJson;
}
