package devdaryl.com.tt;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Telephony;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ComponentActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.net.URI;

import static android.app.Activity.RESULT_OK;

public class imageDialog extends DialogFragment {

    private CharSequence[] items = {"Camera", "Choose from Gallery"};

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context mContext = getContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Pick an Option");
        String imageFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/picture.jpg";
        File imageFile = new File(imageFilePath);


        Uri imageFileUri = Uri.fromFile(imageFile); // convert path to Uri
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // The 'which' argument contains the index position
                // of the selected item

                // Camera is the first option
                if(which == 0){

                    Uri imageUri;
                    File photo = null;
                    Activity activity = getActivity();
                    if(getActivity().checkSelfPermission(Manifest.permission.CAMERA)
                            == PackageManager.PERMISSION_GRANTED){
                        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        Uri outputFileUri = null;
                        try{
                            photo = this.createTempFile("picture", ".jpg");
                        } catch(Exception e){
                            Toast.makeText(getActivity(), "Cant make temp dir", Toast.LENGTH_LONG);
                        }

//                        imageUri = Uri.fromFile(photo);
                        imageUri = FileProvider.getUriForFile(getContext(),
                                getContext().getApplicationContext().getPackageName()+".provider",
                                photo);


                        System.out.println("imageUri: " + imageUri.toString());
                        cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

                        Bundle bundle = new Bundle();
                        bundle.putString("imageuri", imageUri.toString());
                        ((AddPOI)getActivity()).getUri(imageUri);

                        getActivity().startActivityForResult(cameraIntent, 2000);
                    } else{
                        requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);

                    }
                }

                // Gallery is the second option
                else if (which == 1){
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto , 1);
                }
                else{
                    // this shouldnt be running so idk
                }
            }

            private File createTempFile(String part, String ext) throws IOException{
                File tempdir = Environment.getExternalStorageDirectory();
                tempdir = new File(tempdir.getAbsolutePath()+"/.temp/");
                if(!tempdir.exists()){
                    tempdir.mkdirs();
                }
                return File.createTempFile(part, ext ,tempdir);
            }
        });
        return builder.create();


    }



}
