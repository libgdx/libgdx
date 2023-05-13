
package com.badlogic.gdx.physics.box2d.rope;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.JniUtil;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.FloatArray;

/** b2_rope.h
 * 
 * @author ice1000 */
public class Rope implements Disposable {
	// @off
	/*JNI
#include <box2d/box2d.h>
#include <box2d/b2_rope.h>
	 */ // @on
	protected long addr;

	protected Rope (long addr) {
		this.addr = addr;
	}

	public Rope () {
		this(newRope());
	}

	private static native long newRope (); /*
		// @off
        return (jlong)(new b2Rope());
    */

  @Override
  public void dispose() {
    jniDispose(addr);
  }

  private native void jniDispose(long addr); /*
		// @off
        b2Rope* rope = (b2Rope*)addr;
        delete rope;
    */

//    ///
//    void Create(const b2RopeDef& def);

  public void create(RopeDef def) {
    RopeTuning tuning = def.tuning;
    float[] verts = JniUtil.arrayOfVec2IntoFloat(def.vertices);
    jniCreate(addr, verts, def.masses, def.masses.length, def.gravity.x, def.gravity.y,
        tuning.stretchingModel.value, tuning.bendingModel.value, tuning.damping,
        tuning.stretchStiffness, tuning.stretchHertz, tuning.stretchDamping,
        tuning.bendStiffness, tuning.bendHertz, tuning.bendDamping,
        tuning.isometric, tuning.fixedEffectiveMass, tuning.warmStart);
  }

  private native void jniCreate(
      long addr, float[] verts, float[] masses, int count,
      float x, float y, int stretchingModel, int bendingModel, float damping, float stretchStiffness,
      float stretchHertz, float stretchDamping, float bendStiffness, float bendHertz,
      float bendDamping, boolean isometric, boolean fixedEffectiveMass, boolean warmStart); /*
		// @off
        b2Rope* rope = (b2Rope*)addr;
        b2RopeDef def;
        def.count = count;
		b2Vec2* verticesOut = new b2Vec2[count];
		int offset = 0;
		for(int i = 0; i < count; i++) {
			verticesOut[i] = b2Vec2(verts[(i<<1) + offset], verts[(i<<1) + offset + 1]);
		}
		float* massesOut = new float[count];
		for(int i = 0; i < count; i++) {
		    massesOut[i] = masses[i];
        }
        def.vertices = verticesOut;
        def.masses = massesOut;
        def.gravity.Set(x, y);
        def.tuning.stretchingModel = (b2StretchingModel)stretchingModel;
        def.tuning.bendingModel = (b2BendingModel)bendingModel;
        def.tuning.damping = damping;
        def.tuning.stretchStiffness = stretchStiffness;
        def.tuning.stretchHertz = stretchHertz;
        def.tuning.stretchDamping = stretchDamping;
        def.tuning.bendStiffness = bendStiffness;
        def.tuning.bendHertz = bendHertz;
        def.tuning.bendDamping = bendDamping;
        def.tuning.isometric = isometric;
        def.tuning.fixedEffectiveMass = fixedEffectiveMass;
        def.tuning.warmStart = warmStart;
        rope->Create(def);
        delete[] verticesOut;
        delete[] massesOut;
    */

//    ///
//    void SetTuning(const b2RopeTuning& tuning);

  public void step(float timeStep, int iterations, Vector2 position) {
    jniStep(addr, timeStep, iterations, position.x, position.y);
  }

  private native void jniStep(long addr, float timeStep, int iterations, float x, float y); /*
		// @off
        b2Rope* rope = (b2Rope*)addr;
        rope->Step(timeStep, iterations, b2Vec2(x, y));
    */

  public void reset(Vector2 position) {
    jniReset(addr, position.x, position.y);
  }

  private native void jniReset(long addr, float x, float y); /*
		// @off
        b2Rope* rope = (b2Rope*)addr;
        rope->Reset(b2Vec2(x, y));
    */

  public static class DrawData {
    private final Vector2 tmp = new Vector2();
    public final FloatArray verticesFlat = new FloatArray();
    public final FloatArray invMasses = new FloatArray();
    public int count;

    public Vector2 pointAt(int i) {
      tmp.x = verticesFlat.get(i * 2);
      tmp.y = verticesFlat.get(i * 2 + 1);
      return tmp;
    }
  }

  private static DrawData drawData;

  private native int jniGetCount(long addr); /*
		// @off
        b2Rope* rope = (b2Rope*)addr;
        return rope->JavaGetCount();
    */

  private native void jniGetPS(long addr, float[] buf); /*
		// @off
        b2Rope* rope = (b2Rope*)addr;
        rope->JavaGetPS(buf);
    */

  private native void jniGetInvMasses(long addr, float[] buf); /*
		// @off
        b2Rope* rope = (b2Rope*)addr;
        rope->JavaGetInvMasses(buf);
    */

  public DrawData getDrawData() {
    if (drawData == null) drawData = new DrawData();
    drawData.verticesFlat.clear();
    drawData.invMasses.clear();
    int count = jniGetCount(addr);
    drawData.count = count;
    drawData.verticesFlat.setSize(count * 2);
    drawData.invMasses.setSize(count);
    jniGetPS(addr, drawData.verticesFlat.items);
    jniGetInvMasses(addr, drawData.invMasses.items);
    return drawData;
  }
}
