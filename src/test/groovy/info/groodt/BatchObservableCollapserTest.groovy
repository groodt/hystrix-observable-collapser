package info.groodt

import com.netflix.hystrix.Hystrix
import spock.lang.Specification

class BatchObservableCollapserTest extends Specification {

    Cache cache
    org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BatchObservableCollapserTest.class);

    def setup() {
        cache = Mock(Cache)
        Hystrix.reset()
    }

    def "collapses multiple requests into single request to cache"() {
        given:
            def cacheKeys = ["key1", "key2", "key3"]
            def cacheResponse = [
                    "key1": "value1",
                    "key2": null,
                    "key3": "value3"
            ]

            def collapser1 = new BatchObservableCollapser(cacheKeys[0], cache).toObservable()
            def collapser2 = new BatchObservableCollapser(cacheKeys[1], cache).toObservable()
            def collapser3 = new BatchObservableCollapser(cacheKeys[2], cache).toObservable()
        when:
            collapser1.toBlocking().toFuture().get()
            collapser2.toBlocking().toFuture().get()
            collapser3.toBlocking().toFuture().get()
        then:
            1 * cache.getValues(cacheKeys) >> {
                log.info("Fetching keys: {}", cacheKeys)
                cacheResponse
            }
    }
}
