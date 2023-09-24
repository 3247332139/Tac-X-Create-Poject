package net.minecraft.network.play.server;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import javax.annotation.Nullable;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.ArgumentTypes;
import net.minecraft.command.arguments.SuggestionProviders;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;

public class SCommandListPacket implements IPacket<IClientPlayNetHandler> {
   private RootCommandNode<ISuggestionProvider> root;

   public SCommandListPacket() {
   }

   public SCommandListPacket(RootCommandNode<ISuggestionProvider> pRoot) {
      this.root = pRoot;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      SCommandListPacket.Entry[] ascommandlistpacket$entry = new SCommandListPacket.Entry[p_148837_1_.readVarInt()];

      for(int i = 0; i < ascommandlistpacket$entry.length; ++i) {
         ascommandlistpacket$entry[i] = readNode(p_148837_1_);
      }

      resolveEntries(ascommandlistpacket$entry);
      this.root = (RootCommandNode)ascommandlistpacket$entry[p_148837_1_.readVarInt()].node;
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      Object2IntMap<CommandNode<ISuggestionProvider>> object2intmap = enumerateNodes(this.root);
      CommandNode<ISuggestionProvider>[] commandnode = getNodesInIdOrder(object2intmap);
      pBuffer.writeVarInt(commandnode.length);

      for(CommandNode<ISuggestionProvider> commandnode1 : commandnode) {
         writeNode(pBuffer, commandnode1, object2intmap);
      }

      pBuffer.writeVarInt(object2intmap.get(this.root));
   }

   private static void resolveEntries(SCommandListPacket.Entry[] p_244294_0_) {
      List<SCommandListPacket.Entry> list = Lists.newArrayList(p_244294_0_);

      while(!list.isEmpty()) {
         boolean flag = list.removeIf((p_244295_1_) -> {
            return p_244295_1_.build(p_244294_0_);
         });
         if (!flag) {
            throw new IllegalStateException("Server sent an impossible command tree");
         }
      }

   }

   private static Object2IntMap<CommandNode<ISuggestionProvider>> enumerateNodes(RootCommandNode<ISuggestionProvider> pRootNode) {
      Object2IntMap<CommandNode<ISuggestionProvider>> object2intmap = new Object2IntOpenHashMap<>();
      Queue<CommandNode<ISuggestionProvider>> queue = Queues.newArrayDeque();
      queue.add(pRootNode);

      CommandNode<ISuggestionProvider> commandnode;
      while((commandnode = queue.poll()) != null) {
         if (!object2intmap.containsKey(commandnode)) {
            int i = object2intmap.size();
            object2intmap.put(commandnode, i);
            queue.addAll(commandnode.getChildren());
            if (commandnode.getRedirect() != null) {
               queue.add(commandnode.getRedirect());
            }
         }
      }

      return object2intmap;
   }

   private static CommandNode<ISuggestionProvider>[] getNodesInIdOrder(Object2IntMap<CommandNode<ISuggestionProvider>> p_244293_0_) {
      CommandNode<ISuggestionProvider>[] commandnode = new CommandNode[p_244293_0_.size()];

      for(Object2IntMap.Entry<CommandNode<ISuggestionProvider>> entry : Object2IntMaps.fastIterable(p_244293_0_)) {
         commandnode[entry.getIntValue()] = entry.getKey();
      }

      return commandnode;
   }

   private static SCommandListPacket.Entry readNode(PacketBuffer pBuffer) {
      byte b0 = pBuffer.readByte();
      int[] aint = pBuffer.readVarIntArray();
      int i = (b0 & 8) != 0 ? pBuffer.readVarInt() : 0;
      ArgumentBuilder<ISuggestionProvider, ?> argumentbuilder = createBuilder(pBuffer, b0);
      return new SCommandListPacket.Entry(argumentbuilder, b0, i, aint);
   }

