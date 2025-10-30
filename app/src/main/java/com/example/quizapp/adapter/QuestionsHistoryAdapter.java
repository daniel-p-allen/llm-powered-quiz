package com.example.quizapp.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizapp.R;
import com.example.quizapp.databinding.ItemQuestionBinding;
import com.example.quizapp.model.QuizItem;

import java.util.ArrayList;
import java.util.List;

public class QuestionsHistoryAdapter
        extends RecyclerView.Adapter<QuestionsHistoryAdapter.VH> {

    private final List<QuizItem> items = new ArrayList<>();

    public QuestionsHistoryAdapter(List<QuizItem> data) {
        this.items.addAll(data);
    }

    /** replace with a new list and refresh */
    public void updateData(List<QuizItem> data) {
        items.clear();
        items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemQuestionBinding b = ItemQuestionBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int pos) {
        QuizItem item = items.get(pos);
        holder.b.questionText.setText((pos + 1) + ". " + item.question);

        // Disable the whole group
        holder.b.optionsGroup.setEnabled(false);

        // Set each option’s text
        holder.b.opt0.setText(item.options.get(0));
        holder.b.opt1.setText(item.options.get(1));
        holder.b.opt2.setText(item.options.get(2));
        holder.b.opt3.setText(item.options.get(3));

        // Clear any previous selection
        holder.b.optionsGroup.clearCheck();

        // Highlight correct answer in green
        int correct = item.correct_index;
        RadioButton correctRb = getRadioButton(holder.b.optionsGroup, correct);
        correctRb.setTextColor(ContextCompat.getColor(
                holder.itemView.getContext(), R.color.correct_green));

        // If user was wrong, highlight their choice in red
        int user = item.userAnswer;
        if (user >= 0 && user != correct) {
            RadioButton userRb = getRadioButton(holder.b.optionsGroup, user);
            userRb.setTextColor(ContextCompat.getColor(
                    holder.itemView.getContext(), R.color.incorrect_red));
        }

        // Optionally show the selected dot
        if (user >= 0) {
            holder.b.optionsGroup.check(getRadioButton(holder.b.optionsGroup, user).getId());
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /** Helper: map an index 0–3 to the corresponding RadioButton in the group */
    private RadioButton getRadioButton(RadioGroup group, int index) {
        switch (index) {
            case 0: return (RadioButton) group.findViewById(group.getChildAt(0).getId());
            case 1: return (RadioButton) group.findViewById(group.getChildAt(1).getId());
            case 2: return (RadioButton) group.findViewById(group.getChildAt(2).getId());
            case 3: return (RadioButton) group.findViewById(group.getChildAt(3).getId());
            default: throw new IllegalArgumentException("Invalid option index");
        }
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemQuestionBinding b;
        VH(ItemQuestionBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }
    }
}
