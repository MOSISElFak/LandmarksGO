package com.example.mosis.landmarksgo.other;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;

/**
 * Created by k on 5/23/2017.
 */

public class CircularImage {
    public static Bitmap getCroppedBitmap(Bitmap bitmap) {
        int smallerSide = bitmap.getHeight();
        if(bitmap.getWidth()<bitmap.getHeight()){
            smallerSide = bitmap.getWidth();
        }

        Bitmap output = Bitmap.createBitmap(smallerSide, smallerSide, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, smallerSide, smallerSide);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        canvas.drawCircle(smallerSide / 2, smallerSide / 2, smallerSide / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
        //return _bmp;
        return output;
    }
}
