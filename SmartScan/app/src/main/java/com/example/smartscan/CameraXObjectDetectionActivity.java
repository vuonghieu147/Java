package com.example.smartscan;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import androidx.activity.EdgeToEdge;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraXObjectDetectionActivity extends AppCompatActivity {
    private ProcessCameraProvider cameraProvider;
    private PreviewView previewView;
    private Button button;
    private SurfaceView surfaceView;
    private int rotation;
    private Yolov11 yolov11;
    private ExecutorService detectionExecutor;
    private Paint boxPaint;
    private Paint textPaint;
    private OverlayView overlayView;
    private final int cameraSau = CameraSelector.LENS_FACING_BACK;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_camera_xobject_detection);
        previewView = findViewById(R.id.preview_view_obj);
        button = findViewById(R.id.cancel_button_obj);
        surfaceView = findViewById(R.id.surfaceView);
        detectionExecutor = Executors.newSingleThreadExecutor();
        previewView.setScaleType(PreviewView.ScaleType.FILL_CENTER);
        yolov11 = new Yolov11(this, "yolo11nfloat32.tflite");
        overlayView = findViewById(R.id.overlay_view);
        khoiTaoPaint();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        moCamera();
    }
    private void moCamera(){
        ListenableFuture<ProcessCameraProvider> listenableFuture = ProcessCameraProvider.getInstance(this);
        listenableFuture.addListener(() -> {
            try {
                cameraProvider = listenableFuture.get();
                rotation = previewView.getDisplay().getRotation();
                Preview preview = new Preview.Builder().setTargetRotation(rotation).build();

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder().setTargetRotation(rotation)
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                        .build();
                CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(cameraSau).build();
                imageAnalysis.setAnalyzer(detectionExecutor, new ImageAnalysis.Analyzer() {
                    @Override
                    public void analyze(ImageProxy imageProxy) {

                        float rotationDegrees = previewView.getRotation();//imageProxy.getImageInfo().getRotationDegrees();
                        Log.d("CHECK", "rotation: " + rotationDegrees);
                        Log.d("CHECK", "chieu cao va rong: " + imageProxy.getHeight() + imageProxy.getWidth());
                        Bitmap imageProxyToBitmap = imageProxyToBitmap(imageProxy);
                        Bitmap temp = rotateBitmapLeft(imageProxyToBitmap);
                        List<Yolov11.DetectionResult> resultList = yolov11.detect(temp);
                        List<Yolov11.DetectionResult> finalResults = yolov11.applyNMS(resultList, 0.4f);
                        List<Yolov11.DetectionResult> resizedResults = new ArrayList<>();
                        for(Yolov11.DetectionResult result: finalResults){
                            RectF box = resizeImage(previewView.getHeight(),previewView.getWidth(),imageProxy.getWidth(), imageProxy.getHeight(),result.box);
                            Yolov11.DetectionResult newResult = new Yolov11.DetectionResult( box,result.classId, result.score);
                            resizedResults.add(newResult);
                        }
                        Log.d("CHECK","ketqua: " + finalResults);
                        runOnUiThread(() -> {
                            overlayView.setResults(resizedResults);
                        ;});
                        imageProxy.close();
                    }
                });

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }
    @OptIn(markerClass = ExperimentalGetImage.class)
    private Bitmap imageProxyToBitmap(ImageProxy imageProxy){
        Image image = imageProxy.getImage();
        ByteBuffer byteBuffer = image.getPlanes()[0].getBuffer();
        Bitmap bitmap = Bitmap.createBitmap(imageProxy.getWidth(), imageProxy.getHeight(), Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(byteBuffer);

        return bitmap;
    }
    public static Bitmap rotateBitmapLeft(Bitmap source) {
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }
    private void khoiTaoPaint(){
        boxPaint = new Paint();
        textPaint = new Paint();

        boxPaint.setColor(Color.RED);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(4f);

        textPaint.setColor(Color.RED);
        textPaint.setTextSize(36f);
        textPaint.setShadowLayer(5f, 30f, 30f, Color.BLACK);
    }
    private RectF resizeImage(int sizeGocH, int sizeGocW, int yoloH, int yoloW,  RectF box){
        float x = sizeGocW/(float)yoloW;
        float y = sizeGocH/(float)yoloH;
        return new RectF(box.left*x, box.top*y, box.right*x, box.bottom*y);
    }
    protected void onDestroy() {
        super.onDestroy();
        if (detectionExecutor != null) {
            detectionExecutor.shutdown();
        }
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
    }
}