package com.utracx.view.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerTextView;
import com.utracx.R;
import com.utracx.api.model.rest.device_data.DeviceData;
import com.utracx.api.model.rest.vehicle_list.LastUpdatedData;
import com.utracx.background.AddressTask;
import com.utracx.util.helper.ResourceHelper;
import com.utracx.view.adapter.viewholder.AlertViewHolder;

import java.util.ArrayList;
import java.util.List;

import static com.utracx.util.helper.MapHelper.getEpochTime;

public class AlertAdapter extends RecyclerView.Adapter<AlertViewHolder> {
    private Context context;
    private AddressTask.GeoAddressUpdateCallback callback;
    private List<DeviceData> alertsList;
    private boolean isGeoCoder;

    int itemCount;

    public void AlertAdapterValues(@NonNull Context context,
                        @NonNull AddressTask.GeoAddressUpdateCallback callback, boolean isGeoCoder) {
        this.context = context;
        this.callback = callback;
        this.isGeoCoder = isGeoCoder;
        this.alertsList = new ArrayList<>();
    }

    @NonNull
    @Override
    public AlertViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AlertViewHolder(
                LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.recycler_item_alert, parent, false)
        );
    }

    private static final DiffUtil.ItemCallback<DeviceData> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<DeviceData>() {
                @Override
                public boolean areItemsTheSame(@NonNull DeviceData oldItem, @NonNull DeviceData newItem) {
                    return oldItem.getId() == newItem.getId();
                }

                @SuppressLint("DiffUtilEquals")
                @Override
                public boolean areContentsTheSame(@NonNull DeviceData oldItem, @NonNull DeviceData newItem) {
                    return oldItem.getId() == newItem.getId();
                }
            };
    @Override
    public void onBindViewHolder(@NonNull AlertViewHolder alertViewHolder, int position) {
        DeviceData currentAlert = friendPosition(position);

        if (currentAlert != null && currentAlert.getD() != null) {
            LastUpdatedData lastUpdatedData = currentAlert.getD();

            if (!TextUtils.isEmpty(currentAlert.getD().getPacketType())) {

                String packetType = lastUpdatedData.getPacketType().toLowerCase();

                alertViewHolder.alertTypeImageView.setBackgroundResource(
                        ResourceHelper.getAlertBackground(context, packetType)
                );

                alertViewHolder.alertTypeImageView.setImageDrawable(
                        ResourceHelper.getAlertDrawable(context, packetType)
                );

                alertViewHolder.textViewAlertType.setText(lastUpdatedData.getPacketType());

                alertViewHolder.textViewVehicleAddress.setText(currentAlert.getD().getAddress());
                handleAddressTextAnimation(alertViewHolder, currentAlert.getD().getAddress());

                if (lastUpdatedData.getGnssFix() != null
                        && lastUpdatedData.getGnssFix() != 0) {
                    if (lastUpdatedData.getAddress() != null
                            && !lastUpdatedData.getAddress().isEmpty()) {
                        setupAddress(alertViewHolder, lastUpdatedData.getAddress());
                    } else if (lastUpdatedData.getLatitude() != null
                            && lastUpdatedData.getLongitude() != null
                            && lastUpdatedData.getLatitude() != 0.0
                            && lastUpdatedData.getLongitude() != 0.0) {
                        setupAddress(alertViewHolder, "");
                        //start a new thread for OpenStreetMapsAPI call
                        new AddressTask(isGeoCoder, lastUpdatedData, context, callback, null).run();
                    } else {
                        setupAddress(alertViewHolder, "");
                    }
                }

            }
            
            if (currentAlert.getD() != null && currentAlert.getD().getSourceDate() != null) {
                alertViewHolder.textViewAlertDate.setText(
                        getEpochTime(currentAlert.getD().getSourceDate(), "hh:mm aa")
                );
            }
        }
    }

    public DeviceData friendPosition(int pos){
        return alertsList.get(pos);
    }
    private String formatAddress(String address) {

        if (address.contains("+")) {
            address = address.substring(address.indexOf(",")+1);
        }

        return address;

    }


    private void setupAddress(@NonNull AlertViewHolder holder, @Nullable String address) {
        holder.textViewVehicleAddress.setText(formatAddress(address));
        handleAddressTextAnimation(holder, address);
    }

    private void handleAddressTextAnimation(AlertViewHolder viewHolder, String address) {
        if (address != null && !address.isEmpty()) {
            if (viewHolder.shimmer.isAnimating()) {
                viewHolder.shimmer.cancel();
            }
        } else {
            viewHolder.textViewVehicleAddress.
                    setText(viewHolder.textViewVehicleAddress.getContext().getString(R.string.location_fetching));
            viewHolder.shimmer.start(viewHolder.textViewVehicleAddress);
        }
    }

    @Override
    public int getItemCount() {
        if(itemCount != 0 && itemCount +20< alertsList.size())
            return itemCount+20;
        else if(alertsList.size() <20)
            alertsList.size();
        else return 20;


        return (alertsList == null || alertsList.isEmpty()) ? 0 : alertsList.size();
    }

    public void updateAlertsList(@NonNull List<DeviceData> alertList) {
        if (alertList.size() > 0) {
            this.alertsList.clear();
            this.alertsList = new ArrayList<>(alertList);
            notifyDataSetChanged();
        }
    }

    public void paginateAlertsList(@NonNull List<DeviceData> alertList) {
        if (alertList.size() > 0) {
            //this.alertsList.clear();
            //this.alertsList = new ArrayList<>(alertList);
            this.alertsList.addAll(alertList);
            notifyDataSetChanged();
        }
    }

    public void setTotalVisibleCount(int itemCount) {
        this.itemCount = itemCount;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView alertTypeImageView;
        public TextView textViewAlertDate;
        public TextView textViewAlertType;
        public ShimmerTextView textViewVehicleAddress;
        public Shimmer shimmer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            alertTypeImageView = itemView.findViewById(R.id.alertTypeImageView);
            textViewAlertType = itemView.findViewById(R.id.textViewAlertType);
            textViewAlertDate = itemView.findViewById(R.id.textViewAlertDate);
            textViewVehicleAddress = itemView.findViewById(R.id.textViewVehicleAddress);
            shimmer = new Shimmer();
        }
    }
}