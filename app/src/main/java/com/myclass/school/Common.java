package com.myclass.school;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.TaskStackBuilder;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;
import java.util.Random;


/*
A class which holds some common methods that are used in other places
 */

public class Common {
    // some default values
    public static final String EMAIL_SUFFIX = "@elearn.jo";
    static final String DEFAULT_PASSWORD = "elearn123";


    private static int[] colors = new int[]{
            R.color.colorAccent,
            R.color.colorPrimary,
            R.color.colorPrimaryDark,
            R.color.red,
            R.color.green,
    };


    static int getRandomColor(Context c, int pos) {
        return ContextCompat.getColor(c, colors[pos % colors.length]);
    }


    /*
    a simple confirm/cancel dialog
    performs an action upon confirmation
     */

    public static void showConfirmDialog(Context context, String title, String message,
                                         Runnable confirmAction) {


        // initialize dialog and its content
        final AlertDialog dialog = new AlertDialog.Builder(context).create();

        final View layout = View.inflate(context, R.layout.confirm_dialog, null);
        dialog.setView(layout);


        // title and message
        final AppCompatTextView titleEd = layout.findViewById(R.id.dialog_title);
        final AppCompatTextView messageEd = layout.findViewById(R.id.dialog_message);
        titleEd.setText(title);
        messageEd.setText(message);

        // buttons confirm and cancel
        final AppCompatButton confirm = layout.findViewById(R.id.dialog_confirm_button);
        final AppCompatButton cancel = layout.findViewById(R.id.dialog_cancel_button);


        // dismiss dialog on cancel
        cancel.setOnClickListener(v -> dialog.dismiss());

        // run action on confirm
        confirm.setOnClickListener(v -> {
            confirmAction.run();

            dialog.dismiss();
        });


        dialog.show();
    }


    // shows a simple toast message
    static void showMessage(Context context, int id) {
        Toast.makeText(context, context.getString(id), Toast.LENGTH_SHORT).show();
    }


    // copies text to device clipboard
    public static void copyText(Context context, String text) {

        // a clipData object to hold the text data
        final ClipData copied = ClipData.newPlainText("code", text);

        // get the ClipboardManager from context
        final ClipboardManager clipboardManager = ((ClipboardManager)
                context.getSystemService(Context.CLIPBOARD_SERVICE));

        // set text data to clipboard
        if (clipboardManager != null)
            clipboardManager.setPrimaryClip(copied);


        showMessage(context, R.string.copied);
    }


    static void setDrawerIcon(Activity ac) {
        if (ac == null) return;


        ActionBar bar = ((MainActivity) ac).getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setHomeAsUpIndicator(R.drawable.ic_format_list);
/*            if (bar.getTitle() == null) return;

            String title = bar.getTitle().toString();
            if (!title.contains("  "))
                bar.setTitle("  " + title);*/

        }

    }

    static void openDrawer(Activity activity) {
        if (activity == null) return;
        DrawerLayout drawer = activity.findViewById(R.id.drawer_layout);

        // close if open
        if (drawer.isOpen())
            drawer.closeDrawer(GravityCompat.START);
        else
            drawer.openDrawer(GravityCompat.START);

    }

    @SuppressLint("ClickableViewAccessibility") // disable an IDE warning
    static void passwordView(final AppCompatEditText editText) {
        // hides and shows password when user clicks at the 'eye' icon.
        editText.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (editText.getRight() - editText.getCompoundDrawables()[0].getBounds().width())) {
                    if (editText.getTransformationMethod() == null) {
                        // Hides password.
                        editText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, R.drawable.ic_visibility_off, 0);
                        editText.setTransformationMethod(new PasswordTransformationMethod());
                    } else {
                        // Shows password
                        editText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, R.drawable.ic_visibility, 0);
                        editText.setTransformationMethod(null);
                    }
                    return true;
                }
            }
            return false;
        });
    }


    static void hideKeypad(View v) {
        Context context = v.getContext();
        ((InputMethodManager)
                Objects.requireNonNull(context.getSystemService(Activity.INPUT_METHOD_SERVICE)))
                .hideSoftInputFromWindow(v.getWindowToken(), 0);
    }



/*
    static void recreateFragment(View view, int id) {
        Navigation.findNavController(view).navigate(id,
                null,
                new NavOptions.Builder().setPopUpTo(id, true).build());

    }
*/


    private static final DatabaseRepository repo = new DatabaseRepository();

    static void setOnline(boolean online) {
        FirebaseUser user = repo.getUser();
        if (user == null) return;
        final String email = user.getEmail();
        if (email == null || email.contains("admin")) return;

        final String id = email.substring(0, email.indexOf('@'));

        if (id.charAt(0) == 't')
            repo.getTeachersRef().document(id).update("online", online);
        else
            repo.getStudentsRef().document(id).update("online", online);
    }

    static void restartApp(Activity activity) {
        if (activity == null) return;
        TaskStackBuilder.create(activity)
                .addNextIntent(new Intent(activity, MainActivity.class))
                .addNextIntent(activity.getIntent())
                .startActivities();
    }
}
