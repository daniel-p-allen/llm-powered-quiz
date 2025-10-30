package com.example.quizapp.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.quizapp.model.QuizSession;

import java.util.List;

@Dao
public interface QuizSessionDao {
    @Insert
    long insert(QuizSession session);

    // ─── Unscoped methods (keep for backward compatibility) ─────────────────

    /** Total number of questions ever asked (sum of every session’s totalQuestions) */
    @Query("SELECT IFNULL(SUM(totalQuestions),0) FROM quiz_sessions")
    int countAll();

    /** Total number of correctly answered questions (sum of every session’s correctCount) */
    @Query("SELECT IFNULL(SUM(correctCount),0) FROM quiz_sessions")
    int countCorrect();

    /** Total number of incorrect answers (sum of every session’s incorrectCount) */
    @Query("SELECT IFNULL(SUM(incorrectCount),0) FROM quiz_sessions")
    int countIncorrect();

    /** All sessions across **all** users, newest first */
    @Query("SELECT * FROM quiz_sessions ORDER BY timestamp DESC")
    List<QuizSession> getAllSessions();

    /** Latest session across **all** users */
    @Query("SELECT * FROM quiz_sessions ORDER BY timestamp DESC LIMIT 1")
    QuizSession getLatestSession();

    // ─── New per-user methods ────────────────────────────────────────────────

    /** Total questions for a specific user */
    @Query("SELECT IFNULL(SUM(totalQuestions),0) FROM quiz_sessions WHERE username = :user")
    int countAllForUser(String user);

    /** Correct answers for a specific user */
    @Query("SELECT IFNULL(SUM(correctCount),0) FROM quiz_sessions WHERE username = :user")
    int sumCorrectForUser(String user);

    /** Incorrect answers for a specific user */
    @Query("SELECT IFNULL(SUM(incorrectCount),0) FROM quiz_sessions WHERE username = :user")
    int sumIncorrectForUser(String user);

    /** All sessions for a specific user, newest first */
    @Query("SELECT * FROM quiz_sessions WHERE username = :user ORDER BY timestamp DESC")
    List<QuizSession> getAllSessionsForUser(String user);

    /** Latest session for a specific user */
    @Query("SELECT * FROM quiz_sessions WHERE username = :user ORDER BY timestamp DESC LIMIT 1")
    QuizSession getLatestSessionForUser(String user);
}
