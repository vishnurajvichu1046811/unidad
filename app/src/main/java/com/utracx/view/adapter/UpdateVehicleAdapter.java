package com.utracx.view.adapter;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.utracx.R;
import com.utracx.api.model.rest.vehicle_list.LastUpdatedData;
import com.utracx.api.model.rest.vehicle_list.VehicleInfo;
import com.utracx.api.request.interfaces.VehicleUpdateItemClick;

import java.util.ArrayList;

public class UpdateVehicleAdapter extends RecyclerView.Adapter<UpdateVehicleAdapter.ViewHolder>{
    private final Context context;
    private ArrayList<VehicleInfo> vehicleInfoList;

    private  VehicleUpdateItemClick vehicleUpdateItemClick;

    public UpdateVehicleAdapter(Context context, ArrayList<VehicleInfo> vehiclesArrayList, VehicleUpdateItemClick vehicleUpdateItemClick) {
        this.context = context;
        this.vehicleInfoList = vehiclesArrayList;
        this.vehicleUpdateItemClick = vehicleUpdateItemClick;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater
                        .from(context)
                        .inflate(R.layout.update_vehicle_list, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        VehicleInfo vehicleInfo = vehicleInfoList.get(position);
        LastUpdatedData lastUpdatedData = vehicleInfo.getLastUpdatedData();
        holder.etUpVehicleNo.setText(vehicleInfo.getVehicleRegistration());

    }

    @Override
    public int getItemCount() {
        return vehicleInfoList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView etUpVehicleNo;
        Button update, cancel;
        ConstraintLayout clMain;
        LinearLayout llupVehicle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            etUpVehicleNo = itemView.findViewById(R.id.et_vehicleno);
            update = itemView.findViewById(R.id.btn_upVehicleNo);
            cancel = itemView.findViewById(R.id.btn_cVehicleNo);
            clMain = itemView.findViewById(R.id.cl_main);
            llupVehicle = itemView.findViewById(R.id.ll_up_vehicle_no);


            etUpVehicleNo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    vehicleUpdateItemClick.onVehicleUpdateItemClicked(vehicleInfoList.get(getAdapterPosition()),getAdapterPosition());
                }
            });

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    llupVehicle.setVisibility(View.GONE);
                    vehicleUpdateItemClick.onCancelClicked(vehicleInfoList.get(getAdapterPosition()),getAdapterPosition());
                }
            });

            update.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    llupVehicle.setVisibility(View.GONE);
                    vehicleUpdateItemClick.onUpdateClicked(vehicleInfoList.get(getAdapterPosition()),getAdapterPosition(),etUpVehicleNo.getText().toString());
                }
            });

        }
    }
}

