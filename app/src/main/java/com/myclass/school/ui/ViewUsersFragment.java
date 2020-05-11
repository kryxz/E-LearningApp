package com.myclass.school.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.myclass.school.CommonUtils;
import com.myclass.school.R;
import com.myclass.school.data.Classroom;
import com.myclass.school.data.Student;
import com.myclass.school.data.Teacher;
import com.myclass.school.items.AdminClassroomItem;
import com.myclass.school.items.AdminStudentItem;
import com.myclass.school.items.AdminTeacherItem;
import com.myclass.school.viewmodels.AdminViewModel;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.GroupieViewHolder;


/*
A simple screen to view users in a list (A recycler view)!
The app admin can see both students and teachers here.
They also can edit a user's name, or their subject/grade.
 */

public class ViewUsersFragment extends Fragment {

    public ViewUsersFragment() {
        // Required empty public constructor
    }

    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_view_users, container, false);
    }


    // view is created and its hierarchy is known.
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        init();
    }


    // initializes and gets data from database
    private void init() {


        // a simple progressbar that is hidden when the device gets data from the database
        ProgressBar progressBar = view.findViewById(R.id.view_users_progress_bar);
        progressBar.setVisibility(View.VISIBLE);


        // Users cannot enter this fragment without passing arguments.
        // see the function viewUsersNow in AdminFragment.java
        if (getArguments() == null)
            return;


        // a view model to manage ui/database interactions
        AdminViewModel model =
                new ViewModelProvider(this).get(AdminViewModel.class);


        // a text that says the user count
        AppCompatTextView countTextView = view.findViewById(R.id.users_count);

        // the adapter manages the list items in a recycler view
        GroupAdapter<GroupieViewHolder> adapter = new GroupAdapter<>();


        RecyclerView rv = view.findViewById(R.id.admin_users_rv);

        /*
        a string of one char to decide to show students or teachers
        t -> teachers, s -> students
         */

        String viewWhat = ViewUsersFragmentArgs.fromBundle(getArguments()).getViewWhat();

        if (viewWhat.equals("t")) {


            // update title and the info text
            updateTitle(R.string.view_teachers);
            countTextView.setText(getString(R.string.empty_teachers));

            // get and observe teachers from database
            model.getTeachers().observe(getViewLifecycleOwner(), teachers -> {

                // clear all items
                adapter.clear();

                if (!teachers.isEmpty()) {
                    // show count
                    countTextView.setText(getString(R.string.teachers_count, teachers.size()));

                    // add items to the list
                    for (Teacher t : teachers)
                        adapter.add(new AdminTeacherItem(t, model, getViewLifecycleOwner()));

                } else // no data, set text to empty
                    countTextView.setText(getString(R.string.empty_teachers));

                // associate adapter with the list
                rv.setAdapter(adapter);

                // hide the progress bar
                progressBar.setVisibility(View.GONE);

            });


        } else if (viewWhat.equals("s")) {

            updateTitle(R.string.view_students);
            countTextView.setText(getString(R.string.empty_students));


            // get and observe students from database
            model.getStudents().observe(getViewLifecycleOwner(), students -> {
                adapter.clear();

                if (!students.isEmpty()) {
                    countTextView.setText(getString(R.string.students_count, students.size()));

                    for (Student s : students)
                        adapter.add(new AdminStudentItem(s, model, getViewLifecycleOwner()));

                } else
                    countTextView.setText(getString(R.string.empty_students));


                // associate adapter with the list
                rv.setAdapter(adapter);

                // hide the progress bar
                progressBar.setVisibility(View.GONE);

            });

        } else {
            updateTitle(R.string.view_classes);
            countTextView.setText(getString(R.string.empty_classes));

            model.getAllClassrooms().observe(getViewLifecycleOwner(), classrooms -> {
                adapter.clear();

                if (!classrooms.isEmpty()) {
                    countTextView.setText(getString(R.string.classes_count, classrooms.size()));

                    for (Classroom c : classrooms)
                        adapter.add(new AdminClassroomItem(c, model, getViewLifecycleOwner()));


                } else
                    countTextView.setText(getString(R.string.empty_classes));

                // associate adapter with the list
                rv.setAdapter(adapter);

                // hide the progress bar
                progressBar.setVisibility(View.GONE);

            });
        }

    }


    /*
    update title in the action bar
    uses a string id from strings.xml
     */

    private void updateTitle(int id) {
        CommonUtils.updateTitle(getActivity(), id);
    }


}


