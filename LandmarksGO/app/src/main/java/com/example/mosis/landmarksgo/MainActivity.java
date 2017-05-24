package com.example.mosis.landmarksgo;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
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

import com.example.mosis.landmarksgo.authentication.LoginActivity;
import com.example.mosis.landmarksgo.authentication.User;
import com.example.mosis.landmarksgo.friends.Friends;
import com.example.mosis.landmarksgo.highscore.HighScore;
import com.example.mosis.landmarksgo.landmark.AddLandmark;
import com.example.mosis.landmarksgo.landmark.Landmark;
import com.example.mosis.landmarksgo.other.BitmapManipulation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

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
    private HashMap<String, Marker> mapMarkersLandmarks = new HashMap<String, Marker>();
    private HashMap<String, Marker> mapUseridMarker = new HashMap<String, Marker>();
    private HashMap<Marker, User> mapMarkerUser = new HashMap<Marker, User>();

    private int spinnerSelectedSearchOption;
    static File localFileProfileImage = null;

    private Marker myLocation = null;

    public static final int MARKER_LANDMARK = 1;
    public static final int MARKER_USER = 2;

    private static Bitmap profilePhotoBitmap=null;
    private static View headerView;
    private static ImageView profilePicture;

    private static boolean settingsShowPlayers;
    private static boolean settingsShowFriends;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

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

        if(user!=null){
            setUpLayout();

            //Google Maps
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(map);
            mapFragment.getMapAsync(this);

            //storage = FirebaseStorage.getInstance().getReference().child("profile_images/" + user.getUid() + ".jpg");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if(user!=null){
            customizeUI();
            readSettingsFromServer();
        }
        //if(mMap!=null)
            //mMap.clear(); //TODO: should this be here?
    }

    private void readSettingsFromServer() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User u = dataSnapshot.getValue(User.class);
                settingsShowFriends = u.showfriends;
                settingsShowPlayers = u.showplayers;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
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

        if (id == R.id.action_sendfriendrequest) {
            //TODO: temporary
            //DatabaseReference dbRef = database.getReference("friends/");

            //dbRef.push().setValue(friendship);
            //dbRef.child(myUid).setValue(getRandomFriendship());
            //root.child(user.getUid()).setValue(friendship);

            //Query phoneQuery = dbRef.orderByChild(myUid).equalTo(myUid);
            //Query phoneQuery = dbRef.equalTo(myUid);

            pushRandomFriendships(user.getUid());
            return true;
        }

        if (id==R.id.action_getfriends){
            Log.d(TAG,"My myUid:" + user.getUid());
            getFriends();
        }
        return super.onOptionsItemSelected(item);
    }

    private void getFriends() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference dbRef = database.getReference("friends/" + user.getUid());
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG,"u valueevent listener: count " + dataSnapshot.getChildrenCount());
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    String json = singleSnapshot.toString();
                    Log.d(TAG,"json: " + json);

                    //TODO: deserialize via class, not like this
                    String friendUid = json.substring(json.indexOf("value = ") + 8);
                    Log.d(TAG,"friendUid: " + friendUid);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled", databaseError.toException());
            }
        });
    }

    @NonNull
    private String getRandomFirebaseUid() {
        String randomUid = UUID.randomUUID().toString();
        randomUid = randomUid.replaceAll("-", "");
        randomUid = randomUid.substring(0, 28);
        return randomUid;
    }

    private List<String> getRandomFriendship(){
        Random ran = new Random();
        int x = ran.nextInt(6) + 1;
        List<String> friendsList = new ArrayList<>();
        for(int i=0;i<x;i++){
            friendsList.add(getRandomFirebaseUid());
        }
        //return new Friendship(user.getUid(), friendsList);
        return friendsList;
    }

    private void pushRandomFriendships(String uid){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference dbRef = database.getReference("friends/");
        dbRef.child(uid).setValue(getRandomFriendship());
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
            moveTaskToBack(true);
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
            //TODO: Stop background service .
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    ///////////////////////////////////////////////////////

    private void setUpLayout() {
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddLandmark.class);
                startActivity(intent);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        //Navigation Drawer
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Search above map
        Spinner spinner = (Spinner) findViewById(R.id.spinnerMapSearchCategory);
        spinner.setOnItemSelectedListener(this);

        setUpSearchView();
    }

    private void customizeUI() {
        Log.d(TAG, "MainActivity:changeUI:photoUrl started");
        user = auth.getCurrentUser();
        if(user!=null) {
            Log.d(TAG, "MainActivity:changeUI: user!=null");

            headerView = navigationView.getHeaderView(0);

            String displayName = user.getDisplayName();
            String email = user.getEmail();

            Log.d(TAG, "MainActivity:changeUI: displayName=" + displayName);
            final TextView profileName = (TextView) headerView.findViewById(R.id.textViewProfileName);
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

            profilePicture = (ImageView) headerView.findViewById(R.id.imageViewProfilePicture);
            changeProfilePhoto(headerView, profilePicture);
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

    private void changeProfilePhoto(View headerView, final ImageView iv) {
        //TODO: Save file app folder, load that file first then download.
        Uri photoUrl = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl();
        Log.d(TAG, "MainActivity:changeUI: photoUrl=" + photoUrl);

        try {
            localFileProfileImage = File.createTempFile("profileImage",".jpg");
            Log.d(TAG,"localFile.getAbsolutePath()" + localFileProfileImage.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        storage = FirebaseStorage.getInstance().getReference().child("profile_images/" + user.getUid() + ".jpg");
        storage.getFile(localFileProfileImage).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Bitmap bitmap = BitmapFactory.decodeFile(localFileProfileImage.getAbsolutePath());
                if(bitmap!=null){
                    Log.d(TAG,"Bitmap is NOT null");
                    bitmap = BitmapManipulation.getCroppedBitmap(bitmap);
                    iv.setImageBitmap(bitmap);
                }else{
                    Log.d(TAG,"Bitmap is null");
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                //Toast.makeText(MainActivity.this, "Error downloading/saving profile image", Toast.LENGTH_SHORT).show();
                //TODO: Can't display this, maybe user doesn't have a profile photo
            }
        });
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
            mMarker = mapMarkersLandmarks.get(query);
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

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                //Toast.makeText(getApplicationContext(), marker.getTitle(), Toast.LENGTH_SHORT).show();
                User user = null;
                user = mapMarkerUser.get(marker);
                if(user!=null){
                    Intent intent = new Intent(MainActivity.this, PlayerInfo.class);
                    intent.putExtra("uid",user.uid);
                    intent.putExtra("firstname",user.firstName);
                    intent.putExtra("lastname",user.lastName);
                    startActivity(intent);
                }

                return false;
            }
        });

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

        //TODO: Make this better
        int height = 50;
        int width = 50;
        BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.obama);
        Bitmap b=bitmapdraw.getBitmap();
        final Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

        if (mMap != null) {
            mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                @Override
                public void onMyLocationChange(Location arg0) {
                    //mMap.addMarker(new MarkerOptions().position(new LatLng(arg0.getLatitude(), arg0.getLongitude())).title("It's Me!"));
                    if(myLocation!=null){
                        myLocation.remove();
                    }

                    //addMarkers(arg0.getLatitude(),arg0.getLongitude(),"I","", smallMarker, false, MARKER_USER);

                    DatabaseReference users = FirebaseDatabase.getInstance().getReference("users");
                    users.child(user.getUid()).child("lat").setValue(arg0.getLatitude());
                    users.child(user.getUid()).child("lon").setValue(arg0.getLongitude());
                }
            });
        }

        Runnable r = new Runnable() {
            @Override
            public void run() {
                loadLandmarksFromServer();
            }
        };
        Thread loadLandmarksFromServerThread = new Thread(r);
        loadLandmarksFromServerThread.start();

        Runnable r2 = new Runnable() {
            @Override
            public void run() {
                loadAllPlayersFromServer();
            }
        };
        Thread loadAllPlayersFromServerThread = new Thread(r2);
        loadAllPlayersFromServerThread.start();
    }

    private void loadLandmarksFromServer() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("landmarks");

        //https://firebase.google.com/docs/database/android/lists-of-data
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                //Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());
                Landmark landmark = dataSnapshot.getValue(Landmark.class);
                Log.d(TAG, "onChildAdded:" + landmark.title);
                Marker marker = addMarkers(landmark.lat, landmark.lon, landmark.title, "", null, false, MARKER_LANDMARK);

                //Add to searchable HashMap
                mapMarkersLandmarks.put(landmark.title, marker);
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
            }
        };
        myRef.addChildEventListener(childEventListener);
    }

    private void loadAllPlayersFromServer() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users");

        //https://firebase.google.com/docs/database/android/lists-of-data
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                if(settingsShowPlayers){
                    //Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());
                    final User user = dataSnapshot.getValue(User.class);
                    Log.d(TAG, "onChildAdded:" + user.firstName + " uid:" + user.uid);

                    Marker marker = addMarkers(user.lat, user.lon, user.firstName + " " + user.lastName, "", null, false, MARKER_USER);
                    mapUseridMarker.put(user.uid, marker);
                    mapMarkerUser.put(marker, user);
                }
        }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                //Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());
                User user = dataSnapshot.getValue(User.class);
                //Log.d(TAG, "onChildChanged:" + user.firstName + " uid:" + user.uid);

                Marker mMarker;
                mMarker = mapUseridMarker.get(user.uid);

                if(mMarker!=null) {
                    Log.d(TAG,"Brisem marker");
                    mMarker.remove();
                    Marker marker = addMarkers(user.lat, user.lon, user.firstName + " " + user.lastName, null, null, false, MARKER_USER);

                    //Add to searchable HashMap
                    mapUseridMarker.remove(user.uid);
                    mapUseridMarker.put(user.uid, marker);

                    mapMarkerUser.put(marker, user);    //TODO: remove previous marker
                }else{
                    Log.d(TAG,"Ne brisem marker");
                }

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "postComments:onCancelled", databaseError.toException());
            }
        };
        myRef.addChildEventListener(childEventListener);
    }

    private Marker addMarkers(double lat, double lng, String title, String snippet, Bitmap icon, boolean moveCamera, int type){
        Marker marker = null;

        MarkerOptions mo = new MarkerOptions();
        mo.position(new LatLng(lat, lng));
        mo.title(title);
        if(snippet!=null && snippet!=""){
            mo.snippet(snippet);
        }
        if(type==MARKER_LANDMARK){
            mo.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        };
        if(type==MARKER_USER) {
            //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            //.icon(BitmapDescriptorFactory.fromBitmap(BitmapManipulation.getMarkerBitmapFromView(icon, MainActivity.this)))); //of course, this takes too much time to process
            mo.icon(BitmapDescriptorFactory.fromBitmap(BitmapManipulation.getMarkerBitmapFromView(R.drawable.person, MainActivity.this)));
        }

        marker = mMap.addMarker(mo);

        if(moveCamera){
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lng)));
            // Zoom in the Google Map
            //mMap.animateCamera(CameraUpdateFactory.zoomTo(5));
        }
        return marker;
    }
}
