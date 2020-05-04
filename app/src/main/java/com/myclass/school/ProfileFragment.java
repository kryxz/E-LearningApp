package com.myclass.school;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.myclass.school.data.Student;
import com.myclass.school.data.Teacher;
import com.myclass.school.data.User;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;


public class ProfileFragment extends Fragment {

    public ProfileFragment() {
        // Required empty public constructor
    }

    private View view;
    private UserViewModel model;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        init();

    }

    private void init() {
        if (getActivity() == null) return;

        model = ((MainActivity) getActivity()).model;

        AppCompatTextView username = view.findViewById(R.id.profile_username);
        AppCompatTextView userType = view.findViewById(R.id.profile_user_type);


        TextInputEditText userEmail = view.findViewById(R.id.profile_email);
        TextInputEditText userSubject = view.findViewById(R.id.profile_grade_subject);


        CircleImageView profilePic = view.findViewById(R.id.profile_pic);

        model.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user == null) return;

            // set email and name
            userEmail.setText(user.getEmail());
            username.setText(user.getName());


            // teacher
            if (user instanceof Teacher) {
                userSubject.setText(((Teacher) user).getSubject());
                userType.setText(getString(R.string.teacher));

                // student
            } else if (user instanceof Student) {
                userSubject.setText(((Student) user).getGrade());
                userSubject.setHint(R.string.grade);
                userType.setText(getString(R.string.student));
            }

            username.setOnClickListener(v -> editUserDialog(user));

            String photo = user.getPhotoUrl();
            if (photo != null)
                Picasso.get().load(photo).placeholder(R.drawable.ic_person).into(profilePic);

        });


        profilePic.setOnClickListener(v -> uploadPicture());

    }


    private void editUserDialog(User user) {

        // initialize dialog and its content
        final AlertDialog dialog = new AlertDialog.Builder(view.getContext()).create();

        final View layout =
                View.inflate(view.getContext(), R.layout.add_user_dialog, null);

        dialog.setView(layout);


        // get views
        final AppCompatTextView dialogTitle = layout.findViewById(R.id.admin_dialog_title);

        final TextInputEditText userNameEd = layout.findViewById(R.id.add_user_name);
        final TextInputEditText subjectEd = layout.findViewById(R.id.add_subject_name);


        // set title and name
        dialogTitle.setText(getString(R.string.edit));
        userNameEd.setText(user.getName());


        // set subject or grade
        if (user instanceof Teacher)
            subjectEd.setText(((Teacher) user).getSubject());
        else if (user instanceof Student)
            subjectEd.setText(((Student) user).getGrade());


        // buttons
        final AppCompatButton confirm = layout.findViewById(R.id.dialog_confirm_button);
        final AppCompatButton cancel = layout.findViewById(R.id.dialog_cancel_button);


        // hide dialog on cancel
        cancel.setOnClickListener(v -> dialog.dismiss());

        confirm.setOnClickListener(v -> {

            // validate input
            if (userNameEd.getText() == null || userNameEd.getText().length() == 0) {
                userNameEd.setError(getString(R.string.cannot_be_empty));
                return;
            }

            if (subjectEd.getText() == null || subjectEd.getText().length() == 0) {
                subjectEd.setError(getString(R.string.cannot_be_empty));
                return;
            }

            // get data from input
            final String name = userNameEd.getText().toString().trim();
            final String subjectOrGrade = subjectEd.getText().toString().trim();


            // update user object
            user.setName(name);
            user.setSubjectGrade(subjectOrGrade);

            // send updates to database
            model.updateUser(user);

            // hide dialog
            dialog.dismiss();
        });

        dialog.show();

    }


    // ask user if they want to change their profile pic
    // then send them to gallery
    private void uploadPicture() {

        // create intent to pick from photos
        Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        photoPickerIntent.setType("image/*");


        // show confirmation dialog
        Common.showConfirmDialog(view.getContext(), getString(R.string.change_img),
                getString(R.string.change_img_confirm),
                () -> startActivityForResult(photoPickerIntent, 1));


    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            // get image as a path
            Uri selectedImage = data.getData();

            AsyncTask.execute(() -> {
                try {
                    // convert to bitmap image
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(view.getContext()
                            .getContentResolver(), selectedImage);
                    setPicture(bitmap);

                } catch (IOException e) {
                    // tell user cannot upload
                    Common.showMessage(getContext(), R.string.cannot_upload);

                }

            });

        }


    }


    // upload chosen picture to database
    private void setPicture(Bitmap pic) {
        if (getActivity() == null) return;

        getActivity().runOnUiThread(() -> {
            // show user message about uploading image
            Common.showMessage(getContext(), R.string.uploading_pic);
        });

        // upload picture to database, and update user data!
        model.updateProfilePic(pic);

    }


}
