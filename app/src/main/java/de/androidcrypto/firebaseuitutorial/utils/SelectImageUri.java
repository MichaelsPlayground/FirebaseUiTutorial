package de.androidcrypto.firebaseuitutorial.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import de.androidcrypto.firebaseuitutorial.database.DatabaseUpdateProfileImageActivity;

public class SelectImageUri extends AppCompatActivity {


    private Activity activity;
    private Context context;
    private final boolean doCropping;
    private Bitmap selectedFileBitmap, selectedFileCroppedBitmap;
    private final String CACHE_FOLDER = "crop";
    private String intermediateName = "1.jpg";
    private String resultName = "2.jpg";
    private Uri imageUriFull, imageUriCrop;
    private final String FILE_PROVIDER_AUTHORITY = "de.androidcrypto.firebaseuitutorial";
    private Uri intermediateProvider;
    private Uri resultProvider;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMediaActivityResultLauncher;
    private ActivityResultLauncher<Intent> cropActivityResultLauncher;
    private ActivityResultLauncher<Boolean> selectImageUriFinishedLauncher;


    public SelectImageUri(Context context, boolean doCropping) {
        this.context = context;
        this.doCropping = doCropping;
        // Launch the photo picker and let the user choose only images.
        https://developer.android.com/training/data-storage/shared/photopicker
        pickMediaActivityResultLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());

        /**
         * section for launcher
         */

