package com.myclass.school.items;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.lifecycle.LifecycleOwner;

import com.myclass.school.AdminViewModel;
import com.myclass.school.Common;
import com.myclass.school.R;
import com.myclass.school.data.Classroom;
import com.xwray.groupie.GroupieViewHolder;
import com.xwray.groupie.Item;

public class AdminClassroomItem extends Item<GroupieViewHolder> {

    private Classroom classroom;
    private AdminViewModel model;
    private LifecycleOwner owner;

    public AdminClassroomItem(Classroom classroom, AdminViewModel m, LifecycleOwner o) {
        this.classroom = classroom;
        model = m;
        owner = o;
    }

    @Override
    public void bind(@NonNull GroupieViewHolder viewHolder, int position) {
        View view = viewHolder.itemView;
        Context context = view.getContext();


        AppCompatTextView name = view.findViewById(R.id.classroom_item_name);

        AppCompatTextView description = view.findViewById(R.id.classroom_item_description);
        AppCompatTextView membersCount = view.findViewById(R.id.classroom_item_size);
        AppCompatTextView instructor = view.findViewById(R.id.classroom_item_instructor);


        if (classroom.getInstructor() != null)
            model.getNameById(classroom.getInstructor()).observe(owner, instructorName -> {
                if (instructorName != null)
                    instructor.setText(context.getString(R.string.instructor, instructorName));
                else
                    instructor.setText(context.getString(R.string.no_instructor));

            });
        else
            instructor.setText(context.getString(R.string.no_instructor));

        // set data from object to text
        name.setText(context.getString(R.string.classroom_name_arg, classroom.getName()));
        description.setText(context.getString(R.string.description_arg, classroom.getDescription()));


        membersCount.setText(context.getString(R.string.members_count, classroom.getMembers().size()));


        // hide and show details on click
        name.setOnClickListener(v -> {
            final View details = view.findViewById(R.id.classroom_item_details);
            if (details.getVisibility() == View.GONE)
                details.setVisibility(View.VISIBLE);

            else details.setVisibility(View.GONE);
        });


        AppCompatButton delete = view.findViewById(R.id.classroom_delete_item);
        delete.setOnClickListener(v -> {
            // references the delete function
            final Runnable deleteAction = this::delete;

            // show delete dialog
            Common.showConfirmDialog(context, context.getString(R.string.delete_classroom),
                    context.getString(R.string.delete_user_confirm, classroom.getName()), deleteAction);


        });


    }

    private void delete() {
        model.deleteClassroom(classroom);

    }

    @Override
    public int getLayout() {
        return R.layout.classroom_admin_item;
    }


}
