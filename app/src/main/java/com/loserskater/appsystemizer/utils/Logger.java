package com.loserskater.appsystemizer.utils;

import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import eu.chainfire.libsuperuser.Shell;

/**
 * Created by Jason on 4/16/2017.
 */

public class Logger {

    private static final String LOG_FILE = Utils.MODULE_DIR + "/systemizer-companion.log";



    public static void log(String tag, String input){
        if (!Shell.SU.available()) {
            return;
        }

        String sb = tag +
                ": " +
                input;
        new Utils.runBackgroundCommand().execute("echo \"" + sb + "\" >> " + LOG_FILE);
    }

    public static void clearLog(){
        new Utils.runBackgroundCommand().execute("rm " + LOG_FILE);
    }
}
