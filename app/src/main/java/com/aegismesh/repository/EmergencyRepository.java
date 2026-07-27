package com.aegismesh.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.aegismesh.models.Emergency;
import com.aegismesh.models.Hospital;
import com.aegismesh.models.Responder;
import com.aegismesh.models.TriageMessage;
import com.aegismesh.services.SOSService;

/**
 * Single source of truth for emergency state.
 *
 * Activities never read {@link SOSService}'s state directly and never hold
 * emergency state themselves - they call this repository to issue commands
 * (trigger/cancel/escalate) and observe its LiveData for updates. SOSService
 * still owns the actual SOS workflow (location lookup, backend delivery,
 * mesh fallback) and, in a follow-up refactor, will push its results in here
 * via the onXxx update methods below instead of updating its own static
 * LiveData fields as it does today.
 */
public class EmergencyRepository {

    private static volatile EmergencyRepository instance;

    private final MutableLiveData<Emergency> activeEmergency = new MutableLiveData<>();
    private final MutableLiveData<TriageMessage> triageMessage = new MutableLiveData<>();
    private final MutableLiveData<Responder> assignedResponder = new MutableLiveData<>();
    private final MutableLiveData<Hospital> recommendedHospital = new MutableLiveData<>();

    private EmergencyRepository() {
    }

    public static EmergencyRepository getInstance() {
        if (instance == null) {
            synchronized (EmergencyRepository.class) {
                if (instance == null) {
                    instance = new EmergencyRepository();
                }
            }
        }
        return instance;
    }

    // ---- State exposed to Activities (read-only) ----

    public LiveData<Emergency> getActiveEmergency() {
        return activeEmergency;
    }

    public LiveData<TriageMessage> getTriageMessages() {
        return triageMessage;
    }

    public LiveData<Responder> getAssignedResponder() {
        return assignedResponder;
    }

    public LiveData<Hospital> getRecommendedHospital() {
        return recommendedHospital;
    }

    // ---- Commands issued by Activities ----
    // SOSService still owns the intent contract and background workflow; this
    // repository is the seam Activities call through instead of naming
    // SOSService directly, so it is the only thing that has to change when
    // that workflow moves.

    public void trigger(@NonNull Context context) {
        SOSService.trigger(context);
    }

    public void cancel(@NonNull Context context, @NonNull String emergencyId) {
        SOSService.cancel(context, emergencyId);
    }

    public void escalate(@NonNull Context context, @NonNull String emergencyId) {
        SOSService.escalate(context, emergencyId);
    }

    // ---- Updates SOSService will push in here once refactored ----

    public void onEmergencyUpdated(@Nullable Emergency emergency) {
        activeEmergency.postValue(emergency);
    }

    public void onTriageMessageReceived(@NonNull TriageMessage message) {
        triageMessage.postValue(message);
    }

    public void onResponderAssigned(@NonNull Responder responder) {
        assignedResponder.postValue(responder);
    }

    public void onHospitalRecommended(@NonNull Hospital hospital) {
        recommendedHospital.postValue(hospital);
    }

    /** Resets all four streams to null, e.g. when an emergency is cancelled. */
    public void clear() {
        activeEmergency.setValue(null);
        triageMessage.setValue(null);
        assignedResponder.setValue(null);
        recommendedHospital.setValue(null);
    }
}
