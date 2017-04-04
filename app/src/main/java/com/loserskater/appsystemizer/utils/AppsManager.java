package com.loserskater.appsystemizer.utils;


import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.loserskater.appsystemizer.R;
import com.loserskater.appsystemizer.objects.Package;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AppsManager {
    private Context mContext;

    private ArrayList<Package> installedPackages;
    private ArrayList<Package> systemPackages;

    public AppsManager(Context context) {
        mContext = context;
    }

    public ArrayList<Package> getPackages(){
        ArrayList<Package> fullList = getInstalledPackages(false);
        fullList.addAll(Utils.addedApps);
        Collections.sort(fullList, new Package.PackageNameComparator());
       return fullList;
    }

    public ArrayList<Package> getInstalledPackages(boolean system) {
        if (installedPackages != null && !system){
            return installedPackages;
        }
        if (systemPackages !=null && system){
            return systemPackages;
        }
        final PackageManager pm = mContext.getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        ArrayList<Package> packageNames = new ArrayList<>();
        for (ApplicationInfo packageInfo : packages) {
            if (system && isSystemPackage(packageInfo)) {
                String packageName = packageInfo.packageName;
                String packageLabel = packageInfo.loadLabel(pm).toString();
                packageNames.add(new Package(packageLabel, packageName, 0));
                installedPackages = packageNames;
            } else if (!isSystemPackage(packageInfo)) {
                String packageName = packageInfo.packageName;
                String packageLabel = packageInfo.loadLabel(pm).toString();
                packageNames.add(new Package(packageLabel, packageName, 0));
                systemPackages = packageNames;
            }
        }
        return packageNames;
    }

    private boolean isSystemPackage(ApplicationInfo applicationInfo) {
        return ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
    }

    public  String getApplicationPackageNameByLabel(String label){
        for (Package mPackage : getInstalledPackages(true)){
            Log.d("TEMP", "installed: " + mPackage.getLabel());
            Log.d("TEMP", "added label: " + label);
            if (mPackage.getLabel().contains(label)){
                Log.d("TEMP", "got " + mPackage.getPackageName());
                return mPackage.getPackageName();
            }
        }
        return null;
    }
}

