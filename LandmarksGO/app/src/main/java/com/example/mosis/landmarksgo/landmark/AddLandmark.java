package com.example.mosis.landmarksgo.landmark;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.mosis.landmarksgo.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddLandmark extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Spinner object_type;
    private Button btnPickFromMap, btnAdd, btnCancel;
    private EditText editName, editDesc, editLon, editLat;

    private FirebaseDatabase database;
    private DatabaseReference root;

    private String type = "Monument";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_landmark);

        //Connecting to database
        database = FirebaseDatabase.getInstance();
        root = database.getReference("landmarks");

        object_type = (Spinner) findViewById(R.id.object_type);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.add_landmark_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        object_type.setAdapter(adapter);
        object_type.setOnItemSelectedListener(this);

        btnPickFromMap = (Button) findViewById(R.id.pick_from_map);
        btnAdd = (Button) findViewById(R.id.add_landmark);
        btnCancel = (Button) findViewById(R.id.cancel_landmark);

        editName = (EditText) findViewById(R.id.landmark_name);
        editDesc = (EditText) findViewById(R.id.landmark_desc);
        editLat = (EditText) findViewById(R.id.landmark_lat);
        editLon = (EditText) findViewById(R.id.landmark_lon);

        btnPickFromMap.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //TODO da mu otvori mapu gde ce da izabere kordinate
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String name = editName.getText().toString();
                String desc = editDesc.getText().toString();
                Double lon = Double.parseDouble(editLon.getText().toString());
                Double lat = Double.parseDouble(editLat.getText().toString());

                Landmark landmark = new Landmark(name, desc, type, lon, lat,  FirebaseAuth.getInstance().getCurrentUser().getUid());

                // dodaj landmark u bazu
                root.push().setValue(landmark);
                Toast.makeText(AddLandmark.this, "Landmark " + name + " has been added!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        this.type = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
