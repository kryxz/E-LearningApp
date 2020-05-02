package com.myclass.school;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.myclass.school.data.Classroom;
import com.myclass.school.data.Student;
import com.myclass.school.data.Teacher;
import com.myclass.school.data.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/*
    A ViewModel manages data in the ui
    ViewModel classes must be public
 */
public class AdminViewModel extends ViewModel {

    // an object which contains methods that deal with the firebase database and auth
    private final DatabaseRepository repo = new DatabaseRepository();


    // observable objects that hold data for teachers and students
    private MutableLiveData<List<Teacher>> teachers = new MutableLiveData<>();
    private MutableLiveData<List<Student>> students = new MutableLiveData<>();
    private MutableLiveData<List<Classroom>> classes = new MutableLiveData<>();


    // logout from auth
    void logout() {
        repo.getAuth().signOut();
    }


    // login as Admin, and execute some action
    void reLogIn(Runnable action) {
        logout();
        signInAsAdmin(action);
    }


    // get admin email and password from database, then sign in as app admin
    private void signInAsAdmin(Runnable action) {
        repo.getUsers().document("admin").get().addOnSuccessListener(task -> {

            final String pass = task.get("password", String.class);
            final String email = task.get("email", String.class);

            if (pass != null && email != null) {
                // login
                repo.getAuth().signInWithEmailAndPassword(email, pass);

                action.run();
            }

        });

    }

    // add user to database, and create an account for them!
    Task<AuthResult> addUser(User user) {
        String email = user.getEmail();

        // if user is Teacher, add a teacher
        if (user instanceof Teacher) {
            String id = ((Teacher) user).getId();
            repo.getTeachersRef().document(id).set(user);
            addToDb(id);


            // add student
        } else if (user instanceof Student) {
            String id = ((Student) user).getId();
            repo.getStudentsRef().document(id).set(user);
            addToDb(id);
        }

        // return creating an account as a task
        return repo.getAuth().createUserWithEmailAndPassword(email, Common.DEFAULT_PASSWORD);

    }


    public void updateUser(User user) {
        if (user instanceof Teacher) {
            Teacher t = (Teacher) user;
            repo.getTeachersRef().document(t.getId()).set(t);

        } else if (user instanceof Student) {
            Student s = (Student) user;
            repo.getStudentsRef().document(s.getId()).set(s);

        }
    }


    // add user to users collection
    private void addToDb(String name) {
        HashMap<String, String> map = new HashMap<>();
        map.put("name", name);
        repo.getUsers().document(name).set(map);

    }

    // remove user from users collection
    private void removeFromDb(String name) {
        repo.getUsers().document(name).delete();
    }


    public void deleteClassroom(Classroom classroom) {
        ArrayList<String> members = classroom.getMembers();

        String id = classroom.getId();
        for (String member : members)
            if (member.charAt(0) == 't')
                repo.getTeachersRef().document(member).update("classes",
                        FieldValue.arrayRemove(id));
            else
                repo.getStudentsRef().document(member).update("classes",
                        FieldValue.arrayRemove(id));


        repo.getClassroomsRef().document(id).delete();


    }

    // delete user from database
    public void deleteUser(User user) {

        // get id from email
        String id = user.getEmail().substring(0, user.getEmail().indexOf('@'));

        ArrayList<String> classes;

        CollectionReference ref = repo.getTeachersRef();
        // remove from database
        if (user instanceof Teacher) {
            classes = ((Teacher) user).getClasses();
        } else {
            ref = repo.getStudentsRef();
            classes = ((Student) user).getClasses();

        }

        for (String classroomItem : classes) {
            // remove from members
            repo.getClassroomsRef().document(classroomItem).update("members",
                    FieldValue.arrayRemove(id));
            // delete instructor from class
            if (user instanceof Teacher)
                repo.getClassroomsRef().document(classroomItem).update("instructor", null);
        }


        // remove from database
        ref.document(id).delete();
        removeFromDb(id);

        // delete profile image from storage
        repo.getImagesRef().child(id + ".png").delete();
        // remove from auth (delete user account)
        removeFromAuth(user.getEmail(), user.getPassword());

    }


    /*
      to delete a user account,
      login as that user,
      delete the user, then re login as admin
     */
    private void removeFromAuth(String email, String password) {
        repo.getAuth().signInWithEmailAndPassword(
                email, password)
                .addOnSuccessListener(task -> {
                    FirebaseUser user = repo.getAuth().getCurrentUser();
                    if (user != null)
                        user.delete().addOnSuccessListener(result -> {
                            // login as admin
                            reLogIn(() -> {
                            });
                        });
                });
    }


    // observe (listen to changes in) the teachers collection
    LiveData<List<Teacher>> getTeachers() {
        // ArrayList to add all teachers from database
        ArrayList<Teacher> allTeachers = new ArrayList<>();

        // listen to changes in teachers
        repo.getTeachersRef().addSnapshotListener((query, exception) -> {
            // check for exceptions, or if there is no data
            if (exception != null || query == null) return;

            // clear array if it holds any data
            allTeachers.clear();


            // loop through all documents and add teachers to the array
            for (DocumentSnapshot snapshot : query.getDocuments()) {
                // get document as a Teacher object
                Teacher teacher = snapshot.toObject(Teacher.class);

                allTeachers.add(teacher);
            }

            // set the value of the array to the LiveData object
            teachers.setValue(allTeachers);
        });
        return teachers;
    }

    // observe the students collection
    LiveData<List<Student>> getStudents() {
        // ArrayList to add all students from database
        List<Student> allStudents = new ArrayList<>();

        repo.getStudentsRef().addSnapshotListener((query, e) -> {
            // check for exceptions, or if there is no data
            if (e != null || query == null) return;

            allStudents.clear();

            for (DocumentSnapshot snapshot : query.getDocuments()) {
                // cast document to a Student object
                Student student = snapshot.toObject(Student.class);

                allStudents.add(student);
            }
            // set the value of the array to the LiveData object
            students.setValue(allStudents);
        });

        return students;
    }

    Task<Void> createClassroom(Classroom classroom) {
        return repo.getClassroomsRef().document(classroom.getId()).set(classroom);
    }


    public LiveData<List<Classroom>> getAllClassrooms() {

        List<Classroom> allClasses = new ArrayList<>();

        repo.getClassroomsRef().addSnapshotListener((query, e) -> {
            if (e != null || query == null) return;

            allClasses.clear();

            for (DocumentSnapshot snapshot : query.getDocuments()) {
                // cast document to a Student object
                Classroom classroom = snapshot.toObject(Classroom.class);

                allClasses.add(classroom);
            }

            classes.setValue(allClasses);

        });

        return classes;
    }

    public void updateClassroom(Classroom classroom) {
        repo.getClassroomsRef().document(classroom.getId()).set(classroom);
    }

    public LiveData<String> getNameById(String id) {

        MutableLiveData<String> name = new MutableLiveData<>();

        CollectionReference ref = repo.getTeachersRef();

        if (id.charAt(0) == 's')
            ref = repo.getStudentsRef();

        ref.document(id).get().addOnSuccessListener(query ->
                name.setValue(query.get("name", String.class)));
        return name;
    }
}
