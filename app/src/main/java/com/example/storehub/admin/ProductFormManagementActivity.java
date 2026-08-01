package com.example.storehub.admin;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.storehub.R;
import com.example.storehub.model.Product;
import com.example.storehub.model.ProductColor;
import com.example.storehub.model.response.Response;
import com.example.storehub.services.HttpResquest;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;

public class ProductFormManagementActivity extends AppCompatActivity {
    private static final String EXTRA_PRODUCT_ID = "product_id";
    private static final MediaType TEXT = MediaType.get("text/plain; charset=utf-8");
    private static final String[][] COLOR_PALETTE = {
            {"Đen", "#000000"},
            {"Trắng", "#FFFFFF"},
            {"Xám", "#808080"},
            {"Đỏ", "#FF0000"},
            {"Xanh dương", "#0000FF"}
    };

    private EditText nameInput, descriptionInput, stockInput, priceInput;
    private TextView formTitle, categoryValue, uploadPrompt;
    private ImageView selectedImage;
    private Button submitButton;
    private View backButton, cancelButton, imagePickerLayout, categoryPickerLayout, addColorLayout;
    private String productId;
    private Product currentProduct;
    private Uri selectedImageUri;
    private Call<Response<Product>> currentCall;
    private LinearLayout adminColorContainer;
    private final List<ProductColor> productColors = new ArrayList<>();
    private boolean originalIsActive = true;

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
        initListener();
        productId = getIntent().getStringExtra(EXTRA_PRODUCT_ID);
        boolean editMode = productId != null && !productId.isBlank();
        formTitle.setText(editMode ? "Chỉnh sửa sản phẩm" : "Thêm sản phẩm mới");
        submitButton.setText(editMode ? "Lưu sản phẩm" : "Thêm");
        ViewGroup.LayoutParams submitLayout = submitButton.getLayoutParams();
        submitLayout.width = dp(editMode ? 174 : 110);
        submitButton.setLayoutParams(submitLayout);

