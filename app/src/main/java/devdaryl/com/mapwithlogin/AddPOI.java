package devdaryl.com.mapwithlogin;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.type.LatLng;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AddPOI extends AppCompatActivity {

    FirebaseFirestore mFirestore;
    double latitude;
    double longitude;
    double markerlat = 0.00;
    double markerlon = 0.00;
    Uri imageUri;
    imageDialog dialog;
    String photoURL = null;
    String type = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_add_poi);

        RadioButton currlocButton = (RadioButton) findViewById(R.id.currlocRadio);
        RadioButton markerlocButton = (RadioButton) findViewById(R.id.markerlocRadio);

        double mylat = getIntent().getDoubleExtra("MyLatitude", 0.00);
        double mylon = getIntent().getDoubleExtra("MyLongitude", 0.00);

        System.out.println("AddPOIActivity: lat " + mylat + ", long " + mylon);
        System.out.println("AddPOIActivity: lat " + getIntent()
                .getDoubleExtra("MyLatitude", 0)+ ", long " + getIntent().getDoubleExtra("MyLongitude", 0));

        System.out.println("AddPOIActivity: lat " + getIntent()
                .getDoubleExtra("MyLatitude", 0)+ ", long " + getIntent().getDoubleExtra("MyLongitude", 0));

        boolean pindropped = getIntent().getBooleanExtra("pindropped", false);

        if(pindropped) {
            markerlat = getIntent().getDoubleExtra("MarkerLatitude", 0.00);
            markerlon = getIntent().getDoubleExtra("MarkerLongitude", 0.00);
        } else{
            markerlocButton.setClickable(false);
            markerlocButton.setTextColor(R.color.color_grey);
        }

        System.out.println("LAT LONG: " + mylat + " " + mylon);
        System.out.println("LAT LONG 2: " + markerlat + " " + markerlon);

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
        spinnerDescriptionArray.add("Water Refill Station");
        spinnerDescriptionArray.add("Other");
        spinnerDescriptionArray.add("Choose type");
        final int listsize = spinnerDescriptionArray.size() - 1;


        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, spinnerDescriptionArray) {
            @Override
            public int getCount() {
                return(listsize); // Truncate the list
            }
        };


        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        spinner.setSelection(listsize);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int index = parent.getSelectedItemPosition();
                if(index == 0) {
                    type = "Restroom";
                } else if (index == 1) {
                    type = "Lecture Hall";
                }else if (index == 2) {
                    type = "Printer";
                }else if (index == 3) {
                    type = "Trash Can";
                }else if (index == 4) {
                    type = "Water";
                }else if (index == 5) {
                    type = "Other";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        /*
         * PHOTO BUTTON MENU
         */


        // Button initializations and listeners
        final EditText nameInput = (EditText) findViewById(R.id.name);
        Button addPicture = (Button) findViewById(R.id.button);
        Button confirm = (Button) findViewById(R.id.button2);
        final EditText desc = (EditText)findViewById(R.id.editText2);


        // Picture button Listener
        addPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // USE THIS CODE FOR FUTURE CAMERA INTENT BUTTON
//                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//                startActivity(cameraIntent);
                dialog = new imageDialog();
                dialog.show(getSupportFragmentManager(), "test");

                // Make menu appear
            }
        });

        currlocButton.setChecked(true);

        // Submit button listener
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(photoURL == null){
                    Toast.makeText(getApplicationContext(), "Please wait for the image to finish uploading...",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                System.out.println(nameInput.getText());
                String name = nameInput.getText().toString();
                String description = desc.getText().toString();
                ArrayList<String> list = new ArrayList<String>();
                double lat;
                double lng;

                if(currlocButton.isChecked()){
                    lat = mylat;
                    lng = mylon;
                }
                else {
                    lat = markerlat;
                    lng = markerlon;
                }

                if(type == null) {
                    Toast.makeText(AddPOI.this, "You must select a type for this POI before submitting", Toast.LENGTH_LONG).show();
                } else {
                    addLocation(name, description, "floor_description", list, lat, lng, 1);
                    //addLocation(name, description, "floor_description", list, latitude, longitude, 1);
                    finish();
                }
            }
        });

    }

    private void addLocation(String name, String description, String floor_description, ArrayList<String> key_words,
                             double latitude, double longtitude, int maps_icon) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name", name);
        //userMap.put("search_name", name.toLowerCase());
        userMap.put("description", description);
        userMap.put("key_words", key_words);
        GeoPoint location = new GeoPoint(latitude, longtitude);
        userMap.put("location", location);
        userMap.put("maps_icon", "icon");
        userMap.put("rating", 1);
        userMap.put("likes", (long) 0);
        userMap.put("dislikes", (long) 0);
        userMap.put("reports", (long) 0);
        userMap.put("photoURL", photoURL);
        userMap.put("creatorid", (String) getIntent().getExtras().get("userid"));


        final String name2 = name;
        String id = mFirestore.collection("locations").document().getId();
        userMap.put("id", id);
        userMap.put("type", type);

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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        System.out.println("ACTIVITY RESULT");
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == 2000 && resultCode == RESULT_OK) {
//            ImageView imageView = (ImageView)findViewById(R.id.imageView1);
            System.out.println("PHOTO TAKEN");
            System.out.println("Uri image in addpoi: " + imageUri);

            uploadPicture(imageUri);


//            imageView.setImageBitmap(photo);
        }
        else if(requestCode == 1000 && resultCode == RESULT_OK){
            Uri selectedImage = data.getData();
            System.out.println("uri of gallery image: " + selectedImage.toString());
//
//            ContentResolver contentResolver = getContentResolver();
//            MimeTypeMap mime = MimeTypeMap.getSingleton();
//            String extension = mime.getExtensionFromMimeType(contentResolver.getType(selectedImage));

            uploadPicture(selectedImage);

        }else{
            System.out.println("PHOTO NOT TAKEN");
        }
    }
    public void getUri(Uri uri){
        imageUri = uri;
    }

    public void uploadPicture(Uri uri){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference filepath = storage.getReference(UUID.randomUUID().toString());

        Toast.makeText(getApplicationContext(), "Please wait while the image is uploading",
                Toast.LENGTH_SHORT).show();

        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        float density  = getResources().getDisplayMetrics().density;
        int dpHeight = (int) (145 * Resources.getSystem().getDisplayMetrics().density);

        Bitmap bitmap = null;

        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
        }catch(Exception e){
            System.out.println("ERROR READING FILE");
        }

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap withCompressed = Bitmap.createScaledBitmap(bitmap,900,900,true);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        withCompressed.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] bytedata = stream.toByteArray();

        UploadTask uploadTask = filepath.putBytes(bytedata);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println("UPLOADING PICTURE FAILED");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Uri downloadUrl = uri;
                        System.out.println("Actual url i will put in firebase: " + uri);
                        photoURL = uri.toString();
                        Toast.makeText(getApplicationContext(), "Image has been uploaded",
                                Toast.LENGTH_SHORT).show();

                    }
                });
                System.out.println("UPLOAD SUCCESSFUL");
//                    System.out.println("URL: " + filepath.getDownloadUrl());
//                    photoURL = filepath.getDownloadUrl().toString();
//                    System.out.println(photoURL);

            }
        });
    }


}