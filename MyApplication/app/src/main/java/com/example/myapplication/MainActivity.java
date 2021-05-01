package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private ImageView img_logo;
    private TextView txt_logo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Utils.blackIconStatusBar(MainActivity.this,R.color.light_background);
        img_logo = findViewById(R.id.img_logo);
        txt_logo = findViewById(R.id.txt_logo);
        ConnectivityManager manager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activityNetwork = manager.getActiveNetworkInfo();
        if(null != activityNetwork){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(MainActivity.this,HomeActivity.class);
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this,
                            Pair.create(img_logo,"logo"),
                            Pair.create(txt_logo,"logo_text"));
                    startActivity(intent,options.toBundle());
                }
            },3000);
            Toast.makeText(this,"Data Enabled",Toast.LENGTH_SHORT).show();
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(MainActivity.this,NoInternetActivity.class);
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this,
                            Pair.create(img_logo,"logo"),
                            Pair.create(txt_logo,"logo_text"));
                    startActivity(intent,options.toBundle());
                }
            },3000);
            Toast.makeText(this,"No Internet Enabled",Toast.LENGTH_SHORT).show();
        }
    }
}