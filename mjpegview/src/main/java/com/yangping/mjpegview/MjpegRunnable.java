package com.yangping.mjpegview;

import android.graphics.*;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.lang.ref.WeakReference;

class MjpegRunnable implements Runnable {
    private final int mjpegViewMode;
    private MjpegInputStream inputStream;
    private final SurfaceHolder surfaceHolder;

    private Paint overlayPaint;                 //fps的畫筆
    private int disWidth = 0;
    private int disHeight = 0;
    private Rect destRect;
    private Bitmap fpsBitmap;
    private boolean isRunning = true;          //是否加載中
    private boolean isCreatedSurface = false;   //Was the SurfaceHolder created.
    private boolean isShowFps = true;           //是否顯示fps

    private int frameCounter = 0; //fps
    private long startTimestamp = 0L;


    //設定圖層關係(可參考下列網址)
    //LINK: https://blog.csdn.net/iispring/article/details/50472485
    private PorterDuffXfermode porterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_OVER);

    public MjpegRunnable(int mode, MjpegInputStream stream, SurfaceHolder holder, boolean isShowFps, boolean isCreatedSurface) {
        this.inputStream = stream;
        this.mjpegViewMode = mode;
        this.surfaceHolder = holder;
        this.isCreatedSurface = isCreatedSurface;
        this.isShowFps = isShowFps;

        //建立畫筆
        overlayPaint = new Paint();
        overlayPaint.setTextAlign(Paint.Align.LEFT);
        overlayPaint.setTextSize(36f);
        overlayPaint.setTypeface(Typeface.DEFAULT);
    }

    void setInputStream(MjpegInputStream inputStream){
        this.inputStream = inputStream;
    }

    void setSurfaceSize(int width, int height) {
        synchronized (surfaceHolder) {
            this.disWidth = width;
            this.disHeight = height;
        }
    }

    void setRunning(boolean running) {
        synchronized (surfaceHolder) {
            this.isRunning = running;
        }
    }

    void setCreatedSurface(boolean createdSurface) {
        synchronized (surfaceHolder) {
            this.isCreatedSurface = createdSurface;
        }
    }

    void setFpsEnable(boolean enable) {
        synchronized (surfaceHolder) {
            this.isShowFps = enable;
        }
    }

    @Override
    public void run() {
        startTimestamp = System.currentTimeMillis();

        //建立畫筆
        Paint paint = new Paint();
        //建立畫布參數
        WeakReference<Canvas> canvas = null;
        while (isRunning) {
            if (isCreatedSurface) {
                try {
                    //取得鎖定的畫布
                    canvas = new WeakReference<Canvas>(surfaceHolder.lockCanvas());
                    long bufferTime = System.currentTimeMillis();
                    synchronized (surfaceHolder) {
                        //畫上畫面
                        synchronizedSurfaceHolder(canvas.get(), paint);
                    }
                    long delayTime = 100 - (System.currentTimeMillis() - bufferTime);
                    if (delayTime > 0) {
                        Thread.sleep(delayTime);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (canvas != null) {
                        //解除畫布鎖定
                        try {
                            surfaceHolder.unlockCanvasAndPost(canvas.get());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                try {
                    Thread.sleep(300L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        if (inputStream != null) {
            try {
                inputStream.close();
                inputStream = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void synchronizedSurfaceHolder(Canvas canvas, Paint paint) {
        try {
            if (inputStream != null) {
                Bitmap bitmap = inputStream.readMjpegFrame();
                if (bitmap != null) {
                    canvas.drawColor(Color.BLACK);
                    destRect = MjpegUtil.destRect(mjpegViewMode, disWidth, disHeight, bitmap.getWidth(), bitmap.getHeight());
                    if (destRect != null) {
                        canvas.drawBitmap(bitmap, null, destRect, paint);
                        if (isShowFps) {
                            paint.setXfermode(porterDuffXfermode);
                            if (fpsBitmap != null) {
                                canvas.drawBitmap(fpsBitmap
                                        , (float) destRect.left
                                        , (float) destRect.bottom - fpsBitmap.getHeight()
                                        , null);
                            }
                            paint.setXfermode(null);
                            ++frameCounter;
                            if ((System.currentTimeMillis() - startTimestamp) >= 1000) {
                                fpsBitmap = MjpegUtil.makeFpsOverlay(overlayPaint, frameCounter + "fps");
                                frameCounter = 0;
                                startTimestamp = System.currentTimeMillis();
                            }
                        }
                        return;
                    }
                }
            }

            /*畫面停止時*/
            if (destRect != null) {
                canvas.drawBitmap(MjpegUtil.makeFpsOverlay(overlayPaint, "00fps")
                        , (float) destRect.left
                        , (float) destRect.bottom - fpsBitmap.getHeight()
                        , null);
            } else {
                canvas.drawColor(Color.BLACK);
                canvas.drawBitmap(MjpegUtil.makeFpsOverlay(overlayPaint, "00fps")
                        ,0f
                        ,disHeight - fpsBitmap.getHeight()
                        , null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
