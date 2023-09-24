package net.minecraft.util.text;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.EntitySelector;
import net.minecraft.command.arguments.EntitySelectorParser;
import net.minecraft.entity.Entity;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;

/**
 * A Component that shows the score for an entity which is selected by an {@link EntitySelector}.
 */
public class ScoreTextComponent extends TextComponent implements ITargetedTextComponent {
   private final String name;
   @Nullable
   private final EntitySelector selector;
   private final String objective;

   @Nullable
   private static EntitySelector parseSelector(String pEntitySelector) {
      try {
         return (new EntitySelectorParser(new StringReader(pEntitySelector))).parse();
      } catch (CommandSyntaxException commandsyntaxexception) {
         return null;
      }
   }

   public ScoreTextComponent(String pEntitySelector, String pObjective) {
      this(pEntitySelector, parseSelector(pEntitySelector), pObjective);
   }

   private ScoreTextComponent(String pName, @Nullable EntitySelector pSelector, String pObjective) {
      this.name = pName;
      this.selector = pSelector;
      this.objective = pObjective;
   }

   /**
    * Gets the name of the entity who owns this score.
    */
   public String getName() {
      return this.name;
   }

   /**
    * Gets the name of the objective for this score.
    */
   public String getObjective() {
      return this.objective;
   }

   private String findTargetName(CommandSource pCommandSourceStack) throws CommandSyntaxException {
      if (this.selector != null) {
         List<? extends Entity> list = this.selector.findEntities(pCommandSourceStack);
         if (!list.isEmpty()) {
            if (list.size() != 1) {
               throw EntityArgument.ERROR_NOT_SINGLE_ENTITY.create();
            }

            return list.get(0).getScoreboardName();
         }
      }

      return this.name;
   }

   private String getScore(String pUsername, CommandSource pCommandSourceStack) {
      MinecraftServer minecraftserver = pCommandSourceStack.getServer();
      if (minecraftserver != null) {
         Scoreboard scoreboard = minecraftserver.getScoreboard();
         ScoreObjective scoreobjective = scoreboard.getObjective(this.objective);
         if (scoreboard.hasPlayerScore(pUsername, scoreobjective)) {
            Score score = scoreboard.getOrCreatePlayerScore(pUsername, scoreobjective);
            return Integer.toString(score.getScore());
         }
      }

      return "";
   }

   /**
    * Creates a copy of this component, losing any style or siblings.
    */
   public ScoreTextComponent plainCopy() {
      return new ScoreTextComponent(this.name, this.selector, this.objective);
   }

   public IFormattableTextComponent resolve(@Nullable CommandSource pCommandSourceStack, @Nullable Entity pEntity, int pRecursionDepth) throws CommandSyntaxException {
      if (pCommandSourceStack == null) {
         return new StringTextComponent("");
      } else {
         String s = this.findTargetName(pCommandSourceStack);
         String s1 = pEntity != null && s.equals("*") ? pEntity.getScoreboardName() : s;
         return new StringTextComponent(this.getScore(s1, pCommandSourceStack));
      }
   }

   public boolean equals(Object p_equals_1_) {
      if (this == p_equals_1_) {
         return true;
      } else if (!(p_equals_1_ instanceof ScoreTextComponent)) {
         return false;
      } else {
         ScoreTextComponent scoretextcomponent = (ScoreTextComponent)p_equals_1_;
         return this.name.equals(scoretextcomponent.name) && this.objective.equals(scoretextcomponent.objective) && super.equals(p_equals_1_);
      }
   }

   public String toString() {
      return "ScoreComponent{name='" + this.name + '\'' + "objective='" + this.objective + '\'' + ", siblings=" + this.siblings + ", style=" + this.getStyle() + '}';
   }
}