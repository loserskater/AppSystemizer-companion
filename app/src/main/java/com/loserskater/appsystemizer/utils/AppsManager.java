package com.loserskater.appsystemizer.utils;


import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

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
        Utils utils = new Utils(mContext);
        ArrayList<Package> fullList = getInstalledPackages(false);
        for (Package addedPkg : utils.getAddedApps()){
            for (Package pkg : fullList){
                if (pkg.getLabel().matches(addedPkg.getLabel())){
                    fullList.remove(pkg);
                    break;
                }
            }
            fullList.add(addedPkg);
        }
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
                packageNames.add(new Package(packageName, packageLabel, 0));
                systemPackages = packageNames;
            } else if (!isSystemPackage(packageInfo)) {
                String packageName = packageInfo.packageName;
                String packageLabel = packageInfo.loadLabel(pm).toString();
                packageNames.add(new Package(packageName, packageLabel, 0));
                installedPackages = packageNames;
            }
        }
        return packageNames;
    }

    private boolean isSystemPackage(ApplicationInfo applicationInfo) {
        return ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
    }
}

