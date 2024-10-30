package com.example.miniotester;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.InputStream;
import java.net.URL;

public class QrActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_qr);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Intent intent =  getIntent();
        String Url = intent.getStringExtra("drawableURL");
        Drawable d = LoadImageFromWebOperations(Url);
        ImageView iv = findViewById(R.id.qrimage);
        iv.setImageDrawable(d);

    }


    public Drawable LoadImageFromWebOperations(String url) {
        try {
            // Allow network operation on the main thread
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            // Get InputStream from the URL
            InputStream is = (InputStream) new URL(url).getContent();

            // Convert InputStream to Drawable
            Drawable d = Drawable.createFromStream(is, "src name");

            // Log success
            Log.d("detail", "no exception");
            return d;

        } catch (Exception e) {
            // Log any exception that occurs
            Log.d("detail", " exception");
            return null;
        }
    }

}