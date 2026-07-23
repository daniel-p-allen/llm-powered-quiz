package com.example.quizapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.quizapp.adapter.QuestionsHistoryAdapter;
import com.example.quizapp.data.AppDatabase;
import com.example.quizapp.data.QuizSessionDao;
import com.example.quizapp.databinding.ActivityHistoryBinding;
import com.example.quizapp.model.QuizItem;
import com.example.quizapp.model.QuizSession;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class HistoryActivity extends AppCompatActivity {
    public static final String EXTRA_LIST_TYPE   = "LIST_TYPE";
    public static final String TYPE_ALL         = "ALL";
    public static final String TYPE_CORRECT     = "CORRECT";
    public static final String TYPE_INCORRECT   = "INCORRECT";

    private ActivityHistoryBinding b;
    private QuestionsHistoryAdapter adapter;
    private SharedPreferences prefs;
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityHistoryBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        // toolbar back arrow
        setSupportActionBar(b.toolbar);
        b.toolbar.setNavigationOnClickListener(v -> finish());

        prefs = getSharedPreferences("quiz_prefs", MODE_PRIVATE);

        // RecyclerView setup
        adapter = new QuestionsHistoryAdapter(new ArrayList<>());
        b.historyList.setLayoutManager(new LinearLayoutManager(this));
        b.historyList.setAdapter(adapter);

        // Determine which list to show
        String type = getIntent().getStringExtra(EXTRA_LIST_TYPE);
        if (type == null) type = TYPE_ALL;
        b.toolbar.setTitle(
                type.equals(TYPE_ALL)       ? "All Questions" :
                        type.equals(TYPE_CORRECT)   ? "Correct Answers" :
                                "Incorrect Answers"
        );

        loadList(type);
    }

    private void loadList(String type) {
        Executors.newSingleThreadExecutor().execute(() -> {
            String user = prefs.getString("username", "");
            QuizSessionDao dao = AppDatabase.getInstance(this).quizSessionDao();
            List<QuizSession> sessions = dao.getAllSessionsForUser(user);

            // flatten all sessions' question lists into one big list
            List<QuizItem> allItems = new ArrayList<>();
            Type listType = new TypeToken<List<QuizItem>>(){}.getType();
            for (QuizSession s : sessions) {
                // assume you stored full quiz JSON in incorrectAnswersJson for incorrect only?
                // ideally you have a field with full quiz JSON per session:
                String fullJson = s.incorrectAnswersJson;
                List<QuizItem> items = gson.fromJson(fullJson, listType);
                allItems.addAll(items);
            }

            // filter according to type
            List<QuizItem> filtered = new ArrayList<>();
            for (QuizItem qi : allItems) {
                boolean correct = (qi.userAnswer == qi.correct_index);
                if (TYPE_ALL.equals(type)
                        || (TYPE_CORRECT.equals(type)   && correct)
                        || (TYPE_INCORRECT.equals(type) && !correct)) {
                    filtered.add(qi);
                }
            }

            runOnUiThread(() -> adapter.updateData(filtered));
        });
    }
}
