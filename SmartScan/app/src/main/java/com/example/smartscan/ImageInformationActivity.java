package com.example.smartscan;

import android.Manifest;
import android.app.DownloadManager;
import android.app.appsearch.SearchResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


public class ImageInformationActivity extends AppCompatActivity {
    private Button chupAnh, chonAnh, back;
    private ImageView hinhAnh;
    private Animation animation1, animation2, animation3, animation4, animation5, animation6;
    private RecyclerView recyclerView;
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                layAnh.launch(new Intent(ImageInformationActivity.this,CameraX.class));
            } else {
                Toast.makeText(this, "Bạn cần cấp quyền để sử dụng camera", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private final ActivityResultLauncher<PickVisualMediaRequest> chonAnhThuVien = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri ->{
        if (uri != null) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),uri);
                timKiemHinhAnh(uri);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            hinhAnh.setBackground(null);
            hinhAnh.setImageURI(uri);
            hinhAnh.setTag(uri);
        } else {
            Log.d("PhotoPicker", "No media selected");
        }
    });


    private final ActivityResultLauncher<Intent> layAnh = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result ->{
        if(result.getResultCode() == RESULT_OK && result.getData() != null){
            Intent duLieu = result.getData();
            String anh = duLieu.getStringExtra("anhUri");
            Uri uri = Uri.parse(anh);
            timKiemHinhAnh(uri);
            hinhAnh.setBackground(null);
            hinhAnh.setImageURI(uri);
            hinhAnh.setTag(uri);

        }
    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_image_information);
        animation1 = AnimationUtils.loadAnimation(this,R.anim.animation1);
        animation2 = AnimationUtils.loadAnimation(this,R.anim.animation2);
        animation3 = AnimationUtils.loadAnimation(this,R.anim.animation3);
        animation4 = AnimationUtils.loadAnimation(this,R.anim.animation4);
        animation5 = AnimationUtils.loadAnimation(this,R.anim.animation5);
        animation6 = AnimationUtils.loadAnimation(this,R.anim.animation6);
        chonAnh = findViewById(R.id.chon_anh);
        chupAnh = findViewById(R.id.chup_anh);
        back = findViewById(R.id.back_button);
        hinhAnh = findViewById(R.id.hinh_anh);
        recyclerView = findViewById(R.id.recyclerview);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        chupAnh.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == motionEvent.ACTION_UP) {
                    chupAnh.startAnimation(animation3);
                    final Context context = view.getContext();
                    if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            String[] permission = {android.Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                            requestPermissions(permission, 1);
                        }
                        else{
                            layAnh.launch(new Intent(ImageInformationActivity.this,CameraX.class));
                        }
                    }else {
                        Toast.makeText(context, "Thiết bị của bạn không có chức năng chụp ảnh", Toast.LENGTH_SHORT).show();
                    }
                } else if(motionEvent.getAction() == motionEvent.ACTION_DOWN){
                    chupAnh.startAnimation(animation4);
                }
                return true;
            }
        });
        chonAnh.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == motionEvent.ACTION_UP) {
                    chonAnh.startAnimation(animation1);
                    chonAnhThuVien.launch(new PickVisualMediaRequest.Builder()
                            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                            .build());
                } else if(motionEvent.getAction() == motionEvent.ACTION_DOWN){
                    chonAnh.startAnimation(animation2);
                }
                return true;
            }
        });
    }
    private Bitmap chuyenSangBitmap(Uri uri) {
        try{
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),uri);
            return bitmap;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }

    }
    private void timKiemHinhAnh(Uri uri) {
        ImageProcessing imageProcessing = new ImageProcessing();
        StringBuilder stringBuilder = new StringBuilder();
        String label = imageProcessing.getLabelObject(this, chuyenSangBitmap(uri));
        try {
            imageProcessing.getTextFromImage(this, uri, new ImageProcessing.TextRecognitionCallback() {
                @Override
                public void onTextRecognized(String text) {
                    String string = String.valueOf(stringBuilder.append(text).append(" ").append(label));
                    customSearchAPIRequest(ImageInformationActivity.this, string);
//                    String encodedQuery = URLEncoder.encode(string, "UTF-8");
//                    Log.d("CHECK","string: " + string);
                }
            });
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    private void customSearchAPIRequest(Context context, String text) {
        String key = "AIzaSyArBSzdA7jOeEur_r4NKuST3JiQ9kFX-HQ";
        String url = "https://www.googleapis.com/customsearch/v1";
        String cx = "a51c3b727b6ed4690";
        String urlRequest = "";
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        try{
            String encodedQuery = URLEncoder.encode(text, "UTF-8");
            urlRequest = url + "?key=" + key + "&cx=" + cx + "&q=" + encodedQuery + "&num=10";
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
            return;
        }
        Log.d("URL_REQUEST", urlRequest);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, urlRequest, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                try {

                    List<ResponseData> results = new ArrayList<>();
                    if (!jsonObject.has("items") || jsonObject.getJSONArray("items").length() == 0) {
                        // Không có kết quả
                        results.add(new ResponseData("Không tìm thấy kết quả", "", ""));
                    } else {
                        JSONArray items = jsonObject.getJSONArray("items");
                        for(int i = 0; i < items.length(); i ++){
                            JSONObject item = items.getJSONObject(i);
                            results.add(new ResponseData(item.getString("title"), item.getString("link"), item.getString("snippet")));
                            Log.d("CHECK","DATA: " + results.get(i));
                        }
                    }
                    SearchRecyclerView adapter = new SearchRecyclerView(ImageInformationActivity.this,results);
                    recyclerView.setAdapter(adapter);
                    recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                String errorMsg = "Lỗi mạng: " + volleyError.getMessage();
                if (volleyError.networkResponse != null) {
                    errorMsg += " (Mã lỗi: " + volleyError.networkResponse.statusCode + ")";
                }
                Log.e("API ERROR", errorMsg);
                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show();
            }
        });
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
        requestQueue.add(jsonObjectRequest);
    }
}