package com.nguyenmanhphuc.storehubapp.admin;

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
import com.nguyenmanhphuc.storehubapp.R;
import com.nguyenmanhphuc.storehubapp.model.Product;
import com.nguyenmanhphuc.storehubapp.model.Category;
import com.nguyenmanhphuc.storehubapp.model.ProductColor;
import com.nguyenmanhphuc.storehubapp.model.response.Response;
import com.nguyenmanhphuc.storehubapp.services.HttpResquest;
import com.nguyenmanhphuc.storehubapp.utils.DataCache;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;

public class ProductFormManagementActivity extends AppCompatActivity {
    private static final String EXTRA_PRODUCT_ID = "product_id";
    private static final MediaType TEXT = MediaType.get("text/plain; charset=utf-8");

    private String[][] getColorPalette() {
        return new String[][]{
                {getString(R.string.color_black), "#000000"},
                {getString(R.string.color_white), "#FFFFFF"},
                {getString(R.string.color_gray), "#808080"},
                {getString(R.string.color_red), "#FF0000"},
                {getString(R.string.color_blue), "#0000FF"}
        };
    }

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
    private List<Category> categoriesList = new ArrayList<>();
    private Category selectedCategory = null;

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
        formTitle.setText(editMode ? getString(R.string.edit_product_title) : getString(R.string.add_product_title));
        submitButton.setText(editMode ? getString(R.string.save_product) : getString(R.string.add));
        ViewGroup.LayoutParams submitLayout = submitButton.getLayoutParams();
        submitLayout.width = dp(editMode ? 174 : 110);
        submitButton.setLayoutParams(submitLayout);
        loadCategories();
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

    private void loadCategories() {
        new HttpResquest().callAPI().getCategories().enqueue(new Callback<Response<ArrayList<Category>>>() {
            @Override
            public void onResponse(@NonNull Call<Response<ArrayList<Category>>> call, @NonNull retrofit2.Response<Response<ArrayList<Category>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    categoriesList = response.body().getData();
                } else {
                    useFallbackCategories();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<ArrayList<Category>>> call, @NonNull Throwable t) {
                useFallbackCategories();
            }
        });
    }

    private void useFallbackCategories() {
        categoriesList = java.util.Arrays.asList(
                new Category("1", getString(R.string.category_phones)),
                new Category("2", getString(R.string.category_computers)),
                new Category("3", getString(R.string.category_headphones)),
                new Category("4", getString(R.string.category_watches))
        );
    }

