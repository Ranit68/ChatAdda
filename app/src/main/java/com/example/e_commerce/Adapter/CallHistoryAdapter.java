package com.example.e_commerce.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.e_commerce.Activity.CallActivity;
import com.example.e_commerce.Model.CallHistory;
import com.example.e_commerce.R;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CallHistoryAdapter extends RecyclerView.Adapter<CallHistoryAdapter.ViewHolder> {
    private Context context;
    private List<CallHistory> callHistoryList;

    public CallHistoryAdapter(Context context, List<CallHistory> callHistoryList) {
        this.context = context;
        this.callHistoryList = callHistoryList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_call_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CallHistory call = callHistoryList.get(position);

        holder.callType.setText(call.getCallType());
        holder.callTime.setText(formatTimestamp(call.getTimestamp()));
        holder.username.setText(call.getUserName());
        Glide.with(context)
                .load(call.getUserProfilePic())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.avtar)
                .into(holder.profilePic);


        if (call.getCallType().equals("Outgoing")) {
            holder.callIcon.setImageResource(R.drawable.outgoing);
        } else {
            holder.callIcon.setImageResource(R.drawable.incoming);
        }
    }


    @Override
    public int getItemCount() {
        return callHistoryList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView callType, callDuration, callTime,username;
        ImageView profilePic,callIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            callType = itemView.findViewById(R.id.callType);
            profilePic = itemView.findViewById(R.id.profilePicHistory);
            callTime = itemView.findViewById(R.id.callTime);
            username = itemView.findViewById(R.id.username3);
            callIcon = itemView.findViewById(R.id.call_icon);
        }
    }

    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    private String formatDuration(long duration) {
        long minutes = duration / 60;
        long seconds = duration % 60;
        return minutes + " min " + seconds + " sec";
    }
}
