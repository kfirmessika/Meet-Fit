package com.example.meet_fit.fragmetns;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.meet_fit.R;
import com.example.meet_fit.activities.MainActivity;
import com.example.meet_fit.models.User;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple {@link Fragment} subclass.
  factory method to
 * create an instance of this fragment.
 */
public class updateAuthentication extends Fragment {

    public updateAuthentication() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_update_authentication, container, false);
        Button btnUpdateCredentials = rootView.findViewById(R.id.btnUpdateCredentials);

        MainActivity main = (MainActivity) getActivity();

        btnUpdateCredentials.setOnClickListener(view -> {
            String etOldEmail = ((EditText)rootView.findViewById(R.id.etOldEmail)).getText().toString();
            String etOldPassword = ((EditText)rootView.findViewById(R.id.etOldPassword)).getText().toString();
            String etNewEmail = ((EditText)rootView.findViewById(R.id.etNewEmail)).getText().toString();
            String etNewPassword = ((EditText)rootView.findViewById(R.id.etNewPassword)).getText().toString();

            User newUser = new User(etNewEmail,etNewPassword);
            User oldUser = new User(etOldEmail,etOldPassword);

            inputValidation( oldUser, newUser,main);

        });
        return rootView;
    }

   private void inputValidation(User oldUser, User newUser,MainActivity main)
    {
        if(newUser.getEmail() == null || newUser.getEmail().isEmpty() )
        { newUser.setEmail(oldUser.getEmail());}

        if(newUser.getPassword() == null ||newUser.getPassword().isEmpty() )
        { newUser.setPassword(oldUser.getPassword()); }

        if((newUser.getPassword() == null || newUser.getPassword().isEmpty()) && (newUser.getEmail() == null || newUser.getEmail().isEmpty()))
        {
            Toast.makeText(getContext(),"Please enter new email or password or both",Toast.LENGTH_LONG).show();
            return;
        }
        if(!PasswordValidation(newUser.getPassword()))
        {
            Toast.makeText(getContext(),"The password is not valid",Toast.LENGTH_LONG).show();
            return;
        }
        if(!EmailValidation(newUser.getEmail()))
        {
            Toast.makeText(getContext(),"The email is not valid",Toast.LENGTH_LONG).show();
            return;
        }

        assert main != null;
        main.updateCredentials(newUser,oldUser);
    }

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

    private boolean EmailValidation(String email) {
        // Regular expression for basic email validation
        String emailRegex = "^[a-zA-Z0-9_+&-]+(?:\\.[a-zA-Z0-9_+&-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

}