package com.example.smartscan;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;


public class TextRecognition extends AppCompatActivity {
    private Button back, chupAnh, chonAnh, saoChepVanBan;
    private TextView vanBan;
    private ImageView hinhAnh;
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //startActivity(new Intent(TextRecognition.this,CameraX.class));
                layAnh.launch(new Intent(TextRecognition.this,CameraX.class));
            } else {
                Toast.makeText(this, "Bạn cần cấp quyền để sử dụng camera", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private final ActivityResultLauncher<Intent> layAnh = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result ->{
        if(result.getResultCode() == RESULT_OK && result.getData() != null){
            Intent duLieu = result.getData();
            String anhString = duLieu.getStringExtra("anhUri");
            Uri uri = Uri.parse(anhString);
            hinhAnh.setImageURI(uri);
        }
    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_text_recognition);

        Log.d("ActivityCheck", "1111");
        back = findViewById(R.id.back_button);
        chupAnh = findViewById(R.id.chup_anh);
        chonAnh = findViewById(R.id.chon_anh);
        saoChepVanBan = findViewById(R.id.copy_text);
        hinhAnh = findViewById(R.id.hinh_anh);
        vanBan = findViewById(R.id.text_result);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(TextRecognition.this,MainActivity.class));
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
                        //startActivity(new Intent(TextRecognition.this,CameraX.class));
                        layAnh.launch(new Intent(TextRecognition.this,CameraX.class));
                    }
                }else {
                    Toast.makeText(context, "Thiết bị của bạn không có chức năng chụp ảnh", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}