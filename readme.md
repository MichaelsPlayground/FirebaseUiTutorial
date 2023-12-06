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

### edit the user profile on Auth database

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

Get a list of all users on Realtime Database including yourself. The data is presented in a ListView. When clicking on an entry the 
chatroom with this user is opened.

### list user on database in RecyclerView (chat)

This is the preferred way of presenting database entries: get a list of all users on Realtime Database (your entry is excluded). If the 
user setup a profile image it is shown, together with his user name and his email address. The second row contains the last timestamp 
when the user was online in the Firebase UI Tutorial app. When clicking on an entry the chatroom with this user is opened.

Beneath the profile image is a small image - green means that the user is probably online, gray is "offline". 

Why do I say "probably" - the app recognizes when going to background and will change the entry, but when the user stops the internet connection 
the app has no more chance to send the "offline" information to the  Firebase database.

### list recent user messages in RecyclerView

This is a second database that collects all chat messages. This is for a simple reason: Without that list you don't know that you received a 
chat from a user (there is no "new chat" information).

### list user chatrooms in RecyclerView

The activity shows how to handle the last received messages from other users. It shows a sorted list of chatrooms, the chatroom with the youngest 
message is on the top.

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

The functionality is identical to the Realtime Firebase's implementation.

### list user chatrooms in RecyclerView

The functionality is identical to the Realtime Firebase's implementation.

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

## Error handling

When running the Query on chatrooms (FirestoreChatroomsActivity) you receive an error because Firestore is missing an index.

```plaintext
Query query = FirebaseUtils.getFirestoreAllChatroomCollectionReference()
                .whereArrayContains("userIds",FirebaseUtils.getCurrentUserId())
                .orderBy("lastMessageTime",Query.Direction.DESCENDING);
```

