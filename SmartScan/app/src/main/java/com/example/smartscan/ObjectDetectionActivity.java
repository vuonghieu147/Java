package com.example.smartscan;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
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
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions;
import com.google.mlkit.vision.objects.defaults.PredefinedCategory;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ObjectDetectionActivity extends AppCompatActivity {
    private Animation animation1, animation2, animation3, animation4;
    private Button chupAnh, chonAnh, backButton;
    private ImageView hinhAnh;
    private InputImage inputImage;
    private final ActivityResultLauncher<Intent> layAnh = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result ->{
        if(result.getResultCode() == RESULT_OK && result.getData() != null){
            Intent duLieu = result.getData();
            String anh = duLieu.getStringExtra("anhUri");
            Uri uri = Uri.parse(anh);
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(),uri);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            hinhAnh.setBackground(null);
            hinhAnh.setImageBitmap(bitmap);
        }
    });
    private final ActivityResultLauncher<PickVisualMediaRequest> chonAnhThuVien = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri ->{
        if (uri != null) {

            hinhAnh.setBackground(null);
            try {
                nhanDienVatThe(uri);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            //hinhAnh.setImageURI(uri);
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(),uri);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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
                            layAnh.launch(new Intent(ObjectDetectionActivity.this,CameraX.class));
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
    private void nhanDienVatThe(Uri uri) throws IOException {
        ObjectDetectorOptions options =
                new ObjectDetectorOptions.Builder()
                        .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
                        .enableMultipleObjects()
                        .enableClassification()  // Optional
                        .build();
        ObjectDetector objectDetector = ObjectDetection.getClient(options);
        try {
            inputImage = InputImage.fromFilePath(this, uri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        InputStream inputStream = getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        inputStream.close();
        Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);

        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1);
        objectDetector.process(inputImage)
                .addOnSuccessListener(new OnSuccessListener<List<DetectedObject>>() {
                            @Override
                            public void onSuccess(List<DetectedObject> detectedObjects) {
                                // Task completed successfully
                                for (DetectedObject detectedObject : detectedObjects) {
                                    Rect boundingBox = detectedObject.getBoundingBox();
                                    Integer trackingId = detectedObject.getTrackingId();
                                    canvas.drawRect(boundingBox, paint);
                                    for (DetectedObject.Label label : detectedObject.getLabels()) {
                                        String text = label.getText();
                                        canvas.drawText(text,boundingBox.left, boundingBox.right-10,paint);
                                        if (PredefinedCategory.FOOD.equals(text)) {

                                        }
                                        int index = label.getIndex();
                                        if (PredefinedCategory.FOOD_INDEX == index) {

                                        }
                                        float confidence = label.getConfidence();
                                    }
                                    hinhAnh.setImageBitmap(mutableBitmap);
                                }
                            }
                        })
                .addOnFailureListener(new OnFailureListener() {
                            public void onFailure(Exception e) {
                                // Task failed with an exception
                            }
                        });
    }
}