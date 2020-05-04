package com.myclass.school;

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
import com.myclass.school.data.ClassroomFile;
import com.myclass.school.data.Teacher;
import com.myclass.school.data.User;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.GroupieViewHolder;
import com.xwray.groupie.Item;


public class FilesFragment extends Fragment {

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
        if (getArguments() == null) return;

        model = ((MainActivity) getActivity()).model;

        classroomId = ClassroomFragmentArgs.fromBundle(getArguments()).getId();


        classroomId = ClassroomFragmentArgs.fromBundle(getArguments()).getId();
        tabLayoutListener();


        View addFileButton = view.findViewById(R.id.add_file_button);

        final RecyclerView rv = view.findViewById(R.id.files_rv);
        final GroupAdapter adapter = new GroupAdapter();

        model.getUser().observe(getViewLifecycleOwner(), u -> {
            if (u == null) return;
            user = u;

            if (user instanceof Teacher) {
                addFileButton.setOnClickListener(v -> chooseFile());
                addFileButton.setVisibility(View.VISIBLE);
            }
        });


        model.getClassFiles(classroomId).observe(getViewLifecycleOwner(), classroomFiles -> {
            if (classroomFiles == null || classroomFiles.isEmpty()) return;
            adapter.clear();

            for (ClassroomFile file : classroomFiles)
                adapter.add(new FileItem(file, getActivity()));

            rv.setAdapter(adapter);
            rv.scrollToPosition(classroomFiles.size() - 1);
        });


    }

    private void tabLayoutListener() {

        final TabLayout tabLayout = view.findViewById(R.id.tabs_layout);

        final TabLayout.Tab tab = tabLayout.getTabAt(1);
        if (tab != null)
            tab.select();

        NavOptions navOptions = new NavOptions.Builder().setPopUpTo(R.id.mainFragment, false).build();
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0)
                    Navigation.findNavController(view).navigate(FilesFragmentDirections.goToPosts(classroomId), navOptions);

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }


    private void sendFileToClass(Uri path, String type) {
        if (getContext() == null) return;

        final AlertDialog dialog = new AlertDialog.Builder(getContext()).create();

        final View layout =
                View.inflate(getContext(), R.layout.dialog_upload_file, null);

        dialog.setView(layout);

        final TextInputEditText fileNameEd = layout.findViewById(R.id.file_name_dialog);
        final TextInputEditText descriptionEd = layout.findViewById(R.id.file_description_dialog);

        final AppCompatButton confirm = layout.findViewById(R.id.dialog_confirm_button);
        final AppCompatButton cancel = layout.findViewById(R.id.dialog_cancel_button);


        confirm.setOnClickListener(v -> {

            if (path == null || fileNameEd.getText() == null || descriptionEd.getText() == null)
                return;

            String name = fileNameEd.getText().toString().trim();
            String description = descriptionEd.getText().toString().trim();

            if (name.isEmpty() || description.isEmpty())
                return;

            Runnable doneAction = () -> Common.showMessage(getContext(), R.string.file_sent);


            AsyncTask.execute(() -> model.uploadFile(path, classroomId, name, description, type, doneAction));
            Common.showMessage(getContext(), R.string.uploading_file);
            dialog.dismiss();

        });

        cancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();


    }

    private void chooseFile() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        startActivityForResult(intent, 2);


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2 && resultCode == Activity.RESULT_OK && data != null) {
            if (data.getData() != null && getContext() != null && getActivity() != null) {
                Uri returnUri = data.getData();
                String mimeType = getContext().getContentResolver().getType(returnUri);
                sendFileToClass(returnUri, mimeType);

            }
        }
    }

    private static class FileItem extends Item<GroupieViewHolder> {

        private final ClassroomFile file;
        private final Activity activity;

        FileItem(ClassroomFile f, Activity a) {
            file = f;
            activity = a;
        }


        @Override
        public int getLayout() {
            return R.layout.classroom_file_item;
        }


        @Override
        public void bind(@NonNull GroupieViewHolder viewHolder, int position) {
            final View view = viewHolder.itemView;

            AppCompatImageView icon = view.findViewById(R.id.file_item_icon);
            AppCompatTextView nameText = view.findViewById(R.id.file_item_name);
            AppCompatTextView dateText = view.findViewById(R.id.post_item_date);
            AppCompatTextView descriptionText = view.findViewById(R.id.file_item_description);

            nameText.setText(file.getName());
            descriptionText.setText(file.getDescription());
            dateText.setText(Common.getTimeAgo(file.getDate()));

            if (file.getType().contains("pdf"))
                icon.setImageResource(R.drawable.ic_pdf);
            else
                icon.setImageResource(R.drawable.ic_file);

            view.setOnClickListener(v -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(file.getDownloadUrl()));
                activity.startActivity(browserIntent);
            });
        }

    }


}
