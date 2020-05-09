package com.myclass.school;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.myclass.school.data.Assignment;
import com.myclass.school.data.ClassroomFile;
import com.myclass.school.data.Submission;
import com.myclass.school.data.Teacher;
import com.myclass.school.data.User;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.GroupieViewHolder;
import com.xwray.groupie.Item;

import java.util.List;
import java.util.concurrent.TimeUnit;


public class CreateAssignmentsFragment extends Fragment {

    private View view;
    private ClassroomVM classroomVM;
    private User user;
    private String classroomId;
    private UserViewModel model;

    public CreateAssignmentsFragment() {
        // Required empty public constructor
    }

    static void setUpAssignmentItem(View view, Assignment assignment) {

        final Context context = view.getContext();
        final AppCompatTextView title = view.findViewById(R.id.assignment_title_item);
        final AppCompatTextView content = view.findViewById(R.id.assignment_content_item);
        final AppCompatTextView date = view.findViewById(R.id.assignment_date_item);
        final AppCompatTextView dueDate = view.findViewById(R.id.assignment_due_date_item);
        final AppCompatTextView headerText = view.findViewById(R.id.item_position_text);

        final AppCompatTextView fileName = view.findViewById(R.id.assignment_file_title);


        title.setText(context.getString(R.string.title_arg_with_class,
                assignment.getClassroomName(), assignment.getTitle()));

        content.setText(context.getString(R.string.content_arg, assignment.getContent()));

        fileName.setText(context.getString(R.string.no_file_assignment));

        final View detailsLayout = view.findViewById(R.id.details_layout);

        headerText.setOnClickListener(v -> {
            if (detailsLayout.getVisibility() == View.VISIBLE) {
                detailsLayout.setVisibility(View.GONE);
                headerText.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_expand_more, 0);
            } else {
                detailsLayout.setVisibility(View.VISIBLE);
                headerText.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_expand_less, 0);

            }
        });
        if (assignment.getFile() != null)
            fileName.setText(assignment.getFile().getName());

        date.setText(context.getString(R.string.open_date_arg,
                Common.getTimeAsString(assignment.getDate())));

        dueDate.setText(context.getString(R.string.due_date_arg,
                Common.getTimeAsString(assignment.getDueDate()),
                Common.getDateFormatted(assignment.getDueDate())));


        // due date text
        long timeNow = System.currentTimeMillis();
        if (assignment.getDueDate() < timeNow) {
            long elapsedTime = timeNow - assignment.getDueDate();
            long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTime);

            long hours = TimeUnit.MILLISECONDS.toHours(elapsedTime);
            long days = TimeUnit.MILLISECONDS.toDays(elapsedTime);

            dueDate.setText(context.getString(R.string.assignment_due_arg_min, minutes));

            if (minutes == 1)
                dueDate.setText(dueDate.getText().subSequence(0, dueDate.getText().length() - 1));

            else if (minutes > 60) {
                dueDate.setText(context.getString(R.string.assignment_due_arg_hour, hours));

                if (hours == 1)
                    dueDate.setText(dueDate.getText().subSequence(0, dueDate.getText().length() - 1));

                else if (hours > 24)
                    dueDate.setText(context.getString(R.string.assignment_due_arg_day, days));

                if (hours > 24 && days == 1)
                    dueDate.setText(dueDate.getText().subSequence(0, dueDate.getText().length() - 1));
            }

            dueDate.setTextColor(ContextCompat.getColor(context, R.color.red));
            dueDate.setTypeface(dueDate.getTypeface(), Typeface.BOLD);

        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_class_assignments, container, false);
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
        classroomVM = ((MainActivity) getActivity()).classroomVM;

        classroomId = CreateAssignmentsFragmentArgs.fromBundle(getArguments()).getId();

        final View createButton = view.findViewById(R.id.new_assignment_button);

        LiveData<User> userLiveData = model.getUser();
        userLiveData.observe(getViewLifecycleOwner(), u -> {
            if (u == null) return;
            user = u;

            if (user instanceof Teacher) {
                createButton.setVisibility(View.VISIBLE);
                createButton.setOnClickListener(v -> createAssignment(classroomId));

            } else
                createButton.setVisibility(View.GONE);


            userLiveData.removeObservers(this);
        });

        final ProgressBar progressBar = view.findViewById(R.id.assignments_progress_bar);
        final RecyclerView rv = view.findViewById(R.id.assignments_rv);
        final GroupAdapter adapter = new GroupAdapter();

        classroomVM.getAssignments(classroomId).observe(getViewLifecycleOwner(), assignments -> {
            if (assignments == null || assignments.isEmpty()) return;
            progressBar.setVisibility(View.GONE);

            adapter.clear();
            for (int i = 0; i < assignments.size(); i++)
                adapter.add(new AssignmentItem(assignments.get(i), i + 1));

            rv.setAdapter(adapter);

        });

    }

    private TextInputEditText getInputText(int id) {
        return view.findViewById(id);
    }

    // hides or shows a specified view
    private void viewVisibility(int id, int visibility) {
        view.findViewById(id).setVisibility(visibility);

    }

    private void createAssignment(String classroomId) {
        if (getActivity() == null) return;
        viewVisibility(R.id.create_assignment_layout, View.VISIBLE);
        viewVisibility(R.id.view_assignments_layout, View.GONE);

        final TextInputEditText titleText = getInputText(R.id.assignment_title);
        final TextInputEditText contentText = getInputText(R.id.assignment_content);


        final TextInputEditText dateText = getInputText(R.id.assignment_date);
        final TextInputEditText dueDateText = getInputText(R.id.assignment_due_date);
        final TextInputEditText durationText = getInputText(R.id.assignment_time);

        final TextInputEditText fileText = getInputText(R.id.assignment_file);

        final AppCompatButton confirm = view.findViewById(R.id.new_assignment_confirm);
        final AppCompatButton cancel = view.findViewById(R.id.new_assignment_cancel);

        final Assignment assignment = new Assignment();


        Common.showDatePickerDialog(dateText, getActivity().getSupportFragmentManager());
        Common.showDatePickerDialog(dueDateText, getActivity().getSupportFragmentManager());


        fileText.setFocusableInTouchMode(false);


        fileText.setOnClickListener(v -> chooseFileDialog(fileText));

        confirm.setOnClickListener(v -> {
            if (titleText.getText() == null || contentText.getText() == null) return;

            if (dateText.getText() == null || dueDateText.getText() == null) return;


            final String title = contentText.getText().toString().trim();
            final String content = titleText.getText().toString().trim();

            final long date = Common.Temp.assignmentDate;
            long dueDate = Common.Temp.assignmentDueDate;


            boolean isSameDay = (dueDate - date) < 450000;
            // 450000 is about 7 minutes! the shortest deadline cannot be less than that

            if (isSameDay && durationText.getVisibility() == View.GONE) {
                durationText.setVisibility(View.VISIBLE);
                durationText.setError(getString(R.string.same_day_choose_duration));
                return;
            }

            if (isSameDay && durationText.getText() == null) {
                durationText.setError(getString(R.string.same_day_choose_duration));
                return;
            }

            if (isSameDay) {
                final String durationString = durationText.getText().toString().trim();
                double durationInMS = Double.parseDouble(durationString) * 3600000; // 3600000ms is one hour
                dueDate += ((long) durationInMS);
            }


            if (title.isEmpty() || content.isEmpty()) return;


            assignment.setTitle(title);
            assignment.setContent(content);
            assignment.setDueDate(dueDate);
            assignment.setDate(date);
            assignment.setFile(Common.Temp.tempFile);
            classroomVM.addAssignment(classroomId, assignment);

            Common.showMessage(view.getContext(), R.string.assignment_sent);

            // clear text
            titleText.getText().clear();
            contentText.getText().clear();

            dateText.getText().clear();
            dueDateText.getText().clear();

            if (fileText.getText() != null)
                fileText.getText().clear();

            viewVisibility(R.id.create_assignment_layout, View.GONE);
            viewVisibility(R.id.view_assignments_layout, View.VISIBLE);
        });

        cancel.setOnClickListener(v -> {
            viewVisibility(R.id.create_assignment_layout, View.GONE);
            viewVisibility(R.id.view_assignments_layout, View.VISIBLE);
        });

    }

    private void chooseFileDialog(TextInputEditText ed) {

        final AlertDialog dialog = new AlertDialog.Builder(view.getContext()).create();

        final View layout =
                View.inflate(view.getContext(), R.layout.dialog_classroom_list, null);

        dialog.setView(layout);

        final RecyclerView rv = layout.findViewById(R.id.classroom_members_rv);
        final AppCompatTextView countText = layout.findViewById(R.id.members_count_dialog);
        countText.setText(getString(R.string.choose_file));

        final GroupAdapter adapter = new GroupAdapter<>();

        LiveData<List<ClassroomFile>> listLiveData = model.getClassFiles(classroomId);
        listLiveData.observe(getViewLifecycleOwner(), classroomFiles -> {
            if (classroomFiles == null || classroomFiles.isEmpty()) {
                return;
            }

            adapter.clear();

            final Runnable callBack = () -> {
                ed.setText(Common.Temp.tempFile.getName());
                dialog.dismiss();
            };

            for (ClassroomFile file : classroomFiles)
                adapter.add(new FilesFragment.FileItem(file, getActivity(), true, callBack));

            rv.setAdapter(adapter);
            listLiveData.removeObservers(getViewLifecycleOwner());
        });

        dialog.show();

    }

    private class AssignmentItem extends Item<GroupieViewHolder> {

        private final Assignment assignment;
        private final int pos;

        AssignmentItem(Assignment a, int p) {
            assignment = a;
            pos = p;
        }


        @Override
        public int getLayout() {

            return R.layout.assignment_item_teacher;
        }


        @Override
        public void bind(@NonNull GroupieViewHolder viewHolder, int position) {
            final View view = viewHolder.itemView;
            final Context context = view.getContext();


            final AppCompatTextView submissions = view.findViewById(R.id.assignment_submissions_count);
            final AppCompatTextView posText = view.findViewById(R.id.item_position_text);

            setUpAssignmentItem(view, assignment);
            posText.setText(context.getString(R.string.number_arg, pos));

            submissions.setText(view.getContext().getString(R.string.submissions_count,
                    assignment.getSubmissions().size()));

            final AppCompatButton delete = view.findViewById(R.id.assignment_delete_item);
            final AppCompatButton submissionsButton = view.findViewById(R.id.view_submissions_btn);


            submissionsButton.setOnClickListener(v -> showSubmissions(context));

            delete.setOnClickListener(v -> {
                Runnable deleteAction = () -> classroomVM.deleteAssignment(classroomId, assignment.getId());

                Common.showConfirmDialog(context,
                        getString(R.string.delete_assignment),
                        getString(R.string.delete_assignment_confirm), deleteAction);

            });
        }

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

                dateText.setText(Common.getTimeAsString(submission.getFile().getDate()));
                if (submission.getComment() != null)
                    commentText.setText(submission.getComment());

                view.setOnClickListener(v -> {
                    if (getActivity() == null) return;
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(submission.getFile().getDownloadUrl()));
                    getActivity().startActivity(browserIntent);

                    Common.showMessage(view.getContext(), R.string.file_download);
                });

            }
        }

    }

}
