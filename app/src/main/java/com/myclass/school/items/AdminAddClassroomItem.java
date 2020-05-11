package com.myclass.school.items;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;

import com.myclass.school.R;
import com.myclass.school.data.Classroom;
import com.myclass.school.data.Teacher;
import com.myclass.school.data.User;
import com.myclass.school.viewmodels.AdminViewModel;
import com.xwray.groupie.GroupieViewHolder;
import com.xwray.groupie.Item;

// A ViewHolder class that contains data about a classroom in the list
public class AdminAddClassroomItem extends Item<GroupieViewHolder> {

    private Classroom classroom;
    private User user;
    private AdminViewModel model;

    AdminAddClassroomItem(User user, Classroom classroom, AdminViewModel m) {
        this.user = user;
        this.classroom = classroom;
        model = m;
    }

    @Override
    public void bind(@NonNull GroupieViewHolder viewHolder, int position) {
        View view = viewHolder.itemView;

        AppCompatTextView name = view.findViewById(R.id.item_classroom_name);
        name.setText(classroom.getName());
        view.setOnClickListener(v -> {
            if (classroom.contains(user.getEmail())) {

                classroom.removeMember(user.getEmail());
                user.removeFromClass(classroom.getId());
                classroom.setInstructor(null);

            } else {
                if (user instanceof Teacher)
                    classroom.setInstructor(((Teacher) user).getId());
                classroom.addMember(user.getEmail());
                user.addToClass(classroom.getId());
            }


            model.updateUser(user);
            model.updateClassroom(classroom);
        });


    }

    @Override
    public int getLayout() {
        return R.layout.add_classroom_item;
    }


}
