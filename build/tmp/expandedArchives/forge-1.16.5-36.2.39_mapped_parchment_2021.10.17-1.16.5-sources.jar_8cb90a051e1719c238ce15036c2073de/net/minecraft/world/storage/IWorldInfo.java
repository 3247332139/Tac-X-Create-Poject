package net.minecraft.world.storage;

import net.minecraft.crash.CrashReportCategory;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;

public interface IWorldInfo {
   /**
    * Returns the x spawn position
    */
   int getXSpawn();

   /**
    * Return the Y axis spawning point of the player.
    */
   int getYSpawn();

   /**
    * Returns the z spawn position
    */
   int getZSpawn();

   float getSpawnAngle();

   long getGameTime();

   /**
    * Get current world time
    */
   long getDayTime();

   /**
    * Returns true if it is thundering, false otherwise.
    */
   boolean isThundering();

   /**
    * Returns true if it is raining, false otherwise.
    */
   boolean isRaining();

   /**
    * Sets whether it is raining or not.
    */
   void setRaining(boolean pIsRaining);

   /**
    * Returns true if hardcore mode is enabled, otherwise false
    */
   boolean isHardcore();

   /**
    * Gets the GameRules class Instance.
    */
   GameRules getGameRules();

   Difficulty getDifficulty();

   boolean isDifficultyLocked();

   default void fillCrashReportCategory(CrashReportCategory p_85118_1_) {
      p_85118_1_.setDetail("Level spawn location", () -> {
         return CrashReportCategory.formatLocation(this.getXSpawn(), this.getYSpawn(), this.getZSpawn());
      });
      p_85118_1_.setDetail("Level time", () -> {
         return String.format("%d game time, %d day time", this.getGameTime(), this.getDayTime());
      });
   }
}