package com.example.storehub.admin;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.storehub.R;
import com.example.storehub.model.Product;
import com.example.storehub.model.Response;
import com.example.storehub.services.HttpResquest;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;

public class ProductFormManagementActivity extends AppCompatActivity {
    private static final String EXTRA_PRODUCT_ID = "product_id";
    private static final MediaType TEXT = MediaType.get("text/plain; charset=utf-8");

    private EditText nameInput, descriptionInput, stockInput, priceInput;
    private TextView categoryValue, uploadPrompt;
    private ImageView selectedImage;
    private Button submitButton;
    private String productId;
    private Uri selectedImageUri;
    private Call<Response<Product>> currentCall;

    private final ActivityResultLauncher<String> imagePicker = registerForActivityResult(
            new ActivityResultContracts.GetContent(), uri -> {
                if (uri == null) return;
                selectedImageUri = uri;
                selectedImage.setVisibility(View.VISIBLE);
                uploadPrompt.setVisibility(View.GONE);
                selectedImage.setImageURI(uri);
            });

    public static Intent createAddIntent(Context context) {
        return new Intent(context, ProductFormManagementActivity.class);
    }

    public static Intent createEditIntent(Context context, String productId) {
        return new Intent(context, ProductFormManagementActivity.class)
                .putExtra(EXTRA_PRODUCT_ID, productId);
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_form_management);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.productFormRoot), (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        initUi();
        productId = getIntent().getStringExtra(EXTRA_PRODUCT_ID);
        boolean editMode = productId != null && !productId.isBlank();
        ((TextView) findViewById(R.id.tvProductFormTitle))
                .setText(editMode ? "Chỉnh sửa sản phẩm" : "Thêm sản phẩm mới");
        submitButton.setText(editMode ? "Lưu sản phẩm" : "Thêm");
        ViewGroup.LayoutParams submitLayout = submitButton.getLayoutParams();
        submitLayout.width = dp(editMode ? 174 : 110);
        submitButton.setLayoutParams(submitLayout);

        findViewById(R.id.btnBackProductForm).setOnClickListener(v -> finish());
        findViewById(R.id.btnCancelProductForm).setOnClickListener(v -> finish());
        findViewById(R.id.layoutAdminImagePicker).setOnClickListener(v -> imagePicker.launch("image/*"));
        findViewById(R.id.layoutAdminCategoryPicker).setOnClickListener(this::showCategoryMenu);
        submitButton.setOnClickListener(v -> submitProduct());

        if (editMode) loadProductDetail();
    }

    private void initUi() {
        nameInput = findViewById(R.id.edtAdminProductName);
        descriptionInput = findViewById(R.id.edtAdminProductDescription);
        stockInput = findViewById(R.id.edtAdminProductStock);
        priceInput = findViewById(R.id.edtAdminProductPrice);
        categoryValue = findViewById(R.id.tvAdminCategoryValue);
        selectedImage = findViewById(R.id.ivAdminSelectedImage);
        uploadPrompt = findViewById(R.id.tvAdminUploadPrompt);
        submitButton = findViewById(R.id.btnSubmitProductForm);
    }

    private void showCategoryMenu(View anchor) {
        PopupMenu menu = new PopupMenu(this, anchor);
        List<String> categories = Arrays.asList("Điện thoại", "Máy tính", "Tai nghe", "Đồng hồ");
        for (String category : categories) menu.getMenu().add(category);
        menu.setOnMenuItemClickListener(item -> {
            categoryValue.setText(item.getTitle());
            return true;
        });
        menu.show();
    }

    private void loadProductDetail() {
        setLoading(true);
        currentCall = new HttpResquest().callAPI().getProductDetail(productId);
        currentCall.enqueue(new Callback<Response<Product>>() {
            @Override
            public void onResponse(@NonNull Call<Response<Product>> call, @NonNull retrofit2.Response<Response<Product>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    bindProduct(response.body().getData());
                } else {
                    Toast.makeText(ProductFormManagementActivity.this,
                            "Không thể tải thông tin sản phẩm", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<Product>> call, @NonNull Throwable throwable) {
                if (call.isCanceled()) return;
                setLoading(false);
                Toast.makeText(ProductFormManagementActivity.this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindProduct(Product product) {
        nameInput.setText(product.getName());
        descriptionInput.setText(product.getDescription());
        stockInput.setText(String.valueOf(product.getStock()));
        priceInput.setText(String.valueOf(product.getPriceAsLong()));
        categoryValue.setText(product.getCategory());
        selectedImage.setVisibility(View.VISIBLE);
        uploadPrompt.setVisibility(View.GONE);
        Glide.with(this).load(product.getImage()).centerCrop().into(selectedImage);
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void submitProduct() {
        String name = nameInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String stock = stockInput.getText().toString().trim();
        String price = priceInput.getText().toString().replaceAll("[^0-9]", "");
        String category = categoryValue.getText().toString().trim();
        boolean editMode = productId != null && !productId.isBlank();

        if (name.isEmpty()) { nameInput.setError("Vui lòng nhập tên sản phẩm"); return; }
        if (category.equals("Chọn danh mục")) {
            Toast.makeText(this, "Vui lòng chọn danh mục", Toast.LENGTH_SHORT).show(); return;
        }
        if (stock.isEmpty()) { stockInput.setError("Vui lòng nhập tồn kho"); return; }
        if (price.isEmpty() || "0".equals(price)) { priceInput.setError("Giá bán phải lớn hơn 0"); return; }
        if (!editMode && selectedImageUri == null) {
            Toast.makeText(this, "Vui lòng chọn hình ảnh sản phẩm", Toast.LENGTH_SHORT).show(); return;
        }

        MultipartBody.Part imagePart;
        try {
            imagePart = selectedImageUri == null ? null : createImagePart(selectedImageUri);
        } catch (IOException exception) {
            Toast.makeText(this, "Không thể đọc ảnh đã chọn", Toast.LENGTH_SHORT).show();
            return;
        }

        String colorsJson = new Gson().toJson(defaultColors());
        setLoading(true);
        HttpResquest request = new HttpResquest();
        currentCall = editMode
                ? request.callAPI().updateProduct(productId, text(name), text(price), text(category),
                        text(description), text(stock), text(colorsJson), imagePart)
                : request.callAPI().addProduct(text(name), text(price), text(category),
                        text(description), text(stock), text(colorsJson), imagePart);
        currentCall.enqueue(new Callback<Response<Product>>() {
            @Override
            public void onResponse(@NonNull Call<Response<Product>> call, @NonNull retrofit2.Response<Response<Product>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(ProductFormManagementActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(ProductFormManagementActivity.this, "Không thể lưu sản phẩm", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<Product>> call, @NonNull Throwable throwable) {
                if (call.isCanceled()) return;
                setLoading(false);
                Toast.makeText(ProductFormManagementActivity.this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private MultipartBody.Part createImagePart(Uri uri) throws IOException {
        String mime = getContentResolver().getType(uri);
        MediaType mediaType = MediaType.get(mime == null ? "image/*" : mime);
        byte[] bytes;
        try (InputStream stream = getContentResolver().openInputStream(uri)) {
            if (stream == null) throw new IOException("Image stream is unavailable");
            bytes = stream.readAllBytes();
        }
        return MultipartBody.Part.createFormData("image", getFileName(uri),
                RequestBody.create(mediaType, bytes));
    }

    private String getFileName(Uri uri) {
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index >= 0) return cursor.getString(index);
            }
        }
        return "product-" + System.currentTimeMillis() + ".jpg";
    }

    private RequestBody text(String value) {
        return RequestBody.create(TEXT, value);
    }

    private List<Map<String, Object>> defaultColors() {
        String[][] values = {{"Đen", "#000000"}, {"Xám", "#E2E3E3"}, {"Trắng", "#FFFFFF"},
                {"Xanh", "#354A40"}, {"Nâu", "#6D665E"}, {"Đỏ", "#D1160D"}};
        java.util.ArrayList<Map<String, Object>> colors = new java.util.ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            Map<String, Object> color = new LinkedHashMap<>();
            color.put("id", String.valueOf(i + 1));
            color.put("name", values[i][0]);
            color.put("hex", values[i][1]);
            color.put("isDefault", i == 0);
            colors.add(color);
        }
        return colors;
    }

    private void setLoading(boolean loading) {
        submitButton.setEnabled(!loading);
        submitButton.setText(loading ? "Đang lưu..." :
                (productId == null || productId.isBlank() ? "Thêm" : "Lưu sản phẩm"));
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDestroy() {
        if (currentCall != null) currentCall.cancel();
        super.onDestroy();
    }
}
