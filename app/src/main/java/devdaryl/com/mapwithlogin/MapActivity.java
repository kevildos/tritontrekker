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
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.arlib.floatingsearchview.util.view.SearchInputView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
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
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.MiniDrawer;
import com.mikepenz.materialdrawer.model.MiniDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import okhttp3.internal.http2.Header;

/**
 * Created by User on 10/2/2017.
 */

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    // nav menu vars
    private Drawer menu;
    private PrimaryDrawerItem accountItem;
    private PrimaryDrawerItem addPoiItem;
    private PrimaryDrawerItem filterItem;
    private PrimaryDrawerItem myPoisItem;
    private AccountHeader header;
    private static final int accountItemID = 1;
    private static final int addPoitItemID = 2;
    private static final int filterItemID = 3;
    private static final int mypoisID = 4;

    // maps vars
    private static final Boolean ENABLE_COMPASS = true;
    private static final Boolean DISABLE_COMPASS = false;


    // Firebase vars
    private static final int ACC_REQ_CODE = 100;
    private static final String ACCOUNT_STATE = "account_state";
    private static final String TAG = "MapActivity";
    private GoogleMap mMap;
    FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private Boolean isLoggedIn;

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
    private static final String GEO_API_KEY = "AIzaSyBUNsVo7I1Yd4qJjkZNbgeFh-Q8hcGsn2Y";

    // Direction Vars
    private Polyline polylineG;
    private DirectionsLeg leg;
    private List<Polyline> mPolylines = new ArrayList<Polyline>();
    private List<Map<String, Object>> locationList;
    private List<Pair> durations = new ArrayList<Pair>();
    private Duration currDuration;

    // onActivityResult request codes
    private int POI_POP_UP = 1;
    private int FILTER_POI = 2;

    // search bar vars
    FloatingSearchView searchBar;

    // image url var
    String imageurl = null;

    //user id stored
    private String userID;

    String queryG;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mFirestore = FirebaseFirestore.getInstance();
        mFirestore.collection("locations");
        mAuth = FirebaseAuth.getInstance();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync((OnMapReadyCallback) this);

        if (geoApiContext == null) {
            geoApiContext = new GeoApiContext.Builder()
                    .apiKey(GEO_API_KEY)
                    .build();
        }

        // setup for menu
        buildMenuItems();
        buildMenuHeader();

        // build menu now
        buildMenu();

        //build search bar
        buildSearchBar();

        // create authentication listener for FirebaseUser
        createAuthList();

        checkUserInDB();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        mMap.getUiSettings().setCompassEnabled(true);

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

        ImageButton locationButton = (ImageButton)findViewById(R.id.myLocation);

        locationButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(myLocation, 18);
                mMap.animateCamera(cameraUpdate);
            }
        });

        ImageButton changeMapType = (ImageButton)findViewById(R.id.maptype);
        changeMapType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int maptype = mMap.getMapType();
                switch (maptype) {

                    case 1:
                        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        break;
                    case 2:
                        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                        break;
                    case 3:
                        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        break;
                    case 4:
                        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        break;
                    default:
                        break;

                }
            }
        });

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        //mMap.setPadding(0, height - 250, 0, 0);

        setUpMapClickListener();
        setUpPolyLinClickListener();
        setUpMarkerClickListener();
    }

    private void setUpMapClickListener() {
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
                MarkerOptions options = new MarkerOptions()
                        .position(pos)
                        .title("Add POI Here")
                        .snippet("0;You can add a POI here if you are logged in and go to the menu; ;0;0;false;false;false");;
                // add marker to map
                mMap.addMarker(options);
                pinDropped = true;
                pinDroppedLocation = pos;

                // zoom in camera on marker
                //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 14f));
            }
        });
    }

    private void setUpPolyLinClickListener() {
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
    }

    private void setUpMarkerClickListener(){
        // Click on the marker to display a pop up
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                mClickedMarker = marker;
                Intent intent = new Intent(MapActivity.this, PoiPopUp.class);
                String toInsert = marker.getTitle();
                intent.putExtra("Name", toInsert);
                String information = marker.getSnippet();
                intent.putExtra("information", information);
                LatLng markLoc = marker.getPosition();
                intent.putExtra("Latitude", markLoc.latitude);
                intent.putExtra("Longitude", markLoc.longitude);
                intent.putExtra("imageurl", imageurl);
                startActivityForResult(intent, POI_POP_UP);

//                intent.putExtra("imageurl", (String)doc.getData().get("photoURL"));
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
        double mylat = myLocation.latitude;
        double mylon = myLocation.longitude;

        if(pinDroppedLocation != null) {
            double markerlat = pinDroppedLocation.latitude;
            double markerlon = pinDroppedLocation.longitude;
            intent.putExtra("MarkerLatitude", markerlat);
            intent.putExtra("MarkerLongitude", markerlon);
            intent.putExtra("pindropped", true);
            System.out.println("userid in map activity: " + userID);

            System.out.println("MapActivity: mlat " + intent
                    .getDoubleExtra("MarkerLatitude", 0)+ ", mlong " + intent.getDoubleExtra("MarkerLongitude", 0));
        }
        else{
            intent.putExtra("pindropped", false);
        }

        intent.putExtra("userid", userID);
        intent.putExtra("MyLatitude", mylat);
        intent.putExtra("MyLongitude", mylon);

        System.out.println("MapActivity: lat " + intent
                .getDoubleExtra("MyLatitude", 0)+ ", long " + intent.getDoubleExtra("MyLongitude", 0));

        System.out.println("MapActivity: lat " + intent
                .getDoubleExtra("MyLatitude", 0)+ ", long " + intent.getDoubleExtra("MyLongitude", 0));

        startActivity(intent);
    }

    public void openFilterPOIActivity() {
        Intent intent = new Intent(this, FilterPOI.class);
        startActivityForResult(intent, FILTER_POI);
    }

    public void openMyPOIsActivity(){
        Intent intent = new Intent(this, mypois.class);
        ArrayList<String> idlist = new ArrayList<String>();
        ArrayList<String> namelist = new ArrayList<String>();

        CollectionReference locsCollection = mFirestore.collection("locations");
        locsCollection.whereEqualTo("creatorid", userID).get().addOnCompleteListener(
                new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot doc : task.getResult()) {
                                idlist.add(doc.getId());
                                System.out.println("docid: " + doc.getId());
                                namelist.add(doc.getData().get("name").toString());
                            }
                        }
                        intent.putStringArrayListExtra("idlist", idlist);
                        intent.putStringArrayListExtra("namelist", namelist);
                        startActivity(intent);

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

    //database search
    //public void onMapSearch(View view, String query)
    public void onMapSearch(String query) {

        queryG = query;
        mMap.clear();
        //EditText locationSearch = (EditText) findViewById(R.id.editText4);
        //String location = locationSearch.getText().toString();

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
                    String name = (String) list.get(0).get("name");
                    String description = (String)list.get(0).get("description");
                    String type = (String)list.get(0).get("type");
                    String id = (String) list.get(0).get("id");
                    long likes = (long) list.get(0).get("likes");
                    long dislikes = (long) list.get(0).get("dislikes");
                    imageurl = (String)list.get(0).get("photoURL");

                    Toast.makeText(MapActivity.this, "Id is " + list.get(0).get("id") + "Latitude: " + latitude + "  Longtitude: " + longtitude, Toast.LENGTH_LONG).show();
                    LatLng maloc = new LatLng(latitude, longtitude);
                    placeMarker(maloc, name, description, id, likes, dislikes, type);
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(maloc, 18);
                    mMap.animateCamera(cameraUpdate);
                }
