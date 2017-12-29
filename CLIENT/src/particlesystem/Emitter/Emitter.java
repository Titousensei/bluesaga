package particlesystem.Emitter;

import particlesystem.Particle.Particle;
import particlesystem.Particle.ParticleType;
import particlesystem.Streak.StreakType;
import screens.Camera;
import screens.ScreenHandler;
import utils.RandomUtils;

import java.util.*;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Vector2f;

public class Emitter {

  Vector2f myPosition = new Vector2f();
  private float myCurrentLifetime;
  private boolean myShouldStopSpawn = false;
  private boolean myShouldBeRemoved = false;
  private float myTimeSinceLastSpawn;
  private float myTimeToSpawn;
  private boolean myIsPaused;
  private float myRotation;

  private List<Particle> myParticles = new ArrayList<>(10);

  private EmitterType myType = new EmitterType();
  private ParticleType myParticleType = new ParticleType();
  private StreakType myStreakType = new StreakType();
  private Random myRandom;

  public Emitter(float aXPos, float aYPos, EmitterType aEmitterType, ParticleType aParticleType) {

    myPosition.x = aXPos;
    myPosition.y = aYPos;

    myType = aEmitterType;

    myCurrentLifetime = 0.0f;
    myTimeSinceLastSpawn = 0.0f;
    myTimeToSpawn = GetType().myEmittionRate;
    myIsPaused = false;

    myRotation = 0.0f;

    myParticleType = aParticleType;

    if (myType.myShouldShowStreaks) {
      myStreakType = ScreenHandler.myStreakContainer.GetStreakByID(myType.myStreakID);
      myParticleType.myStreakType = myStreakType;
    }
    myParticleType.myShouldShow = myType.myShouldShowParticles;

    myRandom = new Random();
  }

  public void Update(float aElapsedTime) {

    if (myIsPaused == false) {
      myCurrentLifetime += aElapsedTime;
      myRotation += (GetType().myRotationSpeed * aElapsedTime);
    }

    if (myCurrentLifetime >= GetType().myLifetime) {
      if (GetType().myIsUnlimited == false) {
        myShouldStopSpawn = true;
      }
    }

    if (myShouldStopSpawn) {
      myTimeSinceLastSpawn += aElapsedTime;
    }

    if (myTimeSinceLastSpawn > myParticleType.myLifetime) {
      myShouldBeRemoved = true;
    }

    // Spawn particle
    if (myShouldStopSpawn == false) {

      if (myIsPaused == false) {
        myTimeToSpawn -= aElapsedTime;
      }

      if (myTimeToSpawn < 0.0f) {
        for (int i = 0; i < 10; ++i) {
          SpawnParticle();
        }
        myTimeToSpawn = GetType().myEmittionRate;
      }
    }

    // Update particles
    for (Particle particle : myParticles) {
      particle.Update(aElapsedTime);
    }

    // Remove particle
    Iterator<Particle> it = myParticles.iterator();
    while(it.hasNext()) {
      Particle pa = it.next();
      if (pa.ShouldBeRemoved()) {
        it.remove();
      }
    }
  }

  public void Render(Graphics g, Camera aCamera) {
    // Draw particles
    for (Particle p : myParticles) {
      p.Render(g, aCamera);
    }
  }

  private void SpawnParticle() {

    float x = RandomUtils.getFloat(myType.myMinPos.x, myType.myMaxPos.x);
    float y = RandomUtils.getFloat(myType.myMinPos.y, myType.myMaxPos.y);
    Vector2f spawnPos = new Vector2f(myPosition.x + x, myPosition.y + y);
    Particle particle = new Particle(spawnPos, myRotation, myParticleType);
    myParticles.add(particle);
  }

  public boolean ShouldBeRemoved() {
    return myShouldBeRemoved;
  }

  public void Remove() {
    myShouldBeRemoved = true;
  }

  public void Pause() {
    myIsPaused = true;
  }

  public void Start() {
    myIsPaused = false;
  }

  public void ToggleOnOff() {
    myIsPaused = !myIsPaused;
  }

  public void SetPosition(float aXPos, float aYPos) {
    myPosition.x = aXPos;
    myPosition.y = aYPos;
  }

  public EmitterType GetType() {
    return myType;
  }

  public ParticleType GetParticleType() {
    return myParticleType;
  }
}
