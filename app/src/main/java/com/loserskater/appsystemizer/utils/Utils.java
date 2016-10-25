package com.loserskater.appsystemizer.utils;


import android.os.AsyncTask;
import android.util.Log;

import com.loserskater.appsystemizer.objects.Package;

import java.util.ArrayList;
import java.util.List;

import eu.chainfire.libsuperuser.Shell;

public class Utils {

    private static final String MODULE_LOCATION = "/magisk/AppSystemizer";
    private static final String SCRIPT_LOCATION = MODULE_LOCATION + "/common/post-fs-data.sh";
    private static final String APP_LIST_LOCATION = MODULE_LOCATION + "/extras/appslist.conf";
    private static final String COMMAND_APP_LIST = "cat " + APP_LIST_LOCATION;

    private static final String COMMAND_ADD_APP(String packageName, String label) {
        return String.format("grep -q '%s' " + APP_LIST_LOCATION + " && sed -i '/%s/s/0/1/' " + APP_LIST_LOCATION + " || printf \"\n" +
                "\\\"%s,%s,priv-app,1\\\"\" >> " + APP_LIST_LOCATION, packageName, packageName, packageName, label);
    }

    private static final String COMMAND_REMOVE_APP(String packageName) {
        return String.format("sed -i '/%s/s/1/0/' " + APP_LIST_LOCATION, packageName);
    }

    private static ArrayList<Package> addedApps;
    private static boolean isAddedOrRemoved = false;

    public static void setAddedApps(ArrayList<Package> addedApps) {
        Utils.addedApps = addedApps;
    }

    public static ArrayList<Package> getAddedApps() {
        if (addedApps == null) {
            generateAddedApps();
        }
        return addedApps;
    }

    public static void initiateLists() {
        generateAddedApps();
    }

    private static void generateAddedApps() {
        if (Shell.SU.available()) {
            List<String> list = Shell.SU.run(COMMAND_APP_LIST);
            setAddedApps(convertToPackageObject(list));
        } else {
            setAddedApps(new ArrayList<Package>());
        }
    }

    private static ArrayList<Package> convertToPackageObject(List<String> list) {
        ArrayList<Package> newList = new ArrayList<>();
        for (String item : list) {
            String[] itemParts = item.split(",");
            newList.add(new Package(itemParts[1], itemParts[0].replace("\"", ""), Integer.parseInt(itemParts[3].substring(0,1))));
        }
        return newList;
    }

    public static boolean isMatch(Package aPackage) {
        for (Package item : getAddedApps()) {
            if (aPackage.getPackageName().contains(item.getPackageName())
                    || item.getPackageName().contains(aPackage.getPackageName())) {
                return item.isEnabled();
            }
        }
        return false;
    }

    public static void addApp(Package aPackage) {
        isAddedOrRemoved = true;
        new runBackgroudTask().execute(COMMAND_ADD_APP(aPackage.getPackageName(), aPackage.getLabel()));
    }

    public static void removeApp(Package aPackage) {
        isAddedOrRemoved = true;
        new runBackgroudTask().execute(COMMAND_REMOVE_APP(aPackage.getPackageName()));
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
