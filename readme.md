# Firebase UI tutorial

This is a sample app to demonstrate the usage of the 4 most used Firebase products - please visit the linked tutorials to setup 
the Firebase products in the Firebase console.

*FirebaseUI is an open-source library for Android that allows you to quickly connect common UI elements to Firebase APIs.*

## Firebase Authentication

The authentication of a user is the "heart" of the application and access rights. All read and write operations 
on the Realtime Database, Cloud Firestore Database and Firebase Storage are permitted for signed in user.

### sign in

This app uses 2 Sign-In provider: **Email/Password** and **Google account** authentication.

The sign in data is stored within the app, so when later returning to the app the user is still signed in.

When FirebaseUI detects a new user a user dataset is stored in Realtime Database and Cloud Firestore Database.

### sign out

The user is signed out from Firebase.

### edit the user profile on Auth

This is editing the available data on the Auth database. You read the user ID, email, display name and photo URL.

If the photo URL is a valid image path the image is displaed.

Changing is allowed on display name and photo URL.

**Note: changing these values does not change the user datasets in Realtime Database and Cloud Firestore Database.**

### verification

When you click on the button an verification email is immediately sent to the email address of the user. When the user clicks on the link 
in the mail the verification status changes to "verified".

This app is showing but not actively using the "Email address verification".

### account deletion

As the deletion is a permanently action I implemented a confirmation dialog, after confirming the user is deleted on the Auth database.

**Note: this deletion does not remove any data on the Realtime Database and Cloud Firestore Database or Firebase Storage, this needs to 
get implemented by yourself !**

### additional data

Step-by-step tutorial for Authentication: 

https://firebaseopensource.com/projects/firebase/firebaseui-android/auth/readme/

## Firebase Realtime Database

This is the legacy database of Firebase but still under development. Please decide carefully if you use this database or opt for the 
new Cloud Firestore Database as it is a great expense to change your app later. If you need more information see this information from 
Firebase: https://firebase.google.com/docs/database/rtdb-vs-firestore. Just an additional note: although Firebase seems to be free there 
are some limitations on the (generous) free "Spark plan". For details see https://firebase.google.com/pricing and https://firebase.google.com/docs/firestore/pricing.

### edit user profile

Basically most of the data is a copy of the user profile in Auth database. Unfortunately only the (authenticated) user is been 
able to read the data, so we do need a dataset that is available in a database readable by other (authenticated) users.

As the user ID and the eser email are used to identify a user I don't allow to change them. The user name is the only one 
editable by typing a new value and press the "save data" button.

If you click on the user image (or the placeholder icon) you are been able to change the profile image. The "Photo Picker" 
will show up to select an image from the gallery, followed by the (default) image cropper. The cropped image itself is 
saved in **Firebase Storage** but the **DownloadUrl** is stored in the dataset. Please note: depending on your tutorial stage 
this functionality is not available in the beginning (as we need Firebase Storage that is introduced at a later point of time).

You may have noticed that this solutions is using a **File Provider** access to intermediately store the selected file before 
cropping and uploading. If you prefer the older ("legacy") solution see "edit user profile (legacy)".

Just a note on the data: the user dataset contains some elements that are not used within this tutorial.

### edit user profile (legacy)

The only difference to "edit user profile" is the image cropper. The newer solution is using the "device's built-in" image cropper 
like "Samsung Gallery" or "Google Photos". The legacy option is using the image cropper of Arthur (android-image-cropper). As the 
author named the project "unmaintained" you probably should not use this option for newer projects, but the implementation 
is very easy.

### List user on database (Listview)


### list user on database in RecyclerView (chat)


### list recent user messages in RecyclerView


### database presence check









### additional data

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

This database is the newer one and Firebase's default database. Before using this solution you should check if this database 
is the right choice (or use the "older" Realtime Database). Please have a look at the pricing plans as well for both databases.

### edit user profile

Basically most of the data is a copy of the user profile in Auth database. Unfortunately only the (authenticated) user is been
able to read the data, so we do need a dataset that is available in a database readable by other (authenticated) users.

As the user ID and the eser email are used to identify a user I don't allow to change them. The user name is the only one
editable by typing a new value and press the "save data" button.

If you click on the user image (or the placeholder icon) you are been able to change the profile image. The "Photo Picker"
will show up to select an image from the gallery, followed by the (default) image cropper. The cropped image itself is
saved in **Firebase Storage** but the **DownloadUrl** is stored in the dataset. Please note: depending on your tutorial stage
this functionality is not available in the beginning (as we need Firebase Storage that is introduced at a later point of time).

You may have noticed that this solutions is using a **File Provider** access to intermediately store the selected file before
cropping and uploading. If you prefer the older ("legacy") solution see "edit user profile (legacy)".

Just a note on the data: the user dataset contains some elements that are not used within this tutorial.

### edit user profile (legacy)

The only difference to "edit user profile" is the image cropper. The newer solution is using the "device's built-in" image cropper
like "Samsung Gallery" or "Google Photos". The legacy option is using the image cropper of Arthur (android-image-cropper). As the
author named the project "unmaintained" you probably should not use this option for newer projects, but the implementation
is very easy.

### list user on database in RecyclerView (chat)

Note: maybe you have noticed that there is no ListView choice, but FirebaseUi does not offer an implementation (helper) for that.

### list recent user messages in RecyclerView

### 


### additional data

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

Firebase UI: https://firebaseopensource.com/projects/firebase/firebaseui-android/

Firebase UI on GitHub: https://github.com/firebase/FirebaseUI-Android

build.gradle (:app):
```plaintext
    // FirebaseUI for Firebase Auth
    implementation 'com.firebaseui:firebase-ui-auth:8.0.2'
    // FirebaseUI for Firebase Realtime Database
    implementation 'com.firebaseui:firebase-ui-database:8.0.2'
    // FirebaseUI for Cloud Firestore
    implementation 'com.firebaseui:firebase-ui-firestore:8.0.2'
    // FirebaseUI for Cloud Storage
    implementation 'com.firebaseui:firebase-ui-storage:8.0.2'

```

settings.gradle:
```plaintext
...
buildscript {
    repositories {
        // Make sure that you have the following two repositories
        google()  // Google's Maven repository
        mavenCentral()  // Maven Central repository
    }
    dependencies {
        // Add the dependency for the Google services Gradle plugin
        // note: stay on version '4.3.15' - version '4.4.0' will fail !
        classpath 'com.google.gms:google-services:4.3.15'
    }
}
...
```



## 

```plaintext
https://stackoverflow.com/a/77503882/8166854


de.androidcrypto.firebaseuitutorial
Firebase UI Tutorial
SHA1: 19:22:A4:D7:01:A8:3D:09:8F:04:93:E9:8E:21:92:2D:5A:5F:B0:54
```

Fine Tutorial series: https://www.youtube.com/playlist?list=PLzLFqCABnRQftQQETzoVMuteXzNiXmnj8

Code: https://github.com/KODDevYouTube/ChatAppTutorial

CircleImageView: implementation 'de.hdodenhof:circleimageview:3.1.0'

Image cropper by Arthur: api 'com.theartofdev.edmodo:android-image-cropper:2.8.0'