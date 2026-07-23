package com.example.quizapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.quizapp.adapter.ResultsAdapter;
import com.example.quizapp.data.AppDatabase;
import com.example.quizapp.data.QuizSessionDao;
import com.example.quizapp.data.TaskDao;
import com.example.quizapp.databinding.ActivityResultsBinding;
import com.example.quizapp.model.QuizItem;
import com.example.quizapp.model.QuizSession;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ResultsActivity extends AppCompatActivity {
    private ActivityResultsBinding b;
    private final ExecutorService dbExec = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityResultsBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        // 1) Deserialize answers and quiz title
        String answersJson = getIntent().getStringExtra("answers");
        String title       = getIntent().getStringExtra("title");

        Type listType = new TypeToken<List<QuizItem>>() {}.getType();
        List<QuizItem> results = new Gson().fromJson(answersJson, listType);

        // 2) Display the question-by-question results
        ResultsAdapter adapter = new ResultsAdapter(results);
        b.recyclerResults.setLayoutManager(new LinearLayoutManager(this));
        b.recyclerResults.setAdapter(adapter);

        // 3) Persist the session and delete the completed task
        dbExec.execute(() -> {
            // a) Get current user
            String user = getSharedPreferences("quiz_prefs", MODE_PRIVATE)
                    .getString("username", "");

            QuizSessionDao sessionDao = AppDatabase.getInstance(this).quizSessionDao();
            TaskDao        taskDao    = AppDatabase.getInstance(this).taskDao();

            // b) Compute totals without streams
            int total     = results.size();
            int correct   = 0;
            List<QuizItem> wrongList = new ArrayList<>();
            for (QuizItem qi : results) {
                if (qi.userAnswer == qi.correct_index) {
                    correct++;
                } else {
                    wrongList.add(qi);
                }
            }
            int incorrect = total - correct;

            // c) Build and insert the session
            QuizSession session = new QuizSession();
            session.username             = user;
            session.timestamp            = System.currentTimeMillis();
            session.totalQuestions       = total;
            session.correctCount         = correct;
            session.incorrectCount       = incorrect;
            session.incorrectAnswersJson = answersJson;
            sessionDao.insert(session);

            // d) Delete only this user’s pending task
            taskDao.deleteByTitleAndUser(title, user);
        });

        // 4) “Continue” brings you back home
        b.continueButton.setOnClickListener(v -> {
            Intent i = new Intent(this, HomeActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
            finish();
        });
    }
}
