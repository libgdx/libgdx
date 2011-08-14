
package com.mojang.metagun;

public class Stats {
	public static Stats instance = new Stats();

	public int deaths = 0;
	public int shots = 0;
	public int kills = 0;
	public int jumps = 0;
	public int time = 0;
	public int hats = 0;

	public static void reset () {
		Stats.instance = new Stats();
	}

	private Stats () {
	}

	public int getSpeedScore () {
		int seconds = time / 60;
		int speedScore = (60 * 10 - seconds) * 100;
		if (speedScore < 0) speedScore = 0;
		return speedScore;
	}

	public int getDeathScore () {
		int deathScore = 10000 - deaths * 100;
		if (deathScore < 0) deathScore = 0;
		return deathScore;
	}

	public int getHatScore () {
		int hatScore = hats * 5000;
		return hatScore;
	}

	public int getShotScore () {
		return shots / 10;
	}

	public int getFinalScore () {
		return getSpeedScore() + getDeathScore() + getHatScore() + getShotScore();
	}

	public String getTimeString () {
		int seconds = time / 60;
		int minutes = seconds / 60;
		seconds %= 60;
		String str = minutes + ":";
		if (seconds < 10) str += "0";
		str += seconds;
		return str;
	}
}
