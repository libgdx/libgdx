
package com.badlogic.gdx.scenes.scene2d.utils;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.SnapshotArray;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;

public class ActorAnimation {
	private Array<Snapshot> snapshots = new Array();

	public void snapshotChildren (Group group) {
		SnapshotArray<Actor> children = group.getChildren();
		snapshots.ensureCapacity(children.size);
		for (int i = 0, n = children.size; i < n; i++) {
			Actor actor = children.get(i);
			Snapshot snapshot = Pools.obtain(Snapshot.class);
			snapshot.actor = actor;
			snapshot.x = actor.getX();
			snapshot.y = actor.getY();
			snapshot.width = actor.getWidth();
			snapshot.height = actor.getHeight();
			snapshot.rotation = actor.getRotation();
			snapshot.scaleX = actor.getScaleX();
			snapshot.scaleY = actor.getScaleY();
			snapshots.add(snapshot);
		}
	}

	public void animate (float duration) {
		for (int i = 0, n = snapshots.size; i < n; i++) {
			Snapshot snapshot = snapshots.get(i);
			Actor actor = snapshot.actor;
			boolean position = actor.getX() != snapshot.x || actor.getY() != snapshot.y;
			boolean size = actor.getWidth() != snapshot.width || actor.getHeight() != snapshot.height;
			boolean scale = actor.getScaleX() != snapshot.scaleX || actor.getScaleY() != snapshot.scaleY;
			boolean rotation = actor.getRotation() != snapshot.rotation;
			if (!position && !size && !scale && !rotation) continue;

			SequenceAction sequence = sequence();
			if (position) {
				sequence.addAction(moveBy(actor.getX() - snapshot.x, actor.getY() - snapshot.y, duration));
				actor.setPosition(snapshot.x, snapshot.y);
			}
			if (size) {
				sequence.addAction(sizeBy(actor.getWidth() - snapshot.width, actor.getHeight() - snapshot.height, duration));
				actor.setSize(snapshot.width, snapshot.height);
			}
			if (scale) {
				sequence.addAction(scaleBy(actor.getScaleX() - snapshot.scaleX, actor.getScaleY() - snapshot.scaleY, duration));
				actor.setScale(snapshot.scaleX, snapshot.scaleY);
			}
			if (rotation) {
				sequence.addAction(rotateBy(actor.getRotation() - snapshot.rotation, duration));
				actor.setRotation(snapshot.rotation);
			}
			Group parent = actor.getParent();
			if (parent instanceof Layout) {
				((Layout)parent).setLayoutEnabled(false);
				sequence.addAction(add(parent, layout(true)));
			}
			actor.addAction(sequence);
		}
		snapshots.clear();
	}

	public void clear () {
		Pools.free(snapshots);
		snapshots.clear();
	}

	static private class Snapshot implements Poolable {
		Actor actor;
		float x, y, width, height, rotation, scaleX, scaleY;

		public void reset () {
			actor = null;
		}
	}
}
