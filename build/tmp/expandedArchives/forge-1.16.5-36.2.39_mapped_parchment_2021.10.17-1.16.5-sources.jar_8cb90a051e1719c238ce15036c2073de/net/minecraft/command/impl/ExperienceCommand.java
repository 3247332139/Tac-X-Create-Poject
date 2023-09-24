package net.minecraft.command.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.ToIntFunction;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TranslationTextComponent;

public class ExperienceCommand {
   private static final SimpleCommandExceptionType ERROR_SET_POINTS_INVALID = new SimpleCommandExceptionType(new TranslationTextComponent("commands.experience.set.points.invalid"));

   public static void register(CommandDispatcher<CommandSource> pDispatcher) {
      LiteralCommandNode<CommandSource> literalcommandnode = pDispatcher.register(Commands.literal("experience").requires((p_198442_0_) -> {
         return p_198442_0_.hasPermission(2);
      }).then(Commands.literal("add").then(Commands.argument("targets", EntityArgument.players()).then(Commands.argument("amount", IntegerArgumentType.integer()).executes((p_198445_0_) -> {
         return addExperience(p_198445_0_.getSource(), EntityArgument.getPlayers(p_198445_0_, "targets"), IntegerArgumentType.getInteger(p_198445_0_, "amount"), ExperienceCommand.Type.POINTS);
      }).then(Commands.literal("points").executes((p_198447_0_) -> {
         return addExperience(p_198447_0_.getSource(), EntityArgument.getPlayers(p_198447_0_, "targets"), IntegerArgumentType.getInteger(p_198447_0_, "amount"), ExperienceCommand.Type.POINTS);
      })).then(Commands.literal("levels").executes((p_198436_0_) -> {
         return addExperience(p_198436_0_.getSource(), EntityArgument.getPlayers(p_198436_0_, "targets"), IntegerArgumentType.getInteger(p_198436_0_, "amount"), ExperienceCommand.Type.LEVELS);
      }))))).then(Commands.literal("set").then(Commands.argument("targets", EntityArgument.players()).then(Commands.argument("amount", IntegerArgumentType.integer(0)).executes((p_198439_0_) -> {
         return setExperience(p_198439_0_.getSource(), EntityArgument.getPlayers(p_198439_0_, "targets"), IntegerArgumentType.getInteger(p_198439_0_, "amount"), ExperienceCommand.Type.POINTS);
      }).then(Commands.literal("points").executes((p_198444_0_) -> {
         return setExperience(p_198444_0_.getSource(), EntityArgument.getPlayers(p_198444_0_, "targets"), IntegerArgumentType.getInteger(p_198444_0_, "amount"), ExperienceCommand.Type.POINTS);
      })).then(Commands.literal("levels").executes((p_198440_0_) -> {
         return setExperience(p_198440_0_.getSource(), EntityArgument.getPlayers(p_198440_0_, "targets"), IntegerArgumentType.getInteger(p_198440_0_, "amount"), ExperienceCommand.Type.LEVELS);
      }))))).then(Commands.literal("query").then(Commands.argument("targets", EntityArgument.player()).then(Commands.literal("points").executes((p_198435_0_) -> {
         return queryExperience(p_198435_0_.getSource(), EntityArgument.getPlayer(p_198435_0_, "targets"), ExperienceCommand.Type.POINTS);
      })).then(Commands.literal("levels").executes((p_198446_0_) -> {
         return queryExperience(p_198446_0_.getSource(), EntityArgument.getPlayer(p_198446_0_, "targets"), ExperienceCommand.Type.LEVELS);
      })))));
      pDispatcher.register(Commands.literal("xp").requires((p_198441_0_) -> {
         return p_198441_0_.hasPermission(2);
      }).redirect(literalcommandnode));
   }

   private static int queryExperience(CommandSource pSource, ServerPlayerEntity pPlayer, ExperienceCommand.Type pType) {
      int i = pType.query.applyAsInt(pPlayer);
      pSource.sendSuccess(new TranslationTextComponent("commands.experience.query." + pType.name, pPlayer.getDisplayName(), i), false);
      return i;
   }

   private static int addExperience(CommandSource pSource, Collection<? extends ServerPlayerEntity> pTargets, int pAmount, ExperienceCommand.Type pType) {
      for(ServerPlayerEntity serverplayerentity : pTargets) {
         pType.add.accept(serverplayerentity, pAmount);
      }

      if (pTargets.size() == 1) {
         pSource.sendSuccess(new TranslationTextComponent("commands.experience.add." + pType.name + ".success.single", pAmount, pTargets.iterator().next().getDisplayName()), true);
      } else {
         pSource.sendSuccess(new TranslationTextComponent("commands.experience.add." + pType.name + ".success.multiple", pAmount, pTargets.size()), true);
      }

      return pTargets.size();
   }

   private static int setExperience(CommandSource pSource, Collection<? extends ServerPlayerEntity> pTargets, int pAmount, ExperienceCommand.Type pType) throws CommandSyntaxException {
      int i = 0;

      for(ServerPlayerEntity serverplayerentity : pTargets) {
         if (pType.set.test(serverplayerentity, pAmount)) {
            ++i;
         }
      }

      if (i == 0) {
         throw ERROR_SET_POINTS_INVALID.create();
      } else {
         if (pTargets.size() == 1) {
            pSource.sendSuccess(new TranslationTextComponent("commands.experience.set." + pType.name + ".success.single", pAmount, pTargets.iterator().next().getDisplayName()), true);
         } else {
            pSource.sendSuccess(new TranslationTextComponent("commands.experience.set." + pType.name + ".success.multiple", pAmount, pTargets.size()), true);
         }

         return pTargets.size();
      }
   }

   static enum Type {
      POINTS("points", PlayerEntity::giveExperiencePoints, (p_198424_0_, p_198424_1_) -> {
         if (p_198424_1_ >= p_198424_0_.getXpNeededForNextLevel()) {
            return false;
         } else {
            p_198424_0_.setExperiencePoints(p_198424_1_);
            return true;
         }
      }, (p_198422_0_) -> {
         return MathHelper.floor(p_198422_0_.experienceProgress * (float)p_198422_0_.getXpNeededForNextLevel());
      }),
      LEVELS("levels", ServerPlayerEntity::giveExperienceLevels, (p_198425_0_, p_198425_1_) -> {
         p_198425_0_.setExperienceLevels(p_198425_1_);
         return true;
      }, (p_198427_0_) -> {
         return p_198427_0_.experienceLevel;
      });

      public final BiConsumer<ServerPlayerEntity, Integer> add;
      public final BiPredicate<ServerPlayerEntity, Integer> set;
      public final String name;
      private final ToIntFunction<ServerPlayerEntity> query;

      private Type(String p_i48027_3_, BiConsumer<ServerPlayerEntity, Integer> p_i48027_4_, BiPredicate<ServerPlayerEntity, Integer> p_i48027_5_, ToIntFunction<ServerPlayerEntity> p_i48027_6_) {
         this.add = p_i48027_4_;
         this.name = p_i48027_3_;
         this.set = p_i48027_5_;
         this.query = p_i48027_6_;
      }
   }
}