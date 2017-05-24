package com.example.mosis.landmarksgo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mosis.landmarksgo.other.BitmapManipulation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

import static com.example.mosis.landmarksgo.MainActivity.localFileProfileImage;

public class PlayerInfo extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_info);

        String uid="";
        String firstname="";
        String lastname="";
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            uid = extras.getString("uid");
            firstname = extras.getString("firstname");
            lastname = extras.getString("lastname");
        }

        TextView tvName = (TextView) findViewById(R.id.textViewPlayerInfoName);
        tvName.setText(firstname + " " + lastname);

        TextView tvUid = (TextView) findViewById(R.id.textViewPlayerInfoUid);
        tvUid.setText("uid: " + uid);

        final ImageView iv = (ImageView) findViewById(R.id.imageViewPlayerInfo);

        try {
            localFileProfileImage = File.createTempFile(uid,".jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }

        StorageReference storage = FirebaseStorage.getInstance().getReference().child("profile_images/" + uid + ".jpg");
        storage.getFile(localFileProfileImage).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Bitmap bitmap = BitmapFactory.decodeFile(localFileProfileImage.getAbsolutePath());
                if(bitmap!=null){
                    bitmap = BitmapManipulation.getCroppedBitmap(bitmap);
                    iv.setImageBitmap(bitmap);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                //TODO: Can't display this, maybe user doesn't have a profile photo
            }
        });

    }
}
