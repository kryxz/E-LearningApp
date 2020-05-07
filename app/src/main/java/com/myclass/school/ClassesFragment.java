package com.myclass.school;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.myclass.school.data.Classroom;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.GroupieViewHolder;
import com.xwray.groupie.Item;

import java.util.ArrayList;


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
        return inflater.inflate(R.layout.fragment_classes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        init();

    }


    private void init() {
        if (getActivity() == null) return;

        model = ((MainActivity) getActivity()).userVM;

        getClasses();

    }

    private void getClasses() {
        final RecyclerView rv = view.findViewById(R.id.classes_rv);
        final GroupAdapter adapter = new GroupAdapter<>();
        final ProgressBar progressBar = view.findViewById(R.id.user_classes_progress_bar);
        final AppCompatTextView noClassesText = view.findViewById(R.id.no_classes_text);


        progressBar.setVisibility(View.VISIBLE);
        model.getMyClasses().observe(getViewLifecycleOwner(), classrooms -> {

            adapter.clear();

            if (classrooms == null || classrooms.isEmpty()) {
                noClassesText.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                return;
            }


            for (final Classroom c : classrooms) {
                // get members names
                final ArrayList<String> members = new ArrayList<>();

                for (String id : c.getMembers())
                    getThenAddName(id, members);

                adapter.add(new ClassroomItem(c, members));

            }
            noClassesText.setVisibility(View.GONE);
            rv.setAdapter(adapter);
            progressBar.setVisibility(View.GONE);
        });


    }


    private void getThenAddName(final String id, final ArrayList<String> members) {
        final LiveData<String> nameLiveData = model.getNameById(id);
        nameLiveData.observe(getViewLifecycleOwner(), s -> {
            if (s == null) return;
            members.add(s);
            nameLiveData.removeObservers(getViewLifecycleOwner());
        });

    }


    private static class ClassroomItem extends Item<GroupieViewHolder> {
        final private Classroom classroom;
        final private ArrayList<String> members;

        ClassroomItem(final Classroom c, final ArrayList<String> m) {
            classroom = c;
            members = m;
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

            view.setOnClickListener(v ->
                    Navigation.findNavController(view).navigate(ClassesFragmentDirections.goToClassroom(classroom.getId())));

            view.setOnLongClickListener(v -> {
                showMembersDialog(view.getContext());
                return false;
            });

        }

        private void showMembersDialog(Context context) {

            // initialize dialog and its content
            final AlertDialog dialog = new AlertDialog.Builder(context).create();

            final View layout =
                    View.inflate(context, R.layout.dialog_classroom_members, null);

            dialog.setView(layout);

            final RecyclerView rv = layout.findViewById(R.id.classroom_members_rv);
            final AppCompatTextView membersText = layout.findViewById(R.id.members_count_dialog);

            int count = members.size();
            if (count == 1)
                membersText.setText(context.getString(R.string.one_member));
            else
                membersText.setText(context.getString(R.string.members_arg, count));

            final GroupAdapter adapter = new GroupAdapter<>();

            for (final String member : members)
                adapter.add(new ClassroomMemberItem(member));

            rv.setAdapter(adapter);
            dialog.show();
        }


        private static class ClassroomMemberItem extends Item<GroupieViewHolder> {

            final private String name;


            ClassroomMemberItem(String n) {
                name = n;
            }

            @Override
            public int getLayout() {
                return R.layout.member_item;
            }

            @Override
            public void bind(@NonNull GroupieViewHolder viewHolder, int position) {
                final View view = viewHolder.itemView;
                final AppCompatTextView firstLetter = view.findViewById(R.id.first_letter_name_item);

                final String firstLetterName = String.valueOf(name.charAt(0));
                firstLetter.setText(firstLetterName);

                firstLetter.getBackground().setTint(Common.getRandomColor(view.getContext(), position));


                final AppCompatTextView nameText = view.findViewById(R.id.member_item_name);
                nameText.setText(name);


            }
        }


    }


}
