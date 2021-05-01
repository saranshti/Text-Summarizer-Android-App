package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfReader;

import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import soup.neumorphism.NeumorphButton;
import soup.neumorphism.NeumorphFloatingActionButton;

public class HomeActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_STORAGE = 1000;
    private static final int READ_REQUEST_CODE = 1001;
    private static final int CAMERA_REQUEST_CODE = 2000;
    private static final int STORAGE_REQUEST_CODE = 2001;
    private static final int IMAGE_PICK_GALLERY_CODE = 2003;
    private static final int IMAGE_PICK_CAMERA_CODE = 2004;

    private static final String URL = "https://flask-text-summary-app.herokuapp.com/sum/";

    Uri image_uri;

    DatabaseReference summaryDB;

    TextToSpeech textToSpeech;

    ProgressDialog progressDoalog;

    InputStream inputStream;
    SeekBar ratio_seek_bar;
    TextView ratio_text;
    EditText original_text,summary_text;
    ImageView btn_paste, btn_clear;
    ClipboardManager clipboardManager;
    NeumorphFloatingActionButton btn_copy, attach_file, gallery_text, camera_text, btn_share, btn_speech, btn_edit, btn_pdf;
    NeumorphButton btn_summary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Utils.blackIconStatusBar(HomeActivity.this,R.color.white);
        String id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        summaryDB = FirebaseDatabase.getInstance().getReference().child("User").child(id);

        clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        ratio_seek_bar = findViewById(R.id.ratio_seek_bar);
        ratio_text = findViewById(R.id.ratio_text);
        original_text = findViewById(R.id.original_text);
        btn_paste = findViewById(R.id.btn_paste);
        btn_clear = findViewById(R.id.btn_clear);
        attach_file = findViewById(R.id.attach_file);
        camera_text = findViewById(R.id.camera_text);
        gallery_text = findViewById(R.id.gallery_text);
        btn_summary = findViewById(R.id.btn_summary);

        summary_text = findViewById(R.id.summary_text);
        btn_copy = findViewById(R.id.btn_copy);
        btn_share = findViewById(R.id.btn_share);
        btn_speech = findViewById(R.id.btn_speech);
        btn_pdf = findViewById(R.id.btn_pdf);
        btn_edit = findViewById(R.id.btn_edit);

        String new_sum_text = getIntent().getStringExtra("New_Summary_Text");
        summary_text.setText(new_sum_text);

        String s = summary_text.getText().toString().trim();
        if(s.equals("")){
            summary_text.setText("Summary Text");
        }

        if(!clipboardManager.hasPrimaryClip()){
            btn_paste.setEnabled(false);
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.INTERNET}, PERMISSION_REQUEST_STORAGE);
        }

        ratio_seek_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ratio_text.setText(progress+"%");
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar){}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar){}
        });

        original_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String ot = original_text.getText().toString().trim();
                if(TextUtils.isEmpty(ot)){
                    btn_paste.setVisibility(View.VISIBLE);
                    btn_clear.setVisibility(View.INVISIBLE);
                }else{
                    btn_paste.setVisibility(View.INVISIBLE);
                    btn_clear.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void afterTextChanged(Editable s){}
        });

        btn_paste.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                ClipData clipData = clipboardManager.getPrimaryClip();
                ClipData.Item item = clipData.getItemAt(0);
                original_text.setText(item.getText().toString());
                Toast.makeText(HomeActivity.this,"Pasted",Toast.LENGTH_SHORT).show();
            }
        });

        btn_clear.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                original_text.setText("");
            }
        });

        btn_copy.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String st = summary_text.getText().toString();
                if(!st.equals("")){
                    ClipData clipData = ClipData.newPlainText("text",st);
                    clipboardManager.setPrimaryClip(clipData);
                    Toast.makeText(HomeActivity.this,"Copied",Toast.LENGTH_SHORT).show();
                }
            }
        });

        attach_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performFileSearch();
            }
        });

        camera_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickCamera();
            }
        });

        gallery_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickGallery();
            }
        });

        btn_summary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String link = original_text.getText().toString().trim();
                if(link.equals("")){
                    Toast.makeText(HomeActivity.this, "Text Is Empty.",Toast.LENGTH_SHORT).show();
                }else if(isValidURL(link) == true){
                    textFromURL(link);
                    AlertDialog.Builder alertDialogBuilder1 = new AlertDialog.Builder(HomeActivity.this);
                    alertDialogBuilder1.setMessage("You Entered a Link it will take some time.");
                    alertDialogBuilder1.setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    showSummary();
                                }
                            });
                    AlertDialog alertDialog = alertDialogBuilder1.create();
                    alertDialog.show();
                }else{
                    showSummary();
                    Toast.makeText(HomeActivity.this, "Data Added",Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT,summary_text.getText().toString());
                startActivity(Intent.createChooser(shareIntent,"Select an App."));
            }
        });

        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i == TextToSpeech.SUCCESS){
                    int lang  = textToSpeech.setLanguage(Locale.ENGLISH);
                }
            }
        });

        btn_speech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = summary_text.getText().toString();
                int speech = textToSpeech.speak(s,TextToSpeech.QUEUE_FLUSH,null);
            }
        });
        btn_pdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePdf();
            }
        });
        btn_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = summary_text.getText().toString();
                Intent intent = new Intent(HomeActivity.this,EditActivity.class);
                intent.putExtra("Summary_Text",s);
                startActivity(intent);
                Animatoo.animateShrink(HomeActivity.this);
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

    private String readText(String input){
        File file = new File(Environment.getExternalStorageDirectory(),input);
        StringBuilder text = new StringBuilder();
        try{
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while((line = br.readLine()) != null){
                text.append(line);
                text.append("\n");
            }
            br.close();
        }catch (IOException e){
            e.printStackTrace();
        }
        return text.toString();
    }

    private void extractTextPDFFile(Uri uri){
        try {
            inputStream = HomeActivity.this.getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                String fileContent="";
                StringBuilder builder = new StringBuilder();
                PdfReader reader = null;
                try {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                        reader = new PdfReader(inputStream);
                        int pages=reader.getNumberOfPages();
                        for(int i=1;i<=pages;i++){
                            fileContent= fileContent+PdfTextExtractor.getTextFromPage(reader,i).trim()+"\n";
                        }
                        builder.append(fileContent);
                    }
                    reader.close();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            original_text.setText(builder.toString());
                        }
                    });
                }catch (IOException e){
                    Toast.makeText(HomeActivity.this, "" + e, Toast.LENGTH_SHORT).show();
                }
            }
        }).start();
    }

    private void performFileSearch(){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        String[] extraMimeType = {"text/*","application/pdf"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES,extraMimeType);
        startActivityForResult(intent,READ_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == IMAGE_PICK_GALLERY_CODE){
                CropImage.activity(data.getData()).setGuidelines(CropImageView.Guidelines.ON).start(this);
            }
            if(requestCode == IMAGE_PICK_CAMERA_CODE){
                CropImage.activity(image_uri).setGuidelines(CropImageView.Guidelines.ON).start(this);
            }
            if(requestCode == READ_REQUEST_CODE){
                if (data != null) {
                    Uri uri = data.getData();
                    String path = uri.getPath();
                    path = path.substring(path.indexOf(":") + 1);
                    if(path.contains("emulated")){
                        path = path.substring(path.indexOf("0") + 1);
                    }else{
                        path = path.trim();
                        String lstChar = path.substring(path.length() - 1);
                        if(lstChar.equals("t")){
                            original_text.setText(readText(path));
                            Toast.makeText(this, "" + path, Toast.LENGTH_SHORT).show();
                        }else if(lstChar.equals("f")){
                            extractTextPDFFile(data.getData());
                            Toast.makeText(this, ""+ path, Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(this, "Wrong File Selected.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        }
        CropImage.ActivityResult result = CropImage.getActivityResult(data);
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            if(resultCode == RESULT_OK){
                Uri resultUri = result.getUri();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(),resultUri);
                    TextRecognizer recognizer = new TextRecognizer.Builder(getApplicationContext()).build();
                    if(!recognizer.isOperational()){
                        Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
                    }else{
                        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                        SparseArray<TextBlock> items = recognizer.detect(frame);
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0;i<items.size();i++){
                            TextBlock myItem = items.valueAt(i);
                            sb.append(myItem.getValue());
                            sb.append("\n");
                        }
                        original_text.setText(sb.toString());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        else if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
            Exception error = result.getError();
            Toast.makeText(this, ""+error, Toast.LENGTH_SHORT).show();
        }
        else {

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
            if(requestCode == CAMERA_REQUEST_CODE){
                pickCamera();
            }
            if(requestCode == STORAGE_REQUEST_CODE){
                pickCamera();
            }
        }else{
            Toast.makeText(this, "Permission denied!",Toast.LENGTH_SHORT).show();
        }
    }

    private void pickCamera(){
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"NewPic");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Image To Text");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(cameraIntent,IMAGE_PICK_CAMERA_CODE);
    }

    private void pickGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);
    }

    private void textFromURL(String url){
        new Thread(new Runnable() {
            @Override
            public void run() {
                final StringBuilder builder = new StringBuilder();
                try{
                    Document doc = Jsoup.connect(url).get();
                    builder.append(doc.text());
                }catch (IOException e){
                    builder.append("Error :").append(e.getMessage()).append("\n");
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        original_text.setText(builder.toString());
                    }
                });
            }
        }).start();
    }

    private boolean isValidURL(String url){
        String regex = "((http|https)://)(www.)?"
                + "[a-zA-Z0-9@:%._\\+~#?&//=]"
                + "{2,256}\\.[a-z]"
                + "{2,6}\\b([-a-zA-Z0-9@:%"
                + "._\\+~#?&//=]*)";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(url);
        return m.matches();
    }

    public void savePdf(){
        String original = original_text.getText().toString().trim();
        String summary = summary_text.getText().toString().trim();
        com.itextpdf.text.Document mDoc= new com.itextpdf.text.Document(PageSize.A4);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss", Locale.getDefault());
        String mFileName = sdf.format(new Date());
        File root = new File(Environment.getExternalStorageDirectory(),"Short Text");
        if(!root.exists()){
            root.mkdir();
        }
        String mFilePath =  "Summary " + mFileName + " app.pdf";
        File file = new File(root,mFilePath);
        try{
            Font font = FontFactory.getFont(FontFactory.TIMES,30f,Font.BOLD);
            PdfWriter.getInstance(mDoc,new FileOutputStream(file));
            mDoc.open();
            mDoc.addAuthor("Saransh Tiwari");
            mDoc.add(new Paragraph("Original Text :",font));
            mDoc.add(new Paragraph(original,FontFactory.getFont(FontFactory.TIMES,16f)));
            mDoc.add(new Paragraph("\n"));
            mDoc.add(new Paragraph("Summarized Text :",font));
            mDoc.add(new Paragraph(summary,FontFactory.getFont(FontFactory.TIMES,16f)));
            mDoc.close();
            Toast.makeText(this,"PDF Generated.",Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            Toast.makeText(this,e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }

    public void showSummary(){
        String newstring = original_text.getText().toString().trim();
        newstring  = newstring.replaceAll("\\s+"," ");
        String ra = ratio_text.getText().toString().trim();
        ra = ra.replaceAll("%","");
        int ratio = Integer.parseInt(ra);
        Data data = new Data(ratio,newstring);
        summaryDB.setValue(data);
        String id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        RequestQueue queue = Volley.newRequestQueue(HomeActivity.this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                URL+id, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String summary = response.getString("Summary");
                    summary = summary.replace("\n", " ").replace("\r", " ");
                    summary_text.setText(summary);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(HomeActivity.this);
                alertDialogBuilder.setMessage("Enter multiple line in text or use proper words.");
                alertDialogBuilder.setPositiveButton("Close",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                Toast.makeText(HomeActivity.this,"Volley Error :"+error,Toast.LENGTH_SHORT).show();
                            }
                        });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                String orte = original_text.getText().toString().trim();
                summary_text.setText(orte);
            }
        });
        queue.add(jsonObjectRequest);
    }
}