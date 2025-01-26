## 11 May 2023

This is an attempt to port box2d 2.4.1 to Java.

0. Lots of the old_changes todos are cleared by @MobiDevelop
0. Attempted to implement `Shape::testPoint`
1. Ropes and the corresponding test. `setTuning` is left because it's just a subset of `create`
2. No-ops for now (also no-op in main box2d):
   * `com.badlogic.gdx.physics.box2d.World#setDestructionListener`
   * `com.badlogic.gdx.physics.box2d.World.setUseDefaultContactFilter`

## 12 May 2023

0. Ported some commits from libGDX, so we have better performance, nullability protection, more APIs, etc.
1. Removed `userData` objects from native library, because we already have them on JVM.
2. Renamed `QueryAABB` into `queryAABB`, provide an overload.
3. Applied some IntelliJ code inspections.
4. Tried to port `b2Hull` but didn't find an optimal way.
