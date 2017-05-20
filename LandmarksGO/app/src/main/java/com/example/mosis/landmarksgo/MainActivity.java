package com.example.mosis.landmarksgo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.mosis.landmarksgo.authentication.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private static final String TAG = "LandmarksGO";
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //get firebase auth instance
        auth = FirebaseAuth.getInstance();

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        customizeUI();
    }

    private void customizeUI() {
        Log.d(TAG, "MainActivity:changeUI:photoUrl started");
        user = auth.getCurrentUser();
        if(user!=null) {
            Log.d(TAG, "MainActivity:changeUI: user!=null");

            View headerView = navigationView.getHeaderView(0);

            String displayName = user.getDisplayName();
            String email = user.getEmail();

            Log.d(TAG, "MainActivity:changeUI: displayName=" + displayName);
            TextView profileName = (TextView) headerView.findViewById(R.id.textViewProfileName);
            if(displayName!=null){
                profileName.setText(displayName);
            }else{
                //there is no displayName when users is signed in with an email
                profileName.setText("");
            }

            Log.d(TAG, "MainActivity:changeUI: email=" + email);
            if(email!=null){
                TextView profileEmail = (TextView) headerView.findViewById(R.id.textViewProfileEmail);
                profileEmail.setText(email);
            }

            Uri photoUrl = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl();
            Log.d(TAG, "MainActivity:changeUI: photoUrl=" + photoUrl);
            ImageView profilePicture = (ImageView) headerView.findViewById(R.id.imageViewProfilePicture);

            if(photoUrl==null){
                Glide.with(this).load(R.drawable.empty_profile_picture).into(profilePicture);
                //Glide.with(this).load("https://lintvwane.files.wordpress.com/2016/01/obama-guns_carr.jpg").into(profilePicture);
                //Bitmap too large to be uploaded into a texture (5428x3698, max=4096x4096)
                //We probably will probably get 50x50 pictures, so this won't be a problem
            }else{
                Glide.with(this).load(photoUrl).into(profilePicture);   //TODO: Test if this works. Add Facebook or Google as sign-in option
            }
            //TODO: Make picture round, not square.

            final float scale = getResources().getDisplayMetrics().density;
            int dpWidthInPx  = (int) (150 * scale);
            int dpHeightInPx = (int) (150 * scale);
            profilePicture.setMaxHeight(dpHeightInPx);
            profilePicture.setMaxWidth(dpWidthInPx);
        }
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

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        else if (id == R.id.action_addLandmark){
            Intent intent = new Intent(MainActivity.this, AddLandmark.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_map) {
            //Intent intent = new Intent(MainActivity.this,MainActivity.class);
            //startActivity(intent);
        } else if (id == R.id.nav_friends){
            Intent intent = new Intent(MainActivity.this,Friends.class);
            startActivity(intent);
        } else if (id == R.id.nav_highscore){
            Intent intent = new Intent(MainActivity.this,HighScore.class);
            startActivity(intent);
        } else if (id == R.id.nav_settings){
            Intent intent = new Intent(MainActivity.this,SettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_exit){
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
