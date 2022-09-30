package me.miquiis.wardrobe.database;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class LocalCache<T> {

    public class Cached
    {
        private long lastUpdate;
        private T value;

        public Cached(T value)
        {
            this.value = value;
        }

        public Cached(T value, long lastUpdate)
        {
            this.value = value;
            this.lastUpdate = lastUpdate;
        }

        public T getValue() {
            return value;
        }

        public long getLastUpdate() {
            return lastUpdate;
        }
    }

    private final List<Cached> cache;

    public LocalCache()
    {
        this.cache = new ArrayList<>();
    }

    public void cache(T cacheValue)
    {
        cache.add(new Cached(cacheValue));
    }

    public Optional<Cached> getCache(Predicate<? super Cached> predicate)
    {
        return cache.stream().filter(predicate).findFirst();
    }

    public boolean hasInCache(Predicate<? super Cached> predicate)
    {
        return cache.stream().anyMatch(predicate);
    }

}
