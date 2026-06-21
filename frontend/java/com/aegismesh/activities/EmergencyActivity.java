package com.aegismesh.activities;

import javax.swing.text.View;

import com.aegismesh.sensors.GestureDetector;
import com.aegismesh.services.MeshService;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class EmergencyActivity extends AppCompatActivity implements GestureDetector.GestureListener {

    private GestureDetector gestureDetector;
    private Button btnHoldToArm;
    private TextView tvStatus;

    // Hardcoded for now. In reality, we fetch this from SQLite/Room or the ProfileActivity
    private String victimName = "Jane Doe";
    private String medicalCondition = "Severe Allergy";

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Note: Assume res/layout/activity_emergency.xml has a Button and TextView
        setContentView(R.layout.activity_emergency);

        btnHoldToArm = findViewById(R.id.btnHoldToArm);
        tvStatus = findViewById(R.id.tvStatus);

        // 1. Initialize our Sensor/C++ Bridge
        gestureDetector = new GestureDetector(this, this);

        // 2. Set up the Two-Factor Touch interaction
        btnHoldToArm.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Finger is on the screen! Tell C++ to start counting shakes
                        gestureDetector.setScreenHeld(true);
                        btnHoldToArm.setText("ARMED! SHAKE NOW!");
                        btnHoldToArm.setBackgroundColor(0xFFFF0000); // Red
                        return true;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // Finger left the screen. Tell C++ to cancel/reset
                        gestureDetector.setScreenHeld(false);
                        btnHoldToArm.setText("HOLD TO ARM");
                        btnHoldToArm.setBackgroundColor(0xFF888888); // Gray
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Turn on the high-frequency sensor when the screen is active
        gestureDetector.startListening();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Save battery by turning off sensor when app is minimized
        gestureDetector.stopListening();
    }

    // 3. This is called automatically by C++ when 3 shakes are detected!
    @Override
    public void onSosTriggered() {
        runOnUiThread(() -> {
            tvStatus.setText("SOS DEPLOYED! BROADCASTING VIA MESH...");
            Toast.makeText(this, "Emergency Protocol Activated!", Toast.LENGTH_LONG).show();

            // Vibrate phone to give physical feedback to the user
            // (Requires VIBRATE permission)

            // 4. Launch the Background Service to blast the Bluetooth Flare
            Intent meshIntent = new Intent(this, MeshService.class);
            meshIntent.setAction("ACTION_START_SOS");
            meshIntent.putExtra("VICTIM_NAME", victimName);
            meshIntent.putExtra("CONDITION", medicalCondition);
            
            // Start the service (Will keep broadcasting even if UI closes)
            startService(meshIntent);
        });
    }
}