        if (editMode) loadProductDetail();
    }

    private void initUi() {
        nameInput = findViewById(R.id.edtAdminProductName);
        descriptionInput = findViewById(R.id.edtAdminProductDescription);
        stockInput = findViewById(R.id.edtAdminProductStock);
        priceInput = findViewById(R.id.edtAdminProductPrice);
        formTitle = findViewById(R.id.tvProductFormTitle);
        categoryValue = findViewById(R.id.tvAdminCategoryValue);
        selectedImage = findViewById(R.id.ivAdminSelectedImage);
        uploadPrompt = findViewById(R.id.tvAdminUploadPrompt);
        submitButton = findViewById(R.id.btnSubmitProductForm);
        adminColorContainer = findViewById(R.id.adminColorContainer);
        backButton = findViewById(R.id.btnBackProductForm);
        cancelButton = findViewById(R.id.btnCancelProductForm);
        imagePickerLayout = findViewById(R.id.layoutAdminImagePicker);
        categoryPickerLayout = findViewById(R.id.layoutAdminCategoryPicker);
        addColorLayout = findViewById(R.id.layoutAddColor);
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void initListener() {
        backButton.setOnClickListener(v -> finish());
        cancelButton.setOnClickListener(v -> finish());
        imagePickerLayout.setOnClickListener(v -> imagePicker.launch("image/*"));
        categoryPickerLayout.setOnClickListener(this::showCategoryMenu);
        addColorLayout.setOnClickListener(v -> showColorDialog(null, -1));
        submitButton.setOnClickListener(v -> submitProduct());
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
        this.currentProduct = product;
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
        originalIsActive = product.isActive();
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

        if (name.isEmpty()) {
            nameInput.setError("Vui lòng nhập tên sản phẩm");
            return;
        }
        if (category.equals("Chọn danh mục")) {
            Toast.makeText(this, "Vui lòng chọn danh mục", Toast.LENGTH_SHORT).show();
            return;
        }
        if (stock.isEmpty()) {
            stockInput.setError("Vui lòng nhập tồn kho");
            return;
        }
        if (price.isEmpty() || "0".equals(price)) {
            priceInput.setError("Giá bán phải lớn hơn 0");
            return;
        }
        if (!editMode && selectedImageUri == null) {
            Toast.makeText(this, "Vui lòng chọn hình ảnh sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }

        MultipartBody.Part imagePart;
        try {
            imagePart = selectedImageUri == null ? null : createImagePart(selectedImageUri);
        } catch (IOException exception) {
            Toast.makeText(this, "Không thể đọc ảnh đã chọn", Toast.LENGTH_SHORT).show();
            return;
        }

        ensureDefaultColor();
        String colorsJson = new Gson().toJson(productColors);
        int currentSold = currentProduct != null ? currentProduct.getSold() : 0;
        setLoading(true);
        HttpResquest request = new HttpResquest();
        currentCall = editMode
                ? request.callAPI().updateProduct(productId, text(name), text(price), text(category),
                text(description), text(stock), text(String.valueOf(currentSold)), text(String.valueOf(originalIsActive)),
                text(colorsJson), imagePart)
                : request.callAPI().addProduct(text(name), text(price), text(category),
                text(description), text(stock), text("0"), text("true"), text(colorsJson), imagePart);
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

    private void renderAdminColors() {
        adminColorContainer.removeAllViews();
        for (int i = 0; i < productColors.size(); i++) {
            final int index = i;
            ProductColor color = productColors.get(i);

            FrameLayout frameLayout = new FrameLayout(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dp(44),
                    dp(44)
            );
            params.setMarginEnd(dp(10));
            frameLayout.setLayoutParams(params);

            // Outer border
            View borderView = new View(this);
            FrameLayout.LayoutParams borderParams = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
            borderView.setLayoutParams(borderParams);

            GradientDrawable outerDrawable = new GradientDrawable();
            outerDrawable.setShape(GradientDrawable.OVAL);
            outerDrawable.setColor(Color.TRANSPARENT);
            if (color.isDefault()) {
                outerDrawable.setStroke(dp(3), Color.parseColor("#172C22"));
            } else {
                outerDrawable.setStroke(dp(1), Color.parseColor("#C3C5C3"));
            }
            borderView.setBackground(outerDrawable);
            frameLayout.addView(borderView);

            // Inner circle
            View circleView = new View(this);
            FrameLayout.LayoutParams circleParams = new FrameLayout.LayoutParams(
                    dp(34),
                    dp(34)
            );
            circleParams.gravity = Gravity.CENTER;
            circleView.setLayoutParams(circleParams);

            GradientDrawable innerDrawable = new GradientDrawable();
            innerDrawable.setShape(GradientDrawable.OVAL);
            int colorVal = parseColorSafely(color.getHex());
            innerDrawable.setColor(colorVal);
            if (isLightColor(colorVal)) {
                innerDrawable.setStroke(dp(1), Color.parseColor("#DDDDDD"));
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
                showColorDialog(color, index);
                return true;
            });

            adminColorContainer.addView(frameLayout);
        }
    }

    private int parseColorSafely(String hex) {
        try {
            if (hex == null || hex.isEmpty()) return Color.LTGRAY;
            if (!hex.startsWith("#")) hex = "#" + hex;
            return Color.parseColor(hex);
        } catch (Exception e) {
            return Color.LTGRAY;
        }
    }

    private boolean isLightColor(int color) {
        double luminance = (0.299 * Color.red(color) + 0.587 * Color.green(color)
                + 0.114 * Color.blue(color)) / 255;
        return luminance > 0.85;
    }

    private void showColorDialog(ProductColor color, int index) {
        boolean editing = color != null;
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_admin_color, null);
        EditText colorNameInput = dialogView.findViewById(R.id.edtAdminColorName);
        EditText hexInput = dialogView.findViewById(R.id.edtAdminColorHex);
        View preview = dialogView.findViewById(R.id.viewAdminColorPreview);
        CheckBox defaultCheck = dialogView.findViewById(R.id.chkAdminDefaultColor);
        Button customColorPicker = dialogView.findViewById(R.id.btnAdminCustomColorPicker);
        colorNameInput.setFocusable(false);
        hexInput.setFocusable(false);
        setColor(preview, editing ? parseColorSafely(color.getHex()) : Color.LTGRAY);
        if (editing) bindColorDialog(color, colorNameInput, hexInput, defaultCheck);
        addColorPalette(colorNameInput, hexInput, preview, dialogView.findViewById(R.id.adminColorPalette));
        customColorPicker.setOnClickListener(v -> new com.skydoves.colorpickerview.ColorPickerDialog.Builder(this)
                .setTitle("Chọn màu")
                .setPositiveButton("Chọn", (com.skydoves.colorpickerview.listeners.ColorEnvelopeListener) (envelope, fromUser) -> {
                    colorNameInput.setText("Màu tùy chỉnh");
                    hexInput.setText(String.format("#%06X", 0xFFFFFF & envelope.getColor()));
                    setColor(preview, envelope.getColor());
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .attachAlphaSlideBar(false)
                .attachBrightnessSlideBar(true)
                .setBottomSpace(12)
                .show());

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(editing ? "Chỉnh sửa màu sắc" : "Thêm biến thể màu sắc")
                .setView(dialogView)
                .setPositiveButton(editing ? "Lưu" : "Thêm", (dialog, which) ->
                        saveColor(color, colorNameInput, hexInput, defaultCheck, editing))
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        if (editing) {
            builder.setNeutralButton("Xóa màu", (dialog, which) -> {
                productColors.remove(index);
                ensureDefaultColor();
                renderAdminColors();
            });
        }
        builder.show();
    }

    private void bindColorDialog(ProductColor color, EditText nameInput,
                                 EditText hexInput, CheckBox defaultCheck) {
        nameInput.setText(color.getName());
        hexInput.setText(color.getHex());
        defaultCheck.setChecked(color.isDefault());
    }

    private void addColorPalette(EditText nameInput, EditText hexInput,
                                 View preview, LinearLayout colors) {
        for (String[] sample : COLOR_PALETTE) {
            View item = LayoutInflater.from(this).inflate(
                    R.layout.item_admin_color_palette, colors, false);
            View swatch = item.findViewById(R.id.ivAdminPaletteSwatch);
            TextView name = item.findViewById(R.id.tvAdminPaletteColorName);
            setColor(swatch, Color.parseColor(sample[1]));
            name.setText(sample[0]);
            item.setContentDescription("Màu " + sample[0]);
            item.setOnClickListener(v -> {
                nameInput.setText(sample[0]);
                hexInput.setText(sample[1]);
                setColor(preview, Color.parseColor(sample[1]));
            });
            colors.addView(item);
        }
    }

    private void setColor(View view, int color) {
        if (view.getBackground() instanceof GradientDrawable background) {
            background.mutate();
            background.setColor(color);
        }
    }

    private void saveColor(ProductColor color, EditText nameInput, EditText hexInput,
                           CheckBox defaultCheck, boolean editing) {
        String name = nameInput.getText().toString().trim();
        String hex = hexInput.getText().toString().trim();
        if (name.isEmpty() || hex.isEmpty()) {
            Toast.makeText(this, "Tên và mã màu không được bỏ trống", Toast.LENGTH_SHORT).show();
            return;
        }

        ProductColor target = editing ? color : new ProductColor();
        if (defaultCheck.isChecked()) {
            for (ProductColor item : productColors) item.setDefault(false);
        }
        target.setName(name);
        target.setHex(hex.startsWith("#") ? hex : "#" + hex);
        target.setDefault(defaultCheck.isChecked());
        if (!editing) productColors.add(target);
        renderAdminColors();
    }

    private void ensureDefaultColor() {
        for (ProductColor color : productColors) {
            if (color.isDefault()) return;
        }
        if (!productColors.isEmpty()) productColors.get(0).setDefault(true);
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
