package com.example.mosis.landmarksgo;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.example.mosis.landmarksgo.authentication.LoginActivity;
import com.example.mosis.landmarksgo.friends.Friends;
import com.example.mosis.landmarksgo.highscore.HighScore;
import com.example.mosis.landmarksgo.landmark.AddLandmark;
import com.example.mosis.landmarksgo.landmark.Landmark;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import static com.example.mosis.landmarksgo.R.id.map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemSelectedListener, OnMapReadyCallback {

    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private StorageReference storage;

    private static final String TAG = "LandmarksGO";
    private NavigationView navigationView;

    private GoogleMap mMap;
    private HashMap<String, Marker> mapMarkers = new HashMap<String, Marker>();

    private int spinnerSelectedSearchOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //get firebase auth instance
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        //get storage reference
        storage = FirebaseStorage.getInstance().getReference().child("profile_images/" + user.getUid() + ".jpg");

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
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                Intent intent = new Intent(MainActivity.this, AddLandmark.class);
                startActivity(intent);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        //Navigation Drawer
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Google Maps
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(map);
        mapFragment.getMapAsync(this);

        Spinner spinner = (Spinner) findViewById(R.id.spinnerMapSearchCategory);
        spinner.setOnItemSelectedListener(this);

        customizeUI();

        setUpSearchView();

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
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_map) {
            //Intent intent = new Intent(MainActivity.this,MapsActivity.class);
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

    ///////////////////////////////////////////////////////

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
            final ImageView profilePicture = (ImageView) headerView.findViewById(R.id.imageViewProfilePicture);

            /*if(photoUrl==null){
                Glide.with(this).load(R.drawable.empty_profile_picture).into(profilePicture);
                //Glide.with(this).load("https://lintvwane.files.wordpress.com/2016/01/obama-guns_carr.jpg").into(profilePicture);
                //Bitmap too large to be uploaded into a texture (5428x3698, max=4096x4096)
                //We probably will probably get 50x50 pictures, so this won't be a problem
            }else{
                Glide.with(this).load(photoUrl).into(profilePicture);   //TODO: Test if this works. Add Facebook or Google as sign-in option
            }*/
            //TODO: Make picture round, not square.

            final long ONE_MEGABYTE = 1024 * 1024;

            //download file as a byte array
            storage.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    profilePicture.setImageBitmap(bitmap);
                }
            });

            final float scale = getResources().getDisplayMetrics().density;
            int dpWidthInPx  = (int) (150 * scale);
            int dpHeightInPx = (int) (150 * scale);
            profilePicture.setMaxHeight(dpHeightInPx);
            profilePicture.setMaxWidth(dpWidthInPx);
        }

        //Spinner for search
        Spinner spinner = (Spinner) findViewById(R.id.spinnerMapSearchCategory);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.search_type, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
    }

    //for Search above map
    private void setUpSearchView() {
        SearchView search=(SearchView) findViewById(R.id.searchViewMap);
        search.setQueryHint("");

        search.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                //Toast.makeText(getBaseContext(), "onFocusChange: " + String.valueOf(hasFocus), Toast.LENGTH_SHORT).show();
            }
        });

        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //Toast.makeText(getBaseContext(), "onQueryTextSubmit: " + query, Toast.LENGTH_SHORT).show();
                searchMarker(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Toast.makeText(getBaseContext(), "onQueryTextChange: " + newText, Toast.LENGTH_SHORT).show();
                searchMarker(newText);
                return false;
            }
        });
    }

    private void searchMarker(String query) {
        Log.d(TAG, "MainActivity: searchMarker: searching for " + query);
        Log.d(TAG, "MainActivity: searchMarker: spinnerSelectedSearchOption=" + spinnerSelectedSearchOption);
        Marker mMarker = null;
        if(spinnerSelectedSearchOption==0){ //searching for name
            mMarker = mapMarkers.get(query);
        }

        if(mMarker!=null){
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(mMarker.getPosition().latitude, mMarker.getPosition().longitude)));
            mMarker.showInfoWindow();
            //TODO: Add smooth animation

            //Force hide the onscreen keyboard
            //InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            //imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        };
        Log.d(TAG, "MainActivity: searchMarker: found " + mMarker);
    }

    //for Spinner above map
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String arr[] = getResources().getStringArray(R.array.search_type);
        //Toast.makeText(this, "Searching: " + arr[position], Toast.LENGTH_SHORT).show();
        spinnerSelectedSearchOption = position;
    }
    //for Spinner above map
    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    //Google Maps
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                Toast.makeText(getApplicationContext(), point.toString(), Toast.LENGTH_SHORT).show();
            }
        });

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(this,"Please grant all permissions",Toast.LENGTH_SHORT).show();
            return;
        }
        mMap.setMyLocationEnabled(true);

        Runnable r = new Runnable() {
            @Override
            public void run() {
                loadLandmarksFromServer();
            }
        };
        Thread loadLandmarksFromServerThread = new Thread(r);
        loadLandmarksFromServerThread.start();

    }

    private void loadLandmarksFromServer() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("landmarks");

        //https://firebase.google.com/docs/database/android/lists-of-data
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                Landmark landmark = dataSnapshot.getValue(Landmark.class);
                Log.d(TAG, "onChildAdded:" + landmark.title);
                addMarkers(landmark.lat, landmark.lon, landmark.title, false);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());
                //We don't have a ability to change a landmark
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());
                //We don't have a ability to change a landmark
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());
                //We don't have a ability to move a landmark in DB.
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "postComments:onCancelled", databaseError.toException());
                Toast.makeText(MainActivity.this, "Failed to load comments.", Toast.LENGTH_SHORT).show();
            }
        };
        myRef.addChildEventListener(childEventListener);

    }

    private void addMarkers(double lat, double lng, String title, boolean moveCamera){
        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(lat, lng))
                .title(title)
                .snippet("Latitude:" + lat + " " + "Longitude:" + lng + " ")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        if(moveCamera){
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lng)));
        }
        //Add to searchable HashMap
        mapMarkers.put(title, marker);
    }

}
