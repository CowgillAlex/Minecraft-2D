package dev.alexco.minecraft.util;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import com.google.common.collect.Maps;

public class MapFiller {
    /**
     * Builds a linked hash map by zipping key and value iterables.
     */
    public static <K, V> Map<K, V> linkedHashMapFrom(Iterable<K> iterable, Iterable<V> iterable2) {
        return MapFiller.from(iterable, iterable2, Maps.newLinkedHashMap());
    }

    /**
     * Fills the target map by zipping keys and values in iteration order.
     */
    public static <K, V> Map<K, V> from(Iterable<K> iterable, Iterable<V> iterable2, Map<K, V> map) {
        Iterator<V> iterator = iterable2.iterator();
        for (K k : iterable) {
            map.put(k, iterator.next());
        }
        if (iterator.hasNext()) {
            throw new NoSuchElementException();
        }
        return map;
    }
}
