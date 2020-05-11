package com.myclass.school.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.myclass.school.CommonUtils;
import com.myclass.school.MainActivity;
import com.myclass.school.R;
import com.myclass.school.data.ClassroomFile;
import com.myclass.school.data.Teacher;
import com.myclass.school.data.User;
import com.myclass.school.viewmodels.UserViewModel;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.GroupieViewHolder;
import com.xwray.groupie.Item;

/*
    A screen that contains a list to show classroom files!
    Teachers can add files from here.
 */
public class FilesFragment extends Fragment {


    // global fields used in several places
    private View view;
    private String classroomId;
    private UserViewModel model;
    private User user;

    public FilesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_files, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        init();
    }


    private void init() {
        if (getActivity() == null) return;
        // cannot enter without arguments
        if (getArguments() == null) return;


        // initialises view models
        model = ((MainActivity) getActivity()).userVM;
        classroomId = ClassroomFragmentArgs.fromBundle(getArguments()).getId();


        // current classroom id
        classroomId = ClassroomFragmentArgs.fromBundle(getArguments()).getId();


        // tab layout allows user to go to classroom page
        tabLayoutListener();


        // add file button shown only to teachers
        final View addFileButton = view.findViewById(R.id.add_file_button);

        // text hint that says no files found!
        final AppCompatTextView noFilesYet = view.findViewById(R.id.no_files_yet);

        // Recycler view for the list of files
        final RecyclerView rv = view.findViewById(R.id.files_rv);

        final GroupAdapter adapter = new GroupAdapter();

        // get user data from database!
        model.getUser().observe(getViewLifecycleOwner(), u -> {
            if (u == null) return;
            user = u;

            if (user instanceof Teacher) {
                // show add file button if user is a teacher!
                addFileButton.setOnClickListener(v -> chooseFile());
                addFileButton.setVisibility(View.VISIBLE);
            }
        });


        // get classroom files from database!
        model.getClassFiles(classroomId).observe(getViewLifecycleOwner(), classroomFiles -> {
            if (classroomFiles == null || classroomFiles.isEmpty()) {
                noFilesYet.setVisibility(View.VISIBLE);
                return;
            }

            noFilesYet.setVisibility(View.GONE);
            adapter.clear();

            for (ClassroomFile file : classroomFiles)
                adapter.add(new FileItem(file, getActivity()));

            rv.setAdapter(adapter);
        });


    }


    // A tabLayout to navigate between the classroom screen and files screen
    private void tabLayoutListener() {

        final TabLayout tabLayout = view.findViewById(R.id.tabs_layout);

        // Select files tab
        final TabLayout.Tab tab = tabLayout.getTabAt(1);
        if (tab != null)
            tab.select();

                /*
            Navigation options to easily navigate between
            the two screens without messing up the navigation stack
         */

        NavOptions navOptions = new NavOptions.Builder().setPopUpTo(R.id.classesFragment, false).build();


        // Send user to classroom screen when click on classroom tab
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0)
                    Navigation.findNavController(view).navigate(FilesFragmentDirections.goToPosts(classroomId), navOptions);

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Required override

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Required override

            }
        });

    }


    // enables teacher to send files to classroom!
    private void sendFileToClass(Uri path, String type) {
        if (getContext() == null) return;

        final AlertDialog dialog = new AlertDialog.Builder(getContext()).create();

        final View layout =
                View.inflate(getContext(), R.layout.dialog_upload_file, null);

        dialog.setView(layout);

        // get text fields and buttons in dialog
        final TextInputEditText fileNameEd = layout.findViewById(R.id.file_name_dialog);
        final TextInputEditText descriptionEd = layout.findViewById(R.id.file_description_dialog);

        final AppCompatButton confirm = layout.findViewById(R.id.dialog_confirm_button);
        final AppCompatButton cancel = layout.findViewById(R.id.dialog_cancel_button);


        confirm.setOnClickListener(v -> {

            // check input
            if (path == null || fileNameEd.getText() == null || descriptionEd.getText() == null)
                return;

            // get name and description from input
            String name = fileNameEd.getText().toString().trim();
            String description = descriptionEd.getText().toString().trim();

            if (name.isEmpty() || description.isEmpty())
                return;

            // action that tells user their file was sent!
            Runnable doneAction = () -> CommonUtils.showMessage(getContext(), R.string.file_sent);


            // execute upload process in background
            AsyncTask.execute(() -> model.uploadFile(path, classroomId, name, description, type, doneAction));


            CommonUtils.showMessage(getContext(), R.string.uploading_file);
            dialog.dismiss();

        });

        // cancel hides dialog
        cancel.setOnClickListener(v -> dialog.dismiss());
        // show upload file dialog
        dialog.show();


    }

    private void chooseFile() {
        // starts file picker to select a file
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, 2);


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // check result
        if (requestCode == 2 && resultCode == Activity.RESULT_OK && data != null) {
            if (data.getData() != null && getContext() != null && getActivity() != null) {
                Uri returnUri = data.getData();
                String mimeType = getContext().getContentResolver().getType(returnUri);
                // show upload dialog when file is selected
                sendFileToClass(returnUri, mimeType);

            }
        }

    }

    /*   A view holder for classroom files, displays data about files
         like name, upload date, and an icon depending on the file type*/
    static class FileItem extends Item<GroupieViewHolder> {

        private final ClassroomFile file;
        private final Activity activity;


        // fields required when choosing a file for a new assignment
        private boolean isFilePick = false;
        private Runnable action;

        // constructor
        FileItem(ClassroomFile f, Activity a) {
            file = f;
            activity = a;
        }

        // constructor for picking a file from the list
        FileItem(ClassroomFile f, Activity a, boolean isPick, Runnable r) {
            file = f;
            activity = a;
            isFilePick = isPick;
            action = r;
        }


        // file layout
        @Override
        public int getLayout() {
            return R.layout.classroom_file_item;
        }


        @Override
        public void bind(@NonNull GroupieViewHolder viewHolder, int position) {
            final View view = viewHolder.itemView;

            // get text fields
            final AppCompatImageView icon = view.findViewById(R.id.file_item_icon);
            final AppCompatTextView nameText = view.findViewById(R.id.file_item_name);
            final AppCompatTextView dateText = view.findViewById(R.id.post_item_date);
            final AppCompatTextView descriptionText = view.findViewById(R.id.file_item_description);


            // set data in text fields
            nameText.setText(file.getName());
            descriptionText.setText(file.getDescription());
            dateText.setText(CommonUtils.getTimeAsString(file.getDate()));


            // decide icon for file, with a random color
            int iconId = R.drawable.ic_file;
            int color = CommonUtils.getRandomColor(view.getContext(), position);
            String type = file.getType();

            if (type.contains("pdf"))
                iconId = R.drawable.ic_pdf;
            else if (type.contains("image"))
                iconId = R.drawable.ic_image;
            else if (type.contains("word"))
                iconId = R.drawable.ic_word;
            else if (type.contains("presentation"))
                iconId = R.drawable.ic_powerpoint;


            // set icon!
            icon.setImageDrawable(CommonUtils.tintDrawable(view.getContext(), iconId, color));

            view.setOnClickListener(v -> {
                // on click
                if (isFilePick) {
                    // file pick
                    CommonUtils.Temp.tempFile = file;
                    action.run();
                } else // download file
                    CommonUtils.downloadFile(activity, file.getDownloadUrl());


            });


        }


    }


}
