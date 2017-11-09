package sound;

import game.ClientSettings;

import java.util.HashMap;
import java.util.Timer;
import org.newdawn.slick.Music;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;

public class BgMusic {

  private HashMap<String, Music> Songs = new HashMap<String, Music>();
  private HashMap<String, Sound> Ambient = new HashMap<String, Sound>();
  public static String activeSong = null;
  public static String activeAmbient = null;

  private Timer timer = new Timer();

  public BgMusic() {}

  public void load() {}

  public void stop() {
    if (activeSong != null && Songs.get(activeSong) != null) {
      Songs.get(activeSong).fade(1000, 0, true);
    }
    if (activeAmbient != null && Ambient.get(activeAmbient) != null) {
      Ambient.get(activeAmbient).stop();
    }
    activeSong = null;
    activeAmbient = null;
  }

  public void changeSong(String nextSongToPlay, String nextAmbientToPlay) {
    if (ClientSettings.MUSIC_ON
    && (activeSong==null || !activeSong.equals(nextSongToPlay))
    ) {
      playSong(nextSongToPlay);
    }
    if (ClientSettings.SFX_ON
    && (activeAmbient==null || !activeAmbient.equals(nextAmbientToPlay))
    ) {
      playAmbient(nextAmbientToPlay);
    }
  }

  public void playSong(String songName) {

    loadSongIfNotLoaded(songName);

    if (Songs.get(activeSong) != null) {
      Songs.get(activeSong).stop();
    }

    if (Songs.get(songName) != null) {
      Songs.get(songName).loop(1.0f, ClientSettings.musicVolume);
      Songs.get(songName).setVolume(ClientSettings.musicVolume);
    }
    activeSong = songName;
  }

  public void playAmbient(String ambientName) {
    loadAmbientIfNotLoaded(ambientName);

    if (Ambient.get(activeAmbient) != null) {
      Ambient.get(activeAmbient).stop();
    }

    if (Ambient.get(ambientName) != null) {
      Ambient.get(ambientName).loop(1.0f, ClientSettings.soundVolume);
    }
    activeAmbient = ambientName;
  }

  public void loadAmbientIfNotLoaded(String ambientName) {
    if (ambientName!=null && Ambient.get(ambientName) == null) {
      try {
        Ambient.put(ambientName, new Sound("sfx/ambient/" + ambientName + ".ogg"));
      } catch (SlickException e) {
        e.printStackTrace();
      }
    }
  }

  public void loadSongIfNotLoaded(String songName) {
    if (!songName.equals("None")) {
      if (Songs.get(songName) == null) {
        MusicLoader musicLoader = new MusicLoader(Songs, songName);
        musicLoader.start();
      }
    }
  }

  public void updateVolume() {
    if (activeSong!=null && Songs.get(activeSong) != null) {
      Songs.get(activeSong).setVolume(ClientSettings.musicVolume);
      if (!Songs.get(activeSong).playing()) {
        Songs.get(activeSong).loop(1.0f, ClientSettings.musicVolume);
      }
    }
    if (activeAmbient!=null && Ambient.get(activeAmbient) != null) {
      Ambient.get(activeAmbient).stop();
      if (ClientSettings.soundVolume >= 0.1f) {
        Ambient.get(activeAmbient).loop(1.0f, ClientSettings.soundVolume * 0.2f);
      }
    }
  }
}
