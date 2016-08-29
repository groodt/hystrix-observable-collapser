package info.groodt;

import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixObservableCommand;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class BatchObservableCommand extends HystrixObservableCommand<CacheResponse> {

    private final List<String> keys;
    private final Cache cache;

    public BatchObservableCommand(final List<String> keys, final Cache cache) {
        super(Setter
                .withGroupKey(HystrixCommandGroupKey.Factory.asKey("BatchObservableCommand"))
                .andCommandKey(HystrixCommandKey.Factory.asKey("BatchObservableCommand")));
        this.keys = keys;
        this.cache = cache;
    }

    @Override
    protected Observable<CacheResponse> construct() {
        Set<String> uniqueKeys = new HashSet<>(keys);
        Map<String, String> values = cache.getValues(uniqueKeys);
        log.info("Values from cache: {}", values);
        return Observable.from(uniqueKeys).map(k -> {
            CacheResponse cacheResponse = new CacheResponse(k, values.get(k));
            log.info("Emitting response: {}", cacheResponse);
            return cacheResponse;
        });
    }
}
