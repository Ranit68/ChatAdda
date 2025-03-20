package com.example.e_commerce.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.e_commerce.Activity.ChatActivity;
import com.example.e_commerce.Model.User;
import com.example.e_commerce.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private Context context;
    private List<User> userList;
    private FirebaseUser currentUser;

    public UserAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);

        holder.username.setText(user.getUsername());

        // Load profile image with null check
        if (user.getImgUrl() != null && !user.getImgUrl().isEmpty()) {
            Glide.with(context).load(user.getImgUrl()).placeholder(R.drawable.avtar).into(holder.profileImage);
        } else {
            holder.profileImage.setImageResource(R.drawable.avtar);
        }

        // Display About info instead of last message
        if (user.getAbout() != null && !user.getAbout().isEmpty()) {
            holder.lastMessage.setText(user.getAbout());
        } else {
            holder.lastMessage.setText("No About yet");
        }

        // Handle unseen messages count visibility
        if (user.getUnseenCount() > 0) {
            holder.unseenCount.setText(String.valueOf(user.getUnseenCount()));
            holder.unseenCount.setVisibility(View.VISIBLE);
        } else {
            holder.unseenCount.setVisibility(View.GONE);
        }

        // Click listener to open chat
        holder.itemView.setOnClickListener(v -> {
            if (user.getUserId() == null) {  // Prevent NullPointerException
                Log.e("UserAdapter", "User ID is NULL. Cannot open ChatActivity.");
                return;
            }

            Log.d("UserAdapter", "Opening ChatActivity with userId: " + user.getUserId());

            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("userId", user.getUserId());
            intent.putExtra("userName", user.getUsername()); // Passing username
            intent.putExtra("userImage", user.getImgUrl()); // Passing profile image URL
            context.startActivity(intent);
            Log.d("UserAdapter", "Starting ChatActivity with receiverId: " + user.getUserId());

        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView username, unseenCount, lastMessage;
        ImageView profileImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            profileImage = itemView.findViewById(R.id.profileImage);
            unseenCount = itemView.findViewById(R.id.unseenCount);
            lastMessage = itemView.findViewById(R.id.lastmessage); // Ensure this matches your XML
        }
    }
}
