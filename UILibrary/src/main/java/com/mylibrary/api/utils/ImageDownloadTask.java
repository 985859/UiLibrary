package com.mylibrary.api.utils;

/**
 * Created by admin on 2017/6/20.
 */

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * 功    能：网络请求大量图片
 * 参    数：
 * 返回值：无
 **/
public class ImageDownloadTask extends AsyncTask<Object, Object, Bitmap> {
    private ImageView imageView = null;

    private boolean isBitmapWH = false;

    public void setBitmapWH(boolean bitmapWH) {
        isBitmapWH = bitmapWH;
    }

    /***
     * 这里获取到手机的分辨率大小
     * */
    public void setDisplayWidth(int width) {
        _displaywidth = width;
    }

    public int getDisplayWidth() {
        return _displaywidth;
    }

    public void setDisplayHeight(int height) {
        _displayheight = height;
    }

    public int getDisplayHeight() {
        return _displayheight;
    }

    public int getDisplayPixels() {
        return _displaypixels;
    }

    private int _displaywidth = 480;
    private int _displayheight = 800;
    private int _displaypixels = _displaywidth * _displayheight;


    @Override
    protected Bitmap doInBackground(Object... params) {
        // TODO Auto-generated method stub
        Bitmap bmp = null;
        imageView = (ImageView) params[1];
        try {
            String url = (String) params[0];
            bmp = getBitmap(url, _displaypixels);
        } catch (Exception e) {

            return null;
        }
        return bmp;
    }

    protected void onPostExecute(Bitmap result) {
        if (imageView != null && result != null) {
            imageView.setImageBitmap(result);
            if (null != result && result.isRecycled() == false)
                System.gc();
        }
    }

    /**
     * 通过URL获得网上图片。如:http://www.xxxxxx.com/xx.jpg
     */
    public Bitmap getBitmap(String url, int displaypixels) throws MalformedURLException, IOException {
        Bitmap bmp = null;
        BitmapFactory.Options opts = new BitmapFactory.Options();
        InputStream stream = new URL(url).openStream();
        byte[] bytes = getBytes(stream);
        //这3句是处理图片溢出的begin( 如果不需要处理溢出直接 opts.inSampleSize=1;)
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
        opts.inSampleSize = computeSampleSize(opts, -1, displaypixels);
        //end
        opts.inJustDecodeBounds = false;
        bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
        return bmp;
    }

    /**
     * 数据流转成btyle[]数组
     */
    private byte[] getBytes(InputStream is) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] b = new byte[2048];
        int len = 0;
        try {
            while ((len = is.read(b, 0, 2048)) != -1) {
                baos.write(b, 0, len);
                baos.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] bytes = baos.toByteArray();
        return bytes;
    }

    /****
     *    处理图片bitmap size exceeds VM budget （Out Of Memory 内存溢出）
     */
    private int computeSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength,
                maxNumOfPixels);

        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }
        return roundedSize;
    }

    private int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;
        if (isBitmapWH) {
            if (imageView != null) {
                ViewGroup.LayoutParams params = imageView.getLayoutParams();
                if (params != null) {
                    params.height = params.width * options.outHeight / options.outWidth;
                }
            }
        }
        int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
                .sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(
                Math.floor(w / minSideLength), Math.floor(h / minSideLength));

        if (upperBound < lowerBound) {
            return lowerBound;
        }

        if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }
}