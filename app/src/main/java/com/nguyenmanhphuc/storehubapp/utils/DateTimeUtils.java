package com.nguyenmanhphuc.storehubapp.utils;

import android.app.DatePickerDialog;
import android.content.Context;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateTimeUtils {

    public static final String ISO_FORMAT_MS = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static final String ISO_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    /**
     * Parses ISO UTC date string and formats it to GMT+7 timezone.
     */
    public static String formatISOToVN(String isoString, String outputPattern) {
        if (isoString == null || isoString.isEmpty()) return "";
        try {
            SimpleDateFormat parser = new SimpleDateFormat(ISO_FORMAT_MS, Locale.US);
            parser.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date;
            try {
                date = parser.parse(isoString);
            } catch (Exception e) {
                // Try format without milliseconds
                SimpleDateFormat parserAlt = new SimpleDateFormat(ISO_FORMAT, Locale.US);
                parserAlt.setTimeZone(TimeZone.getTimeZone("UTC"));
                date = parserAlt.parse(isoString);
            }

            if (date == null) return isoString;

            SimpleDateFormat formatter = new SimpleDateFormat(outputPattern, Locale.US);
            formatter.setTimeZone(TimeZone.getTimeZone("GMT+7"));
            return formatter.format(date);
        } catch (Exception e) {
            return fallbackParse(isoString, outputPattern);
        }
    }

    /**
     * Parses ISO UTC date string and formats it to default local timezone.
     */
    public static String formatISOToLocal(String isoString, String outputPattern) {
        if (isoString == null || isoString.isEmpty()) return "";
        try {
            SimpleDateFormat parser = new SimpleDateFormat(ISO_FORMAT_MS, Locale.getDefault());
            parser.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date;
            try {
                date = parser.parse(isoString);
            } catch (Exception e) {
                SimpleDateFormat parserAlt = new SimpleDateFormat(ISO_FORMAT, Locale.getDefault());
                parserAlt.setTimeZone(TimeZone.getTimeZone("UTC"));
                date = parserAlt.parse(isoString);
            }

            if (date == null) return isoString;

            SimpleDateFormat formatter = new SimpleDateFormat(outputPattern, Locale.getDefault());
            return formatter.format(date);
        } catch (Exception e) {
            return fallbackParse(isoString, outputPattern);
        }
    }

    /**
     * Calculates delivery date +5 days later and formats it in VN Locale.
     */
    public static String calculateVNEstimatedDelivery(String createdAtString) {
        if (createdAtString == null || createdAtString.isEmpty()) {
            return "Sau 5 ngày";
        }
        try {
            SimpleDateFormat parser = new SimpleDateFormat(ISO_FORMAT_MS, Locale.US);
            parser.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = parser.parse(createdAtString);
            if (date == null) return "Sau 5 ngày";

            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.DAY_OF_YEAR, 5);
            Date estimatedDate = cal.getTime();

            SimpleDateFormat formatter = new SimpleDateFormat("dd 'Tháng' MM, yyyy", Locale.US);
            formatter.setTimeZone(TimeZone.getTimeZone("GMT+7"));
            return formatter.format(estimatedDate);
        } catch (Exception e) {
            return "Sau 5 ngày";
        }
    }

    private static String fallbackParse(String isoString, String outputPattern) {
        try {
            String datePart = isoString.split("T")[0];
            String timePart = isoString.split("T")[1].substring(0, 5);
            String[] dateSplit = datePart.split("-");
            
            if (outputPattern.contains("Tháng")) {
                return dateSplit[2] + " Tháng " + dateSplit[1] + ", " + dateSplit[0];
            } else if (outputPattern.contains("•")) {
                return timePart + "  •  " + dateSplit[2] + "/" + dateSplit[1] + "/" + dateSplit[0];
            } else if (outputPattern.contains(",")) {
                return timePart + ", " + dateSplit[2] + "/" + dateSplit[1] + "/" + dateSplit[0];
            } else {
                return dateSplit[2] + "/" + dateSplit[1] + "/" + dateSplit[0] + " " + timePart;
            }
        } catch (Exception e) {
            return isoString;
        }
    }
    public static Date parseISO(String isoString) {
        if (isoString == null || isoString.isEmpty()) return null;
        String[] patterns = {
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ssXXX",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd"
        };
        for (String pattern : patterns) {
            try {
                SimpleDateFormat parser = new SimpleDateFormat(pattern, Locale.US);
                parser.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date d = parser.parse(isoString);
                if (d != null) return d;
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    public static String formatToISO(Date date) {
        if (date == null) return "";
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(ISO_FORMAT_MS, Locale.US);
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            return formatter.format(date);
        } catch (Exception e) {
            return "";
        }
    }

    public static String getRelativeTime(String isoString) {
        return getRelativeTime(isoString, false);
    }

    public static String getRelativeTime(String isoString, boolean isOnline) {
        if (isoString == null || isoString.isEmpty()) {
            return "Chưa hoạt động";
        }
        if (isoString.equals("Vừa xong")) {
            return "Vừa xong";
        }
        Date date = parseISO(isoString);
        if (date == null) {
            return isoString;
        }
        long now = System.currentTimeMillis();
        long time = date.getTime();
        long diff = now - time;

        // Trạng thái online: hiển thị "Đang hoạt động" nếu isOnline=true và hoạt động gần đây dưới 5 phút
        if (isOnline && diff >= 0 && diff < 5 * 60 * 1000) {
            return "Đang hoạt động";
        }

        if (diff < 0) {
            return "Đang hoạt động";
        }
        long diffSeconds = diff / 1000;
        long diffMinutes = diffSeconds / 60;
        long diffHours = diffMinutes / 60;
        long diffDays = diffHours / 24;

        if (diffSeconds < 60) {
            return "Vừa xong";
        } else if (diffMinutes < 60) {
            return diffMinutes + " phút trước";
        } else if (diffHours < 24) {
            return diffHours + " giờ trước";
        } else if (diffDays < 7) {
            return diffDays + " ngày trước";
        } else {
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
            formatter.setTimeZone(TimeZone.getTimeZone("GMT+7"));
            return formatter.format(date);
        }
    }

    public static void showDatePicker(Context context, final TextView targetView) {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(
                context,
                (view, year, month, dayOfMonth) -> {
                    String selectedDate = String.format(
                            Locale.getDefault(),
                            "%02d/%02d/%04d",
                            dayOfMonth,
                            month + 1,
                            year
                    );
                    targetView.setText(selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }
}
