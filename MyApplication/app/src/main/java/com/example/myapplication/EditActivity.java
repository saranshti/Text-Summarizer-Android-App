package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;

import soup.neumorphism.NeumorphFloatingActionButton;

public class EditActivity extends AppCompatActivity {

    EditText edit_area,search_text;
    ImageView btn_erase;
    NeumorphFloatingActionButton btn_big,btn_small,btn_save,btn_back;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        Utils.blackIconStatusBar(this,R.color.white);

        edit_area = findViewById(R.id.edit_area);
        search_text = findViewById(R.id.search_text);
        btn_erase = findViewById(R.id.btn_erase);
        btn_small = findViewById(R.id.btn_small);
        btn_big = findViewById(R.id.btn_big);
        btn_save = findViewById(R.id.btn_save);
        btn_back = findViewById(R.id.btn_back);

        String sum_text = getIntent().getStringExtra("Summary_Text");
        edit_area.setText(sum_text);

        btn_big.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float px = edit_area.getTextSize();
                float sp = px/getResources().getDisplayMetrics().scaledDensity;
                if(sp <= 60.0){
                    float nsp = sp+1;
                    edit_area.setTextSize(nsp);
                }else{
                    Toast.makeText(EditActivity.this,"You Reached the Maximum Text size.",Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_small.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float px = edit_area.getTextSize();
                float sp = px/getResources().getDisplayMetrics().scaledDensity;
                if(sp >= 5.0){
                    float nsp = sp-1;
                    edit_area.setTextSize(nsp);
                }else{
                    Toast.makeText(EditActivity.this,"You Reached the Minimum Text size.",Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String new_sum_text = edit_area.getText().toString().trim();
                Intent intent = new Intent(EditActivity.this,HomeActivity.class);
                intent.putExtra("New_Summary_Text",new_sum_text);
                startActivity(intent);
                Animatoo.animateZoom(EditActivity.this);
            }
        });

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String new_sum_text = edit_area.getText().toString().trim();
                Intent intent = new Intent(EditActivity.this,HomeActivity.class);
                intent.putExtra("New_Summary_Text",new_sum_text);
                startActivity(intent);
                Animatoo.animateZoom(EditActivity.this);
            }
        });

        btn_erase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search_text.setText("");
            }
        });

        search_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String st = search_text.getText().toString().trim();
                if(TextUtils.isEmpty(st)){
                    btn_erase.setVisibility(View.INVISIBLE);
                }else{
                    btn_erase.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
                String textToHighlight = search_text.getText().toString().trim();
                String replaceWith = "<span style='background-color:yellow'>"+textToHighlight+"</span>";
                String originalText = edit_area.getText().toString();
                String modifiedText = originalText.replaceAll(textToHighlight,replaceWith);
                edit_area.setText(Html.fromHtml(modifiedText));
            }
        });
    }

    @Override
    public void onBackPressed() {
        String new_sum_text = edit_area.getText().toString().trim();
        Intent intent = new Intent(EditActivity.this, HomeActivity.class);
        intent.putExtra("New_Summary_Text",new_sum_text);
        startActivity(intent);
        Animatoo.animateZoom(EditActivity.this);
    }
}