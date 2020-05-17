package com.myclass.school.ui;

import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.myclass.school.CommonUtils;
import com.myclass.school.R;
import com.myclass.school.viewmodels.DatabaseRepository;


public class LoginFragment extends Fragment {

    public LoginFragment() {
        // Required empty public constructor
    }


    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_login, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = getView();
        init();

    }

    private void init() {
        final DatabaseRepository repo = new DatabaseRepository();

        // hide fab button
        CommonUtils.fabVisibility(getActivity(), View.GONE);

        final TextInputEditText passwordEd = view.findViewById(R.id.login_password);
        final TextInputEditText emailEd = view.findViewById(R.id.login_email);

        // handle user tap login button
        view.findViewById(R.id.login_btn).setOnClickListener(v -> {

            // check input
            if (passwordEd.getText() == null || emailEd.getText() == null)
                return;

            final String pass = passwordEd.getText().toString();
            final String email = emailEd.getText().toString()
                    .trim().replaceAll("\\s", "");

            // check email pattern
            final boolean isValidEmail = Patterns.EMAIL_ADDRESS.matcher(email).matches();

            if (!isValidEmail) {
                CommonUtils.showMessage(view.getContext(), R.string.invalid_email);
                return;
            }
            // show progress bar
            final ContentLoadingProgressBar progressBar = view.findViewById(R.id.login_progress_bar);
            progressBar.setVisibility(View.VISIBLE);

            CommonUtils.hideKeypad(view);
            String id = email.substring(0, email.indexOf('@'));
            repo.getUsers().document(id)
                    .get().addOnCompleteListener(task -> {

                // true if user is in the database
                final boolean isValid = task.isSuccessful();
                if (isValid)
                    // login
                    repo.getAuth().signInWithEmailAndPassword(email, pass).addOnCompleteListener(loginTask -> {
                        if (loginTask.isSuccessful())
                            CommonUtils.restartApp(getActivity());
                        else // invalid email or password
                            invalidLogin();
                    });

                else
                    invalidLogin();

            });


        });


    }

    private void invalidLogin() {
        // user doesn't exist
        CommonUtils.showMessage(view.getContext(), R.string.invalid_user);
        // hide progress bar
        view.findViewById(R.id.login_progress_bar).setVisibility(View.GONE);
    }


    // options menu in action bar
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.language_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);

    }

    // change language when click
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.changeLanguage)
            changeLanguage();
        return super.onOptionsItemSelected(item);
    }

    private void changeLanguage() {
        if (getActivity() != null)
            CommonUtils.changeLanguage(getActivity());
    }

}
