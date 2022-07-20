package com.training.textreconizemlkit.UI.ReadText;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.training.textreconizemlkit.UI.ReadText.GraphicOverlay;

/**
 * Graphic instance for rendering FirebaseVisionTextBlock position, size, and ID within an associated graphic
 * overlay view.
 */
public class TextGraphic extends GraphicOverlay.Graphic {

    private static final String TAG = "FirebaseVisionTextGraphic";
    private static final int FirebaseVisionText_COLOR = Color.RED;
    private static final float FirebaseVisionText_SIZE = 54.0f;
    private static final float STROKE_WIDTH = 4.0f;

    private final Paint rectPaint;
    private final Paint FirebaseVisionTextPaint;
    private final FirebaseVisionText.Element element;

    TextGraphic(GraphicOverlay overlay, FirebaseVisionText.Element element) {
        super(overlay);

        this.element = element;

        rectPaint = new Paint();
        rectPaint.setColor(FirebaseVisionText_COLOR);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(STROKE_WIDTH);

        FirebaseVisionTextPaint = new Paint();
        FirebaseVisionTextPaint.setColor(FirebaseVisionText_COLOR);
        FirebaseVisionTextPaint.setTextSize(FirebaseVisionText_SIZE);
        // Redraw the overlay, as this graphic has been added.
        postInvalidate();
    }

    /**
     * Draws the FirebaseVisionText block annotations for position, size, and raw value on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        Log.d(TAG, "on draw FirebaseVisionText graphic");
        if (element == null) {
            throw new IllegalStateException("Attempting to draw a null FirebaseVisionText.");
        }

        // Draws the bounding box around the FirebaseVisionTextBlock.
        RectF rect = new RectF(element.getBoundingBox());
        canvas.drawRect(rect, rectPaint);

        // Renders the FirebaseVisionText at the bottom of the box.
        // canvas.drawFirebaseVisionText(element.getFirebaseVisionText(), rect.left, rect.bottom, FirebaseVisionTextPaint);
    }
}
