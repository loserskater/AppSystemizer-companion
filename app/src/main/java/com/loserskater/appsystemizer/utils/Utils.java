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

    private static final String MODULE_DIR = "/magisk/AppSystemizer";
    private static final String MODULE_SCRIPT = MODULE_DIR + "/post-fs-data.sh";
    private static final String COMMAND_APP_LIST = "find " + MODULE_DIR + "/system/priv-app -type f";
    public static final String COMMAND_RUN_SCRIPT = "sh " + MODULE_SCRIPT;
    public static final String COMMAND_REBOOT = "reboot";

    private static ArrayList<Package> addedApps;
    private static boolean addedOrRemoved = false;
    private Context mContext;

    public Utils(Context context) {
        mContext = context;
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
                if (mPackage.getPackageName().contains(systemApp.getPackageName()) || systemApp.getPackageName().contains(mPackage.getPackageName())){
                    finalList.add(new Package(systemApp.getLabel(), systemApp.getPackageName(), 1));
                }
            }
        }
        return finalList;
    }

    private ArrayList<Package> convertToPackageObject(List<String> list) {
        ArrayList<Package> newList = new ArrayList<>();
        for (String item : list) {
            String[] filename = item.split("/");
            newList.add(new Package(null, filename[filename.length - 1], 1));
        }
        return newList;
    }

    public static boolean isAddedOrRemoved(){
        return addedOrRemoved;
    }

    public static void addApp(Package aPackage) {
        addedOrRemoved = true;
        addedApps.add(aPackage);
    }

    public static void removeApp(Package aPackage) {
        addedOrRemoved = true;
        addedApps.remove(aPackage);
    }

    public static class runBackgroundCommand extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            Log.d("SYSTEMIZER", "running: " + params[0]);
            Shell.SU.run(params);
            return null;
        }
    }

    public static class writeConfList extends AsyncTask<ArrayList<Package>, Void, Void>{

        private Context context;

        public writeConfList(Context context){
            this.context = context;
        }

        @Override
        protected Void doInBackground(ArrayList<Package>... arrayLists) {
            Log.d("SYSTEMIZER", "clearing and writing file");
            StringBuilder sb = new StringBuilder();
            ArrayList<Package> packages = arrayLists[0];
            for (Package pkg : packages){
                sb.append(pkg.getPackageName());
                sb.append("\n");
            }
            try {
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("appslist.conf", Context.MODE_PRIVATE));
                outputStreamWriter.write(sb.toString());
                outputStreamWriter.close();
            }
            catch (IOException e) {
                Log.e("Exception", "File write failed: " + e.toString());
            }
            return null;
        }
    }
}
