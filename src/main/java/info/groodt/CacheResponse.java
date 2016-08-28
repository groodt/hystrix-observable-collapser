package info.groodt;

import lombok.Data;

@Data
public class CacheResponse {
    private final String key;
    private final String value;
}
