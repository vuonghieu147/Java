package com.example.smartscan;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;
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
    public void LabelImages(Context context, Uri uri, TextRecognitionCallback callback){
        InputImage image = null;
        try {
            image = InputImage.fromFilePath(context, uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        ImageLabeler labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);
        ImageLabelerOptions options = new ImageLabelerOptions.Builder().setConfidenceThreshold(0.7f).build();
        ImageLabeler labeler = ImageLabeling.getClient(options);
        labeler.process(image)
                .addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
                    public void onSuccess(List<ImageLabel> labels) {
                        StringBuilder stringBuilder = new StringBuilder();
                        for (ImageLabel label : labels) {
                            String text = label.getText();
                            float confidence = label.getConfidence();
                            int index = label.getIndex();
                            Log.d("CHECK", " text conf index: " + text + confidence + " " + index);
                            stringBuilder.append(text).append(" ");
                        }
                        callback.onTextRecognized(stringBuilder.toString().trim());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    public void onFailure( Exception e) {
                        Toast.makeText(context, "Nhận diện thất bại", Toast.LENGTH_SHORT).show();
                    }
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
