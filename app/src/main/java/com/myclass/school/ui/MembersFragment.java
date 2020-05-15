package com.myclass.school.ui;

import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.myclass.school.CommonUtils;
import com.myclass.school.MainActivity;
import com.myclass.school.R;
import com.myclass.school.viewmodels.ClassroomVM;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.GroupieViewHolder;
import com.xwray.groupie.Item;


public class MembersFragment extends Fragment {

    public MembersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_members, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
    }

    private void init() {
        if (getActivity() == null) return;
        // cannot enter without arguments
        if (getArguments() == null) return;
        if (getView() == null) return;

        final String id = MembersFragmentArgs.fromBundle(getArguments()).getId();
        ClassroomVM classroomVM = ((MainActivity) getActivity()).classroomVM;

        final RecyclerView rv = getView().findViewById(R.id.members_rv);
        final ProgressBar progressBar = getView().findViewById(R.id.members_progress_bar);
        final GroupAdapter adapter = new GroupAdapter();


        progressBar.setVisibility(View.VISIBLE);
        classroomVM.getMembers(id).observe(getViewLifecycleOwner(), nameIdPairs -> {
            if (nameIdPairs == null) return;
            if (nameIdPairs.isEmpty()) {
                progressBar.setVisibility(View.VISIBLE);
                return;
            }
            adapter.clear();
            progressBar.setVisibility(View.GONE);

            for (Pair<String, String> nameIdPair : nameIdPairs)
                adapter.add(new ClassroomMemberItem(nameIdPair.first, nameIdPair.second));


            rv.setAdapter(adapter);

        });

    }

    private void goToProfile(String id) {
        if (getView() == null) return;
        Navigation.findNavController(getView()).navigate(
                MembersFragmentDirections.goToThisProfile().setUserId(id)
        );

    }

    private class ClassroomMemberItem extends Item<GroupieViewHolder> {


        // user name
        final private String name;
        final private String id;


        // constructor
        ClassroomMemberItem(String n, String userId) {
            name = n;
            id = userId;
        }

        // item layout
        @Override
        public int getLayout() {
            return R.layout.member_item;
        }

        @Override
        public void bind(@NonNull GroupieViewHolder viewHolder, int position) {
            final View itemView = viewHolder.itemView;


            // set first letter and user name
            final AppCompatTextView firstLetter = itemView.findViewById(R.id.first_letter_name_item);

            final String firstLetterName = String.valueOf(name.charAt(0));
            firstLetter.setText(firstLetterName);

            firstLetter.getBackground().setTint(CommonUtils.getRandomColor(itemView.getContext(), position));

            final AppCompatTextView nameText = itemView.findViewById(R.id.member_item_name);
            nameText.setText(name);


            itemView.setOnClickListener(v -> goToProfile(id));
        }
    }


}
