package com.example.smartscan;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

import org.tensorflow.lite.Delegate;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.CompatibilityList;
import org.tensorflow.lite.gpu.GpuDelegate;
import org.tensorflow.lite.gpu.GpuDelegateFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Yolov11{
    private static final int INPUT_SIZE = 640;
    private static final int OUTPUT_CHANNELS = 84;
    private static final int OUTPUT_POINTS = 8400;
    private static final float CONFIDENCE = 0.5f;
    private float widthGoc;
    private float heightGoc;


    private Interpreter interpreter;


    public Yolov11(Context context, String modelFile) {
        try {
            Interpreter.Options options = new Interpreter.Options();
            options.setNumThreads(4);
            interpreter = new Interpreter(loadModelFile(context, modelFile), options);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load model", e);
        }
    }

    private MappedByteBuffer loadModelFile(Context context, String modelFile) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(modelFile);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public List<DetectionResult> detect(Bitmap bitmap) {
        int imageWidth = bitmap.getWidth();
        int imageHeight = bitmap.getHeight();
        float[][][][] input = preprocessImage(bitmap);
        float[][][] output = new float[1][OUTPUT_CHANNELS][OUTPUT_POINTS];

        interpreter.run(input, output);

        return parseOutput(output, imageWidth, imageHeight);
    }

    private float[][][][] preprocessImage(Bitmap bitmap) {
        heightGoc = bitmap.getHeight();
        widthGoc = bitmap.getWidth();
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true);
        float[][][][] input = new float[1][INPUT_SIZE][INPUT_SIZE][3];
        for (int y = 0; y < INPUT_SIZE; y++) {
            for (int x = 0; x < INPUT_SIZE; x++) {
                int pixel = resized.getPixel(x, y);
                float r = ((pixel >> 16) & 0xFF) / 255.0f;
                float g = ((pixel >> 8) & 0xFF) / 255.0f;
                float b = (pixel & 0xFF) / 255.0f;

                input[0][y][x][0] = r;
                input[0][y][x][1] = g;
                input[0][y][x][2] = b;
            }
        }

        return input;
    }

    private float calculateIoU(RectF box1, RectF box2) {
        float x1 = Math.max(box1.left, box2.left);
        float y1 = Math.max(box1.top, box2.top);
        float x2 = Math.min(box1.right, box2.right);
        float y2 = Math.min(box1.bottom, box2.bottom);
        float intersection = Math.max(0, x2 - x1) * Math.max(0, y2 - y1);
        float area1 = (box1.right - box1.left) * (box1.bottom - box1.top);
        float area2 = (box2.right - box2.left) * (box2.bottom - box2.top);
        return intersection / (area1 + area2 - intersection);
    }
    public List<DetectionResult> applyNMS(List<DetectionResult> results, float iouThreshold) {
        List<DetectionResult> filteredResults = new ArrayList<>();
        Collections.sort(results, (a, b) -> Float.compare(b.score, a.score)); // sắp xếp giảm dần theo score

        boolean[] isSuppressed = new boolean[results.size()];

        for (int i = 0; i < results.size(); i++) {
            if (isSuppressed[i]) continue;
            filteredResults.add(results.get(i));

            for (int j = i + 1; j < results.size(); j++) {
                if (isSuppressed[j]) continue;
                float iou = calculateIoU(results.get(i).box, results.get(j).box);
                if (iou > iouThreshold) {
                    isSuppressed[j] = true;
                }
            }
        }
        return filteredResults;
    }

    private List<DetectionResult> parseOutput(float[][][] output, int imageWidth, int imageHeight) {
        List<DetectionResult> results = new ArrayList<>();

        for (int i = 0; i < OUTPUT_POINTS; i++) {
            float cx = output[0][0][i];
            float cy = output[0][1][i];
            float w  = output[0][2][i];
            float h  = output[0][3][i];

            float maxClassScore = -1f;
            int classId = -1;

            // tìm class có score cao nhất
            for (int c = 0; c < 80; c++) {
                float classScore = output[0][4 + c][i];
                if (classScore > maxClassScore) {
                    maxClassScore = classScore;
                    classId = c;
                }
            }
            // chuyển sang toạ độ pixel
            if (maxClassScore < CONFIDENCE) continue;
            float left   = (cx - w / 2f) * imageWidth;
            float top    = (cy - h / 2f) * imageHeight;
            float right  = (cx + w / 2f) * imageWidth;
            float bottom = (cy + h / 2f) * imageHeight;
            RectF box = new RectF(left, top, right, bottom);
            results.add(new DetectionResult(box, classId, maxClassScore));
        }

        return applyNMS(results, 0.4f);
    }
    public String[] loadLabels(Context context) {
        try {
            InputStream inputStream = context.getAssets().open("labels.txt");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            List<String> labels = new ArrayList<>();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                labels.add(line);
            }
            bufferedReader.close();
            return labels.toArray(new String[0]);
        } catch (IOException e) {
            e.printStackTrace();
            return new String[]{"unknown"};
        }
    }
    private RectF resizeImageVeBanDau(int yoloH, int yoloW,  RectF box){
        float x = widthGoc/(float)yoloW;
        float y = heightGoc/(float)yoloH;
        return new RectF(box.left*x, box.top*y, box.right*x, box.bottom*y);
    }

    public Bitmap drawResults(Context context, Bitmap bitmap, List<Yolov11.DetectionResult> results) {
        String[] classLabels = loadLabels(context);
        Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);

        Paint boxPaint = new Paint();
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(4);
        boxPaint.setColor(Color.RED);

        Paint textPaint = new Paint();
        textPaint.setColor(Color.RED);
        textPaint.setTextSize(20);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setShadowLayer(5.0f, 0f, 0f, Color.BLACK);

        for (Yolov11.DetectionResult result : results) {
            RectF box = resizeImageVeBanDau(bitmap.getHeight(), bitmap.getWidth(), result.box);

            canvas.drawRect(box, boxPaint);


            String label;
            if (classLabels != null && result.classId < classLabels.length) {
                label = classLabels[result.classId];
            } else {
                label = "Unknown";
            }
            Log.d("CHECK", "ten : " + label);


            String text = label + String.format(" %.2f", result.score);
            canvas.drawText(text, box.left, box.top - 10, textPaint);
        }

        return mutableBitmap;
    }

    public static class DetectionResult {
        public RectF box;
        public int classId;
        public float score;

        public DetectionResult(RectF box, int classId, float score) {
            this.box = box;
            this.classId = classId;
            this.score = score;
        }
    }
}
