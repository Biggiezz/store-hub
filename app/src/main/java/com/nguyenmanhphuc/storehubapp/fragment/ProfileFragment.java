commit 94c812d0ad165a4559b4f629556c41a674f54c83
Author: manh phuc <manhphuc300501@gmail.com>
Date:   Sun Aug 9 08:57:14 2026 +0700

    chore: lưu toàn bộ thay đổi hiện tại (bug fixes, UI updates)

diff --git a/app/src/main/java/com/nguyenmanhphuc/storehubapp/fragment/ProfileFragment.java b/app/src/main/java/com/nguyenmanhphuc/storehubapp/fragment/ProfileFragment.java
new file mode 100644
index 0000000..170c38c
--- /dev/null
+++ b/app/src/main/java/com/nguyenmanhphuc/storehubapp/fragment/ProfileFragment.java
@@ -0,0 +1,321 @@
+package com.nguyenmanhphuc.storehubapp.fragment;
+
+import android.app.Activity;
+import android.content.Intent;
+import android.graphics.Color;
+import android.graphics.drawable.ColorDrawable;
+import android.os.Bundle;
+import android.view.LayoutInflater;
+import android.view.View;
+import android.view.ViewGroup;
+import android.view.WindowManager;
+import android.widget.ImageView;
+import android.widget.TextView;
+import android.widget.Toast;
+
+import androidx.activity.result.ActivityResultLauncher;
+import androidx.activity.result.contract.ActivityResultContracts;
+import androidx.annotation.NonNull;
+import androidx.annotation.Nullable;
+import androidx.appcompat.app.AlertDialog;
+import androidx.fragment.app.Fragment;
+
+import com.bumptech.glide.Glide;
+import com.google.android.material.button.MaterialButton;
+import com.nguyenmanhphuc.storehubapp.ChangePasswordActivity;
+import com.nguyenmanhphuc.storehubapp.EditProfileActivity;
+import com.nguyenmanhphuc.storehubapp.MainActivity;
+import com.nguyenmanhphuc.storehubapp.R;
+import com.nguyenmanhphuc.storehubapp.model.User;
+import com.nguyenmanhphuc.storehubapp.model.response.Response;
+import com.nguyenmanhphuc.storehubapp.services.HttpResquest;
+import com.nguyenmanhphuc.storehubapp.utils.DateTimeUtils;
+import com.nguyenmanhphuc.storehubapp.utils.LocaleHelper;
+import com.nguyenmanhphuc.storehubapp.utils.SharedPreferencesManager;
+
+import java.util.Date;
+
+import retrofit2.Call;
+import retrofit2.Callback;
+
+public class ProfileFragment extends Fragment {
+
+    private TextView txtProfileName, txtEmailValue, txtPhoneValue, txtAddressValue, txtPasswordChangedSub, btnLangVI, btnLangEN;
+    private ImageView imgProfileAvatar, btnBack;
+    private View btnEditPersonalInfo, btnChangePassword, btnLogout;
+
+    private SharedPreferencesManager sharedPreferencesManager;
+    private final ActivityResultLauncher<Intent> editProfileLauncher = registerForActivityResult(
+            new ActivityResultContracts.StartActivityForResult(),
+            result -> {
+                if (result.getResultCode() == Activity.RESULT_OK) {
+                    bindUserData();
+                }
+            }
+    );
+
+    private final ActivityResultLauncher<Intent> changePasswordLauncher = registerForActivityResult(
+            new ActivityResultContracts.StartActivityForResult(),
+            result -> {
+                if (result.getResultCode() == Activity.RESULT_OK) {
+                    bindUserData();
+                }
+            }
+    );
+
+    @Nullable
+    @Override
+    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
+        return inflater.inflate(R.layout.fragment_profile, container, false);
+    }
+
+    @Override
+    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
+        super.onViewCreated(view, savedInstanceState);
+        sharedPreferencesManager = new SharedPreferencesManager(requireContext());
+
+        initUi(view);
+        updateLanguageToggleUI();
+        bindUserData();
+        setupClickListeners();
+    }
+
+    @Override
+    public void onResume() {
+        super.onResume();
+        bindUserData();
+    }
+
+    private void updateLanguageToggleUI() {
+        if (!isAdded()) return;
+        String lang = SharedPreferencesManager.getInstance(requireContext()).getLanguage();
+        if (lang.equals("vi")) {
+            if (btnLangVI != null) {
+                btnLangVI.setBackgroundResource(R.drawable.bg_language_selected);
+                btnLangVI.setTextColor(Color.parseColor("#41413F"));
+                btnLangVI.setTypeface(null, android.graphics.Typeface.BOLD);
+            }
+            if (btnLangEN != null) {
+                btnLangEN.setBackgroundColor(Color.TRANSPARENT);
+                btnLangEN.setTextColor(Color.parseColor("#8F8E8A"));
+                btnLangEN.setTypeface(null, android.graphics.Typeface.NORMAL);
+            }
+        } else {
+            if (btnLangEN != null) {
+                btnLangEN.setBackgroundResource(R.drawable.bg_language_selected);
+                btnLangEN.setTextColor(Color.parseColor("#41413F"));
+                btnLangEN.setTypeface(null, android.graphics.Typeface.BOLD);
+            }
+            if (btnLangVI != null) {
+                btnLangVI.setBackgroundColor(Color.TRANSPARENT);
+                btnLangVI.setTextColor(Color.parseColor("#8F8E8A"));
+                btnLangVI.setTypeface(null, android.graphics.Typeface.NORMAL);
+            }
+        }
+    }
+
+    private void initUi(View view) {
+        btnBack = view.findViewById(R.id.btnBack);
+        txtProfileName = view.findViewById(R.id.txtProfileName);
+        txtEmailValue = view.findViewById(R.id.txtEmailValue);
+        txtPhoneValue = view.findViewById(R.id.txtPhoneValue);
+        txtAddressValue = view.findViewById(R.id.txtAddressValue);
+        txtPasswordChangedSub = view.findViewById(R.id.txtPasswordChangedSub);
+        btnLangVI = view.findViewById(R.id.btnLangVI);
+        btnLangEN = view.findViewById(R.id.btnLangEN);
+        imgProfileAvatar = view.findViewById(R.id.imgProfileAvatar);
+        btnEditPersonalInfo = view.findViewById(R.id.btnEditPersonalInfo);
+        btnChangePassword = view.findViewById(R.id.btnChangePassword);
+        btnLogout = view.findViewById(R.id.btnLogout);
+    }
+
+    private void bindUserData() {
+        if (!isAdded()) return;
+        User user = sharedPreferencesManager.getUser();
+        if (user != null) {
+            String role = (user.getRole() != null && user.getRole().equalsIgnoreCase("admin"))
+                    ? getString(R.string.role_admin) : getString(R.string.role_customer);
+            String displayName = (user.getName() != null && !user.getName().isEmpty()) ? user.getName() : getString(R.string.role_customer);
+            if (txtProfileName != null) txtProfileName.setText(displayName + " • " + role);
+            if (txtEmailValue != null) txtEmailValue.setText(user.getEmail());
+            if (txtPhoneValue != null) txtPhoneValue.setText(user.getPhone());
+            if (txtAddressValue != null) {
+                txtAddressValue.setText(user.getAddress() == null || user.getAddress().isEmpty()
+                        ? getString(R.string.no_address_update) : user.getAddress());
+            }
+
+            updatePasswordChangeSubtext(user.getChangePasswordDate());
+
+            if (user.getImage() != null && !user.getImage().isEmpty() && imgProfileAvatar != null) {
+                Glide.with(this)
+                        .load(user.getImage())
+                        .placeholder(R.drawable.ic_avatar)
+                        .error(R.drawable.ic_avatar)
+                        .into(imgProfileAvatar);
+            }
+
+            if (user.getId() != null && !user.getId().isEmpty()) {
+                new HttpResquest().callAPI().getUserById(user.getId()).enqueue(new Callback<Response<User>>() {
+                    @Override
+                    public void onResponse(@NonNull Call<Response<User>> call, @NonNull retrofit2.Response<Response<User>> response) {
+                        if (!isAdded()) return;
+                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
+                            User freshUser = response.body().getData();
+                            sharedPreferencesManager.updateUser(freshUser);
+                            if (freshUser.getImage() != null && !freshUser.getImage().isEmpty() && imgProfileAvatar != null) {
+                                Glide.with(ProfileFragment.this)
+                                        .load(freshUser.getImage())
+                                        .placeholder(R.drawable.ic_avatar)
+                                        .error(R.drawable.ic_avatar)
+                                        .into(imgProfileAvatar);
+                            }
+                        }
+                    }
+
+                    @Override
+                    public void onFailure(@NonNull Call<Response<User>> call, @NonNull Throwable t) {
+                    }
+                });
+            }
+        }
+    }
+
+    private void updatePasswordChangeSubtext(String changeDateStr) {
+        if (!isAdded()) return;
+        if (changeDateStr == null || changeDateStr.isEmpty()) {
+            if (txtPasswordChangedSub != null) {
+                txtPasswordChangedSub.setText(getString(R.string.no_password_change));
+            }
+            return;
+        }
+
+        try {
+            Date changeDate = DateTimeUtils.parseISO(changeDateStr);
+            Date currentDate = new Date();
+
+            if (changeDate != null) {
+                long diffMs = currentDate.getTime() - changeDate.getTime();
+                long diffDays = diffMs / (1000 * 60 * 60 * 24);
+
+                String relativeText;
+                if (diffDays < 1) {
+                    relativeText = getString(R.string.password_changed_today);
+                } else if (diffDays < 30) {
+                    relativeText = String.format(getString(R.string.password_changed_days_ago), diffDays);
+                } else {
+                    long diffMonths = diffDays / 30;
+                    relativeText = String.format(getString(R.string.password_changed_months_ago), diffMonths);
+                }
+
+                if (txtPasswordChangedSub != null) {
+                    txtPasswordChangedSub.setText(relativeText);
+                }
+            }
+        } catch (Exception e) {
+            e.printStackTrace();
+        }
+    }
+
+    private void setupClickListeners() {
+        if (btnBack != null) {
+            btnBack.setOnClickListener(v -> {
+                if (getActivity() != null) {
+                    getActivity().getOnBackPressedDispatcher().onBackPressed();
+                }
+            });
+        }
+
+        if (btnEditPersonalInfo != null) {
+            btnEditPersonalInfo.setOnClickListener(v -> {
+                Intent intent = new Intent(requireContext(), EditProfileActivity.class);
+                editProfileLauncher.launch(intent);
+            });
+        }
+
+        if (btnChangePassword != null) {
+            btnChangePassword.setOnClickListener(v -> {
+                Intent intent = new Intent(requireContext(), ChangePasswordActivity.class);
+                changePasswordLauncher.launch(intent);
+            });
+        }
+
+        if (btnLangVI != null) btnLangVI.setOnClickListener(v -> selectLanguage(true));
+        if (btnLangEN != null) btnLangEN.setOnClickListener(v -> selectLanguage(false));
+        if (btnLogout != null) btnLogout.setOnClickListener(v -> showCustomLogoutDialog());
+    }
+
+    private void selectLanguage(boolean isVI) {
+        String lang = isVI ? "vi" : "en";
+        LocaleHelper.setLocale(requireContext(), lang);
+
+        Intent intent = new Intent(requireContext(), MainActivity.class);
+        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
+        startActivity(intent);
+        if (getActivity() != null) {
+            getActivity().finish();
+        }
+    }
+
+    private void showCustomLogoutDialog() {
+        if (!isAdded()) return;
+        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
+        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_logout, null);
+        builder.setView(dialogView);
+
+        AlertDialog dialog = builder.create();
+        MaterialButton btnConfirmLogout = dialogView.findViewById(R.id.btnConfirmLogout);
+        MaterialButton btnCancelLogout = dialogView.findViewById(R.id.btnCancelLogout);
+
+        if (btnConfirmLogout != null) {
+            btnConfirmLogout.setOnClickListener(v -> {
+                dialog.dismiss();
+                performServerLogout();
+            });
+        }
+
+        if (btnCancelLogout != null) {
+            btnCancelLogout.setOnClickListener(v -> dialog.dismiss());
+        }
+
+        dialog.show();
+
+        if (dialog.getWindow() != null) {
+            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
+            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
+        }
+    }
+
+    private void performServerLogout() {
+        if (!isAdded()) return;
+        String tokenHeader = "Bearer " + sharedPreferencesManager.getToken();
+
+        HttpResquest httpResquest = new HttpResquest();
+        httpResquest.callAPI().logout(tokenHeader).enqueue(new Callback<Response<Void>>() {
+            @Override
+            public void onResponse(@NonNull Call<Response<Void>> call, @NonNull retrofit2.Response<Response<Void>> response) {
+                if (!isAdded()) return;
+                sharedPreferencesManager.logout();
+                MainActivity.preloadedProducts = null;
+                MainActivity.preloadedNews = null;
+                Toast.makeText(requireContext().getApplicationContext(), getString(R.string.logout_success_toast), Toast.LENGTH_SHORT).show();
+                Intent intent = new Intent(requireContext().getApplicationContext(), MainActivity.class);
+                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
+                startActivity(intent);
+                if (getActivity() != null) getActivity().finish();
+            }
+
+            @Override
+            public void onFailure(@NonNull Call<Response<Void>> call, @NonNull Throwable t) {
+                if (!isAdded()) return;
+                sharedPreferencesManager.logout();
+                MainActivity.preloadedProducts = null;
+                MainActivity.preloadedNews = null;
+                Toast.makeText(requireContext().getApplicationContext(), getString(R.string.logout_toast), Toast.LENGTH_SHORT).show();
+                Intent intent = new Intent(requireContext().getApplicationContext(), MainActivity.class);
+                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
+                startActivity(intent);
+                if (getActivity() != null) getActivity().finish();
+            }
+        });
+    }
+}
