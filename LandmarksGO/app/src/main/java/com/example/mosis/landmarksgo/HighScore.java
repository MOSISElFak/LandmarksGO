package com.example.mosis.landmarksgo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HighScore extends AppCompatActivity {

    private DatabaseReference database;

    private ListView rankList;

    private ArrayList<String> usernames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_high_score);

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
    }
}
