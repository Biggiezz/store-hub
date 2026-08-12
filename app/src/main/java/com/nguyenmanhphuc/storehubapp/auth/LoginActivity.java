package com.nguyenmanhphuc.storehubapp.auth;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.util.Log;
import android.util.Patterns;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialCancellationException;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.credentials.exceptions.NoCredentialException;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.nguyenmanhphuc.storehubapp.BaseActivity;
import com.nguyenmanhphuc.storehubapp.MainActivity;
import com.nguyenmanhphuc.storehubapp.R;
import com.nguyenmanhphuc.storehubapp.admin.HomePageManagementActivity;
import com.nguyenmanhphuc.storehubapp.model.News;
import com.nguyenmanhphuc.storehubapp.model.Product;
import com.nguyenmanhphuc.storehubapp.model.User;
import com.nguyenmanhphuc.storehubapp.model.request.FirebaseLoginRequest;
import com.nguyenmanhphuc.storehubapp.model.request.LoginRequest;
import com.nguyenmanhphuc.storehubapp.model.response.LoginResponse;
import com.nguyenmanhphuc.storehubapp.model.response.Response;
import com.nguyenmanhphuc.storehubapp.services.HttpResquest;
import com.nguyenmanhphuc.storehubapp.utils.LoadingDialogHelper;
import com.nguyenmanhphuc.storehubapp.utils.SharedPreferencesManager;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;

public class LoginActivity extends BaseActivity {

    private static final String TAG = "LoginActivity";

    private TextInputEditText edtEmail, edtPassword;
    private TextInputLayout tilEmail, tilPassword;
    private MaterialButton btnLogin, btnGoogleLogin;
    private TextView tvRegisterNow;
    private SharedPreferencesManager prefManager;
    private CredentialManager credentialManager;
    private FirebaseAuth firebaseAuth;

    private ArrayList<Product> preloadedProducts = null;
    private ArrayList<News> preloadedNews = null;
    private boolean isProductsCallDone = false, isNewsCallDone = false;
    private static final int FEATURED_PRODUCT_LIMIT = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        prefManager = new SharedPreferencesManager(this);
        credentialManager = CredentialManager.create(this);
        firebaseAuth = FirebaseAuth.getInstance();

