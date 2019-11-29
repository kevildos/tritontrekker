package devdaryl.com.mapwithlogin;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;


public class imageDialog extends DialogFragment {

    private CharSequence[] items = {"Camera", "Choose from Gallery"};

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context mContext = getContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Pick an Option");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // The 'which' argument contains the index position
                // of the selected item

                // Camera is the first option
                if(which == 0){
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivity(cameraIntent);
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
        });
        return builder.create();
    }
}
