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
import com.example.storehub.model.ProductColor;
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
    private android.widget.LinearLayout adminColorContainer;
    private List<ProductColor> productColors = new java.util.ArrayList<>();

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
        findViewById(R.id.layoutAddColor).setOnClickListener(v -> showAddColorDialog());
        submitButton.setOnClickListener(v -> submitProduct());

        if (editMode) {
            loadProductDetail();
        } else {
            initializeDefaultColors();
        }
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
        adminColorContainer = findViewById(R.id.adminColorContainer);
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

        productColors.clear();
        if (product.getColors() != null) {
            productColors.addAll(product.getColors());
        }
        renderAdminColors();
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

        // Ensure at least one color is default before submitting
        if (!productColors.isEmpty()) {
            boolean hasDefault = false;
            for (ProductColor c : productColors) {
                if (c.isDefault()) {
                    hasDefault = true;
                    break;
                }
            }
            if (!hasDefault) {
                productColors.get(0).setDefault(true);
            }
        }
        String colorsJson = new Gson().toJson(productColors);
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

    private void initializeDefaultColors() {
        String[][] values = {{"Đen", "#000000"}, {"Xám", "#E2E3E3"}, {"Trắng", "#FFFFFF"},
                {"Xanh", "#354A40"}, {"Nâu", "#6D665E"}, {"Đỏ", "#D1160D"}};
        productColors.clear();
        for (int i = 0; i < values.length; i++) {
            ProductColor color = new ProductColor();
            color.setId(String.valueOf(i + 1));
            color.setName(values[i][0]);
            color.setHex(values[i][1]);
            color.setDefault(i == 0);
            productColors.add(color);
        }
        renderAdminColors();
    }

    private void renderAdminColors() {
        adminColorContainer.removeAllViews();
        if (productColors == null || productColors.isEmpty()) {
            return;
        }
        for (int i = 0; i < productColors.size(); i++) {
            final int index = i;
            ProductColor color = productColors.get(i);
            
            android.widget.FrameLayout frameLayout = new android.widget.FrameLayout(this);
            android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                    dp(44),
                    dp(44)
            );
            params.setMarginEnd(dp(10));
            frameLayout.setLayoutParams(params);

            // Outer border
            View borderView = new View(this);
            android.widget.FrameLayout.LayoutParams borderParams = new android.widget.FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
            borderView.setLayoutParams(borderParams);
            
            android.graphics.drawable.GradientDrawable outerDrawable = new android.graphics.drawable.GradientDrawable();
            outerDrawable.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            outerDrawable.setColor(android.graphics.Color.TRANSPARENT);
            if (color.isDefault()) {
                outerDrawable.setStroke(dp(3), android.graphics.Color.parseColor("#172C22"));
            } else {
                outerDrawable.setStroke(dp(1), android.graphics.Color.parseColor("#C3C5C3"));
            }
            borderView.setBackground(outerDrawable);
            frameLayout.addView(borderView);

            // Inner circle
            View circleView = new View(this);
            android.widget.FrameLayout.LayoutParams circleParams = new android.widget.FrameLayout.LayoutParams(
                    dp(34),
                    dp(34)
            );
            circleParams.gravity = android.view.Gravity.CENTER;
            circleView.setLayoutParams(circleParams);
            
            android.graphics.drawable.GradientDrawable innerDrawable = new android.graphics.drawable.GradientDrawable();
            innerDrawable.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            int colorVal = parseColorSafely(color.getHex());
            innerDrawable.setColor(colorVal);
            if (isLightColor(colorVal)) {
                innerDrawable.setStroke(dp(1), android.graphics.Color.parseColor("#DDDDDD"));
            }
            circleView.setBackground(innerDrawable);
            frameLayout.addView(circleView);

            frameLayout.setContentDescription(color.getName());
            
            // Click to select default
            frameLayout.setOnClickListener(v -> {
                for (int j = 0; j < productColors.size(); j++) {
                    productColors.get(j).setDefault(j == index);
                }
                renderAdminColors();
            });

            // Long click to edit or delete
            frameLayout.setOnLongClickListener(v -> {
                showEditColorDialog(color, index);
                return true;
            });

            adminColorContainer.addView(frameLayout);
        }
    }

    private int parseColorSafely(String hex) {
        try {
            if (hex == null || hex.isEmpty()) return android.graphics.Color.LTGRAY;
            if (!hex.startsWith("#")) hex = "#" + hex;
            return android.graphics.Color.parseColor(hex);
        } catch (Exception e) {
            return android.graphics.Color.LTGRAY;
        }
    }

    private boolean isLightColor(int color) {
        double luminance = (0.299 * android.graphics.Color.red(color) + 0.587 * android.graphics.Color.green(color) + 0.114 * android.graphics.Color.blue(color)) / 255;
        return luminance > 0.85;
    }

    private void showAddColorDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Thêm biến thể màu sắc");

        android.widget.ScrollView scrollView = new android.widget.ScrollView(this);
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(dp(20), dp(15), dp(20), dp(10));

        final EditText nameInput = new EditText(this);
        nameInput.setHint("Tên màu (Ví dụ: Vàng cát)");
        nameInput.setSingleLine(true);
        layout.addView(nameInput);

        View space = new View(this);
        space.setLayoutParams(new android.widget.LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(12)));
        layout.addView(space);

        final EditText hexInput = new EditText(this);
        hexInput.setHint("Mã Hex (Ví dụ: #FFD700)");
        hexInput.setSingleLine(true);
        layout.addView(hexInput);

        View space2 = new View(this);
        space2.setLayoutParams(new android.widget.LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(12)));
        layout.addView(space2);

        // Color preview
        final View colorPreview = new View(this);
        android.widget.LinearLayout.LayoutParams previewParams = new android.widget.LinearLayout.LayoutParams(dp(50), dp(50));
        previewParams.gravity = android.view.Gravity.CENTER_HORIZONTAL;
        colorPreview.setLayoutParams(previewParams);
        android.graphics.drawable.GradientDrawable previewDrawable = new android.graphics.drawable.GradientDrawable();
        previewDrawable.setShape(android.graphics.drawable.GradientDrawable.OVAL);
        previewDrawable.setColor(android.graphics.Color.LTGRAY);
        previewDrawable.setStroke(dp(1), android.graphics.Color.parseColor("#CCCCCC"));
        colorPreview.setBackground(previewDrawable);
        layout.addView(colorPreview);

        View space3 = new View(this);
        space3.setLayoutParams(new android.widget.LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(12)));
        layout.addView(space3);

        // Button to open color picker dialog
        Button pickColorBtn = new Button(this);
        pickColorBtn.setText("🎨 Chọn từ bảng màu");
        pickColorBtn.setAllCaps(false);
        pickColorBtn.setOnClickListener(v -> {
            new com.skydoves.colorpickerview.ColorPickerDialog.Builder(this)
                    .setTitle("Chọn màu")
                    .setPositiveButton("Chọn", (com.skydoves.colorpickerview.listeners.ColorEnvelopeListener) (envelope, fromUser) -> {
                        String hex = "#" + envelope.getHexCode().substring(2);
                        hexInput.setText(hex);
                        nameInput.setText(guessColorName(envelope.getColor()));
                        android.graphics.drawable.GradientDrawable bg = (android.graphics.drawable.GradientDrawable) colorPreview.getBackground();
                        bg.setColor(envelope.getColor());
                    })
                    .setNegativeButton("Hủy", (dialogInterface, i) -> dialogInterface.dismiss())
                    .attachAlphaSlideBar(false)
                    .attachBrightnessSlideBar(true)
                    .setBottomSpace(12)
                    .show();
        });
        layout.addView(pickColorBtn);

        View space4 = new View(this);
        space4.setLayoutParams(new android.widget.LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(12)));
        layout.addView(space4);

        final android.widget.CheckBox defaultCheck = new android.widget.CheckBox(this);
        defaultCheck.setText("Đặt làm màu mặc định");
        layout.addView(defaultCheck);

        View space5 = new View(this);
        space5.setLayoutParams(new android.widget.LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(12)));
        layout.addView(space5);

        TextView presetLabel = new TextView(this);
        presetLabel.setText("Chọn nhanh màu mẫu:");
        presetLabel.setTextSize(14);
        presetLabel.setTextColor(android.graphics.Color.GRAY);
        layout.addView(presetLabel);

        android.widget.HorizontalScrollView scrollPresets = new android.widget.HorizontalScrollView(this);
        scrollPresets.setHorizontalScrollBarEnabled(false);
        
        android.widget.LinearLayout scrollContent = new android.widget.LinearLayout(this);
        scrollContent.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        
        String[][] presets = {
            {"Đen", "#000000"}, {"Xám", "#E2E3E3"}, {"Trắng", "#FFFFFF"},
            {"Xanh", "#354A40"}, {"Nâu", "#6D665E"}, {"Đỏ", "#D1160D"},
            {"Vàng", "#FFD700"}, {"Hồng", "#FFC0CB"}, {"Tím", "#800080"}
        };

        for (String[] preset : presets) {
            View swatch = new View(this);
            android.widget.LinearLayout.LayoutParams swParams = new android.widget.LinearLayout.LayoutParams(dp(30), dp(30));
            swParams.setMarginEnd(dp(8));
            swatch.setLayoutParams(swParams);
            
            android.graphics.drawable.GradientDrawable swDrawable = new android.graphics.drawable.GradientDrawable();
            swDrawable.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            int colVal = android.graphics.Color.parseColor(preset[1]);
            swDrawable.setColor(colVal);
            swDrawable.setStroke(dp(1), android.graphics.Color.parseColor("#CCCCCC"));
            swatch.setBackground(swDrawable);
            
            swatch.setOnClickListener(v -> {
                nameInput.setText(preset[0]);
                hexInput.setText(preset[1]);
                android.graphics.drawable.GradientDrawable bg = (android.graphics.drawable.GradientDrawable) colorPreview.getBackground();
                bg.setColor(android.graphics.Color.parseColor(preset[1]));
            });
            scrollContent.addView(swatch);
        }
        scrollPresets.addView(scrollContent);
        layout.addView(scrollPresets);

        scrollView.addView(layout);
        builder.setView(scrollView);

        builder.setPositiveButton("Thêm", (dialog, which) -> {
            String name = nameInput.getText().toString().trim();
            String hex = hexInput.getText().toString().trim();
            
            if (name.isEmpty() || hex.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ tên và mã màu", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!hex.startsWith("#")) {
                hex = "#" + hex;
            }
            
            ProductColor newColor = new ProductColor();
            newColor.setId(String.valueOf(System.currentTimeMillis()));
            newColor.setName(name);
            newColor.setHex(hex);
            newColor.setDefault(defaultCheck.isChecked());

            if (defaultCheck.isChecked()) {
                for (ProductColor c : productColors) {
                    c.setDefault(false);
                }
            }
            
            productColors.add(newColor);
            renderAdminColors();
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showEditColorDialog(ProductColor color, int index) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Chỉnh sửa màu sắc");

        android.widget.ScrollView scrollView = new android.widget.ScrollView(this);
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(dp(20), dp(15), dp(20), dp(10));

        final EditText nameInput = new EditText(this);
        nameInput.setHint("Tên màu");
        nameInput.setText(color.getName());
        nameInput.setSingleLine(true);
        layout.addView(nameInput);

        View space = new View(this);
        space.setLayoutParams(new android.widget.LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(12)));
        layout.addView(space);

        final EditText hexInput = new EditText(this);
        hexInput.setHint("Mã Hex");
        hexInput.setText(color.getHex());
        hexInput.setSingleLine(true);
        layout.addView(hexInput);

        View space2 = new View(this);
        space2.setLayoutParams(new android.widget.LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(12)));
        layout.addView(space2);

        // Color preview (shows current color)
        final View colorPreview = new View(this);
        android.widget.LinearLayout.LayoutParams previewParams = new android.widget.LinearLayout.LayoutParams(dp(50), dp(50));
        previewParams.gravity = android.view.Gravity.CENTER_HORIZONTAL;
        colorPreview.setLayoutParams(previewParams);
        android.graphics.drawable.GradientDrawable previewDrawable = new android.graphics.drawable.GradientDrawable();
        previewDrawable.setShape(android.graphics.drawable.GradientDrawable.OVAL);
        previewDrawable.setColor(parseColorSafely(color.getHex()));
        previewDrawable.setStroke(dp(1), android.graphics.Color.parseColor("#CCCCCC"));
        colorPreview.setBackground(previewDrawable);
        layout.addView(colorPreview);

        View space3 = new View(this);
        space3.setLayoutParams(new android.widget.LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(12)));
        layout.addView(space3);

        // Button to open color picker dialog
        Button pickColorBtn = new Button(this);
        pickColorBtn.setText("🎨 Chọn từ bảng màu");
        pickColorBtn.setAllCaps(false);
        pickColorBtn.setOnClickListener(v -> {
            new com.skydoves.colorpickerview.ColorPickerDialog.Builder(this)
                    .setTitle("Chọn màu")
                    .setPositiveButton("Chọn", (com.skydoves.colorpickerview.listeners.ColorEnvelopeListener) (envelope, fromUser) -> {
                        String hex = "#" + envelope.getHexCode().substring(2);
                        hexInput.setText(hex);
                        nameInput.setText(guessColorName(envelope.getColor()));
                        android.graphics.drawable.GradientDrawable bg = (android.graphics.drawable.GradientDrawable) colorPreview.getBackground();
                        bg.setColor(envelope.getColor());
                    })
                    .setNegativeButton("Hủy", (dialogInterface, i) -> dialogInterface.dismiss())
                    .attachAlphaSlideBar(false)
                    .attachBrightnessSlideBar(true)
                    .setBottomSpace(12)
                    .show();
        });
        layout.addView(pickColorBtn);

        View space4 = new View(this);
        space4.setLayoutParams(new android.widget.LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(12)));
        layout.addView(space4);

        final android.widget.CheckBox defaultCheck = new android.widget.CheckBox(this);
        defaultCheck.setText("Đặt làm màu mặc định");
        defaultCheck.setChecked(color.isDefault());
        layout.addView(defaultCheck);

        scrollView.addView(layout);
        builder.setView(scrollView);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String name = nameInput.getText().toString().trim();
            String hex = hexInput.getText().toString().trim();
            
            if (name.isEmpty() || hex.isEmpty()) {
                Toast.makeText(this, "Tên và mã màu không được bỏ trống", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!hex.startsWith("#")) {
                hex = "#" + hex;
            }
            
            color.setName(name);
            color.setHex(hex);
            color.setDefault(defaultCheck.isChecked());

            if (defaultCheck.isChecked()) {
                for (int i = 0; i < productColors.size(); i++) {
                    if (i != index) {
                        productColors.get(i).setDefault(false);
                    }
                }
            }
            
            renderAdminColors();
        });

        builder.setNeutralButton("Xóa màu", (dialog, which) -> {
            productColors.remove(index);
            boolean hasDefault = false;
            for (ProductColor c : productColors) {
                if (c.isDefault()) {
                    hasDefault = true;
                    break;
                }
            }
            if (!hasDefault && !productColors.isEmpty()) {
                productColors.get(0).setDefault(true);
            }
            renderAdminColors();
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private static final String[][] COLOR_NAME_TABLE = {
            {"Đen", "#000000"}, {"Trắng", "#FFFFFF"},
            {"Xám đậm", "#404040"}, {"Xám", "#808080"}, {"Xám nhạt", "#C0C0C0"},
            {"Đỏ", "#FF0000"}, {"Đỏ đậm", "#8B0000"}, {"Đỏ tươi", "#DC143C"},
            {"Cam", "#FF8C00"}, {"Cam nhạt", "#FFA500"},
            {"Vàng", "#FFD700"}, {"Vàng nhạt", "#FFFF00"}, {"Vàng cát", "#F4C430"},
            {"Xanh lá", "#00AA00"}, {"Xanh lá đậm", "#006400"}, {"Xanh lá nhạt", "#90EE90"},
            {"Xanh rêu", "#556B2F"}, {"Xanh olive", "#808000"},
            {"Xanh ngọc", "#00CED1"}, {"Xanh cyan", "#00FFFF"},
            {"Xanh dương", "#0000FF"}, {"Xanh dương nhạt", "#4169E1"}, {"Xanh navy", "#000080"},
            {"Xanh da trời", "#87CEEB"}, {"Xanh cobalt", "#0047AB"},
            {"Tím", "#800080"}, {"Tím nhạt", "#9370DB"}, {"Tím đậm", "#4B0082"},
            {"Hồng", "#FF69B4"}, {"Hồng nhạt", "#FFC0CB"}, {"Hồng đậm", "#C71585"},
            {"Nâu", "#8B4513"}, {"Nâu nhạt", "#D2B48C"}, {"Nâu đậm", "#5C4033"},
            {"Be", "#F5F5DC"}, {"Kem", "#FFFDD0"}, {"Bạc", "#C0C0C0"},
            {"Vàng đồng", "#B87333"}, {"Vàng gold", "#FFD700"}, {"Xanh mint", "#98FF98"},
            {"Xanh teal", "#008080"}, {"Đỏ san hô", "#FF7F50"}, {"Lavender", "#E6E6FA"}
    };

    private String guessColorName(int color) {
        int r = android.graphics.Color.red(color);
        int g = android.graphics.Color.green(color);
        int b = android.graphics.Color.blue(color);

        String bestName = "Tùy chỉnh";
        double bestDist = Double.MAX_VALUE;

        for (String[] entry : COLOR_NAME_TABLE) {
            int ref = android.graphics.Color.parseColor(entry[1]);
            int dr = r - android.graphics.Color.red(ref);
            int dg = g - android.graphics.Color.green(ref);
            int db = b - android.graphics.Color.blue(ref);
            double dist = Math.sqrt(dr * dr + dg * dg + db * db);
            if (dist < bestDist) {
                bestDist = dist;
                bestName = entry[0];
            }
        }
        return bestName;
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
