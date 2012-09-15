/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.badlogic.gdx.graphics.g2d.particle.emitterattributs;

import java.util.Arrays;

/**
 *
 * @author Daniel Heinrich <dannynullzwo@gmail.com>
 */
public class ScaledNumericValue extends ParticleValue {

    private float[] scaling = {1};
    float[] timeline = {0};
    private boolean relative;
    private RangedNumericValue low, heigh;

    public ScaledNumericValue() {
        low = new RangedNumericValue();
        heigh = new RangedNumericValue();
    }

    public float newHighValue() {
        return heigh.newValue();
    }

    public float newLowValue() {
        return low.newValue();
    }

    public RangedNumericValue getHeigh() {
        return heigh;
    }

    public RangedNumericValue getLow() {
        return low;
    }

    public float[] getScaling() {
        return scaling;
    }

    public void setScaling(float[] values) {
        this.scaling = values;
    }

    public float[] getTimeline() {
        return timeline;
    }
    
    public int getTimeLineCount()
    {
        return timeline.length;
    }

    public void setTimeline(float[] timeline) {
        this.timeline = timeline;
    }

    public boolean isRelative() {
        return relative;
    }

    public void setRelative(boolean relative) {
        this.relative = relative;
    }

    public float getScale(float percent) {
        int endIndex = -1;
        for (int i = 1; i < timeline.length; i++) {
            if (timeline[i] > percent) {
                endIndex = i;
                break;
            }
        }
        if (endIndex == -1) {
            return scaling[timeline.length - 1];
        }
        int startIndex = endIndex - 1;
        float startValue = scaling[startIndex];
        float startTime = timeline[startIndex];
        return startValue + (scaling[endIndex] - startValue) * ((percent - startTime) / (timeline[endIndex] - startTime));
    }
//        public void save(Writer output) throws IOException {
//            super.save(output);
//            if (!active) {
//                return;
//            }
//            output.write("highMin: " + highMin + "\n");
//            output.write("highMax: " + highMax + "\n");
//            output.write("relative: " + relative + "\n");
//            output.write("scalingCount: " + scaling.length + "\n");
//            for (int i = 0; i < scaling.length; i++) {
//                output.write("scaling" + i + ": " + scaling[i] + "\n");
//            }
//            output.write("timelineCount: " + timeline.length + "\n");
//            for (int i = 0; i < timeline.length; i++) {
//                output.write("timeline" + i + ": " + timeline[i] + "\n");
//            }
//        }
//
//        public void load(BufferedReader reader) throws IOException {
//            super.load(reader);
//            if (!active) {
//                return;
//            }
//            highMin = readFloat(reader, "highMin");
//            highMax = readFloat(reader, "highMax");
//            relative = readBoolean(reader, "relative");
//            scaling = new float[readInt(reader, "scalingCount")];
//            for (int i = 0; i < scaling.length; i++) {
//                scaling[i] = readFloat(reader, "scaling" + i);
//            }
//            timeline = new float[readInt(reader, "timelineCount")];
//            for (int i = 0; i < timeline.length; i++) {
//                timeline[i] = readFloat(reader, "timeline" + i);
//            }
//        }

    @Override
    public ScaledNumericValue clone() {
        ScaledNumericValue n= (ScaledNumericValue) super.clone();
        n.low = low.clone();
        n.heigh = heigh.clone();
        n.scaling = Arrays.copyOf(scaling, scaling.length);
        n.timeline = Arrays.copyOf(timeline, timeline.length);
        return n;
    }
}
