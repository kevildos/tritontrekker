package devdaryl.com.mapwithlogin;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.model.LatLng;

import devdaryl.com.mapwithlogin.R;

public class PoiPopUp extends AppCompatActivity {

    int numLikes = 0;
    int numDislikes = 0;
    String name;
    String id;
    boolean favorite;
    String type;
    String information;
    String description;
    String place;
    double latitude;
    double longitude;

    boolean up = false;
    boolean down = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_info_window);

        latitude = getIntent().getExtras().getDouble("Latitude");
        longitude = getIntent().getExtras().getDouble("Longitude");

       // numLikes = getIntent().getExtras().getInt("likes");
       // numDislikes = getIntent().getExtras().getInt("dislikes");

        name = getIntent().getStringExtra("Name");
        TextView nameString = findViewById(R.id.poiTitle);
        nameString.setText(name);

        information = getIntent().getStringExtra("information");
        String[] separated = information.split(";");
        id = separated[0];
        description = separated[1];
        type = separated[2];
        numLikes = Integer.parseInt(separated[3]);
        numDislikes = Integer.parseInt(separated[4]);

        TextView desc = findViewById(R.id.poiDescription);
        desc.setText(description);

        TextView t = findViewById(R.id.poiType);
        t.setText(type);

        TextView likeCounter = findViewById(R.id.likeCounter);
        likeCounter.setText(Integer.toString(numLikes));
        TextView dislikeCounter = findViewById(R.id.dislikeCounter);
        dislikeCounter.setText(Integer.toString(numDislikes));

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width*.8),(int)(height*.7));

        Intent returnIntent = getIntent();
        returnIntent.putExtra("id", id);
        setResult(Activity.RESULT_OK,returnIntent);
    }

    /**
     * Called every time the like button is clicked.
     */
    public void onLikeToggleClick(View view) {
        System.out.println("dfja'sfjefia'dlfnifae'nfieifadfneiafifnionf'eoiafefiadfnsnef;laeifnadiofneapfndainfiensfo;iaf;nf");

        ToggleButton likeBtn = findViewById(R.id.likeButton);
        ToggleButton dislikeBtn = findViewById(R.id.dislikeButton);

        TextView likeCounter = findViewById(R.id.likeCounter);
        TextView dislikeCounter = findViewById(R.id.dislikeCounter);

        /*
        Works in reverse because function called after you click/"check" the button.
        if button is already "checked" add a like
        otherwise remove one
         */
        if(likeBtn.isChecked()) {
            if(dislikeBtn.isChecked()){
                --numDislikes;
                dislikeBtn.setChecked(false);

                //Update the text to reflect correct number of likes.
                dislikeCounter.setText(Integer.toString(numDislikes));
                dislikeCounter.setTextColor(Color.parseColor("#FFFFFF"));
            }
            ++numLikes;

            //Update the text to reflect correct number of likes.
            likeCounter.setText(Integer.toString(numLikes));
            likeCounter.setTextColor(Color.parseColor("#FFD100"));
            Toast.makeText(this, "You liked this", Toast.LENGTH_SHORT).show();
        } else {
            --numLikes;

            //Update the text to reflect correct number of likes.
            likeCounter.setText(Integer.toString(numLikes));
            likeCounter.setTextColor(Color.parseColor("#FFFFFF"));
            Toast.makeText(this, "You removed your like", Toast.LENGTH_SHORT).show();
        }
        Intent returnIntent = getIntent();
        returnIntent.putExtra("likes", numLikes);
        returnIntent.putExtra("dislikes", numDislikes);
    }

    /**
     * Called every time the dislike button is clicked.
     */
    public void onDislikeToggleClick(View view) {


        ToggleButton likeBtn = findViewById(R.id.likeButton);
        ToggleButton dislikeBtn = findViewById(R.id.dislikeButton);

        TextView likeCounter = findViewById(R.id.likeCounter);
        TextView dislikeCounter = findViewById(R.id.dislikeCounter);

        /*
        Works in reverse for some reason
        if button is already "checked" add a like
        otherwise remove one
         */
        if(dislikeBtn.isChecked()) {
            if(likeBtn.isChecked()){
                --numLikes;
                likeBtn.setChecked(false);
                //Update the text to reflect correct number of likes.
                likeCounter.setText(Integer.toString(numLikes));
                likeCounter.setTextColor(Color.parseColor("#FFFFFF"));
            }
            ++numDislikes;

            //Update the text to reflect correct number of likes.
            dislikeCounter.setText(Integer.toString(numDislikes));
            dislikeCounter.setTextColor(Color.parseColor("#FFD100"));
            Toast.makeText(this, "You disliked this", Toast.LENGTH_SHORT).show();
        } else {
            --numDislikes;

            //Update the text to reflect correct number of likes.
            dislikeCounter.setText(Integer.toString(numDislikes));
            dislikeCounter.setTextColor(Color.parseColor("#FFFFFF"));
            Toast.makeText(this, "You removed your dislike", Toast.LENGTH_SHORT).show();
        }
        Intent returnIntent = getIntent();
        returnIntent.putExtra("likes", numLikes);
        returnIntent.putExtra("dislikes", numDislikes);
    }

    public void onFavoriteToggleClick(View view) {

        ToggleButton favBtn = findViewById(R.id.favoriteButton);

        /*
        Works in reverse because function called after you click/"check" the button.
        if button is already "checked" save to favorites
        otherwise remove it from favorites
         */
        if(favBtn.isChecked()) {
            //Save the POI as a favorite

            Toast.makeText(this, "Saved to Favorites", Toast.LENGTH_SHORT).show();
        } else {

            //Remove the POI from favorites
            Toast.makeText(this, "Removed from Favorites", Toast.LENGTH_SHORT).show();
        }


    }

    public void onReportToggleClick(View view) {

        ToggleButton reportBtn = findViewById(R.id.reportButton);

        /*
        Works in reverse because function called after you click/"check" the button.
        if button is already "checked" save to favorites
        otherwise remove it from favorites
         */
        if(reportBtn.isChecked()) {
            //Save the POI as a favorite

            Toast.makeText(this, "Reported bogus POI", Toast.LENGTH_SHORT).show();
        } else {

            //Remove the POI from favorites
            Toast.makeText(this, "Removed report", Toast.LENGTH_SHORT).show();
        }

    }

    public void onDirectionClick(View view) {

        Intent returnIntent = getIntent();

        returnIntent.putExtra("latit", latitude);
        returnIntent.putExtra("longit", longitude);

        setResult(Activity.RESULT_OK,returnIntent);
        finish();

        //Get the directions to the poi
        Toast.makeText(this, "Getting directions to POI", Toast.LENGTH_SHORT).show();
    }
}