    private void showCategoryMenu(View anchor) {
        if (categoriesList.isEmpty()) {
            Toast.makeText(this, this.getString(R.string.toast_dang_tai_danh_muc_vui_long_thu_lai_sau), Toast.LENGTH_SHORT).show();
            return;
        }
        PopupMenu menu = new PopupMenu(this, anchor);
        for (int i = 0; i < categoriesList.size(); i++) {
            menu.getMenu().add(0, i, 0, categoriesList.get(i).getName());
        }
        menu.setOnMenuItemClickListener(item -> {
            int index = item.getItemId();
            selectedCategory = categoriesList.get(index);
            categoryValue.setText(selectedCategory.getName());
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
                    Toast.makeText(ProductFormManagementActivity.this, ProductFormManagementActivity.this.getString(R.string.toast_khong_the_tai_thong_tin_san_pham), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<Product>> call, @NonNull Throwable throwable) {
                if (call.isCanceled()) return;
                setLoading(false);
                Toast.makeText(ProductFormManagementActivity.this, ProductFormManagementActivity.this.getString(R.string.toast_loi_ket_noi_server), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindProduct(Product product) {
        this.currentProduct = product;
        nameInput.setText(product.getName());
        descriptionInput.setText(product.getDescription());
        stockInput.setText(String.valueOf(product.getStock()));
        priceInput.setText(String.valueOf(product.getPriceAsLong()));
        if (product.getCategory() != null) {
            this.selectedCategory = product.getCategory();
            categoryValue.setText(product.getCategory().getName());
        } else {
            this.selectedCategory = null;
            categoryValue.setText(getString(R.string.choose_category));
        }
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
        boolean editMode = productId != null && !productId.isBlank();

        if (name.isEmpty()) {
            nameInput.setError(getString(R.string.enter_product_name));
            return;
        }
        if (selectedCategory == null) {
            Toast.makeText(this, this.getString(R.string.toast_vui_long_chon_danh_muc), Toast.LENGTH_SHORT).show();
            return;
        }
        String categoryId = selectedCategory.get_id();
        if (stock.isEmpty()) {
            stockInput.setError(getString(R.string.enter_stock));
            return;
        }
        if (price.isEmpty() || "0".equals(price)) {
            priceInput.setError(getString(R.string.price_must_be_positive));
            return;
        }
        if (!editMode && selectedImageUri == null) {
            Toast.makeText(this, this.getString(R.string.toast_vui_long_chon_hinh_anh_san_pham), Toast.LENGTH_SHORT).show();
            return;
        }

        MultipartBody.Part imagePart;
        try {
            imagePart = selectedImageUri == null ? null : createImagePart(selectedImageUri);
        } catch (IOException exception) {
            Toast.makeText(this, this.getString(R.string.toast_khong_the_doc_anh_da_chon), Toast.LENGTH_SHORT).show();
            return;
        }

        ensureDefaultColor();
        String colorsJson = new Gson().toJson(productColors);
        int currentSold = currentProduct != null ? currentProduct.getSold() : 0;
        setLoading(true);
        HttpResquest request = new HttpResquest();
        String token = HttpResquest.authorizationHeader(this);
        currentCall = editMode
                ? request.callAPI().updateProduct(token, productId, text(name), text(price), text(categoryId),
                text(description), text(stock), text(String.valueOf(currentSold)), text(String.valueOf(originalIsActive)),
                text(colorsJson), imagePart)
                : request.callAPI().addProduct(token, text(name), text(price), text(categoryId),
                text(description), text(stock), text("0"), text("true"), text(colorsJson), imagePart);
        currentCall.enqueue(new Callback<Response<Product>>() {
            @Override
            public void onResponse(@NonNull Call<Response<Product>> call, @NonNull retrofit2.Response<Response<Product>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(ProductFormManagementActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    // Xóa cache sản phẩm (cả admin & user) sau khi thêm/sửa thành công
                    DataCache.get().invalidate("admin_products");
                    DataCache.get().invalidate("user_products");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(ProductFormManagementActivity.this, ProductFormManagementActivity.this.getString(R.string.toast_khong_the_luu_san_pham), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<Product>> call, @NonNull Throwable throwable) {
                if (call.isCanceled()) return;
                setLoading(false);
                Toast.makeText(ProductFormManagementActivity.this, ProductFormManagementActivity.this.getString(R.string.toast_loi_ket_noi_server), Toast.LENGTH_SHORT).show();
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
        colorNameInput.setFocusable(true);
        colorNameInput.setFocusableInTouchMode(true);
        hexInput.setFocusable(false);
        setColor(preview, editing ? parseColorSafely(color.getHex()) : Color.LTGRAY);
        if (editing) bindColorDialog(color, colorNameInput, hexInput, defaultCheck);
        addColorPalette(colorNameInput, hexInput, preview, dialogView.findViewById(R.id.adminColorPalette));
        customColorPicker.setOnClickListener(v -> new com.skydoves.colorpickerview.ColorPickerDialog.Builder(this)
                .setTitle(getString(R.string.choose_color))
                .setPositiveButton(getString(R.string.select), (com.skydoves.colorpickerview.listeners.ColorEnvelopeListener) (envelope, fromUser) -> {
                    colorNameInput.setText(getClosestColorName(envelope.getColor()));
                    hexInput.setText(String.format("#%06X", 0xFFFFFF & envelope.getColor()));
                    setColor(preview, envelope.getColor());
                })
                .setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss())
                .attachAlphaSlideBar(false)
                .attachBrightnessSlideBar(true)
                .setBottomSpace(12)
                .show());

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(editing ? getString(R.string.edit_color_title) : getString(R.string.add_color_variant_title))
                .setView(dialogView)
                .setPositiveButton(editing ? getString(R.string.save) : getString(R.string.add), (dialog, which) ->
                        saveColor(color, colorNameInput, hexInput, defaultCheck, editing))
                .setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss());
        if (editing) {
            builder.setNeutralButton(getString(R.string.delete_color), (dialog, which) -> {
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
        for (String[] sample : getColorPalette()) {
            View item = LayoutInflater.from(this).inflate(
                    R.layout.item_admin_color_palette, colors, false);
            View swatch = item.findViewById(R.id.ivAdminPaletteSwatch);
            TextView name = item.findViewById(R.id.tvAdminPaletteColorName);
            setColor(swatch, Color.parseColor(sample[1]));
            name.setText(sample[0]);
            item.setContentDescription(String.format(getString(R.string.color_prefix), sample[0]));
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
            Toast.makeText(this, this.getString(R.string.toast_ten_va_ma_mau_khong_duoc_bo_trong), Toast.LENGTH_SHORT).show();
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
        submitButton.setText(loading ? getString(R.string.saving_ellipsis) :
                (productId == null || productId.isBlank() ? getString(R.string.add) : getString(R.string.save_product)));
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private String getClosestColorName(int color) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        
        String closestName = getString(R.string.custom_color);
        double minDistance = Double.MAX_VALUE;
        
        Object[][] namedColors = {
            {getString(R.string.color_black), 0, 0, 0},
            {getString(R.string.color_white), 255, 255, 255},
            {getString(R.string.color_gray), 128, 128, 128},
            {getString(R.string.color_red), 255, 0, 0},
            {getString(R.string.color_green), 0, 255, 0},
            {getString(R.string.color_blue), 0, 0, 255},
            {getString(R.string.color_yellow), 255, 255, 0},
            {getString(R.string.color_orange), 255, 165, 0},
            {getString(R.string.color_purple), 128, 0, 128},
            {getString(R.string.color_pink), 255, 192, 203},
            {getString(R.string.color_brown), 165, 42, 42},
            {getString(R.string.color_cyan), 0, 206, 209},
            {getString(R.string.color_olive), 85, 107, 47}
        };
        
        for (Object[] nc : namedColors) {
            String name = (String) nc[0];
            int nr = (int) nc[1];
            int ng = (int) nc[2];
            int nb = (int) nc[3];
            
            double distance = Math.sqrt(Math.pow(r - nr, 2) + Math.pow(g - ng, 2) + Math.pow(b - nb, 2));
            if (distance < minDistance) {
                minDistance = distance;
                closestName = name;
            }
        }
        return closestName;
    }

    @Override
    protected void onDestroy() {
        if (currentCall != null) currentCall.cancel();
        super.onDestroy();
    }
}
