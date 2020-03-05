package com.example.markerdemo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;


//AppCompatActivity extends FragmentActivity
public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {


    private static final String TAG = "MapsActivity";
    //Request code for location permission request.
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied = false;
    private static final String API_KEY = "AIzaSyDKoYoQaNYCCKtuCjSPmC0z_pE8-nTPwnQ";
    private static final String baseUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json";


    private FusedLocationProviderClient mFusedLocationProviderClient;

    private GoogleMap mMap;
    private GoogleApiClient client;
    Marker marker;

    private Location lastLocation;
    private Marker currentLocationMarker;
    double latitude, longitude;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: Creation begins");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Log.d(TAG, "onCreate: Creation ends");
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: Map is now ready to be used");

        mMap = googleMap;

        mMap.getUiSettings().setZoomControlsEnabled(true);
        enableMyLocation();
        updateLocationUI();

        //mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
        mMap.setOnMarkerClickListener(this);
        //mMap.setOnInfoWindowClickListener(this);

    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "enableMyLocation: Permission to access the location is missing");
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE, Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            Log.d(TAG, "enableMyLocation: Location access granted");
            mMap.setMyLocationEnabled(true);
        }
    }

    private void updateLocationUI() {
        mFusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        Log.d(TAG, "onSuccess: Location successfully retrieved");
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            Log.d(TAG, "onSuccess: Retrieved location is not null ");
                            marker = mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(location.getLatitude(), location.getLongitude()))
                                    .title("Current Location")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(marker.getPosition().latitude, marker.getPosition().longitude), 10));

                            Log.d(TAG, "onSuccess: location retrieved" + latitude + " " + longitude);

                            getLocationsFromGoogleMaps();
                        }
                    }
                });
    }


    protected synchronized void buildGoogleApiClient() {
        client = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        client.connect();

    }

    private String urlLocation;
    private String urlKey;
    private String urlRadius = "15000";
    int switcher;


    protected void getLocationsFromGoogleMaps() {
        Log.d(TAG, "getLocationsFromGoogleMaps: Obtaining location from google maps " + latitude + " " + longitude);
        
        Object dataTransfer[] = new Object[4];
        GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();

        mMap.clear();

        //switcher stores the result of the button or drop down for
        switcher = 1;
        //Specify whether to saerch for links by type or name
        boolean byType = true;
        String query = "";
        String url;

        switch(switcher) {
            case 1: //Toilet
                query = "toilet";
                byType = false;
                break;
            case 2: //Restaurant
                query = "restaurant";
                break;
            case 3: //Mall
                query = "shopping_mall";
                break;
            case 4: //Gas station
                query = "gas_station";
                break;
            case 5:
                query = "bar";
                break;
            default:
                Log.d(TAG, "getLocationsFromGoogleMaps: Invalid query");



        }

        //TODO: Get rating filter and free/paid filter from user dropDownMenus
        //Currently because these are not implemented, the rating and free/paid values are hardcoded
        int rating = 5;
        boolean free = true;

        url = getUrl(latitude, longitude, query, byType);
        dataTransfer[0] = mMap;
        dataTransfer[1] = url;
        dataTransfer[2] = rating;
        dataTransfer[3] = free;

        getNearbyPlacesData.execute(dataTransfer);
        Toast.makeText(MapsActivity.this, "Showing Nearby " + query, Toast.LENGTH_SHORT).show();

        //Toast.makeText(MapsActivity.this, "Showing Nearby School", Toast.LENGTH_SHORT).show();

    }

    private String getUrl(double latitude , double longitude , String nearbyPlace, boolean byType)
    {

        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlaceUrl.append("location="+latitude+","+longitude);
        googlePlaceUrl.append("&radius="+urlRadius);
        //type has predefined types. name can be given any string. Check up documentation https://developers.google.com/places/web-service/search#PlaceSearchRequests
        if(byType) {
            googlePlaceUrl.append("&type="+nearbyPlace);
        } else {
            googlePlaceUrl.append("&name=" + nearbyPlace);
        }
        googlePlaceUrl.append("&sensor=true");
        googlePlaceUrl.append("&key="+API_KEY);

        Log.d("MapsActivity", "url = "+googlePlaceUrl.toString());

        return googlePlaceUrl.toString();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: Result of Request Permission");

        if(requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            Log.d(TAG, "onRequestPermissionsResult: REquest code incorrect");
            return;
        }

        if(PermissionUtils.isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION)) {
            Log.d(TAG, "onRequestPermissionsResult: Permission granted");
            if(client == null) {
                buildGoogleApiClient();
            }
            enableMyLocation();
        } else {
            Log.d(TAG, "onRequestPermissionsResult: Permission denied");
            mPermissionDenied = true;
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        lastLocation = location;
        if(currentLocationMarker != null)
        {
            currentLocationMarker.remove();

        }
        Log.d("lat = ",""+latitude);
        LatLng latLng = new LatLng(location.getLatitude() , location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Location");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        currentLocationMarker = mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomBy(10));

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocationButton Clicked", Toast.LENGTH_SHORT).show();

        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current Location: " + location, Toast.LENGTH_SHORT).show();
    }

    /**
     * Demonstrates converting a {@link Drawable} to a {@link BitmapDescriptor},
     * for use as a marker icon.
     */
    private BitmapDescriptor vectorToBitmap(@DrawableRes int id, @ColorInt int color) {
        Log.d(TAG, "vectorToBitmap: Converting vector to BitMap");

        Drawable vectorDrawable = ResourcesCompat.getDrawable(getResources(), id, null);
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        DrawableCompat.setTint(vectorDrawable, color);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private boolean checkReady() {
        Log.d(TAG, "checkReady: Checking is map is ready");

        if (mMap == null) {
            Toast.makeText(this, R.string.map_not_ready, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }



    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if(mPermissionDenied) {
            Log.d(TAG, "onResumeFragments: Permission not granted. Displaying error dialog");
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog.newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }



    FileOutputStream fStream;
    FileInputStream fis;
    private static final String toiletFile = "toiletFile.txt";
    private static final String restaurantFile = "restaurantFile.txt";
    private static final String mallFile = "mallFile.txt";
    private static final String gasFile = "gasFile.txt";
    private static final String barFile = "barFile.txt";

    public void processResult(List<HashMap<String, String>> nearbyPlacesList) {
        //TODO: Modify finalList to contain only the necessary fields.

        /*String finalResult = "";


        for(int i=0; i<nearbyPlacesList.size(); i++) {
            HashMap<String, String> googlePlace = nearbyPlacesList.get(i);
            String placeName = googlePlace.get("place_name");
            double lat = Double.parseDouble( googlePlace.get("lat"));
            double lng = Double.parseDouble( googlePlace.get("lng"));
            Random rand = new Random();
            int rating = rand.nextInt(5) + 1;
            int isFreeInt = rand.nextInt(2);
            boolean isFree;
            if(isFreeInt == 0) isFree = false;
            else isFree = true;

            //if(i==11) placeName = "Siri Corneropp:uco Bank";
            //if(i==16) placeName = "Good Gopalapatnam Bunk Junction";


            finalResult += placeName + "," + lat + "," + lng + "," + rating + "," + isFree + "#";


        }

        Log.d(TAG, "processResult: called");

        writeToFile(finalResult, this);


         */



        String finalRes = readFromFile(this);

        Log.d(TAG, "processResult: The result is " + finalRes);

        //TODO: Place all 5 different location storage data in files.


        MarkerOptions markerOptions = new MarkerOptions();

        int i=0;
        String[] resList = finalRes.split("#");
        Log.d(TAG, "processResult: String length: " + resList.length);

        for(String result : resList) {
            i++;
            Log.d(TAG, "processResult: result = " + result);




            String[] properties = result.split(",");
            //placename, lat, lng, rating, isFree

            String placeName = properties[0];
            double lat = Double.parseDouble(properties[1]);
            double lng = Double.parseDouble(properties[2]);
            int rating = Integer.parseInt(properties[3]);
            boolean isFree = Boolean.parseBoolean(properties[4]);

            int btnRating = 4;
            boolean btnIsFree = true;
            if(rating >= btnRating && isFree) {
                Log.d(TAG, "processResult: filtered location " + result);

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

    private void writeToFile(String data,Context context) {
        Log.d(TAG, "writeToFile: Writing into file");
        String fileName;
        switch (switcher) {
            case 1:
                fileName = toiletFile;
                break;
            case 2:
                fileName = restaurantFile;
                break;
            case 3:
                fileName = mallFile;
                break;
            case 4:
                fileName = gasFile;
                break;
            case 5:
                fileName = barFile;
                break;
            default:
                fileName = toiletFile;

        }

        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(fileName, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private String readFromFile(Context context) {
        Log.d(TAG, "readFromFile: Reading from file");

        String ret = "";
        InputStream inputStream;

        try {
            switch (switcher) {
                case 1:
                    inputStream = context.openFileInput(toiletFile);
                    break;
                case 2:
                    inputStream = context.openFileInput(restaurantFile);
                    break;
                case 3:
                    inputStream = context.openFileInput(mallFile);
                    break;
                case 4:
                    inputStream = context.openFileInput(gasFile);
                    break;
                case 5:
                    inputStream = context.openFileInput(barFile);
                    break;
                default:
                    inputStream = context.openFileInput(toiletFile);

            }

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        Log.d(TAG, "readFromFile: Successfully read from file " + ret);

        return ret;
    }




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
            List<HashMap<String, String>> finalList = showNearbyPlaces(nearbyPlaceList);
            processResult(finalList);
        }

        private List<HashMap<String, String>> showNearbyPlaces(List<HashMap<String, String>> nearbyPlaceList)
        {
            Log.d(TAG, "showNearbyPlaces: nearby places" + nearbyPlaceList);



            /*for(int i = 0; i < nearbyPlaceList.size(); i++)
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

             */
            return nearbyPlaceList;
        }
    }
}
