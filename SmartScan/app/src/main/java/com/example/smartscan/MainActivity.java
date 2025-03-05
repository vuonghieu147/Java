
package com.example.smartscan;

import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private CardView chupAnh, nhanDienVanBan, dichVanBan, nhanDienVatThe, timKiemThongTinAnh;
    Animation animation1, animation2, animation3, animation4, animation5, animation6, animation7, animation8, animation9, animation10;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        nhanDienVanBan = findViewById(R.id.nhan_dien_van_ban_button);
        dichVanBan = findViewById(R.id.dich_van_ban_button);
        nhanDienVatThe = findViewById(R.id.nhan_dien_vat_the_button);
        timKiemThongTinAnh = findViewById(R.id.tim_kiem_thong_tin_anh_button);
        animation1 = AnimationUtils.loadAnimation(this, R.anim.animation1);
        animation2 = AnimationUtils.loadAnimation(this, R.anim.animation2);
        animation3 = AnimationUtils.loadAnimation(this,R.anim.animation3);
        animation4 = AnimationUtils.loadAnimation(this,R.anim.animation4);
        animation5 = AnimationUtils.loadAnimation(this,R.anim.animation5);
        animation6 = AnimationUtils.loadAnimation(this,R.anim.animation6);
        animation7 = AnimationUtils.loadAnimation(this,R.anim.animation7);
        animation8 = AnimationUtils.loadAnimation(this,R.anim.animation8);
        animation9 = AnimationUtils.loadAnimation(this,R.anim.animation9);
        animation10 = AnimationUtils.loadAnimation(this,R.anim.animation10);

        nhanDienVanBan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        nhanDienVanBan.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == motionEvent.ACTION_UP) {
                    nhanDienVanBan.startAnimation(animation3);
                } else if(motionEvent.getAction() == motionEvent.ACTION_DOWN){
                    nhanDienVanBan.startAnimation(animation4);
                }
                startActivity(new Intent(MainActivity.this,TextRecognition.class));
                return true;
            }
        });
        dichVanBan.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == motionEvent.ACTION_UP) {
                    dichVanBan.startAnimation(animation5);
                } else if(motionEvent.getAction() == motionEvent.ACTION_DOWN){
                    dichVanBan.startAnimation(animation6);
                }
                return true;
            }
        });
        nhanDienVatThe.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == motionEvent.ACTION_UP) {
                    nhanDienVatThe.startAnimation(animation7);
                } else if(motionEvent.getAction() == motionEvent.ACTION_DOWN){
                    nhanDienVatThe.startAnimation(animation8);
                }
                return true;
            }
        });
        timKiemThongTinAnh.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == motionEvent.ACTION_UP) {
                    timKiemThongTinAnh.startAnimation(animation9);
                } else if(motionEvent.getAction() == motionEvent.ACTION_DOWN){
                    timKiemThongTinAnh.startAnimation(animation10);
                }
                return true;
            }
        });

    }

}
