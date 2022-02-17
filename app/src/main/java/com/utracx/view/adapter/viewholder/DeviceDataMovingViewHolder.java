package com.utracx.view.adapter.viewholder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.utracx.R;

public class DeviceDataMovingViewHolder extends RecyclerView.ViewHolder {

    public static final int HEADER_TYPE_MOVING = 0;
    public static final int NORMAL_TYPE_MOVING = 1;
    public static final int FOOTER_TYPE_MOVING = 2;
    public static final int SINGLE_TYPE_MOVING = -1;

    public TextView textViewTripTime;
    public View topLine;
    public View bottomLine;
    public TextView textViewSpeed;
    public TextView textViewDuration;
    public TextView textViewDistanceTravelled;


    public DeviceDataMovingViewHolder(@NonNull View itemView, int viewType) {
        super(itemView);
        textViewTripTime = itemView.findViewById(R.id.textViewTripTime);
        textViewSpeed = itemView.findViewById(R.id.textViewSpeed);
        textViewDuration = itemView.findViewById(R.id.textViewDuration);
        textViewDistanceTravelled = itemView.findViewById(R.id.textViewDistanceTravelled);

        topLine = itemView.findViewById(R.id.topLine);
        bottomLine = itemView.findViewById(R.id.bottomLine);

        if (viewType == NORMAL_TYPE_MOVING) {
            topLine.setVisibility(View.VISIBLE);
            bottomLine.setVisibility(View.VISIBLE);
        } else if (viewType == HEADER_TYPE_MOVING) {
            topLine.setVisibility(View.INVISIBLE);
            bottomLine.setVisibility(View.VISIBLE);
        } else if (viewType == FOOTER_TYPE_MOVING) {
            topLine.setVisibility(View.VISIBLE);
            bottomLine.setVisibility(View.INVISIBLE);
        } else if (viewType == SINGLE_TYPE_MOVING) {
            topLine.setVisibility(View.INVISIBLE);
            bottomLine.setVisibility(View.INVISIBLE);
        }
    }
}