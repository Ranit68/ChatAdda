package com.example.e_commerce.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.e_commerce.Activity.FullScreenImageActivity;
import com.example.e_commerce.Model.Chat;
import com.example.e_commerce.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    private Context context;
    private List<Chat> chatList;
    private String currentUserId;
    private List<String> imageUrls = new ArrayList<>();

    public ChatAdapter(Context context, List<Chat> chatList, String currentUserId) {
        this.context = context;
        this.chatList = chatList;
        this.currentUserId = currentUserId;

        for (Chat chat : chatList){
            if ("image".equals(chat.getMessageType())){
                imageUrls.add(chat.getMediaUrl());
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 1) {
            view = LayoutInflater.from(context).inflate(R.layout.chat_item_right, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.chat_item_left, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Chat chat = chatList.get(position);

        String formattedTime = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(chat.getTimestamp());
        holder.timestampText.setText(formattedTime);
        if (chat.getSenderId().equals(currentUserId)){
            if (chat.isSeen()){
                holder.statusIcon.setImageResource(R.drawable.mark2);
            }else if (chat.isRecived()){
                holder.statusIcon.setImageResource(R.drawable.mark);
            }else {
                holder.statusIcon.setImageResource(R.drawable.mark1);
            }
        }

        if ("call".equals(chat.getMessageType())){
            holder.messageText.setText("Call ended");
            holder.messageText.setTextColor(Color.BLUE);
        }

        if ("image".equals(chat.getMessageType())) {
            holder.messageText.setVisibility(View.GONE);
            holder.chatImage.setVisibility(View.VISIBLE);
            holder.progressBar.setVisibility(View.VISIBLE);


                Log.d("Glide","Loading image: " + chat.getMessage());

            Glide.with(context)
                    .load(chat.getMediaUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.avtar)
                    .into(holder.chatImage);


            holder.progressBar.setVisibility(View.GONE);

            holder.chatImage.setOnClickListener(v -> {
                Intent intent = new Intent(context, FullScreenImageActivity.class);
                intent.putStringArrayListExtra("imageUrls", getAllImageUrls());

                // 🔥 Fix: Get the correct position of the clicked image
                int Imgposition = getImagePosition(chat.getMediaUrl());
                Log.d("ChatAdapter", "Clicked Image URL: " + chat.getMediaUrl());
                Log.d("ChatAdapter", "Image Position: " + Imgposition);

                intent.putExtra("position", Imgposition);
                context.startActivity(intent);
            });
        } else {
            holder.chatImage.setVisibility(View.GONE);
            holder.progressBar.setVisibility(View.GONE);
            holder.messageText.setVisibility(View.VISIBLE);
            holder.messageText.setText(chat.getMessage());
        }

    }
    private int getImagePosition(String imageUrl) {
        ArrayList<String> urls = getAllImageUrls();
        return urls.indexOf(imageUrl);
    }
    private ArrayList<String> getAllImageUrls() {
        ArrayList<String> urls = new ArrayList<>();
        for (Chat chat : chatList) {
            if ("image".equals(chat.getMessageType())) {
                urls.add(chat.getMediaUrl());
            }
        }
        return urls;
    }

    @Override
    public int getItemViewType(int position) {
        return chatList.get(position).getSenderId().equals(currentUserId) ? 1 : 0;
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        ImageView chatImage;
        ProgressBar progressBar;
        TextView timestampText;
        ImageView statusIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            chatImage = itemView.findViewById(R.id.chatImage);
            timestampText = itemView.findViewById(R.id.timestampText);
            progressBar = itemView.findViewById(R.id.progressbar);
            statusIcon = itemView.findViewById(R.id.statusIcon);
        }
    }
}