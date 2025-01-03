package com.example.meet_fit.models;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;

import com.example.meet_fit.activities.MainActivity;

import java.io.ByteArrayOutputStream;

public class dataAdapter {




    public static void validateInputs(Info userInfo, MainActivity main, View view) {
        // Validate phone number
        if (userInfo.getPhoneNumber() == null || userInfo.getPhoneNumber().isEmpty()) {
            Toast.makeText(view.getContext(), "Please enter a phone number.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (userInfo.getPhoneNumber().length() != 10) {
            Toast.makeText(view.getContext(), "Please make sure the phone number is 10 digits long.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate age
        if (userInfo.getAge() == null || userInfo.getAge().isEmpty() || userInfo.getAge().equals("Select Age")) {
            Toast.makeText(view.getContext(), "Please select a valid age.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate fitness level
        if (userInfo.getFitLevel() == null || userInfo.getFitLevel().isEmpty() || userInfo.getFitLevel().equals("Select Fitness Level")) {
            Toast.makeText(view.getContext(), "Please select your fitness level.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate About Me field
        if (userInfo.getAboutMe() == null || userInfo.getAboutMe().trim().isEmpty()) {
            Toast.makeText(view.getContext(), "Please enter some information about yourself.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate location
        if (userInfo.getLocation() == null || userInfo.getLocation().isEmpty() || userInfo.getLocation().equals("Select Location")) {
            Toast.makeText(view.getContext(), "Please select a location.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate activities
        if (userInfo.getActivities() == null || userInfo.getActivities().isEmpty()) {
            Toast.makeText(view.getContext(), "Please select at least one activity.", Toast.LENGTH_SHORT).show();
            return;
        }

        // All validations passed
        Toast.makeText(view.getContext(), "All inputs are valid.", Toast.LENGTH_SHORT).show();
        main.saveInfo(userInfo, isSuccess -> {
            if (isSuccess) {
                // Navigate to the next fragment only on success
                Toast.makeText(view.getContext(), "Data Updated Successfully!", Toast.LENGTH_SHORT).show();
            } else {
                // Handle failure case if needed
                Toast.makeText(view.getContext(), "Failed to save info. Please try again.", Toast.LENGTH_SHORT).show();
            }
        }); // Call the registration method
    }

    public static void openGallery(ActivityResultLauncher<Intent> pickImageLauncher) {
        // Create an intent to pick an image from the gallery
        Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Launch the intent with the ActivityResultLauncher
        pickImageLauncher.launch(pickIntent);
    }

    public static String imageViewToBase64(ImageView imageView) {
        // Step 1: Get the Bitmap from the ImageView
        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();

        // Step 2: Convert the Bitmap to a Byte Array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos); // Use PNG or JPEG
        byte[] imageBytes = baos.toByteArray();

        // Step 3: Encode the Byte Array to Base64 String
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }
}
