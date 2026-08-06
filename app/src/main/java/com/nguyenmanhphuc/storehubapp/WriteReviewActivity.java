package com.nguyenmanhphuc.storehubapp;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.nguyenmanhphuc.storehubapp.adapter.SelectedMediaAdapter;
import com.nguyenmanhphuc.storehubapp.model.CartItem;
import com.nguyenmanhphuc.storehubapp.model.Order;
import com.nguyenmanhphuc.storehubapp.model.Product;
import com.nguyenmanhphuc.storehubapp.model.User;
import com.nguyenmanhphuc.storehubapp.services.ApiServices;
import com.nguyenmanhphuc.storehubapp.services.HttpResquest;
import com.nguyenmanhphuc.storehubapp.utils.SharedPreferencesManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WriteReviewActivity extends BaseActivity {

    private ImageView btnBack;
    private ShapeableImageView ivReviewProductImage;
    private TextView tvReviewProductName, tvReviewProductVariant;
    private final ImageView[] starViews = new ImageView[5];
    private EditText edtReviewContent;
    private View btnUploadImage;
    private RecyclerView rvSelectedMedia;
    private MaterialButton btnSubmitReview;

    private int selectedRating = 5; // Mặc định 5 sao
    private ApiServices apiServices;
    private SelectedMediaAdapter mediaAdapter;
    private final ArrayList<Uri> selectedUris = new ArrayList<>();

    // Khởi tạo Activity Result Launcher để chọn tối đa 5 file ảnh hoặc video
    private final ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMedia =
            registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(5), uris -> {
                if (uris != null && !uris.isEmpty()) {
                    for (Uri uri : uris) {
                        if (selectedUris.size() >= 5) {
                            Toast.makeText(this, "Chỉ được chọn tối đa 5 hình ảnh hoặc video!", Toast.LENGTH_SHORT).show();
                            break;
                        }
                        // Kiểm tra nếu là video và dung lượng vượt quá 30MB
                        if (isVideo(uri)) {
                            long size = getFileSize(uri);
                            if (size > 30 * 1024 * 1024) { // 30 MB
                                Toast.makeText(this, "Dung lượng video vượt quá 30MB, vui lòng chọn video ngắn hơn!", Toast.LENGTH_LONG).show();
                                continue;
                            }
                        }
                        if (!selectedUris.contains(uri)) {
                            selectedUris.add(uri);
                        }
                    }
                    mediaAdapter.notifyDataSetChanged();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_review);

        apiServices = new HttpResquest().callAPI();
        initUi();

        btnBack.setOnClickListener(v -> finish());

        Order order = (Order) getIntent().getSerializableExtra("order_item");
        if (order != null) {
            tvReviewProductName.setText(order.getProductName());
            tvReviewProductVariant.setText(order.getProductVariant());
            Glide.with(this)
                    .load(order.getProductImage())
                    .placeholder(R.drawable.ic_product)
                    .error(R.drawable.ic_product)
                    .into(ivReviewProductImage);
        }

        setupStarRating();

        btnUploadImage.setOnClickListener(v -> {
            if (selectedUris.size() >= 5) {
                Toast.makeText(this, "Đã chọn tối đa 5 tệp tin!", Toast.LENGTH_SHORT).show();
                return;
            }
            // Mở thư viện chọn cả ảnh và video
            pickMultipleMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageAndVideo.INSTANCE)
                    .build());
        });

        btnSubmitReview.setOnClickListener(v -> {
            String content = edtReviewContent.getText().toString().trim();
            if (content.isEmpty()) {
                Toast.makeText(this, getString(R.string.toast_enter_comment), Toast.LENGTH_SHORT).show();
                return;
            }
            if (order == null || order.getItems() == null || order.getItems().isEmpty()) {
                Toast.makeText(this, getString(R.string.toast_no_product_info), Toast.LENGTH_SHORT).show();
                return;
            }

            // Lấy productId từ CartItem đầu tiên
            CartItem firstItem = order.getItems().get(0);
            String productId = firstItem.getProductId();
            if (productId.isEmpty()) {
                Toast.makeText(this, getString(R.string.toast_no_product_id), Toast.LENGTH_SHORT).show();
                return;
            }

            String customerName = getString(R.string.role_customer);
            String customerImage = "";
            User user = SharedPreferencesManager.getInstance(this).getUser();
            if (user != null) {
                if (user.getName() != null && !user.getName().isEmpty()) {
                    customerName = user.getName();
                }
                if (user.getImage() != null && !user.getImage().isEmpty()) {
                    customerImage = user.getImage();
                }
            }

            // Đóng gói MultipartBody
            RequestBody productIdBody = RequestBody.create(MediaType.parse("text/plain"), productId);
            RequestBody customerNameBody = RequestBody.create(MediaType.parse("text/plain"), customerName);
            RequestBody customerImageBody = RequestBody.create(MediaType.parse("text/plain"), customerImage);
            RequestBody ratingBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(selectedRating));
            RequestBody contentBody = RequestBody.create(MediaType.parse("text/plain"), content);
            RequestBody orderIdBody = RequestBody.create(MediaType.parse("text/plain"), order.getOrderId());

            List<MultipartBody.Part> mediaParts = new ArrayList<>();
            for (Uri uri : selectedUris) {
                MultipartBody.Part part = prepareFilePart("media", uri);
                if (part != null) {
                    mediaParts.add(part);
                }
            }

            btnSubmitReview.setEnabled(false);
            btnSubmitReview.setText("Đang gửi đánh giá...");

            apiServices.addReview(productIdBody, customerNameBody, customerImageBody, ratingBody, contentBody, orderIdBody, mediaParts)
                    .enqueue(new Callback<com.nguyenmanhphuc.storehubapp.model.response.Response<Product>>() {
                @Override
                public void onResponse(@NonNull Call<com.nguyenmanhphuc.storehubapp.model.response.Response<Product>> call, @NonNull Response<com.nguyenmanhphuc.storehubapp.model.response.Response<Product>> response) {
                    btnSubmitReview.setEnabled(true);
                    btnSubmitReview.setText("Gửi đánh giá");
                    if (response.isSuccessful() && response.body() != null && response.body().getCode() == 200) {
                        Toast.makeText(WriteReviewActivity.this, getString(R.string.toast_submit_success), Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        String errMsg = (response.body() != null ? response.body().getMessage() : getString(R.string.toast_server_error));
                        Toast.makeText(WriteReviewActivity.this, String.format(getString(R.string.toast_submit_failed_prefix), errMsg), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<com.nguyenmanhphuc.storehubapp.model.response.Response<Product>> call, @NonNull Throwable t) {
                    btnSubmitReview.setEnabled(true);
                    btnSubmitReview.setText("Gửi đánh giá");
                    Toast.makeText(WriteReviewActivity.this, String.format(getString(R.string.toast_connection_error_prefix), t.getMessage()), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void initUi() {
        btnBack = findViewById(R.id.btnBack);
        ivReviewProductImage = findViewById(R.id.ivReviewProductImage);
        tvReviewProductName = findViewById(R.id.tvReviewProductName);
        tvReviewProductVariant = findViewById(R.id.tvReviewProductVariant);

        starViews[0] = findViewById(R.id.ivStar1);
        starViews[1] = findViewById(R.id.ivStar2);
        starViews[2] = findViewById(R.id.ivStar3);
        starViews[3] = findViewById(R.id.ivStar4);
        starViews[4] = findViewById(R.id.ivStar5);

        edtReviewContent = findViewById(R.id.edtReviewContent);
        btnUploadImage = findViewById(R.id.btnUploadImage);
        rvSelectedMedia = findViewById(R.id.rvSelectedMedia);
        btnSubmitReview = findViewById(R.id.btnSubmitReview);

        // Thiết lập RecyclerView chọn media
        rvSelectedMedia.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mediaAdapter = new SelectedMediaAdapter(this, selectedUris, position -> {
            selectedUris.remove(position);
            mediaAdapter.notifyDataSetChanged();
        });
        rvSelectedMedia.setAdapter(mediaAdapter);
    }

    private void setupStarRating() {
        for (int i = 0; i < starViews.length; i++) {
            final int starIndex = i + 1;
            starViews[i].setOnClickListener(v -> setRating(starIndex));
        }
        setRating(5);
    }

    private void setRating(int rating) {
        this.selectedRating = rating;
        for (int i = 0; i < 5; i++) {
            if (i < rating) {
                starViews[i].setImageResource(R.drawable.ic_star_fill);
            } else {
                starViews[i].setImageResource(R.drawable.ic_star_empty);
            }
        }
    }

    // Helper kiểm tra file là video
    private boolean isVideo(Uri uri) {
        String type = getContentResolver().getType(uri);
        if (type != null && type.startsWith("video")) {
            return true;
        }
        String path = uri.toString().toLowerCase();
        return path.endsWith(".mp4") || path.endsWith(".3gp") || path.endsWith(".mkv") || path.endsWith(".webm");
    }

    // Helper lấy dung lượng file từ Uri
    private long getFileSize(Uri uri) {
        try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE);
                if (sizeIndex != -1) {
                    return cursor.getLong(sizeIndex);
                }
            }
        } catch (Exception e) {
            Log.e("WriteReviewActivity", "Lỗi lấy dung lượng tệp", e);
        }
        return 0;
    }

    // Đóng gói Uri sang MultipartBody.Part
    private MultipartBody.Part prepareFilePart(String partName, Uri fileUri) {
        try {
            String displayName = "file";
            try (android.database.Cursor cursor = getContentResolver().query(fileUri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        displayName = cursor.getString(nameIndex);
                    }
                }
            }

            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            if (inputStream == null) return null;

            byte[] bytes = getBytes(inputStream);
            String mimeType = getContentResolver().getType(fileUri);
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }

            RequestBody requestFile = RequestBody.create(
                    MediaType.parse(mimeType),
                    bytes
            );

            return MultipartBody.Part.createFormData(partName, displayName, requestFile);
        } catch (Exception e) {
            Log.e("WriteReviewActivity", "Lỗi tạo Multipart file", e);
            return null;
        }
    }

    private byte[] getBytes(InputStream inputStream) throws Exception {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }
}
