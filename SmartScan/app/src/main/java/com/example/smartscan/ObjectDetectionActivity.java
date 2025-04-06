package com.example.smartscan;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.Surface;
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
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.imagecapture.JpegBytes2Disk;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.common.model.CustomRemoteModel;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.common.model.LocalModel;
import com.google.mlkit.common.model.RemoteModelManager;
import com.google.mlkit.linkfirebase.FirebaseModelSource;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions;

import java.io.IOException;

public class ObjectDetectionActivity extends AppCompatActivity {
    private Animation animation1, animation2, animation3, animation4;
    private Button chupAnh, chonAnh, backButton;
    private ImageView hinhAnh;
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
            hinhAnh.setImageURI(uri);
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

        ObjectDetectorOptions options =
                new ObjectDetectorOptions.Builder()
                        .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
                        .enableMultipleObjects()
                        .enableClassification()  // Optional
                        .build();

    }
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }

    /**
     * Get the angle by which an image must be rotated given the device's current
     * orientation.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private int getRotationCompensation(String cameraId, Activity activity, boolean isFrontFacing)
            throws CameraAccessException {
        // Get the device's current rotation relative to its "native" orientation.
        // Then, from the ORIENTATIONS table, look up the angle the image must be
        // rotated to compensate for the device's rotation.
        int deviceRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int rotationCompensation = ORIENTATIONS.get(deviceRotation);

        // Get the device's sensor orientation.
        CameraManager cameraManager = (CameraManager) activity.getSystemService(CAMERA_SERVICE);
        int sensorOrientation = cameraManager
                .getCameraCharacteristics(cameraId)
                .get(CameraCharacteristics.SENSOR_ORIENTATION);

        if (isFrontFacing) {
            rotationCompensation = (sensorOrientation + rotationCompensation) % 360;
        } else { // back-facing
            rotationCompensation = (sensorOrientation - rotationCompensation + 360) % 360;
        }
        return rotationCompensation;
    }
}