package com.nguyenmanhphuc.storehubapp.utils;

import android.text.TextUtils;
import android.util.Log;
import com.nguyenmanhphuc.storehubapp.services.HttpResquest;

public class ImageUtils {

    /**
     * Tự động sửa lỗi đường dẫn ảnh (Địa chỉ IP, Port và Localhost) cho khớp với môi trường chạy thực tế của máy ảo Android.
     */
    public static String getCorrectedImageUrl(String originalUrl, String baseUrl) {
        if (TextUtils.isEmpty(originalUrl)) {
            return "";
        }
        
        String effectiveBaseUrl = baseUrl;
        if (TextUtils.isEmpty(effectiveBaseUrl)) {
            effectiveBaseUrl = HttpResquest.BASE_URL; // Fallback mặc định
        }

        // Trường hợp 1: Nếu là đường dẫn tương đối (bắt đầu bằng "/" hoặc "uploads/")
        if (originalUrl.startsWith("/") || originalUrl.startsWith("uploads/")) {
            String cleanBase = effectiveBaseUrl.endsWith("/") ? effectiveBaseUrl.substring(0, effectiveBaseUrl.length() - 1) : effectiveBaseUrl;
            String cleanPath = originalUrl.startsWith("/") ? originalUrl : "/" + originalUrl;
            String result = cleanBase + cleanPath;
            Log.d("ImageUtils", "Relative path: " + originalUrl + " -> " + result);
            return result;
        }

        // Trường hợp 2: Thay thế localhost / 127.0.0.1 thành 10.0.2.2 để máy ảo truy cập được máy thật
        String correctedUrl = originalUrl.replace("localhost", "10.0.2.2").replace("127.0.0.1", "10.0.2.2");

        // Trường hợp 3: Trích xuất Port từ effectiveBaseUrl
        String currentPort = "";
        try {
            java.util.regex.Pattern portPattern = java.util.regex.Pattern.compile(":(\\d+)/?");
            java.util.regex.Matcher matcher = portPattern.matcher(effectiveBaseUrl);
            if (matcher.find()) {
                currentPort = matcher.group(1);
            }
        } catch (Exception ignored) {}

        // Trường hợp 4: Đồng bộ hóa Port của ảnh theo Port của API
        if (currentPort != null && !currentPort.isEmpty()) {
            correctedUrl = correctedUrl.replaceAll(":(3000|5000|8080|8081)", ":" + currentPort);
        }

        // Trường hợp 5: Xử lý địa chỉ IP cụ thể (ví dụ: 192.168.x.x)
        if (effectiveBaseUrl.contains("192.168.") || effectiveBaseUrl.contains("172.") || effectiveBaseUrl.contains("10.")) {
            java.util.regex.Pattern ipPattern = java.util.regex.Pattern.compile("(\\d+\\.\\d+\\.\\d+\\.\\d+)");
            java.util.regex.Matcher matcher = ipPattern.matcher(effectiveBaseUrl);
            if (matcher.find()) {
                String currentIp = matcher.group(1);
                if (currentIp != null && !currentIp.equals("127.0.0.1")) {
                    correctedUrl = correctedUrl.replace("10.0.2.2", currentIp);
                }
            }
        }
        
        // Trường hợp 6: Nếu BASE_URL là máy ảo (10.0.2.2), ép tất cả IP local về 10.0.2.2
        if (effectiveBaseUrl.contains("10.0.2.2")) {
            correctedUrl = correctedUrl.replaceAll("192\\.168\\.\\d+\\.\\d+", "10.0.2.2")
                                     .replaceAll("172\\.\\d+\\.\\d+\\.\\d+", "10.0.2.2")
                                     .replaceAll("localhost", "10.0.2.2");
        }

        // Trường hợp 7: Domain thực tế
        if (effectiveBaseUrl.contains(".onrender.com") || effectiveBaseUrl.contains(".vercel.app")) {
            String cleanBase = effectiveBaseUrl.endsWith("/") ? effectiveBaseUrl.substring(0, effectiveBaseUrl.length() - 1) : effectiveBaseUrl;
            if (correctedUrl.contains("10.0.2.2") || correctedUrl.contains("192.168.") || correctedUrl.contains("localhost")) {
                int uploadIndex = correctedUrl.indexOf("/uploads/");
                if (uploadIndex != -1) {
                    correctedUrl = cleanBase + correctedUrl.substring(uploadIndex);
                }
            }
        }

        // Kiểm tra cuối cùng: Nếu vẫn không có giao thức http, bổ sung baseUrl
        if (!correctedUrl.startsWith("http") && !TextUtils.isEmpty(correctedUrl)) {
            String cleanBase = effectiveBaseUrl.endsWith("/") ? effectiveBaseUrl.substring(0, effectiveBaseUrl.length() - 1) : effectiveBaseUrl;
            String cleanPath = correctedUrl.startsWith("/") ? correctedUrl : "/" + correctedUrl;
            correctedUrl = cleanBase + cleanPath;
        }

        Log.d("ImageUtils", "Final URL: " + correctedUrl);
        return correctedUrl;
    }
}
