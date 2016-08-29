package info.groodt;

import com.netflix.hystrix.HystrixCollapser;
import com.netflix.hystrix.HystrixCollapserKey;
import com.netflix.hystrix.HystrixObservableCollapser;
import com.netflix.hystrix.HystrixObservableCommand;
import lombok.extern.slf4j.Slf4j;
import rx.functions.Func1;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class BatchObservableCollapser extends HystrixObservableCollapser<String, CacheResponse, CacheResponse, String> {

    private final String key;
    private final Cache cache;

    public BatchObservableCollapser(final String key, final Cache cache) {
        super(Setter.withCollapserKey(HystrixCollapserKey.Factory.asKey("BatchObservableCollapser"))
                .andScope(Scope.GLOBAL));
        this.key = key;
        this.cache = cache;
    }

    @Override
    public String getRequestArgument() {
        return key;
    }

    @Override
    protected HystrixObservableCommand<CacheResponse> createCommand(Collection<HystrixCollapser.CollapsedRequest<CacheResponse, String>> collapsedRequests) {
        List<String> keys = collapsedRequests.stream().map(HystrixCollapser.CollapsedRequest::getArgument).collect(Collectors.toList());
        log.info("Creating command with keys: {}", keys);
        return new BatchObservableCommand(keys, cache);
    }

    @Override
    protected Func1<CacheResponse, String> getBatchReturnTypeKeySelector() {
        return CacheResponse::getKey;
    }

    @Override
    protected Func1<String, String> getRequestArgumentKeySelector() {
        return (arg) -> arg;
    }

    @Override
    protected Func1<CacheResponse, CacheResponse> getBatchReturnTypeToResponseTypeMapper() {
        return (batch) -> batch;
    }

    @Override
    protected void onMissingResponse(HystrixCollapser.CollapsedRequest<CacheResponse, String> r) {
        throw new RuntimeException("Missing Response for request");
    }
}
