package com.loserskater.appsystemizer.adapters;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.l4digital.fastscroll.FastScroller;
import com.loserskater.appsystemizer.R;
import com.loserskater.appsystemizer.objects.Package;
import com.loserskater.appsystemizer.utils.IconLoaderTask;
import com.loserskater.appsystemizer.utils.Utils;

import java.util.ArrayList;

public class PackageAdapter extends RecyclerView.Adapter<PackageAdapter.ViewHolder> implements FastScroller.SectionIndexer {

    private Context mContext;
    private ArrayList<Package> mDataSet;

    public PackageAdapter(Context context, ArrayList<Package> list) {
        mContext = context;
        mDataSet = list;
    }

    @Override
    public String getSectionText(int position) {
        return mDataSet.get(position).getLabel().substring(0, 1).toUpperCase();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView mTextViewLabel;
        ImageView mImageViewIcon;
        CheckBox mCheckBox;

        ViewHolder(View v) {
            super(v);
            mTextViewLabel = (TextView) v.findViewById(R.id.package_label);
            mImageViewIcon = (ImageView) v.findViewById(R.id.package_icon);
            mCheckBox = (CheckBox) v.findViewById(R.id.package_enabled_checkbox);
        }

    }

    @Override
    public PackageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.item_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final Package mPackage = mDataSet.get(position);
        String label = mPackage.getLabel();
        holder.mTextViewLabel.setText(label);
        new IconLoaderTask(mContext, holder.mImageViewIcon).execute(mPackage);
        holder.mCheckBox.setOnCheckedChangeListener(null);
        holder.mCheckBox.setChecked(mPackage.isEnabled());
        holder.mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    mPackage.setEnabled(true);
                    Utils.addApp(mPackage);
                } else {
                    // When removing a package we need the original package object so set enabled after removing.
                    Utils.removeApp(mPackage);
                    mPackage.setEnabled(false);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

}