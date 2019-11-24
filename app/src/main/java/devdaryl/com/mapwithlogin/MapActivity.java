package devdaryl.com.mapwithlogin;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiActivity;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TravelMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * Created by User on 10/2/2017.
 */

public class MapActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback,
        LocationListener
{

    private static final String TAG = "MapActivity";
    private GoogleMap mMap;
    private Button accountButton;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private DrawerLayout getmDrawerLayout;
    FirebaseFirestore mFirestore;

    private boolean pinDropped = false;
    private Location mLastLocation;
    private LatLng pinDroppedLocation;
    private LatLng currLocation;
    private Marker mCurrLocationMarker;

    private LatLng myLocation;

    private FusedLocationProviderClient mFusedLocationProviderClient;

    private GeoApiContext geoApiContext = null;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
         //   checkLocationPermission();
        //}

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mFirestore = FirebaseFirestore.getInstance();

        if (mNavigationView != null) {
            mNavigationView.setNavigationItemSelectedListener(this);
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync((OnMapReadyCallback) this);

        if(geoApiContext == null){
            geoApiContext = new GeoApiContext.Builder().apiKey("AIzaSyBUNsVo7I1Yd4qJjkZNbgeFh-Q8hcGsn2Y").build();
        }
    }

    // Usage: pulls a list of the document id's under that key_word
    private void locationKeyWord(final String key_word) {
        mFirestore.collection("key_words").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                boolean bool = false;
                if(task.isSuccessful()) {
                    List<DocumentSnapshot> list = task.getResult().getDocuments();
                    for(DocumentSnapshot document: list){
                        if(document.get(key_word) != null) {
                            List<String> kw_list = (List<String>) document.get(key_word);
                            printKeyWordLocations(kw_list);
                            Toast.makeText(MapActivity.this, "KeyWord list retrieved from Firestore", Toast.LENGTH_LONG).show();
                            bool = true;
                            break;
                        }
                    }
                    if(!bool){
                        Toast.makeText(MapActivity.this, "Not in Firestore", Toast.LENGTH_LONG).show();

                    }
                    //DocumentSnapshot snapshot = task.getResult();
                    //String song = snapshot.getString("")
                }
                else {
                    Log.d("Firebase Error", "Error: " + task.getException().getMessage());

                }
            }
        });
    }
    /*
     * Usage: List contains strings, which are the document id's for locations in the "location" collection on Firestore
     */
    private void printKeyWordLocations(List<String> list) {
        for(String document_id: list) {
            mFirestore.collection("locations").document(document_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        //Map<String, Object> document = task.getResult().getData();
                        if(document.exists()){
                            Map<String, Object> doc = task.getResult().getData();
                            Toast.makeText(MapActivity.this, "Item in key_word is: " + doc.get("name"), Toast.LENGTH_LONG).show();
                        }
                        else {
                            Log.d( "No such document", "Error");
                        }
                        Toast.makeText(MapActivity.this, "KeyWord list retrieved from Firestore", Toast.LENGTH_LONG).show();

                        //DocumentSnapshot snapshot = task.getResult();
                        //String song = snapshot.getString("")
                    } else {
                        Log.d("Firebase Error", "Error: " + task.getException().getMessage());

                    }
                }
            });
        }
    }

    public void openLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void openAddPOIActivity(){
        Intent intent = new Intent(this, AddPOI.class);
        LatLng toPassIn;
        if(pinDropped == true) {
            toPassIn = pinDroppedLocation;
        }
        else {
            toPassIn = myLocation;
        }


        Double lat = toPassIn.latitude;
        Double lon = toPassIn.longitude;


        intent.putExtra("Latitude", lat.toString());
        intent.putExtra("Longitude", lon.toString());
        startActivity(intent);
    }

    public void openFilterPOIActivity() {
        Intent intent = new Intent(this, FilterPOI.class);
        startActivity(intent);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        int id = menuItem.getItemId();


        // login activity handler
        if(id == R.id.nav_account){
            mDrawerLayout.closeDrawers();
            openLoginActivity();
        }

        // addpoi activity handler
        if(id == R.id.nav_addpoi){
            mDrawerLayout.closeDrawers();
            openAddPOIActivity();
        }

        // filterpoi activity handler
        if(id == R.id.nav_filterpoi){
            mDrawerLayout.closeDrawers();
            openFilterPOIActivity();
        }

        if(id == R.id.nav_directions){
            mDrawerLayout.closeDrawers();
            calculateDirections(pinDroppedLocation);
        }


        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            }
        }
        else {
            mMap.setMyLocationEnabled(true);
        }

        getDeviceLocation();

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                pinDropped = false;
                pinDroppedLocation = null;
                mMap.clear();
            }
        });

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng pos) {
                // clear map
                mMap.clear();

                // set marker options:
                // is draggable, can change title and snippet (text under title)
                MarkerOptions options = new MarkerOptions().position(pos).title("Name").snippet("Type");

                // add marker to map
                mMap.addMarker(options);
                pinDropped = true;
                pinDroppedLocation = pos;

                // zoom in camera on marker
                //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 14f));
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = mMap.addMarker(markerOptions);

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public void onMapSearch(View view) {
        mMap.clear();
        EditText locationSearch = (EditText) findViewById(R.id.editText4);
        String location = locationSearch.getText().toString();
        List<Address>addressList = null;

        if (location != null || !location.equals("")) {
            Geocoder geocoder = new Geocoder(this);
            try {
                addressList = geocoder.getFromLocationName(location, 1);

            } catch (IOException e) {
                e.printStackTrace();
            }
            Address address = addressList.get(0);
            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
            mMap.addMarker(new MarkerOptions().position(latLng).title(location));
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLng)      // Sets the center of the map to Mountain View
                    .zoom(18)                   // Sets the zoom
                    .bearing(0)                // Sets the orientation of the camera to east
                    .build();                   // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    private void getDeviceLocation() {

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        Task location = mFusedLocationProviderClient.getLastLocation();
        location.addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    Location currentLocation = (Location) task.getResult();
                    myLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(myLocation)      // Sets the center of the map to Mountain View
                            .zoom(18)                   // Sets the zoom
                            .bearing(0)                // Sets the orientation of the camera to east
                            .build();                   // Creates a CameraPosition from the builder
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
            }
        });

    }

    private void filterList() {

        //sample creating
        POI first = new POI(32.8812, -117.2375, "geisel", "place to live life", "building");
        POI second = new POI(32.8818, -117.2375, "close first", "life", "building");
        POI third = new POI(32.8899, -117.2440, "north east", "place ", "building");
        POI fourth = new POI(32.8800, -117.2350, "south west", "idk", "building");
        List<POI> returnList = new ArrayList<>();
        returnList.add(first);
        returnList.add(second);
        returnList.add(third);
        returnList.add(fourth);
        putMarkerOnMap(returnList);
    }

    private void putMarkerOnMap(List<POI> poiList) {

        for(POI cur : poiList) {
            double lat = cur.getLatitude();
            double lon = cur.getLongitude();
            LatLng location = new LatLng(lat, lon);
            mMap.addMarker(new MarkerOptions()
                .position(location)
                    .title(cur.getName())
                    .snippet(cur.getDescription())
            );
        }
    }

    private void addPolyLinesToMap(final DirectionsResult result){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: result routes: " + result.routes.length);

                for(DirectionsRoute route: result.routes){
                    Log.d(TAG, "run: leg: " + route.legs[0].toString());
                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());

                    List<LatLng> newDecodedPath = new ArrayList<>();

                    for(com.google.maps.model.LatLng latLng: decodedPath){

                        newDecodedPath.add(new LatLng(
                                latLng.lat, latLng.lng
                        ));
                    }

                    Polyline polyline = mMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                    polyline.setClickable(true);
                }
            }
        });
    }

    private void calculateDirections(LatLng latlongDest){
        Log.d(TAG, "calculateDirections: calculating directions.");

        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                latlongDest.latitude,latlongDest.longitude);
        DirectionsApiRequest directions = new DirectionsApiRequest(geoApiContext);
        directions.mode(TravelMode.WALKING);

        directions.alternatives(true);
        directions.origin(
                new com.google.maps.model.LatLng(myLocation.latitude,myLocation.longitude)
        );
        Log.d(TAG, "calculateDirections: destination: " + destination.toString());
        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                Log.d(TAG, "onResult: routes: " + result.routes[0].toString());
                Log.d(TAG, "onResult: geocodedWayPoints: " + result.geocodedWaypoints[0].toString());
                addPolyLinesToMap(result);
            }

            @Override
            public void onFailure(Throwable e) {
                Log.e(TAG, "onFailure: " + e.getMessage() );

            }
        });
    }
}
