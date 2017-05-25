package com.example.mosis.landmarksgo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.mosis.landmarksgo.authentication.LoginActivity;
import com.example.mosis.landmarksgo.authentication.SignupActivity;
import com.example.mosis.landmarksgo.authentication.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private Button btnChangeEmail, btnChangePassword, btnRemoveUser, btnAbout, btnSave, btnChangePhoto,
            changeEmail, changePassword, camera, gallery, signOut;
    private CheckBox work_check, players_check, friends_check;
    private EditText newEmail, newPassword;
    private ProgressBar progressBar;
    private Spinner gpsSpinner;
    private LinearLayout photoLayout;
    
    private Integer gpsRefresh;
    private Boolean friends_status, players_status, workback_status;
    private Uri savedURI;

    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseDatabase database;
    private StorageReference storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //get firebase auth instance
        auth = FirebaseAuth.getInstance();

        //get current user
        user = auth.getCurrentUser();

        //get database instance
        database = FirebaseDatabase.getInstance();

        //get storage reference
        storage = FirebaseStorage.getInstance().getReference().child("profile_images/" + user.getUid() + ".jpg");

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };

        btnSave = (Button) findViewById(R.id.save_button);
        btnChangeEmail = (Button) findViewById(R.id.change_email_button);
        btnChangePassword = (Button) findViewById(R.id.change_password_button);
        btnRemoveUser = (Button) findViewById(R.id.remove_user_button);
        btnChangePhoto = (Button) findViewById(R.id.changePhoto);
        btnAbout = (Button) findViewById(R.id.about);
        changeEmail = (Button) findViewById(R.id.changeEmail);
        changePassword = (Button) findViewById(R.id.changePass);
        camera = (Button) findViewById(R.id.camera);
        gallery = (Button) findViewById(R.id.gallery);
        signOut = (Button) findViewById(R.id.sign_out);

        newEmail = (EditText) findViewById(R.id.new_email);
        newPassword = (EditText) findViewById(R.id.newPassword);

        work_check = (CheckBox) findViewById(R.id.workback);
        players_check = (CheckBox) findViewById(R.id.showplayers);
        friends_check = (CheckBox) findViewById(R.id.showfriends);

        gpsSpinner = (Spinner) findViewById(R.id.gps_spinner);
        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.gps_refresh_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gpsSpinner.setAdapter(adapter);
        gpsSpinner.setOnItemSelectedListener(this);

        photoLayout = (LinearLayout) findViewById(R.id.upload_photo_layout);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        hideAllInputs();

        // OVDE CITAM SETTINGS IZ BAZE
        database.getReference("users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User u = dataSnapshot.getValue(User.class);
                friends_status = u.showfriends;
                players_status = u.showplayers;
                workback_status = u.workback;
                gpsRefresh = u.gpsrefresh;

                friends_check.setChecked(friends_status);
                players_check.setChecked(players_status);
                work_check.setChecked(workback_status);
                int pos = adapter.getPosition(gpsRefresh.toString());
                gpsSpinner.setSelection(pos);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(SettingsActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                database.getReference("users").child(user.getUid()).child("workback").setValue(work_check.isChecked());
                database.getReference("users").child(user.getUid()).child("showplayers").setValue(players_check.isChecked());
                database.getReference("users").child(user.getUid()).child("showfriends").setValue(friends_check.isChecked());
                database.getReference("users").child(user.getUid()).child("gpsrefresh").setValue(gpsRefresh);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(SettingsActivity.this, "Settings saved", Toast.LENGTH_SHORT).show();

                Snackbar.make(findViewById(android.R.id.content), "Please exit the app in order to apply the settings about showing players or friends", Snackbar.LENGTH_LONG).show();
            }
        });

        btnChangeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideAllInputs();
                newEmail.setVisibility(View.VISIBLE);
                changeEmail.setVisibility(View.VISIBLE);
            }
        });

        changeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                if (user != null && !newEmail.getText().toString().trim().equals("")) {
                    user.updateEmail(newEmail.getText().toString().trim())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(SettingsActivity.this, "Email address is updated. Please sign in with new email!", Toast.LENGTH_LONG).show();
                                        signOut();
                                        progressBar.setVisibility(View.GONE);
                                    } else {
                                        Toast.makeText(SettingsActivity.this, "Failed to update email!", Toast.LENGTH_LONG).show();
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }
                            });
                } else if (newEmail.getText().toString().trim().equals("")) {
                    newEmail.setError("Enter email");
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideAllInputs();
                newPassword.setVisibility(View.VISIBLE);
                changePassword.setVisibility(View.VISIBLE);
            }
        });

        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                if (user != null && !newPassword.getText().toString().trim().equals("")) {
                    if (newPassword.getText().toString().trim().length() < 6) {
                        newPassword.setError("Password too short, enter minimum 6 characters");
                        progressBar.setVisibility(View.GONE);
                    } else {
                        user.updatePassword(newPassword.getText().toString().trim())
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(SettingsActivity.this, "Password is updated, sign in with new password!", Toast.LENGTH_SHORT).show();
                                            signOut();
                                            progressBar.setVisibility(View.GONE);
                                        } else {
                                            Toast.makeText(SettingsActivity.this, "Failed to update password!", Toast.LENGTH_SHORT).show();
                                            progressBar.setVisibility(View.GONE);
                                        }
                                    }
                                });
                    }
                } else if (newPassword.getText().toString().trim().equals("")) {
                    newPassword.setError("Enter password");
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        btnChangePhoto.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                hideAllInputs();
                photoLayout.setVisibility(View.VISIBLE);
            }
        });
        camera.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent imageIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

                File imagesFolder = new File(Environment.getExternalStorageDirectory(), "WorkingWithPhotosApp");
                imagesFolder.mkdirs();

                File image = new File(imagesFolder, "QR_1.png");
                savedURI = Uri.fromFile(image);

                imageIntent.putExtra(MediaStore.EXTRA_OUTPUT, savedURI);

                startActivityForResult(imageIntent, 0);
            }
        });
        gallery.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(pickPhoto , 1);
            }
        });

        btnRemoveUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                progressBar.setVisibility(View.VISIBLE);
                                if (user != null) {
                                    onAccDelete(user.getUid());
                                    user.delete()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(SettingsActivity.this, "Your profile is deleted! Create new account now!", Toast.LENGTH_SHORT).show();
                                                        startActivity(new Intent(SettingsActivity.this, SignupActivity.class));
                                                        finish();
                                                        progressBar.setVisibility(View.GONE);
                                                    } else {
                                                        Toast.makeText(SettingsActivity.this, "Failed to delete your account!", Toast.LENGTH_SHORT).show();
                                                        progressBar.setVisibility(View.GONE);
                                                    }
                                                }
                                            });
                                }
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                dialog.dismiss();
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                builder.setTitle("Warning!").setMessage("Are you sure you want to delete your account?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
            }
        });

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        btnAbout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                builder.setMessage("App made by Koma & Ugre\nmail@outlook.com \nmail@gmail.com")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    //sign out method
    public void signOut() {
        auth.signOut();
    }

    // funkcija koja skriva sve dugmice i to tako
    private void hideAllInputs()
    {
        newEmail.setVisibility(View.GONE);
        changeEmail.setVisibility(View.GONE);

        newPassword.setVisibility(View.GONE);
        changePassword.setVisibility(View.GONE);

        photoLayout.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }

    private void onAccDelete(String userid)
    {
        // Ovde se brisu svi njegovi podaci iz baze
        storage.delete();
        database.getReference("scoreTable").child(userid).removeValue();
        database.getReference("users").child(userid).removeValue();
        // TODO izbrisi ga i iz prijateljskih veza kasnije
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        this.gpsRefresh = Integer.parseInt(parent.getItemAtPosition(position).toString());
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0 || requestCode == 1)
        {
            if (resultCode == RESULT_OK) {
                progressBar.setVisibility(View.VISIBLE);
                if (data != null)
                    savedURI = data.getData();
                /*UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setPhotoUri(selectedImage).build();
                user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(SettingsActivity.this, "Profile picture updated!", Toast.LENGTH_SHORT).show();
                    }
                });*/

                storage.putFile(savedURI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(SettingsActivity.this, "Profile picture updated!", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SettingsActivity.this, "Failed to upload picture, please try again!", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
            else {
                Toast.makeText(this, "Action canceled!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
