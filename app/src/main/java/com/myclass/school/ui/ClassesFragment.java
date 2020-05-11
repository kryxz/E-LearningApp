package com.myclass.school.ui;

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

import com.myclass.school.CommonUtils;
import com.myclass.school.MainActivity;
import com.myclass.school.R;
import com.myclass.school.data.Classroom;
import com.myclass.school.viewmodels.UserViewModel;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.GroupieViewHolder;
import com.xwray.groupie.Item;

import java.util.ArrayList;

/*
    a screen that shows the user their classes
    users can enter their classes from here
 */
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

        // initialize model
        model = ((MainActivity) getActivity()).userVM;

        getClasses();

    }

    private void getClasses() {

        // get recyclerview from view
        final RecyclerView rv = view.findViewById(R.id.classes_rv);
        // adapter object
        final GroupAdapter adapter = new GroupAdapter<>();

        // progress bar and hint text
        final ProgressBar progressBar = view.findViewById(R.id.user_classes_progress_bar);

        // this text is shown when the user doesn't have any classes!
        final AppCompatTextView noClassesText = view.findViewById(R.id.no_classes_text);

        // show progress bar!
        progressBar.setVisibility(View.VISIBLE);
        // get user classes!
        model.getMyClasses().observe(getViewLifecycleOwner(), classrooms -> {

            adapter.clear();

            // show hint if no classes found!
            if (classrooms == null || classrooms.isEmpty()) {
                noClassesText.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                return;
            }


            // add classes to adapter using a view holder object
            for (final Classroom c : classrooms) {
                // get members names
                final ArrayList<String> members = new ArrayList<>();

                for (String id : c.getMembers())
                    getThenAddName(id, members);

                adapter.add(new ClassroomItem(c, members));

            }
            // hide progress bar and hint text
            noClassesText.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);

            rv.setAdapter(adapter);
        });


    }


    private void getThenAddName(final String id, final ArrayList<String> members) {
        // get username by id, then adds it to members array!
        final LiveData<String> nameLiveData = model.getNameById(id);
        nameLiveData.observe(getViewLifecycleOwner(), s -> {
            if (s == null) return;
            members.add(s);

            nameLiveData.removeObservers(getViewLifecycleOwner());
        });

    }


    /*
    A view holder class to display data about classrooms
        Contains
        An icon of the first letter for a classroom
        Classroom name
        Sends user to the classroom when clicked on item
        Shows members of classroom when long clicked!
     */
    private static class ClassroomItem extends Item<GroupieViewHolder> {

        // class fields
        final private Classroom classroom;
        final private ArrayList<String> members;


        // constructor
        ClassroomItem(final Classroom c, final ArrayList<String> m) {
            classroom = c;
            members = m;
        }


        // view layout!
        @Override
        public int getLayout() {
            return R.layout.classroom_item;
        }


        @Override
        public void bind(@NonNull GroupieViewHolder viewHolder, int position) {
            final View view = viewHolder.itemView;

            // get text fields
            final AppCompatTextView nameText = view.findViewById(R.id.classroom_item_name);
            final AppCompatTextView descriptionText = view.findViewById(R.id.classroom_item_description);

            final AppCompatTextView firstLetter = view.findViewById(R.id.first_letter_name_item);


            // set random background color
            firstLetter.getBackground().setTint(CommonUtils.getRandomColor(view.getContext(), position));

            // set first letter
            final String firstLetterName = String.valueOf(classroom.getName().charAt(0));
            firstLetter.setText(firstLetterName);

            // set name and description
            nameText.setText(classroom.getName());
            descriptionText.setText(classroom.getDescription());

            // send user to classroom when click
            view.setOnClickListener(v ->
                    Navigation.findNavController(view).navigate(ClassesFragmentDirections.goToClassroom(classroom.getId())));

            // show to classroom members when long click

            view.setOnLongClickListener(v -> {
                showMembersDialog(view.getContext());
                return false; // call requires to return a bool!
            });

        }


        // a dialog that contains a recyclerView to show members!
        private void showMembersDialog(Context context) {

            // initialize dialog and its content
            final AlertDialog dialog = new AlertDialog.Builder(context).create();

            final View layout =
                    View.inflate(context, R.layout.dialog_classroom_list, null);

            dialog.setView(layout);


            // get recyclerView and text field
            final RecyclerView rv = layout.findViewById(R.id.classroom_members_rv);
            final AppCompatTextView membersText = layout.findViewById(R.id.members_count_dialog);

            // set members count text (3 members, 1 member...)
            int count = members.size();
            if (count == 1)
                membersText.setText(context.getString(R.string.one_member));
            else
                membersText.setText(context.getString(R.string.members_arg, count));


            // add member items to the list
            final GroupAdapter adapter = new GroupAdapter<>();

            for (final String member : members)
                adapter.add(new ClassroomMemberItem(member));

            rv.setAdapter(adapter);
            dialog.show();
        }


        // a view holder to show member name and their picture
        private static class ClassroomMemberItem extends Item<GroupieViewHolder> {


            // user name
            final private String name;


            // constructor
            ClassroomMemberItem(String n) {
                name = n;
            }

            // item layout
            @Override
            public int getLayout() {
                return R.layout.member_item;
            }

            @Override
            public void bind(@NonNull GroupieViewHolder viewHolder, int position) {
                final View view = viewHolder.itemView;


                // set first letter and user name
                final AppCompatTextView firstLetter = view.findViewById(R.id.first_letter_name_item);

                final String firstLetterName = String.valueOf(name.charAt(0));
                firstLetter.setText(firstLetterName);

                firstLetter.getBackground().setTint(CommonUtils.getRandomColor(view.getContext(), position));

                final AppCompatTextView nameText = view.findViewById(R.id.member_item_name);
                nameText.setText(name);


            }
        }


    }


}
