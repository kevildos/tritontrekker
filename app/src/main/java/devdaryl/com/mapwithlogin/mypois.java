package devdaryl.com.mapwithlogin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class mypois extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypois);

        androidx.appcompat.widget.Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setElevation(1);

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_close);
        }

        ArrayList<String> idlist = getIntent().getStringArrayListExtra("idlist");
        ArrayList<String> namelist = getIntent().getStringArrayListExtra("namelist");

        ArrayAdapter adapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, namelist);

        ListView listView = (ListView)findViewById(R.id.list);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                int adapterindex = i;
                System.out.println("adapter index: " + adapterindex);
                AlertDialog.Builder builder = new AlertDialog.Builder(mypois.this);
                builder.setMessage("Delete POI \"" + adapter.getItem(i) + "\"?")
                .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        System.out.println("deleted D:");

                        FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
                        CollectionReference locsCollection = mFirestore.collection("locations");

                        locsCollection.whereEqualTo
                                ("name", namelist.get(adapterindex))
                                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if(task.isSuccessful()){
                                    String url = "";
                                    for(QueryDocumentSnapshot doc: task.getResult()){
                                        url = (String)doc.getData().get("photoURL");
                                    }

                                    System.out.println("URL OF DELETION" + url);
                                    StorageReference photoRef =
                                            FirebaseStorage.getInstance().getReferenceFromUrl(url);
                                    photoRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            System.out.println("finished deleting photo");
                                        }
                                    });
                                    locsCollection.document(idlist.get(adapterindex)).delete()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    Toast.makeText(getApplicationContext(),
                                                            "POI deleted",
                                                            Toast.LENGTH_SHORT).show();

                                                    adapter.remove(adapterindex);
                                                    namelist.remove(adapterindex);
                                                    idlist.remove(adapterindex);
                                                    listView.invalidateViews();
                                                    adapter.notifyDataSetChanged();
                                                }
                                            });


                                }
                            }
                        });


                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        System.out.println("not deleted :D");
                    }
                }).create().show();
            }
        });
    }

//    public void isPOIDeleted(boolean result){
//        isPOIDeleted = result;
//    }
}
//
//class dialog extends DialogFragment{
//    @Override
//    public Dialog onCreateDialog(Bundle savedInstanceState){
//
//
//
//        return builder.create();
//    }
//}
