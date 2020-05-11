package com.myclass.school.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.textfield.TextInputEditText;
import com.myclass.school.CommonUtils;
import com.myclass.school.MainActivity;
import com.myclass.school.R;
import com.myclass.school.data.Student;
import com.myclass.school.data.Teacher;
import com.myclass.school.data.User;
import com.myclass.school.viewmodels.UserViewModel;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Arrays;

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
        if (getArguments() == null) return;

        model = ((MainActivity) getActivity()).userVM;


        final AppCompatTextView username = view.findViewById(R.id.profile_username);
        final AppCompatTextView userType = view.findViewById(R.id.profile_user_type);

        final AppCompatTextView userStatus = view.findViewById(R.id.profile_user_status);

        final AppCompatTextView privateMessage = view.findViewById(R.id.private_message_profile);

        final TextInputEditText userEmail = view.findViewById(R.id.profile_email);
        final TextInputEditText userSubject = view.findViewById(R.id.profile_grade_subject);

        final CircleImageView profilePic = view.findViewById(R.id.profile_pic);

        final String userId = ProfileFragmentArgs.fromBundle(getArguments()).getUserId();

        final boolean isOwner = model.getUserId().equals(userId) || userId.equals("null");

        final char[] chatId = (model.getUserId() + userId).toCharArray();
        Arrays.sort(chatId);
        model.getUserById(userId).observe(getViewLifecycleOwner(), user -> {
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


            // can edit if user is owner of profile
            if (isOwner) {
                username.setOnClickListener(v -> editUserDialog(user));

                Drawable drawable = CommonUtils.tintDrawable(getContext(), R.drawable.ic_edit,
                        ContextCompat.getColor(view.getContext(), R.color.maz_blue));

                username.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);

                profilePic.setOnClickListener(v -> uploadPicture());


            } else {
                // can message
                privateMessage.setOnClickListener(v -> Navigation.findNavController(view).navigate(
                        ProfileFragmentDirections.startPrivateChat(userId, user.getName(), new String(chatId))));

                privateMessage.setVisibility(View.VISIBLE);
                // update title to username's profile}
                CommonUtils.updateTitle(getActivity(), getString(R.string.user_profile_arg, user.getName()));

            }


            // show profile image
            String photo = user.getPhotoUrl();
            if (photo != null)
                Picasso.get().load(photo).placeholder(R.drawable.ic_person).into(profilePic);

            // online status
            if (user.isOnline()) {
                userStatus.setText(getString(R.string.status_online));
                CommonUtils.tintDrawableTextView(userStatus, R.color.green);
            } else {
                userStatus.setText(getString(R.string.status_offline));
                CommonUtils.tintDrawableTextView(userStatus, R.color.red);

            }


        });


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
        CommonUtils.showConfirmDialog(view.getContext(), getString(R.string.change_img),
                getString(R.string.change_img_confirm),
                () -> startActivityForResult(photoPickerIntent, 1));


    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            // get image as a path
            Uri selectedImage = data.getData();

            // do in background
            AsyncTask.execute(() -> {
                try {
                    // convert to bitmap image
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(view.getContext()
                            .getContentResolver(), selectedImage);
                    setPicture(bitmap);

                } catch (IOException e) {
                    // tell user cannot upload
                    CommonUtils.showMessage(getContext(), R.string.cannot_upload);

                }

            });

        }


    }


    // upload chosen picture to database
    private void setPicture(Bitmap pic) {
        if (getActivity() == null) return;

        getActivity().runOnUiThread(() -> {
            // show user message about uploading image
            CommonUtils.showMessage(getContext(), R.string.uploading_pic);
        });

        // upload picture to database, and update user data!
        model.updateProfilePic(pic);

    }


}
