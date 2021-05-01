package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.Toast;

import soup.neumorphism.NeumorphButton;

public class NoInternetActivity extends AppCompatActivity {

    NeumorphButton btn_close_app;
    RelativeLayout relative1;
    private Animation animation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_internet);
        Utils.blackIconStatusBar(NoInternetActivity.this,R.color.white);
        btn_close_app = findViewById(R.id.btn_close_app);
        relative1 = findViewById(R.id.relative1);
        animation = AnimationUtils.loadAnimation(NoInternetActivity.this,R.anim.fade_in);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                relative1.setVisibility(View.VISIBLE);
                relative1.setAnimation(animation);
            }
        },500);

        btn_close_app.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAffinity();
            }
        });
    }

    private long backPressedTime;
    private Toast backToast;
    @Override
    public void onBackPressed() {
        if(backPressedTime + 2000 > System.currentTimeMillis()){
            backToast.cancel();
            finishAffinity();
            return;
        }else {
            backToast = Toast.makeText(getBaseContext(),"Press back again to exit",Toast.LENGTH_SHORT);
            backToast.show();
        }
        backPressedTime = System.currentTimeMillis();
    }

}