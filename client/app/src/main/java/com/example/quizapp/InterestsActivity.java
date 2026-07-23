package com.example.quizapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import com.example.quizapp.adapter.TopicsAdapter;
import com.example.quizapp.databinding.ActivityInterestsBinding;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InterestsActivity extends AppCompatActivity
        implements TopicsAdapter.SelectionListener {
    private ActivityInterestsBinding b;
    private List<String> topics;
    private List<String> selected = new ArrayList<>();

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        b = ActivityInterestsBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        topics = loadTopics();
        TopicsAdapter adapter = new TopicsAdapter(topics, this);
        b.recycler.setLayoutManager(new GridLayoutManager(this, 2));
        b.recycler.setAdapter(adapter);

        b.nextButton.setOnClickListener(v -> {
            // Save as a String Set so HomeActivity.fetchTasksFromServer()
            // can read it with prefs.getStringSet("selected_topics",…)
            SharedPreferences prefs = getSharedPreferences("quiz_prefs", MODE_PRIVATE);
            Set<String> topicSet = new HashSet<>(selected);
            prefs.edit()
                    .putStringSet("selected_topics", topicSet)
                    .apply();

            startActivity(new Intent(this, HomeActivity.class));
        });
    }

    private List<String> loadTopics() {
        try {
            String json = new String(
                    getAssets().open("topics.json").readAllBytes(),
                    java.nio.charset.StandardCharsets.UTF_8
            );
            return new com.google.gson.Gson().fromJson(
                    json, new com.google.gson.reflect.TypeToken<List<String>>(){}.getType()
            );
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public void onSelectionChanged(List<String> current) {
        selected = current;
        b.nextButton.setEnabled(!current.isEmpty());
    }
}
