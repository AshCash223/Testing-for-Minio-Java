package com.example.miniotester;

import android.graphics.drawable.BitmapDrawable;
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
import java.net.HttpURLConnection;
import java.net.URL;

public class QrActivity extends AppCompatActivity {

    private static final String TAG = "QrActivity";
    private ImageView qrCodeImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_qr);

        qrCodeImageView = findViewById(R.id.qrimage);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get the URL from the intent
        String url = getIntent().getStringExtra("drawableURL");
        if (url != null) {
            generateQRCode(url);
        } else {
            Log.e(TAG, "URL is null");
        }
    }

    private void generateQRCode(String url) {
        new Thread(() -> {
            try {
                // building the QR code URL
                String qrCodeUrl = "https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=" + url;

                // opening connection and getting the image
                HttpURLConnection connection = (HttpURLConnection) new URL(qrCodeUrl).openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Drawable drawable = Drawable.createFromStream(input, "src name");

                // updating the ImageView on the UI thread
                runOnUiThread(() -> {
                    if (drawable != null) {
                        qrCodeImageView.setImageDrawable(drawable);
                    } else {
                        Log.e(TAG, "Failed to create drawable from input stream");
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Error generating QR Code: " + e.getMessage(), e);
            }
        }).start();
    }
}
