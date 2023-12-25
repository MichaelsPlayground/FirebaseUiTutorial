package de.androidcrypto.firebaseuitutorial.models;

// from Tutorial https://programtown.com/how-to-build-online-user-presence-system-in-android-using-realtime-database/
public class UserPresenceModel {
    public String name,onlineStatus;

    public UserPresenceModel() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public UserPresenceModel(String name, String onlineStatus) {
        this.name = name;
        this.onlineStatus=onlineStatus;
    }
}
