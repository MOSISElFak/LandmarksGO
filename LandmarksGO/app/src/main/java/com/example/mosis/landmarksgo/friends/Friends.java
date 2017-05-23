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
import android.widget.TextView;
import android.widget.Toast;

import com.example.mosis.landmarksgo.R;
import com.example.mosis.landmarksgo.bluetooth.ChatService;
import com.example.mosis.landmarksgo.bluetooth.DeviceListActivity;
import com.example.mosis.landmarksgo.highscore.CustomAdapter;
import com.example.mosis.landmarksgo.highscore.DataModel;
import com.example.mosis.landmarksgo.other.BitmapManipulation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class Friends extends AppCompatActivity {
    public static final String FRIEND_REQUEST_CODE = "MONUMENTS_GO_FRIEND_REQUEST_";
    private static ArrayList<DataModel> dataModels;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "MainActivity: onCreate started");
        setContentView(R.layout.activity_friends);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabAddNewFriend);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Waiting for incoming friend request or send one.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                //Intent intent = new Intent(Friends.this, FriendsAddNew.class);
                //startActivity(intent);

                addNewFriend();
            }
        });

        dataModels = new ArrayList<>();
        ListView listView = (ListView) findViewById(R.id.listViewFriends);
        CustomAdapter adapter;
        adapter= new CustomAdapter(dataModels,getApplicationContext());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DataModel dataModel= dataModels.get(position);
                Toast.makeText(Friends.this,"" + dataModel.getName(),Toast.LENGTH_SHORT).show();
            }
        });

        //TODO: Load stuff from server
        Bitmap bitmap =  BitmapFactory.decodeResource(this.getResources(), R.drawable.obama);

        dataModels.add(new DataModel("President",100,bitmap, 1));
        bitmap = BitmapManipulation.getCroppedBitmap(bitmap);
        dataModels.add(new DataModel("Circular",50,bitmap, 2));
        dataModels.add(new DataModel("",0,null, 3));
        adapter.notifyDataSetChanged();
    }

    private void addNewFriend() {

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

                    //TODO: Test this on two real devices
                    int _char = message.lastIndexOf("_");
                    String messageCheck = message.substring(0,_char+1);
                    final String friendsUid = message.substring(_char+1);
                    Log.d(TAG,"TEMP messageCheck:" + messageCheck); //messageCheck:MONUMENTS_GO_FRIEND_REQUEST_
                    Log.d(TAG,"TEMP friendsUid:" + friendsUid);
                    Log.d(TAG,"TEMP FRIEND_REQUEST_CODE:" + FRIEND_REQUEST_CODE);//FRIEND_REQUEST_CODE:MONUMENTS_GO_FRIEND_REQUEST_
                    if(messageCheck.equals(FRIEND_REQUEST_CODE)){
                        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        v.vibrate(500);

                        new AlertDialog.Builder(Friends.this)
                                .setTitle("Confirm friend request")
                                .setMessage("Are you sure you want to become friends with a device\n" + connectedDeviceName + "\nUserID(" + friendsUid + ")")
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        Toast.makeText(Friends.this, "You accepted friend request!",Toast.LENGTH_SHORT).show();
                                        FirebaseDatabase database;
                                        DatabaseReference root;

                                        //send friendship to the server
                                        //TODO: this code can add many same friendships. Don't send friendship data if you are already friends with other user.
                                        database = FirebaseDatabase.getInstance();
                                        root = database.getReference("friends");
                                        Friendship friendship = new Friendship(FirebaseAuth.getInstance().getCurrentUser().getUid(), friendsUid);

                                        root.push().setValue(friendship);
                                    }
                                })
                                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        Toast.makeText(Friends.this, "You declined friend request",Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    }

                    //chatArrayAdapter.add(connectedDeviceName + ":  " + readMessage);

                    //SimpleDateFormat s = new SimpleDateFormat("ddMMyyyyhhmmss");
                    //String format = s.format(new Date());
                    //sendMessage("ok: " + format);
                    break;
                case MESSAGE_DEVICE_NAME:
                    Log.d(TAG, "MainActivity: handleMessage MESSAGE_DEVICE_NAME");
                    connectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to " + connectedDeviceName, Toast.LENGTH_SHORT).show();
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
                if (resultCode == Activity.RESULT_OK) {
                    setupChat();
                } else {
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    private void connectDevice(Intent data, boolean secure) {
        Log.d(TAG, "MainActivity: connectDevice started");
        String address = data.getExtras().getString(DeviceListActivity.DEVICE_ADDRESS);
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        try{
            chatService.connect(device, secure);
        }catch (Exception e){
            Toast.makeText(this, "Error! Other user must be in the Friends activity with activated Bluetooth.", Toast.LENGTH_LONG).show();
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

    private void ensureDiscoverable() {
        Log.d(TAG, "MainActivity: ensureDiscoverable started");
        if (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
            startActivity(discoverableIntent);
        }
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

    private void setupChat() {
        Log.d(TAG, "MainActivity: setupChat started");
        //chatArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
        //lvMainChat.setAdapter(chatArrayAdapter);

        chatService = new ChatService(this, handler);

        outStringBuffer = new StringBuffer("");
    }

    private void sendFriendRequest(){
        //TODO: When two devices are connected, send friend request. Show alert to confirm it.
        String message = FRIEND_REQUEST_CODE + FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d(TAG, "MainActivity: addNewFriend sendingMessage:" + message);
        sendMessage(message);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "MainActivity: onStart started");
        /* moved this to addNewFriend function
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            if (chatService == null)
                setupChat();
        }
        */

        //TODO: Move this to FAB
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            //Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {

            if (chatService == null) {
                setupChat();
            }
        }
        if (bluetoothAdapter == null) {
            Toast.makeText(Friends.this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        ensureDiscoverable();

    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        Log.d(TAG, "MainActivity: onResume started");
        if (chatService != null) {
            if (chatService.getState() == ChatService.STATE_NONE) {
                chatService.start();
            }
        }
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
