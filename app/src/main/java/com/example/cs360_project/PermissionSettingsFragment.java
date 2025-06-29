package com.example.cs360_project;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class PermissionSettingsFragment extends Fragment {

    Button promptButton;

    TextView permStatusText;
    TextView changeWarning;

    private final ActivityResultLauncher<String> reqSmsPermLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                Log.d("PermRequest", "SMS permission granted: " + isGranted);
                updateStatusText();
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.notify_screen, container, false);

        Log.d("PermSettings", "onCreateView reached");

        permStatusText = v.findViewById(R.id.permStatusText);
        promptButton = v.findViewById(R.id.smsPromptBtn);

        changeWarning = v.findViewById(R.id.permChangeWarningText);
        changeWarning.setText(R.string.sms_change_warning);
        changeWarning.setTextColor(Color.RED);
        changeWarning.setVisibility(View.INVISIBLE);

        promptButton.setOnClickListener(v2 -> {
            if (shouldShowRequestPermissionRationale(Manifest.permission.SEND_SMS)) {
                reqSmsPermLauncher.launch(Manifest.permission.SEND_SMS);
            } else {
                // meaning system refuses to show dialog
                showGoToSettingsMsg();
            }

            Log.d("PermRequest", "Launching SMS perm request...");
            reqSmsPermLauncher.launch(Manifest.permission.SEND_SMS);
        });

        updateStatusText();

        return v;
    }

    private void showGoToSettingsMsg() {
        // disable the button
        promptButton.setEnabled(false);
        // show explain that android dictates this dialogue can only be opened once per installation.
        changeWarning.setVisibility(View.VISIBLE);
    }

    private void updateStatusText() {
        boolean hasSmsPerms = WarehouseApplication.getInstance().appHasSmsPerms();

        permStatusText.setText(hasSmsPerms ? R.string.sms_perm_granted_by_user : R.string.sms_perm_denied_by_user);
    }
}
