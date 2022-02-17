package com.utracx.view.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.utracx.R;
import com.utracx.api.model.rest.vehicle_list.VehicleInfo;
import com.utracx.util.helper.NavigationHelper;
import com.utracx.view.activity.AlertListActivity;
import com.utracx.view.adapter.viewholder.AlertVehicleViewHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.utracx.api.request.ApiUtils.cancelAllSOSRequest;
import static com.utracx.util.helper.ResourceHelper.getVehicleIconResource;
import static com.utracx.view.activity.HomeActivity.BUNDLE_KEY_VEHICLE_DETAIL;

public class AlertVehicleAdapter extends RecyclerView.Adapter<AlertVehicleViewHolder> implements AlertVehicleViewHolder.ParentVehicleClickListener {

    private Context context;
    private List<VehicleInfo> alertsVehicleList;

    public AlertVehicleAdapter(@NonNull Context context) {
        this.context = context;
        this.alertsVehicleList = new ArrayList<>();
    }

    @NonNull
    @Override
    public AlertVehicleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AlertVehicleViewHolder(
                LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.recycler_item_alert_vehicle, parent, false),
                this
        );
    }

    @Override
    public void onBindViewHolder(@NonNull AlertVehicleViewHolder parentViewHolder, int position) {
        VehicleInfo currentVehicle = alertsVehicleList.get(position);
        parentViewHolder.imageViewVehicleType.setImageDrawable(
                getVehicleIconResource(context, currentVehicle.getVehicleType())
        );

        parentViewHolder.textViewVehicleRegNo.setText(currentVehicle.getVehicleRegistration());

        setupCountTextView(parentViewHolder.textViewAlerts01, currentVehicle.getMainPowerRemovalAlertCount(), 1000);

        setupCountTextView(parentViewHolder.textViewAlerts02, currentVehicle.getWireCutAlertCount(), 1000);

        setupCountTextView(parentViewHolder.textViewAlerts03, currentVehicle.getEmergencyAlertCount(), 1000);

        setupCountTextView(parentViewHolder.textViewAlerts04, currentVehicle.getOverspeedAlertCount(), 1000);
        setupCountTextView(parentViewHolder.textViewAlerts05, currentVehicle.getOthersAlertCount(), 1000);

        setupCountTextView(
                parentViewHolder.textViewAlertTotal,
                currentVehicle.getAlertCount(),
                100
        );

        parentViewHolder.setTag(currentVehicle);
    }

    private void setupCountTextView(TextView textView, int count, int threshold) {
        if (count > 0) {
            textView.setVisibility(VISIBLE);
            if (count < threshold) {
                textView.setText(String.valueOf(count));
            } else {
                textView.setText(threshold == 1000 ? "999+" : "99+");
            }
        } else {
            textView.setVisibility(GONE);
            textView.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return (alertsVehicleList == null || alertsVehicleList.isEmpty()) ? 0 : alertsVehicleList.size();
    }

    @Override
    public void onClick(View rowView) {
        VehicleInfo clickedRow = (VehicleInfo) rowView.getTag();
        if (clickedRow != null && clickedRow.getLastUpdatedData() != null
                && clickedRow.getLastUpdatedData().getSerialNumber() != null
                && !clickedRow.getLastUpdatedData().getSerialNumber().isEmpty()) {

            cancelAllSOSRequest();

            Bundle bundle = new Bundle();
            bundle.putParcelable(BUNDLE_KEY_VEHICLE_DETAIL, clickedRow);
            NavigationHelper.navigateToNewActivity((Activity) context, AlertListActivity.class, bundle);
        }
    }

    public void setVehicleList(@NonNull List<VehicleInfo> alertsVehicleList) {
        this.alertsVehicleList.clear();
        this.alertsVehicleList.addAll(alertsVehicleList);
        notifyDataSetChanged();
    }
}

