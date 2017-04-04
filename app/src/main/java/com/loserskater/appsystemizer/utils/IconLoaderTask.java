package com.loserskater.appsystemizer.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;

import com.loserskater.appsystemizer.R;
import com.loserskater.appsystemizer.objects.Package;

import java.lang.ref.WeakReference;


public class IconLoaderTask extends AsyncTask<Package, Void, Drawable> {
    private final WeakReference<ImageView> imageViewReference;


    private Context mContext;
    private PackageManager packageManager;

    public IconLoaderTask(Context context, PackageManager pm, ImageView imageView) {
        mContext = context;
        // Use a WeakReference to ensure the ImageView can be garbage collected
        imageViewReference = new WeakReference<ImageView>(imageView);
        this.packageManager = pm;
    }

    // Decode image in background.
    @Override
    protected Drawable doInBackground(Package... params) {
        PackageManager packageManager = mContext.getPackageManager();
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = packageManager.getApplicationInfo(params[0].getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return applicationInfo.loadIcon(packageManager);
    }

    // Once complete, see if ImageView is still around and set bitmap.
    @Override
    protected void onPostExecute(Drawable drawable) {
            final ImageView imageView = imageViewReference.get();
            if (imageView != null) {

                if (drawable != null) {
                    imageView.setImageDrawable(drawable);
                }
            }
    }
}