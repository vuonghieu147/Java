package com.example.smartscan;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.example.smartscan.ml.MobilenetV110224Quant;
import com.google.mlkit.vision.common.InputImage;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.InterpreterApi;
import org.tensorflow.lite.InterpreterFactory;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ObjectDetectionActivity extends AppCompatActivity {
    private Animation animation1, animation2, animation3, animation4;
    private Button chupAnh, chonAnh, backButton;
    private ImageView hinhAnh;
    private Bitmap bitmap;
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                layAnh.launch(new Intent(ObjectDetectionActivity.this,CameraXObjectDetectionActivity.class));
            } else {
                Toast.makeText(this, "Bạn cần cấp quyền để sử dụng camera", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private final ActivityResultLauncher<Intent> layAnh = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result ->{
        if(result.getResultCode() == RESULT_OK && result.getData() != null){
            Intent duLieu = result.getData();
            String anh = duLieu.getStringExtra("anhUri");
            int rotation = duLieu.getIntExtra("rotation", 1000);
            Uri uri = Uri.parse(anh);
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),uri);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            hinhAnh.setBackground(null);
            hinhAnh.setImageURI(uri);
        }
    });
    protected final ActivityResultLauncher<PickVisualMediaRequest> chonAnhThuVien = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri ->{
        if (uri != null) {
            hinhAnh.setBackground(null);
            //hinhAnh.setImageURI(uri);
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),uri);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            nhanDienVatThe();
        } else {
            Log.d("PhotoPicker", "No media selected");
        }
    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_object_detection);
        animation1 = AnimationUtils.loadAnimation(this, R.anim.animation1);
        animation2 = AnimationUtils.loadAnimation(this, R.anim.animation2);
        animation3 = AnimationUtils.loadAnimation(this,R.anim.animation3);
        animation4 = AnimationUtils.loadAnimation(this,R.anim.animation4);
        chupAnh = findViewById(R.id.chup_anh_object);
        chonAnh = findViewById(R.id.chon_anh_object);
        backButton = findViewById(R.id.back_button_object_detection);
        hinhAnh = findViewById(R.id.hinh_anh);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ObjectDetectionActivity.this, MainActivity.class));
                finish();
            }
        });
        chupAnh.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == motionEvent.ACTION_UP) {
                    chupAnh.startAnimation(animation1);
                    final Context context = view.getContext();
                    if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            String[] permission = {android.Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                            requestPermissions(permission, 1);
                        }
                        else{
                            layAnh.launch(new Intent(ObjectDetectionActivity.this,CameraXObjectDetectionActivity.class));
                        }
                    }else {
                        Toast.makeText(context, "Thiết bị của bạn không có chức năng chụp ảnh", Toast.LENGTH_SHORT).show();
                    }
                } else if(motionEvent.getAction() == motionEvent.ACTION_DOWN){
                    chupAnh.startAnimation(animation2);
                }
                return true;
            }
        });
        chonAnh.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == motionEvent.ACTION_UP) {
                    chonAnh.startAnimation(animation3);
                    chonAnhThuVien.launch(new PickVisualMediaRequest.Builder()
                            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                            .build());

                } else if(motionEvent.getAction() == motionEvent.ACTION_DOWN){
                    chonAnh.startAnimation(animation4);
                }
                return true;
            }
        });
    }
    private void nhanDienVatThe(){
        Yolov11 detector = new Yolov11(ObjectDetectionActivity.this, "yolo11nfloat32.tflite");
        List<Yolov11.DetectionResult> results = detector.detect(bitmap);
        Bitmap output = detector.drawResults(this, bitmap, results);
        hinhAnh.setImageBitmap(output);
    }
}