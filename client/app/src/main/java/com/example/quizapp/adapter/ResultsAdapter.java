package com.example.quizapp.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quizapp.databinding.ItemResultBinding;
import com.example.quizapp.model.QuizItem;
import java.util.List;

public class ResultsAdapter
        extends RecyclerView.Adapter<ResultsAdapter.VH> {

    private final List<QuizItem> data;

    public ResultsAdapter(List<QuizItem> list) {
        this.data = list;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int v) {
        ItemResultBinding b = ItemResultBinding.inflate(
                LayoutInflater.from(p.getContext()), p, false);
        return new VH(b);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int i) {
        QuizItem item = data.get(i);
        h.b.questionText.setText(item.question);
        String ans = "Your answer: " +
                (item.userAnswer>=0 ? item.options.get(item.userAnswer) : "None");
        String correct = "Correct answer: " +
                item.options.get(item.correct_index);
        h.b.correctAnswer.setText(ans + "\n" + correct);
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final ItemResultBinding b;
        VH(ItemResultBinding bind) {
            super(bind.getRoot());
            b = bind;
        }
    }
}
