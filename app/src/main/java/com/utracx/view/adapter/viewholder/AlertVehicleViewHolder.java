package com.utracx.view.adapter.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.utracx.R;
import com.utracx.api.model.rest.vehicle_list.VehicleInfo;

public class AlertVehicleViewHolder extends RecyclerView.ViewHolder {
    public ImageView imageViewVehicleType;
    public TextView textViewVehicleRegNo;
    public TextView textViewAlerts01;
    public TextView textViewAlerts02;
    public TextView textViewAlerts03;
    public TextView textViewAlerts04;
    public TextView textViewAlerts05;
    public TextView textViewAlertTotal;
    public View itemView;

    public AlertVehicleViewHolder(@NonNull View itemView, @NonNull ParentVehicleClickListener onClickListener) {
        super(itemView);
        this.itemView = itemView;
        imageViewVehicleType = itemView.findViewById(R.id.imageViewVehicleType);
        textViewVehicleRegNo = itemView.findViewById(R.id.textViewVehicleRegNo);
        textViewAlerts01 = itemView.findViewById(R.id.textViewAlerts01);
        textViewAlerts02 = itemView.findViewById(R.id.textViewAlerts02);
        textViewAlerts03 = itemView.findViewById(R.id.textViewAlerts03);
        textViewAlerts04 = itemView.findViewById(R.id.textViewAlerts04);
        textViewAlerts05 = itemView.findViewById(R.id.textOther);
        textViewAlertTotal = itemView.findViewById(R.id.textViewAlertTotal);
        itemView.setOnClickListener(onClickListener::onClick);
    }

    public void setTag(@NonNull VehicleInfo currentVehicle) {
        itemView.setTag(currentVehicle);
    }


    public interface ParentVehicleClickListener {
        void onClick(View rowView);
    }
}