package info.groodt;

import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixObservableCommand;
import rx.Observable;

import java.util.List;
import java.util.Map;

public class BatchObservableCommand extends HystrixObservableCommand<Map<String, String>> {

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
    protected Observable<Map<String, String>> construct() {
        return Observable.just(cache.getValues(keys));
    }
}
