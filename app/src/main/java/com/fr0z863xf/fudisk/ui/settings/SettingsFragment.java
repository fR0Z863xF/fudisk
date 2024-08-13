/*package com.fr0z863xf.fudisk.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.compose.runtime.Composer;
import androidx.compose.ui.platform.ComposeView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.fr0z863xf.fudisk.R;
import com.fr0z863xf.fudisk.databinding.FragmentSettingsBinding;


import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function2;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        SettingsViewModel settingsViewModel =
                new ViewModelProvider(this).get(SettingsViewModel.class);

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();




        return root;
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ComposeView composeView = view.findViewById(R.id.settings_compose_view);
        composeView.setContent(new Function2<Composer, Integer, Unit>() {
            @Override
            public Unit invoke(Composer composer, Integer integer) {
                com.fr0z863xf.fudisk.ui.settings.SettingsViewKt.SettingsView();
                return null;
            }
        });




    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
*
 */