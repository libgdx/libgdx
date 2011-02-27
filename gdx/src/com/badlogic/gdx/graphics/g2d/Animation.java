package com.badlogic.gdx.graphics.g2d;

/**
 * <p>An Animation stores a list of {@link TextureRegion}s representing an
 * animated sequence, e.g. for running or jumping. Each region of an
 * Animation is called a key frame, multiple key frames make up the
 * animation.<p>  
 * 
 * @author mzechner
 *
 */
public class Animation {     
   final TextureRegion[] keyFrames;
   final float frameDuration;
   
   /**
    * Constructor, storing the frame duration and key frames. 
    * 
    * @param frameDuration the time between frames in seconds.
    * @param keyFrames the {@link TextureRegion}s representing the frames.
    */
   public Animation(float frameDuration, TextureRegion ... keyFrames) {
       this.frameDuration = frameDuration;
       this.keyFrames = keyFrames;
   }
   
   /**
    * Returns a {@link TextureRegion} based on the so called state time. 
    * This is the amount of seconds an object has spent in the state this
    * Animation instance represents, e.g. running, jumping and so on. The
    * mode specifies whether the animation is looping or not. 
    * @param stateTime the time spent in the state represented by this animation.
    * @param looping whether the animation is looping or not.
    * @return the TextureRegion representing the frame of animation for the given state time.
    */
   public TextureRegion getKeyFrame(float stateTime, boolean looping) {
       int frameNumber = (int)(stateTime / frameDuration);
       
       if(!looping) {
           frameNumber = Math.min(keyFrames.length-1, frameNumber);            
       } else {
           frameNumber = frameNumber % keyFrames.length;
       }        
       return keyFrames[frameNumber];
   }
}
