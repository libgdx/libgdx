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
public class GradientColorValue extends ParticleValue {

    private float[] colors = {1, 1, 1};
    float[] timeline = {0};

    public GradientColorValue() {
        setAlwaysActive(true);
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

    public float[] getColors() {
        return colors;
    }

    public void setColors(float[] colors) {
        this.colors = colors;
    }

    public float[] getColor(float percent) {
        int startIndex = 0, endIndex = -1;
        for (int i = 1; i < timeline.length; i++) {
            float t = timeline[i];
            if (t > percent) {
                endIndex = i;
                break;
            }
            startIndex = i;
        }
        float startTime = timeline[startIndex];
        startIndex *= 3;
        float r1 = colors[startIndex];
        float g1 = colors[startIndex + 1];
        float b1 = colors[startIndex + 2];
        float[] temp = new float[3];
        if (endIndex == -1) {
            temp[0] = r1;
            temp[1] = g1;
            temp[2] = b1;
            return temp;
        }
        float factor = (percent - startTime) / (timeline[endIndex] - startTime);
        endIndex *= 3;
        temp[0] = r1 + (colors[endIndex] - r1) * factor;
        temp[1] = g1 + (colors[endIndex + 1] - g1) * factor;
        temp[2] = b1 + (colors[endIndex + 2] - b1) * factor;
        return temp;
    }
//        public void save(Writer output) throws IOException {
//            super.save(output);
//            if (!active) {
//                return;
//            }
//            output.write("colorsCount: " + colors.length + "\n");
//            for (int i = 0; i < colors.length; i++) {
//                output.write("colors" + i + ": " + colors[i] + "\n");
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
//            colors = new float[readInt(reader, "colorsCount")];
//            for (int i = 0; i < colors.length; i++) {
//                colors[i] = readFloat(reader, "colors" + i);
//            }
//            timeline = new float[readInt(reader, "timelineCount")];
//            for (int i = 0; i < timeline.length; i++) {
//                timeline[i] = readFloat(reader, "timeline" + i);
//            }
//        }
//
//        public void load(GradientColorValue value) {
//            super.load(value);
//            colors = new float[value.colors.length];
//            System.arraycopy(value.colors, 0, colors, 0, colors.length);
//            timeline = new float[value.timeline.length];
//            System.arraycopy(value.timeline, 0, timeline, 0, timeline.length);
//        }

    @Override
    public GradientColorValue clone() {
        GradientColorValue n = (GradientColorValue) super.clone();
        n.colors = Arrays.copyOf(colors, colors.length);
        n.timeline = Arrays.copyOf(timeline, timeline.length);
        return n;
    }
}
