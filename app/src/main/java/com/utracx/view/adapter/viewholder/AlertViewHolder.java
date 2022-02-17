package com.utracx.view.adapter.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerTextView;
import com.utracx.R;

//public class AlertViewHolder extends RecyclerView.ViewHolder {
public class AlertViewHolder extends RecyclerView.ViewHolder {

    public ImageView alertTypeImageView;
    public TextView textViewAlertDate;
    public TextView textViewAlertType;
    public ShimmerTextView textViewVehicleAddress;
    public Shimmer shimmer;

    public AlertViewHolder(@NonNull View itemView) {
        super(itemView);

        alertTypeImageView = itemView.findViewById(R.id.alertTypeImageView);
        textViewAlertType = itemView.findViewById(R.id.textViewAlertType);
        textViewAlertDate = itemView.findViewById(R.id.textViewAlertDate);
        textViewVehicleAddress = itemView.findViewById(R.id.textViewVehicleAddress);
        shimmer = new Shimmer();
    }
}