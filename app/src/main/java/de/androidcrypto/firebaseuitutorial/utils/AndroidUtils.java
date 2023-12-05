package de.androidcrypto.firebaseuitutorial.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.snackbar.Snackbar;

import de.androidcrypto.firebaseuitutorial.R;
import de.androidcrypto.firebaseuitutorial.models.UserModel;

public class AndroidUtils {

    private static final String INTENT_USER_ID = "userId";
    private static final String INTENT_USER_NAME = "userName";
    private static final String INTENT_USER_EMAIL = "userEmail";
    private static final String INTENT_USER_PHOTO_URL = "userPhotoUrl";
    private static final String INTENT_USER_DEVICE_TOKEN = "userDeviceToken";

    public static String shortenString(String input, int maxLength) {
        if (input != null && input.length() > maxLength) {
            return input.substring(0, (maxLength - 3)) + " ..";
        } else {
            return input;
        }
    }

    public static void passUserModelAsIntent(Intent intent, UserModel model){
        intent.putExtra(INTENT_USER_ID, model.getUserId());
        intent.putExtra(INTENT_USER_NAME, model.getUserName());
        intent.putExtra(INTENT_USER_EMAIL, model.getUserMail());
        intent.putExtra(INTENT_USER_PHOTO_URL, model.getUserPhotoUrl());
        intent.putExtra(INTENT_USER_DEVICE_TOKEN, model.getDeviceToken());
    }

    public static UserModel getUserModelFromIntent(Intent intent){
        UserModel userModel = new UserModel();
        userModel.setUserId(intent.getStringExtra(INTENT_USER_ID));
        userModel.setUserName(intent.getStringExtra(INTENT_USER_NAME));
        userModel.setUserMail(intent.getStringExtra(INTENT_USER_EMAIL));
        userModel.setUserPhotoUrl(intent.getStringExtra(INTENT_USER_PHOTO_URL));
        userModel.setDeviceToken(intent.getStringExtra(INTENT_USER_DEVICE_TOKEN));
        return userModel;
    }

    public static void setProfilePic(Context context, Uri imageUri, ImageView imageView){
        Glide.with(context).load(imageUri).apply(RequestOptions.circleCropTransform()).into(imageView);
    }

    public static void showSnackbarGreenShort(View view, String message) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
        snackbar.setBackgroundTint(ContextCompat.getColor(view.getContext(), R.color.green_snackbar));
        snackbar.show();
    }
    public static void showSnackbarRedLong(View view, String message) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        snackbar.setBackgroundTint(ContextCompat.getColor(view.getContext(), R.color.red_snackbar));
        snackbar.show();
    }

    public static  void showToast(Context context,String message){
        Toast.makeText(context,message,Toast.LENGTH_SHORT).show();
    }
}
