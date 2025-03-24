package com.example.smartscan;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
    private TextView outputText;
    private String inNgonNguMacDinh = "vi";
    private String outNgonNguMacDinh = "en";

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
            }
        });

        TranslatorOptions options = new TranslatorOptions.Builder().setSourceLanguage(TranslateLanguage.ENGLISH).setTargetLanguage(TranslateLanguage.GERMAN).build();

        dichButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String selectSourceLanguage = inputLanguage.getSelectedItem().toString();
                String selectTargetLanguage = outputLanguage.getSelectedItem().toString();
                String sourceLanguage = DanhSachNgonNgu.danhSachNgonNgu().get(selectSourceLanguage);
                String targetLanguage = DanhSachNgonNgu.danhSachNgonNgu().get(selectTargetLanguage);
                String text = inputText.getText().toString();
                dichVanBan(text, sourceLanguage, targetLanguage);
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
}