package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 * ParticleEffectActor holds an {@link ParticleEffect} to use in Scene2d applications.
 * The particle effect is positioned in the centered in the ParticleEffectActor. Its bounding box
 * is not limited to the size of this actor.
 */
public class ParticleEffectActor extends Actor {
    private final ParticleEffect particleEffect;
    float lastDelta;
    private boolean isComplete = true;
    private boolean resetOnStart;

    public ParticleEffectActor(ParticleEffect particleEffect, boolean resetOnStart) {
        super();
        this.particleEffect = particleEffect;
        this.resetOnStart = resetOnStart;
    }

    public ParticleEffectActor(FileHandle particleFile, TextureAtlas atlas) {
        super();
        particleEffect = new ParticleEffect();
        particleEffect.load(particleFile, atlas);
    }

    public ParticleEffectActor(FileHandle particleFile, FileHandle imagesDir) {
        super();
        particleEffect = new ParticleEffect();
        particleEffect.load(particleFile, imagesDir);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        particleEffect.setPosition(getX() + getWidth() / 2, getY() + getHeight() / 2);
        if (lastDelta > 0) {
            particleEffect.update(lastDelta);
            lastDelta = 0;
        }
        if (!isComplete) {
            particleEffect.draw(batch);
            isComplete = particleEffect.isComplete();
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        // don't do particleEffect.update() here - the correct position is set  just while we
        // are in draw() method. We save the delta here to update in draw()
        lastDelta = delta;
    }

    public void start() {
        isComplete = false;
        if (resetOnStart)
            particleEffect.reset();
        particleEffect.start();
    }

    public boolean isResetOnStart() {
        return resetOnStart;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void setResetOnStart(boolean resetOnStart) {
        this.resetOnStart = resetOnStart;
    }

    public ParticleEffect getEffect() {
        return this.particleEffect;
    }

    @Override
    protected void scaleChanged() {
        super.scaleChanged();
        particleEffect.scaleEffect(getScaleX(), getScaleY(), getScaleY());
    }

    public void cancel() {
        isComplete = true;
    }

    public void allowCompletion() {
        particleEffect.allowCompletion();
    }

    public void dispose() {
        particleEffect.dispose();
    }

}