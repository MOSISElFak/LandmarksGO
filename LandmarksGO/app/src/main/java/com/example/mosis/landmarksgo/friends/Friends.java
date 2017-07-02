package com.example.mosis.landmarksgo.friends;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mosis.landmarksgo.R;
import com.example.mosis.landmarksgo.authentication.User;
import com.example.mosis.landmarksgo.bluetooth.ChatService;
import com.example.mosis.landmarksgo.bluetooth.DeviceListActivity;
import com.example.mosis.landmarksgo.highscore.CustomAdapter;
import com.example.mosis.landmarksgo.highscore.DataModel;
import com.example.mosis.landmarksgo.other.BackgroundService;
import com.example.mosis.landmarksgo.other.BitmapManipulation;
import com.example.mosis.landmarksgo.other.LandmarksDBAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class Friends extends AppCompatActivity {
    public static final String FRIEND_REQUEST_CODE = "MONUMENTS_GO_FRIEND_REQUEST_";
    private static final int BT_DISCOVERABLE_TIME = 120;
    private static ArrayList<DataModel> dataModels;
    private  CustomAdapter adapter;
    private LandmarksDBAdapter dbAdapter;
    ProgressBar pb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "MainActivity: onCreate started");
        setContentView(R.layout.activity_friends);

        pb = (ProgressBar) findViewById(R.id.progressBarFriends);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabAddNewFriend);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Wait for incoming friend request or send one.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (bluetoothAdapter == null) {
                    Toast.makeText(Friends.this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
                    //finish();
                    return;
                }
                ensureDiscoverable(bluetoothAdapter);   //onActivityResult checks if discoverability in enabled and then sends friend request
            }});

        FirebaseAuth auth = FirebaseAuth.getInstance();
        final FirebaseUser user = auth.getCurrentUser();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference dbRef = database.getReference("friends/" + user.getUid());

        dataModels = new ArrayList<>();
        final ListView listView = (ListView) findViewById(R.id.listViewFriends);
        adapter= new CustomAdapter(dataModels,getApplicationContext());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                final DataModel dataModel= dataModels.get(position);
                new AlertDialog.Builder(Friends.this)
                        .setTitle("Removing friendship")
                        .setMessage("Are you sure you want to remove " + dataModel.getName())
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                final DatabaseReference dbRef = database.getReference("friends/" + user.getUid());

                                //dbRef.child(String.valueOf(position)).setValue("null");
                                dbRef.child(String.valueOf(dataModel.getFriendNumberOnServer())).removeValue();
                                dataModels.remove(position);
                                adapter.notifyDataSetChanged();

                                Snackbar.make(findViewById(android.R.id.content), "Removed " + dataModel.getName(), Snackbar.LENGTH_LONG).show();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

            }
        });
        dbAdapter = new LandmarksDBAdapter(getApplicationContext());

        //search server for current user's friends
        getFriendsFromServer(dbRef, adapter);
    }

    private void getFriendsFromServer(DatabaseReference dbRef, final CustomAdapter adapter) {
        Toast.makeText(this,"Getting friends from server",Toast.LENGTH_SHORT).show();
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Log.d(TAG,"u valueevent listener: count " + dataSnapshot.getChildrenCount());
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    String json = singleSnapshot.toString();
                   // Log.d(TAG,"json: " + json);

                    //TODO: deserialize via class, not like this
                    final String friendUid = json.substring(json.indexOf("value = ") + 8, json.length()-2);
                    //Log.d(TAG,"friendUid: " + friendUid);

                    final String friendNumber = json.substring(json.indexOf("key = ") + 6, json.indexOf(","));
                    //Log.d(TAG,"friendNumber: " + friendNumber);

                    //search server for friend's account
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    database.getReference("users").child(friendUid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            pb.setVisibility(View.VISIBLE);
                            final User user = dataSnapshot.getValue(User.class);
                            if(user!=null){
                                StorageReference storage = FirebaseStorage.getInstance().getReference().child("profile_images/" + friendUid + ".jpg");
                                final long MEMORY = 10 * 1024 * 1024;

                                //first download friend's photo
                                storage.getBytes(MEMORY).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                    @Override
                                    public void onSuccess(final byte[] bytes) {

                                        //get points for each friend
                                        final FirebaseDatabase database = FirebaseDatabase.getInstance();
                                        DatabaseReference dbRef = database.getReference("scoreTable/" + friendUid);
                                        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                Log.d(TAG, "NASAO dataSnapshot: " + dataSnapshot.toString());
                                                //TODO: don't deserialize like this
                                                String snapshot = dataSnapshot.toString();
                                                String pointsS = snapshot.substring(snapshot.indexOf("points=") + 7, snapshot.length()-3);

                                                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                                bitmap = BitmapManipulation.getCroppedBitmap(bitmap);

                                                dataModels.add(new DataModel(user.firstName + " " + user.lastName + "\n" + user.uid,Integer.parseInt(pointsS),bitmap,5,Integer.parseInt(friendNumber)));
                                                adapter.notifyDataSetChanged();

                                                pb.setVisibility(View.GONE);
                                                bitmap = null;
                                            }
                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                                Log.e(TAG, "onCancelled", databaseError.toException());
                                            }
                                        });
                                        //< got points
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        //user exists, but doesn't have profile photo
                                        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.empty_profile_picture);
                                        dataModels.add(new DataModel(user.firstName + " " + user.lastName + "\n" + user.uid,0,bitmap,5,Integer.parseInt(friendNumber)));
                                        adapter.notifyDataSetChanged();
                                        bitmap = null;
                                    }
                                });
                            }else{
                                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.empty_profile_picture);
                                dataModels.add(new DataModel("fake user\n" + friendUid,0,bitmap,5,Integer.parseInt(friendNumber)));
                                adapter.notifyDataSetChanged();
                                bitmap = null;
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(Friends.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled", databaseError.toException());
            }
        });
    }

    private void addNewFriend() {
        Log.d(TAG, "Friends addNewFriend started");
        Runnable r = new Runnable() {

            @Override
            public void run() {
                Intent serverIntent = new Intent(Friends.this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
            }
        };
        Thread btThread = new Thread(r);
        btThread.start();
    }

    //----------------------------------------------------------------------------------------------------------------------------------

    private final static String TAG = "BT";
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    private ListView lvMainChat;
    private EditText etMain;
    private Button btnSend;

    private String connectedDeviceName = null;
    //private ArrayAdapter<String> chatArrayAdapter;

    private StringBuffer outStringBuffer;
    private BluetoothAdapter bluetoothAdapter = null;
    private ChatService chatService = null;

    private Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            Log.d(TAG, "MainActivity: handleMessage started");
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case ChatService.STATE_CONNECTED:
                            Log.d(TAG, "MainActivity: handleMessage MESSAGE_STATE_CHANGE STATE_CONNECTED");     //for new devices
                            setStatus(getString(R.string.title_connected_to, connectedDeviceName));
                            //chatArrayAdapter.clear();
                            sendFriendRequest();
                            break;
                        case ChatService.STATE_CONNECTING:
                            Log.d(TAG, "MainActivity: handleMessage MESSAGE_STATE_CHANGE STATE_CONNECTING");    //for paired devices??
                            setStatus(R.string.title_connecting);
                            sendFriendRequest();
                            break;
                        case ChatService.STATE_LISTEN:
                        case ChatService.STATE_NONE:
                            Log.d(TAG, "MainActivity: handleMessage MESSAGE_STATE_CHANGE STATE_NONE");
                            setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    Log.d(TAG, "MainActivity: handleMessage MESSAGE_WRITE");
                    byte[] writeBuf = (byte[]) msg.obj;

                    String writeMessage = new String(writeBuf);
                    //chatArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case MESSAGE_READ:
                    Log.d(TAG, "MainActivity: handleMessage MESSAGE_READ");
                    byte[] readBuf = (byte[]) msg.obj;

                    String readMessage = new String(readBuf, 0, msg.arg1);

                    Log.d(TAG, "readMessage:" + readMessage);
                    //Toast.makeText(Friends.this, ""+ readMessage, Toast.LENGTH_LONG).show();

                    String message = readMessage;

                    int _char = message.lastIndexOf("_");
                    String messageCheck = message.substring(0,_char+1);
                    final String friendsUid = message.substring(_char+1);
                    Log.d(TAG,"TEMP messageCheck:" + messageCheck); //messageCheck:MONUMENTS_GO_FRIEND_REQUEST_
                    Log.d(TAG,"TEMP friendsUid:" + friendsUid);
                    Log.d(TAG,"TEMP FRIEND_REQUEST_CODE:" + FRIEND_REQUEST_CODE);//FRIEND_REQUEST_CODE:MONUMENTS_GO_FRIEND_REQUEST_
                    if(messageCheck.equals(FRIEND_REQUEST_CODE)) {
                        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        v.vibrate(500);

                        runOnUiThread(new Runnable() {
                            public void run() {
                                new AlertDialog.Builder(Friends.this)
                                        .setTitle("Confirm friend request")
                                        .setMessage("Are you sure you want to become friends with a device\n" + connectedDeviceName + "\nUserID(" + friendsUid + ")")
                                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                final String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                                final FirebaseDatabase database = FirebaseDatabase.getInstance();
                                                final DatabaseReference dbRef = database.getReference("friends/" + myUid);
                                                dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        List<String> friendsList = new ArrayList<>();
                                                        Log.d(TAG, "u valueevent listener: count " + dataSnapshot.getChildrenCount());
                                                        for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                                                            String json = singleSnapshot.toString();
                                                            //TODO: deserialize via class, not like this
                                                            String friendUid = json.substring(json.indexOf("value = ") + 8, json.length() - 2);
                                                            Log.d(TAG, "friendUid2: " + friendUid);
                                                            friendsList.add(friendUid);
                                                        }
                                                        if (friendsList.contains(friendsUid)) {
                                                            Toast.makeText(Friends.this, "You already have this friend!", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            friendsList.add(friendsUid); //adding new friendship
                                                            DatabaseReference dbFriends = database.getReference("friends/");
                                                            dbFriends.child(myUid).setValue(friendsList);

                                                            dbAdapter.open();
                                                            if (!dbAdapter.checkFriendship(friendsUid))
                                                            {
                                                                dbAdapter.insertFriendship(friendsUid);
                                                                BackgroundService.myPoints += 5;
                                                                FirebaseDatabase.getInstance().getReference("scoreTable").child(myUid).child("points").setValue(BackgroundService.myPoints);
                                                            }
                                                            dbAdapter.close();

                                                            Snackbar.make(findViewById(android.R.id.content), "You are now friends with " + friendsUid, Snackbar.LENGTH_LONG).show();
                                                            adapter.clear();
                                                            getFriendsFromServer(dbRef, adapter);
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {
                                                        Log.e(TAG, "onCancelled", databaseError.toException());
                                                    }
                                                });
                                            }
                                        })
                                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                Toast.makeText(Friends.this, "You declined friend request", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .show();
                                }
                            });
                    }
                    break;
                case MESSAGE_DEVICE_NAME:
                    Log.d(TAG, "MainActivity: handleMessage MESSAGE_DEVICE_NAME");
                    connectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to " + connectedDeviceName + "\nClose upper window and confirm friend request.", Toast.LENGTH_LONG).show();
                    break;
                case MESSAGE_TOAST:
                    Log.d(TAG, "MainActivity: handleMessage MESSAGE_TOAST");
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
                    break;
            }
            return false;
        }
    });

    private void getWidgetReferences() {
        Log.d(TAG, "MainActivity: getWidgetReferences started");
        //lvMainChat = (ListView) findViewById(R.id.lvMainChat);
        //etMain = (EditText) findViewById(R.id.etMain);
        //btnSend = (Button) findViewById(R.id.btnSend);
    }

    private void bindEventHandler() {
        Log.d(TAG, "MainActivity: bindEventHandler started");
        /*
        etMain.setOnEditorActionListener(mWriteListener);


        btnSend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String message = etMain.getText().toString();
                sendMessage(message);
            }
        });
        */
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "MainActivity: onActivityResult started");
        Log.d(TAG, "requestCode=" + requestCode + " resultCode=" + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                if (resultCode == BT_DISCOVERABLE_TIME) {
                    //Toast.makeText(this,"Setup chat", Toast.LENGTH_SHORT).show();
                    setupChat();
                    addNewFriend();
                } else {
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    //finish();
                }
                break;
        }
    }

    private void connectDevice(Intent data, boolean secure) {
        Log.d(TAG, "MainActivity: connectDevice started");
        String address = data.getExtras().getString(DeviceListActivity.DEVICE_ADDRESS);
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        try{
            chatService.connect(device, secure);
        }catch (Exception e){
            Toast.makeText(this, "Error! Other user must click on + button.", Toast.LENGTH_LONG).show();
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(500);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        //inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*
        Intent serverIntent = null;
        switch (item.getItemId()) {
            case R.id.secure_connect_scan:
                serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                return true;
            case R.id.insecure_connect_scan:
                serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent,
                        REQUEST_CONNECT_DEVICE_INSECURE);
                return true;
            case R.id.discoverable:
                ensureDiscoverable();
                return true;
        }
        */
        return false;
    }

    private void ensureDiscoverable(BluetoothAdapter bluetoothAdapter) {
        Log.d(TAG, "MainActivity: ensureDiscoverable started");
        //if (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) { //this must be commented because then onActivityResult is not called when BT is enabled before enterin Friends activity
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, BT_DISCOVERABLE_TIME);
            startActivityForResult(discoverableIntent,REQUEST_ENABLE_BT);
        //}
    }

    private void sendMessage(String message) {
        Log.d(TAG, "MainActivity: sendMessage started");
        if (chatService.getState() != ChatService.STATE_CONNECTED) {
            //Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        if (message.length() > 0) {
            byte[] send = message.getBytes();
            chatService.write(send);

            outStringBuffer.setLength(0);
            //etMain.setText(outStringBuffer);
        }
    }

    private TextView.OnEditorActionListener mWriteListener = new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_NULL
                    && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                sendMessage(message);
            }
            return true;
        }
    };

    private final void setStatus(int resId) {
        Log.d(TAG, "MainActivity: setStatus1 started");
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setSubtitle(resId);
    }

    private final void setStatus(CharSequence subTitle) {
        Log.d(TAG, "MainActivity: setStatus2 started");
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setSubtitle(subTitle);
    }

    private boolean setupChat() {
        Log.d(TAG, "MainActivity: setupChat started");
        //chatArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
        //lvMainChat.setAdapter(chatArrayAdapter);

        chatService = new ChatService(this, handler);

        outStringBuffer = new StringBuffer("");

        if (chatService.getState() == ChatService.STATE_NONE) {
            chatService.start();
        }
        return true;
    }

    private void sendFriendRequest(){
        String message = FRIEND_REQUEST_CODE + FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d(TAG, "MainActivity: addNewFriend sendingMessage:" + message);
        sendMessage(message);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "MainActivity: onStart started");
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        Log.d(TAG, "MainActivity: onResume started");
        /*
        if (chatService != null) {
            if (chatService.getState() == ChatService.STATE_NONE) {
                chatService.start();
            }
        }
        */
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        Log.d(TAG, "MainActivity: onPause started");
        /*
        if (bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.disable();
        }
        */
        /*
        if (chatService != null) {
            if (chatService.getState() != ChatService.STATE_NONE) {
                chatService.stop();
            }
        }
        */
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (chatService != null)
            chatService.stop();
    }
}
