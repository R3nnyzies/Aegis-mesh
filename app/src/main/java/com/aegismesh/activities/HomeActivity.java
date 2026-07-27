package com.aegismesh.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;

import com.aegismesh.R;
import com.aegismesh.databinding.ActivityHomeBinding;
import com.aegismesh.models.MeshStatus;
import com.aegismesh.models.User;
import com.aegismesh.repository.EmergencyRepository;
import com.aegismesh.sensors.GestureDetector;
import com.aegismesh.services.MeshService;
import com.aegismesh.session.UserSession;

import java.util.ArrayList;
import java.util.List;

/**
 * Main dashboard shown after login.
 *
 * Responsibilities:
 *  - Arm {@link GestureDetector} so a shake/wave gesture can trigger an SOS
 *    from anywhere in the app, per the "gesture SOS activates the full
 *    response chain in under 3 seconds" design principle.
 *  - Surface live mesh network status (peer count, offline/online mode) by
 *    observing {@link MeshService}.
 *  - Provide the manual SOS button as an accessible fallback to the gesture.
 *  - Show a lightweight greeting and profile-completeness hint, and let the
 *    user log out.
 */
public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;
    private GestureDetector gestureDetector;

    private final ActivityResultLauncher<String[]> requestPermissions =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), grants -> {
                boolean allGranted = true;
                for (Boolean granted : grants.values()) {
                    allGranted &= Boolean.TRUE.equals(granted);
                }
                if (allGranted) {
                    startMeshService();
                } else {
                    binding.textMeshStatus.setText(getString(R.string.permissions_required_for_mesh));
                }
            });

    private final Observer<MeshStatus> meshStatusObserver = this::renderMeshStatus;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        gestureDetector = new GestureDetector(this, this::triggerSos);

        binding.buttonSos.setOnClickListener(v -> triggerSos());
        binding.buttonProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));
        binding.btnLogout.setOnClickListener(v -> logout());

        ensurePermissionsThenStartServices();
        MeshService.getStatusLiveData().observe(this, meshStatusObserver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        gestureDetector.start();
        renderProfileStatus();
    }

    @Override
    protected void onPause() {
        gestureDetector.stop();
        super.onPause();
    }

    private void ensurePermissionsThenStartServices() {
        String[] required = new String[] {
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.ACCESS_FINE_LOCATION,
        };

        List<String> missing = new ArrayList<>();
        for (String permission : required) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                missing.add(permission);
            }
        }

        if (missing.isEmpty()) {
            startMeshService();
        } else {
            requestPermissions.launch(missing.toArray(new String[0]));
        }
    }

    private void startMeshService() {
        MeshService.start(this);
    }

    private void renderMeshStatus(MeshStatus status) {
        if (status instanceof MeshStatus.Online) {
            binding.textMeshStatus.setText(getString(R.string.mesh_status_online));
        } else if (status instanceof MeshStatus.OfflineRelay) {
            int peerCount = ((MeshStatus.OfflineRelay) status).nearbyPeerCount;
            binding.textMeshStatus.setText(getString(R.string.mesh_status_offline_relay, peerCount));
        } else {
            binding.textMeshStatus.setText(getString(R.string.mesh_status_disconnected));
        }
    }

    private void renderProfileStatus() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        boolean hasProfile = currentUser != null
                && currentUser.getFullName() != null
                && !currentUser.getFullName().trim().isEmpty();

        if (hasProfile) {
            binding.tvGreeting.setText("Hello, " + currentUser.getFullName());
            binding.tvProfileStatus.setText("Medical profile is ready.");
            binding.tvProfileStatus.setTextColor(0xFF00AA00);
        } else {
            binding.tvGreeting.setText("Hello,");
            binding.tvProfileStatus.setText("Set up your medical profile for AI triage.");
            binding.tvProfileStatus.setTextColor(0xFFFF0000);
        }
    }

    private void logout() {
        getSharedPreferences("AegisAuthPrefs", MODE_PRIVATE).edit().clear().apply();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    /** Triggered by either the gesture sensor or the manual SOS button. */
    private void triggerSos() {
        binding.buttonSos.setEnabled(false);
        EmergencyRepository.getInstance().trigger(this);
        startActivity(new Intent(this, EmergencyActivity.class));
        binding.buttonSos.setEnabled(true);
    }
}