//                else
//                    googleDatabase(query);

            }
        }, query);

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
                Toast.makeText(MapActivity.this, location + " was not found!", Toast.LENGTH_LONG).show();
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

    public void filter(final boolean tra, final boolean prin, final boolean wat,
                       final boolean lh, final boolean rest) {

        System.out.println("Got to 1 ");
        filterData(new FirestoreCallback() {
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
                    for(Map<String, Object> poi : list) {
                        GeoPoint geoPoint = (GeoPoint) poi.get("location");
                        double latitude = geoPoint.getLatitude();
                        double longtitude = geoPoint.getLongitude();
                        String name = (String) poi.get("name");
                        String description = (String)poi.get("description");
                        String type = (String)poi.get("type");
                        String id = (String)poi.get("id");
                        long likes = (long)poi.get("likes");
                        long dislikes = (long)poi.get("dislikes");
                        Toast.makeText(MapActivity.this, "Id is " + poi.get("id") + "Latitude: " + latitude + "  Longtitude: " + longtitude, Toast.LENGTH_LONG).show();
                        placeMarker(new LatLng(latitude, longtitude), name, description, id, likes, dislikes, type);
                    }

                }
            }
        }, tra, prin, wat, lh, rest);

    }

    public void filterData(final FirestoreCallback firestoreCallback, final boolean tra,
                      final boolean prin, final boolean wat, final boolean lh,
                      final boolean rest) {

        Task<QuerySnapshot> task =
                FirebaseFirestore.getInstance().collection("locations").get();
        task.addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            public void onFailure(Exception e) {
                // handle any errors here
            }
        });

        List<String> filterChoices = new ArrayList<>();
        if(tra) {
            filterChoices.add("Trash Can");
        }
        if(prin) {
            filterChoices.add("Printer");
        }
        if(rest) {
            filterChoices.add("Restroom");
        }
        if(wat) {
            filterChoices.add("Water");
        }
        if(lh) {
            filterChoices.add("Lecture Hall");
        }

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
                                if(doc.exists()){
                                    System.out.println("Got to 5.25 ");
                                    Map<String, Object> e = doc.getData();
                                    System.out.println("Got to 5.5 ");

                                    for(String type : filterChoices) {
                                        if (e.get("type").equals(type)) {
                                            System.out.println("Got to 6 ");
                                            eventList.add(e);
                                        }
                                    }
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

    public void filterSystem(final boolean tra, final boolean prin, final boolean wat,
                             final boolean lh, final boolean rest) {

    }

    public void readData(final FirestoreCallback firestoreCallback, final String location) {
        System.out.println("Got to 3 ");


        Task<QuerySnapshot> task =
                FirebaseFirestore.getInstance().collection("locations").get();
        task.addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            public void onFailure(Exception e) {
                // handle any errors here
            }
        });

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
                                if(doc.exists()){
                                    System.out.println("Got to 5.25 ");
                                    Map<String, Object> e = doc.getData();
                                    System.out.println("Got to 5.5 ");

                                    // case insensitive search
                                    if (e.get("name").toString().toLowerCase().equals(location.toLowerCase())) {
                                        System.out.println("Got to 6 ");
                                        eventList.add(e);
                                    }
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
        menu.openDrawer();
    }

    private void placeMarker(LatLng latLng, String name, String description, String id, long likes, long dislikes, String type) {

        DocumentReference docIdRef = mFirestore.collection("users").document(userID);
        docIdRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        if (document.get(id) != null) {
                            List<Boolean> poi = (List<Boolean>) document.get(id);
                            boolean liked = poi.get(0);
                            boolean disliked = poi.get(1);
                            boolean favorited = poi.get(2);
                            MarkerOptions options = new MarkerOptions().
                                    position(latLng)
                                    .title(name)
                                    .snippet(id + ";" + description + ";" + type + ";" + likes + ";" + dislikes + ";"
                                                + liked + ";" + disliked + ";" +favorited);
                            mMap.addMarker(options);
                        } else {
                            MarkerOptions options = new MarkerOptions().
                                    position(latLng)
                                    .title(name)
                                    .snippet(id + ";" + description + ";" + type + ";" + likes + ";" + dislikes + ";"
                                            + false + ";" + false + ";" + false);
                            mMap.addMarker(options);
                        }
                    }
                }
            }
        });
       /*MarkerOptions options = new MarkerOptions().
                position(latLng)
                .title(name)
                .snippet(id + ";" + description + ";" + type + ";" + likes + ";" + dislikes);
        mMap.addMarker(options);*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == POI_POP_UP) {
            System.out.println("daf'sjijfseai'fd;afaesjfiadsfnseifadnfifheiafdsnfsiafeninjfilefa''o;");

            //Toast.makeText(MapActivity.this, id, Toast.LENGTH_LONG).show();
            int lik = data.getExtras().getInt("likes");
            int dis = data.getExtras().getInt("dislikes");
            String id = data.getStringExtra("id");
            mFirestore.collection("locations").document(id).update("likes", lik);
            mFirestore.collection("locations").document(id).update("dislikes", dis);

            Boolean liked = data.getExtras().getBoolean("liked");
            Boolean disliked = data.getExtras().getBoolean("disliked");
            Boolean favorited = data.getExtras().getBoolean("favorited");

            DocumentReference docIdRef = mFirestore.collection("users").document(userID);
            docIdRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            List<Boolean> poi = new ArrayList ();
                            poi.add(liked);
                            poi.add(disliked);
                            poi.add(favorited);
                            mFirestore.collection("users").document(userID).update(id, poi);
                        }
                        Toast.makeText(getApplicationContext(), "Updated POI",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });


            if (resultCode == Activity.RESULT_OK) {
                double lat = data.getExtras().getDouble("latit");
                double lon = data.getExtras().getDouble("longit");
                calculateDirections(new LatLng(lat, lon));
                // Get likes and dislikes from intent
                //boolean like = data.getBooleanExtra("like", false);
               // boolean dislike = data.getBooleanExtra("dislike", false);
            }
        } else if(requestCode == FILTER_POI) {
            if(resultCode == Activity.RESULT_OK) {

                boolean trash = data.getExtras().getBoolean("trash");
                boolean restroom = data.getExtras().getBoolean("restroom");
                boolean water = data.getExtras().getBoolean("water");
                boolean printer = data.getExtras().getBoolean("printer");
                boolean lectureHall = data.getExtras().getBoolean("lectureHall");
                boolean fav = data.getExtras().getBoolean("favorite");
                if(fav) {
                    filterSystem(trash, printer, water, lectureHall, restroom);
                }

                //Toast.makeText(MapActivity.this, " trash is " + trash + " restroom is " + restroom +
                //        " water is " + water + " printer is " + printer + " lectureHall is " + lectureHall
                //        , Toast.LENGTH_LONG).show();

                filter(trash, printer, water, lectureHall, restroom);
            }
        }

            mMap.clear();
            //EditText locationSearch = (EditText) findViewById(R.id.editText4);
            //String location = locationSearch.getText().toString();

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
                        String name = (String) list.get(0).get("name");
                        String description = (String)list.get(0).get("description");
                        String type = (String)list.get(0).get("type");
                        String id = (String) list.get(0).get("id");
                        long likes = (long) list.get(0).get("likes");
                        long dislikes = (long) list.get(0).get("dislikes");
                        imageurl = (String)list.get(0).get("photoURL");

                        Toast.makeText(MapActivity.this, "Id is " + list.get(0).get("id") + "Latitude: " + latitude + "  Longtitude: " + longtitude, Toast.LENGTH_LONG).show();
                        LatLng maloc = new LatLng(latitude, longtitude);
                        placeMarker(maloc, name, description, id, likes, dislikes, type);
//                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(maloc, 18);
//                        mMap.animateCamera(cameraUpdate);
                    }
                }
            }, queryG);

            locationList = new ArrayList<>();
        }


    // build the menu items going into the menu
    private void buildMenuItems(){
        accountItem = new PrimaryDrawerItem()
                .withIdentifier(accountItemID)
                .withName("Log in")
                .withIcon(R.drawable.ic_person_black_24dp)
                .withSelectable(false);
        addPoiItem = new PrimaryDrawerItem().withIdentifier(addPoitItemID)
                .withName("Add POI")
                .withIdentifier(addPoitItemID)
                .withIcon(R.drawable.ic_add_location_black_24dp)
                .withSelectable(false);
        filterItem = new PrimaryDrawerItem().withIdentifier(accountItemID)
                .withIdentifier(filterItemID)
                .withName("Filter")
                .withIcon(R.drawable.ic_filter_list_black_24dp)
                .withSelectable(false);
        myPoisItem = new PrimaryDrawerItem().withIdentifier(mypoisID)
                .withIdentifier(mypoisID)
                .withName("My POIs")
                .withIcon(R.drawable.ic_directions_black_24dp)
                .withSelectable(false);

    }

    // build the header for the menu
    private void buildMenuHeader(){
        header = new AccountHeaderBuilder()
                .withActivity(MapActivity.this)
                .withAccountHeader(R.layout.material_drawer_layout)
                .withHeaderBackground(R.drawable.logo_2)
                .withSelectionListEnabledForSingleProfile(false)
                .withProfileImagesVisible(false)
                .build();
    }

    // updates the menu header as the user logs in and out
    private void updateMenuHeader(Boolean isLoggedIn){
        if(isLoggedIn){
            FirebaseUser user = mAuth.getCurrentUser();

            header.removeProfile(0);
            header.addProfiles(
                    new ProfileDrawerItem()
                            .withName(user.getDisplayName())
                            .withEmail(user.getEmail())
            );

        }
        else{
            header.removeProfile(0);
            header.addProfiles(
                    new ProfileDrawerItem()
                            .withName("Guest")
                            .withEmail("Log in to gain full access")
            );
        }
    }

    // builds the navigation menu
    private void buildMenu(){
        menu = new DrawerBuilder()
                .withActivity(MapActivity.this)
                .addDrawerItems(
                        accountItem,
                        filterItem
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        long id = drawerItem.getIdentifier();

                        return openMenuItem(id);
                    }
                })
                .withAccountHeader(header)
                .withSelectedItem(-1)

                .build();
    }

    // close menu if back button is pressed
    @Override
    public void onBackPressed() {
        if (menu.isDrawerOpen()) {
            menu.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    // builds the search bar
    private void buildSearchBar(){
        searchBar = findViewById(R.id.floating_search_view);

        searchBar.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSuggestionClicked(SearchSuggestion searchSuggestion) {

            }
            @Override
            public void onSearchAction(String currentQuery) {
                onMapSearch(currentQuery);
            }
        });

        searchBar.setOnLeftMenuClickListener(new FloatingSearchView.OnLeftMenuClickListener() {
            @Override
            public void onMenuOpened() {
                menu.openDrawer();
                searchBar.closeMenu(true);
            }

            @Override
            public void onMenuClosed() {
                // intentionally left blank
                // not needed
            }
        });
    }

    // creates and attaches an authentication listener to check for log in changes for Firebase user
    private void createAuthList(){
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
    }

    // helper method for menu on click listener. Decides which menu item to open based on id passed
    private boolean openMenuItem(long itemID){

        // account activity handler
        if(itemID == accountItemID){
            openAccountActivity();
        }

        // addpoi activity handler
        else if (itemID == addPoitItemID) {
            menu.closeDrawer();
            openAddPOIActivity();
        }

        // filterpoi activity handler
        else if (itemID == filterItemID) {
            menu.closeDrawer();
            openFilterPOIActivity();
        }

        else if(itemID == mypoisID){
            menu.closeDrawer();
            openMyPOIsActivity();
        }

        return true;
    }

    // update the state of the log in button as the user logs in and out
    private void updateLoginButton(Boolean isLoggedIn){
        if(isLoggedIn){
            accountItem.withName("Log out");
            menu.updateItem(accountItem);
            menu.addItemAtPosition(addPoiItem, 2);
            menu.addItemAtPosition(myPoisItem, 4);
        }
        else {
            accountItem.withName("Log in");
            menu.updateItem(accountItem);
            menu.removeItem(addPoitItemID);
            menu.removeItem(mypoisID);
        }

        updateMenuHeader(isLoggedIn);
    }

    private void checkUserInDB() {

        // check if user signed in
        if(mAuth.getCurrentUser() != null) {
            mFirestore
                    .collection("users")
                    .document(mAuth.getCurrentUser().getUid())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()){

                                DocumentSnapshot user = task.getResult();

                                // add user to DB if they do not exist
                                if(!user.exists()){
                                    System.out.println("Add user!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

                                    Map<String, Object> userMap = new HashMap<>();

                                    mFirestore
                                            .collection("users")
                                            .document(mAuth.getCurrentUser().getUid())
                                            .set(userMap);
                                }
                            }
                            userID = mAuth.getCurrentUser().getUid();
                            System.out.println("userID " + userID);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MapActivity.this, "Problem with getting user from DB", Toast.LENGTH_LONG).show();
                }
            });
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
