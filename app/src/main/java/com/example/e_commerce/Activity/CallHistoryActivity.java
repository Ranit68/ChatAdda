package com.example.e_commerce.Activity;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.e_commerce.Adapter.CallHistoryAdapter;
import com.example.e_commerce.Model.CallHistory;
import com.example.e_commerce.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CallHistoryActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private CallHistoryAdapter adapter;
    private List<CallHistory> callHistoryList;
    private DatabaseReference callHistoryRef;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_history);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        callHistoryList = new ArrayList<>();
        adapter = new CallHistoryAdapter(this, callHistoryList);
        recyclerView.setAdapter(adapter);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            callHistoryRef = FirebaseDatabase.getInstance().getReference("CallHistory").child(currentUser.getUid());
            loadCallHistory();
        }
    }

    private void loadCallHistory() {
        callHistoryRef.orderByChild("timestamp").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                callHistoryList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    CallHistory call = data.getValue(CallHistory.class);
                    if (call != null) {
                        callHistoryList.add(call);
                    }
                }
                // Sort by latest call first
                Collections.reverse(callHistoryList);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CallHistoryActivity.this, "Failed to load call history", Toast.LENGTH_SHORT).show();
            }
        });
    }
}