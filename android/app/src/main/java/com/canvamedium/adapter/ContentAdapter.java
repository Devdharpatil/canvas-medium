package com.canvamedium.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.canvamedium.R;
import com.canvamedium.model.Content;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ContentAdapter extends RecyclerView.Adapter<ContentAdapter.ContentViewHolder> {
    
    private List<Content> contentList;
    private final OnContentClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    
    public interface OnContentClickListener {
        void onContentClick(Content content);
    }
    
    public ContentAdapter(List<Content> contentList, OnContentClickListener listener) {
        this.contentList = contentList;
        this.listener = listener;
    }
    
    public void updateData(List<Content> newContentList) {
        this.contentList = newContentList;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public ContentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_content, parent, false);
        return new ContentViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ContentViewHolder holder, int position) {
        Content content = contentList.get(position);
        holder.titleTextView.setText(content.getTitle());
        holder.descriptionTextView.setText(content.getDescription());
        
        if (content.getCreatedAt() != null) {
            holder.dateTextView.setText(dateFormat.format(content.getCreatedAt()));
        }
        
        holder.itemView.setOnClickListener(v -> listener.onContentClick(content));
    }
    
    @Override
    public int getItemCount() {
        return contentList != null ? contentList.size() : 0;
    }
    
    static class ContentViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView descriptionTextView;
        TextView dateTextView;
        
        public ContentViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.textTitle);
            descriptionTextView = itemView.findViewById(R.id.textDescription);
            dateTextView = itemView.findViewById(R.id.textDate);
        }
    }
} 