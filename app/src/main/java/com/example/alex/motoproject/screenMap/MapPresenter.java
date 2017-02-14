package com.example.alex.motoproject.screenMap;



public class MapPresenter implements MapViewInterface {
    private MapViewInterface mapViewInterface;
    public MapPresenter mapPresenter;


    public MapPresenter getMapPresenter() {
        return mapPresenter;
    }

    public MapPresenter(MapViewInterface mapViewInterface) {
        this.mapViewInterface = mapViewInterface;
        mapPresenter=MapPresenter.this;

    }

    @Override
    public void showOnMap() {
        mapViewInterface.showOnMap();
    }
}
