package com.loserskater.appsystemizer;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.l4digital.fastscroll.FastScrollRecyclerView;
import com.loserskater.appsystemizer.adapters.PackageAdapter;
import com.loserskater.appsystemizer.utils.AppsManager;
import com.loserskater.appsystemizer.utils.Utils;

public class MainActivity extends AppCompatActivity {

    private FastScrollRecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRecyclerView = (FastScrollRecyclerView) findViewById(R.id.my_recycler_view);

        mRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        new setAdapter().execute();
    }

    @Override
    public void onBackPressed() {
        if(Utils.isAddedOrRemoved()){
            Utils utils = new Utils(this);
            new Utils.writeConfList(this).execute(utils.getAddedApps());
            displayDialog();
        } else {
            finish();
        }
    }

    private void displayDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                new Utils.runBackgroundCommand().execute(Utils.COMMAND_RUN_SCRIPT + "; " + Utils.COMMAND_REBOOT);
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                new Utils.runBackgroundCommand().execute(Utils.COMMAND_RUN_SCRIPT);
                finish();            }
        });

        builder.setMessage(R.string.reboot_desc)
                .setTitle(R.string.reboot);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private class setAdapter extends AsyncTask<Void, Void, Void> {
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(MainActivity.this, getString(R.string.please_wait), getString(R.string.loading_packages), true);
            dialog.setCancelable(false);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mRecyclerView.setAdapter(mAdapter);
            dialog.dismiss();
        }

        @Override
        protected Void doInBackground(Void... params) {
            Utils utils = new Utils(MainActivity.this);
            utils.initiateLists();
            mAdapter = new PackageAdapter(MainActivity.this, new AppsManager(MainActivity.this).getPackages());
            return null;
        }
    }

}
