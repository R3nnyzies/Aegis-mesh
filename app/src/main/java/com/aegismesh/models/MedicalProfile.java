package com.aegismesh.models;

import java.io.Serializable;

public class MedicalProfile implements Serializable {
    public String bloodGroup;
    public String[] allergies;
    public String[] chronicIllnesses;
    public String[] medications;

    public MedicalProfile() {
        this.allergies = new String[0];
        this.chronicIllnesses = new String[0];
        this.medications = new String[0];
    }

    public MedicalProfile(String bloodGroup, String[] allergies, String[] chronicIllnesses, String[] medications) {
        this.bloodGroup = bloodGroup;
        this.allergies = allergies != null ? allergies : new String[0];
        this.chronicIllnesses = chronicIllnesses != null ? chronicIllnesses : new String[0];
        this.medications = medications != null ? medications : new String[0];
    }

    public String allergiesCsv() {
        return joinCsv(allergies);
    }

    public String chronicIllnessesCsv() {
        return joinCsv(chronicIllnesses);
    }

    public String currentMedicationsCsv() {
        return joinCsv(medications);
    }

    private String joinCsv(String[] items) {
        if (items == null || items.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.length; i++) {
            sb.append(items[i]);
            if (i < items.length - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}
