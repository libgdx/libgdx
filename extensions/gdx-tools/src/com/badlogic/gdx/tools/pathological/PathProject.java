
package com.badlogic.gdx.tools.pathological;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class PathProject implements Serializable {
	private String imageFile;
	private List<PathModel> pathModels;
	private transient boolean changed;

	public PathProject () {
		this.imageFile = null;
		this.pathModels = new ArrayList<>();
		this.pathModels.add(new PathModel());
		this.changed = true;
	}

	public void setImageFile (String file) {
		if (!Objects.equals(this.imageFile, file)) {
			// To handle cases where either is null
			this.imageFile = file;
			this.changed = true;
		}
	}

	public boolean hasChanged () {
		boolean c = false;
		for (PathModel model : this.pathModels) {
			if (model.hasChanged()) {
				c = true;
				break;
			}
		}

		return this.changed || c;
	}

	public void unsetChanged () {
		this.changed = false;
		for (PathModel model : this.pathModels) {
			model.unsetChanged();
		}
	}

	public String getImageFile () {
		return this.imageFile;
	}

	public PathModel createPathModel () {
		PathModel p = new PathModel();
		this.pathModels.add(p);
		this.changed = true;
		return p;
	}

	public int getNumberOfPathModels () {
		return this.pathModels.size();
	}

	public PathModel getPathModel (int index) {
		return (0 <= index && index < this.pathModels.size()) ? this.pathModels.get(index) : null;
	}

	public void removePathModel (int index) {
		if (0 <= index && index < this.pathModels.size()) {
			this.changed = true;
			this.pathModels.remove(index);
		}
	}

	public void removePathModel (PathModel model) {
		if (this.pathModels.contains(model)) {
			this.changed = true;
			this.pathModels.remove(model);
		}
	}
}
