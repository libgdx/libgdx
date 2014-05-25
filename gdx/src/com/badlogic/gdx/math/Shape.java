package com.badlogic.gdx.math;

import java.io.Serializable;

public abstract class Shape<T extends Shape<T>> implements Serializable {

    public float x, y;

    public Shape() {
        setPosition(0f, 0f);
    }

    public Shape(float x, float y) {
        setPosition(x, y);
    }

    public Shape(Vector2 position) {
        this(position.x, position.y);
    }

    public abstract void set(T shape);

    public void setPosition(float x, float y) {
        setX(x);
        setY(y);
    }

    public void setPosition(Vector2 position) {
        setPosition(position.x, position.y);
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void translate(float x, float y) {
        setPosition(this.x + x, this.y + y);
    }

    public abstract boolean overlaps(T shape);

    public abstract boolean contains(T shape);

    public abstract boolean contains(float x, float y);

    public boolean contains(Vector2 point) {
        return contains(point.x, point.y);
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    @Override
    public String toString () {
		return x + ", " + y;
	}

}
