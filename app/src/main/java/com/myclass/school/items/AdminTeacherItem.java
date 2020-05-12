package com.myclass.school.items;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.material.textfield.TextInputEditText;
import com.myclass.school.CommonUtils;
import com.myclass.school.R;
import com.myclass.school.data.Classroom;
import com.myclass.school.data.Student;
import com.myclass.school.data.Teacher;
import com.myclass.school.data.User;
import com.myclass.school.viewmodels.AdminViewModel;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.GroupieViewHolder;
import com.xwray.groupie.Item;

// A ViewHolder class that contains data about the item in the list
public class AdminTeacherItem extends Item<GroupieViewHolder> {

    // teacher object which contains the data needed for this item
    private final Teacher teacher;


    // a reference to the Admin view model to perform the edit and delete actions
    private final AdminViewModel model;
    private LifecycleOwner lifecycleOwner;

    // A simple Constructor
    public AdminTeacherItem(Teacher t, AdminViewModel m, LifecycleOwner owner) {
        teacher = t;
        model = m;
        lifecycleOwner = owner;
    }


    // bind is called to display data the specified position
    @Override
    public void bind(@NonNull GroupieViewHolder viewHolder, int position) {


        // reference to itemView and context
        View view = viewHolder.itemView;
        Context context = view.getContext();


        // set subject
        final AppCompatTextView subject = view.findViewById(R.id.item_subject_grade);

        subject.setText(context.getString(R.string.subject_details, teacher.getSubject()));


        // initializes and displays other data
        AdminStudentItem.initUserData(view, teacher);


        // get the edit and delete buttons from view
        final AppCompatButton editUser = view.findViewById(R.id.user_edit_item);
        final AppCompatButton deleteUser = view.findViewById(R.id.user_delete_item);

        final AppCompatButton manageClasses = view.findViewById(R.id.manage_classes_item);

        // shows the edit dialog
        editUser.setOnClickListener(v -> editUserDialog(context, teacher, model));


        manageClasses.setOnClickListener(v ->
                manageClasses(context)
        );


        // delete user on confirm
        deleteUser.setOnClickListener(v -> {

            // references the delete function
            final Runnable deleteAction = this::delete;

            // show the delete dialog with the specified title and message
            CommonUtils.showConfirmDialog(context, context.getString(R.string.delete_user),
                    context.getString(R.string.delete_user_confirm, teacher.getName()), deleteAction);


        });


    }


    private void manageClasses(Context context) {
        // initialize dialog and its content
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        final View layout = View.inflate(context, R.layout.dialog_manage_classes, null);
        dialog.setView(layout);

        final RecyclerView allClassesRV = layout.findViewById(R.id.all_classes_rv);
        final RecyclerView userClassesRV = layout.findViewById(R.id.item_classes_rv);

        allClassesRV.setLayoutManager(new StaggeredGridLayoutManager(2,
                StaggeredGridLayoutManager.HORIZONTAL));

        userClassesRV.setLayoutManager(new StaggeredGridLayoutManager(2,
                StaggeredGridLayoutManager.HORIZONTAL));


        final GroupAdapter allClassesAdapter = new GroupAdapter();


        final GroupAdapter userClassesAdapter = new GroupAdapter();


        model.getAllClassrooms().observe(lifecycleOwner, classrooms -> {
            if (classrooms == null) return;

            userClassesAdapter.clear();
            allClassesAdapter.clear();

            if (!classrooms.isEmpty()) {
                for (Classroom item : classrooms) {
                    if (teacher.getClasses().contains(item.getId()))
                        userClassesAdapter.add(new AdminAddClassroomItem(teacher, item, model));
                    else if (item.getInstructor() == null)
                        allClassesAdapter.add(new AdminAddClassroomItem(teacher, item, model));

                }
                userClassesRV.setAdapter(userClassesAdapter);
                allClassesRV.setAdapter(allClassesAdapter);


            }

        });

        // confirm and cancel buttons
        final AppCompatButton confirm = layout.findViewById(R.id.dialog_confirm_button);


        confirm.setOnClickListener(v -> dialog.dismiss());


        dialog.show();
    }

    // delete teacher from auth and database
    private void delete() {
        model.deleteUser(teacher);

    }


    // layout for this view holder
    @Override
    public int getLayout() {
        return R.layout.user_item_admin;
    }


    // dialog to edit some user data
    static void editUserDialog(Context context, User user, AdminViewModel model) {

        // initialize dialog and its content
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        final View layout = View.inflate(context, R.layout.add_user_dialog, null);
        dialog.setView(layout);


        // set title
        final AppCompatTextView titleTextView = layout.findViewById(R.id.admin_dialog_title);
        titleTextView.setText(context.getString(R.string.edit));


        // get username and subject editText
        final TextInputEditText userNameEd = layout.findViewById(R.id.add_user_name);
        final TextInputEditText userSubjectEd = layout.findViewById(R.id.add_subject_name);

        // set name
        userNameEd.setText(user.getName());

        // store the subject or grade
        String subjectOrGradeText = "";

        if (user instanceof Teacher) {
            // get subject from student user
            subjectOrGradeText = ((Teacher) user).getSubject();

            // set hints in EditTexts
            userNameEd.setHint(context.getString(R.string.teacher_name));
            userSubjectEd.setHint(context.getString(R.string.subject_name));

        } else if (user instanceof Student) {
            // get grade from student user
            subjectOrGradeText = ((Student) user).getGrade();

            // set hints in EditTexts
            userNameEd.setHint(context.getString(R.string.student_name));
            userSubjectEd.setHint(context.getString(R.string.grade));

        }
        // set subject or grade (depends on user type)
        userSubjectEd.setText(subjectOrGradeText);


        // confirm and cancel buttons
        final AppCompatButton confirm = layout.findViewById(R.id.dialog_confirm_button);
        final AppCompatButton cancel = layout.findViewById(R.id.dialog_cancel_button);


        // hide dialog on cancel
        cancel.setOnClickListener(v -> dialog.dismiss());


        confirm.setOnClickListener(v -> {


            // check input, must be longer than two chars
            if (userNameEd.getText() == null || userNameEd.getText().length() < 2)
                return;

            if (userSubjectEd.getText() == null || userSubjectEd.getText().length() < 2)
                return;

            // get new name and subject from input
            final String newName = userNameEd.getText().toString();
            final String newSubjectOrGrade = userSubjectEd.getText().toString();

            // update user
            if (user instanceof Teacher) {
                // cast user to Teacher
                final Teacher teacher = (Teacher) user;

                // update fields
                teacher.setSubject(newSubjectOrGrade);
                teacher.setName(newName);


                // update user in database
                model.updateUser(teacher);

            } else if (user instanceof Student) {

                // cast user to Student
                final Student student = (Student) user;

                // update fields
                student.setGrade(newSubjectOrGrade);
                student.setName(newName);

                // update user in database
                model.updateUser(student);

            }


            // hide dialog
            dialog.dismiss();
        });

        dialog.show();
    }


}