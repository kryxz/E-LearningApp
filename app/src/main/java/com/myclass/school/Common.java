package com.myclass.school;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.format.DateUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.TaskStackBuilder;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;


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
            R.color.pico_void,
            R.color.maz_blue,
            R.color.wild_green,
            R.color.aqua_velvet,
            R.color.steel_pink,
            R.color.orange_light,

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
        if (context != null)
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

        Drawable drawable = ContextCompat.getDrawable(ac, R.drawable.ic_format_list);
        if (drawable == null) return;

        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, ContextCompat.getColor(ac, R.color.lynx_white));
        DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_IN);

        ActionBar bar = ((MainActivity) ac).getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setHomeAsUpIndicator(drawable);
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

    static void fabVisibility(Activity activity, int visibility) {
        if (activity == null) return;
        activity.findViewById(R.id.open_drawer_fab).setVisibility(visibility);

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


    static void tintDrawableTextView(AppCompatTextView tv, int color) {
        Drawable drawable = ContextCompat.getDrawable(tv.getContext(), R.drawable.colored_circle);
        if (drawable == null) return;

        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, ContextCompat.getColor(tv.getContext(), color));
        DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_IN);
        tv.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
    }

    static Drawable tintDrawable(Context context, int id, int color) {
        Drawable drawable = ContextCompat.getDrawable(context, id);
        if (drawable == null) return null;

        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, color);
        DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_IN);
        return drawable;
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


    static void updateTitle(Activity ac, int id) {
        MainActivity activity = ((MainActivity) ac);

        if (activity != null) {
            ActionBar bar = activity.getSupportActionBar();
            if (bar != null)
                bar.setTitle(ac.getString(id));
        }

    }

    static void updateTitle(Activity ac, String text) {
        MainActivity activity = ((MainActivity) ac);

        if (activity != null) {
            ActionBar bar = activity.getSupportActionBar();
            if (bar != null)
                bar.setTitle(text);
        }

    }

    static void hideActionBar(Activity ac) {
        if (ac != null) {
            ActionBar bar = ((AppCompatActivity) ac).getSupportActionBar();
            if (bar != null) bar.hide();
        }

    }

    static void showActionBar(Activity ac) {
        if (ac != null) {
            ActionBar bar = ((AppCompatActivity) ac).getSupportActionBar();
            if (bar != null) bar.show();
        }

    }

    static String getTimeAgo(long time) {
        return DateUtils.getRelativeTimeSpanString(
                time,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
        ).toString();
    }

    static Drawable getDrawableFromView(View v) {
        Bitmap returnedBitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable = v.getBackground();
        if (bgDrawable != null)
            // has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        else
            // does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);

        v.draw(canvas);
        //return the bitmap
        return new BitmapDrawable(v.getResources(),
                returnedBitmap);
    }
}

