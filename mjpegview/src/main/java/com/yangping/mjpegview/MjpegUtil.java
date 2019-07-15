package com.yangping.mjpegview;

import android.graphics.*;

class MjpegUtil {
    static Rect destRect(int displayMode, int disWidth, int disHeight, int bitmapWidth, int bitmapHeight) {
        int tempx;
        int tempy;
        if (displayMode == MjpegView.SIZE_STANDARD) {
            tempx = (disWidth / 2) - (bitmapWidth / 2);
            tempy = (disHeight / 2) - (bitmapHeight / 2);
            return new Rect(tempx, tempy, bitmapWidth + tempx, bitmapHeight + tempy);
        }
        if (displayMode == MjpegView.SIZE_BEST_FIT) {
            float bitmapAsp = (float) bitmapWidth / bitmapHeight;
            bitmapWidth = disWidth;
            bitmapHeight = (int) (disWidth / bitmapAsp);
            if (bitmapHeight > disHeight) {
                bitmapHeight = disHeight;
                bitmapWidth = (int) (disHeight * bitmapAsp);
            }
            tempx = (disWidth / 2) - (bitmapWidth / 2);
            tempy = (disHeight / 2) - (bitmapHeight / 2);
            return new Rect(tempx, tempy, bitmapWidth + tempx, bitmapHeight + tempy);
        }

        if (displayMode == MjpegView.SIZE_FULLSCREEN) {
            return new Rect(0, 0, disWidth, disHeight);
        } else {
            return null;
        }
    }

    static Bitmap makeFpsOverlay(Paint paint, String text) {
        Rect rect = new Rect();
        paint.getTextBounds(text, 0, text.length(), rect);
        int rectWidth = rect.width() + 2;
        int rectHeight = rect.height() + 2;
        Bitmap bitmap = Bitmap.createBitmap(rectWidth, rectHeight, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        paint.setColor(Color.BLACK);
        canvas.drawRect(0f, 0f, (float) rectWidth, (float) rectHeight, paint);
        paint.setColor(Color.WHITE);
        canvas.drawText(text, (float) (-rect.left + 1), (rectHeight / 2f) - ((paint.ascent() + paint.descent()) / 2) + 1, paint);
        return bitmap;
    }
}
