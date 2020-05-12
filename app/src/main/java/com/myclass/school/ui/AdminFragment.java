package com.myclass.school.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.textfield.TextInputEditText;
import com.myclass.school.CommonUtils;
import com.myclass.school.R;
import com.myclass.school.data.Classroom;
import com.myclass.school.data.Student;
import com.myclass.school.data.Teacher;
import com.myclass.school.data.User;
import com.myclass.school.viewmodels.AdminViewModel;

import java.util.Random;



/*
    A page that contains six buttons:
        add student/teacher
        view students/teachers
        create class/ view classes
 */

public class AdminFragment extends Fragment {

    public AdminFragment() {
        // Required empty public constructor
    }

    private View view;
    private AdminViewModel model;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_admin, container, false);
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        // logout button in the action bar
        inflater.inflate(R.menu.admin_logout_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // handle logout button click
        if (item.getItemId() == R.id.adminLogout)
            logout();
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        init();
    }


    private void init() {

        // hide fab button
        CommonUtils.fabVisibility(getActivity(), View.GONE);


        model = new ViewModelProvider(this).get(AdminViewModel.class);


        final AppCompatButton[] buttons = new AppCompatButton[]{
                view.findViewById(R.id.add_teacher_btn), // addTeacherBtn
                view.findViewById(R.id.add_student_btn), // addStudentBtn
                view.findViewById(R.id.view_teachers_btn), // viewTeachersBtn
                view.findViewById(R.id.view_students_btn), // viewStudentsBtn
                view.findViewById(R.id.add_classroom_btn), // addClassBtn
                view.findViewById(R.id.view_classrooms_btn) // viewClassesBtn
        };

        // add listener for the 4 buttons
        for (final AppCompatButton button : buttons) {
            button.setOnClickListener(v -> {
                switch (button.getId()) {

                    // add teacher
                    case R.id.add_teacher_btn: {
                        Teacher t = new Teacher();
                        addUserDialog(t);
                        break;
                    }

                    // add student
                    case R.id.add_student_btn: {
                        Student s = new Student();
                        addUserDialog(s);
                        break;
                    }

                    // view teachers
                    case R.id.view_teachers_btn: {
                        viewUsersNow("t");
                        break;
                    }
                    // view students
                    case R.id.view_students_btn: {
                        viewUsersNow("s");
                        break;

                    }

                    // create classroom
                    case R.id.add_classroom_btn: {
                        createClassroom();
                        break;

                    }

                    // view classes
                    case R.id.view_classrooms_btn: {
                        viewUsersNow("c");
                        break;

                    }

                }
            });
        }


    }


    private void createClassroom() {
        // initialize dialog and its content
        final AlertDialog dialog = new AlertDialog.Builder(view.getContext()).create();

        final View layout =
                View.inflate(view.getContext(), R.layout.add_user_dialog, null);

        dialog.setView(layout);

        // get views
        final AppCompatTextView dialogTitle = layout.findViewById(R.id.admin_dialog_title);
        final TextInputEditText classNameEd = layout.findViewById(R.id.add_user_name);
        final TextInputEditText descriptionEd = layout.findViewById(R.id.add_subject_name);


        // set texts
        dialogTitle.setText(getString(R.string.create_classroom));
        classNameEd.setHint(getString(R.string.classroom_name));
        descriptionEd.setHint(getString(R.string.description));


        final AppCompatButton confirm = layout.findViewById(R.id.dialog_confirm_button);
        final AppCompatButton cancel = layout.findViewById(R.id.dialog_cancel_button);

        cancel.setOnClickListener(v -> dialog.dismiss());

        confirm.setOnClickListener(v -> {
            // check inputs
            if (classNameEd.getText() == null || classNameEd.getText().length() == 0) {
                classNameEd.setError(getString(R.string.cannot_be_empty));
                return;
            }

            if (descriptionEd.getText() == null || descriptionEd.getText().length() == 0) {
                descriptionEd.setError(getString(R.string.cannot_be_empty));
                return;
            }

            // get input from EditTexts
            final String name = classNameEd.getText().toString();
            final String description = descriptionEd.getText().toString();
            final String id = generateId(name, 'c');

            // create classroom object
            Classroom classroom = new Classroom();
            classroom.setName(name);
            classroom.setDescription(description);
            classroom.setId(id);

            addClassroom(classroom);

            dialog.dismiss();

        });
        dialog.show();


    }


    // go to View Users page
    private void viewUsersNow(String argument) {
        Navigation.findNavController(view)
                .navigate(AdminFragmentDirections.viewUsersNow(argument));
    }


    private void addUserDialog(User user) {


        // initialize dialog and its content
        final AlertDialog dialog = new AlertDialog.Builder(view.getContext()).create();

        final View layout =
                View.inflate(view.getContext(), R.layout.add_user_dialog, null);

        dialog.setView(layout);


        // get views
        final AppCompatTextView dialogTitle = layout.findViewById(R.id.admin_dialog_title);

        final TextInputEditText userNameEd = layout.findViewById(R.id.add_user_name);
        final TextInputEditText subjectEd = layout.findViewById(R.id.add_subject_name);


        // add teacher
        if (user instanceof Teacher) {
            subjectEd.setHint(getString(R.string.subject_name));
            dialogTitle.setText(getString(R.string.add_teacher));

        } else {  // add student
            subjectEd.setHint(getString(R.string.grade));
            dialogTitle.setText(getString(R.string.add_student));

        }

        final AppCompatButton confirm = layout.findViewById(R.id.dialog_confirm_button);
        final AppCompatButton cancel = layout.findViewById(R.id.dialog_cancel_button);

        cancel.setOnClickListener(v -> dialog.dismiss());


        confirm.setOnClickListener(v -> {
            // check inputs
            if (userNameEd.getText() == null || userNameEd.getText().length() == 0) {
                userNameEd.setError(getString(R.string.cannot_be_empty));
                return;
            }

            if (subjectEd.getText() == null || subjectEd.getText().length() == 0) {
                subjectEd.setError(getString(R.string.cannot_be_empty));
                return;
            }


            // get input from EditTexts
            final String name = userNameEd.getText().toString();
            final String subjectOrGrade = subjectEd.getText().toString();

            if (user instanceof Teacher) {
                final Teacher teacher = (Teacher) user;
                // update fields
                final String id = generateId(name, 't');
                teacher.setName(name);
                teacher.setSubject(subjectOrGrade);
                teacher.setId(id);
                ((Teacher) user).setPassword(CommonUtils.DEFAULT_PASSWORD);
                // add teacher to database, and create an account for them.
                addTeacher(teacher);

            } else if (user instanceof Student) {
                final Student student = (Student) user;
                // update object fields
                final String id = generateId(name, 's');
                student.setName(name);
                student.setGrade(subjectOrGrade);
                student.setId(id);
                // set default password
                ((Student) user).setPassword(CommonUtils.DEFAULT_PASSWORD);

                // add student to database, and create an account for them.
                addStudent(student);
            }

            dialog.dismiss();
        });


        dialog.show();
    }


    private void addClassroom(Classroom c) {
        model.createClassroom(c).addOnSuccessListener(task -> {
            CommonUtils.showMessage(getContext(), R.string.classroom_created);
            viewUsersNow("c");

        });

    }

    /*
    Creates an account for a teacher user, and adds
    their info to the database.
    When done, goes to ViewUsers Page.
     */
    private void addTeacher(Teacher t) {
        model.addUser(t).addOnSuccessListener(task -> {
            CommonUtils.showMessage(getContext(), R.string.teacher_added);
            model.reLogIn(() -> viewUsersNow("t"));

        });

    }

    /*
    Creates an account for a student user, and adds
    their info to the database.
    When done, goes to ViewUsers Page.
        */
    private void addStudent(Student s) {
        model.addUser(s).addOnSuccessListener(task -> {
            CommonUtils.showMessage(getContext(), R.string.student_added);
            model.reLogIn(() -> viewUsersNow("s"));

        });

    }


    // id is (t or s) + first char of name + last char of name + some random numbers
    // Examples: tam843351, stk140243, chg339520
    private String generateId(String name, char idLetter) {
        StringBuilder id = new StringBuilder();


        if (Character.isLetterOrDigit(idLetter))
            id.append(idLetter);


        name = name.toLowerCase();


        if (Character.isLetterOrDigit(name.charAt(0))) // first letter of name
            id.append(name.charAt(0));


        char lastChar = name.charAt(name.length() - 1); // get last letter
        if (Character.isLetterOrDigit(lastChar))
            id.append(lastChar);


        // random 6 digits
        Random random = new Random();
        for (int i = 0; i < 6; i++)
            id.append(random.nextInt(10));

        return id.toString();


    }


    /*
    shows a logout dialog
    goes back to login page when signed out
     */
    private void logout() {

        // code that is executed upon confirmation (when user says yes to logout)
        Runnable logout = () -> {
            model.logout();

            CommonUtils.restartApp(getActivity());
        };

        CommonUtils.showConfirmDialog(getContext(), getString(R.string.logout),
                getString(R.string.logout_confirm), logout);

    }


}