        this.pickMediaActivityResultLauncher =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    // Callback is invoked after the user selects a media item or closes the
                    // photo picker.
                    if (uri != null) {
                        imageUriFull = uri;
                        saveBitmapFileToIntermediate(imageUriFull);


                        selectedFileBitmap = loadFromUri(intermediateProvider);
                        if (doCropping) {
                            onCropImage();
                        } else {
                            // launch the ActivityResultLauchner
                            selectImageUriFinishedLauncher.launch(true);
                        }

/*
                        //Bitmap rotated = rotateBitmap(getResizedBitmap(inputImage, 800), imageUriFull);
                        Bitmap rotated = getResizedBitmap(inputImage, 500);
                        profileImageView.setImageBitmap(rotated);

                        int height = profileImageView.getHeight();
                        int width = profileImageView.getWidth();

                        //Bitmap inputImage = uriToBitmap(imageUriFull);
                        String imageInfo = "height: " + height + " width: " + width + " resolution: " + (height * width) +
                                "\nOriginal Bitmap height: " + inputImage.getHeight() + " width: " + inputImage.getWidth() +
                                " res: " + (inputImage.getHeight() * inputImage.getWidth());
                        //tvFull.setText(imageInfo);
                        //Log.d(TAG, "imageInfo: " + imageInfo);
                        */
                    } else {
                        //Log.d(TAG, "No media selected");
                        selectImageUriFinishedLauncher.launch(false);
                    }
                });

        this.cropActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        imageUriCrop = resultProvider;
                        selectedFileCroppedBitmap = loadFromUri(resultProvider);
                        if (imageUriCrop != null) {
                            selectImageUriFinishedLauncher.launch(true);
                        } else {
                            selectImageUriFinishedLauncher.launch(false);
                        }
                    }
                });
    }

    public Bitmap getBitmapFromImage() {
        return selectedFileBitmap;
    }

    public Bitmap getResizedBitmapFromImage(int maxSize) {
        return getResizedBitmap(selectedFileBitmap, maxSize);
    }

    public Bitmap getResizedCroppedBitmapFromImage(int maxSize) {
        return getResizedBitmap(selectedFileCroppedBitmap, maxSize);
    }

    public Bitmap getResizedBitmap(Bitmap bitmap, int maxSize) {
        if (maxSize < 100) return null;
        if (bitmap != null) {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            float bitmapRatio = (float) width / (float) height;
            if (bitmapRatio > 1) {
                width = maxSize;
                height = (int) (width / bitmapRatio);
            } else {
                height = maxSize;
                width = (int) (height * bitmapRatio);
            }
            return Bitmap.createScaledBitmap(bitmap, width, height, true);
        } else {
            return null;
        }
    }

    public Bitmap getCroppedBitmapFromImage() {
        return selectedFileCroppedBitmap;
    }

    private void saveBitmapFileToIntermediate(Uri sourceUri) {
        //Log.d(TAG, "saveBitmapFileToIntermediate from URI: " + sourceUri);
        try {
            Bitmap bitmap = loadFromUri(sourceUri);
            File imageFile = getPhotoFileUri(intermediateName);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                intermediateProvider = FileProvider.getUriForFile(SelectImageUri.this, FILE_PROVIDER_AUTHORITY + ".provider", imageFile);
            else
                intermediateProvider = Uri.fromFile(imageFile);

            OutputStream out = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.close();
            //Log.d(TAG, "intermediate file written to intermediateProvider: " + intermediateName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Bitmap loadFromUri(Uri photoUri) {
        Bitmap image = null;
        try {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1) {
                // on newer versions of Android, use the new decodeBitmap method
                ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(), photoUri);
                image = ImageDecoder.decodeBitmap(source);
            } else {
                // support older versions of Android by using getBitmap
                image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    // Returns the File for a photo stored on disk given the fileName
    public File getPhotoFileUri(String fileName) {
        // see https://developer.android.com/reference/androidx/core/content/FileProvider
        File mediaStorageDir = new File(getCacheDir(), CACHE_FOLDER);
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            //Log.d(TAG, "failed to create directory");
        }
        File file = new File(mediaStorageDir.getPath() + File.separator + fileName);
        //Log.d(TAG, "getPhotoFileUri for fileName: " + fileName + " is: " + file.getAbsolutePath());
        return file;
    }

    private void onCropImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            grantUriPermission("com.android.camera", intermediateProvider, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Intent intent = new Intent("com.android.camera.action.CROP");
            intent.setDataAndType(intermediateProvider, "image/*");

            List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);

            int size = 0;

            if (list != null) {
                grantUriPermission(list.get(0).activityInfo.packageName, intermediateProvider, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                size = list.size();
            }

            if (size == 0) {
                Toast.makeText(this, "Error, wasn't taken image!", Toast.LENGTH_SHORT).show();
            } else {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                intent.putExtra("crop", "true");
                //intent.putExtra("scale", true);
                intent.putExtra("aspectX", 1);
                intent.putExtra("aspectY", 1);
                File photoFile = getPhotoFileUri(resultName);
                // wrap File object into a content provider
                // required for API >= 24
                // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
                resultProvider = FileProvider.getUriForFile(SelectImageUri.this, FILE_PROVIDER_AUTHORITY + ".provider", photoFile);
                intent.putExtra("return-data", false);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, resultProvider);
                intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());

                Intent cropIntent = new Intent(intent);
                // this is using the default cropper
                ResolveInfo res = list.get(0);
                cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                cropIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                grantUriPermission(res.activityInfo.packageName, resultProvider, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                cropIntent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));

                /*
                if (rbChooseCropperApplicationFixed0.isChecked()) {
                    ResolveInfo res = list.get(0);
                    cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    cropIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    grantUriPermission(res.activityInfo.packageName, resultProvider, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    cropIntent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
                } else {
                    // granting the rights for all registered cropping apps
                    for (int i = 0; i < list.size(); i++) {
                        ResolveInfo res = list.get(i);
                        cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        cropIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        grantUriPermission(res.activityInfo.packageName, resultProvider, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }
                }
                 */
                cropActivityResultLauncher.launch(cropIntent);
            }
        } else {
            File photoFile = getPhotoFileUri(resultName);
            resultProvider = Uri.fromFile(photoFile);

            Intent intentCrop = new Intent("com.android.camera.action.CROP");
            intentCrop.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intentCrop.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intentCrop.setDataAndType(intermediateProvider, "image/*");
            intentCrop.putExtra("crop", "true");
            intentCrop.putExtra("scale", true);
            intentCrop.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
            intentCrop.putExtra("noFaceDetection", true);
            intentCrop.putExtra("return-data", false);
            intentCrop.putExtra(MediaStore.EXTRA_OUTPUT, resultProvider);
            cropActivityResultLauncher.launch(intentCrop);
        }
    }

}


