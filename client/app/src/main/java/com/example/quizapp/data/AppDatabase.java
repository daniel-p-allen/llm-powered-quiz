package com.example.quizapp.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.example.quizapp.model.QuizSession;
import com.example.quizapp.model.TaskEntity;

@Database(
        entities = { QuizSession.class, TaskEntity.class },
        version  = 6,           // bumped version
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    public abstract QuizSessionDao quizSessionDao();
    public abstract TaskDao        taskDao();
    //public abstract UserDao         userDao();
    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context ctx) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    ctx.getApplicationContext(),
                                    AppDatabase.class,
                                    "quiz_app.db"
                            )
                            .fallbackToDestructiveMigration()  // destructive on version bump
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
