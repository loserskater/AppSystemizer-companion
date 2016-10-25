package com.loserskater.systemizer.adapters;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.loserskater.systemizer.R;
import com.loserskater.systemizer.objects.Package;
import com.loserskater.systemizer.utils.AppsManager;

import java.util.ArrayList;

public class PackageAdapter extends RecyclerView.Adapter<PackageAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<Package> mDataSet;

    public PackageAdapter(Context context, ArrayList<Package> list) {
        mContext = context;
        mDataSet = list;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView mTextViewLabel;
        ImageView mImageViewIcon;
        CheckBox mCheckBox;

        ViewHolder(View v) {
            super(v);
            mTextViewLabel = (TextView) v.findViewById(R.id.package_label);
            mImageViewIcon = (ImageView) v.findViewById(R.id.package_icon);
            mCheckBox = (CheckBox) v.findViewById(R.id.package_enabled_checkbox);
            mCheckBox.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

        }
    }

    @Override
    public PackageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.item_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        AppsManager appsManager = new AppsManager(mContext);

        final String packageName = mDataSet.get(position).getPackageName();
        Drawable icon = appsManager.getAppIconByPackageName(packageName);
        String label = mDataSet.get(position).getLabel();

        holder.mTextViewLabel.setText(label);
        holder.mImageViewIcon.setImageDrawable(icon);
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

}