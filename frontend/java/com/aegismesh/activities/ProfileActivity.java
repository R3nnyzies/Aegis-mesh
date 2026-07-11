package com.aegismesh.activities;

import javax.naming.Context;

import com.aegismesh.R;
import com.aegismesh.models.User;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private static final String PREF_NAME = "AegisProfilePrefs";
    private static final String KEY_FULL_NAME = "fullName";
    private static final String KEY_AGE = "age";
    private static final String KEY_ALLERGIES = "allergies";
    private static final String KEY_CONDITIONS = "conditions";

    private EditText etFullName, etAge, etAllergies, etConditions;
    private Button btnSaveProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Assumes you have a layout file named activity_profile.xml
        setContentView(R.layout.activity_profile);

        etFullName = findViewById(R.id.etFullName);
        etAge = findViewById(R.id.etAge);
        etAllergies = findViewById(R.id.etAllergies);
        etConditions = findViewById(R.id.etConditions);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        // Load any previously saved data into the UI
        loadProfileData();

        btnSaveProfile.setOnClickListener(v -> saveProfileData());
    }

    /**
     * Reads from SharedPreferences and populates the EditTexts.
     */
    private void loadProfileData() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        etFullName.setText(prefs.getString(KEY_FULL_NAME, ""));
        etAge.setText(prefs.getString(KEY_AGE, ""));
        etAllergies.setText(prefs.getString(KEY_ALLERGIES, ""));
        etConditions.setText(prefs.getString(KEY_CONDITIONS, ""));
    }

    /**
     * Validates and saves the data securely on the device.
     */
    private void saveProfileData() {
        String name = etFullName.getText().toString().trim();
        String age = etAge.getText().toString().trim();
        String allergies = etAllergies.getText().toString().trim();
        String conditions = etConditions.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etFullName.setError("Full name is required for emergency responders.");
            return;
        }

        // If left blank, default to "None" so the AI knows for sure
        if (TextUtils.isEmpty(allergies)) allergies = "None known";
        if (TextUtils.isEmpty(conditions)) conditions = "None known";
        if (TextUtils.isEmpty(age)) age = "Unknown";

        SharedPreferences.Editor editor = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(KEY_FULL_NAME, name);
        editor.putString(KEY_AGE, age);
        editor.putString(KEY_ALLERGIES, allergies);
        editor.putString(KEY_CONDITIONS, conditions);
        editor.apply(); // apply() is asynchronous and safe for the UI thread

        Toast.makeText(this, "Medical Profile Saved Securely", Toast.LENGTH_SHORT).show();
        
        // Return to the previous screen (e.g., HomeActivity)
        finish();
    }

    /**
     * UTILITY METHOD: 
     * Call this from EmergencyActivity or MeshService to instantly get the User's Profile
     * when an SOS is triggered.
     */
    public static User getSavedUser(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        
        // If no profile exists yet, return a generic default (or handle it in the UI)
        String name = prefs.getString(KEY_FULL_NAME, "Unknown Victim");
        String age = prefs.getString(KEY_AGE, "Unknown");
        String allergies = prefs.getString(KEY_ALLERGIES, "None known");
        String conditions = prefs.getString(KEY_CONDITIONS, "None known");

        return new User(name, age, allergies, conditions);
    }
}