        initUi();
        setUpListener();
    }

    private void initUi() {
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        tvRegisterNow = findViewById(R.id.tvRegisterNow);
    }

    private void setUpListener() {
        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> handleLogin());
        }
        if (btnGoogleLogin != null) {
            btnGoogleLogin.setOnClickListener(v -> startGoogleSignIn());
        }

        if (tvRegisterNow != null) {
            tvRegisterNow.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            });
        }

        if (edtEmail != null) {
            edtEmail.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (tilEmail != null) tilEmail.setError(null);
                }

                @Override
                public void afterTextChanged(android.text.Editable s) {
                }
            });
        }
        if (edtPassword != null) {
            edtPassword.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (tilPassword != null) tilPassword.setError(null);
                }

                @Override
                public void afterTextChanged(android.text.Editable s) {
                }
            });
        }
    }

    private void startGoogleSignIn() {
        String clientId = getString(R.string.default_web_client_id).trim();
        if (clientId.isEmpty()) {
            Toast.makeText(this, R.string.google_login_not_configured, Toast.LENGTH_LONG).show();
            return;
        }

        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(clientId)
                .setAutoSelectEnabled(false)
                .build();

        GetSignInWithGoogleOption signInWithGoogleOption = new GetSignInWithGoogleOption.Builder(clientId).build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .addCredentialOption(signInWithGoogleOption)
                .build();

        credentialManager.getCredentialAsync(this, request, new CancellationSignal(), ContextCompat.getMainExecutor(this),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        handleGoogleCredential(result.getCredential());
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        Log.e(TAG, "Google credential failed: " + e.getType(), e);

                        if (e instanceof NoCredentialException) {
                            Toast.makeText(LoginActivity.this, "Không tìm thấy tài khoản Google trên thiết bị. Vui lòng kiểm tra cài đặt.", Toast.LENGTH_LONG).show();
                        } else if (e instanceof GetCredentialCancellationException) {
                            Toast.makeText(LoginActivity.this, R.string.google_login_cancelled, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LoginActivity.this, R.string.google_login_failed, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    private void handleGoogleCredential(Credential credential) {
        if (!(credential instanceof CustomCredential)
                || !GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(credential.getType())) {
            Toast.makeText(this, R.string.google_login_failed, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            GoogleIdTokenCredential googleCredential = GoogleIdTokenCredential.createFrom(credential.getData());
            authenticateWithFirebase(googleCredential.getIdToken());
        } catch (RuntimeException e) {
            Log.e(TAG, "Cannot parse Google credential", e);
            Toast.makeText(this, R.string.google_login_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void authenticateWithFirebase(String googleIdToken) {
        LoadingDialogHelper loadingDialog = new LoadingDialogHelper(this);
        loadingDialog.setMessage(getString(R.string.google_login_loading));
        loadingDialog.show();

        AuthCredential credential = GoogleAuthProvider.getCredential(googleIdToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (!task.isSuccessful() || firebaseAuth.getCurrentUser() == null) {
                        loadingDialog.dismiss();
                        Log.e(TAG, "Firebase sign-in failed", task.getException());
                        Toast.makeText(this, R.string.google_login_failed, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    firebaseAuth.getCurrentUser().getIdToken(true)
                            .addOnCompleteListener(tokenTask -> {
                                if (!tokenTask.isSuccessful() || tokenTask.getResult().getToken() == null) {
                                    loadingDialog.dismiss();
                                    Log.e(TAG, "Cannot obtain Firebase ID token", tokenTask.getException());
                                    Toast.makeText(this, R.string.google_login_failed, Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                loginWithFirebaseToken(tokenTask.getResult().getToken(), loadingDialog);
                            });
                });
    }

    private void loginWithFirebaseToken(String firebaseIdToken, LoadingDialogHelper loadingDialog) {
        new HttpResquest().callAPI().googleLogin(new FirebaseLoginRequest(firebaseIdToken))
                .enqueue(new Callback<LoginResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<LoginResponse> call,
                                           @NonNull retrofit2.Response<LoginResponse> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getCode() == 200) {
                            LoginResponse apiResponse = response.body();
                            prefManager.saveUserSession(apiResponse.getToken(), apiResponse.getData());
                            loadingDialog.setMessage(getString(R.string.loading_product_data));
                            preloadData(loadingDialog);
                            return;
                        }
                        loadingDialog.dismiss();
                        String message = response.body() != null
                                ? response.body().getMessage()
                                : getString(R.string.google_login_failed);
                        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                        loadingDialog.dismiss();
                        Toast.makeText(LoginActivity.this,
                                String.format(getString(R.string.connection_error_prefix), t.getMessage()),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleLogin() {
        String email = edtEmail != null && edtEmail.getText() != null ? edtEmail.getText().toString().trim() : "";
        String password = edtPassword != null && edtPassword.getText() != null ? edtPassword.getText().toString().trim() : "";

        if (tilEmail != null) tilEmail.setError(null);
        if (tilPassword != null) tilPassword.setError(null);

        if (email.isEmpty()) {
            if (tilEmail != null) {
                tilEmail.setError(getString(R.string.toast_enter_email));
                tilEmail.requestFocus();
            } else {
                Toast.makeText(this, getString(R.string.toast_enter_email), Toast.LENGTH_SHORT).show();
            }
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (tilEmail != null) {
                tilEmail.setError(getString(R.string.toast_invalid_email));
                tilEmail.requestFocus();
            } else {
                Toast.makeText(this, getString(R.string.toast_invalid_email), Toast.LENGTH_SHORT).show();
            }
            return;
        }

        if (password.isEmpty()) {
            if (tilPassword != null) {
                tilPassword.setError(getString(R.string.toast_enter_password));
                tilPassword.requestFocus();
            } else {
                Toast.makeText(this, getString(R.string.toast_enter_password), Toast.LENGTH_SHORT).show();
            }
            return;
        }

        LoadingDialogHelper loadingDialog = new LoadingDialogHelper(this);
        loadingDialog.setMessage(getString(R.string.loading_logging_in));
        loadingDialog.show();

        LoginRequest request = new LoginRequest(email, password);

        HttpResquest httpResquest = new HttpResquest();
        httpResquest.callAPI().login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull retrofit2.Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse apiResponse = response.body();
                    if (apiResponse.getCode() == 200) {
                        prefManager.saveUserSession(apiResponse.getToken(), apiResponse.getData());
                        loadingDialog.setMessage(getString(R.string.loading_product_data));
                        preloadData(loadingDialog);
                    } else {
                        loadingDialog.dismiss();
                        Toast.makeText(LoginActivity.this, apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    loadingDialog.dismiss();
                    Toast.makeText(LoginActivity.this, getString(R.string.toast_login_failed), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                loadingDialog.dismiss();
                Toast.makeText(LoginActivity.this, String.format(getString(R.string.connection_error_prefix), t.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void preloadData(LoadingDialogHelper loadingDialog) {
        HttpResquest httpResquest = new HttpResquest();

        isProductsCallDone = false;
        isNewsCallDone = false;
        preloadedProducts = null;
        preloadedNews = null;

        // Tải danh sách sản phẩm (50 sản phẩm để phục vụ lọc danh mục)
        httpResquest.callAPI().getListProduct(1, 50, "", false, "").enqueue(new Callback<Response<ArrayList<Product>>>() {
            @Override
            public void onResponse(@NonNull Call<Response<ArrayList<Product>>> call, @NonNull retrofit2.Response<Response<ArrayList<Product>>> response) {
                isProductsCallDone = true;
                if (response.isSuccessful() && response.body() != null) {
                    Response<ArrayList<Product>> apiResponse = response.body();
                    if (apiResponse.getCode() == 200) {
                        preloadedProducts = apiResponse.getData();
                    }
                }
                preloadProductImages(loadingDialog);
            }

            @Override
            public void onFailure(@NonNull Call<Response<ArrayList<Product>>> call, @NonNull Throwable t) {
                isProductsCallDone = true;
                checkPreloadComplete(loadingDialog);
            }
        });

        // Tải danh sách tin tức
        httpResquest.callAPI().getListNews(1, 5, "published").enqueue(new Callback<Response<ArrayList<News>>>() {
            @Override
            public void onResponse(@NonNull Call<Response<ArrayList<News>>> call, @NonNull retrofit2.Response<Response<ArrayList<News>>> response) {
                isNewsCallDone = true;
                if (response.isSuccessful() && response.body() != null) {
                    Response<ArrayList<News>> apiResponse = response.body();
                    if (apiResponse.getCode() == 200) {
                        preloadedNews = apiResponse.getData();
                    }
                }
                preloadNewsImages(loadingDialog);
            }

            @Override
            public void onFailure(@NonNull Call<Response<ArrayList<News>>> call, @NonNull Throwable t) {
                isNewsCallDone = true;
                checkPreloadComplete(loadingDialog);
            }
        });
    }

    private void preloadProductImages(LoadingDialogHelper loadingDialog) {
        ArrayList<String> imageUrls = new ArrayList<>();
        if (preloadedProducts != null) {
            for (Product product : preloadedProducts) {
                imageUrls.add(product.getImage());
                if (imageUrls.size() == FEATURED_PRODUCT_LIMIT) break;
            }
        }
        preloadImages(imageUrls, () -> {
            isProductsCallDone = true;
            checkPreloadComplete(loadingDialog);
        });
    }

    private void preloadNewsImages(LoadingDialogHelper loadingDialog) {
        ArrayList<String> imageUrls = new ArrayList<>();
        if (preloadedNews != null) {
            for (News news : preloadedNews) imageUrls.add(news.getImage());
        }
        preloadImages(imageUrls, () -> {
            isNewsCallDone = true;
            checkPreloadComplete(loadingDialog);
        });
    }

    private void preloadImages(ArrayList<String> imageUrls, Runnable onComplete) {
        if (imageUrls.isEmpty()) {
            onComplete.run();
            return;
        }

        int[] remaining = {imageUrls.size()};
        for (String imageUrl : imageUrls) {
            Glide.with(this)
                    .load(imageUrl)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(GlideException e, Object model,
                                                    Target<Drawable> target, boolean isFirstResource) {
                            finishPreload();
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model,
                                                       Target<Drawable> target,
                                                       com.bumptech.glide.load.DataSource dataSource,
                                                       boolean isFirstResource) {
                            finishPreload();
                            return false;
                        }

                        private void finishPreload() {
                            if (--remaining[0] == 0) onComplete.run();
                        }
                    })
                    .preload();
        }
    }

    private void checkPreloadComplete(LoadingDialogHelper loadingDialog) {
        if (isProductsCallDone && isNewsCallDone) {
            if (loadingDialog != null && loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }

            // Gán dữ liệu preload vào cache static của MainActivity
            MainActivity.preloadedProducts = preloadedProducts;
            MainActivity.preloadedNews = preloadedNews;

            Toast.makeText(LoginActivity.this, LoginActivity.this.getString(R.string.toast_dang_nhap_thanh_cong), Toast.LENGTH_SHORT).show();

            /// Kiểm tra role để chuyển đến màn hình Quản trị (HomePageManagement) hoặc Trang người dùng (MainActivity)
            User user = prefManager.getUser();
            String role = user != null && user.getRole() != null ? user.getRole().trim().toLowerCase() : "";

            Intent intent;
            if (role.equals("admin") || role.equals("super admin") || role.equals("superadmin")) {
                intent = new Intent(LoginActivity.this, HomePageManagementActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            } else {
                intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            }

            startActivity(intent);
            finish();
        }
    }
}
