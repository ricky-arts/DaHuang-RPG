package com.honghuang.rpg;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.InputStream;

public class AssetLoader {
    /**
     * Android 真机标准加载图片：
     * 1. 优先通过 Resources.getIdentifier 获取 res/drawable-* 资源
     * 2. 次选通过 AssetManager 从 assets/ 目录加载
     */
    public static Bitmap loadDrawableOrAsset(Context context, String baseName) {
        if (context == null || baseName == null) return null;

        // 剔除后缀作为资源名
        String resName = baseName.replace(".png", "").replace(".jpg", "");

        // 1. 通过 R.drawable 反射查找
        try {
            int resId = context.getResources().getIdentifier(resName, "drawable", context.getPackageName());
            if (resId != 0) {
                Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), resId);
                if (bmp != null) return bmp;
            }
        } catch (Exception ignored) {}

        // 2. 通过 assets 目录流式加载
        try {
            InputStream is = context.getAssets().open(baseName);
            Bitmap bmp = BitmapFactory.decodeStream(is);
            is.close();
            if (bmp != null) return bmp;
        } catch (Exception ignored) {}

        // 3. 尝试在 assets/ 里寻找无后缀文件
        try {
            InputStream is = context.getAssets().open(resName + ".png");
            Bitmap bmp = BitmapFactory.decodeStream(is);
            is.close();
            if (bmp != null) return bmp;
        } catch (Exception ignored) {}

        return null;
    }
}
