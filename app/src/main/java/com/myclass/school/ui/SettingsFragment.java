package com.myclass.school.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.myclass.school.CommonUtils;
import com.myclass.school.MainActivity;
import com.myclass.school.R;
import com.myclass.school.viewmodels.UserViewModel;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.GroupieViewHolder;
import com.xwray.groupie.Item;

import org.apache.commons.io.FileUtils;


public class SettingsFragment extends Fragment {

    public SettingsFragment() {
        // Required empty public constructor
    }

    private View view;
    private UserViewModel model;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        init();
    }


    private void init() {
        if (getActivity() == null) return;


        final RecyclerView rv = view.findViewById(R.id.settings_rv);
        final GroupAdapter adapter = new GroupAdapter();
        model = ((MainActivity) getActivity()).userVM;

        adapter.add(new OptionItem(Option.CHANGE_PASSWORD));
        adapter.add(new OptionItem(Option.CHANGE_LANGUAGE));
        adapter.add(new OptionItem(Option.CLEAR_CACHE));
        adapter.add(new OptionItem(Option.LOGOUT));
        adapter.add(new OptionItem(Option.HELP));
        adapter.add(new OptionItem(Option.ABOUT));
        rv.setAdapter(adapter);

    }

    private void goToHelpScreen() {
        Navigation.findNavController(view).navigate(R.id.helpFragment);
    }

    private void changePassword() {
        // initialize dialog and its content
        final Context context = view.getContext();

        final AlertDialog dialog = new AlertDialog.Builder(context).create();

        final View layout =
                View.inflate(context, R.layout.change_password_dialog, null);

        dialog.setView(layout);


        final TextInputEditText oldPassText = layout.findViewById(R.id.old_password);
        final TextInputEditText newPassText = layout.findViewById(R.id.new_password);


        final AppCompatButton confirm = layout.findViewById(R.id.dialog_confirm_button);
        final AppCompatButton cancel = layout.findViewById(R.id.dialog_cancel_button);

        cancel.setOnClickListener(v -> dialog.dismiss());

        confirm.setOnClickListener(v -> {
            if (oldPassText.getText() == null || newPassText.getText() == null) return;
            final String oldPassword = oldPassText.getText().toString();
            final String newPassword = newPassText.getText().toString();

            if (oldPassword.isEmpty() || oldPassword.length() < 8) {
                oldPassText.setError(getString(R.string.password_too_short));
                return;
            }

            if (newPassword.isEmpty() || newPassword.length() < 8) {
                newPassText.setError(getString(R.string.password_too_short));
                return;
            }
            final Runnable doneAction = () ->
                    CommonUtils.showMessage(view.getContext(), R.string.password_changed);

            model.changePassword(oldPassword, newPassword, doneAction);

            dialog.dismiss();


        });
        dialog.show();

    }

    private void about() {
        final String title = getString(R.string.about);
        final String text = getString(R.string.text_sample);

        final AlertDialog.Builder dialog = new AlertDialog.Builder(view.getContext());

        dialog.setPositiveButton(R.string.ok, (dialog1, which) -> dialog1.dismiss());

        dialog.setTitle(title);
        dialog.setMessage(text);
        dialog.show();
    }

    private void clearCache() {
        FileUtils.deleteQuietly(view.getContext().getCacheDir());

        CommonUtils.showMessage(getContext(), R.string.clear_cache_done);
    }

    private void logout() {
        // code that is executed upon confirmation (when user says yes to logout)
        Runnable logout = () -> {
            model.logout();
            CommonUtils.setOnline(false);

            CommonUtils.restartApp(getActivity());
        };

        CommonUtils.showConfirmDialog(getContext(), getString(R.string.logout),
                getString(R.string.logout_confirm), logout);

    }

    private void changeLanguage() {
        if (getActivity() != null)
            CommonUtils.changeLanguage(getActivity());
    }


    enum Option {
        ABOUT, CHANGE_PASSWORD, CLEAR_CACHE, LOGOUT, HELP, CHANGE_LANGUAGE
    }

    private class OptionItem extends Item<GroupieViewHolder> {

        private Option option;

        OptionItem(Option op) {
            option = op;
        }


        @Override
        public int getLayout() {
            return R.layout.option_item;
        }

        @Override
        public void bind(@NonNull GroupieViewHolder viewHolder, int position) {

            final View itemView = viewHolder.itemView;
            final Context context = itemView.getContext();
            final AppCompatTextView title = itemView.findViewById(R.id.option_item_title);
            final AppCompatImageView icon = itemView.findViewById(R.id.option_item_icon);

            final Drawable drawable;
            final String text;
            final Runnable action;

            // set icon, text and click action based on option type
            switch (option) {
                case ABOUT: {
                    text = context.getString(R.string.about);
                    drawable = ContextCompat.getDrawable(context, R.drawable.ic_info);
                    action = SettingsFragment.this::about;
                    break;
                }
                case LOGOUT: {
                    text = context.getString(R.string.logout);
                    drawable = ContextCompat.getDrawable(context, R.drawable.ic_exit);
                    action = SettingsFragment.this::logout;
                    break;

                }
                case CLEAR_CACHE: {
                    text = context.getString(R.string.clear_cache);
                    drawable = ContextCompat.getDrawable(context, R.drawable.ic_cached);
                    action = SettingsFragment.this::clearCache;
                    break;

                }
                case CHANGE_PASSWORD: {
                    text = context.getString(R.string.change_password);
                    drawable = ContextCompat.getDrawable(context, R.drawable.ic_lock);
                    action = SettingsFragment.this::changePassword;

                    break;

                }
                case HELP: {
                    text = context.getString(R.string.help);
                    drawable = ContextCompat.getDrawable(context, R.drawable.ic_help);
                    action = SettingsFragment.this::goToHelpScreen;
                    break;
                }
                case CHANGE_LANGUAGE: {
                    text = context.getString(R.string.change_language);
                    drawable = ContextCompat.getDrawable(context, R.drawable.ic_language);
                    action = SettingsFragment.this::changeLanguage;
                    break;
                }
                // redundant case
                default: {
                    text = context.getString(R.string.app_name);
                    drawable = ContextCompat.getDrawable(context, R.drawable.ic_settings);
                    action = SettingsFragment.this::clearCache;
                    break;
                }

            }


            icon.setImageDrawable(drawable);
            title.setText(text);
            itemView.setOnClickListener(v -> action.run());


        }

    }
}