   @Nullable
   private static ArgumentBuilder<ISuggestionProvider, ?> createBuilder(PacketBuffer pBuffer, byte pFlags) {
      int i = pFlags & 3;
      if (i == 2) {
         String s = pBuffer.readUtf(32767);
         ArgumentType<?> argumenttype = ArgumentTypes.deserialize(pBuffer);
         if (argumenttype == null) {
            return null;
         } else {
            RequiredArgumentBuilder<ISuggestionProvider, ?> requiredargumentbuilder = RequiredArgumentBuilder.argument(s, argumenttype);
            if ((pFlags & 16) != 0) {
               requiredargumentbuilder.suggests(SuggestionProviders.getProvider(pBuffer.readResourceLocation()));
            }

            return requiredargumentbuilder;
         }
      } else {
         return i == 1 ? LiteralArgumentBuilder.literal(pBuffer.readUtf(32767)) : null;
      }
   }

   private static void writeNode(PacketBuffer pBuffer, CommandNode<ISuggestionProvider> pNode, Map<CommandNode<ISuggestionProvider>, Integer> pNodeIds) {
      byte b0 = 0;
      if (pNode.getRedirect() != null) {
         b0 = (byte)(b0 | 8);
      }

      if (pNode.getCommand() != null) {
         b0 = (byte)(b0 | 4);
      }

      if (pNode instanceof RootCommandNode) {
         b0 = (byte)(b0 | 0);
      } else if (pNode instanceof ArgumentCommandNode) {
         b0 = (byte)(b0 | 2);
         if (((ArgumentCommandNode)pNode).getCustomSuggestions() != null) {
            b0 = (byte)(b0 | 16);
         }
      } else {
         if (!(pNode instanceof LiteralCommandNode)) {
            throw new UnsupportedOperationException("Unknown node type " + pNode);
         }

         b0 = (byte)(b0 | 1);
      }

      pBuffer.writeByte(b0);
      pBuffer.writeVarInt(pNode.getChildren().size());

      for(CommandNode<ISuggestionProvider> commandnode : pNode.getChildren()) {
         pBuffer.writeVarInt(pNodeIds.get(commandnode));
      }

      if (pNode.getRedirect() != null) {
         pBuffer.writeVarInt(pNodeIds.get(pNode.getRedirect()));
      }

      if (pNode instanceof ArgumentCommandNode) {
         ArgumentCommandNode<ISuggestionProvider, ?> argumentcommandnode = (ArgumentCommandNode)pNode;
         pBuffer.writeUtf(argumentcommandnode.getName());
         ArgumentTypes.serialize(pBuffer, argumentcommandnode.getType());
         if (argumentcommandnode.getCustomSuggestions() != null) {
            pBuffer.writeResourceLocation(SuggestionProviders.getName(argumentcommandnode.getCustomSuggestions()));
         }
      } else if (pNode instanceof LiteralCommandNode) {
         pBuffer.writeUtf(((LiteralCommandNode)pNode).getLiteral());
      }

   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleCommands(this);
   }

   public RootCommandNode<ISuggestionProvider> getRoot() {
      return this.root;
   }

   static class Entry {
      @Nullable
      private final ArgumentBuilder<ISuggestionProvider, ?> builder;
      private final byte flags;
      private final int redirect;
      private final int[] children;
      @Nullable
      private CommandNode<ISuggestionProvider> node;

      private Entry(@Nullable ArgumentBuilder<ISuggestionProvider, ?> pBuilder, byte pFlags, int pRedirect, int[] pChildren) {
         this.builder = pBuilder;
         this.flags = pFlags;
         this.redirect = pRedirect;
         this.children = pChildren;
      }

      public boolean build(SCommandListPacket.Entry[] p_197723_1_) {
         if (this.node == null) {
            if (this.builder == null) {
               this.node = new RootCommandNode<>();
            } else {
               if ((this.flags & 8) != 0) {
                  if (p_197723_1_[this.redirect].node == null) {
                     return false;
                  }

                  this.builder.redirect(p_197723_1_[this.redirect].node);
               }

               if ((this.flags & 4) != 0) {
                  this.builder.executes((p_197724_0_) -> {
                     return 0;
                  });
               }

               this.node = this.builder.build();
            }
         }

         for(int i : this.children) {
            if (p_197723_1_[i].node == null) {
               return false;
            }
         }

         for(int j : this.children) {
            CommandNode<ISuggestionProvider> commandnode = p_197723_1_[j].node;
            if (!(commandnode instanceof RootCommandNode)) {
               this.node.addChild(commandnode);
            }
         }

         return true;
      }
   }
}