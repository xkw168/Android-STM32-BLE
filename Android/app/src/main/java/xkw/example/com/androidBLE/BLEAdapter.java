package xkw.example.com.androidBLE;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;

/**
 * @author xkw
 * @date 12/24/2018
 * @file Bluetooth_serial_assistant
 * @description
 */
class BLEAdapter extends SectionedRecyclerViewAdapter {
    private ArrayList<BLEDevice> mDevice;
    private BLEAdapter mAdapter;

    private OnItemClickListener mItemClickListener;

    public interface OnItemClickListener {
        /**
         * item in the recycler view is clicked
         * @param position the position of clicked item
         */
        void onClick(int position);
    }

    public void setClickListener(OnItemClickListener itemClickListener) {
        mItemClickListener = itemClickListener;
    }

    BLEAdapter(){
        super();
        mDevice = new ArrayList<BLEDevice>();
        mAdapter = this;
    }

    BLEAdapter(ArrayList<BLEDevice> devices){
        mDevice = devices;
        mAdapter = this;
    }

    void addDevice(BLEDevice device){
        if (!mDevice.contains(device)){
            mDevice.add(device);
        }
    }

    BLEDevice getDevice(int index){
        return mDevice.get(index);
    }

    public void removeDevice(int index){
        mDevice.remove(index);
    }

    void clearAll(){
        mDevice.clear();
    }


    void updateSections(){
        mAdapter.removeAllSections();
        List<BLEDevice> usefulDevice = new LinkedList<>();
        List<BLEDevice> uselessDevice = new LinkedList<>();
        for (BLEDevice device : mDevice){
            if (device.getDeviceName() == null || device.getDeviceName().contains("abeacon")){
                uselessDevice.add(device);
            }else {
                usefulDevice.add(device);
            }
        }
        if (!usefulDevice.isEmpty()){
            mAdapter.addSection(new ExpandableDeviceSection("常用设备", usefulDevice));
        }
        if (!uselessDevice.isEmpty()){
            mAdapter.addSection(new ExpandableDeviceSection("不常用设备", uselessDevice));
        }
    }

    public class ExpandableDeviceSection extends StatelessSection {

        String title;
        List<BLEDevice> devices;
        boolean expanded = true;

        private ExpandableDeviceSection(String title, List<BLEDevice> devices){
            super(SectionParameters.builder()
                    .itemResourceId(R.layout.listitem_device)
                    .headerResourceId(R.layout.listitem_device_header)
                    .build());

            this.title = title;
            this.devices = devices;
        }

        @Override
        public int getContentItemsTotal() {
            return expanded ? devices.size() : 0;
        }

        @Override
        public RecyclerView.ViewHolder getItemViewHolder(View view) {
            return new MyItemHolder(view);
        }

        @Override
        public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
            MyItemHolder itemHolder = (MyItemHolder)holder;
            itemHolder.mDevice.setOnClickListener(view -> {
                //notify the task interface controller
                if (mItemClickListener != null){
                    mItemClickListener.onClick(position);
                }
            });
            BLEDevice device = devices.get(position);
            if (device.getDeviceName() == null){
                itemHolder.deviceName.setText(device.getDeviceAddress());
                itemHolder.deviceAddress.setVisibility(View.GONE);
            }else {
                itemHolder.deviceName.setText(device.getDeviceName());
                itemHolder.deviceAddress.setText(device.getDeviceAddress());
            }
        }

        @Override
        public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
            return new MyHeadHolder(view);
        }

        @Override
        public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
            MyHeadHolder headerHolder = (MyHeadHolder) holder;

            headerHolder.deviceCategory.setText(title);

            headerHolder.expand.setOnClickListener(view -> {
                headerHolder.expand.setRotation(expanded ? 0 : 180);
                expanded = !expanded;
                mAdapter.notifyDataSetChanged();
            });
        }
    }

    public static class MyHeadHolder extends RecyclerView.ViewHolder {

        TextView deviceCategory;
        ImageView expand;

        MyHeadHolder(View itemView) {
            super(itemView);
            deviceCategory = itemView.findViewById(R.id.device_category);
            expand = itemView.findViewById(R.id.expand_arrow);
        }

    }

    public static class MyItemHolder extends RecyclerView.ViewHolder {
        LinearLayout mDevice;
        TextView deviceName;
        TextView deviceAddress;
        MyItemHolder(View itemView) {
            super(itemView);
            mDevice = itemView.findViewById(R.id.device_layout);
            deviceName = itemView.findViewById(R.id.device_name);
            deviceAddress = itemView.findViewById(R.id.device_address);
        }

    }
}
