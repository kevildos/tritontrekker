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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiActivity;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.gms.maps.OnMapReadyCallback;

import java.io.IOException;
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


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mFirestore = FirebaseFirestore.getInstance();

        if (mNavigationView != null) {
            mNavigationView.setNavigationItemSelectedListener(this);
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync((OnMapReadyCallback) this);

//        accountButton = (Button) findViewById(R.id.nav_account);
//        accountButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                openLoginActivity();
//            }
//        });
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
       /* LatLng toPassIn;
        if(pinDropped == true)
            toPassIn = pinDroppedLocation;
        else
            toPassIn = currLocation;

        intent.putExtra("Pos", toPassIn);*/
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

    //marker click listener for pop up screen
   /* public void onMarkerLongClick
    {
        Intent intent = new Intent(this, PoiPopUp.class);

    }*/
}
