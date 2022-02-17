package com.utracx.view.adapter.helper;

import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.utracx.util.ConstantVariables;
import com.utracx.view.adapter.VehicleAdapter;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class VehicleAdapterDataObserver extends RecyclerView.AdapterDataObserver {

    private final String vehicleType;
    private final TextView emptyTextView;
    private final VehicleAdapter vehicleAdapter;

    public VehicleAdapterDataObserver(VehicleAdapter vehicleAdapter, String vehicleType,
                                      TextView emptyTextView) {
        this.vehicleType = vehicleType;
        this.emptyTextView = emptyTextView;
        this.vehicleAdapter = vehicleAdapter;
    }

    @Override
    public void onChanged() {
        super.onChanged();
        checkEmpty();
    }

    @Override
    public void onItemRangeInserted(int positionStart, int itemCount) {
        super.onItemRangeInserted(positionStart, itemCount);
        checkEmpty();
    }

    @Override
    public void onItemRangeRemoved(int positionStart, int itemCount) {
        super.onItemRangeRemoved(positionStart, itemCount);
        checkEmpty();
    }

    private void checkEmpty() {
        String message;
        if (((VehicleListFilter) vehicleAdapter.getFilter()).isSearching()) {
            message = "There are no vehicles matching the search.";
        } else if (vehicleType == null || vehicleType.isEmpty()) {
            message = "There are no vehicles to show";
        } else if (vehicleType.equalsIgnoreCase(ConstantVariables.VEHICLE_MODE_MOVING)) {
            message = "There are no moving vehicles";
        } else if (vehicleType.equalsIgnoreCase(ConstantVariables.VEHICLE_MODE_IDLE)) {
            message = "There are no idle vehicles";
        } else if (vehicleType.equalsIgnoreCase(ConstantVariables.VEHICLE_MODE_SLEEP)) {
            message = "There are no sleeping vehicles";
        } else if (vehicleType.equalsIgnoreCase(ConstantVariables.VEHICLE_MODE_NON_COMMUNICATING)) {
            message = "There are no offline vehicles";
        } else if (vehicleType.equalsIgnoreCase(ConstantVariables.VEHICLE_MODE_ONLINE)) {
            message = "There are no online vehicles";
        } else {
            message = "There are no vehicles to show";
        }

        emptyTextView.setText(message);
        emptyTextView.setVisibility(vehicleAdapter.getItemCount() == 0 ? VISIBLE : GONE);
    }
}
