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


import com.myclass.school.AdminViewModel;
import com.myclass.school.data.Classroom;
import com.myclass.school.Common;
import com.myclass.school.R;
import com.myclass.school.data.Student;
import com.myclass.school.data.User;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.GroupieViewHolder;
import com.xwray.groupie.Item;

// A ViewHolder class that contains data about the item in the list
public class AdminStudentItem extends Item<GroupieViewHolder> {

    // student object which contains the data needed for this item
    final private Student student;

    // a reference to the Admin view model to perform the edit and delete actions
    private final AdminViewModel model;

    private LifecycleOwner lifecycleOwner;

    public AdminStudentItem(Student t, AdminViewModel m, LifecycleOwner o) {
        student = t;
        model = m;
        lifecycleOwner = o;
    }


    static void initUserData(View view, User user) {
        Context context = view.getContext();


        // get textViews from the itemView
        final AppCompatTextView email = view.findViewById(R.id.item_user_email);
        final AppCompatTextView password = view.findViewById(R.id.item_user_password);
        final AppCompatTextView name = view.findViewById(R.id.item_username);

        final String userEmail = user.getEmail();

        // set text
        email.setText(context.getString(R.string.email_details, userEmail));


        password.setText(context.getString(R.string.password_details, user.getPassword()));
        name.setText(context.getString(R.string.name_details, user.getName()));


        email.setOnClickListener(v -> Common.copyText(context, userEmail));

        password.setOnClickListener(v -> Common.copyText(context, userEmail));

        // hide and show view on click
        name.setOnClickListener(v -> {
            // view layout reference
            final View details = view.findViewById(R.id.item_details_admin);

            if (details.getVisibility() == View.GONE)
                details.setVisibility(View.VISIBLE);

            else details.setVisibility(View.GONE);

        });


        // the copy button
        final AppCompatButton copyDetails = view.findViewById(R.id.copy_login_details);


        copyDetails.setOnClickListener(v -> {

            // get login info
            String loginDetails = context.getString(R.string.login_details,
                    user.getEmail(), user.getPassword());

            // copy text data
            Common.copyText(context, loginDetails);

        });


    }


    // bind is called to display data the specified position
    @Override
    public void bind(@NonNull GroupieViewHolder viewHolder, int position) {


        View view = viewHolder.itemView;
        Context context = view.getContext();


        // set student grade
        final AppCompatTextView grade = view.findViewById(R.id.item_subject_grade);
        grade.setText(context.getString(R.string.grade_details, student.getGrade()));


        initUserData(view, student);


        // edit and delete buttons
        final AppCompatButton editUser = view.findViewById(R.id.user_edit_item);
        final AppCompatButton deleteUser = view.findViewById(R.id.user_delete_item);
        final AppCompatButton manageClasses = view.findViewById(R.id.manage_classes_item);

        // show edit dialog on click
        editUser.setOnClickListener(v -> AdminTeacherItem.editUserDialog(context, student, model));

        manageClasses.setOnClickListener(v ->

                manageClasses(context)

        );

        // delete user on confirm
        deleteUser.setOnClickListener(v -> {

            // references the delete function
            final Runnable deleteAction = this::delete;

            // show delete dialog
            Common.showConfirmDialog(context, context.getString(R.string.delete_user),
                    context.getString(R.string.delete_user_confirm, student.getName()), deleteAction);

        });


    }


    private void manageClasses(Context context) {
        // initialize dialog and its content
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        final View layout = View.inflate(context, R.layout.dialog_manage_classes, null);
        dialog.setView(layout);

        final RecyclerView allClassesRV = layout.findViewById(R.id.all_classes_rv);
        final RecyclerView userClassesRV = layout.findViewById(R.id.item_classes_rv);

        allClassesRV.setLayoutManager(new StaggeredGridLayoutManager(1,
                StaggeredGridLayoutManager.HORIZONTAL));

        userClassesRV.setLayoutManager(new StaggeredGridLayoutManager(1,
                StaggeredGridLayoutManager.HORIZONTAL));


        GroupAdapter<GroupieViewHolder> allClassesAdapter = new GroupAdapter<>();


        GroupAdapter<GroupieViewHolder> userClassesAdapter = new GroupAdapter<>();


        model.getAllClassrooms().observe(lifecycleOwner, classrooms -> {
            if (classrooms == null) return;

            userClassesAdapter.clear();
            allClassesAdapter.clear();

            if (!classrooms.isEmpty()) {
                for (Classroom item : classrooms) {
                    if (student.getClasses().contains(item.getId()))
                        userClassesAdapter.add(new AdminAddClassroomItem(student, item, model));
                    else
                        allClassesAdapter.add(new AdminAddClassroomItem(student, item, model));

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


    // delete student from auth and database
    private void delete() {
        model.deleteUser(student);

    }


    // layout for this view holder
    @Override
    public int getLayout() {
        return R.layout.user_item_admin;
    }


}
