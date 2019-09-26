package com.cenzhipeng.redis.data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataProvider {
    private Map<String, KeyType> keyTypeMap = new ConcurrentHashMap<>();
    private Map<String, String> stringMap = new ConcurrentHashMap<>();

    void set(String k, String v) {
        stringMap.put(k, v);
    }

    String get(String k) {
        return stringMap.get(k);
    }

}