```plaintext
(23.0.1) [Firestore]: Listen for Query(target=Query(chatrooms where userIds array_contains # com.google.firestore.v1.Value@1a83ab28
integer_value: 0
string_value: "rpv1yNXRgnhCb7flsXBkyaTgSZ22" order by -lastMessageTime, -__name__);limitType=LIMIT_TO_FIRST) failed: Status{code=FAILED_PRECONDITION, description=The query requires an index. You can create it here: https://console.firebase.google.com/v1/r/project/fir-tutorial-365bc/firestore/indexes?create_composite=ClRwcm9qZWN0cy9maXItdHV0b3JpYWwtMzY1YmMvZGF0YWJhc2VzLyhkZWZhdWx0KS9jb2xsZWN0aW9uR3JvdXBzL2NoYXRyb29tcy9pbmRleGVzL18QARoLCgd1c2VySWRzGAEaEwoPbGFzdE1lc3NhZ2VUaW1lEAIaDAoIX19uYW1lX18QAg, cause=null}
onError
com.google.firebase.firestore.FirebaseFirestoreException: FAILED_PRECONDITION: The query requires an index. You can create it here: https://console.firebase.google.com/v1/r/project/fir-tutorial-365bc/firestore/indexes?create_composite=ClRwcm9qZWN0cy9maXItdHV0b3JpYWwtMzY1YmMvZGF0YWJhc2VzLyhkZWZhdWx0KS9jb2xsZWN0aW9uR3JvdXBzL2NoYXRyb29tcy9pbmRleGVzL18QARoLCgd1c2VySWRzGAEaEwoPbGFzdE1lc3NhZ2VUaW1lEAIaDAoIX19uYW1lX18QAg
	at com.google.firebase.firestore.util.Util.exceptionFromStatus(Util.java:117)
	at com.google.firebase.firestore.core.EventManager.onError(EventManager.java:166)
	at com.google.firebase.firestore.core.SyncEngine.removeAndCleanupTarget(SyncEngine.java:588)
	at com.google.firebase.firestore.core.SyncEngine.handleRejectedListen(SyncEngine.java:424)
	at com.google.firebase.firestore.core.MemoryComponentProvider$RemoteStoreCallback.handleRejectedListen(MemoryComponentProvider.java:99)
	at com.google.firebase.firestore.remote.RemoteStore.processTargetError(RemoteStore.java:562)
	at com.google.firebase.firestore.remote.RemoteStore.handleWatchChange(RemoteStore.java:446)
	at com.google.firebase.firestore.remote.RemoteStore.access$100(RemoteStore.java:53)
	at com.google.firebase.firestore.remote.RemoteStore$1.onWatchChange(RemoteStore.java:176)
	at com.google.firebase.firestore.remote.WatchStream.onNext(WatchStream.java:108)
	at com.google.firebase.firestore.remote.WatchStream.onNext(WatchStream.java:38)
	at com.google.firebase.firestore.remote.AbstractStream$StreamObserver.lambda$onNext$1$com-google-firebase-firestore-remote-AbstractStream$StreamObserver(AbstractStream.java:119)
	at com.google.firebase.firestore.remote.AbstractStream$StreamObserver$$ExternalSyntheticLambda2.run(Unknown Source:4)
	at com.google.firebase.firestore.remote.AbstractStream$CloseGuardedRunner.run(AbstractStream.java:67)
	at com.google.firebase.firestore.remote.AbstractStream$StreamObserver.onNext(AbstractStream.java:110)
	at com.google.firebase.firestore.remote.FirestoreChannel$1.onMessage(FirestoreChannel.java:125)
	at io.grpc.internal.ClientCallImpl$ClientStreamListenerImpl$1MessagesAvailable.runInternal(ClientCallImpl.java:658)
	at io.grpc.internal.ClientCallImpl$ClientStreamListenerImpl$1MessagesAvailable.runInContext(ClientCallImpl.java:643)
	at io.grpc.internal.ContextRunnable.run(ContextRunnable.java:37)
	at io.grpc.internal.SerializingExecutor.run(SerializingExecutor.java:123)
	at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:463)
	at java.util.concurrent.FutureTask.run(FutureTask.java:264)
	at java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.run(ScheduledThreadPoolExecutor.java:307)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1137)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:637)
	at com.google.firebase.firestore.util.AsyncQueue$SynchronizedShutdownAwareExecutor$DelayedStartFactory.run(AsyncQueue.java:229)
	at java.lang.Thread.run(Thread.java:1012)
Caused by: io.grpc.StatusException: FAILED_PRECONDITION: The query requires an index. You can create it here: https://console.firebase.google.com/v1/r/project/fir-tutorial-365bc/firestore/indexes?create_composite=ClRwcm9qZWN0cy9maXItdHV0b3JpYWwtMzY1YmMvZGF0YWJhc2VzLyhkZWZhdWx0KS9jb2xsZWN0aW9uR3JvdXBzL2NoYXRyb29tcy9pbmRleGVzL18QARoLCgd1c2VySWRzGAEaEwoPbGFzdE1lc3NhZ2VUaW1lEAIaDAoIX19uYW1lX18QAg
	at io.grpc.Status.asException(Status.java:541)
	at com.google.firebase.firestore.util.Util.exceptionFromStatus(Util.java:115)
	at com.google.firebase.firestore.core.EventManager.onError(EventManager.java:166) 
	at com.google.firebase.firestore.core.SyncEngine.removeAndCleanupTarget(SyncEngine.java:588) 
	at com.google.firebase.firestore.core.SyncEngine.handleRejectedListen(SyncEngine.java:424) 
	at com.google.firebase.firestore.core.MemoryComponentProvider$RemoteStoreCallback.handleRejectedListen(MemoryComponentProvider.java:99) 
	at com.google.firebase.firestore.remote.RemoteStore.processTargetError(RemoteStore.java:562) 
	at com.google.firebase.firestore.remote.RemoteStore.handleWatchChange(RemoteStore.java:446) 
	at com.google.firebase.firestore.remote.RemoteStore.access$100(RemoteStore.java:53) 
	at com.google.firebase.firestore.remote.RemoteStore$1.onWatchChange(RemoteStore.java:176) 
	at com.google.firebase.firestore.remote.WatchStream.onNext(WatchStream.java:108) 
	at com.google.firebase.firestore.remote.WatchStream.onNext(WatchStream.java:38) 
	at com.google.firebase.firestore.remote.AbstractStream$StreamObserver.lambda$onNext$1$com-google-firebase-firestore-remote-AbstractStream$StreamObserver(AbstractStream.java:119) 
	at com.google.firebase.firestore.remote.AbstractStream$StreamObserver$$ExternalSyntheticLambda2.run(Unknown Source:4) 
	at com.google.firebase.firestore.remote.AbstractStream$CloseGuardedRunner.run(AbstractStream.java:67) 
	at com.google.firebase.firestore.remote.AbstractStream$StreamObserver.onNext(AbstractStream.java:110) 
	at com.google.firebase.firestore.remote.FirestoreChannel$1.onMessage(FirestoreChannel.java:125) 
	at io.grpc.internal.ClientCallImpl$ClientStreamListenerImpl$1MessagesAvailable.runInternal(ClientCallImpl.java:658) 
	at io.grpc.internal.ClientCallImpl$ClientStreamListenerImpl$1MessagesAvailable.runInContext(ClientCallImpl.java:643) 
	at io.grpc.internal.ContextRunnable.run(ContextRunnable.java:37) 
	at io.grpc.internal.SerializingExecutor.run(SerializingExecutor.java:123) 
	at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:463) 
	at java.util.concurrent.FutureTask.run(FutureTask.java:264) 
	at java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.run(ScheduledThreadPoolExecutor.java:307) 
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1137) 
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:637) 
	at com.google.firebase.firestore.util.AsyncQueue$SynchronizedShutdownAwareExecutor$DelayedStartFactory.run(AsyncQueue.java:229) 
	at java.lang.Thread.run(Thread.java:1012) 

```

