package com.loserskater.appsystemizer.utils;


import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

import com.loserskater.appsystemizer.R;
import com.loserskater.appsystemizer.objects.Package;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AppsManager {
    private Context mContext;

    public AppsManager(Context context) {
        mContext = context;
    }

    public ArrayList<Package> getInstalledPackages() {
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        );

        List<ResolveInfo> resolveInfoList = mContext.getPackageManager().queryIntentActivities(intent, 0);
        ArrayList<Package> packageNames = new ArrayList<>();

        for (ResolveInfo resolveInfo : resolveInfoList) {
            ActivityInfo activityInfo = resolveInfo.activityInfo;
            if (!isSystemPackage(resolveInfo)) {
                String packageName = activityInfo.applicationInfo.packageName;
                String packageLabel = getApplicationLabelByPackageName(packageName);
                packageNames.add(new Package(packageLabel, packageName));
            }
        }

        Collections.sort(packageNames, new Package.PackageNameComparator());
        return packageNames;

    }

    public boolean isSystemPackage(ResolveInfo resolveInfo) {
        return ((resolveInfo.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
    }

    public Drawable getAppIconByPackageName(String packageName) {
        Drawable icon;
        try {
            icon = mContext.getPackageManager().getApplicationIcon(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            icon = ContextCompat.getDrawable(mContext, R.mipmap.ic_launcher);
        }
        return icon;
    }

    private String getApplicationLabelByPackageName(String packageName) {
        PackageManager packageManager = mContext.getPackageManager();
        ApplicationInfo applicationInfo;
        String label = "Unknown";
        try {
            applicationInfo = packageManager.getApplicationInfo(packageName, 0);
            if (applicationInfo != null) {
                label = (String) packageManager.getApplicationLabel(applicationInfo);
            }

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return label;
    }
}

