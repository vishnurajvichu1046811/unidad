package com.utracx.view.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.utracx.R;
import com.utracx.api.model.rest.vehicle_list.LastUpdatedData;
import com.utracx.api.model.rest.vehicle_list.VehicleInfo;
import com.utracx.background.AddressTask;
import com.utracx.util.helper.MapHelper;
import com.utracx.util.helper.NavigationHelper;
import com.utracx.view.activity.MapActivity;
import com.utracx.view.adapter.helper.VehicleListFilter;
import com.utracx.view.adapter.viewholder.VehicleViewHolder;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.utracx.api.request.ApiUtils.cancelAllOpenStreetMapsRequest;
import static com.utracx.util.ConstantVariables.THRESHOLD_TIME_FOR_NON_COMM_VEHICLE;
import static com.utracx.util.helper.MathHelper.round;
import static com.utracx.util.helper.ResourceHelper.getGSMStrengthIconResource;
import static com.utracx.util.helper.ResourceHelper.getVehicleIconResource;
import static com.utracx.util.helper.ResourceHelper.getVehicleModeBackground;
import static com.utracx.view.activity.HomeActivity.BUNDLE_KEY_VEHICLE_DETAIL;

public class VehicleAdapter extends RecyclerView.Adapter<VehicleViewHolder>
        implements Filterable {

    private final AddressTask.GeoAddressUpdateCallback callback;
    private final Context context;
    private final VehicleListFilter filter;

    private List<VehicleInfo> vehicleInfoList;

    public VehicleAdapter(Context context,
                          AddressTask.GeoAddressUpdateCallback callback,
                          String vehicleMode) {
        this.context = context;
        this.callback = callback;
        this.filter = new VehicleListFilter(this, vehicleMode);
        this.vehicleInfoList = new ArrayList<>();
    }

    @NonNull
    @Override
    public VehicleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VehicleViewHolder(
                LayoutInflater
                        .from(context)
                        .inflate(R.layout.recycler_item_vehicle, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull VehicleViewHolder holder, final int position) {

        VehicleInfo vehicleInfo = vehicleInfoList.get(position);
        LastUpdatedData lastUpdatedData = vehicleInfo.getLastUpdatedData();

        if (vehicleInfo.getVehicleRegistration() != null) {
            holder.textViewVehicleRegNo.setText(
                    vehicleInfo
                            .getVehicleRegistration()
                            .replace(".", "")
                            .replace(" ", "")
                            .toUpperCase()
            );
        }

        if (lastUpdatedData != null && lastUpdatedData.getImeiNo() != null) {
            holder.txtIMEI.setText(lastUpdatedData.getImeiNo());
        } else {
            holder.txtIMEI.setText(context.getString(R.string.card_imei_no_format));
        }
        if (vehicleInfo.getVehicleType() != null) {
            holder.imageViewVehicleType.setImageDrawable(
                    getVehicleIconResource(context, vehicleInfo.getVehicleType())
            );
        }


        if (lastUpdatedData == null || lastUpdatedData.getSourceDate() == null) {
            clearDataViews(holder);
        } else {
            setupDataInView(holder, lastUpdatedData, lastUpdatedData.getSourceDate(), vehicleInfo.getUseGoogleApi());
            populateAddress(holder, lastUpdatedData, vehicleInfo.getUseGoogleApi());
        }

        holder.itemView.setOnClickListener(v -> {
            cancelAllOpenStreetMapsRequest();
            Bundle bundle = new Bundle();
            bundle.putParcelable(BUNDLE_KEY_VEHICLE_DETAIL, vehicleInfoList.get(position));
            NavigationHelper.navigateToNewActivity((Activity) context, MapActivity.class, bundle);
        });
    }

    private void setupDataInView(@NonNull VehicleViewHolder holder,
                                 @NonNull LastUpdatedData lastUpdatedData,
                                 @NonNull Long sourceDate, boolean isGeoCoder) {
        if (lastUpdatedData.getSourceDate() != null) {
            String date = MapHelper.getEpochTime(
                    lastUpdatedData.getSourceDate(),
                    "dd-MMM-yyyy"
            );
            String time = MapHelper.getEpochTime(
                    lastUpdatedData.getSourceDate(),
                    "hh:mm aa"
            );
            holder.textViewVehicleDate.setText(
                    String.format(context.getString(R.string.card_date), date, time)
            );
        } else {
            holder.textViewVehicleDate.setText(context.getString(R.string.card_date_no_format));
        }

        if (lastUpdatedData.getVehicleMode() != null) {
            holder.vehicleImageContainerLayout.setBackground(
                    getVehicleModeBackground(
                            context,
                            lastUpdatedData.getVehicleMode(),
                            sourceDate
                    )
            );
        }

        if (lastUpdatedData.getSpeed() != null
                && lastUpdatedData.getSpeed() > 0.0) {
            holder.textViewVehicleSpeed.setText(
                    String.format(context.getString(R.string.card_speed),
                            round(lastUpdatedData.getSpeed(), 1)));
        } else {
            holder.textViewVehicleSpeed.setText(String.format(context.getString(R.string.card_speed),
                    0.0));
        }

        if (lastUpdatedData.getGnssFix() != null
                && (System.currentTimeMillis() - sourceDate) < THRESHOLD_TIME_FOR_NON_COMM_VEHICLE) {
            holder.txtStatus.setText(
                    context.getString(R.string.card_connect_label));
        } else {
            holder.txtStatus.setText(
                    context.getString(R.string.card_disconnect_label));
        }


        if (lastUpdatedData.getIgnition() != null
                && lastUpdatedData.getIgnition().equalsIgnoreCase("ON")
                && (System.currentTimeMillis() - sourceDate) < THRESHOLD_TIME_FOR_NON_COMM_VEHICLE) {
            holder.txtIgnition.setText(
                    context.getString(R.string.card_ignition_on_label));
        } else {
            holder.txtIgnition.setText(
                    context.getString(R.string.card_ignition_off_label));
        }

        if (lastUpdatedData.getGsmSignalStrength() != null
                && (System.currentTimeMillis() - sourceDate) < THRESHOLD_TIME_FOR_NON_COMM_VEHICLE) {
            holder.imageViewSignal.setImageDrawable(
                    getGSMStrengthIconResource(
                            context,
                            lastUpdatedData.getGsmSignalStrength()
                    )
            );
        } else {
            holder.imageViewSignal.setImageDrawable(
                    getGSMStrengthIconResource(
                            context,
                            0
                    )
            );
        }

        populateAddress(holder, lastUpdatedData, isGeoCoder);
    }

    private void populateAddress(@NonNull VehicleViewHolder holder, @NotNull LastUpdatedData lastUpdatedData, boolean isGeoCoder) {
        if (lastUpdatedData.getGnssFix() != null
                && lastUpdatedData.getGnssFix() != 0) {
            if (lastUpdatedData.getAddress() != null
                    && !lastUpdatedData.getAddress().isEmpty()) {
                setupAddress(holder, lastUpdatedData.getAddress(), false);
            } else if (lastUpdatedData.getLatitude() != null
                    && lastUpdatedData.getLongitude() != null
                    && lastUpdatedData.getLatitude() != 0.0
                    && lastUpdatedData.getLongitude() != 0.0) {
                setupAddress(holder, "", true);
                //start a new thread for OpenStreetMapsAPI call

                new AddressTask(isGeoCoder, lastUpdatedData, context, callback, null).run();
            } else {
                setupAddress(holder, "", true);
            }
        }
    }

    private String formatAddress(String address) {

        if (address.contains("+")) {
            address = address.substring(address.indexOf(",")+1);
        }

        return address;

    }

    private void clearDataViews(@NonNull VehicleViewHolder holder) {
        holder.textViewVehicleSpeed.setText("");
        holder.textViewVehicleDate.setText("");
        setupAddress(holder, "", true);
    }

    private void setupAddress(@NonNull VehicleViewHolder holder, @Nullable String address, boolean hideViews) {
        if (address == null || address.isEmpty() || hideViews) {
            holder.textViewVehicleAddress.setText(holder.textViewVehicleAddress.getContext().getString(R.string.location_fetching));
            if (!holder.shimmer.isAnimating())
                holder.shimmer.start(holder.textViewVehicleAddress);
        } else {
            holder.textViewVehicleAddress.setText(formatAddress(address));
            if (holder.shimmer.isAnimating())
                holder.shimmer.cancel();
        }
    }

    @Override
    public int getItemCount() {
        return (vehicleInfoList == null || vehicleInfoList.isEmpty()) ? 0 : vehicleInfoList.size();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    public void updateFilterList(List<VehicleInfo> values) {
        this.vehicleInfoList.clear();
        if (values != null && !values.isEmpty()) {
            this.vehicleInfoList.addAll(values);
        }
        notifyDataSetChanged();
    }
}