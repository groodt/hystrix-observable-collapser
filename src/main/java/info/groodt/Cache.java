package info.groodt;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
public class Cache {

    public Map<String, String> getValues(final List<String> keys) {
        log.info("Fetching keys: {}", keys);

        ImmutableMap.Builder<String, String> mapBuilder = ImmutableMap.builder();
        keys.forEach(k -> mapBuilder.put(k, k));
        return mapBuilder.build();
    }
}
