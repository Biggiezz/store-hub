package com.nguyenmanhphuc.storehubapp.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * In-memory cache với timeout tự động 5 phút.
 *
 * Cách dùng:
 *   DataCache.put("products_page_1", data);          // Lưu cache
 *   DataCache.get("products_page_1", ArrayList.class) // Lấy cache (null nếu hết hạn)
 *   DataCache.invalidate("products");                 // Xóa tất cả key chứa "products"
 *   DataCache.invalidateExact("products_page_1");     // Xóa đúng key đó
 */
public class DataCache {

    /** Thời gian cache tối đa: 5 phút */
    private static final long TTL_MS = 5 * 60 * 1000L;

    private static final DataCache INSTANCE = new DataCache();

    private final Map<String, CacheEntry> store = new HashMap<>();

    private DataCache() {}

    public static DataCache get() {
        return INSTANCE;
    }

    // ─── Public API ───────────────────────────────────────────────────────────

    /** Lưu một giá trị vào cache với key cho trước (TTL mặc định 5 phút) */
    public synchronized <T> void put(String key, T value) {
        store.put(key, new CacheEntry(value, System.currentTimeMillis(), TTL_MS));
    }

    /** Lưu với TTL tùy chỉnh (tính bằng milliseconds) */
    public synchronized <T> void put(String key, T value, long ttlMs) {
        store.put(key, new CacheEntry(value, System.currentTimeMillis(), ttlMs));
    }

    /**
     * Lấy dữ liệu từ cache.
     * Trả về null nếu key không tồn tại hoặc đã hết hạn (> 5 phút).
     */
    @SuppressWarnings("unchecked")
    public synchronized <T> T get(String key, Class<T> type) {
        CacheEntry entry = store.get(key);
        if (entry == null) return null;
        if (System.currentTimeMillis() - entry.timestamp > entry.ttlMs) {
            store.remove(key);
            return null;
        }
        try {
            return type.cast(entry.value);
        } catch (ClassCastException e) {
            store.remove(key);
            return null;
        }
    }

    /**
     * Xóa tất cả cache có key chứa prefix.
     * Ví dụ: invalidate("products") sẽ xóa "products_page_1", "products_page_2", ...
     */
    public synchronized void invalidate(String prefix) {
        store.entrySet().removeIf(e -> e.getKey().startsWith(prefix));
    }

    /** Xóa đúng một key cụ thể */
    public synchronized void invalidateExact(String key) {
        store.remove(key);
    }

    /** Xóa toàn bộ cache */
    public synchronized void clear() {
        store.clear();
    }

    /** Kiểm tra xem cache key có còn hợp lệ không */
    public synchronized boolean isValid(String key) {
        CacheEntry entry = store.get(key);
        if (entry == null) return false;
        if (System.currentTimeMillis() - entry.timestamp > entry.ttlMs) {
            store.remove(key);
            return false;
        }
        return true;
    }

    // ─── Internal ─────────────────────────────────────────────────────────────

    private static class CacheEntry {
        final Object value;
        final long timestamp;
        final long ttlMs;

        CacheEntry(Object value, long timestamp, long ttlMs) {
            this.value = value;
            this.timestamp = timestamp;
            this.ttlMs = ttlMs;
        }
    }
}
