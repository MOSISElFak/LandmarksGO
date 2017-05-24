package com.example.mosis.landmarksgo.highscore;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.mosis.landmarksgo.R;
import com.example.mosis.landmarksgo.other.BitmapManipulation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class HighScore extends AppCompatActivity {

    private DatabaseReference database;

    private ListView rankList;

    //private ArrayList<String> usernames = new ArrayList<>();

    ArrayList<DataModel> dataModels;
    ListView listView;
    private static CustomAdapter adapter;
    static int number=0;
    static boolean emptyList=false;
    ProgressBar progressBar;
    static int howManyTopPlayers=5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_high_score);

        progressBar = (ProgressBar) findViewById(R.id.progressBarHighscore);

        /*
        database = FirebaseDatabase.getInstance().getReference().child("scoreTable");
        rankList = (ListView) findViewById(R.id.rank_list);

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, usernames);
        rankList.setAdapter(arrayAdapter);

        Query topUsers = database.orderByChild("points").limitToLast(10);
        topUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                usernames.clear();
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    String value = postSnapshot.child("name").getValue(String.class);
                    value += ": " + postSnapshot.child("points").getValue(Integer.class).toString() + " points";
                    usernames.add(0,value);
                }
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(HighScore.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        */

        listView=(ListView)findViewById(R.id.rank_list);

        dataModels= new ArrayList<>();

        adapter= new CustomAdapter(dataModels,getApplicationContext());

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DataModel dataModel= dataModels.get(position);
                Toast.makeText(HighScore.this,"" + dataModel.getName(),Toast.LENGTH_SHORT).show();
            }
        });

        //>for better UX, show an empty list because loading from Server can take a few seconds, or there is no Internet connection
        for(int i=0;i<howManyTopPlayers;i++){
            dataModels.add(new DataModel("",0,null, ++i,0));
        }
        emptyList=true;
        adapter.notifyDataSetChanged();
        //<

        database = FirebaseDatabase.getInstance().getReference().child("scoreTable");
        rankList = (ListView) findViewById(R.id.rank_list);

        Query topUsers = database.orderByChild("points").limitToLast(howManyTopPlayers);
        topUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(emptyList){
                    emptyList=false;
                    adapter.clear();
                    progressBar.setVisibility(View.INVISIBLE);
                }
                for (final DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    //Log.d("KOMA","key u highscore:" + postSnapshot.getKey());

                    StorageReference storage = FirebaseStorage.getInstance().getReference().child("profile_images/" + postSnapshot.getKey() + ".jpg");
                    final long MEMORY = 10 * 1024 * 1024;

                    //first download friend's photo
                    storage.getBytes(MEMORY).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            bitmap = BitmapManipulation.getCroppedBitmap(bitmap);
                            //dataModels.add(new DataModel(postSnapshot.child("name").getValue(String.class),postSnapshot.child("points").getValue(Integer.class),bitmap, howManyTopPlayers-(number++),0));
                            dataModels.add(new DataModel(postSnapshot.child("name").getValue(String.class),postSnapshot.child("points").getValue(Integer.class),bitmap, number,0));

                            Collections.sort(dataModels, new Comparator<DataModel>(){
                                public int compare(DataModel obj1, DataModel obj2)
                                {
                                    return (obj1.getPoints() > obj2.getPoints()) ? -1: (obj1.getPoints() > obj2.getPoints()) ? 1:0 ;
                                }
                            });

                            adapter.notifyDataSetChanged();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            //user exists, but doesn't have profile photo
                            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.empty_profile_picture);
                            //dataModels.add(new DataModel(postSnapshot.child("name").getValue(String.class),postSnapshot.child("points").getValue(Integer.class),bitmap, howManyTopPlayers-(number++),0));
                            dataModels.add(new DataModel(postSnapshot.child("name").getValue(String.class),postSnapshot.child("points").getValue(Integer.class),bitmap, number,0)); //TODO: can't make it to show correct number

                            Collections.sort(dataModels, new Comparator<DataModel>(){
                                public int compare(DataModel obj1, DataModel obj2)
                                {
                                    return (obj1.getPoints() > obj2.getPoints()) ? -1: (obj1.getPoints() > obj2.getPoints()) ? 1:0 ;
                                }
                            });

                            adapter.notifyDataSetChanged();
                        }
                    });

                }
                //adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(HighScore.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
