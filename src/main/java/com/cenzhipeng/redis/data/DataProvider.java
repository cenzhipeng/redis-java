package com.cenzhipeng.redis.data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * all the server data is here
 *
 */
public class DataProvider {
    private Map<String, KeyType> keyTypeMap = new ConcurrentHashMap<>();
    private Map<String, String> stringMap = new ConcurrentHashMap<>();

    /**
     * set a key value
     * todo, complete the setType and expire time
     * @param k
     * @param v
     */
    public void set(String k, String v) {
        stringMap.put(k, v);
    }

    /**
     * get a value from a key
     * @param k
     * @return
     */
    public String get(String k) {
        return stringMap.get(k);
    }

}
