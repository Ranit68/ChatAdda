package com.example.e_commerce.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.e_commerce.Adapter.ImagePagerAdapter;
import com.example.e_commerce.R;

import java.util.ArrayList;
import java.util.List;

public class FullScreenImageActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private List<String> imageUrls;
    private int startPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        viewPager = findViewById(R.id.viewPager1);
        ImageView close = findViewById(R.id.closeBtn);

        close.setOnClickListener(v -> onBackPressed());

        Intent intent = getIntent();
        imageUrls = intent.getStringArrayListExtra("imageUrls");
        startPosition = intent.getIntExtra("position", 0);

        Log.d("FullScreenImageActivity", "Received imageUrls: " + imageUrls);
        Log.d("FullScreenImageActivity", "Start position: " + startPosition);

        if (imageUrls != null && !imageUrls.isEmpty()) {
            ImagePagerAdapter adapter = new ImagePagerAdapter(this, imageUrls);
            viewPager.setAdapter(adapter);
            viewPager.post(() -> {
                Log.d("FullScreenImageActivity", "Setting ViewPager to position: " + startPosition);
                viewPager.setCurrentItem(startPosition, false);
            });
        } else {
            Log.e("FullScreenImageActivity", "imageUrls is null or empty!");
        }
    }
}