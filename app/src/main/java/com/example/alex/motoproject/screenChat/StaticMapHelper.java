package com.example.alex.motoproject.screenChat;


import com.google.android.gms.maps.model.LatLng;

class StaticMapHelper {
    static String createStaticMapLink(LatLng latLng, int width, int height) {
        double lat = latLng.latitude;
        double lng = latLng.longitude;
        return "https://maps.googleapis.com/maps/api/staticmap?center=&zoom=14&language=ukr&scale=1&size=" + width + "x" + height + "&maptype=roadmap&key=AIzaSyCUUbIjXkkaEo8wAzJ--Mk-DoH7PtvpWw0&format=png&visual_refresh=true&markers=size:mid%7Ccolor:0x0080ff%7Clabel:%7C" + lat + ",+" + lng;
    }
}
