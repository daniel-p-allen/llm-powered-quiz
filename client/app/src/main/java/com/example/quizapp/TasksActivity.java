package com.example.quizapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.quizapp.adapter.TaskAdapter;
import com.example.quizapp.data.AppDatabase;
import com.example.quizapp.data.TaskDao;
import com.example.quizapp.databinding.ActivityTasksBinding;
import com.example.quizapp.model.Task;
import com.example.quizapp.model.TaskEntity;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TasksActivity extends AppCompatActivity implements TaskAdapter.OnStartListener {
    private ActivityTasksBinding b;
    private TaskAdapter         adapter;
    private TaskDao             taskDao;
    private final ExecutorService dbExec = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityTasksBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        setSupportActionBar(b.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Pending Tasks");

        adapter = new TaskAdapter(this, this);
        b.taskList.setLayoutManager(new LinearLayoutManager(this));
        b.taskList.setAdapter(adapter);

        taskDao = AppDatabase.getInstance(this).taskDao();
        loadTasks();
    }

    private void loadTasks() {
        dbExec.execute(() -> {
            String user = getSharedPreferences("quiz_prefs", MODE_PRIVATE)
                    .getString("username", "");
            List<TaskEntity> entities = taskDao.getAllTasksForUser(user);
            runOnUiThread(() -> {
                adapter.clear();
                for (TaskEntity e : entities) {
                    Task t = new Task();
                    t.title       = e.title;
                    t.description = e.description;
                    adapter.addTask(t);
                }
            });
        });
    }

    @Override
    public void onStart(Task task) {
        dbExec.execute(() -> {
            String user = getSharedPreferences("quiz_prefs", MODE_PRIVATE)
                    .getString("username", "");
            TaskEntity e = taskDao.findByTitleAndUser(task.title, user);
            if (e == null) return;
            runOnUiThread(() -> {
                Intent i = new Intent(this, QuizActivity.class);
                i.putExtra("task",  e.questionJson);
                i.putExtra("title", e.title);
                startActivity(i);
            });
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
