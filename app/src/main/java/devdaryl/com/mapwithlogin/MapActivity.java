package devdaryl.com.mapwithlogin;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.location.FusedLocationProviderClient;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.Duration;
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
        LocationListener {

    private static final int ACC_REQ_CODE = 100;
    private static final String ACCOUNT_STATE = "account_state";
    private static final String TAG = "MapActivity";
    private GoogleMap mMap;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    FirebaseFirestore mFirestore;
    private SharedPreferences sp;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private Boolean isLoggedIn;
    private Menu menu;

    // Location vars
    private boolean pinDropped = false;
    private Location mLastLocation;
    private LatLng pinDroppedLocation;
    private Marker mCurrLocationMarker;
    private Marker mClickedMarker;
    private String[] greetings;
    private int numGreetings;

    private LatLng myLocation;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    private GeoApiContext geoApiContext = null;

    // Direction Vars
    private Polyline polylineG;
    private DirectionsLeg leg;
    private List<Polyline> mPolylines = new ArrayList<Polyline>();

    private List<Map<String, Object>> locationList;

    private List<Pair> durations = new ArrayList<Pair>();
    private Duration currDuration;

    // onActivityResult request codes
    private int POI_POP_UP = 1;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mFirestore = FirebaseFirestore.getInstance();
        mFirestore.collection("locations");


        if (mNavigationView != null) {
            mNavigationView.setNavigationItemSelectedListener(this);
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync((OnMapReadyCallback) this);

        if (geoApiContext == null) {
            geoApiContext = new GeoApiContext.Builder().apiKey("AIzaSyBUNsVo7I1Yd4qJjkZNbgeFh-Q8hcGsn2Y").build();
        }

        menu = mNavigationView.getMenu();
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                // check if user is logged in
                if(mAuth.getCurrentUser() != null){
                    isLoggedIn = true;
                }
                else{
                    isLoggedIn = false;
                }

                updateLoginButton(isLoggedIn);
            }
        };
        mAuth.addAuthStateListener(mAuthListener);

        updateNavMenu();
    }

    // update the menu items according to the logged in state of the user
    private void updateNavMenu(){

    }

    private void updateLoginButton(Boolean isLoggedIn){
        if(isLoggedIn){
            menu.findItem(R.id.nav_account).setTitle("Log out");
            menu.findItem(R.id.nav_addpoi).setVisible(true);
        }
        else{
            menu.findItem(R.id.nav_account).setTitle("Log in");
            menu.findItem(R.id.nav_addpoi).setVisible(false);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            }
        } else {
            mMap.setMyLocationEnabled(true);
        }

        getDeviceLocation();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        mMap.setPadding(0, height - 250, 0, 0);

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

        mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(Polyline polyline) {
                for (Polyline polyline1 : mPolylines) {

                    if (polyline1.getId().equals(polyline.getId())) {
                        polyline1.setColor(
                                ContextCompat.getColor(getApplicationContext(), R.color.color_blue));
                        polyline1.setZIndex(1);

                        for (Pair pair : durations) {
                            if (pair.getPolyID().equals(polyline1.getId())) {
                                currDuration = pair.getDuration();
                                break;
                            }
                        }
                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(myLocation)
                                .title("Estimated Time: " + currDuration));
                        System.out.println("CURRDURATION: " + currDuration);
                        marker.showInfoWindow();
                    } else {
                        polyline1.setColor(
                                ContextCompat.getColor(getApplicationContext(), R.color.color_grey));
                        polyline1.setZIndex(0);
                    }
                }
            }
        });

        // Click on the marker to display a pop up
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                mClickedMarker = marker;
                Intent intent = new Intent(MapActivity.this, PoiPopUp.class);
                startActivityForResult(intent, POI_POP_UP);
                return true;
            }
        });
    }

    // Usage: pulls a list of the document id's under that key_word
    private void locationKeyWord(final String key_word) {
        mFirestore.collection("key_words").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                boolean bool = false;
                if (task.isSuccessful()) {
                    List<DocumentSnapshot> list = task.getResult().getDocuments();
                    for (DocumentSnapshot document : list) {
                        if (document.get(key_word) != null) {
                            List<String> kw_list = (List<String>) document.get(key_word);
                            printKeyWordLocations(kw_list);
                            Toast.makeText(MapActivity.this, "KeyWord list retrieved from Firestore", Toast.LENGTH_LONG).show();
                            bool = true;
                            break;
                        }
                    }
                    if (!bool) {
                        Toast.makeText(MapActivity.this, "Not in Firestore", Toast.LENGTH_LONG).show();

                    }
                    //DocumentSnapshot snapshot = task.getResult();
                    //String song = snapshot.getString("")
                } else {
                    Log.d("Firebase Error", "Error: " + task.getException().getMessage());

                }
            }
        });
    }

    /*
     * Usage: List contains strings, which are the document id's for locations in the "location" collection on Firestore
     */
    private void printKeyWordLocations(List<String> list) {
        for (String document_id : list) {
            mFirestore.collection("locations").document(document_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        //Map<String, Object> document = task.getResult().getData();
                        if (document.exists()) {
                            Map<String, Object> doc = task.getResult().getData();
                            Toast.makeText(MapActivity.this, "Item in key_word is: " + doc.get("name"), Toast.LENGTH_LONG).show();
                        } else {
                            Log.d("No such document", "Error");
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

    public void openAccountActivity() {
        Intent intent = new Intent(this, AccountActivity.class);

        intent.putExtra(ACCOUNT_STATE, isLoggedIn);
        startActivityForResult(intent, ACC_REQ_CODE);
    }

    public void openAddPOIActivity() {
        Intent intent = new Intent(this, AddPOI.class);
        LatLng toPassIn;
        if (pinDropped == true) {
            toPassIn = pinDroppedLocation;
        } else {
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


        // account activity handler
        if(id == R.id.nav_account){
            mDrawerLayout.closeDrawers();
            openAccountActivity();
        }

        // addpoi activity handler
        if (id == R.id.nav_addpoi) {
            mDrawerLayout.closeDrawers();
            openAddPOIActivity();
        }

        // filterpoi activity handler
        if (id == R.id.nav_filterpoi) {
            mDrawerLayout.closeDrawers();
            openFilterPOIActivity();
        }

        if (id == R.id.nav_directions) {
            mDrawerLayout.closeDrawers();
            calculateDirections(pinDroppedLocation);
        }


        return true;
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

    //database search
    public void onMapSearch(View view) {

        mMap.clear();
        EditText locationSearch = (EditText) findViewById(R.id.editText4);
        String location = locationSearch.getText().toString();

        System.out.println("Got to 1 ");
        readData(new FirestoreCallback() {
            @Override
            public void onCallback(List<Map<String, Object>> list) {
                System.out.println("Got to 2 ");
                locationList = list;

                if(list != null) {
                    System.out.println("List not null " + list.toString());
                }
                if(list == null) {
                    System.out.println("List is null ");
                }
                else if (!list.isEmpty()) {
                    Toast.makeText(MapActivity.this, "locationList is neither EMPTY NOR NULL" +
                            " with description " + list.get(0).get("description"), Toast.LENGTH_LONG).show();
                    GeoPoint geoPoint = (GeoPoint) list.get(0).get("location");
                    double latitude = geoPoint.getLatitude();
                    double longtitude = geoPoint.getLongitude();
                    Toast.makeText(MapActivity.this, "Latitude: " + latitude + "  Longtitude: " + longtitude, Toast.LENGTH_LONG).show();
                    moveCamera(new LatLng(latitude, longtitude), 20);
                }
                else
                    googleDatabase(location);

            }
        }, location);

        locationList = new ArrayList<>();
    }
    public void googleDatabase(String location){
        List<Address>addressList = null;

        if (location != null || !location.equals("")) {
            Geocoder geocoder = new Geocoder(this);
            try {
                // while(addressList.size() == 0) {
                addressList = geocoder.getFromLocationName(location, 1);
                // }

            } catch (IOException e) {
                e.printStackTrace();
            }
            if(addressList.size() == 0){
                Toast.makeText(MapActivity.this, "Address + " + location + " not found!", Toast.LENGTH_LONG).show();
                return;
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
    public void readData(final FirestoreCallback firestoreCallback, final String location) {
        System.out.println("Got to 3 ");

        mFirestore.collection("locations")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        System.out.println("Got to 4 ");

                        if (task.isSuccessful()) {
                            System.out.println("Got to 5 ");

                            List<Map<String, Object>> eventList = new ArrayList<>();
                            //List<DocumentSnapshot> list = task.getResult().getDocuments();

                            for (DocumentSnapshot doc : task.getResult()) {
                                System.out.println("Got to 5.25 ");

                                Map<String, Object> e = doc.getData();
                                System.out.println("Got to 5.5 ");

                                if (e.get("name").equals(location)) {
                                    System.out.println("Got to 6 ");

                                    eventList.add(e);
                                }
                            }
                            System.out.println("Got to 7 ");

                            firestoreCallback.onCallback(eventList);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }

                });
    }

    public interface FirestoreCallback {
        void onCallback(List<Map<String, Object>> list);
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
        //putMarkerOnMap(returnList);
    }
/*
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
    }*/
    private void addPolyLinesToMap(final DirectionsResult result){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: result routes: " + result.routes.length);

                if (mPolylines.size() > 0) {
                    for (Polyline polyline : mPolylines) {
                        polyline.remove();
                    }
                    mPolylines.clear();
                    mPolylines = new ArrayList<>();
                    durations.clear();
                    durations = new ArrayList<>();
                }

                for (DirectionsRoute route : result.routes) {
                    Log.d(TAG, "run: leg: " + route.legs[0].toString());
                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());

                    List<LatLng> newDecodedPath = new ArrayList<>();

                    for (com.google.maps.model.LatLng latLng : decodedPath) {

                        newDecodedPath.add(new LatLng(
                                latLng.lat, latLng.lng
                        ));
                    }

                    polylineG = mMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                    polylineG.setColor(ContextCompat.getColor(getApplicationContext(), R.color.color_grey));
                    polylineG.setClickable(true);
                    polylineG.setWidth(15f);
                    mPolylines.add(polylineG);
                    System.out.println("POLYLINES ARRAY LENGTH: " + mPolylines.size());

                    mPolylines.get(0).setColor
                            (ContextCompat.getColor(getApplicationContext(), R.color.color_blue));
                    mPolylines.get(0).setZIndex(1);

                    Pair pair = new Pair(polylineG.getId(), route.legs[0].duration);
                    durations.add(pair);
                    System.out.println("ROUTE LEGS LENGTH: " + route.legs.length);

                }

                for (Polyline polyline : mPolylines) {
                    System.err.println("POLYLINE ARRAY POLYLINEID: " + polyline.getId());
                }

                for (Pair pair : durations) {
                    System.err.println("DURATIONS PAIR ARRAY: " + pair.getDuration() + " " + pair.polyID);
                }


                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(myLocation)
                        .title("Estimated Time: " + durations.get(0).duration));
                marker.showInfoWindow();

                int i = 1;
                for (Pair pair : durations) {
                    System.out.println
                            ("Duration " + i + ": " + pair.polyID + " " + pair.duration);
                }
            }
        });
    }

    private void calculateDirections(LatLng latlongDest) {
        Log.d(TAG, "calculateDirections: calculating directions.");

        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                latlongDest.latitude, latlongDest.longitude);
        DirectionsApiRequest directions = new DirectionsApiRequest(geoApiContext);
        directions.mode(TravelMode.WALKING);

        directions.alternatives(true);
        directions.origin(
                new com.google.maps.model.LatLng(myLocation.latitude, myLocation.longitude)
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
                Log.e(TAG, "onFailure: " + e.getMessage());

            }
        });
    }

    public void openMenu(View view) {

        //This function needs to open the menu/ drawer
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.openDrawer(GravityCompat.START);
    }

    private void moveCamera(LatLng latLng, float zoom) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        MarkerOptions options = new MarkerOptions().
                position(latLng)
                .title("u a fag");
        mMap.addMarker(options);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == POI_POP_UP) {
            if (resultCode == Activity.RESULT_OK) {
                // Get likes and dislikes from intent
                boolean like = data.getBooleanExtra("like", false);
                boolean dislike = data.getBooleanExtra("dislike", false);
            }
        }
    }
}

class Pair
{
    String polyID;
    Duration duration;

    public Pair(String id, Duration dur)
    {
        this.polyID = id;
        this.duration = dur;
    }

    public String getPolyID(){
        return polyID;
    }

    public Duration getDuration(){
        return duration;
    }
}
