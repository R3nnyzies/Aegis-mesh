package com.aegismesh.network;

import com.aegismesh.models.MedicalProfile;
import com.aegismesh.models.User;

public interface UserService {
    void getCurrentUser(ApiCallback<User> callback);
    void updateProfile(String fullName, MedicalProfile profile, ApiCallback<User> callback);
}
