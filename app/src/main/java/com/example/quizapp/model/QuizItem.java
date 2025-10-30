package com.example.quizapp.model;

import java.util.List;

public class QuizItem {
    public String topic;
    public String question;
    public List<String> options;
    public int correct_index;
    public int userAnswer = -1;
}
