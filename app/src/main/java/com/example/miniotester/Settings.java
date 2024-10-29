package com.example.miniotester;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.InputStream;
import java.net.URL;

public class Settings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


    }


    public void addBucketSettings(View view) {
    }

    public void removeBucketSettings(View view) {
    }

    public void updateKeys(View view)
    {
        EditText  accessKey = (EditText) findViewById(R.id.accesskeyField);
        EditText secretKey = findViewById(R.id.secretskeyField);

        String accesskey = accessKey.getText().toString();
        String secretkey = secretKey.getText().toString();

        String Url = "https://" + accesskey + ":" + secretkey + "@z4-n1.dv.exaba.io:9000" + "/";


        Intent swap = new Intent(Settings.this, QrActivity.class);
        swap.putExtra("drawableURL", Url);
        startActivity(swap);
    }

}