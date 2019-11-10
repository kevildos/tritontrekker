package devdaryl.com.mapwithlogin;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import javax.annotation.Nullable;

/**
 * Created by User on 10/2/2017.
 */

public class MapActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MapActivity";
    private Button accountButton;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private DrawerLayout getmDrawerLayout;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (mNavigationView != null) {
            mNavigationView.setNavigationItemSelectedListener(this);
        }

//        accountButton = (Button) findViewById(R.id.nav_account);
//        accountButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                openLoginActivity();
//            }
//        });
    }

    public void openLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void openAddPOIActivity(){
        Intent intent = new Intent(this, AddPOI.class);
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
}
