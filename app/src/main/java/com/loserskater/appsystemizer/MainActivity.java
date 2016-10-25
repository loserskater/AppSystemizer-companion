package com.loserskater.appsystemizer;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.l4digital.fastscroll.FastScrollRecyclerView;

import com.loserskater.appsystemizer.adapters.PackageAdapter;
import com.loserskater.appsystemizer.utils.AppsManager;

public class MainActivity extends AppCompatActivity {

    private FastScrollRecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRecyclerView = (FastScrollRecyclerView) findViewById(R.id.my_recycler_view);

        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        new setAdapter().execute();
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
//            Utils.initiateLists();
            mAdapter = new PackageAdapter(MainActivity.this, new AppsManager(MainActivity.this).getInstalledPackages());
            return null;
        }
    }

}
