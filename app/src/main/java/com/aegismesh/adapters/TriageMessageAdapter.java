package com.aegismesh.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aegismesh.models.TriageMessage;

import java.util.List;

<<<<<<< HEAD
=======
<<<<<<< HEAD
/**
 * Renders the stream of AI-generated first-aid instructions.
 */
=======
>>>>>>> origin/main
>>>>>>> origin/main
public class TriageMessageAdapter extends RecyclerView.Adapter<TriageMessageAdapter.ViewHolder> {

    private final List<TriageMessage> messages;

    public TriageMessageAdapter(List<TriageMessage> messages) {
        this.messages = messages;
    }

    public void append(TriageMessage message) {
<<<<<<< HEAD
=======
<<<<<<< HEAD
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
=======
>>>>>>> origin/main
        if (message != null) {
            messages.add(message);
            notifyItemInserted(messages.size() - 1);
        }
<<<<<<< HEAD
=======
>>>>>>> origin/main
>>>>>>> origin/main
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
<<<<<<< HEAD
=======
<<<<<<< HEAD
        // Use simple layout for now
=======
>>>>>>> origin/main
>>>>>>> origin/main
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TriageMessage msg = messages.get(position);
<<<<<<< HEAD
        holder.textView.setText(msg.message);
=======
<<<<<<< HEAD
        holder.text.setText(msg.text);
=======
        holder.textView.setText(msg.message);
>>>>>>> origin/main
>>>>>>> origin/main
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

<<<<<<< HEAD
=======
<<<<<<< HEAD
    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView text;

        ViewHolder(View itemView) {
            super(itemView);
            text = itemView.findViewById(android.R.id.text1);
=======
>>>>>>> origin/main
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.textView = itemView.findViewById(android.R.id.text1);
<<<<<<< HEAD
=======
>>>>>>> origin/main
>>>>>>> origin/main
        }
    }
}
