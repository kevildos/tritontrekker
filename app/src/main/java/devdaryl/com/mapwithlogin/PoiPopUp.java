package devdaryl.com.mapwithlogin;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.net.URL;
import com.google.android.gms.maps.model.LatLng;


import devdaryl.com.mapwithlogin.R;

public class PoiPopUp extends AppCompatActivity {

    int numLikes = 0;
    int numDislikes = 0;
    long reports = 0;
    String name;
    String id;
    boolean favorite;
    String type;
    String information;
    String description;
    String place;
    double latitude;
    double longitude;

    boolean liked = false;
    boolean disliked = false;
    boolean favorited = false;

    final long REPORT_THRESHOLD = 5;

    FirebaseFirestore mFirebase = FirebaseFirestore.getInstance();


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
        liked = Boolean.parseBoolean(separated[5]);
        reports = (long)getIntent().getExtras().get("reports");

        if(liked) {
            ToggleButton likeBtn = findViewById(R.id.likeButton);
            TextView likeCounter = findViewById(R.id.likeCounter);

            likeCounter.setTextColor(Color.parseColor("#FFD100"));
            likeBtn.setChecked(true);
        }
        disliked = Boolean.parseBoolean(separated[6]);
        if(disliked) {
            ToggleButton disLikeBtn = findViewById(R.id.dislikeButton);
            TextView dislikeCounter = findViewById(R.id.dislikeCounter);
            dislikeCounter.setTextColor(Color.parseColor("#FFD100"));
            disLikeBtn.setChecked(true);
        }
        favorited = Boolean.parseBoolean(separated[7]);
        if(favorited) {
            ToggleButton favB = findViewById(R.id.favoriteButton);
            favB.setChecked(true);
        }

        Toast.makeText(this, liked + "liked" + disliked + "disliked" + favorited + "favorited", Toast.LENGTH_LONG).show();

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

        ImageView image = findViewById(R.id.imageView);
        System.out.println("Image url in poit pop up" +
                (String)getIntent().getExtras().get("imageurl"));
        String imageurl = (String)getIntent().getExtras().get("imageurl");

        try {
            Picasso.get().load(imageurl).into(image);
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), "Coulndnt load image",
                    Toast.LENGTH_SHORT).show();
        }

        Intent returnIntent = getIntent();
        returnIntent.putExtra("id", id);
        returnIntent.putExtra("likes", numLikes);
        returnIntent.putExtra("dislikes", numDislikes);
        returnIntent.putExtra("liked", liked);
        returnIntent.putExtra("disliked", disliked);
        returnIntent.putExtra("favorited", favorited);
        returnIntent.putExtra("deletedpoi", false);
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
            disliked = false;
            liked = true;

            //Update the text to reflect correct number of likes.
            likeCounter.setText(Integer.toString(numLikes));
            likeCounter.setTextColor(Color.parseColor("#FFD100"));
            Toast.makeText(this, "You liked this", Toast.LENGTH_SHORT).show();
        } else {
            --numLikes;
            disliked = false;
            liked = false;

            //Update the text to reflect correct number of likes.
            likeCounter.setText(Integer.toString(numLikes));
            likeCounter.setTextColor(Color.parseColor("#FFFFFF"));
            Toast.makeText(this, "You removed your like", Toast.LENGTH_SHORT).show();
        }
        Intent returnIntent = getIntent();
        returnIntent.putExtra("likes", numLikes);
        returnIntent.putExtra("dislikes", numDislikes);
        returnIntent.putExtra("liked", liked);
        returnIntent.putExtra("disliked", disliked);

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
                liked = false;
                likeBtn.setChecked(false);
                //Update the text to reflect correct number of likes.

                likeCounter.setText(Integer.toString(numLikes));
                likeCounter.setTextColor(Color.parseColor("#FFFFFF"));
            }
            ++numDislikes;
            disliked = true;

            //Update the text to reflect correct number of likes.
            dislikeCounter.setText(Integer.toString(numDislikes));
            dislikeCounter.setTextColor(Color.parseColor("#FFD100"));
            Toast.makeText(this, "You disliked this", Toast.LENGTH_SHORT).show();
        } else {
            --numDislikes;
            disliked = false;
            liked = false;

            //Update the text to reflect correct number of likes.
            dislikeCounter.setText(Integer.toString(numDislikes));
            dislikeCounter.setTextColor(Color.parseColor("#FFFFFF"));
            Toast.makeText(this, "You removed your dislike", Toast.LENGTH_SHORT).show();
        }
        Intent returnIntent = getIntent();
        returnIntent.putExtra("likes", numLikes);
        returnIntent.putExtra("dislikes", numDislikes);
        returnIntent.putExtra("liked", liked);
        returnIntent.putExtra("disliked", disliked);
        returnIntent.putExtra("deletedpoi", false);
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
            favorited = true;

            Toast.makeText(this, "Saved to Favorites", Toast.LENGTH_SHORT).show();
        } else {
            favorited = false;

            //Remove the POI from favorites
            Toast.makeText(this, "Removed from Favorites", Toast.LENGTH_SHORT).show();
        }
        Intent returnIntent = getIntent();
        returnIntent.putExtra("favorited", favorited);
    }

    public void onReportToggleClick(View view) {

        ToggleButton reportBtn = findViewById(R.id.reportButton);
        System.out.println("Reports for poi: " + reports);
        /*
        Works in reverse because function called after you click/"check" the button.
        if button is already "checked" save to favorites
        otherwise remove it from favorites
         */
        if (reportBtn.isChecked()) {
            //Save the POI as a favorite

            Toast.makeText(this, "Reported bogus POI", Toast.LENGTH_SHORT).show();

            if (reports + 1 == REPORT_THRESHOLD) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PoiPopUp.this);
                builder.setMessage("Reporting this POI will delete it for all users because " +
                        "it will exceed the report count. Are you sure you want to report?");
                builder.setPositiveButton("YES - DELETE POI", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        mFirebase.collection("locations").document(id).delete()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(getApplicationContext(), "POI Deleted",
                                                Toast.LENGTH_SHORT);

                                        Intent returnIntent = getIntent();
                                        returnIntent.putExtra("deletedpoi", true);
                                        finish();
                                    }
                                });
                    }
                });
                builder.setNegativeButton("NO - UNDO REPORT", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        reportBtn.setChecked(false);
                    }
                });
                builder.create().show();
            } else {
                reports = reports++;
            }
        } else {

            //Remove the POI from favorites
            Toast.makeText(this, "Removed report", Toast.LENGTH_SHORT).show();
            reports = reports--;
        }
    }


    public void onDirectionClick(View view) {

        Intent returnIntent = getIntent();

        returnIntent.putExtra("latit", latitude);
        returnIntent.putExtra("longit", longitude);
        returnIntent.putExtra("reports", reports);
        returnIntent.putExtra("deletedpoi", false);


        setResult(Activity.RESULT_OK,returnIntent);
        finish();

        //Get the directions to the poi
        Toast.makeText(this, "Getting directions to POI", Toast.LENGTH_SHORT).show();
    }
}