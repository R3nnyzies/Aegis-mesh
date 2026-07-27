package com.aegismesh.services;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.aegismesh.database.EmergencyDbHelper;
import com.aegismesh.models.Emergency;

import androidx.work.ListenableWorker.Result;

import java.util.List;

/**
 * WorkManager Worker responsible for eventual background retransmission of any failed or pending
 * emergency alerts. Runs periodically (e.g. 15-minute intervals) when network connectivity is available.
 */
public class EmergencyResendWorker extends Worker {
    private static final String TAG = "EmergencyResendWorker";

    public EmergencyResendWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.i(TAG, "WorkManager backup recovery started: Checking for unsent emergency alerts...");
        Context context = getApplicationContext();
        EmergencyDbHelper dbHelper = EmergencyDbHelper.getInstance(context);

        List<Emergency> unsentList = dbHelper.getUnsentEmergencies();
        if (unsentList.isEmpty()) {
            Log.d(TAG, "No unsent emergencies found in local storage.");
            return Result.success();
        }

<<<<<<< HEAD
        Log.i(TAG, "Found " + unsentList.size() + " unsent emergency alert(s) in database. Attempting retransmission...");
        boolean allSentSuccessfully = true;

        for (Emergency emergency : unsentList) {
            try {
                Log.d(TAG, "Retransmitting emergency ID: " + emergency.getEmergencyId());
                // Fetch the saved profile from the device and send it with the emergency for AI Triage
                com.aegismesh.models.User victim = com.aegismesh.activities.ProfileActivity.getSavedUser(getApplicationContext());
                ApiClient.sendEmergency(emergency, victim);
                
                // If transmission succeeds, mark as delivered
                dbHelper.updateStatus(emergency.getEmergencyId(), Emergency.STATUS_DELIVERED);
                Log.i(TAG, "Emergency alert " + emergency.getEmergencyId() + " successfully delivered via background worker.");
            } catch (Exception e) {
                Log.e(TAG, "Failed to retransmit emergency alert " + emergency.getEmergencyId() + ": " + e.getMessage());
                // Leave it in local storage as PENDING or FAILED for the next execution
                dbHelper.updateStatus(emergency.getEmergencyId(), Emergency.STATUS_FAILED);
                allSentSuccessfully = false;
            }
        }

        if (allSentSuccessfully) {
            Log.i(TAG, "All local emergency alerts have been successfully synchronized.");
            return Result.success();
        } else {
            Log.w(TAG, "Some emergency alerts failed to send. Will retry in the next scheduled execution.");
            // Returning success since the periodic scheduler runs it every 15 minutes.
            return Result.success();
        }
=======
        Log.w(TAG, "Found " + unsentList.size() + " queued emergency alert(s), but the local database "
                + "does not retain the User profile required for backend delivery. Keeping them queued "
                + "until a profile-aware SOS request can send them safely.");
        return Result.success();
>>>>>>> origin/main
    }
}
