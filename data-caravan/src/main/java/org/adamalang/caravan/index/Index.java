/*
 * This file is subject to the terms and conditions outlined in the file 'LICENSE' (hint: it's MIT); this file is located in the root directory near the README.md which you should also read.
 *
 * This file is part of the 'Adama' project which is a programming language and document store for board games; however, it can be so much more.
 *
 * See http://www.adama-lang.org/ for more information.
 *
 * (c) 2020 - 2022 by Jeffrey M. Barber (http://jeffrey.io)
 */
package org.adamalang.caravan.index;

import io.netty.buffer.ByteBuf;

import java.util.*;

/** maps longs to lists of regions */
public class Index {
  private final HashMap<Long, ArrayList<AnnotatedRegion>> index;

  public Index() {
    this.index = new HashMap<>();
  }

  /** append a region to an id */
  public int append(long id, AnnotatedRegion region) {
    ArrayList<AnnotatedRegion> regions = index.get(id);
    if (regions == null) {
      regions = new ArrayList<>();
      index.put(id, regions);
    }
    regions.add(region);
    return regions.size();
  }

  /** return the regions bound to an object */
  public Iterator<AnnotatedRegion> get(long id) {
    List<AnnotatedRegion> regions = index.get(id);
    if (regions == null) {
      regions = Collections.emptyList();
    }
    return regions.iterator();
  }

  /** does the index contain the given id */
  public boolean exists(long id) {
    return index.containsKey(id);
  }

  /** delete an object by id; return the regions allocated to it */
  public ArrayList<AnnotatedRegion> delete(long id) {
    return index.remove(id);
  }

  /** trim the head of an object (by id) the given count; returned the returned regions */
  public ArrayList<AnnotatedRegion> trim(long id, int count) {
    ArrayList<AnnotatedRegion> regions = index.get(id);
    if (regions != null) {
      ArrayList<AnnotatedRegion> trimmed = new ArrayList<>();
      Iterator<AnnotatedRegion> it = regions.iterator();
      int k = 0;
      while (k < count && it.hasNext()) {
        AnnotatedRegion region = it.next();
        trimmed.add(region);
        it.remove();
        k++;
      }
      return trimmed;
    }
    return null;
  }

  /** take a snapshot of the index */
  public void snapshot(ByteBuf buf) {
    for (Map.Entry<Long, ArrayList<AnnotatedRegion>> entry : index.entrySet()) {
      buf.writeBoolean(true);
      buf.writeLongLE(entry.getKey());
      buf.writeIntLE(entry.getValue().size());
      for (AnnotatedRegion region : entry.getValue()) {
        buf.writeLongLE(region.position);
        buf.writeIntLE(region.size);
        buf.writeIntLE(region.seq);
        buf.writeLongLE(region.assetBytes);
      }
    }
    buf.writeBoolean(false);
  }

  /** load an index from a snapshot */
  public void load(ByteBuf buf) {
    index.clear();
    while (buf.readBoolean()) {
      long id = buf.readLongLE();
      int count = buf.readIntLE();
      ArrayList<AnnotatedRegion> regions = new ArrayList<>(count);
      for (int k = 0; k < count; k++) {
        long start = buf.readLongLE();
        int size = buf.readIntLE();
        int seq = buf.readIntLE();
        long assetBytes = buf.readLongLE();
        AnnotatedRegion region = new AnnotatedRegion(start, size, seq, assetBytes);
        regions.add(region);
      }
      index.put(id, regions);
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<Long, ArrayList<AnnotatedRegion>> entry : index.entrySet()) {
      sb.append(entry.getKey()).append("=");
      for (AnnotatedRegion region : entry.getValue()) {
        sb.append(region.toString());
      }
      sb.append(";");
    }
    return sb.toString();
  }
}
