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
            def cacheKeys = ["key1", "key2", "key3"].toSet()
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

    def "correctly maps collapsed response to the individual requests"() {
        given:
            def cacheKeys = ["key1", "key2", "key3"].toSet()
            def cacheResponse = [
                    "key1": "value1",
                    "key2": null,
                    "key3": "value3"
            ]
            1 * cache.getValues(cacheKeys) >> {
                log.info("Fetching keys: {}", cacheKeys)
                cacheResponse
            }

            def collapser1 = new BatchObservableCollapser(cacheKeys[0], cache).toObservable()
            def collapser2 = new BatchObservableCollapser(cacheKeys[1], cache).toObservable()
            def collapser3 = new BatchObservableCollapser(cacheKeys[2], cache).toObservable()
        when:
            def value1 = collapser1.toBlocking().toFuture().get()
            def value2 = collapser2.toBlocking().toFuture().get()
            def value3 = collapser3.toBlocking().toFuture().get()
        then:
            value1 == "value1"
            value2 == null
            value3 == "value3"
    }

    def "correctly maps collapsed response to the individual requests even if there are duplicate keys"() {
        given:
            def duplicateKey = "key1"
            def cacheKeys = [duplicateKey].toSet()
            def key1Value = "value1"
            def cacheResponse = [
                    "key1": key1Value
            ]
            1 * cache.getValues(cacheKeys) >> {
                log.info("Fetching keys: {}", cacheKeys)
                cacheResponse
            }

            def collapser1 = new BatchObservableCollapser(duplicateKey, cache).toObservable()
            def collapser2 = new BatchObservableCollapser(duplicateKey, cache).toObservable()
            def collapser3 = new BatchObservableCollapser(duplicateKey, cache).toObservable()
        when:
            def value1 = collapser1.toBlocking().toFuture().get()
            def value2 = collapser2.toBlocking().toFuture().get()
            def value3 = collapser3.toBlocking().toFuture().get()
        then:
            value1 == key1Value
            value2 == key1Value
            value3 == key1Value
    }
}
