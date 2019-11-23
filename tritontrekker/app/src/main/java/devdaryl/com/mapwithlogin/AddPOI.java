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

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

public class AddPOI extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_add_poi);

        androidx.appcompat.widget.Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setElevation(0);

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

        List<String> spinnerDescriptionArray =  new ArrayList<String>();
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
        final EditText nameInput = (EditText) findViewById(R.id.editText);
        Button addPicture = (Button) findViewById(R.id.button);
        Button confirm = (Button) findViewById(R.id.button2);
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
                finish();
            }
        });

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
