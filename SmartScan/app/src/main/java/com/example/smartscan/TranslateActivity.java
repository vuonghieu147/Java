package com.example.smartscan;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.languageid.LanguageIdentification;
import com.google.mlkit.nl.languageid.LanguageIdentifier;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.util.ArrayList;
import java.util.Map;

public class TranslateActivity extends AppCompatActivity {
    private Spinner inputLanguage, outputLanguage;
    private Button back, dichButton;
    private EditText inputText;
    private TextView outputText, changeLanguageInput;
    private Animation animation3, animation4;
    private String inNgonNguMacDinh;
    private String outNgonNguMacDinh;
    private String returnLanguage = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_translate);
        inputLanguage = findViewById(R.id.input_language);
        outputLanguage = findViewById(R.id.output_language);
        inputText = findViewById(R.id.input_text);
        outputText = findViewById(R.id.output_text);
        back = findViewById(R.id.back_button_dich);
        dichButton = findViewById(R.id.dich_button);
        changeLanguageInput = findViewById(R.id.change_language_input);
        animation3 = AnimationUtils.loadAnimation(this,R.anim.animation3);
        animation4 = AnimationUtils.loadAnimation(this,R.anim.animation4);
        inputText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                view.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
        outputText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                view.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
        Map <String, String> ngonNgu = DanhSachNgonNgu.danhSachNgonNgu();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(TranslateActivity.this, android.R.layout.simple_spinner_dropdown_item, new ArrayList<String>(ngonNgu.keySet()));
        inputLanguage.setAdapter(adapter);
        inputLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                inNgonNguMacDinh = new ArrayList<>(ngonNgu.values()).get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        outputLanguage.setAdapter(adapter);
        outputLanguage.setSelection(1);
        outputLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                outNgonNguMacDinh = new ArrayList<>(ngonNgu.values()).get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(TranslateActivity.this,MainActivity.class));
                finish();
            }
        });

        TranslatorOptions options = new TranslatorOptions.Builder().setSourceLanguage(TranslateLanguage.ENGLISH).setTargetLanguage(TranslateLanguage.GERMAN).build();
        
        dichButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == motionEvent.ACTION_UP) {
                    dichButton.startAnimation(animation3);
                } else if(motionEvent.getAction() == motionEvent.ACTION_DOWN){
                    dichButton.startAnimation(animation4);
                }
                String selectSourceLanguage = inputLanguage.getSelectedItem().toString();
                String selectTargetLanguage = outputLanguage.getSelectedItem().toString();
                String sourceLanguage = DanhSachNgonNgu.danhSachNgonNgu().get(selectSourceLanguage);
                String targetLanguage = DanhSachNgonNgu.danhSachNgonNgu().get(selectTargetLanguage);
                String text = inputText.getText().toString();
                nhanDienNgonNgu(text, sourceLanguage);
                dichVanBan(text, sourceLanguage, targetLanguage);
                return true;
            }
        });
        changeLanguageInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int index = new ArrayList<String>(DanhSachNgonNgu.danhSachNgonNgu().values()).indexOf(returnLanguage);
                if(index >= 0){
                    Log.d("CHECK","index2: " + index);
                    inputLanguage.setSelection(index);
                }
            }
        });
    }
    private void dichVanBan(String vanBan, String sourceLanguage, String targetLanguage){
        TranslatorOptions options =
                new TranslatorOptions.Builder()
                        .setSourceLanguage(sourceLanguage)
                        .setTargetLanguage(targetLanguage)
                        .build();
        final Translator translator =
                Translation.getClient(options);
        DownloadConditions conditions = new DownloadConditions.Builder()
                .requireWifi()
                .build();
        translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                translator.translate(vanBan).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        outputText.setText(s);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(TranslateActivity.this, "Dịch thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(TranslateActivity.this, "Tải ngôn ngữ thất bại", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void nhanDienNgonNgu(String text, String sourceLanguage){;
        LanguageIdentifier languageIdentifier =
                LanguageIdentification.getClient();
        languageIdentifier.identifyLanguage(text)
                .addOnSuccessListener(
                        new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(String languageCode) {
                                if (languageCode.equals("und")) {
                                    Log.i("CHECK", "Can't identify language.");
                                } else {
                                    if(!languageCode.equals(sourceLanguage)){
                                        for (Map.Entry<String, String> entry : DanhSachNgonNgu.danhSachNgonNgu().entrySet()) {
                                            if (entry.getValue().equals(languageCode)) {
                                                String temp = entry.getKey();
                                                changeLanguageInput.setText("Chuyển sang " + temp);
                                                returnLanguage = languageCode;
                                            }
                                        }
                                    }
                                    else {
                                        changeLanguageInput.setText("");
                                    }
                                }
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Model couldn’t be loaded or other internal error.
                                // ...
                            }
                        });
    }
}