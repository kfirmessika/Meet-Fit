package com.example.meet_fit.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.meet_fit.R;
import com.example.meet_fit.adapters.RecyclerAdapter;
import com.example.meet_fit.models.Event;
import com.example.meet_fit.models.Info;
import com.example.meet_fit.models.User;
import com.example.meet_fit.models.dataAdapter;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    private String currentUserUid;


    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;


        });

    }

    public interface SaveInfoCallback {
        void onSaveComplete(boolean isSuccess);
    }

    public interface OnEventsFetchedListener {
        void onEventsFetched(ArrayList<Event> events);
    }

    public interface OnUsernameFetchedListener {
        void onUsernameFetched(String username);
    }

    private  String getCurrentUserId() {
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            return mAuth.getCurrentUser().getUid();
        }
        return null;
    }

    public void homepageToMyProfile()
    {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navController.navigate(R.id.action_homepage_to_myProfile2);
    }

    public void homepageToCreateEvent()
    {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navController.navigate(R.id.action_homepage_to_addEvent);
    }

    public void homepageToEvents()
    {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navController.navigate(R.id.action_homepage_to_events);
    }
    public void homepageToUpdateAuthentication()
    {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navController.navigate(R.id.action_homepage_to_updateAuthentication2);
    }

    public void homepageToLogin()
    {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navController.navigate(R.id.action_homepage_to_login);
    }

    public void loginToHomepage(View view)
    {
        Navigation.findNavController(view).navigate(R.id.action_login_to_homepage);
    }

    public void loginToInfo(View view)
    {Navigation.findNavController(view).navigate(R.id.action_login_to_userinfo);}
    public void registerToUser(View view) {
        Navigation.findNavController(view).navigate(R.id.action_register_to_userinfo);
    }

    public void infoToSignIn(View view)
    {
        Navigation.findNavController(view).navigate(R.id.action_userinfo_to_login2);
    }
    public void loginToRegister(View view)
    {
        Navigation.findNavController(view).navigate(R.id.action_login_to_register);
    }
    public void register(User user ,SaveInfoCallback callback) {
        // Check if user exists in the database
        if (checkUser()) {
            Toast.makeText(MainActivity.this, "This user already exists", Toast.LENGTH_LONG).show();
            callback.onSaveComplete(false);
        } else {
            // Use Firebase Authentication to create a user
            mAuth = FirebaseAuth.getInstance();
            mAuth.createUserWithEmailAndPassword(user.getEmail(), user.getPassword())
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Sign-up successful, get the authenticated user
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();

                            if (firebaseUser != null) {
                                String Uid = firebaseUser.getUid();
                                saveData(user, Uid,"user", isSuccess -> {
                                    if (isSuccess) {
                                        // Perform next actions on success
                                        Toast.makeText(this, "Data successfully saved and processed!", Toast.LENGTH_SHORT).show();
                                        callback.onSaveComplete(true);
                                    } else {
                                        // Handle failure case
                                        Toast.makeText(this, "Failed to save data2222. Please try again.", Toast.LENGTH_SHORT).show();
                                        callback.onSaveComplete(true);
                                    }
                                });
                            }
                        } else {
                            // If sign-up fails
                            Toast.makeText(MainActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            callback.onSaveComplete(false);
                        }
                    });
        }
    }

    public boolean checkUser() {
        // Create a boolean wrapper to store the result
        final boolean[] userExists = {false};

        // Get a reference to the "users" node in your database
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference myRef = database.getReference("users").child(uid);

        // Add a listener to check if the node exists
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // If the snapshot exists, the user exists in the database
                if (snapshot.exists()) {
                    userExists[0] = true;
                } else {
                    userExists[0] = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.err.println("Error: " + error.getMessage());
            }
        });

        // Return the result (might need to handle async in real-time scenarios)
        return userExists[0];
    }

    public void saveInfo(Info userInfo, SaveInfoCallback callback) {

        String userId = getCurrentUserId();
        saveData(userInfo, userId,"info", isSuccess -> {
            if (isSuccess) {
                // Perform next actions on success
                Toast.makeText(this, "Data successfully saved and processed!", Toast.LENGTH_SHORT).show();
                callback.onSaveComplete(true);
            } else {
                // Handle failure case
                Toast.makeText(this, "Failed to save data. Please try again.", Toast.LENGTH_SHORT).show();
                callback.onSaveComplete(true);
            }
        });

    }

    public void saveData(Object data, String userId,String section, SaveInfoCallback callback) {
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Invalid User ID.", Toast.LENGTH_SHORT).show();
            callback.onSaveComplete(false); // Notify failure
            return;
        }

        // Reference to Firebase
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users");

        // Save the data
        userRef.child(userId).child(section).setValue(data)
                .addOnSuccessListener(aVoid -> {
                    callback.onSaveComplete(true); // Notify success
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    callback.onSaveComplete(false); // Notify failure
                });

    }

    public void login(String email,String password ,SaveInfoCallback callback)
    {
        mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null ) {
                            Toast.makeText(this, "Logged in successfully!" , Toast.LENGTH_SHORT).show();
                            callback.onSaveComplete(true);

                        }
                    } else {
                        Toast.makeText(this, "Login failed: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                        callback.onSaveComplete(false);
                    }
                });
    }
    public void fetchDataFromFirebase(RecyclerAdapter recycleAdapter,List <Info> infoList,List <Info> originalList) {
        // Listen for data changes in the "users" node

        if (infoList!= null)
        {
            infoList.clear();
        }
       DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                assert firebaseUser != null;
                String uid = firebaseUser.getUid();
                // Loop through all UIDs in the "users" node
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    if(userSnapshot.getKey()!= null && userSnapshot.getKey().equals(uid))
                    {continue;}
                    // Access the "info" node for each user
                    DataSnapshot infoSnapshot = userSnapshot.child("info");

                    // Only proceed if "info" exists
                    if (infoSnapshot.exists()) {
                        Info info = new Info();
                        // Get individual fields
                        info.setUserName(infoSnapshot.child("userName").getValue(String.class));
                        info.setAge(infoSnapshot.child("age").getValue(String.class));
                        info.setFitLevel(infoSnapshot.child("fitLevel").getValue(String.class));
                        info.setAboutMe(infoSnapshot.child("aboutMe").getValue(String.class));
                        info.setLocation(infoSnapshot.child("location").getValue(String.class));
                        info.setPhoto(infoSnapshot.child("photo").getValue(String.class));
                        info.setPhoneNumber(infoSnapshot.child("phoneNumber").getValue(String.class));

                        // Build the activities list
                        List<String> activities = new ArrayList<>();
                        for (DataSnapshot activitySnapshot : infoSnapshot.child("activities").getChildren()) {
                            String activity = activitySnapshot.getValue(String.class);
                            if (activity != null) {
                                activities.add(activity);
                            }
                        }
                        info.setActivities(activities);

                        // Add the Info object to the list


                        infoList.add(info);
                        originalList.add(info);

                    }
                }

                // Notify the adapter that data has changed
                recycleAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Log the error for debugging
                Log.e("FirebaseError", "Error fetching data: " + error.getMessage());
            }
        });

    }

    public void fetchAndPopulateUserData(View view, ArrayAdapter<String> adapter ) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            currentUserUid = user.getUid();
        }
        if (currentUserUid == null) {
            // No user is logged in, handle accordingly

        }

        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentUserUid)
                .child("info");  // The "info" node

        // Use addListenerForSingleValueEvent if you only need it once
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Now call the method in the fragment
                    Info info = snapshot.getValue(Info.class);
                    assert info != null;
                    populateUI(info,view,adapter);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Log the error
                Log.e("FirebaseError", "Error fetching user data: " + error.getMessage());
            }
        });

    }

    private void populateUI(Info info , View rootView, ArrayAdapter<String> adapter ) {
         ImageView ivProfilePicture;
         Spinner spPhonePrefix;
         EditText etPhoneNumber;
         Spinner spAge;
         CheckBox cbGym, cbBiking, cbHiking, cbRunning, cbSwimming, cbOther;
         Spinner spFitnessLevel;
         Spinner etLocation;
         EditText etAboutMe;
         TextView name;



        ivProfilePicture  = rootView.findViewById(R.id.ivProfilePicture);
        spPhonePrefix     = rootView.findViewById(R.id.spPhonePrefix);
        etPhoneNumber     = rootView.findViewById(R.id.etPhoneNumber);
        spAge             = rootView.findViewById(R.id.spAge);
        cbGym             = rootView.findViewById(R.id.cbGym);
        cbBiking          = rootView.findViewById(R.id.cbBiking);
        cbHiking          = rootView.findViewById(R.id.cbHiking);
        cbRunning         = rootView.findViewById(R.id.cbRunning);
        cbSwimming        = rootView.findViewById(R.id.cbSwimming);
        cbOther           = rootView.findViewById(R.id.cbOther);
        spFitnessLevel    = rootView.findViewById(R.id.spFitnessLevel);
        etLocation        = rootView.findViewById(R.id.etLocation);
        etAboutMe         = rootView.findViewById(R.id.etAboutMe);
        name = rootView.findViewById(R.id.tvTitle);


        dataAdapter.base64ToImageView(info.getPhoto(), ivProfilePicture);

        String fullPhoneNumber = info.getPhoneNumber();
        if (fullPhoneNumber != null && fullPhoneNumber.length() >= 3) {
            String prefix = fullPhoneNumber.substring(0,3);
            String remaining = fullPhoneNumber.substring(3,10);

            // Set spinner selection by prefix
            setSpinnerToValue(spPhonePrefix, prefix);

            // Fill the EditText with the remaining digits
            etPhoneNumber.setText(remaining);

        }

        // 3. Age
        //    Set the spinner to the stored age
        setSpinnerToValue(spAge, info.getAge());

        // 4. Activities (List<String>)
        //    Check each checkbox if its corresponding string is in the activities list
        List<String> activities = info.getActivities();
        if (activities != null) {
            cbGym.setChecked(activities.contains("Gym"));
            cbBiking.setChecked(activities.contains("Biking"));
            cbHiking.setChecked(activities.contains("Hiking"));
            cbRunning.setChecked(activities.contains("Running"));
            cbSwimming.setChecked(activities.contains("Swim"));
            cbOther.setChecked(activities.contains("Other"));
        }

        // 5. Fitness Level
        setSpinnerToValue(spFitnessLevel, info.getFitLevel());

        // 6. Location
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            String desiredOption = info.getLocation();
            int position = adapter.getPosition(desiredOption);
            if (position >= 0) {
                etLocation.setSelection(position);
            }
        }, 1000);
