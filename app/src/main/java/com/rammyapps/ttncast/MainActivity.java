package com.rammyapps.ttncast;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.koushikdutta.async.ByteBufferList;
import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.callback.DataCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends ActionBarActivity {

    ArrayList<String> msgs = new ArrayList<String>();
    ArrayAdapter<String> msgadapter;

    Runnable socketRunnable = new Runnable () {
        public void run() {

        AsyncHttpClient.getDefaultInstance().websocket("wss://www.treesnetwork.com/socket", null, new AsyncHttpClient.WebSocketConnectCallback() {
            @Override

            public void onCompleted(Exception ex, WebSocket webSocket) {
                if (ex != null) {
                    System.out.println("I got an error");
                    ex.printStackTrace();
                    return;
                }

                webSocket.setStringCallback(new WebSocket.StringCallback() {
                    public void onStringAvailable(String s) {
                        try {
                            JSONObject jObject = new JSONObject(s);
                            String type = jObject.getString("type");
                            if (type.equals("messages:add")) {
                                String data = jObject.getString("data");
                                JSONObject jObj = new JSONObject(data);
                                final String msg = jObj.getString("message");
                                String usr = jObj.getString("user");
                                JSONObject jDataUser = new JSONObject(usr);
                                final String uname = jDataUser.getString("username");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        msgs.add(uname + ":  " + msg);
                                        msgadapter.notifyDataSetChanged();
                                    }
                                });
                            } else {
                                Log.d("Test", "Nonmessage! " + type);
                            }
                        } catch (Exception e){
                            Log.d("Test", "Exception thrown! " + e);
                        }

                    }
                });

                webSocket.setClosedCallback(new CompletedCallback() {
                    @Override
                    public void onCompleted(Exception ex) {
                        try {
                            if (ex != null)
                                Log.e("WebSocket", "Error");
                        } finally {
                            //
                        }
                    }
                });
            }
        });

    } };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        final Runnable actualTask = null;

        executorService.scheduleAtFixedRate(socketRunnable, 0, 35000, TimeUnit.MILLISECONDS);

        msgadapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, msgs);
        ListView listview = ((ListView) findViewById(R.id.msgView));
        listview.setAdapter(msgadapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
