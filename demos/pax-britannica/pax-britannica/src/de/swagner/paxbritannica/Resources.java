package de.swagner.paxbritannica;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public class Resources {

	public Sprite title = new Sprite(new Texture(Gdx.files.internal("data/spritepack/title.png")));
	public Sprite credits = new Sprite(new Texture(Gdx.files.internal("data/spritepack/credits.png")));
	
	public TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("data/spritepack/packhigh.pack"));
	// public AtlasRegion region = atlas.findRegion("imagename");
	// Sprite sprite = atlas.createSprite("otherimagename");

	public Music music = Gdx.audio.newMusic(Gdx.files.internal("data/audio/music.mp3"));

	public Sprite factoryP1 = atlas.createSprite("factoryp1");
	public Sprite factoryP2 = atlas.createSprite("factoryp2");
	public Sprite factoryP3 = atlas.createSprite("factoryp3");
	public Sprite factoryP4 = atlas.createSprite("factoryp4");
	
	public Sprite factoryP1Small = atlas.createSprite("factoryp1");
	public Sprite factoryP2Small = atlas.createSprite("factoryp2");
	public Sprite factoryP3Small = atlas.createSprite("factoryp3");
	public Sprite factoryP4Small = atlas.createSprite("factoryp4");

	public Sprite fighterP1 = atlas.createSprite("fighterp1");
	public Sprite fighterP2 = atlas.createSprite("fighterp2");
	public Sprite fighterP3 = atlas.createSprite("fighterp3");
	public Sprite fighterP4 = atlas.createSprite("fighterp4");

	public Sprite bomberP1 = atlas.createSprite("bomberp1");
	public Sprite bomberP2 = atlas.createSprite("bomberp2");
	public Sprite bomberP3 = atlas.createSprite("bomberp3");
	public Sprite bomberP4 = atlas.createSprite("bomberp4");

	public Sprite frigateP1 = atlas.createSprite("frigatep1");
	public Sprite frigateP2 = atlas.createSprite("frigatep2");
	public Sprite frigateP3 = atlas.createSprite("frigatep3");
	public Sprite frigateP4 = atlas.createSprite("frigatep4");

	public Sprite debrisSmall = atlas.createSprite("debrissmall");
	public Sprite debrisMed = atlas.createSprite("debrismed");
	public Sprite debrisLarge = atlas.createSprite("debrislarge");

	public Sprite fish1 = atlas.createSprite("fish1");
	public Sprite fish2 = atlas.createSprite("fish2");
	public Sprite fish3 = atlas.createSprite("fish3");
	public Sprite fish4 = atlas.createSprite("fish4");
	public Sprite fish5 = atlas.createSprite("fish5");
	public Sprite fish6 = atlas.createSprite("fish6");
	public Sprite fish7 = atlas.createSprite("fish7");
	public Sprite fish8 = atlas.createSprite("fish8");

	public Sprite needle = atlas.createSprite("needle");

	public Sprite background = atlas.createSprite("background");

	public Sprite blackFade = atlas.createSprite("blackfade");

	public Sprite laser = atlas.createSprite("laser");
	public Sprite missile = atlas.createSprite("missile");
	public Sprite bomb = atlas.createSprite("bomb");

	public Sprite production1 = atlas.createSprite("production1");
	public Sprite production2 = atlas.createSprite("production2");
	public Sprite production3 = atlas.createSprite("production3");

	public Sprite production_tile1 = atlas.createSprite("productiontile");
	public Sprite production_tile2 = atlas.createSprite("productiontile");
	public Sprite production_tile3 = atlas.createSprite("productiontile");
	public Sprite production_tile4 = atlas.createSprite("productiontile");

	public Sprite upgradeOutline = atlas.createSprite("upgradeoutline");
	public Sprite frigateOutline = atlas.createSprite("frigateoutline");
	public Sprite bomberOutline = atlas.createSprite("bomberoutline");
	public Sprite fighterOutline = atlas.createSprite("fighteroutline");

	public Sprite healthNone = atlas.createSprite("healthnone");
	public Sprite healthSome = atlas.createSprite("healthsome");
	public Sprite healthFull = atlas.createSprite("healthfull");

	public Sprite aButton = atlas.createSprite("abutton");
	public Sprite aCpuButton = atlas.createSprite("acpubutton");
	public Sprite aPlayerButton = atlas.createSprite("aplayerbutton");
	
	public Sprite cpuButton = atlas.createSprite("cpubutton");
	public Sprite playerButton = atlas.createSprite("playerbutton");

	public Sprite cnt1 = atlas.createSprite("1");
	public Sprite cnt2 = atlas.createSprite("2");
	public Sprite cnt3 = atlas.createSprite("3");
	public Sprite cnt4 = atlas.createSprite("4");
	public Sprite cnt5 = atlas.createSprite("5");

	public Sprite spark = atlas.createSprite("spark");
	public Sprite bubble = atlas.createSprite("bubble");
	public Sprite bigbubble = atlas.createSprite("bigbubble");
	public Sprite explosion = atlas.createSprite("explosion");

	public Sprite factoryHeavyDamage1 = atlas.createSprite("factoryheavydamage1");
	public Sprite factoryHeavyDamage2 = atlas.createSprite("factoryheavydamage2");
	public Sprite factoryHeavyDamage3 = atlas.createSprite("factoryheavydamage3");
	public Sprite factoryLightDamage1 = atlas.createSprite("factorylightdamage1");
	public Sprite factoryLightDamage2 = atlas.createSprite("factorylightdamage2");
	public Sprite factoryLightDamage3 = atlas.createSprite("factorylightdamage3");

	public Sprite touchArea1 = atlas.createSprite("touchArea");
	public Sprite touchArea2 = atlas.createSprite("touchArea");
	public Sprite touchArea3 = atlas.createSprite("touchArea");
	public Sprite touchArea4 = atlas.createSprite("touchArea");
	
	public Sprite help = atlas.createSprite("help");
	public Sprite musicOnOff = atlas.createSprite("music");
	public Sprite back = atlas.createSprite("back");
	public Sprite settings = atlas.createSprite("settings");
	public Sprite checkboxOn = atlas.createSprite("checkboxon");
	public Sprite checkboxOff = atlas.createSprite("checkboxoff");

	public static Resources instance;

	public static Resources getInstance() {
		if (instance == null) {
			instance = new Resources();
		}
		return instance;
	}

	public Resources() {
		reInit();
	}

	public void reInit() {
		dispose();
		
		Preferences prefs = Gdx.app.getPreferences("paxbritannica");
		if (prefs.getInteger("antiAliasConfig", 1) == 0) {
			atlas = new TextureAtlas(Gdx.files.internal("data/spritepack/pack.pack"));
		} else {
			atlas = new TextureAtlas(Gdx.files.internal("data/spritepack/packhigh.pack"));
		}

		try {
			if (music != null) {
				music.stop();
				music.dispose();
			} 
			music = Gdx.audio.newMusic(Gdx.files.internal("data/audio/music.mp3"));
		} catch (Exception e) {
			music = Gdx.audio.newMusic(Gdx.files.internal("data/audio/music.mp3"));
		}

		factoryP1 = atlas.createSprite("factoryp1");
		factoryP2 = atlas.createSprite("factoryp2");
		factoryP3 = atlas.createSprite("factoryp3");
		factoryP4 = atlas.createSprite("factoryp4");
		
		factoryP1Small = atlas.createSprite("factoryp1");
		factoryP2Small = atlas.createSprite("factoryp2");
		factoryP3Small = atlas.createSprite("factoryp3");
		factoryP4Small = atlas.createSprite("factoryp4");

		fighterP1 = atlas.createSprite("fighterp1");
		fighterP2 = atlas.createSprite("fighterp2");
		fighterP3 = atlas.createSprite("fighterp3");
		fighterP4 = atlas.createSprite("fighterp4");

		bomberP1 = atlas.createSprite("bomberp1");
		bomberP2 = atlas.createSprite("bomberp2");
		bomberP3 = atlas.createSprite("bomberp3");
		bomberP4 = atlas.createSprite("bomberp4");

		frigateP1 = atlas.createSprite("frigatep1");
		frigateP2 = atlas.createSprite("frigatep2");
		frigateP3 = atlas.createSprite("frigatep3");
		frigateP4 = atlas.createSprite("frigatep4");

		debrisSmall = atlas.createSprite("debrissmall");
		debrisMed = atlas.createSprite("debrismed");
		debrisLarge = atlas.createSprite("debrislarge");

		fish1 = atlas.createSprite("fish1");
		fish2 = atlas.createSprite("fish2");
		fish3 = atlas.createSprite("fish3");
		fish4 = atlas.createSprite("fish4");
		fish5 = atlas.createSprite("fish5");
		fish6 = atlas.createSprite("fish6");
		fish7 = atlas.createSprite("fish7");
		fish8 = atlas.createSprite("fish8");

		needle = atlas.createSprite("needle");

		background = atlas.createSprite("background");

		blackFade = atlas.createSprite("blackfade");

		laser = atlas.createSprite("laser");
		missile = atlas.createSprite("missile");
		bomb = atlas.createSprite("bomb");

		production1 = atlas.createSprite("production1");
		production2 = atlas.createSprite("production2");
		production3 = atlas.createSprite("production3");

		production_tile1 = atlas.createSprite("productiontile");
		production_tile1.rotate90(true);
		production_tile1.rotate90(true);
		production_tile2 = atlas.createSprite("productiontile");
		production_tile2.rotate90(false);
		production_tile3 = atlas.createSprite("productiontile");
		production_tile4 = atlas.createSprite("productiontile");
		production_tile4.rotate90(true);

		upgradeOutline = atlas.createSprite("upgradeoutline");
		frigateOutline = atlas.createSprite("frigateoutline");
		bomberOutline = atlas.createSprite("bomberoutline");
		fighterOutline = atlas.createSprite("fighteroutline");

		healthNone = atlas.createSprite("healthnone");
		healthSome = atlas.createSprite("healthsome");
		healthFull = atlas.createSprite("healthfull");

		aButton = atlas.createSprite("abutton");
		aCpuButton = atlas.createSprite("acpubutton");
		aPlayerButton = atlas.createSprite("aplayerbutton");
		
		cpuButton = atlas.createSprite("cpubutton");
		playerButton = atlas.createSprite("playerbutton");

		cnt1 = atlas.createSprite("1");
		cnt2 = atlas.createSprite("2");
		cnt3 = atlas.createSprite("3");
		cnt4 = atlas.createSprite("4");
		cnt5 = atlas.createSprite("5");

		spark = atlas.createSprite("spark");
		bubble = atlas.createSprite("bubble");
		bigbubble = atlas.createSprite("bigbubble");
		explosion = atlas.createSprite("explosion");

		factoryHeavyDamage1 = atlas.createSprite("factoryheavydamage1");
		factoryHeavyDamage2 = atlas.createSprite("factoryheavydamage2");
		factoryHeavyDamage3 = atlas.createSprite("factoryheavydamage3");
		factoryLightDamage1 = atlas.createSprite("factorylightdamage1");
		factoryLightDamage2 = atlas.createSprite("factorylightdamage2");
		factoryLightDamage3 = atlas.createSprite("factorylightdamage3");

		touchArea1 = atlas.createSprite("touchArea");
		touchArea2 = atlas.createSprite("touchArea");
		touchArea3 = atlas.createSprite("touchArea");
		touchArea4 = atlas.createSprite("touchArea");
		
		help = atlas.createSprite("help");
		musicOnOff = atlas.createSprite("music");
		back = atlas.createSprite("back");
		settings = atlas.createSprite("settings");
		checkboxOn = atlas.createSprite("checkboxon");
		checkboxOff = atlas.createSprite("checkboxoff");

		title = new Sprite(new Texture(Gdx.files.internal("data/spritepack/title.png")));
		title.getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
		credits = new Sprite(new Texture(Gdx.files.internal("data/spritepack/credits.png")));
		credits.getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
	}

	public void dispose() {
		atlas.dispose();
	}

}
