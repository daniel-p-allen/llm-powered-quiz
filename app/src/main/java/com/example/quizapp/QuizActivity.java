package com.example.quizapp;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.quizapp.adapter.QuizAdapter;
import com.example.quizapp.databinding.ActivityQuizBinding;
import com.example.quizapp.model.QuizItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;

public class QuizActivity extends AppCompatActivity {
    private ActivityQuizBinding b;
    private QuizAdapter adapter;

    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        b = ActivityQuizBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        String json = getIntent().getStringExtra("task");
        Type tList = new TypeToken<List<QuizItem>>(){}.getType();
        List<QuizItem> items = new Gson().fromJson(json, tList);

        b.title.setText(getIntent().getStringExtra("title"));
        adapter = new QuizAdapter(items);
        b.recyclerQuestions.setLayoutManager(new LinearLayoutManager(this));
        b.recyclerQuestions.setAdapter(adapter);

        b.submitButton.setOnClickListener(v -> {
            adapter.grade();
            b.submitButton.postDelayed(() -> {
                Intent i = new Intent(this, ResultsActivity.class);
                i.putExtra("answers", new Gson().toJson(adapter.getAnswers()));
                i.putExtra("title",   getIntent().getStringExtra("title"));
                startActivity(i);
            }, 1000);
        });
    }
}
