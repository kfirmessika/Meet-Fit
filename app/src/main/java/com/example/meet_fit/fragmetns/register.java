package com.example.meet_fit.fragmetns;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.meet_fit.R;
import com.example.meet_fit.activities.MainActivity;
import com.example.meet_fit.models.User;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class register extends Fragment {


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER


    // TODO: Rename and change types of parameters


    public register() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootview = inflater.inflate(R.layout.fragment_register, container, false);

        TextView terms = rootview.findViewById(R.id.termsText);

        terms.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                // Create a scrollable message with terms and conditions
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle("Terms and Conditions");

                // Create a TextView for the terms message
                TextView message = new TextView(requireContext());
                message.setText("Welcome to our Sports Meeting and Events App. By using this app, you agree to the following terms and conditions:\n\n"
                        + "1. You must be at least 18 years old to create or join events.\n"
                        + "2. The app is not responsible for any injury, harm, or damages caused during events.\n"
                        + "3. Users must behave respectfully and responsibly at all times.\n"
                        + "4. The app reserves the right to remove events or users violating terms.\n"
                        + "5. Your data will be used in accordance with our Privacy Policy.\n\n"
                        + "By continuing, you accept all terms and conditions.");
                message.setPadding(50, 50, 50, 50);
                message.setTextSize(16f);

                // Add a ScrollView to make the message scrollable
                ScrollView scrollView = new ScrollView(requireContext());
                scrollView.addView(message);

                builder.setView(scrollView);

                // Add an OK button to dismiss the dialog
                builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

                // Show the dialog
                builder.show();

            }
        });

        Button buttonContinue = rootview.findViewById(R.id.btnContinue);
        buttonContinue.setOnClickListener(view -> {
            MainActivity main = (MainActivity) getActivity();
            String email = ((EditText)rootview.findViewById(R.id.etEmail)).getText().toString();
            String password = ((EditText)rootview.findViewById(R.id.etPassword)).getText().toString();
            String passwordVerification = ((EditText)rootview.findViewById(R.id.etConfirmPassword)).getText().toString();
            assert main != null;
            User user = new User(email, password);
            ValidateSignUpInputs( user ,passwordVerification, rootview, main);

        });


        return rootview;
    }
    private boolean EmailValidation(String email) {
        // Regular expression for basic email validation
        String emailRegex = "^[a-zA-Z0-9_+&-]+(?:\\.[a-zA-Z0-9_+&-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    // 2. Password Validation
    private boolean PasswordValidation(String password) {
        // Password must be at least 8 characters, containing at least one uppercase letter,
        // one lowercase letter, one digit, and one special character.
        if (password.length() < 8) {
            return false;
        }
        String passwordRegex = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%?&])[A-Za-z\\d@$!%?&]{8,}$";
        Pattern pattern = Pattern.compile(passwordRegex);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }

    // 3. Password Verification Validation (check if passwords match)
    private boolean PasswordVerificationValidation(String password, String PasswordVerification) {
        // Check if both passwords match
        return password.equals(PasswordVerification);
    }

    private void ValidateSignUpInputs(User user, String passwordVerification, View view , MainActivity main) {
        // Check email validation

        if (!EmailValidation(user.getEmail())) {
            Toast.makeText(requireContext(), "Invalid email format. Please enter a valid email.", Toast.LENGTH_SHORT).show();
            return ;
        }

        // Check password validation
        if (!PasswordValidation(user.getPassword())) {
            Toast.makeText(requireContext(), "Password must be at least 8 characters long, contain at least one uppercase letter, one lowercase letter, one digit, and one special character.", Toast.LENGTH_LONG).show();
            return ;
        }

        // Check password verification
        if (!PasswordVerificationValidation(user.getPassword(), passwordVerification)) {
            Toast.makeText(requireContext(), "Passwords do not match. Please try again.", Toast.LENGTH_SHORT).show();
            return ;
        }
        CheckBox terms = view.findViewById(R.id.cbAgree);
        if(!terms.isChecked())
        {
            Toast.makeText(requireContext(), "Please agree to terms and conditions", Toast.LENGTH_SHORT).show();
            return ;
        }

        // All validations passed, proceed to register the user
        Toast.makeText(requireContext(), "All inputs are valid. Proceeding to register...", Toast.LENGTH_SHORT).show();
        main.register(user, isSuccess -> {
            if (isSuccess) {
                // Navigate to the next fragment only on success
                main.registerToUser(view);
            } else {
                // Handle failure case if needed
                Toast.makeText(getActivity(), "Failed to save info. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });

    }
}