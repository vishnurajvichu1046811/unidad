package com.utracx.model.map;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class MapDataModel implements Parcelable {

    public static final Creator<MapDataModel> CREATOR = new Creator<MapDataModel>() {
        @Override
        public MapDataModel createFromParcel(Parcel in) {
            return new MapDataModel(in);
        }

        @Override
        public MapDataModel[] newArray(int size) {
            return new MapDataModel[size];
        }
    };
    private List<CustomMapPoint> mapPointList;

    public MapDataModel() {
        this.mapPointList = new ArrayList<>();
    }


    private MapDataModel(Parcel in) {
        mapPointList = in.createTypedArrayList(CustomMapPoint.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(mapPointList);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public List<CustomMapPoint> getMapPoints() {
        return mapPointList;
    }

    public void addNewMapPoint(CustomMapPoint newMapPoint) {
        this.mapPointList.add(newMapPoint);
    }
}
