package com.vart.library.vdownloader.customer;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.vart.library.vdownloader.R;

public class UpgradeDialog extends DialogFragment {

    private Activity activity;
    private Button btnInstall;
    private TextView tvVersion;
    private LinearLayout llLogs;
    private ProgressBar progressBar;
    private ImageView ivExit;
    String buttonText;
    String version;
    String[] upgradeLogs;
    boolean canCancel;
    View.OnClickListener onButtonClick;

    public UpgradeDialog(String buttonText, String version, String[] upgradeLogs, boolean canCancel, View.OnClickListener onButtonClick) {
        this.buttonText = buttonText;
        this.version = version;
        this.upgradeLogs = upgradeLogs;
        this.canCancel = canCancel;
        this.onButtonClick = onButtonClick;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.activity = getActivity();
        Log.d(">>>>", "onActivityCreated");
        btnInstall = getDialog().findViewById(R.id.btnInstall);
        tvVersion = getDialog().findViewById(R.id.tvVersion);
        llLogs = getDialog().findViewById(R.id.llLogs);
        progressBar = getDialog().findViewById(R.id.progressBar);
        ivExit = getDialog().findViewById(R.id.ivExit);

        btnInstall.setText(this.buttonText);
        tvVersion.setText(this.version);
        llLogs.removeAllViews();
        if (this.upgradeLogs != null && this.upgradeLogs.length > 0) {
            for (String upgradeLog : this.upgradeLogs) {
                TextView textView = (TextView) LayoutInflater.from(activity).inflate(R.layout.tv_dialog_upgrade_item, null, false);
                textView.setText(upgradeLog);
                llLogs.addView(textView);
            }
        }
        getDialog().setCancelable(this.canCancel);
        this.btnInstall.setOnClickListener(this.onButtonClick);
        this.progressBar.setVisibility(View.GONE);
        ivExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getDialog() != null && canCancel)
                    getDialog().dismiss();
            }
        });
    }

    @Override public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(0x00000000));
            getDialog().getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getDialog() != null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
        return inflater.inflate(R.layout.dialog_upgrade, container, false);
    }

    @Override
    public void show(@NonNull FragmentManager manager, @Nullable String tag) {
        manager.beginTransaction().remove(this).commit();
        super.show(manager, tag);
    }

    public void setButtonText(String text) {
        btnInstall.setText(text);
    }

    public void showProgressVisible(int visible) {
        progressBar.setVisibility(visible);
    }

    public void setProgress(int progress) {
        progressBar.setProgress(progress);
    }

    public void enableButton(boolean enabled) {
        btnInstall.setEnabled(enabled);
    }

    public void setOnButtonClick(View.OnClickListener onButtonClick) {
        this.onButtonClick = onButtonClick;
        this.btnInstall.setOnClickListener(onButtonClick);
    }

    public static class Builder {
        String buttonText;
        String version;
        String[] upgradeLogs;
        boolean canCancel;
        View.OnClickListener onButtonClick;

        public Builder buttonText(String text) {
            this.buttonText = text;
            return this;
        }
        public Builder version(String version) {
            this.version = version;
            return this;
        }
        public Builder upgradeLogs(String[] upgradeLogs) {
            this.upgradeLogs = upgradeLogs;
            return this;
        }
        public Builder upgradeLogs(String upgradeLogs) {
            if (!TextUtils.isEmpty(upgradeLogs)) {
                this.upgradeLogs = upgradeLogs.split("\n");
            }
            return this;
        }
        public Builder canCancel(boolean canCancel) {
            this.canCancel = canCancel;
            return this;
        }
        public Builder onButtonClick(View.OnClickListener listener) {
            this.onButtonClick = listener;
            return this;
        }
        public UpgradeDialog build() {
            return new UpgradeDialog(buttonText, version, upgradeLogs, canCancel, onButtonClick);
        }
    }
}
