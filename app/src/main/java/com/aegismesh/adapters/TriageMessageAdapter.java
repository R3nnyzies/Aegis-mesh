package com.aegismesh.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aegismesh.models.TriageMessage;

import java.util.List;

/**
 * Renders the stream of AI-generated first-aid instructions.
 */
public class TriageMessageAdapter extends RecyclerView.Adapter<TriageMessageAdapter.ViewHolder> {

    private final List<TriageMessage> messages;

    public TriageMessageAdapter(List<TriageMessage> messages) {
        this.messages = messages;
    }

    public void append(TriageMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Use simple layout for now
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TriageMessage msg = messages.get(position);
        holder.text.setText(msg.text);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView text;

        ViewHolder(View itemView) {
            super(itemView);
            text = itemView.findViewById(android.R.id.text1);
        }
    }
}
