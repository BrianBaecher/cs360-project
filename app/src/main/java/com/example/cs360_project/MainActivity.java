package com.example.cs360_project;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.os.HandlerCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import grid.StockGridFragment;

public class MainActivity extends AppCompatActivity {

    LinearLayout navLayout;
    Button inventoryBtn;
    Button smsSettingsButton;
    Button toggleNavButton;

    TextView userNameDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        navLayout = findViewById(R.id.navbar);

        userNameDisplay = findViewById(R.id.userNameDisplay);
        String displayStr = "Logged in as: " + WarehouseApplication.getInstance().getUsername();
        userNameDisplay.setText(displayStr);

        // nav buttons, control which fragment appears in container
        inventoryBtn = findViewById(R.id.inventoryBtn);

        inventoryBtn.setOnClickListener(v -> setActiveFragment(new StockGridFragment()));

        smsSettingsButton = findViewById(R.id.smsSettingsBtn);
        smsSettingsButton.setOnClickListener(v -> {
            setActiveFragment(new PermissionSettingsFragment());
        });


        toggleNavButton = findViewById(R.id.toggleButton);
        toggleNavButton.setOnClickListener(v -> toggleNavVisibility());

    }
    private void setActiveFragment(Fragment frag) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, frag).commit();
    }

    private void toggleNavVisibility() {
        boolean isGone = View.GONE == navLayout.getVisibility();

        navLayout.setVisibility(isGone ? View.VISIBLE : View.GONE);

        toggleNavButton.setText(isGone ? "Hide" : "Show" );
    }
}