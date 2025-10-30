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

public class QuizAdapter extends RecyclerView.Adapter<QuizAdapter.VH> {
    private final List<QuizItem> items;
    private boolean isGrading = false;

    public QuizAdapter(List<QuizItem> data) {
        this.items = data;
    }

    /**
     * Call this to switch the adapter into grading mode.
     * Highlights correct/incorrect and disables further changes.
     */
    public void grade() {
        isGrading = true;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemQuestionBinding b = ItemQuestionBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        QuizItem item = items.get(position);
        holder.b.questionText.setText(item.question);

        RadioGroup rg = holder.b.optionsGroup;
        if (!isGrading) {
            // In answer mode, clear previous selection
            rg.clearCheck();
        }

        // Populate options and restore selection
        for (int j = 0; j < 4; j++) {
            RadioButton rb = (RadioButton) rg.getChildAt(j);
            rb.setText(item.options.get(j));
            rb.setBackground(null);
            rb.setEnabled(!isGrading);
            if (!isGrading && item.userAnswer == j) {
                rb.setChecked(true);
            }
        }

        if (!isGrading) {
            // Capture user selections in answer mode
            rg.setOnCheckedChangeListener((group, checkedId) -> {
                int idx = group.indexOfChild(group.findViewById(checkedId));
                item.userAnswer = idx;
            });
        } else {
            // In grading mode, disable listener and highlight correct/incorrect
            rg.setOnCheckedChangeListener(null);
            for (int j = 0; j < 4; j++) {
                RadioButton rb = (RadioButton) rg.getChildAt(j);
                if (j == item.correct_index) {
                    rb.setBackgroundColor(
                            ContextCompat.getColor(rb.getContext(), R.color.correct_green)
                    );
                } else if (j == item.userAnswer) {
                    rb.setBackgroundColor(
                            ContextCompat.getColor(rb.getContext(), R.color.incorrect_red)
                    );
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /** Returns the list of QuizItems with user answer populated. */
    public List<QuizItem> getAnswers() {
        return new ArrayList<>(items);
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemQuestionBinding b;
        VH(ItemQuestionBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }
    }
}