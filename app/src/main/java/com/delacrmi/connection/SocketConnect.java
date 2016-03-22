package com.delacrmi.connection;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by miguel on 13/10/15.
 */
public abstract class SocketConnect {

    private Socket socket;
    private AppCompatActivity context;
    private String URI;
    private boolean CONNECTED = false;
    private IO.Options opts;

    public SocketConnect(AppCompatActivity context,String URI){
        this.context = context;
        initComponents(URI);
    }

    public SocketConnect(AppCompatActivity context,String URI,IO.Options opts){
        this.context = context;
        this.opts = opts;
        initComponents(URI);
    }

    public SocketConnect(String URI){
        initComponents(URI);
    }
    public SocketConnect(String URI,IO.Options opts){
        this.opts = opts;
        initComponents(URI);
    }

    private void initComponents(String URI){
        this.URI = URI;
        try {
            if(opts == null) {
                socket = IO.socket(URI);
                Log.d("Opts", "is null");
            }else {
                Log.d("Opts", "not null");
                socket = IO.socket(URI, opts);
            }

            socket.on("synchronizeClient", onSynchronizeClient);
            socket.on("synchronizeServer", onSynchronizeServer);
            socket.on("syncReject",onSynchronizeReject);
            socket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
            socket.on(Socket.EVENT_DISCONNECT,onDisconnect);
            socket.on(Socket.EVENT_CONNECT,onConnectSuccess);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void setURI(String URI){
        if(socket.connected()){
            socket.disconnect();
        }

        socket = null;
        initComponents(URI);
    }

    public void init(){
        socket.connect();
    }

    public Socket getSocket() {
        return socket;
    }

    public void stopSocket() {
        try {
            socket = socket.disconnect();
        }catch (NullPointerException npe){}
    }

    public void onSynchronizeClient(final Object ... args){}
    public void onSynchronizeServer(final Object ... args){}
    public void onSyncSuccess(final Object ... args){
        CONNECTED = true;
    }

    public void onErrorConnection(){
        CONNECTED = false;
        Log.e("Connection", "The socket.io isn't connected to "+URI);
    }

    public void onDisconnected(){
        CONNECTED = false;
        Log.e("Connection", "The socket.io is disconnected to "+URI);
    }

    public void onSyncReject(Object... args) {
        JSONObject obj = (JSONObject) args[0];
        try {
            Log.e("error", obj.getString("error"));
            Log.e("error", "============================");
            Log.e("error", obj.getString("valueToInsert"));
            Log.e("error", "============================");
            Log.e("error", obj.getString("tableName"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String event,JSONObject json){
        socket.emit(event,json);
    }

    private Emitter.Listener onConnectError = new Emitter.Listener(){

        @Override
        public void call(Object... args) {
            onErrorConnection();
        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener(){

        @Override
        public void call(Object... args) {
            onDisconnected();
        }
    };

    private Emitter.Listener onSynchronizeClient = new Emitter.Listener(){
        @Override
        public void call(final Object... args) {
            if (context != null)
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onSynchronizeClient(args);
                    }
                });
            else
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        onSynchronizeClient(args);
                    }
                }).start();

        }
    };

    private Emitter.Listener onSynchronizeServer = new Emitter.Listener(){
            @Override
            public void call(final Object... args) {
                onSynchronizeServer(args);
            }
    };

    private Emitter.Listener onSynchronizeReject = new Emitter.Listener(){
        @Override
        public void call(final Object... args) {
            if (context != null)
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onSyncReject(args);
                    }
                });
            else
                onSyncReject(args);
        }
    };

    private Emitter.Listener onConnectSuccess = new Emitter.Listener(){
        @Override
        public void call(final Object... args) {
            if (context != null)
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onSyncSuccess(args);
                    }
                });
            else
            new Thread(new Runnable() {
                @Override
                public void run() {
                    onSyncSuccess(args);
                }
            }).start();

        }
    };

    public boolean isConnected() {
        return CONNECTED;
    }
}
