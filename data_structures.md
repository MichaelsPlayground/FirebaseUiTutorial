# Data Structures in Realtime Database, Firestore Database and Firebase Storage

This document gives an overview about the paths to an information in the databases and storage.

# Realtime Database

```plaintext
Basic reference for Realtime Database
DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
```

## Chatrooms

## User Credentials

### Files entry used on Firebase Storage

```plaintext
ref.child("credentials")
      .child(userId)
         .child("files")
            .child(fileName)
               .setValue(fileInformationModel)
Note: fileInformationModel is saved as Map               
```

### Images entry used on Firebase Storage

```plaintext
ref.child("credentials")
      .child(userId)
         .child("images")
            .child(fileName)
               .setValue(fileInformationModel)
Note: fileInformationModel is saved as Map               
```

### Resized Images entry used on Firebase Storage

```plaintext
ref.child("credentials")
      .child(userId)
         .child("imagesResized")
            .child(fileName)
               .setValue(fileInformationModel)
Note: fileInformationModel is saved as Map               
```

## Messages

## Recent Messages

## Users


# Cloud Firestore Database

```plaintext
Basic reference for Cloud Firestore Database
FirebaseFirestore ref = FirebaseFirestore.getInstance();
```

## Chatrooms

## User Credentials

### Files entry used on Firebase Storage

```plaintext
ref.child("credentials")
      .child(userId)
         .child("files")
            .child(fileName)
               .setValue(fileInformationModel)
Note: fileInformationModel is saved as Map               
```

### Images entry used on Firebase Storage

```plaintext
ref.child("credentials")
      .child(userId)
         .child("images")
            .child(fileName)
               .setValue(fileInformationModel)
Note: fileInformationModel is saved as Map               
```

### Resized Images entry used on Firebase Storage

```plaintext
ref.child("credentials")
      .child(userId)
         .child("imagesResized")
            .child(fileName)
               .setValue(fileInformationModel)
Note: fileInformationModel is saved as Map               
```

## Messages

## Recent Messages

## Users


# Firebase Storage

## Profile Images

## User ID entries

### User ID entry Files

### User ID entry Images

### User ID entry Resized Images





