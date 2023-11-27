# Firebase UI tutorial

This is a sample app to demonstrate the usage of the 4 most used Firebase products - please visit the linked tutorials to setup 
the Firebase products in the Firebase console.

## Firebase Authentication

This app uses 2 Sign-In provider: **Email/Password** and **Google account** authentication.

This app is not using the "Email address verification".

Step-by-step tutorial for Authentication: 

https://firebaseopensource.com/projects/firebase/firebaseui-android/auth/readme/

## Firebase Realtime Database

Step-by-step tutorial for Realtime Database:

Setup the Realtime Database with this rule:
```plaintext
{
  "rules": {
    ".read": "auth.uid != null",
    ".write": "auth.uid != null"
  }
}
```

## Cloud Firestore Database

Step-by-step tutorial for Firestore Database: 

Setup the Cloud Firestore Database with this rule:

```plaintext
rules_version = '2';

service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

## Firebase Storage

Step-by-step tutorial for Storage:

Setup the Firebase Storage with this rule:

```plaintext
rules_version = '2';

// Craft rules based on data in your Firestore database
// allow write: if firestore.get(
//    /databases/(default)/documents/users/$(request.auth.uid)).data.isAdmin;
service firebase.storage {
  match /b/{bucket}/o {
    match /{allPaths=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```







## 

```plaintext
https://stackoverflow.com/a/77503882/8166854


de.androidcrypto.firebaseuitutorial
Firebase UI Tutorial
SHA1: 19:22:A4:D7:01:A8:3D:09:8F:04:93:E9:8E:21:92:2D:5A:5F:B0:54
```