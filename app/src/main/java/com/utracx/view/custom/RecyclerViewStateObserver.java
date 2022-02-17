package com.utracx.view.custom;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewStateObserver extends RecyclerView.AdapterDataObserver {
    private View emptyView;
    private View loadingView;
    private RecyclerView recyclerView;
    private boolean isLoading;
    private View[] optionalViews;
    private View[] optionalClickViews;

    public RecyclerViewStateObserver(@NonNull RecyclerView recyclerView, @NonNull View emptyView,
                                     @NonNull View loadingView, @Nullable View[] optionalViews,
                                     @Nullable View[] optionalClickViews) {
        this.emptyView = emptyView;
        this.loadingView = loadingView;
        this.recyclerView = recyclerView;
        this.optionalViews = optionalViews;
        this.optionalClickViews = optionalClickViews;
        checkIfEmpty();
    }

    private void checkIfEmpty() {
        if (emptyView != null && recyclerView.getAdapter() != null) {
            if (isDataLoading()) {
                loadingView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                emptyView.setVisibility(View.GONE);
                toggleOptionalViews(true);
                disableClicking(true);
            } else if (recyclerView.getAdapter().getItemCount() == 0) {
                emptyView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                loadingView.setVisibility(View.GONE);
                toggleOptionalViews(true);
                disableClicking(true);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyView.setVisibility(View.GONE);
                loadingView.setVisibility(View.GONE);
                toggleOptionalViews(false);
                disableClicking(false);
            }
        }
    }

    private void toggleOptionalViews(boolean shouldHideViews) {
        if (optionalViews == null || optionalViews.length <= 0) {
            return;
        }
        for (View eachView : optionalViews) {
            eachView.setVisibility(shouldHideViews ? View.GONE : View.VISIBLE);
        }
    }

    private void disableClicking(boolean shouldDisableView) {
        if (optionalClickViews == null || optionalClickViews.length <= 0) {
            return;
        }
        for (View eachView : optionalClickViews) {
            eachView.setClickable(shouldDisableView);
            eachView.setEnabled(shouldDisableView);
        }
    }

    @Override
    public void onChanged() {
        checkIfEmpty();
    }

    @Override
    public void onItemRangeInserted(int positionStart, int itemCount) {
        checkIfEmpty();
        super.onItemRangeInserted(positionStart, itemCount);
    }

    @Override
    public void onItemRangeRemoved(int positionStart, int itemCount) {
        checkIfEmpty();
        super.onItemRangeRemoved(positionStart, itemCount);
    }

    @Override
    public void onItemRangeChanged(int positionStart, int itemCount) {
        checkIfEmpty();
        super.onItemRangeChanged(positionStart, itemCount);
    }

    @Override
    public void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
        checkIfEmpty();
        super.onItemRangeChanged(positionStart, itemCount, payload);
    }

    @Override
    public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
        checkIfEmpty();
        super.onItemRangeMoved(fromPosition, toPosition, itemCount);
    }

    private boolean isDataLoading() {
        return isLoading;
    }

    public void setDataLoading(boolean loadingState) {
        this.isLoading = loadingState;
        checkIfEmpty();
    }
}