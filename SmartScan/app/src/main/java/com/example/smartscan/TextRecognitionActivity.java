package com.example.smartscan;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import java.io.IOException;




public class TextRecognitionActivity extends AppCompatActivity {
    private Button back, chupAnh, chonAnh, saoChepVanBan;
    private TextView vanBan;
    private ImageView hinhAnh;
    private ImageButton xoayAnh;
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                layAnh.launch(new Intent(TextRecognitionActivity.this,CameraX.class));
            } else {
                Toast.makeText(this, "Bạn cần cấp quyền để sử dụng camera", Toast.LENGTH_SHORT).show();
            }
        }
    }
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
            nhanDienVanBan(bitmap);
        } else {
            Log.d("PhotoPicker", "No media selected");
        }
    });
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
            nhanDienVanBan(bitmap);
        }
    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_text_recognition);

        back = findViewById(R.id.back_button);
        chupAnh = findViewById(R.id.chup_anh);
        chonAnh = findViewById(R.id.chon_anh);
        saoChepVanBan = findViewById(R.id.copy_text);
        hinhAnh = findViewById(R.id.hinh_anh);
        vanBan = findViewById(R.id.text_result);
        xoayAnh = findViewById(R.id.xoay_anh);
        vanBan.setMovementMethod(new ScrollingMovementMethod());

        vanBan.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                view.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
        xoayAnh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hinhAnh.setRotation(hinhAnh.getRotation() + 90F);
                BitmapDrawable drawable = (BitmapDrawable) hinhAnh.getDrawable();
                Bitmap bitmap = drawable.getBitmap();
                vanBan.setText(null);
                nhanDienVanBan(bitmap);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(TextRecognitionActivity.this,MainActivity.class));
                finish();
            }
        });
        chupAnh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Context context = view.getContext();
                if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                    if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        requestPermissions(permission, 1);
                    }
                    else{
                        layAnh.launch(new Intent(TextRecognitionActivity.this,CameraX.class));
                    }
                }else {
                    Toast.makeText(context, "Thiết bị của bạn không có chức năng chụp ảnh", Toast.LENGTH_SHORT).show();
                }
            }
        });
        chonAnh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chonAnhThuVien.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
            }
        });
        saoChepVanBan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(vanBan.getText().toString().isEmpty()){
                    Toast.makeText(TextRecognitionActivity.this,"Không có văn bản để  sao chép",Toast.LENGTH_SHORT).show();
                    return;
                }
                ClipboardManager clipboardManager = (ClipboardManager)getSystemService(TextRecognitionActivity.this.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("vanban",vanBan.getText().toString());
                clipboardManager.setPrimaryClip(clipData);
                Toast.makeText(TextRecognitionActivity.this,"Đã sao chép",Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void nhanDienVanBan(Bitmap bitmap) {
        if (bitmap != null) {
            InputImage inputImage = InputImage.fromBitmap(bitmap, 0);
            TextRecognizer textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
            Task<Text> result = textRecognizer.process(inputImage).addOnSuccessListener(new OnSuccessListener<Text>() {
                @Override
                public void onSuccess(Text text) {

                    String resultText = text.getText();
                    vanBan.setText(resultText);
                }
            });

        }
    }

}