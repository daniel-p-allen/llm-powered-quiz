package com.example.quizapp.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.quizapp.model.TaskEntity;
import java.util.List;

@Dao
public interface TaskDao {
    /** Count tasks for a specific user */
    @Query("SELECT COUNT(*) FROM tasks WHERE username = :user")
    int countAllTasksForUser(String user);

    /** Load every task for a specific user, newest first */
    @Query("SELECT * FROM tasks WHERE username = :user ORDER BY id DESC")
    List<TaskEntity> getAllTasksForUser(String user);

    /** Insert a new task and return its row ID */
    @Insert
    long insert(TaskEntity task);

    /** Find a task by title and user */
    @Query("SELECT * FROM tasks WHERE title = :title AND username = :user LIMIT 1")
    TaskEntity findByTitleAndUser(String title, String user);

    /** Delete a completed task for a specific user */
    @Query("DELETE FROM tasks WHERE title = :title AND username = :user")
    void deleteByTitleAndUser(String title, String user);
}