package com.aegismesh.models;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class User implements Serializable {
    private String fullName;
    private String age;
    private String allergies;
    private String chronicConditions;

    public User(String fullName, String age, String allergies, String chronicConditions) {
        this.fullName = fullName;
        this.age = age;
        this.allergies = allergies;
        this.chronicConditions = chronicConditions;
    }

    public String getFullName() { return fullName; }
    public String getAge() { return age; }
    public String getAllergies() { return allergies; }
    public String getChronicConditions() { return chronicConditions; }

    /**
     * Converts the medical profile into the nested JSON object required by the FastAPI backend.
     */
    public JSONObject toProfileJsonObject() throws JSONException {
        JSONObject profileJson = new JSONObject();
        profileJson.put("age", age);
        profileJson.put("allergies", allergies);
        profileJson.put("chronic_conditions", chronicConditions);
        return profileJson;
    }
}