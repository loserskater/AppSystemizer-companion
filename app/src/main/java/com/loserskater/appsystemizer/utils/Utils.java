package com.loserskater.appsystemizer.utils;


import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.loserskater.appsystemizer.objects.Package;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import eu.chainfire.libsuperuser.Shell;

public class Utils {

    public static final String LOG_TAG = "AppSystemizer-companion";
    private static final String MODULE_DIR = "/magisk/AppSystemizer";
    private static final String MODULE_TMP_DIR = "/dev/magisk_merge/AppSystemizer";
    private static final String MODULE_SCRIPT = MODULE_DIR + "/post-fs-data.sh";
    public static final String INVALID_PACKAGE = "android";
    public static final String INVALID_LABEL = "AndroidSystem";
    public static final String COMMAND_RUN_SCRIPT = "sh " + MODULE_SCRIPT + " update";
    public static final String COMMAND_REBOOT = "reboot";
    private static final String[] defaultList = {
            "com.google.android.apps.nexuslauncher,NexusLauncherPrebuilt",
            "com.google.android.apps.pixelclauncher,PixelCLauncherPrebuilt",
            "com.actionlauncher.playstore,ActionLauncher",
    };

    public static ArrayList<Package> includedApps = new ArrayList<>();

    private static ArrayList<Package> addedApps;
    private static boolean addedOrRemoved = false;
    private Context mContext;

    public Utils(Context context) {
        mContext = context;
    }

    private static String commandAppList(String dir) {
        return "find " + dir + "/system/priv-app -type f";
    }

    private void buildIncludedApps() {
        for (String pkg : defaultList) {
            String[] pkgIDAndLabel = pkg.split(",");
            includedApps.add(new Package(pkgIDAndLabel[0], pkgIDAndLabel[1], 0));
        }
    }

    private void setAddedApps(ArrayList<Package> addedApps) {
        Utils.addedApps = addedApps;
    }

    public ArrayList<Package> getAddedApps() {
        if (addedApps == null) {
            generateAddedApps();
        }
        return addedApps;
    }

    public void initiateLists() {
        buildIncludedApps();
        generateAddedApps();
    }

    private void generateAddedApps() {
        if (Shell.SU.available()) {
            boolean tmpDirExists = Shell.SU.run("[ -e \"" + MODULE_TMP_DIR + "\" ] && echo true || echo false").get(0).trim().matches("true");
            String modDir = MODULE_DIR;
            if (tmpDirExists) {
                Log.d(Utils.LOG_TAG, "Using magisk_merge");
                modDir = MODULE_TMP_DIR;
            }
            List<String> list = Shell.SU.run(commandAppList(modDir));
            setAddedApps(loadSystemizedApps(convertToPackageObject(list)));
        } else {
            setAddedApps(new ArrayList<Package>());
        }
    }

    private ArrayList<Package> loadSystemizedApps(ArrayList<Package> packages) {
        AppsManager appsManager = new AppsManager(mContext);
        ArrayList<Package> finalList = new ArrayList<>();
        for (Package mPackage : packages) {
            for (Package systemApp : appsManager.getInstalledPackages(true)) {
                if (mPackage.getPackageName().contains(systemApp.getPackageName()) || systemApp.getPackageName().contains(mPackage.getPackageName())) {
                    finalList.add(new Package(systemApp.getPackageName(), systemApp.getLabel(), 1));
                }
            }
        }
        return finalList;
    }

    private ArrayList<Package> convertToPackageObject(List<String> list) {
        ArrayList<Package> newList = new ArrayList<>();
        if (list != null) {
            if (!list.isEmpty()) {
                for (String item : list) {
                    String[] filename = item.split("/");
                    newList.add(new Package(filename[filename.length - 1], null, 1));
                }
            }
        }
        return newList;
    }

    public static boolean isAddedOrRemoved() {
        return addedOrRemoved;
    }

    public static void addApp(Package aPackage) {
        Log.d(Utils.LOG_TAG, "Add app: " + aPackage.getLabel());
        addedOrRemoved = true;
        addedApps.add(aPackage);
    }

    public static void removeApp(Package aPackage) {
        Log.d(Utils.LOG_TAG, "Remove app: " + aPackage.getLabel());
        addedOrRemoved = true;
        addedApps.remove(aPackage);
    }

    public static class runBackgroundCommand extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            Shell.SU.run(params);
            return null;
        }
    }
}
