package com.simats.smartnetworkanalyzerfrontend.ui.auth;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.simats.smartnetworkanalyzerfrontend.R;
import com.simats.smartnetworkanalyzerfrontend.databinding.ActivityAuthBinding;

public class AuthActivity extends AppCompatActivity {
    private ActivityAuthBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (savedInstanceState == null) {
            navigateTo(new LoginFragment(), false);
        }
    }

    public void navigateTo(Fragment fragment, boolean addToBackStack) {
        var tx = getSupportFragmentManager().beginTransaction();
        tx.replace(R.id.auth_container, fragment);
        if (addToBackStack) {
            tx.addToBackStack(null);
        }
        tx.commit();
    }
}
