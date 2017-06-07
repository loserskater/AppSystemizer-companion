package com.loserskater.appsystemizer;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.jorgecastilloprz.FABProgressCircle;
import com.github.jorgecastilloprz.listeners.FABProgressListener;
import com.l4digital.fastscroll.FastScrollRecyclerView;
import com.loserskater.appsystemizer.adapters.PackageAdapter;
import com.loserskater.appsystemizer.objects.Package;
import com.loserskater.appsystemizer.utils.AppsManager;
import com.loserskater.appsystemizer.utils.Utils;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import eu.chainfire.libsuperuser.Shell;

public class MainActivity extends AppCompatActivity implements FABProgressListener {

    private FastScrollRecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private FloatingActionButton fab;
    private FABProgressCircle fabProgressCircle;
    private boolean isTaskRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fabProgressCircle = (FABProgressCircle) findViewById(R.id.fabProgressCircle);
        fabProgressCircle.attachListener(this);
        fabProgressCircle.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                ImageView imgView = (ImageView) v.findViewById(R.id.completeFabIcon);
                if ((imgView != null) && (imgView.getScaleType() != ImageView.ScaleType.CENTER_INSIDE)) {
                    imgView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                }
            }
        });

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isTaskRunning) {
                    isTaskRunning = true;
                    fabProgressCircle.show();
                    Utils utils = new Utils(MainActivity.this);
                    new writeConfList(MainActivity.this).execute(utils.getAddedApps());
                }
            }
        });
        fab.hide();

        mRecyclerView = (FastScrollRecyclerView) findViewById(R.id.my_recycler_view);

        mRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        new setAdapter().execute();
    }

    public void showFab(boolean show){
        if (show) {
            fabProgressCircle.hide();
            fab.show();
        } else {
            fab.hide();
        }
    }


//    private void writeFileAndRunScript(){
//
//        new writeConfList(this).execute(utils.getAddedApps());
//        new Utils.runBackgroundCommand().execute(Utils.COMMAND_RUN_SCRIPT);
//    }
//
//    @Override
//    public void onBackPressed() {
//        if(Utils.isAddedOrRemoved()){
//            displayDialog();
//        } else {
//            finish();
//        }
//    }
//
//    private void displayDialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int id) {
//                writeFileAndRunScript();
//                new Utils.runBackgroundCommand().execute(Utils.COMMAND_REBOOT);
//            }
//        });
//        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int id) {
//                finish();            }
//        });
//
//        builder.setMessage(R.string.reboot_desc)
//                .setTitle(R.string.reboot);
//        AlertDialog dialog = builder.create();
//        dialog.show();
//    }

    private class setAdapter extends AsyncTask<Void, Void, Boolean> {
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(MainActivity.this, getString(R.string.please_wait), getString(R.string.loading_packages), true);
            dialog.setCancelable(false);
        }

        @Override
        protected void onPostExecute(Boolean isRoot) {
            if (!isRoot){
                Toast.makeText(MainActivity.this, getString(R.string.no_root), Toast.LENGTH_SHORT).show();
            }
            mRecyclerView.setAdapter(mAdapter);
            dialog.dismiss();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if (!Shell.SU.available()){
                mAdapter = new PackageAdapter(MainActivity.this, new ArrayList<Package>());
                return false;
            }
            Utils utils = new Utils(MainActivity.this);
            utils.initiateLists();
            mAdapter = new PackageAdapter(MainActivity.this, new AppsManager(MainActivity.this).getPackages());
            return true;
        }
    }

    private class writeConfList extends AsyncTask<ArrayList<Package>, Void, Void> {

        private Context context;

        private writeConfList(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(ArrayList<Package>... arrayLists) {
            StringBuilder sb = new StringBuilder();
            ArrayList<Package> packages = arrayLists[0];
            // For some reason android,AndroidSystem is getting added to the list. We don't want that.
            for (Package pkg : packages) {
                if (pkg.getPackageName().matches(Utils.INVALID_PACKAGE) || pkg.getLabel().matches(Utils.INVALID_LABEL)) {
                    continue;
                }
                for (Package includedPkg : Utils.includedApps) {
                    if (pkg.getPackageName().matches(includedPkg.getPackageName())) {
                        pkg.setLabel(includedPkg.getLabel());
                    }
                }
                sb.append(pkg.getPackageName());
                sb.append(",");
                sb.append(pkg.getLabel());
                sb.append("\n");
            }
            try {
                Log.d(Utils.LOG_TAG, "Write conf: " + sb.toString());
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("appslist.conf", Context.MODE_PRIVATE));
                outputStreamWriter.write(sb.toString());
                outputStreamWriter.close();
            } catch (IOException e) {
                Log.e("Exception", "File write failed: " + e.toString());
            }
            Shell.SU.run(Utils.COMMAND_RUN_SCRIPT);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            fabProgressCircle.beginFinalAnimation();
            isTaskRunning = false;
        }
    }
        @Override public void onFABProgressAnimationEnd() {
            Snackbar.make(fabProgressCircle, R.string.write_complete, Snackbar.LENGTH_LONG)
                    .setAction("Reboot", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new Utils.runBackgroundCommand().execute(Utils.COMMAND_REBOOT);
                        }
                    })
                    .show();
            showFab(false);
        }
}
