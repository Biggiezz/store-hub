package com.example.storehub.utils;

import android.text.TextUtils;

public class ImageUtils {

    /**
     * Tự động sửa lỗi đường dẫn ảnh (Địa chỉ IP, Port và Localhost) cho khớp với môi trường chạy thực tế của máy ảo Android.
     *
     * @param originalUrl Đường dẫn ảnh gốc trong Database (ví dụ: "/uploads/abc.jpg" hoặc "http://localhost:3000/uploads/abc.jpg")
     * @param baseUrl     Địa chỉ gốc API hiện tại (ví dụ: "http://10.0.2.2:5000/")
     * @return Đường dẫn ảnh đã được chuẩn hóa để hiển thị thành công 100% trên máy ảo
     */
    public static String getCorrectedImageUrl(String originalUrl, String baseUrl) {
        if (TextUtils.isEmpty(originalUrl)) {
            return "";
        }
        if (TextUtils.isEmpty(baseUrl)) {
            baseUrl = "http://10.0.2.2:5000/"; // Fallback mặc định
        }

        // Trường hợp 1: Đường dẫn tương đối bắt đầu bằng "/"
        if (originalUrl.startsWith("/")) {
            String cleanBase = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
            return cleanBase + originalUrl;
        }

        // Trường hợp 2: Thay thế localhost / 127.0.0.1 thành 10.0.2.2 để máy ảo truy cập được máy thật
        String correctedUrl = originalUrl.replace("localhost", "10.0.2.2").replace("127.0.0.1", "10.0.2.2");

        // Trường hợp 3: Tự động đồng bộ hóa Port theo BASE_URL của máy đang chạy hiện tại
        try {
            String currentPort = "";
            if (baseUrl.contains("10.0.2.2:")) {
                int start = baseUrl.indexOf("10.0.2.2:") + 9;
                int end = baseUrl.indexOf("/", start);
                if (end == -1) end = baseUrl.length();
                currentPort = baseUrl.substring(start, end).trim();
            } else if (baseUrl.contains("localhost:")) {
                int start = baseUrl.indexOf("localhost:") + 10;
                int end = baseUrl.indexOf("/", start);
                if (end == -1) end = baseUrl.length();
                currentPort = baseUrl.substring(start, end).trim();
            }

            if (!currentPort.isEmpty() && correctedUrl.contains("10.0.2.2:")) {
                int start = correctedUrl.indexOf("10.0.2.2:") + 9;
                int end = correctedUrl.indexOf("/", start);
                if (end == -1) end = correctedUrl.length();

                String oldPort = correctedUrl.substring(start, end);
                correctedUrl = correctedUrl.replace("10.0.2.2:" + oldPort, "10.0.2.2:" + currentPort);
            }
        } catch (Exception ignored) {}

        return correctedUrl;
    }
}
