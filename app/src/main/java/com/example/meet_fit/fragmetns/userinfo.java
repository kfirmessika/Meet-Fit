package com.example.meet_fit.fragmetns;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.meet_fit.R;
import com.example.meet_fit.activities.MainActivity;
import com.example.meet_fit.models.Info;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;


import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class userinfo extends Fragment {

    private ImageView imgGallery;
    private String fetchedCityName = null;

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


    // TODO: Rename and change types of parameters

    public userinfo() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    
    @SuppressLint("CutPasteId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_userinfo, container, false);

        Button buttonSubmit = rootView.findViewById(R.id.btnSubmit);

        imgGallery = rootView.findViewById(R.id.ivProfilePicture);
        Button btnGallery = rootView.findViewById(R.id.upload_button);

        // Set click listener on the button
        btnGallery.setOnClickListener(view -> openGallery());

        Spinner spLocation = rootView.findViewById(R.id.etLocation);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.location,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spLocation.setAdapter(adapter);

        // Handle selection of "My Location"
        spLocation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String selectedOption = parent.getItemAtPosition(position).toString();
                if ("My Location".equals(selectedOption)) {
                    fetchUserLocation();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No action needed
            }
        });

        buttonSubmit.setOnClickListener(view -> {
            MainActivity main = (MainActivity) getActivity();
            String userName = ((TextView)rootView.findViewById(R.id.etUsername)).getText().toString();
            String age = ((Spinner)rootView.findViewById(R.id.spAge)).getSelectedItem().toString();
            String fitLevel = ((Spinner)rootView.findViewById(R.id.spFitnessLevel)).getSelectedItem().toString();
            String aboutMe = ((EditText)rootView.findViewById(R.id.etAboutMe)).getText().toString();
//                String location = ((Spinner)rootView.findViewById(R.id.etLocation)).getSelectedItem().toString();
            String location =spLocation.getSelectedItem().toString();
            List<String> activities = getSelectedActivities(rootView);
            String digits = ((Spinner)rootView.findViewById(R.id.spPhonePrefix)).getSelectedItem().toString();
            String number = ((EditText)rootView.findViewById(R.id.etPhoneNumber)).getText().toString();
            String phoneNumber = digits.concat(number);
            ImageView photo = rootView.findViewById(R.id.ivProfilePicture);

            if ("My Location".equals(location)) {
                // Use the fetched city name instead
                location = fetchedCityName; // fetchedCityName is from getCityNameFromCoordinates
            }

            String image = imageViewToBase64(photo);

            assert main != null;
            Info userInfo = new Info( userName,  activities, age,  fitLevel,
                     aboutMe,  location,  phoneNumber, image);
            validateUserName( userInfo, main,rootView);

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

    private void validateUserName(Info userInfo, MainActivity main, View view)
    {
        if (userInfo.getUserName() == null || userInfo.getUserName().isEmpty()) {
            Toast.makeText(getActivity(), "Please enter a username.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the username exists
        main.checkIfUsernameExists(userInfo.getUserName(), isExists -> {
            if (isExists) {
                // Username exists; show a message and stop further execution
                Toast.makeText(getActivity(), "Username already exists.", Toast.LENGTH_SHORT).show();
            } else {
                // Username does not exist; continue with other validations
                validateInputs(userInfo, main, view);
            }
        });

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
        if (imageView.getDrawable() == null) {
            // Handle the null case (e.g., log it, return a default value, or show an error)
            Log.e("ImageViewError", "Drawable is null. Cannot convert to Base64.");
            return null;
        }
        // Step 1: Get the Bitmap from the ImageView
        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();

        // Step 2: Convert the Bitmap to a Byte Array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos); // Use PNG or JPEG
        byte[] imageBytes = baos.toByteArray();

        // Step 3: Encode the Byte Array to Base64 String
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    private void fetchUserLocation() {
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext());

        // Check for location permissions
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Request permissions
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            return;
        }

        // Get the last known location
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {

                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                getCityNameFromCoordinates(latitude, longitude);

            } else {
                Toast.makeText(getContext(), "Unable to fetch location. Try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getCityNameFromCoordinates(double latitude, double longitude) {
        String apiKey = "AIzaSyDDMyJshO_uQ04CVRqs6NcGY6uV_L4chcQ"; // Replace with your API key
        String url = "https://maps.googleapis.com/maps/api/geocode/json?latlng=" + latitude + "," + longitude + "&key=" + apiKey;

        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(url).build();
                Response response = client.newCall(request).execute();
                assert response.body() != null;
                String responseBody = response.body().string();

                JSONObject jsonObject = new JSONObject(responseBody);
                JSONArray results = jsonObject.getJSONArray("results");

                if (results.length() > 0) {
                    JSONObject firstResult = results.getJSONObject(0);
                    String cityName = firstResult.getString("formatted_address");
                    fetchedCityName = cityName;

                    // Update UI with the city name
                    requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Your location: " + cityName, Toast.LENGTH_LONG).show());
                }
            } catch (Exception e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Failed to fetch location name", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

}


