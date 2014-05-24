package org.jbox2d.pooling.arrays;

import java.util.HashMap;

import org.jbox2d.particle.VoronoiDiagram;

public class GeneratorArray {

  private final HashMap<Integer, VoronoiDiagram.Generator[]> map =
      new HashMap<Integer, VoronoiDiagram.Generator[]>();

  public VoronoiDiagram.Generator[] get(int length) {
    assert (length > 0);

    if (!map.containsKey(length)) {
      map.put(length, getInitializedArray(length));
    }

    assert (map.get(length).length == length) : "Array not built of correct length";
    return map.get(length);
  }

  protected VoronoiDiagram.Generator[] getInitializedArray(int length) {
    final VoronoiDiagram.Generator[] ray = new VoronoiDiagram.Generator[length];
    for (int i = 0; i < ray.length; i++) {
      ray[i] = new VoronoiDiagram.Generator();
    }
    return ray;
  }
}
