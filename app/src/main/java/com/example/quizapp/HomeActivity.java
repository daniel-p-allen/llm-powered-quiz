package com.example.quizapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.example.quizapp.UserManager;
import com.example.quizapp.data.AppDatabase;
import com.example.quizapp.data.TaskDao;
import com.example.quizapp.databinding.ActivityHomeBinding;
import com.example.quizapp.model.QuizItem;
import com.example.quizapp.model.QuizResponse;
import com.example.quizapp.model.TaskEntity;
import com.example.quizapp.network.ApiClient;
import com.example.quizapp.network.QuizService;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import android.app.AlertDialog;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.content.Intent;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {
    private ActivityHomeBinding  b;
    private UserManager um;
    private AppDatabase db;
    private TaskDao taskDao;
    private QuizService quizService;
    private SharedPreferences prefs;
    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();
    private final Gson gson   = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityHomeBinding.inflate(getLayoutInflater());
        um = new UserManager(this);

        prefs = getSharedPreferences("quiz_prefs", MODE_PRIVATE);

        String user  = um.getCurrent();
        String email = um.getCurrentEmail();  // returns null or the saved email
        String plan = um.getCurrentPlan();

        b.tvGreeting.setText("Hello, " + user);
        b.tvEmail.setText("Email: " + (email != null ? email : "username@example.com"));
        b.tvPlan.setText("Current Plan: " + plan);

        setContentView(b.getRoot());
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setOnClickListener(v -> {
            // Launch your UpgradeActivity
            startActivity(new Intent(this, UpgradeActivity.class));
        });

        // 2) Init DB, DAO, network
        db          = AppDatabase.getInstance(this);
        taskDao     = db.taskDao();
        quizService = ApiClient.getService();

        // 3) Card click listeners
        b.cardNotification.setOnClickListener(v ->
                startActivity(new Intent(this, TasksActivity.class))
        );

        b.cardTotal.setOnClickListener(v -> {
            Intent i = new Intent(this, HistoryActivity.class);
            i.putExtra(HistoryActivity.EXTRA_LIST_TYPE, HistoryActivity.TYPE_ALL);
            startActivity(i);
        });

        b.cardCorrect.setOnClickListener(v -> {
            Intent i = new Intent(this, HistoryActivity.class);
            i.putExtra(HistoryActivity.EXTRA_LIST_TYPE, HistoryActivity.TYPE_CORRECT);
            startActivity(i);
        });

        b.cardIncorrect.setOnClickListener(v -> {
            Intent i = new Intent(this, HistoryActivity.class);
            i.putExtra(HistoryActivity.EXTRA_LIST_TYPE, HistoryActivity.TYPE_INCORRECT);
            startActivity(i);
        });

        b.cardShare.setOnClickListener(v -> {
            dbExecutor.execute(() -> {
                //String user    = um.getCurrent();
                int total      = db.quizSessionDao().countAllForUser(user);
                int correct    = db.quizSessionDao().sumCorrectForUser(user);
                int incorrect  = total - correct;
                String summary = String.format(
                        "I’ve answered %d/%d correctly (%d incorrect).",
                        correct, total, incorrect
                );
                runOnUiThread(() -> showShareDialog(summary));
            });
        });

        // 4) Initial stats & fetch
        updateStats();
        fetchTasksFromServer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStats();
        String plan = um.getCurrentPlan();
        b.tvPlan.setText("Current Plan: " + plan);
    }

    private void fetchTasksFromServer() {
        dbExecutor.execute(() -> {
            String user = um.getCurrent();
            int pending = taskDao.countAllTasksForUser(user);
            if (pending > 0) {
                updateStats();
                return;
            }

            Set<String> topics = prefs.getStringSet("selected_topics", null);
            if (topics == null || topics.isEmpty()) {
                updateStats();
                return;
            }

            for (String topic : topics) {
                Log.d("HomeActivity", "Fetching quiz for topic: " + topic);
                quizService.getQuiz(topic).enqueue(new Callback<QuizResponse>() {
                    @Override
                    public void onResponse(Call<QuizResponse> call,
                                           Response<QuizResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<QuizItem> items = response.body().quiz;
                            String json = gson.toJson(items);

                            dbExecutor.execute(() -> {
                                TaskEntity t = new TaskEntity();
                                t.username     = user;
                                t.title        = topic;
                                t.description  = "Quiz on \"" + topic + "\"";
                                t.questionJson = json;
                                taskDao.insert(t);
                                updateStats();
                            });
                        }
                    }

                    @Override
                    public void onFailure(Call<QuizResponse> call, Throwable t) {
                        Log.e("HomeActivity", "Failed to fetch quiz for " + topic, t);
                    }
                });
            }
        });
    }

    private void updateStats() {
        dbExecutor.execute(() -> {
            String user = um.getCurrent();
            int taskCount = taskDao.countAllTasksForUser(user);

            // Scope history stats per user
            int total     = db.quizSessionDao().countAllForUser(user);
            int correct   = db.quizSessionDao().sumCorrectForUser(user);
            int incorrect = total - correct;

            runOnUiThread(() -> {
                b.tvNotification .setText("You have " + taskCount + " tasks waiting!");
                b.tvTotalCount    .setText(String.valueOf(total));
                b.tvCorrectCount  .setText(String.valueOf(correct));
                b.tvIncorrectCount.setText(String.valueOf(incorrect));
            });
        });
    }
    private void showShareDialog(String summary) {
        BottomSheetDialog sheet = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.share_bottom_sheet, null);
        sheet.setContentView(sheetView);

        LinearLayout fbOption = sheetView.findViewById(R.id.optionFacebook);
        LinearLayout igOption = sheetView.findViewById(R.id.optionInstagram);

        fbOption.setOnClickListener(v -> {
            String msg = "Facebook\n" + summary + "\nSuccessfully shared to your Facebook profile.";
            Toast toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
            toast.show();
            Handler handler = new Handler(Looper.getMainLooper());

            handler.postDelayed(toast::show, 9000);
            sheet.dismiss();
        });

        igOption.setOnClickListener(v -> {
            String msg = "Instagram\n" + summary + "\nSuccessfully shared to your Instagram profile.";
            Toast toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
            toast.show();
            Handler handler = new Handler(Looper.getMainLooper());

            handler.postDelayed(toast::show, 9000);
            sheet.dismiss();
        });

        sheet.show();
    }
//    private void shareStats() {
//        dbExecutor.execute(() -> {
//            String user      = um.getCurrent();
//            int total        = db.quizSessionDao().countAllForUser(user);
//            int correct      = db.quizSessionDao().sumCorrectForUser(user);
//            int incorrect    = total - correct;
//            String summary   = String.format(
//                    "I’ve answered %d/%d correctly (%d incorrect).",
//                    correct, total, incorrect
//            );
//
//            runOnUiThread(() -> showShareDialog(summary));
//        });
//    }

}