## Firebase Storage

*Cloud Storage for Firebase is built on fast and secure Google Cloud infrastructure for app developers who need to 
store and serve user-generated content, such as photos or videos.*

*Cloud Storage for Firebase is a powerful, simple, and cost-effective object storage service built for Google scale. 
The Firebase SDKs for Cloud Storage add Google security to file uploads and downloads for your Firebase apps, 
regardless of network quality.*

### Upload of files and images to Storage

The activity is using Android's system file chooser to select a file or image - the difference is in the intent 
media type. The upload process is equals for both and, depending on the file type chooser, the uploaded file is 
stored in a subfolder "files" or "images". The root folder is the user ID of the signed-in user.

An implementation note: I'm using the built-in file chooser because this way does not require an permission 
granting by the user.

After a successful upload the activity provides a **public downloadURL** of the file. You can copy the URL to the 
clipboard for later usage. If you share the URL (e.g. by sending the URL by email or chat) to someone other he can 
download the file using a regular browser without any password.

Please keep in mind that anybody who has access to this URL has a (reading) access to the file. If the URL is 
abused you have to delete the file.

See the note on storing upload file information on Realtime Database or Firestore Database below.

### Download of files and images from Storage

Depending on the file type chooser the activity shows the content of the subfolder "files" or "images". Please keep 
in mind that the only information about the file at this moment is the file name only (e.g. no file size is available). 
For additional information see the note on storing upload file information on Realtime Database or Firestore Database 
below.

After selecting a file the Android's system file chooser is called to selec a folder and filename for the saved file.

An implementation note: I'm using the built-in file chooser because this way does not require an permission
granting by the user.

### Note on storing upload file information on Realtime Database or Firestore Database

The Storage API is providing a "listAll" method to retrieve all files stored in a folder but this is a file name list 
only (e.g. no file size is provided at this moment). Of course it is possible to retrieve file meta data for each 
file name in a second step, but this is a time and bandwidth consuming action, especially when having a lot of files 
in the folder.

If you should need these information you may consider of storing the information about the uploaded file in the 
Realtime Database or Firestore Database. List the saved filed on the database and get the download URL from the 
database record. To test this functionality I'm providing two sample methods:

## Save the file information to Realtime Database

After uploading a file or image to Firebase Storage this button is enabled. The method retrieves the file meta data of 
the uploaded file and stores it in the Realtime Database.

## Save the file information to Firestore Database

After uploading a file or image to Firebase Storage this button is enabled. The method retrieves the file meta data of
the uploaded file and stores it in the Firestore Database.



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

Firestore vs. Realtime Database: Choosing the Right Database for Your Android App: https://medium.com/@myofficework000/firestore-vs-realtime-database-choosing-the-right-database-for-your-android-app-746447f19322

# Google Firebase Pricing

Pricing for Spark ("free") and Blaze ("paid"): https://firebase.google.com/pricing and https://firebase.google.com/support/faq#pricing

## What happens if I exceed Spark plan storage or download limits for Realtime Database?

To provide you with a predictable price, the resources available to you in the Spark plans are capped. This means that when you exceed 
any plan limit in any month, your app will be turned off to prevent any further resource usage and additional charges.

If you exceed the no-cost quota limit in a calendar month for any product, your project's usage of that specific product will be shut off 
for the remainder of that month. This applies to all apps registered with that Firebase project.

To use that specific product again, you'll need to wait until the next billing cycle or upgrade to the Blaze pricing plan.

Paid Google Cloud products and features (like Pub/Sub, Cloud Run, or BigQuery streaming for Analytics) are not available for projects on the Spark plan.

App Logo: https://pixabay.com/vectors/data-technology-digital-computer-6769727/ by symphysismarketing

See license summary: https://pixabay.com/service/license-summary/ and full license: https://pixabay.com/service/terms 

App icon background color: 9AE8FA



