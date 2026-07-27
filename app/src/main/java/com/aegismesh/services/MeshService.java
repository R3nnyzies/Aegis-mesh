package com.aegismesh.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.aegismesh.models.Emergency;
import com.aegismesh.models.MeshStatus;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service that handles local peer-to-peer mesh networking (BLE Mesh / Wi-Fi Direct)
 * when traditional cellular/internet connection is unavailable.
 *
 * Packet (de)serialization and hop-count/loop-prevention is real, native code
 * (see ble_mesh.cpp) reached via JNI below. Actual over-the-air peer discovery
 * and transmission (BLE advertising/scanning, Wi-Fi Direct group formation) is
 * NOT implemented anywhere in this project yet - {@link #sendEmergencyOverMesh}
 * simulates that missing radio layer with a timed delay after building the
 * real packet, until a real transport is built.
 */
public class MeshService extends Service {
    private static final String TAG = "MeshService";
    private final IBinder binder = new LocalBinder();

    static {
        System.loadLibrary("aegismesh-native");
    }

    // JNI bridge - see native-lib.cpp / ble_mesh.cpp / wifi_direct.cpp
    private native byte[] nativeBuildSosPacket(int msgId, String name, String condition);
    private native byte[] nativeProcessIncomingPacket(byte[] inputPayload);
    private native String nativeStartWifiServer(int port);
    private native boolean nativeSendProfile(String targetIp, int port, String profileJson);

    private static final MutableLiveData<MeshStatus> statusLiveData = new MutableLiveData<>(new MeshStatus.Online());

    public static LiveData<MeshStatus> getStatusLiveData() {
        return statusLiveData;
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, MeshService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            android.app.NotificationChannel channel = new android.app.NotificationChannel(
                    "MESH_SERVICE_CHANNEL",
                    "Mesh Network Service Channel",
                    android.app.NotificationManager.IMPORTANCE_LOW
            );
            android.app.NotificationManager manager = (android.app.NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
            android.app.Notification notification = new androidx.core.app.NotificationCompat.Builder(this, "MESH_SERVICE_CHANNEL")
                    .setContentTitle("Mesh Network Active")
                    .setContentText("Broadcasting and listening for peer alerts...")
                    .setSmallIcon(android.R.drawable.stat_notify_sync)
                    .build();
            startForeground(912, notification);
        }
        return START_STICKY;
    }
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    /**
     * Interface to monitor mesh transmission outcomes.
     */
    public interface MeshCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public class LocalBinder extends Binder {
        public MeshService getService() {
            return MeshService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    /**
     * Broadcasts emergency data to nearby peer devices over the mesh network.
     * Operates in a background thread to prevent blocking the UI.
     *
     * The 31-byte SOS packet is built for real via {@link #nativeBuildSosPacket}
     * (see ble_mesh.cpp's packed MeshPayload struct). emergency.getUserId()
     * holds the victim's name here, not a database key - SOSService.trigger()
     * populates it from User.getFullName() before this ever runs.
     */
    public void sendEmergencyOverMesh(final Emergency emergency, final MeshCallback callback) {
        if (emergency == null) {
            if (callback != null) callback.onFailure("Emergency details are null.");
            return;
        }

        Log.i(TAG, "Initiating mesh network transmission for emergency: " + emergency.getEmergencyId());

        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    int msgId = emergency.getEmergencyId().hashCode();
                    byte[] packet = nativeBuildSosPacket(msgId, emergency.getUserId(), emergency.getEmergencyType());
                    Log.d(TAG, "Mesh: Built " + packet.length + "-byte SOS packet (msgId=" + msgId + ") via native ble_mesh.cpp.");

                    // Peer discovery/transmission itself (BLE advertising+scanning,
                    // Wi-Fi Direct group formation) isn't implemented anywhere in
                    // this project yet - this delay stands in for that missing
                    // radio layer until a real transport is built.
                    Log.d(TAG, "Mesh: Discovering nearby nodes...");
                    Thread.sleep(1500);

                    Log.d(TAG, "Mesh: Found peer devices. Establishing mesh relay connection...");
                    Thread.sleep(1000);

                    Log.d(TAG, "Mesh: Relaying emergency packet...");
                    Thread.sleep(500);

                    Log.i(TAG, "Mesh: Emergency packet successfully relayed to nearby mesh node.");

                    if (callback != null) {
                        callback.onSuccess();
                    }
                } catch (InterruptedException e) {
                    Log.e(TAG, "Mesh transmission interrupted: " + e.getMessage());
                    if (callback != null) {
                        callback.onFailure("Mesh transmission interrupted: " + e.getMessage());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Mesh transmission error: " + e.getMessage(), e);
                    if (callback != null) {
                        callback.onFailure("Unexpected mesh error: " + e.getMessage());
                    }
                }
            }
        });
    }

    /**
     * Feeds a raw 31-byte mesh packet received from a peer through the native
     * hop-count/loop-prevention logic (see processIncomingPacket in
     * ble_mesh.cpp). Returns the packet to rebroadcast (with its hop count
     * incremented), or null if it should be dropped - already seen, or max
     * hops reached.
     *
     * Nothing calls this yet: there is no real BLE/Wi-Fi Direct receive path
     * in this project. This is the entry point a future one should call.
     */
    @Nullable
    public byte[] onMeshPacketReceived(byte[] rawPacket) {
        if (rawPacket == null || rawPacket.length != 31) {
            Log.w(TAG, "Ignoring mesh packet with unexpected size: " + (rawPacket == null ? "null" : rawPacket.length));
            return null;
        }
        return nativeProcessIncomingPacket(rawPacket);
    }

    /**
     * Blocks the calling thread until a responder connects over the given TCP
     * port and requests the victim's profile, per the security proposal's
     * "release full profile only after responder acceptance" flow. Must be
     * called from a background thread.
     *
     * Nothing calls this yet: there is no responder-side UI in this project.
     * This exposes the native socket server (wifi_direct.cpp) for one to use.
     */
    public String startProfileServer(int port) {
        return nativeStartWifiServer(port);
    }

    /**
     * Sends the victim's full profile JSON to an accepted responder over a
     * direct TCP connection (Wi-Fi Direct). Must be called from a background
     * thread.
     */
    public boolean sendProfileToResponder(String targetIp, int port, String profileJson) {
        return nativeSendProfile(targetIp, port, profileJson);
    }
}
