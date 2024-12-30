package com.example.meet_fit.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.Navigation;

import com.example.meet_fit.R;
import com.example.meet_fit.adapters.RecyclerAdapter;
import com.example.meet_fit.models.Info;
import com.example.meet_fit.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    FirebaseDatabase database = FirebaseDatabase.getInstance();


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

    private  String getCurrentUserId() {
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            return mAuth.getCurrentUser().getUid();
        }
        return null;
    }

    public void loginToHomepage(View view)
    {
        Navigation.findNavController(view).navigate(R.id.action_login_to_homepage);
    }
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
        if (checkUser(user.getUser())) {
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

    public boolean checkUser(String username) {
        // Create a boolean wrapper to store the result
        final boolean[] userExists = {false};

        // Get a reference to the "users" node in your database
        DatabaseReference myRef = database.getReference("users").child(username);

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
                    Toast.makeText(this, "Data saved successfully!", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                            callback.onSaveComplete(true);
                        }
                    } else {
                        Toast.makeText(this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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

                // Loop through all UIDs in the "users" node
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    // Access the "info" node for each user
                    DataSnapshot infoSnapshot = userSnapshot.child("info");

                    // Only proceed if "info" exists
                    if (infoSnapshot.exists()) {
                        Info info = new Info();
                        // Get individual fields
                        info.setAge(infoSnapshot.child("age").getValue(String.class));
                        info.setFitLevel(infoSnapshot.child("fitLevel").getValue(String.class));
                        info.setAboutMe(infoSnapshot.child("aboutMe").getValue(String.class));
                        info.setLocation(infoSnapshot.child("location").getValue(String.class));
                        info.setPhoto(infoSnapshot.child("photo").getValue(String.class));

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

}