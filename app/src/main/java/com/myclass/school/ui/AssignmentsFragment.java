package com.myclass.school.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.myclass.school.CommonUtils;
import com.myclass.school.MainActivity;
import com.myclass.school.R;
import com.myclass.school.data.Assignment;
import com.myclass.school.data.Classroom;
import com.myclass.school.data.ClassroomFile;
import com.myclass.school.data.Notification;
import com.myclass.school.data.NotificationType;
import com.myclass.school.data.Submission;
import com.myclass.school.data.User;
import com.myclass.school.viewmodels.ClassroomVM;
import com.myclass.school.viewmodels.UserViewModel;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.GroupieViewHolder;
import com.xwray.groupie.Item;

import java.util.List;
import java.util.Random;
import java.util.UUID;


public class AssignmentsFragment extends Fragment {

    public AssignmentsFragment() {
        // Required empty public constructor
    }


    private View view;
    private ClassroomVM classroomVM;
    private UserViewModel model;
    private User user;


    // progress bar shown when uploading files as a submission
    private ProgressBar progressBar;

    // dialog text to show file name
    private TextInputEditText fileNameDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_assignments, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        init();
    }

    private void init() {
        if (getActivity() == null) return;

        // init fields
        model = ((MainActivity) getActivity()).userVM;
        classroomVM = ((MainActivity) getActivity()).classroomVM;

        final boolean isTeacher = model.getUserId().charAt(0) == 't';
        final LiveData<List<Classroom>> listLiveData = model.getMyClasses();

        // get progress bar
        progressBar = view.findViewById(R.id.assignments_progress_bar);

        progressBar.setVisibility(View.VISIBLE);

        final AppCompatTextView noAssignmentsText = view.findViewById(R.id.no_assignments_text);

        final RecyclerView rv = view.findViewById(R.id.assignments_rv);
        final GroupAdapter adapter = new GroupAdapter();

        // get classrooms from database
        listLiveData.observe(getViewLifecycleOwner(), classrooms -> {
            if (classrooms == null) return;

            if (classrooms.isEmpty()) {
                noAssignmentsText.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            } else
                noAssignmentsText.setVisibility(View.GONE);

            for (Classroom classroom : classrooms)
                getAssignmentsThenAdd(classroom.getId(), isTeacher, rv, adapter);


            progressBar.setProgress(new Random().nextInt(100));

            // doesn't observe database changes
            listLiveData.removeObservers(getViewLifecycleOwner());

        });

        // get user
        final LiveData<User> userLiveData = model.getUser();
        userLiveData.observe(getViewLifecycleOwner(), u -> {
            if (u == null) return;
            user = u;
            userLiveData.removeObservers(getViewLifecycleOwner());
        });


    }


    // adds assignments to recycler view
    private void getAssignmentsThenAdd(String id, boolean isTeacher,
                                       RecyclerView rv, GroupAdapter adapter) {


        final AppCompatTextView noAssignmentsText = view.findViewById(R.id.no_assignments_text);

        final LiveData<List<Assignment>> listLiveData = classroomVM.getAssignments(id);

        // observe assignments changes and add them to the adapter!
        listLiveData.observe(getViewLifecycleOwner(), assignments -> {
            if (assignments == null) return;

            for (int i = 0; i < assignments.size(); i++)
                adapter.add(new AssignmentItem(assignments.get(i), i + 1, isTeacher));

            rv.setAdapter(adapter);

            if (adapter.getItemCount() == 0)
                noAssignmentsText.setVisibility(View.VISIBLE);

            if (adapter.getItemCount() > 0)
                noAssignmentsText.setVisibility(View.GONE);

            progressBar.setVisibility(View.GONE);
        });


    }


    // lets user choose any file!
    private void chooseFile(TextInputEditText ed) {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, 2);

        fileNameDialog = ed;

    }


    // gets file chosen from user
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2 && resultCode == Activity.RESULT_OK && data != null) {
            if (data.getData() != null && getContext() != null && getActivity() != null) {
                Uri returnUri = data.getData();

                CommonUtils.Temp.fileUri = returnUri;
                CommonUtils.Temp.fileType = getContext().getContentResolver().getType(returnUri);

                fileNameDialog.setText(CommonUtils.queryName(getActivity().getContentResolver(),
                        CommonUtils.Temp.fileUri));

            }
        }
    }


    // a view holder for assignment data
    private class AssignmentItem extends Item<GroupieViewHolder> {

        private final Assignment assignment;
        private final int pos;
        private final boolean isTeacher;

        AssignmentItem(Assignment a, int p, boolean teacher) {
            assignment = a;
            pos = p;
            isTeacher = teacher;
        }


        @Override
        public int getLayout() {
            return R.layout.assignment_item;
        }

        @Override
        public void bind(@NonNull GroupieViewHolder viewHolder, int position) {
            final View view = viewHolder.itemView;


            // init fields
            final AppCompatTextView fileName = view.findViewById(R.id.assignment_file_title);

            final AppCompatTextView posText = view.findViewById(R.id.item_position_text);
            final AppCompatButton submitButton = view.findViewById(R.id.assignment_submit);

            CreateAssignmentsFragment.setUpAssignmentItem(view, assignment);


            // set position text. #1 #2
            posText.setText(getString(R.string.number_arg, pos));


            // if assignment has a file,
            // allow user to download that file
            if (assignment.getFile() != null && getActivity() != null) {
                fileName.setText(getString(R.string.tap_to_download_arg, fileName.getText()));
                fileName.setOnClickListener(v -> CommonUtils.downloadFile(getActivity(),
                        assignment.getFile().getDownloadUrl()));
            }


            if (assignment.containsSubmission(model.getUserId()))
                // user has submitted to the assignment, can download
                submitButton.setText(getString(R.string.download_submission));
            else // no submission!
                submitButton.setText(getString(R.string.submit));


            // teacher can view submissions
            if (isTeacher)
                submitButton.setText(getString(R.string.submissions));

            submitButton.setOnClickListener(v -> {
                if (isTeacher)
                    showSubmissions(view.getContext());
                else {
                    if (!assignment.containsSubmission(model.getUserId()))
                        showSubmitDialog(view.getContext());
                    else if (getActivity() != null) {
                        CommonUtils.downloadFile(getActivity(), assignment.downloadFile(model.getUserId()));
                    }

                }

            });


        }

        private void showSubmitDialog(Context context) {

            // initialize dialog and its content
            final AlertDialog dialog = new AlertDialog.Builder(context).create();

            final View layout =
                    View.inflate(context, R.layout.dialog_upload_file, null);

            dialog.setView(layout);

            final TextInputEditText fileNameEd = layout.findViewById(R.id.file_name_dialog);
            final TextInputEditText commentEd = layout.findViewById(R.id.file_description_dialog);

            final AppCompatButton confirm = layout.findViewById(R.id.dialog_confirm_button);
            final AppCompatButton cancel = layout.findViewById(R.id.dialog_cancel_button);

            commentEd.setHint(R.string.comment_hint);


            // choose file when click at fileName text
            fileNameEd.setFocusableInTouchMode(false);

            fileNameEd.setOnClickListener(v -> chooseFile(fileNameEd)
            );


            // cancel hides dialog
            cancel.setOnClickListener(v -> dialog.dismiss());


            confirm.setOnClickListener(v -> {

                // check input, and send submission to database
                if (CommonUtils.Temp.fileUri == null) {
                    fileNameEd.setError(getString(R.string.choose_file_error));
                    return;
                }

                ClassroomFile file = new ClassroomFile(
                        UUID.randomUUID().toString().substring(0, 10), // file id
                        user.getName(), // sender name
                        CommonUtils.queryName(context.getContentResolver(), CommonUtils.Temp.fileUri), // file name
                        CommonUtils.Temp.fileType,
                        getString(R.string.assignment_submission),
                        System.currentTimeMillis()
                );

                Submission submission = new Submission(file, model.getUserId());

                if (commentEd.getText() != null)
                    submission.setComment(commentEd.getText().toString());

                classroomVM.uploadSubmission(submission, assignment, progressBar);

                // send new submission notification to instructor
                Notification notification = new Notification(
                        getString(R.string.new_submission_title),
                        getString(R.string.new_submission, assignment.getTitle()),
                        System.currentTimeMillis(), assignment.getClassroomId(),
                        NotificationType.NEW_SUBMISSION

                );
                classroomVM.sendNotificationInstructor(notification, assignment.getClassroomId());

                dialog.dismiss();

                // show progress bar
                progressBar.setVisibility(View.VISIBLE);

            });

            dialog.show();
        }


        // a dialog that contains assignment submissions
        private void showSubmissions(Context context) {
            // initialize dialog and its content
            final AlertDialog dialog = new AlertDialog.Builder(context).create();

            final View layout =
                    View.inflate(context, R.layout.dialog_classroom_list, null);

            dialog.setView(layout);

            final RecyclerView rv = layout.findViewById(R.id.classroom_members_rv);
            final AppCompatTextView countText = layout.findViewById(R.id.members_count_dialog);

            final int count = assignment.getSubmissions().size();
            countText.setText(context.getString(R.string.submissions_arg, count));

            if (count == 1)
                countText.setText(countText.getText().subSequence(0,
                        countText.getText().length() - 1));

            countText.setText(context.getString(R.string.submissions_hint, countText.getText()));
            final GroupAdapter adapter = new GroupAdapter<>();

            for (Submission s : assignment.getSubmissions())
                adapter.add(new SubmissionItem(s));

            rv.setAdapter(adapter);
            dialog.show();
        }


        // a view holder class that contains submission data, filename, date etc
        private class SubmissionItem extends Item<GroupieViewHolder> {
            private final Submission submission;

            SubmissionItem(Submission s) {
                submission = s;
            }


            @Override
            public int getLayout() {
                return R.layout.classroom_file_item;
            }

            @Override
            public void bind(@NonNull GroupieViewHolder viewHolder, int position) {
                final View view = viewHolder.itemView;
                final AppCompatTextView nameText = view.findViewById(R.id.file_item_name);
                final AppCompatTextView dateText = view.findViewById(R.id.post_item_date);
                final AppCompatTextView commentText = view.findViewById(R.id.file_item_description);


                nameText.setText(getString(R.string.name));
                LiveData<String> stringLiveData = model.getNameById(submission.getSenderId());

                stringLiveData.observe(getViewLifecycleOwner(), s -> {
                    if (s == null) return;
                    nameText.setText(s);
                    stringLiveData.removeObservers(getViewLifecycleOwner());
                });

                dateText.setText(CommonUtils.getTimeAsString(submission.getFile().getDate()));
                if (submission.getComment() != null)
                    commentText.setText(submission.getComment());

                view.setOnClickListener(v -> {
                    if (getActivity() == null) return;
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(submission.getFile().getDownloadUrl()));
                    getActivity().startActivity(browserIntent);

                    CommonUtils.showMessage(view.getContext(), R.string.file_download);
                });

            }
        }

    }


}
