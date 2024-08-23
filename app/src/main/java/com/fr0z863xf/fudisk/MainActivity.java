package com.fr0z863xf.fudisk;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.fr0z863xf.fudisk.Utils.UpdateManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.fr0z863xf.fudisk.databinding.ActivityMainBinding;
import com.google.android.material.color.DynamicColors;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {



    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //SharedPreferences sharedPref = getDefaultSharedPreferences(this.getApplicationContext());

        //DynamicColors.applyToActivitiesIfAvailable(this.getApplication());

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_settings)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        UpdateManager.getInstance(null).needUpdate.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (!aBoolean) return;
                UpdateManager uM =  UpdateManager.getInstance(null);
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("软件更新")
                        .setMessage("发现新版本，更新日志：\n" + uM.releaseNotes )
                        .setCancelable(false)
                        .setPositiveButton("立即更新", (dialog, which) -> {
                        //复制链接
                        ClipboardManager clipboard = (ClipboardManager) MainActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("label", Objects.requireNonNullElse(uM.downloadLink, "https://github.com/fR0Z863xF/fudisk/releases/latest"));
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(MainActivity.this, "更新链接已复制到剪贴板", Toast.LENGTH_SHORT).show();
                        MainActivity.this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Objects.requireNonNullElse(uM.downloadLink, "https://github.com/fR0Z863xF/fudisk/releases/latest"))));
                        MainActivity.this.finish();
                    });
                builder.create().show();

            }
        });
    }





}