package com.aegismesh.activities;

import java.util.jar.Manifest;

import javax.naming.Context;

import com.aegismesh.R;
import com.aegismesh.models.User;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class HomeActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;

    private TextView tvGreeting, tvProfileStatus;
    private Button btnGoToEmergency, btnEditProfile, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        tvGreeting = findViewById(R.id.tvGreeting);
        tvProfileStatus = findViewById(R.id.tvProfileStatus);
        btnGoToEmergency = findViewById(R.id.btnGoToEmergency);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnLogout = findViewById(R.id.btnLogout);

        // Check and request dangerous permissions immediately upon reaching the dashboard
        checkPermissions();

        // Navigate to the SOS Mode
        btnGoToEmergency.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, EmergencyActivity.class));
        });

        // Navigate to Medical Profile Setup
        btnEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
        });

        // Handle Logout
        btnLogout.setOnClickListener(v -> {
            SharedPreferences.Editor editor = getSharedPreferences("AegisAuthPrefs", Context.MODE_PRIVATE).edit();
            editor.putBoolean("isLoggedIn", false);
            editor.apply();

            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update the UI every time the user returns to this screen (e.g., after editing profile)
        User currentUser = ProfileActivity.getSavedUser(this);
        
        tvGreeting.setText("Hello, " + currentUser.getFullName());

        // Check if the user has filled out their medical info for the AI
        if (currentUser.getFullName().equals("Unknown Victim")) {
            tvProfileStatus.setText("⚠️ Please setup your Medical Profile for AI Triage.");
            tvProfileStatus.setTextColor(0xFFFF0000); // Red
        } else {
            tvProfileStatus.setText("✅ Medical Profile is Ready.");
            tvProfileStatus.setTextColor(0xFF00AA00); // Green
        }
    }

    /**
     * Android 12+ requires explicit runtime permissions for Bluetooth Scanning and Advertising.
     * We also need Location permissions for the Mesh routing logic.
     */
    private void checkPermissions() {
        String[] requiredPermissions;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ specific Bluetooth permissions
            requiredPermissions = new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION
            };
        } else {
            // Legacy Android permissions
            requiredPermissions = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };
        }

        boolean allGranted = true;
        for (String permission : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }

        if (!allGranted) {
            ActivityCompat.requestPermissions(this, requiredPermissions, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Warning: Mesh Networking requires Bluetooth and Location permissions to function in an emergency.", Toast.LENGTH_LONG).show();
                    return;
                }
            }
            Toast.makeText(this, "All permissions granted. Aegis Mesh is ready.", Toast.LENGTH_SHORT).show();
        }
    }
}