package com.utracx.view.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.utracx.R;
import com.utracx.background.AddressTask;
import com.utracx.view.adapter.data.TripSummary;
import com.utracx.view.adapter.viewholder.DeviceDataMovingViewHolder;
import com.utracx.view.adapter.viewholder.DeviceDataStoppedViewHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.utracx.util.ConstantVariables.VEHICLE_MODE_MOVING;
import static com.utracx.util.helper.MapHelper.getEpochTime;
import static com.utracx.util.helper.MathHelper.round;
import static com.utracx.view.adapter.viewholder.DeviceDataMovingViewHolder.FOOTER_TYPE_MOVING;
import static com.utracx.view.adapter.viewholder.DeviceDataMovingViewHolder.HEADER_TYPE_MOVING;
import static com.utracx.view.adapter.viewholder.DeviceDataMovingViewHolder.NORMAL_TYPE_MOVING;
import static com.utracx.view.adapter.viewholder.DeviceDataMovingViewHolder.SINGLE_TYPE_MOVING;
import static com.utracx.view.adapter.viewholder.DeviceDataStoppedViewHolder.FOOTER_TYPE_STOPPED;
import static com.utracx.view.adapter.viewholder.DeviceDataStoppedViewHolder.HEADER_TYPE_STOPPED;
import static com.utracx.view.adapter.viewholder.DeviceDataStoppedViewHolder.NORMAL_TYPE_STOPPED;
import static com.utracx.view.adapter.viewholder.DeviceDataStoppedViewHolder.SINGLE_TYPE_STOPPED;
import static java.lang.String.valueOf;

