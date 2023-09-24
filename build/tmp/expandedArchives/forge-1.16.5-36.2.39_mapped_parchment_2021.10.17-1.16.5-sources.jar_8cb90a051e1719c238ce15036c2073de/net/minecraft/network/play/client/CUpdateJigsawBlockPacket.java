package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.IServerPlayNetHandler;
import net.minecraft.tileentity.JigsawTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CUpdateJigsawBlockPacket implements IPacket<IServerPlayNetHandler> {
   private BlockPos pos;
   private ResourceLocation name;
   private ResourceLocation target;
   private ResourceLocation pool;
   private String finalState;
   private JigsawTileEntity.OrientationType joint;

   public CUpdateJigsawBlockPacket() {
   }

   @OnlyIn(Dist.CLIENT)
   public CUpdateJigsawBlockPacket(BlockPos pPos, ResourceLocation pName, ResourceLocation pTarget, ResourceLocation pPool, String pFinalState, JigsawTileEntity.OrientationType pJoint) {
      this.pos = pPos;
      this.name = pName;
      this.target = pTarget;
      this.pool = pPool;
      this.finalState = pFinalState;
      this.joint = pJoint;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.pos = p_148837_1_.readBlockPos();
      this.name = p_148837_1_.readResourceLocation();
      this.target = p_148837_1_.readResourceLocation();
      this.pool = p_148837_1_.readResourceLocation();
      this.finalState = p_148837_1_.readUtf(32767);
      this.joint = JigsawTileEntity.OrientationType.byName(p_148837_1_.readUtf(32767)).orElse(JigsawTileEntity.OrientationType.ALIGNED);
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeBlockPos(this.pos);
      pBuffer.writeResourceLocation(this.name);
      pBuffer.writeResourceLocation(this.target);
      pBuffer.writeResourceLocation(this.pool);
      pBuffer.writeUtf(this.finalState);
      pBuffer.writeUtf(this.joint.getSerializedName());
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IServerPlayNetHandler pHandler) {
      pHandler.handleSetJigsawBlock(this);
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public ResourceLocation getName() {
      return this.name;
   }

   public ResourceLocation getTarget() {
      return this.target;
   }

   public ResourceLocation getPool() {
      return this.pool;
   }

   public String getFinalState() {
      return this.finalState;
   }

   public JigsawTileEntity.OrientationType getJoint() {
      return this.joint;
   }
}