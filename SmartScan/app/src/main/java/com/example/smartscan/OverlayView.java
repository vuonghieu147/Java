package com.example.smartscan;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class OverlayView extends View {
    private List<Yolov11.DetectionResult> results = new ArrayList<>();
    private Paint boxPaint, textPaint;

    public OverlayView(Context context) {
        super(context);
        init();
    }

    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        boxPaint = new Paint();
        boxPaint.setColor(Color.RED);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(5f);

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(50f);
        textPaint.setShadowLayer(5f, 0f, 0f, Color.BLACK);
    }
    public void setResults(List<Yolov11.DetectionResult> results) {
        this.results = results;
        postInvalidate();
    }


    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Yolov11 yolov11 = new Yolov11(this.getContext(), "yolo11nfloat32.tflite");
        String[] labels = yolov11.loadLabels(this.getContext());
        setBackgroundColor(Color.TRANSPARENT);
        for (Yolov11.DetectionResult result : results) {
            canvas.drawRect(result.box, boxPaint);

            String className;
            if (labels != null && result.classId < labels.length) {
                className = labels[result.classId];
            } else {
                className = "unknown";
            }

            String label = String.format("%s conf: %.2f", className, result.score);
            canvas.drawText(label, result.box.left, result.box.top - 10, textPaint);
        }
    }
}