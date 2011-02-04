package com.mojang.metagun;

import java.io.*;

import javax.sound.sampled.*;

public class Sound {
    public static class Clips {
        public Clip[] clips;
        private int p;
        private int count;
        
        public Clips(byte[] buffer, int count) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
            if (buffer==null) return;
            
            clips = new Clip[count];
            this.count = count;
            for (int i=0; i<count; i++) {
                clips[i] = AudioSystem.getClip();
                clips[i].open(AudioSystem.getAudioInputStream(new ByteArrayInputStream(buffer)));
            }
        }
        
        public void play() {
            if (clips==null) return;

            clips[p].stop();
            clips[p].setFramePosition(0);
            clips[p].start();
            p++;
            if (p>=count) p = 0;
        }
    }
    public static Clips boom = load("/boom.wav", 4);
    public static Clips hit = load("/hit.wav", 4);
    public static Clips splat = load("/splat.wav", 4);
    public static Clips launch = load("/launch.wav", 4);
    public static Clips pew = load("/pew.wav", 4);
    public static Clips oof = load("/oof.wav", 4);
    public static Clips gethat = load("/gethat.wav", 4);
    public static Clips death = load("/death.wav", 4);
    public static Clips startgame = load("/startgame.wav", 1);
    public static Clips jump = load("/jump.wav", 1);

    private static Clips load(String name, int count) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataInputStream dis = new DataInputStream(Sound.class.getResourceAsStream(name));
            byte[] buffer = new byte[1024];
            int read = 0;
            while ((read = dis.read(buffer)) >= 0) {
                baos.write(buffer, 0, read);
            }
            dis.close();
            
            byte[] data = baos.toByteArray();
            return new Clips(data, count);
        } catch (Exception e) {
            try {
                return new Clips(null, 0);
            } catch (Exception ee) {
                return null;
            }
        }
    }

    public static void touch() {
    }
}
