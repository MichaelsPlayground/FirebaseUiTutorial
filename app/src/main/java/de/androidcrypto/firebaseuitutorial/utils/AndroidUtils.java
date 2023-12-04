package de.androidcrypto.firebaseuitutorial.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import de.androidcrypto.firebaseuitutorial.models.UserModel;

public class AndroidUtils {

    public static String shortenString(String input, int maxLength) {
        if (input != null && input.length() > maxLength) {
            return input.substring(0, (maxLength - 3)) + " ..";
        } else {
            return input;
        }
    }

    public static void passUserModelAsIntent(Intent intent, UserModel model){
        intent.putExtra("username",model.getUserName());
        // todo change putExtra ("phone" to "email"
        intent.putExtra("phone",model.getUserMail());
        intent.putExtra("userId",model.getUserId());
        intent.putExtra("fcmToken",model.getDeviceToken());

    }

    public static UserModel getUserModelFromIntent(Intent intent){
        UserModel userModel = new UserModel();
        userModel.setUserName(intent.getStringExtra("username"));
        // todo change intent.getStringExtra("phone") to intent.getStringExtra("email")
        userModel.setUserMail(intent.getStringExtra("phone"));
        userModel.setUserId(intent.getStringExtra("userId"));
        userModel.setDeviceToken(intent.getStringExtra("fcmToken"));
        return userModel;
    }

    public static void setProfilePic(Context context, Uri imageUri, ImageView imageView){
        Glide.with(context).load(imageUri).apply(RequestOptions.circleCropTransform()).into(imageView);
    }
}
