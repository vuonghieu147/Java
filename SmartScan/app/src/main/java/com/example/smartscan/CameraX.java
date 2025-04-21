package com.example.smartscan;


import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.camera.core.Camera;
import androidx.camera.core.ExperimentalZeroShutterLag;
import androidx.camera.core.ImageCaptureException;
import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class CameraX extends AppCompatActivity {
    private Button chup;
    private ImageButton flash;
    private ProcessCameraProvider cameraProvider;
    private PreviewView previewView;
    private ImageCapture imageCapture;
    private int rotation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_camera_x);
        flash = findViewById(R.id.flash);
        chup = findViewById(R.id.chup_cameraX);
        previewView = findViewById(R.id.preview_view);
        flash.setTag(R.drawable.baseline_flash_on_24);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 101);
        }

        moCamera();
        chup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chupLuuAnh();
            }
        });
    }

    private void moCamera(){
        ListenableFuture<ProcessCameraProvider> listenableFuture = ProcessCameraProvider.getInstance(this);
        listenableFuture.addListener(() -> {
            try {
                cameraProvider = listenableFuture.get();
                Preview preview = new Preview.Builder().build();
                rotation = previewView.getDisplay().getRotation();
                imageCapture = new ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).setTargetRotation(rotation).build();
                CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                cameraProvider.unbindAll();
                Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
                flash.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if((int)flash.getTag() == R.drawable.baseline_flash_off_24){
                            camera.getCameraControl().enableTorch(true);
                            flash.setBackgroundResource(R.drawable.baseline_flash_on_24);
                            flash.setTag(R.drawable.baseline_flash_on_24);
                        } else if ((int)flash.getTag() == R.drawable.baseline_flash_on_24) {
                            camera.getCameraControl().enableTorch(false);
                            flash.setBackgroundResource(R.drawable.baseline_flash_off_24);
                            flash.setTag(R.drawable.baseline_flash_off_24);
                        }
                    }
                });
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }
    private void chupLuuAnh(){
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, "smartscan_" + System.currentTimeMillis() + ".jpg");
//        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
//        contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/SmartScan");
        File tempFile = new File(getFilesDir(), "smartscan_" + System.currentTimeMillis() + ".jpg");
        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(tempFile).build();
        imageCapture.takePicture(outputFileOptions, Executors.newSingleThreadExecutor(), new ImageCapture.OnImageSavedCallback(){

            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                Uri anhUri = outputFileResults.getSavedUri();
                int rotation = imageCapture.getTargetRotation();
                Intent intent = new Intent();
                intent.putExtra("anhUri", anhUri.toString());
                intent.putExtra("rotation", rotation);
                setResult(RESULT_OK, intent);
                finish();
            }
            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Toast.makeText(CameraX.this,"Chụp ảnh thất bại",Toast.LENGTH_SHORT).show();
            }
        });
    }
}