package com.myclass.school.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.myclass.school.R;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.GroupieViewHolder;
import com.xwray.groupie.Item;


/*
    A simple screen that shows several questions and their answers. Also known as FAQ.

 */
public class HelpFragment extends Fragment {

    public HelpFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_help, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
    }

    private void init() {
        if (getView() == null) return;

        final RecyclerView rv = getView().findViewById(R.id.help_rv);
        final GroupAdapter adapter = new GroupAdapter();

        // get questions and answers arrays
        final String[] questions = getResources().getStringArray(R.array.questions);
        final String[] answers = getResources().getStringArray(R.array.answers);

        final int questionsCount = questions.length;
        final int answersCount = answers.length;


        // add items to adapter
        for (int i = 0; i < questionsCount && i < answersCount; i++)
            adapter.add(new HelpItem(questions[i], answers[i]));

        rv.setAdapter(adapter);
    }


    // A view holder that holds a question, tap it to show the answer, another tap to hide.
    private static class HelpItem extends Item<GroupieViewHolder> {
        private final String question;
        private final String answer;

        HelpItem(String q, String a) {
            question = q;
            answer = a;
        }

        @Override
        public int getLayout() {
            return R.layout.help_item;
        }

        @Override
        public void bind(@NonNull GroupieViewHolder viewHolder, int position) {
            final View itemView = viewHolder.itemView;

            final AppCompatTextView questionText = itemView.findViewById(R.id.question_item);
            final AppCompatTextView answerText = itemView.findViewById(R.id.answer_item);

            // set texts
            questionText.setText(question);
            answerText.setText(answer);

            // hide show answer text
            questionText.setOnClickListener(v -> {
                if (answerText.getVisibility() == View.VISIBLE)
                    answerText.setVisibility(View.GONE);
                else answerText.setVisibility(View.VISIBLE);
            });
            answerText.setOnClickListener(v -> v.setVisibility(View.GONE));

        }

    }
}
