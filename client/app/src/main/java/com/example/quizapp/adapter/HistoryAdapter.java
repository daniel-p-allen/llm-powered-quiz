package com.example.quizapp.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizapp.databinding.ItemHistoryBinding;
import com.example.quizapp.model.QuizSession;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryVH> {
    private List<QuizSession> sessions;

    public HistoryAdapter(List<QuizSession> sessions) {
        this.sessions = sessions;
    }

    public void updateData(List<QuizSession> newSessions) {
        this.sessions = newSessions;
        notifyDataSetChanged();
    }

    @NonNull @Override
    public HistoryVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHistoryBinding b = ItemHistoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new HistoryVH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryVH holder, int position) {
        QuizSession s = sessions.get(position);
        // Format timestamp
        String time = android.text.format.DateFormat.format(
                "dd MMM yyyy, HH:mm", s.timestamp).toString();
        holder.binding.tvTimestamp.setText(time);

        String counts = s.totalQuestions + " Q / " +
                s.correctCount + "✔️ / " + s.incorrectCount + "❌";
        holder.binding.tvCounts.setText(counts);
    }

    @Override public int getItemCount() {
        return sessions == null ? 0 : sessions.size();
    }

    static class HistoryVH extends RecyclerView.ViewHolder {
        final ItemHistoryBinding binding;
        HistoryVH(ItemHistoryBinding b) {
            super(b.getRoot());
            binding = b;
        }
    }
}
