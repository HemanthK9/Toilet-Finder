package com.example.markerdemo;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/*

class GetNearbyPlacesData extends AsyncTask<Object, String, String> {

    private String googlePlacesData;
    private GoogleMap mMap;
    String url;
    private String TAG = "GetNearbyPlacesData";
    private int rating;
    private boolean isFree;



    @Override
    protected String doInBackground(Object... objects){
        //TODO: Pass passedRating and passedFree from getLocationsFromGoogleMaps()
        //rating = passedRating;
        //free = passedFree;

        Log.d(TAG, "doInBackground: " + objects);

        mMap = (GoogleMap)objects[0];
        url = (String)objects[1];
        rating = (int) objects[2];
        isFree = (boolean) objects[3];

        Log.d(TAG, "doInBackground: The url is " + url);

        DownloadURL downloadURL = new DownloadURL();
        try {
            googlePlacesData = downloadURL.readUrl(url);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "doInBackground: After running " + googlePlacesData);

        return googlePlacesData;
    }

    @Override
    protected void onPostExecute(String s){

        Log.d(TAG, "onPostExecute: The string " + s);

        List<HashMap<String, String>> nearbyPlaceList;
        DataParser parser = new DataParser();
        nearbyPlaceList = parser.parse(s);
        Log.d("nearbyplacesdata","called parse method");
        showNearbyPlaces(nearbyPlaceList);
        //processResult();
    }

    private void showNearbyPlaces(List<HashMap<String, String>> nearbyPlaceList)
    {
        Log.d(TAG, "showNearbyPlaces: nearby places" + nearbyPlaceList);



        for(int i = 0; i < nearbyPlaceList.size(); i++)
        {
            MarkerOptions markerOptions = new MarkerOptions();
            HashMap<String, String> googlePlace = nearbyPlaceList.get(i);

            String placeName = googlePlace.get("place_name");
            String vicinity = googlePlace.get("vicinity");
            double lat = Double.parseDouble( googlePlace.get("lat"));
            double lng = Double.parseDouble( googlePlace.get("lng"));

            // googlePlace - name, lat, lng
            //TODO: Add these values to the database
            //TODO: Create columns: rating, free/paid
            //TODO: COmpare with rating and free variables present in this class

            //TODO: The dbRating and dbFree are variables which are retrieved from the database. Implement later.
            int dbRating = 5;
            boolean dbFree = true;
            if(dbRating == rating && dbFree == isFree) {

                Log.d(TAG, "showNearbyPlaces: The place is " + placeName + lat + lng);

                LatLng latLng = new LatLng(lat, lng);
                markerOptions.position(latLng);
                markerOptions.title(placeName);
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

                mMap.addMarker(markerOptions);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
            }
        }
    }
}

 */