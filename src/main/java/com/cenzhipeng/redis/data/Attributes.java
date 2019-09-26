package com.cenzhipeng.redis.data;

import io.netty.util.AttributeKey;

public interface Attributes {
    AttributeKey<DataProvider> DATA = AttributeKey.newInstance("data");
}
