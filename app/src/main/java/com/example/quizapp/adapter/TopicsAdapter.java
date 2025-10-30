package com.example.quizapp.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quizapp.databinding.ItemTopicBinding;
import java.util.ArrayList;
import java.util.List;

public class TopicsAdapter
        extends RecyclerView.Adapter<TopicsAdapter.VH> {

    public interface SelectionListener {
        void onSelectionChanged(List<String> current);
    }

    private final List<String> items;
    private final List<String> selected = new ArrayList<>();
    private final SelectionListener listener;

    public TopicsAdapter(List<String> items, SelectionListener l) {
        this.items = items;
        this.listener = l;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int v) {
        ItemTopicBinding b = ItemTopicBinding.inflate(
                LayoutInflater.from(p.getContext()), p, false);
        return new VH(b);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int i) {
        String topic = items.get(i);
        h.binding.topicToggle.setTextOff(topic);
        h.binding.topicToggle.setTextOn(topic);
        h.binding.topicToggle.setChecked(selected.contains(topic));
        h.binding.topicToggle.setOnCheckedChangeListener((btn, on) -> {
            if (on) selected.add(topic);
            else selected.remove(topic);
            listener.onSelectionChanged(new ArrayList<>(selected));
        });
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final ItemTopicBinding binding;
        VH(ItemTopicBinding b) {
            super(b.getRoot());
            binding = b;
        }
    }
}
