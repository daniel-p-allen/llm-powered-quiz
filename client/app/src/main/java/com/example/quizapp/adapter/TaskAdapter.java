package com.example.quizapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quizapp.databinding.ItemTaskBinding;
import com.example.quizapp.model.Task;
import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.VH> {

    public interface OnStartListener {
        void onStart(Task task);
    }

    private final List<Task> tasks = new ArrayList<>();
    private final OnStartListener listener;

    public TaskAdapter(Context ctx, OnStartListener l) {
        this.listener = l;
    }

    /**
     * Add a task and notify the adapter.
     */
    public void addTask(Task t) {
        tasks.add(t);
        notifyItemInserted(tasks.size() - 1);
    }

    /**
     * Clear all tasks and refresh the list.
     */
    public void clear() {
        tasks.clear();
        notifyDataSetChanged();
    }
    public void removeByTitle(String title) {
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).title.equals(title)) {
                tasks.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int v) {
        ItemTaskBinding b = ItemTaskBinding.inflate(
                LayoutInflater.from(p.getContext()), p, false);
        return new VH(b);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int i) {
        Task t = tasks.get(i);
        h.binding.title.setText(t.title);
        h.binding.description.setText(t.description);
        h.binding.startButton.setOnClickListener(v -> listener.onStart(t));
    }

    @Override public int getItemCount() {
        return tasks.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemTaskBinding binding;
        VH(ItemTaskBinding b) {
            super(b.getRoot());
            binding = b;
        }
    }
}
