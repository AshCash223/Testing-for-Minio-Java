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
import android.widget.RadioButton;
import android.widget.Toast;

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
        EditText accessKey = (EditText) findViewById(R.id.accesskeyField);
        EditText secretKey = findViewById(R.id.secretskeyField);
        EditText endPoint = findViewById(R.id.endpointField);
        RadioButton rb1 = findViewById(R.id.b1);
        RadioButton rb2 = findViewById(R.id.b2);

        String accesskey = accessKey.getText().toString();
        String secretkey = secretKey.getText().toString();
        String endpoint = endPoint.getText().toString();
        String radioText = "";

        boolean isRb1Selected = rb1.isChecked();
        boolean isRb2Selected = rb2.isChecked();

        if(isRb1Selected){
            radioText = rb1.getText().toString();
        } else if (isRb2Selected) {
            radioText = rb2.getText().toString();
        }

        String Url = "https://" + accesskey + ":" + secretkey + endpoint + "/";

        if (endpoint.contains("exaba")){
            Intent swap = new Intent(Settings.this, MainActivity.class);
            swap.putExtra("drawableURL", Url);
            swap.putExtra("radioText", radioText);
            startActivity(swap);
        } else {
            Toast.makeText(Settings.this, "If you are not using an exaba end point, we suggest a different app.", Toast.LENGTH_SHORT).show();
        }
    }

}