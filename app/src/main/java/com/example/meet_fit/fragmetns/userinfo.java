package com.example.meet_fit.fragmetns;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.example.meet_fit.R;
import com.example.meet_fit.activities.MainActivity;
import com.example.meet_fit.models.Info;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link userinfo#newInstance} factory method to
 * create an instance of this fragment.
 */
public class userinfo extends Fragment {

    private ImageView imgGallery;
    private Button btnGallery;

    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    // 2) Get the image URI from the intent
                    Uri selectedImageUri = result.getData().getData();
                    // 3) Display the image in the ImageView
                    imgGallery.setImageURI(selectedImageUri);
                }
            });

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public userinfo() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment userinfo.
     */
    // TODO: Rename and change types and number of parameters
    public static userinfo newInstance(String param1, String param2) {
        userinfo fragment = new userinfo();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_userinfo, container, false);

        Button buttonSubmit = rootView.findViewById(R.id.btnSubmit);

        imgGallery = rootView.findViewById(R.id.ivProfilePicture);
        btnGallery = rootView.findViewById(R.id.upload_button);

        // Set click listener on the button
        btnGallery.setOnClickListener(view -> openGallery());



        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity main = (MainActivity) getActivity();
                String age = ((Spinner)rootView.findViewById(R.id.spAge)).getSelectedItem().toString();
                String fitLevel = ((Spinner)rootView.findViewById(R.id.spFitnessLevel)).getSelectedItem().toString();
                String aboutMe = ((EditText)rootView.findViewById(R.id.etAboutMe)).getText().toString();
                String location = ((Spinner)rootView.findViewById(R.id.etLocation)).getSelectedItem().toString();
                List<String> activities = getSelectedActivities(rootView);
                String digits = ((Spinner)rootView.findViewById(R.id.spPhonePrefix)).getSelectedItem().toString();
                String number = ((EditText)rootView.findViewById(R.id.etPhoneNumber)).getText().toString();
                String phoneNumber = digits.concat(number);
                ImageView photo = rootView.findViewById(R.id.ivProfilePicture);

                String image = imageViewToBase64(photo);

                assert main != null;
                Info userInfo = new Info( activities,  age,  fitLevel, aboutMe,  location,  phoneNumber,  image);
                validateInputs( userInfo, main,rootView);

            }
        });
        return rootView;
    }

    public List<String> getSelectedActivities(View view) {
        List<String> activities = new ArrayList<>();

        // Find the checkboxes by their IDs
        CheckBox cbGym = view.findViewById(R.id.cbGym);
        CheckBox cbBiking = view.findViewById(R.id.cbBiking);
        CheckBox cbHiking = view.findViewById(R.id.cbHiking);
        CheckBox cbRunning = view.findViewById(R.id.cbRunning);
        CheckBox cbSwimming = view.findViewById(R.id.cbSwimming);
        CheckBox cbOther = view.findViewById(R.id.cbOther);

        // Check if each checkbox is checked, and if so, add it to the list
        if (cbGym.isChecked()) {
            activities.add("Gym");
        }
        if (cbBiking.isChecked()) {
            activities.add("Biking");
        }
        if (cbHiking.isChecked()) {
            activities.add("Hiking");
        }
        if (cbRunning.isChecked()) {
            activities.add("Running");
        }
        if (cbSwimming.isChecked()) {
            activities.add("Swimming");
        }
        if (cbOther.isChecked()) {
            activities.add("Other");
        }

        return activities;
    }

    private void validateInputs(Info userInfo, MainActivity main, View view) {
            // Validate phone number
            if (userInfo.getPhoneNumber() == null || userInfo.getPhoneNumber().isEmpty()) {
                Toast.makeText(getActivity(), "Please enter a phone number.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (userInfo.getPhoneNumber().length() != 10) {
                Toast.makeText(getActivity(), "Please make sure the phone number is 10 digits long.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate age
            if (userInfo.getAge() == null || userInfo.getAge().isEmpty() || userInfo.getAge().equals("Select Age")) {
                Toast.makeText(getActivity(), "Please select a valid age.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate fitness level
            if (userInfo.getFitLevel() == null || userInfo.getFitLevel().isEmpty() || userInfo.getFitLevel().equals("Select Fitness Level")) {
                Toast.makeText(getActivity(), "Please select your fitness level.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate About Me field
            if (userInfo.getAboutMe() == null || userInfo.getAboutMe().trim().isEmpty()) {
                Toast.makeText(getActivity(), "Please enter some information about yourself.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate location
            if (userInfo.getLocation() == null || userInfo.getLocation().isEmpty() || userInfo.getLocation().equals("Select Location")) {
                Toast.makeText(getActivity(), "Please select a location.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate activities
            if (userInfo.getActivities() == null || userInfo.getActivities().isEmpty()) {
                Toast.makeText(getActivity(), "Please select at least one activity.", Toast.LENGTH_SHORT).show();
                return;
            }

            // All validations passed
            Toast.makeText(requireContext(), "All inputs are valid. Proceeding to register...", Toast.LENGTH_SHORT).show();
            main.saveInfo(userInfo, isSuccess -> {
                if (isSuccess) {
                    // Navigate to the next fragment only on success
                    main.infoToSignIn(view);
                } else {
                    // Handle failure case if needed
                    Toast.makeText(getActivity(), "Failed to save info. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }); // Call the registration method
        }

    private void openGallery() {
        // Create an intent to pick an image from the gallery
        Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Launch the intent with the ActivityResultLauncher
        pickImageLauncher.launch(pickIntent);
    }

    private String imageViewToBase64(ImageView imageView) {
        // Step 1: Get the Bitmap from the ImageView
        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();

        // Step 2: Convert the Bitmap to a Byte Array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos); // Use PNG or JPEG
        byte[] imageBytes = baos.toByteArray();

        // Step 3: Encode the Byte Array to Base64 String
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    private void base64ToImageView(String base64String, ImageView imageView) {
        // Step 1: Decode the Base64 String into a byte array
        byte[] imageBytes = Base64.decode(base64String, Base64.DEFAULT);

        // Step 2: Convert the byte array into a Bitmap
        Bitmap decodedBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

        // Step 3: Set the Bitmap to the ImageView
        imageView.setImageBitmap(decodedBitmap);
    }
}


