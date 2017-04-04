package com.loserskater.appsystemizer.utils;


import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.loserskater.appsystemizer.MainActivity;
import com.loserskater.appsystemizer.objects.Package;

import java.util.ArrayList;
import java.util.List;

import eu.chainfire.libsuperuser.Shell;

public class Utils {

    private static final String MODULE_DIR = "/magisk/AppSystemizer";
    private static final String COMMAND_APP_LIST = "ls " + MODULE_DIR + "/system/priv-app/";
    public static final String COMMAND_REBOOT = "reboot";

    private static  String COMMAND_ADD_APP(String packageName, String label) {
        return String.format("mkdir -p %s && " +
                "cp -f %s %s", getSystemAppLocation(label), getDataAppLocation(packageName), getSystemAppLocation(label));
    }

    private static String COMMAND_REMOVE_APP(String label) {
        return String.format("rm -rf %s", getSystemAppLocation(label));
    }

    public static ArrayList<Package> addedApps;
    public static boolean isAddedOrRemoved = false;
    private Context mContext;

    public Utils(Context context) {
        mContext = context;
    }

    private static String getDataAppLocation(String packageName) {
        return String.format("/data/app/%s-*/base.apk", packageName);
    }

    private static String getSystemAppLocation(String label){
        return String.format("%s/system/priv-app/%s", MODULE_DIR, label);
    }

    public void setAddedApps(ArrayList<Package> addedApps) {
        Utils.addedApps = addedApps;
    }

    public ArrayList<Package> getAddedApps() {
        if (addedApps == null) {
            generateAddedApps();
        }
        return addedApps;
    }

    public void initiateLists() {
        generateAddedApps();
    }

    private void generateAddedApps() {
        if (Shell.SU.available()) {
            List<String> list = Shell.SU.run(COMMAND_APP_LIST);
            setAddedApps(loadSystemizedApps(convertToPackageObject(list)));
        } else {
            setAddedApps(new ArrayList<Package>());
        }
    }

    private ArrayList<Package> loadSystemizedApps(ArrayList<Package> packages) {
        AppsManager appsManager = new AppsManager(mContext);
        ArrayList<Package> finalList = new ArrayList<>();
        for (Package mPackage : packages){
            for (Package systemApp : appsManager.getInstalledPackages(true)){
                if (mPackage.getLabel().contains(systemApp.getLabel()) || systemApp.getLabel().contains(mPackage.getLabel())){
                    finalList.add(new Package(systemApp.getLabel(), systemApp.getPackageName(), 1));
                }
            }
        }
        return finalList;
    }

    private ArrayList<Package> convertToPackageObject(List<String> list) {
        ArrayList<Package> newList = new ArrayList<>();
        for (String item : list) {
            newList.add(new Package(item, null, 1));
        }
        return newList;
    }

    public static void addApp(Package aPackage) {
        isAddedOrRemoved = true;
        new runBackgroudTask().execute(COMMAND_ADD_APP(aPackage.getPackageName(), aPackage.getLabel()));
    }

    public static void removeApp(Package aPackage) {
        isAddedOrRemoved = true;
        new runBackgroudTask().execute(COMMAND_REMOVE_APP(aPackage.getLabel()));
    }

    public static class runBackgroudTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            Log.d("SYSTEMIZER", "running: " + params[0]);
            Shell.SU.run(params);
            return null;
        }
    }
}
