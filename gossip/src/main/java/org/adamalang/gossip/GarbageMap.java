/*
 * This file is subject to the terms and conditions outlined in the file 'LICENSE' (hint: it's MIT); this file is located in the root directory near the README.md which you should also read.
 *
 * This file is part of the 'Adama' project which is a programming language and document store for board games; however, it can be so much more.
 *
 * See http://www.adama-lang.org/ for more information.
 *
 * (c) 2020 - 2022 by Jeffrey M. Barber (http://jeffrey.io)
 */
package org.adamalang.gossip;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/** represents a map that cleans things up based on time */
public class GarbageMap<T> implements Iterable<T> {

  private final LinkedHashMap<String, AgingItem> items;

  public GarbageMap(int maxSize) {
    this.items = new LinkedHashMap<>(maxSize, 0.75f, true) {
      @Override
      protected boolean removeEldestEntry(Map.Entry<String, AgingItem> eldest) {
        return this.size() > maxSize;
      }
    };
  }

  public Collection<String> keys() {
    return items.keySet();
  }

  @Override
  public Iterator<T> iterator() {
    Iterator<AgingItem> it = items.values().iterator();
    return new Iterator<T>() {
      @Override
      public boolean hasNext() {
        return it.hasNext();
      }

      @Override
      public T next() {
        return it.next().item;
      }
    };
  }

  public void put(String key, T value, long now) {
    items.put(key, new AgingItem(value, now));
  }

  public T get(String key) {
    AgingItem item = items.get(key);
    if (item != null) {
      return item.item;
    }
    return null;
  }

  public T remove(String key) {
    AgingItem item = items.remove(key);
    if (item != null) {
      return item.item;
    }
    return null;
  }

  public int size() {
    return items.size();
  }

  public int gc(long now) {
    int removals = 0;
    Iterator<Map.Entry<String, AgingItem>> it = items.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<String, AgingItem> entry = it.next();
      long age = now - entry.getValue().time;
      if (age > Constants.MILLISECONDS_TO_SIT_IN_GARBAGE_MAP) {
        it.remove();
        removals++;
      }
    }
    return removals;
  }

  private class AgingItem {
    public final T item;
    public final long time;

    public AgingItem(T item, long now) {
      this.item = item;
      this.time = now;
    }
  }
}
