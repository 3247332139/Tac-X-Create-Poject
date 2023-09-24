package net.minecraft.command.impl;

import com.google.common.collect.Sets;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.Set;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.util.text.TranslationTextComponent;

public class TagCommand {
   private static final SimpleCommandExceptionType ERROR_ADD_FAILED = new SimpleCommandExceptionType(new TranslationTextComponent("commands.tag.add.failed"));
   private static final SimpleCommandExceptionType ERROR_REMOVE_FAILED = new SimpleCommandExceptionType(new TranslationTextComponent("commands.tag.remove.failed"));

   public static void register(CommandDispatcher<CommandSource> pDispatcher) {
      pDispatcher.register(Commands.literal("tag").requires((p_198751_0_) -> {
         return p_198751_0_.hasPermission(2);
      }).then(Commands.argument("targets", EntityArgument.entities()).then(Commands.literal("add").then(Commands.argument("name", StringArgumentType.word()).executes((p_198746_0_) -> {
         return addTag(p_198746_0_.getSource(), EntityArgument.getEntities(p_198746_0_, "targets"), StringArgumentType.getString(p_198746_0_, "name"));
      }))).then(Commands.literal("remove").then(Commands.argument("name", StringArgumentType.word()).suggests((p_198745_0_, p_198745_1_) -> {
         return ISuggestionProvider.suggest(getTags(EntityArgument.getEntities(p_198745_0_, "targets")), p_198745_1_);
      }).executes((p_198742_0_) -> {
         return removeTag(p_198742_0_.getSource(), EntityArgument.getEntities(p_198742_0_, "targets"), StringArgumentType.getString(p_198742_0_, "name"));
      }))).then(Commands.literal("list").executes((p_198747_0_) -> {
         return listTags(p_198747_0_.getSource(), EntityArgument.getEntities(p_198747_0_, "targets"));
      }))));
   }

   /**
    * Gets all tags that are present on at least one of the given entities.
    */
   private static Collection<String> getTags(Collection<? extends Entity> pEntities) {
      Set<String> set = Sets.newHashSet();

      for(Entity entity : pEntities) {
         set.addAll(entity.getTags());
      }

      return set;
   }

   private static int addTag(CommandSource pSource, Collection<? extends Entity> pEntities, String pTagName) throws CommandSyntaxException {
      int i = 0;

      for(Entity entity : pEntities) {
         if (entity.addTag(pTagName)) {
            ++i;
         }
      }

      if (i == 0) {
         throw ERROR_ADD_FAILED.create();
      } else {
         if (pEntities.size() == 1) {
            pSource.sendSuccess(new TranslationTextComponent("commands.tag.add.success.single", pTagName, pEntities.iterator().next().getDisplayName()), true);
         } else {
            pSource.sendSuccess(new TranslationTextComponent("commands.tag.add.success.multiple", pTagName, pEntities.size()), true);
         }

         return i;
      }
   }

   private static int removeTag(CommandSource pSource, Collection<? extends Entity> pEntities, String pTagName) throws CommandSyntaxException {
      int i = 0;

      for(Entity entity : pEntities) {
         if (entity.removeTag(pTagName)) {
            ++i;
         }
      }

      if (i == 0) {
         throw ERROR_REMOVE_FAILED.create();
      } else {
         if (pEntities.size() == 1) {
            pSource.sendSuccess(new TranslationTextComponent("commands.tag.remove.success.single", pTagName, pEntities.iterator().next().getDisplayName()), true);
         } else {
            pSource.sendSuccess(new TranslationTextComponent("commands.tag.remove.success.multiple", pTagName, pEntities.size()), true);
         }

         return i;
      }
   }

   private static int listTags(CommandSource pSource, Collection<? extends Entity> pEntities) {
      Set<String> set = Sets.newHashSet();

      for(Entity entity : pEntities) {
         set.addAll(entity.getTags());
      }

      if (pEntities.size() == 1) {
         Entity entity1 = pEntities.iterator().next();
         if (set.isEmpty()) {
            pSource.sendSuccess(new TranslationTextComponent("commands.tag.list.single.empty", entity1.getDisplayName()), false);
         } else {
            pSource.sendSuccess(new TranslationTextComponent("commands.tag.list.single.success", entity1.getDisplayName(), set.size(), TextComponentUtils.formatList(set)), false);
         }
      } else if (set.isEmpty()) {
         pSource.sendSuccess(new TranslationTextComponent("commands.tag.list.multiple.empty", pEntities.size()), false);
      } else {
         pSource.sendSuccess(new TranslationTextComponent("commands.tag.list.multiple.success", pEntities.size(), set.size(), TextComponentUtils.formatList(set)), false);
      }

      return set.size();
   }
}