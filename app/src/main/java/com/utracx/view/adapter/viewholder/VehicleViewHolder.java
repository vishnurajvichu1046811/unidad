package com.utracx.view.adapter.viewholder;

import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerTextView;
import com.utracx.R;

public class VehicleViewHolder extends RecyclerView.ViewHolder {

    public TextView textViewVehicleDate;
    public ShimmerTextView textViewVehicleAddress;

    public View vehicleImageContainerLayout;
    public ImageView imageViewVehicleType;
    public TextView textViewVehicleRegNo;

    public TextView textViewVehicleSpeed;
    public TextView txtStatus;
    public TextView txtIgnition;
    public TextView txtIMEI;
    public ImageView imageViewSignal;
    public Shimmer shimmer;

    public VehicleViewHolder(@NonNull View itemView) {
        super(itemView);

        textViewVehicleDate = itemView.findViewById(R.id.txtVehicleDate);
        textViewVehicleDate.setVisibility(View.VISIBLE);
        textViewVehicleAddress = itemView.findViewById(R.id.textViewVehicleAddress);

        vehicleImageContainerLayout = itemView.findViewById(R.id.vehicleImageContainerLayout);
        imageViewVehicleType = itemView.findViewById(R.id.imageViewVehicleType);
        textViewVehicleRegNo = itemView.findViewById(R.id.textViewVehicleRegNo);

        textViewVehicleSpeed = itemView.findViewById(R.id.txtCurrentSpeed);
        imageViewSignal = itemView.findViewById(R.id.imageViewSignal);
        txtIgnition = itemView.findViewById(R.id.txtIgnition);
        txtStatus = itemView.findViewById(R.id.txtStatus);
        txtIMEI = itemView.findViewById(R.id.txtIMEI);
        shimmer = new Shimmer();
    }
}