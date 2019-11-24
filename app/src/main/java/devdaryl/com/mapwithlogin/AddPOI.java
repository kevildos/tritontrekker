package devdaryl.com.mapwithlogin;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.type.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddPOI extends AppCompatActivity {

    FirebaseFirestore mFirestore;
    double latitude;
    double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String lat = getIntent().getStringExtra("Latitude");
        String lon = getIntent().getStringExtra("Longitude");

        latitude = Double.parseDouble(lat);
        longitude = Double.parseDouble(lon);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_add_poi);

        androidx.appcompat.widget.Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setElevation(0);

        mFirestore = FirebaseFirestore.getInstance();
        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_close);
        }

        /*
         *  DESCRIPTION SELECTOR SPINNER START
         */
        Spinner spinner = findViewById(R.id.spinner);

        final List<String> spinnerDescriptionArray =  new ArrayList<String>();
        spinnerDescriptionArray.add("Bathroom");
        spinnerDescriptionArray.add("Lecture Hall");
        spinnerDescriptionArray.add("Printer");
        spinnerDescriptionArray.add("Trash Cans");
        spinnerDescriptionArray.add("Other");
        spinnerDescriptionArray.add("Choose type");
        final int listsize = spinnerDescriptionArray.size() - 1;

//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
//                android.R.layout.simple_spinner_item, spinnerArray);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinner.setAdapter(adapter);

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, spinnerDescriptionArray) {
            @Override
            public int getCount() {
                return(listsize); // Truncate the list
            }
        };


        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        spinner.setSelection(listsize);


        /*
         * PHOTO BUTTON MENU
         */


        // Button initializations and listeners
        final EditText nameInput = (EditText) findViewById(R.id.name);
        Button addPicture = (Button) findViewById(R.id.button);
        Button confirm = (Button) findViewById(R.id.button2);
        final EditText keywords = (EditText) findViewById(R.id.keywords);
        final EditText desc = (EditText)findViewById(R.id.editText2);
//        Button cancel = (Button) findViewById(R.id.button3);


        // Picture button Listener
        addPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // USE THIS CODE FOR FUTURE CAMERA INTENT BUTTON
//                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//                startActivity(cameraIntent);
                imageDialog dialog = new imageDialog();
                dialog.show(getSupportFragmentManager(), "test");

                // Make menu appear
            }
        });

        // Submit button listener
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println(nameInput.getText());
                String name = nameInput.getText().toString();
                String description = desc.getText().toString();
                ArrayList<String> list = new ArrayList<String>();
                list.add(keywords.getText().toString());
                double lat = latitude;
                double lng = longitude;
                addLocation(name, description, "floor_description", list, lat, lng, 1);
                //addLocation(name, description, "floor_description", list, latitude, longitude, 1);
                finish();
            }
        });

    }

    private void addLocation(String name, String description, String floor_description, ArrayList<String> key_words,
                             double latitude, double longtitude, int maps_icon) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name", name);
        userMap.put("description", description);
        userMap.put("key_words", key_words);
        GeoPoint location = new GeoPoint(latitude, longtitude);
        userMap.put("location", location);
        userMap.put("maps_icon", "icon");
        userMap.put("rating", 1);


        final String name2 = name;
        String id = mFirestore.collection("locations").document().getId();
        mFirestore.collection("locations").document(id).set(userMap);
        Toast.makeText(AddPOI.this, "Location " + name + " added to firestore", Toast.LENGTH_LONG).show();
    }

    // Handles X button click
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }


}