//        setSpinnerToValue(etLocation, info.getLocation());

        // 7. About Me
        etAboutMe.setText(info.getAboutMe());

        name.setText(info.getUserName());
    }

    private void setSpinnerToValue(Spinner spinner, String value) {
        if (value == null) return;

        SpinnerAdapter adapter = spinner.getAdapter();
        if (adapter == null) return;

        for (int i = 0; i < adapter.getCount(); i++) {
            if (value.equals(adapter.getItem(i).toString())) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    public void updateCredentials(User newUser, User oldUser) {

        // Get the currently logged-in Firebase user
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate input
        if (oldUser == null || TextUtils.isEmpty(oldUser.getEmail()) || TextUtils.isEmpty(oldUser.getPassword())) {
            Toast.makeText(this, "Please enter current email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Re-authenticate the user
        AuthCredential credential = EmailAuthProvider.getCredential(oldUser.getEmail(), oldUser.getPassword());
        firebaseUser.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if(!newUser.getEmail().equals(oldUser.getEmail())) {
                    updateEmail(firebaseUser, newUser.getEmail());
                }

                if(!newUser.getPassword().equals(oldUser.getPassword())) {
                    updatePassword(firebaseUser, newUser.getPassword());
                }

                // Update the database with the new user information
                updateDatabase(firebaseUser.getUid(), newUser);

                Toast.makeText(this, "Credentials updated successfully.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Authentication failed. Check your credentials.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateEmail(FirebaseUser user, String newEmail) {
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Use verifyBeforeUpdateEmail instead of updateEmail
        user.verifyBeforeUpdateEmail(newEmail).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Verification email sent to " + newEmail, Toast.LENGTH_LONG).show();
            } else {
                Exception exception = task.getException();
                if (exception != null) {
                    Toast.makeText(this, "Failed to send verification email: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Failed to send verification email for unknown reasons", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updatePassword(FirebaseUser user, String newPassword) {
        user.updatePassword(newPassword).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to update password", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateDatabase(String userId, User newUser) {

        saveData(newUser, userId,"user", isSuccess -> {
            if (isSuccess) {
                // Perform next actions on success
                Toast.makeText(this, "Data updated successfully", Toast.LENGTH_SHORT).show();

            } else {
                // Handle failure case
                Toast.makeText(this, "Failed to save data. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void checkIfUsernameExists(String usernameToCheck, SaveInfoCallback callback) {
        // Reference to the "info" node in Firebase
        DatabaseReference infoRef = FirebaseDatabase.getInstance().getReference("users");

        // Query to check if any user has the given username
        infoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String existingUsername = userSnapshot.child("info").child("userName").getValue(String.class);
                        if (usernameToCheck.equals(existingUsername)) {
                            // Username exists
                            callback.onSaveComplete(true);
                            return;
                        }
                    }
                }
                // Username does not exist
                callback.onSaveComplete(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors (e.g., network issues or permission errors)

            }
        });
    }

    public void checkFullSignIn(SaveInfoCallback callback)
    {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // No user is logged in
            callback.onSaveComplete(false);
            return;
        }

        // Get the user's UID
        String uid = currentUser.getUid();

        // Reference the "info" node for the current user
        DatabaseReference infoRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("info");

        // Check if the "info" node exists
        infoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                callback.onSaveComplete(snapshot.exists());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void saveEventToFirebase(Event event) {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "No user logged in!", Toast.LENGTH_LONG).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference usersRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("events");

        // Create a map to store each field manually
        // so that `date` is saved as a long, and `time` as a String.
        long dateTimestamp = 0L;
        if (event.getDate() != null) {
            dateTimestamp = event.getDate().getTime();
        }

        String timeString = null;
        if (event.getTime() != null) {
            // Convert LocalTime to a String (e.g., "14:30")
            timeString = event.getTime().toString();
            // or use a formatter if you want a specific pattern
            // timeString = event.getTime().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        }

        Map<String, Object> eventMap = new HashMap<>();
        eventMap.put("activity",   event.getActivity());
        eventMap.put("fitLevel",   event.getFitLevel());
        eventMap.put("aboutEvent", event.getAboutEvent());
        eventMap.put("location",   event.getLocation());
        eventMap.put("date",       dateTimestamp);
        eventMap.put("time",       timeString);// Stored as a number
        eventMap.put("uId",   userId);// Stored as a number
        eventMap.put("eventId",    event.getEventId());

        eventMap.put("participants", event.getParticipants());// Stored as a string

        // Determine the next event key (e.g., event1, event2, ...)
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();
                String eventKey = "event" + (count + 1);

                // Save the map instead of the raw Event object
                usersRef.child(eventKey).setValue(eventMap)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(MainActivity.this,
                                    "Event saved successfully!",
                                    Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(MainActivity.this,
                                    "Failed to save event: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this,
                        "Database error: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    public void fetchAllEvents(final OnEventsFetchedListener listener) {


        DatabaseReference usersRef =  FirebaseDatabase.getInstance().getReference().child("users");
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Event> allEventsList = new ArrayList<>();

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    // Grab 'events' node for this user
                    DataSnapshot eventsSnapshot = userSnapshot.child("events");
                    if (eventsSnapshot.exists()) {
                        for (DataSnapshot eventChild : eventsSnapshot.getChildren()) {
                            Event event = parseEventSnapshot(eventChild);
                            if (event != null) {
                                allEventsList.add(event);
                            }
                        }
                    }
                }

                // Once done, pass the array back via the listener
                if (listener != null) {
                    listener.onEventsFetched(allEventsList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this,
                        "Failed to fetch events: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();

                if (listener != null) {
                    // Return an empty list or handle error
                    listener.onEventsFetched(new ArrayList<>());
                }
            }
        });
    }

    /**
     * Convert DataSnapshot -> Event
     */
    private Event parseEventSnapshot(DataSnapshot snapshot) {
        try {
            String aboutEvent = snapshot.child("aboutEvent").getValue(String.class);
            String activity   = snapshot.child("activity").getValue(String.class);
            String fitLevel   = snapshot.child("fitLevel").getValue(String.class);
            String location   = snapshot.child("location").getValue(String.class);
            String userName   = snapshot.child("uId").getValue(String.class);
            String userId   = snapshot.child("eventId").getValue(String.class);
            List<String> participantsList = new ArrayList<>();
            DataSnapshot participantsSnap = snapshot.child("participants");
            if (participantsSnap.exists()) {
                for (DataSnapshot p : participantsSnap.getChildren()) {
                    String participant = p.getValue(String.class);
                    participantsList.add(participant);
                }
            }

            // date stored as a Long (timestamp)
            Long dateLong = snapshot.child("date").getValue(Long.class);
            Date date = (dateLong != null) ? new Date(dateLong) : null;

            // time stored as a string (e.g. "14:30:00")
            String timeStr = snapshot.child("time").getValue(String.class);
            LocalTime localTime = null;
            if (timeStr != null && !timeStr.isEmpty()) {
                localTime = LocalTime.parse(timeStr, timeFormatter);
            }

            return new Event(activity, localTime, fitLevel, aboutEvent, location, date,participantsList,userName,userId);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void getUsernameOfCurrentUser(OnUsernameFetchedListener listener) {
        // Check if user is logged in
        if (mAuth.getCurrentUser() == null) {
            listener.onUsernameFetched(null);
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        // Path: users -> userId -> info -> userName
        DatabaseReference userNameRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("info")
                .child("userName");

        userNameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String userName = snapshot.getValue(String.class);
                // Return the username to the caller via callback
                if (listener != null) {
                    listener.onUsernameFetched(userName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // On error, pass null or handle differently
                if (listener != null) {
                    listener.onUsernameFetched(null);
                }
            }
        });
    }




}