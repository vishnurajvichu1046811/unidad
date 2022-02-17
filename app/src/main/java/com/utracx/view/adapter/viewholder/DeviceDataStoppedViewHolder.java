package com.utracx.view.adapter.viewholder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerTextView;
import com.utracx.R;

public class DeviceDataStoppedViewHolder extends RecyclerView.ViewHolder {

    public static final int HEADER_TYPE_STOPPED = 10;
    public static final int NORMAL_TYPE_STOPPED = 11;
    public static final int FOOTER_TYPE_STOPPED = 12;
    public static final int SINGLE_TYPE_STOPPED = -10;

    public TextView textViewTripTime;
    public View topLine;
    public View bottomLine;
    public TextView textViewDuration;
    public ShimmerTextView textViewVehicleAddress;
    public Shimmer shimmer;

    public DeviceDataStoppedViewHolder(@NonNull View itemView, int viewType) {
        super(itemView);

        textViewTripTime = itemView.findViewById(R.id.textViewTripTime);
        textViewDuration = itemView.findViewById(R.id.textViewDuration);
        textViewVehicleAddress = itemView.findViewById(R.id.textViewVehicleAddress);

        topLine = itemView.findViewById(R.id.topLine);
        bottomLine = itemView.findViewById(R.id.bottomLine);
        shimmer = new Shimmer();
        if (viewType == NORMAL_TYPE_STOPPED) {
            topLine.setVisibility(View.VISIBLE);
            bottomLine.setVisibility(View.VISIBLE);
        } else if (viewType == HEADER_TYPE_STOPPED) {
            topLine.setVisibility(View.INVISIBLE);
            bottomLine.setVisibility(View.VISIBLE);
        } else if (viewType == FOOTER_TYPE_STOPPED) {
            topLine.setVisibility(View.VISIBLE);
            bottomLine.setVisibility(View.INVISIBLE);
        } else if (viewType == SINGLE_TYPE_STOPPED) {
            topLine.setVisibility(View.INVISIBLE);
            bottomLine.setVisibility(View.INVISIBLE);
        }
    }
}