package com.example.meet_fit.fragmetns;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.meet_fit.R;
import com.example.meet_fit.activities.MainActivity;
import com.example.meet_fit.models.Event;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class addEvent extends Fragment {

    private Spinner spActivity, spFitnessLevel, spLocation;
    private EditText etDate, etTime, etAboutMe;
    private Button btnSubmit;
    private String fetchedCityName;
    private List<String> participants = new ArrayList<>();
    private String userName;
    // SimpleDateFormat for parsing the date string from EditText
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    // DateTimeFormatter for parsing the time string
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_add_event, container, false);

        spLocation = view.findViewById(R.id.etLocation);

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

        // Initialize UI
        spActivity = view.findViewById(R.id.spActivity);
        spFitnessLevel = view.findViewById(R.id.spFitnessLevel);
        etDate = view.findViewById(R.id.etDate);
        etTime = view.findViewById(R.id.etTime);
        etAboutMe = view.findViewById(R.id.etAboutMe);
        btnSubmit = view.findViewById(R.id.btnSubmit);

        // Set listeners
        etDate.setOnClickListener(v -> showDatePickerDialog());
        etTime.setOnClickListener(v -> showTimePickerDialog());

        btnSubmit.setOnClickListener(v -> validateAndCreateEvent());

        return view;
    }

    /**
     * Show a DatePickerDialog to select the date.
     */
    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Format: dd/MM/yyyy
                    String dateString = String.format(Locale.getDefault(), "%02d/%02d/%d",
                            selectedDay, (selectedMonth + 1), selectedYear);
                    etDate.setText(dateString);
                },
                year, month, day
        );
        dialog.show();
    }

    /**
     * Show a TimePickerDialog to select the time of day.
     */
    private void showTimePickerDialog() {
        // Default to current time
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog dialog = new TimePickerDialog(
                requireContext(),
                (view, selectedHour, selectedMinute) -> {
                    // Format: HH:mm (24-hour)
                    String timeString = String.format(Locale.getDefault(), "%02d:%02d",
                            selectedHour, selectedMinute);
                    etTime.setText(timeString);
                },
                hour,
                minute,
                true // true = 24-hour format; false = 12-hour format (AM/PM)
        );
        dialog.show();
    }

    /**
     * Validate user input and create the event if everything is correct.
     */
    private void validateAndCreateEvent() {
        // Get values from Spinners
        String activity = spActivity.getSelectedItem().toString().trim();
        String fitLevel = spFitnessLevel.getSelectedItem().toString().trim();
        String location = spLocation.getSelectedItem().toString().trim();
        if (location.equals("My Location")) {
            location = fetchedCityName;
        }
        String dateStr = etDate.getText().toString().trim();
        String timeStr = etTime.getText().toString().trim();
        String aboutEvent = etAboutMe.getText().toString().trim();

        // Check for empty fields
        if (TextUtils.isEmpty(activity) || activity.equalsIgnoreCase("Select Activity")) {
            showError("Please select an activity.");
            return;
        }
        if (TextUtils.isEmpty(fitLevel) || fitLevel.equalsIgnoreCase("Activity Fitness Level")) {
            showError("Please select a fitness level.");
            return;
        }
        if (TextUtils.isEmpty(location) || location.equalsIgnoreCase("Choose A location")) {
            showError("Please select a location.");
            return;
        }
        if (TextUtils.isEmpty(dateStr)) {
            showError("Please select a date.");
            return;
        }
        if (TextUtils.isEmpty(timeStr)) {
            showError("Please select a time.");
            return;
        }
        if (TextUtils.isEmpty(aboutEvent)) {
            showError("Please provide some details about the activity.");
            return;
        }

        // Convert date string to Date object
        Date dateObject;
        try {
            dateObject = dateFormat.parse(dateStr);
        } catch (ParseException e) {
            showError("Invalid date format. Please select a valid date.");
            return;
        }

        // Convert time string to LocalTime object
        LocalTime timeObject;
        try {
            timeObject = LocalTime.parse(timeStr, timeFormatter);
        } catch (Exception e) {
            showError("Invalid time format. Please select a valid time.");
            return;
        }

        // *** Start of Added Code: Check if the selected date and time are not in the past ***
        // Convert Date to LocalDate
        Instant instant = dateObject.toInstant();
        LocalDate eventDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();

        // Combine LocalDate and LocalTime to LocalDateTime
        LocalDateTime eventDateTime = LocalDateTime.of(eventDate, timeObject);

        // Get current LocalDateTime
        LocalDateTime now = LocalDateTime.now();

        // Compare eventDateTime with now
        if (eventDateTime.isBefore(now)) {
            showError("The selected date and time are in the past. Please choose a future date and time.");
            return;
        }
        // *** End of Added Code ***



        MainActivity mainActivity = (MainActivity) requireActivity();
        String finalLocation = location;
        mainActivity.getUsernameOfCurrentUser(username -> {
            if (username == null) {
                Toast.makeText(getContext(), "Failed to get username or user not logged in.", Toast.LENGTH_SHORT).show();
            } else {
                // 1) We have the username, so add it to the list

                participants.add(username);

                // 2) Now participants.get(0) is safe because the list has the user
                DatabaseReference eventsRef = FirebaseDatabase.getInstance().getReference("events");
                String eventId = eventsRef.push().getKey();

                // 3) Construct the Event inside the callback
                Event newEvent = new Event(
                        activity,
                        timeObject,
                        fitLevel,
                        aboutEvent,
                        finalLocation,
                        dateObject,
                        participants,     // the entire list
                        participants.get(0), // index 0 is the user we just added
                        eventId
                );

                // 4) Save the Event now that it's fully populated
                mainActivity.saveEventToFirebase(newEvent);
            }
        });
        // Call the MainActivity method to save to database
        // Make sure your activity is actually MainActivity or whichever holds the save method

    }


    /**
     * Show an error message (Toast or Snackbar).
     */
    private void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
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
        cityList.add("Mountain View, United States");


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