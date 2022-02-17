package com.utracx.view.adapter.helper;

import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.utracx.view.adapter.AlertAdapter;

import static android.view.View.GONE;

public class AlertAdapterDataObserver extends RecyclerView.AdapterDataObserver {

    private final AlertAdapter alertAdapter;
    private final TextView noDataTextView;
    private final ProgressBar progressBar;
    private final TextView loadingTextView;

    public AlertAdapterDataObserver(AlertAdapter alertAdapter, TextView noDataTextView,
                                    ProgressBar progressBar, TextView loadingTextView) {

        this.alertAdapter = alertAdapter;
        this.noDataTextView = noDataTextView;
        this.progressBar = progressBar;
        this.loadingTextView = loadingTextView;
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
        if (alertAdapter.getItemCount() > 0) {
            this.noDataTextView.setVisibility(GONE);
            this.progressBar.setVisibility(GONE);
            this.loadingTextView.setVisibility(GONE);
        }
    }
}
