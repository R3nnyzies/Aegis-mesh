package com.aegismesh.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.aegismesh.R;
import com.aegismesh.adapters.TriageMessageAdapter;
import com.aegismesh.databinding.ActivityEmergencyBinding;
import com.aegismesh.models.Emergency;
import com.aegismesh.models.Hospital;
import com.aegismesh.models.Responder;
import com.aegismesh.models.TriageMessage;
import com.aegismesh.services.SOSService;

import java.util.ArrayList;

/**
 * Active-emergency screen.
 *
 * Shown immediately after an SOS is triggered and stays active for the
 * duration of the incident. It surfaces three concurrent data streams from
 * {@link SOSService}, each backed by a LiveData the service updates as
 * mesh relays, responders, and the AI triage engine report in:
 *
 *   1. Mesh relay status  - how the alert is propagating peer-to-peer.
 *   2. AI first-aid guidance - step-by-step instructions while help is en route.
 *   3. Responder + hospital routing - who is coming and where the victim
 *      should ultimately be taken.
 *
 * Per the security proposal, the victim's exact location is only released
 * to a responder after acceptance/confirmation; this screen reflects that
 * by showing "approximate" location language until [Emergency.locationConfirmed]
 * flips to true.
 */
public class EmergencyActivity extends AppCompatActivity {

    public static final String EXTRA_EMERGENCY_ID = "extra_emergency_id";

    private ActivityEmergencyBinding binding;
    private TriageMessageAdapter triageAdapter;
    @Nullable private String emergencyId;

    private final Observer<Emergency> emergencyObserver = this::renderEmergency;
    private final Observer<TriageMessage> triageObserver = this::appendTriageMessage;
    private final Observer<Responder> responderObserver = this::renderResponder;
    private final Observer<Hospital> hospitalObserver = this::renderHospital;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEmergencyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        emergencyId = getIntent().getStringExtra(EXTRA_EMERGENCY_ID);
        triageAdapter = new TriageMessageAdapter(new ArrayList<>());
        binding.recyclerTriage.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerTriage.setAdapter(triageAdapter);

        binding.buttonCancelSos.setOnClickListener(v -> confirmCancel());
        binding.buttonEscalate.setOnClickListener(v -> escalate());

<<<<<<< HEAD
        // IMPROVED TEST BUTTON FOR EMULATOR - ADDED INSIDE SCROLLVIEW TO AVOID OVERLAYING
        Button btnForceSos = new Button(this);
        btnForceSos.setText("FORCE TRIGGER SOS (TEST)");
        btnForceSos.setBackgroundColor(0xFFFF0000); // Red
        btnForceSos.setTextColor(0xFFFFFFFF); // White
        
        android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = 50;
        btnForceSos.setLayoutParams(params);
        
        btnForceSos.setOnClickListener(v -> {
            onSosTriggered();
        });
        
        // Add it to the main LinearLayout inside the ScrollView
        ((android.view.ViewGroup) binding.getRoot().getChildAt(0)).addView(btnForceSos);

=======
>>>>>>> origin/main
        observeActiveEmergency();
    }

    private void onSosTriggered() {
        SOSService.trigger(this);
    }

    private void observeActiveEmergency() {
        SOSService.getActiveEmergency().observe(this, emergencyObserver);
        SOSService.getTriageMessages().observe(this, triageObserver);
        SOSService.getAssignedResponder().observe(this, responderObserver);
        SOSService.getRecommendedHospital().observe(this, hospitalObserver);
    }

    private void renderEmergency(@Nullable Emergency emergency) {
        if (emergency == null) {
            // Only finish if we were actually tracking a specific emergency
            if (emergencyId != null) {
                finish();
            }
            return;
        }

        binding.textEmergencyType.setText(emergency.getEmergencyType());
        binding.textMeshRelayStatus.setText("Relayed by mesh"); // Placeholder or use getter if available

        binding.textLocationStatus.setText(
                emergency.getStatus().equals(Emergency.STATUS_DELIVERED)
                        ? getString(R.string.location_shared_exact)
                        : getString(R.string.location_shared_approximate, 100)); // Placeholder radius
    }

    private void appendTriageMessage(@Nullable TriageMessage message) {
        if (message == null) return;
        triageAdapter.append(message);
        binding.recyclerTriage.scrollToPosition(triageAdapter.getItemCount() - 1);
    }

    private void renderResponder(@Nullable Responder responder) {
        if (responder == null) {
            binding.groupResponder.setVisibility(View.GONE);
            return;
        }

        binding.groupResponder.setVisibility(View.VISIBLE);
        binding.textResponderName.setText(responder.getName());
        binding.textResponderTrustScore.setText(
                getString(R.string.responder_trust_score, responder.getTrustScore(), responder.getAssists()));
        binding.textResponderEta.setText(getString(R.string.responder_eta, responder.getEtaMinutes()));
        binding.iconVerifiedBadge.setVisibility(responder.isVerified() ? View.VISIBLE : View.GONE);
    }

    private void renderHospital(@Nullable Hospital hospital) {
        if (hospital == null) {
            binding.groupHospital.setVisibility(View.GONE);
            return;
        }

        binding.groupHospital.setVisibility(View.VISIBLE);
        binding.textHospitalName.setText(hospital.getName());
        binding.textHospitalReason.setText(hospital.routingReason != null ? hospital.routingReason : "Recommended Facility");
        binding.textHospitalDistance.setText(getString(R.string.hospital_distance_km, hospital.distanceKm));
    }

    private void confirmCancel() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_cancel_sos_title)
                .setMessage(R.string.dialog_cancel_sos_message)
                .setPositiveButton(R.string.action_confirm, (dialog, which) -> cancelSos())
                .setNegativeButton(R.string.action_dismiss, null)
                .show();
    }

    private void cancelSos() {
        if (emergencyId != null) {
            SOSService.cancel(this, emergencyId);
        }
        finish();
    }

    private void escalate() {
        // Notifies trusted contacts, nearby responders, and emergency
        // services, and begins continuous GPS tracking - see
        // "Automatic Escalation" in the security proposal.
        if (emergencyId != null) {
            SOSService.escalate(this, emergencyId);
        }
    }

    @Override
    public void onBackPressed() {
        // Prevent accidental dismissal of an active emergency; require
        // an explicit cancel action instead.
        confirmCancel();
    }
}
