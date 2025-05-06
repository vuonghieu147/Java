package com.example.smartscan;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.util.List;

public class ImageProcessing {
    public interface TextRecognitionCallback {
        void onTextRecognized(String text);
    }
    public void getTextFromImage(Context context, Uri uri, TextRecognitionCallback callback) throws IOException {
        InputImage inputImage = InputImage.fromFilePath(context,uri);
        TextRecognizer textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        Task <Text> results = textRecognizer.process(inputImage).addOnSuccessListener(new OnSuccessListener<Text>() {
            @Override
            public void onSuccess(Text text) {
                StringBuilder noiText = new StringBuilder();
                for(Text.TextBlock block : text.getTextBlocks()){
                    for(Text.Line line : block.getLines()){
                        noiText.append(line.getText()).append(" ");
                    }
                }
                callback.onTextRecognized(noiText.toString().trim());
            }
        }).addOnFailureListener(e -> {
            e.printStackTrace();
            callback.onTextRecognized(null);
        });
    }
    public String getLabelObject(Context context, Bitmap bitmap){
        if(bitmap == null || bitmap.isRecycled()){return "";}
        try{
            Yolov11 yolov11 = new Yolov11(context, "yolo11nfloat32.tflite");
            StringBuilder stringBuilder = new StringBuilder();
            String label = null;
            int idObject = -1;
            List<Yolov11.DetectionResult> results = yolov11.detect(bitmap);
            List<Yolov11.DetectionResult> filteredResults = yolov11.applyNMS(results, 0.4f);
            for(Yolov11.DetectionResult result: filteredResults){
                if(result.classId != idObject) {
                    idObject = result.classId;
                    String[] classLabels = yolov11.loadLabels(context);
                    if (classLabels != null && result.classId < classLabels.length) {
                        stringBuilder.append(classLabels[result.classId]).append(" ");
                    }
                }
            }
            return stringBuilder.toString().trim();
        }catch (Exception e){
            e.printStackTrace();
            return "";
        }

    }
}
