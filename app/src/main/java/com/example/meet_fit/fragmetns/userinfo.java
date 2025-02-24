package com.example.meet_fit.fragmetns;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.meet_fit.R;
import com.example.meet_fit.activities.MainActivity;
import com.example.meet_fit.models.Info;
import com.example.meet_fit.models.dataAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONObject;

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
                    Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        // Use Glide to load the image with a circular crop
                        Glide.with(this)
                                .load(selectedImageUri)
                                .apply(RequestOptions.circleCropTransform()) // Apply circular crop
                                .into(imgGallery);
                    }
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

        List<String> cityList = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, cityList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        // Replace with your actual API key
        String apiKey = "AIzaSyDDMyJshO_uQ04CVRqs6NcGY6uV_L4chcQ";

        // Fetch cities
        fetchCities(apiKey, cityList, adapter);

        spLocation.setAdapter(adapter);

        spLocation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCity = cityList.get(position);
                if ("My Location".equals(selectedCity)) {
                    fetchUserLocation();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
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

            String image = dataAdapter.imageViewToBase64(photo);

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
            activities.add("Swim");
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

    private void fetchUserLocation() {
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext());

        // Check for location permissions
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Request permissions if not granted
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            return;
        }

        // Try to get the last known location
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                getCityNameFromCoordinates(latitude, longitude);
            } else {
                // If last location is null, request a fresh location update
                LocationRequest locationRequest = LocationRequest.create();
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                locationRequest.setInterval(10000); // 10 seconds interval
                locationRequest.setFastestInterval(5000); // 5 seconds fastest interval

                LocationCallback locationCallback = new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        if (locationResult == null) {
                            Toast.makeText(getContext(), "Unable to fetch location. Try again.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        for (Location loc : locationResult.getLocations()) {
                            if (loc != null) {
                                double lat = loc.getLatitude();
                                double lon = loc.getLongitude();
                                getCityNameFromCoordinates(lat, lon);
                                // Stop further updates once a location is obtained
                                fusedLocationProviderClient.removeLocationUpdates(this);
                                break;
                            }
                        }
                    }
                };

                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to fetch location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                    JSONArray addressComponents = firstResult.getJSONArray("address_components");

                    String cityName = "";
                    String countryName = "";

                    // Iterate through the address components to find the city and country
                    for (int i = 0; i < addressComponents.length(); i++) {
                        JSONObject component = addressComponents.getJSONObject(i);
                        JSONArray types = component.getJSONArray("types");

                        if (types.toString().contains("locality")) { // City
                            cityName = component.getString("long_name");
                        } else if (types.toString().contains("country")) { // Country
                            countryName = component.getString("long_name");
                        }
                    }

                    if (!cityName.isEmpty() && !countryName.isEmpty()) {
                        String cityAndCountry = cityName + ", " + countryName;
                        fetchedCityName = cityAndCountry;

                        // Update UI with the city and state
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "Your location: " + cityAndCountry, Toast.LENGTH_LONG).show()
                        );
                    } else {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "Could not fetch city and state", Toast.LENGTH_SHORT).show()
                        );
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Failed to fetch location name", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    private void fetchCities(String apiKey, List<String> cityList, ArrayAdapter<String> adapter) {
        String[] inputs = {"a", "b", "c", "d", "e", "f", "g", "h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z"}; // You can expand this array
        OkHttpClient client = new OkHttpClient();

        cityList.clear();
        cityList.add("");
        cityList.add("My Location");

        new Thread(() -> {
            for (String input : inputs) {
                String url = "https://maps.googleapis.com/maps/api/place/autocomplete/json?" +
                        "input=" + input +
                        "&types=(cities)&components=country:il&key=" + apiKey;

                try {
                    Request request = new Request.Builder().url(url).build();
                    Response response = client.newCall(request).execute();
                    String responseBody = response.body().string();

                    JSONObject jsonObject = new JSONObject(responseBody);
                    JSONArray predictions = jsonObject.getJSONArray("predictions");

                    synchronized (cityList) {
                        for (int i = 0; i < predictions.length(); i++) {
                            JSONObject prediction = predictions.getJSONObject(i);
                            String cityName = prediction.getString("description");

                            // Avoid duplicates
                            if (!cityList.contains(cityName)) {
                                cityList.add(cityName);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Notify the adapter on the main thread
            requireActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
        }).start();
    }

}


