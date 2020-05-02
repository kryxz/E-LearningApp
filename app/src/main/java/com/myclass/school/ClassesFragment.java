package com.myclass.school;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.myclass.school.data.Classroom;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.GroupieViewHolder;
import com.xwray.groupie.Item;


public class ClassesFragment extends Fragment {

    public ClassesFragment() {
        // Required empty public constructor
    }


    private View view;
    private UserViewModel model;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_classes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Common.setDrawerIcon(getActivity());
        this.view = view;
        init();

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // open drawer on click
        if (item.getItemId() == android.R.id.home)
            Common.openDrawer(getActivity());
        return super.onOptionsItemSelected(item);
    }

    private void init() {
        if (getActivity() == null) return;

        model = ((MainActivity) getActivity()).model;

        getClasses();

    }

    private void getClasses() {
        final RecyclerView rv = view.findViewById(R.id.classes_rv);
        final GroupAdapter adapter = new GroupAdapter<>();
        ProgressBar progressBar = view.findViewById(R.id.user_classes_progress_bar);

        progressBar.setVisibility(View.VISIBLE);
        model.getMyClasses().observe(getViewLifecycleOwner(), classrooms -> {

            adapter.clear();
            if (classrooms == null || classrooms.isEmpty()) return;

            for (final Classroom c : classrooms)
                adapter.add(new ClassroomItem(c));

            rv.setAdapter(adapter);
            progressBar.setVisibility(View.GONE);
        });


    }


    private static class ClassroomItem extends Item<GroupieViewHolder> {
        final private Classroom classroom;

        ClassroomItem(final Classroom c) {
            classroom = c;
        }

        @Override
        public int getLayout() {
            return R.layout.classroom_item;
        }

        @Override
        public void bind(@NonNull GroupieViewHolder viewHolder, int position) {
            final View view = viewHolder.itemView;

            final AppCompatTextView nameText = view.findViewById(R.id.classroom_item_name);
            final AppCompatTextView descriptionText = view.findViewById(R.id.classroom_item_description);

            final AppCompatTextView firstLetter = view.findViewById(R.id.first_letter_name_item);


            // set random background color
            firstLetter.getBackground().setTint(Common.getRandomColor(view.getContext(), position));

            // set first letter
            final String firstLetterName = String.valueOf(classroom.getName().charAt(0));
            firstLetter.setText(firstLetterName);

            // set name and description
            nameText.setText(classroom.getName());
            descriptionText.setText(classroom.getDescription());


        }

    }


}
