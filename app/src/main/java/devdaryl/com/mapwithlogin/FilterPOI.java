package devdaryl.com.mapwithlogin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


public class FilterPOI extends AppCompatActivity {

    boolean favorite = false;
    boolean trash = false;
    boolean printer = false;
    boolean restroom = false;
    boolean water = false;
    boolean lectureHall = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_filter_poi);
        androidx.appcompat.widget.Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        toolbar.setElevation(0);

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_close);
        }


        // Button initializations and listeners
        CompoundButton favoriteSelected = findViewById(R.id.checkBox);
        CompoundButton trashSelected = findViewById(R.id.checkBox2);
        CompoundButton printerSelected = findViewById(R.id.checkBox3);
        CompoundButton restroomSelected = findViewById(R.id.checkBox4);
        CompoundButton waterSelected = findViewById(R.id.checkBox5);

        favoriteSelected.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(((CompoundButton) v).isChecked()){
                    System.out.println("Favorite Checked");
                    favorite = true;
                } else {
                    System.out.println("Favorite Un-Checked");
                    favorite = false;
                }
            }
        });
        trashSelected.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(((CompoundButton) v).isChecked()){
                    System.out.println("Trash Checked");
                    trash = true;
                } else {
                    System.out.println("Trash Un-Checked");
                    trash = false;
                }
            }
        });
        printerSelected.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(((CompoundButton) v).isChecked()){
                    System.out.println("Printer Checked");
                    printer = true;
                } else {
                    System.out.println("Printer Un-Checked");
                    printer = false;
                }
            }
        });
        restroomSelected.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(((CompoundButton) v).isChecked()){
                    System.out.println("Restroom Checked");
                    restroom = true;
                } else {
                    System.out.println("Restroom Un-Checked");
                    restroom = false;
                }
            }
        });
        waterSelected.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(((CompoundButton) v).isChecked()){
                    System.out.println("Water Checked");
                    water = true;
                } else {
                    System.out.println("Water Un-Checked");
                    restroom = false;
                }
            }
        });

    }

    // Handles X button click
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            Intent returnIntent = getIntent();
            returnIntent.putExtra("favorite", favorite);
            returnIntent.putExtra("trash", trash);
            returnIntent.putExtra("restroom", restroom);
            returnIntent.putExtra("water", water);
            returnIntent.putExtra("lectureHall", lectureHall);
            returnIntent.putExtra("printer", printer);
            setResult(Activity.RESULT_OK,returnIntent);
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }
}