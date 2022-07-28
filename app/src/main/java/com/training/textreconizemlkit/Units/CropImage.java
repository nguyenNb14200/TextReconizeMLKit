package com.training.textreconizemlkit.Units;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


import com.training.textreconizemlkit.R;

public class CropImage extends View {
    Bitmap bitmap;
    Path path = new Path();
    Paint paint;

    public CropImage(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.white_page);
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.RED);
        paint.setStrokeWidth(5f);
    }

    public void clearPaint(){
    }

    public void setImageBitmap(Bitmap bitmap){
        this.bitmap = bitmap;
    }

    public Bitmap getImageBitmap(){
        return  this.bitmap;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setStrokeWidth(5f);
        canvas.drawBitmap(bitmap, 0, 0, null);
        canvas.drawPath(path, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                path.moveTo(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                path.lineTo(x, y);
                break;
            case MotionEvent.ACTION_UP:
                path.lineTo(x, y);
                crop();
                break;
        }
        invalidate();
        return true;
    }

    private void crop() {
        Bitmap croppedBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
        Canvas cropCanvas = new Canvas(croppedBitmap);
        Paint paint = new Paint();
        cropCanvas.drawPath(path, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        cropCanvas.drawBitmap(bitmap, 0, 0, paint);
        bitmap = croppedBitmap;
    }
}

