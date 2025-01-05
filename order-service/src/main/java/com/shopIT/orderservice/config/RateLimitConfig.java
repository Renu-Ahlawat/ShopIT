package com.shopit.orderservice.config;

import com.shopit.orderservice.entity.TPSEntity;
import com.shopit.orderservice.repository.TPSRepository;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.Refill;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;

@Configuration
public class RateLimitConfig {
    public final ProxyManager buckets;

    private final TPSRepository tpsRepo;

    private final RedissonClient redissonClient;

    @Autowired
    public RateLimitConfig(ProxyManager buckets, TPSRepository tpsRepo, RedissonClient redissonClient) {
        this.buckets = buckets;
        this.tpsRepo = tpsRepo;
        this.redissonClient = redissonClient;
    }

    public Bucket resolveBucket(String key) {
        Supplier<BucketConfiguration> configurationSupplier = getConfigSupplier(key);
        return buckets.builder().build(key, configurationSupplier);
    }

    private Supplier<BucketConfiguration> getConfigSupplier(String key) {
        RMapCache<String, TPSEntity> cache = redissonClient.getMapCache("tpsOrderCache"); // Cache map created under
                                                                                          // 'Cache' which will hold
                                                                                          // the username's TPSentity
        TPSEntity tpsEntity = cache.get(key);

        if (tpsEntity == null) {
            Optional<TPSEntity> tpsOpt = tpsRepo.findByUsername(key);
            // Default TPS value if not found in the database
            tpsEntity = tpsOpt.orElseGet(() -> TPSEntity.builder()
                    .username(key)
                    .tps(10)
                    .build());
            tpsRepo.save(tpsEntity);
            cache.put(key, tpsEntity);
        }

        int tps = tpsEntity.getTps();
        Refill refill = Refill.intervally(tps, Duration.ofMinutes(1)); // Here tps defines the no. of tokens inserted
                                                                       // every 1 min
        Bandwidth limit = Bandwidth.classic(tps, refill); // Here tps is the max no. of tokens or capacity of the bucket

        return () -> (BucketConfiguration.builder()
                .addLimit(limit)
                .build());
    }
}