public class DeviceDataAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final Context context;
    private final List<TripSummary> deviceDataArrayList;
    private final AddressTask.GeoAddressUpdateCallback callback;
    private boolean isGeoCoder;
    public DeviceDataAdapter(Context context, AddressTask.GeoAddressUpdateCallback callback, boolean isGeoCoder) {
        this.context = context;
        this.callback = callback;
        this.isGeoCoder = isGeoCoder;
        this.deviceDataArrayList = new ArrayList<>();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case HEADER_TYPE_MOVING:
            case NORMAL_TYPE_MOVING:
            case FOOTER_TYPE_MOVING:
            case SINGLE_TYPE_MOVING:
                return new DeviceDataMovingViewHolder(
                        LayoutInflater
                                .from(parent.getContext())
                                .inflate(R.layout.recycler_item_trip_summary_moving, parent, false),
                        viewType
                );

            case HEADER_TYPE_STOPPED:
            case NORMAL_TYPE_STOPPED:
            case FOOTER_TYPE_STOPPED:
            case SINGLE_TYPE_STOPPED:
            default:
                return new DeviceDataStoppedViewHolder(
                        LayoutInflater
                                .from(parent.getContext())
                                .inflate(R.layout.recycler_item_trip_summary_stopped, parent, false),
                        viewType
                );
        }
    }

    @Override
    public int getItemViewType(int position) {
        boolean isMoving = deviceDataArrayList.get(position).getVehicleMode().equalsIgnoreCase(VEHICLE_MODE_MOVING);

        if (getItemCount() == 1) {
            return isMoving ? SINGLE_TYPE_MOVING : SINGLE_TYPE_STOPPED;
        } else if (position == 0) {
            return isMoving ? HEADER_TYPE_MOVING : HEADER_TYPE_STOPPED;
        } else if (position == deviceDataArrayList.size() - 1) {
            return isMoving ? FOOTER_TYPE_MOVING : FOOTER_TYPE_STOPPED;
        } else {
            return isMoving ? NORMAL_TYPE_MOVING : NORMAL_TYPE_STOPPED;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        TripSummary currentTripSummary = deviceDataArrayList.get(position);
        if (currentTripSummary.getVehicleMode().equalsIgnoreCase(VEHICLE_MODE_MOVING)) {
            loadMovingTripNode((DeviceDataMovingViewHolder) holder, currentTripSummary);
        } else {
            loadStoppedTripNode((DeviceDataStoppedViewHolder) holder, currentTripSummary);
        }
    }

    private void loadMovingTripNode(@NonNull DeviceDataMovingViewHolder holder,
                                    @NonNull TripSummary currentTripSummary) {
        if (currentTripSummary.getStartTime() == 0) {
            holder.textViewTripTime.setText("");
        } else {
            holder.textViewTripTime.setText(
                    getEpochTime(currentTripSummary.getStartTime(), "hh:mm aa")
            );
        }

        CharSequence finalTextSpeed = TextUtils.concat(round(currentTripSummary.getAverageSpeed(), 1) + " km/hr");
        holder.textViewSpeed.setText(finalTextSpeed);

        holder.textViewDuration.setText(getTimeText(currentTripSummary.getDuration()));

        String distanceText = round(currentTripSummary.getDistance(), 2) + " Km";
        holder.textViewDistanceTravelled.setText(distanceText);

    }

    private void loadStoppedTripNode(@NonNull DeviceDataStoppedViewHolder holder,
                                     @NonNull TripSummary currentTripSummary) {
        if (currentTripSummary.getStartTime() == 0) {
            holder.textViewTripTime.setText("");
        } else {
            holder.textViewTripTime.setText(
                    getEpochTime(currentTripSummary.getStartTime(), "hh:mm aa")
            );
        }

        holder.textViewDuration.setText(getTimeText(currentTripSummary.getDuration()));

        if (currentTripSummary.getAddress() != null
                && !currentTripSummary.getAddress().isEmpty()) {
            holder.textViewVehicleAddress.setText(formatAddress(currentTripSummary.getAddress()));
            if (holder.shimmer.isAnimating()) {
                holder.shimmer.cancel();
            }
        } else {
            holder.textViewVehicleAddress.setText(holder.textViewVehicleAddress.getContext().getString(R.string.location_fetching));
            if (!holder.shimmer.isAnimating()) {
                holder.shimmer.start(holder.textViewVehicleAddress);
            }
            new AddressTask(isGeoCoder , currentTripSummary.getLastUpdatedData(), context, callback, null).run();
        }
    }

    private String formatAddress(String address) {

        if (address.contains("+")) {
            address = address.substring(address.indexOf(",")+1);
        }

        return address;

    }

    private CharSequence getTimeText(long durationInMilliSeconds) {

        // Values
        long hours = TimeUnit.MILLISECONDS.toHours(durationInMilliSeconds);

        long minutes = TimeUnit.MILLISECONDS.toMinutes(durationInMilliSeconds) - (hours * 60);

        long seconds = TimeUnit.MILLISECONDS.toSeconds(durationInMilliSeconds) - (hours * 60 * 60) - (minutes * 60);

        if (hours > 0) {
            return TextUtils.concat(
                    valueOf(hours), "h",
                    " ", valueOf(minutes), "m",
                    " ", valueOf(seconds), "s"
            );
        }

        if (minutes > 0) {
            return TextUtils.concat(
                    " ", valueOf(minutes), "m",
                    " ", valueOf(seconds), "s"
            );
        }

        // Only seconds
        return TextUtils.concat(
                valueOf(seconds), " ", "s"
        );
    }

    @Override
    public int getItemCount() {
        return deviceDataArrayList.isEmpty() ? 0 : deviceDataArrayList.size();
    }

    public void updateDeviceDataList(List<TripSummary> values) {
        this.deviceDataArrayList.clear();
        this.deviceDataArrayList.addAll(values);
        notifyDataSetChanged();
    }


    public double getTotalDistance() {
        double totalDistance = 0.0;
        if (!deviceDataArrayList.isEmpty()) {
            for (int i = 0; i < deviceDataArrayList.size(); i++) {
                totalDistance += deviceDataArrayList.get(i).getDistance();
            }
        }
        return totalDistance;
    }
}

