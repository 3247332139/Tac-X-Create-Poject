package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.IServerPlayNetHandler;
import net.minecraft.state.properties.StructureMode;
import net.minecraft.tileentity.StructureBlockTileEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CUpdateStructureBlockPacket implements IPacket<IServerPlayNetHandler> {
   private BlockPos pos;
   private StructureBlockTileEntity.UpdateCommand updateType;
   private StructureMode mode;
   private String name;
   private BlockPos offset;
   private BlockPos size;
   private Mirror mirror;
   private Rotation rotation;
   private String data;
   private boolean ignoreEntities;
   private boolean showAir;
   private boolean showBoundingBox;
   private float integrity;
   private long seed;

   public CUpdateStructureBlockPacket() {
   }

   @OnlyIn(Dist.CLIENT)
   public CUpdateStructureBlockPacket(BlockPos p_i49541_1_, StructureBlockTileEntity.UpdateCommand p_i49541_2_, StructureMode p_i49541_3_, String p_i49541_4_, BlockPos p_i49541_5_, BlockPos p_i49541_6_, Mirror p_i49541_7_, Rotation p_i49541_8_, String p_i49541_9_, boolean p_i49541_10_, boolean p_i49541_11_, boolean p_i49541_12_, float p_i49541_13_, long p_i49541_14_) {
      this.pos = p_i49541_1_;
      this.updateType = p_i49541_2_;
      this.mode = p_i49541_3_;
      this.name = p_i49541_4_;
      this.offset = p_i49541_5_;
      this.size = p_i49541_6_;
      this.mirror = p_i49541_7_;
      this.rotation = p_i49541_8_;
      this.data = p_i49541_9_;
      this.ignoreEntities = p_i49541_10_;
      this.showAir = p_i49541_11_;
      this.showBoundingBox = p_i49541_12_;
      this.integrity = p_i49541_13_;
      this.seed = p_i49541_14_;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.pos = p_148837_1_.readBlockPos();
      this.updateType = p_148837_1_.readEnum(StructureBlockTileEntity.UpdateCommand.class);
      this.mode = p_148837_1_.readEnum(StructureMode.class);
      this.name = p_148837_1_.readUtf(32767);
      int i = 48;
      this.offset = new BlockPos(MathHelper.clamp(p_148837_1_.readByte(), -48, 48), MathHelper.clamp(p_148837_1_.readByte(), -48, 48), MathHelper.clamp(p_148837_1_.readByte(), -48, 48));
      int j = 48;
      this.size = new BlockPos(MathHelper.clamp(p_148837_1_.readByte(), 0, 48), MathHelper.clamp(p_148837_1_.readByte(), 0, 48), MathHelper.clamp(p_148837_1_.readByte(), 0, 48));
      this.mirror = p_148837_1_.readEnum(Mirror.class);
      this.rotation = p_148837_1_.readEnum(Rotation.class);
      this.data = p_148837_1_.readUtf(12);
      this.integrity = MathHelper.clamp(p_148837_1_.readFloat(), 0.0F, 1.0F);
      this.seed = p_148837_1_.readVarLong();
      int k = p_148837_1_.readByte();
      this.ignoreEntities = (k & 1) != 0;
      this.showAir = (k & 2) != 0;
      this.showBoundingBox = (k & 4) != 0;
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeBlockPos(this.pos);
      pBuffer.writeEnum(this.updateType);
      pBuffer.writeEnum(this.mode);
      pBuffer.writeUtf(this.name);
      pBuffer.writeByte(this.offset.getX());
      pBuffer.writeByte(this.offset.getY());
      pBuffer.writeByte(this.offset.getZ());
      pBuffer.writeByte(this.size.getX());
      pBuffer.writeByte(this.size.getY());
      pBuffer.writeByte(this.size.getZ());
      pBuffer.writeEnum(this.mirror);
      pBuffer.writeEnum(this.rotation);
      pBuffer.writeUtf(this.data);
      pBuffer.writeFloat(this.integrity);
      pBuffer.writeVarLong(this.seed);
      int i = 0;
      if (this.ignoreEntities) {
         i |= 1;
      }

      if (this.showAir) {
         i |= 2;
      }

      if (this.showBoundingBox) {
         i |= 4;
      }

      pBuffer.writeByte(i);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IServerPlayNetHandler pHandler) {
      pHandler.handleSetStructureBlock(this);
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public StructureBlockTileEntity.UpdateCommand getUpdateType() {
      return this.updateType;
   }

   public StructureMode getMode() {
      return this.mode;
   }

   public String getName() {
      return this.name;
   }

   public BlockPos getOffset() {
      return this.offset;
   }

   public BlockPos getSize() {
      return this.size;
   }

   public Mirror getMirror() {
      return this.mirror;
   }

   public Rotation getRotation() {
      return this.rotation;
   }

   public String getData() {
      return this.data;
   }

   public boolean isIgnoreEntities() {
      return this.ignoreEntities;
   }

   public boolean isShowAir() {
      return this.showAir;
   }

   public boolean isShowBoundingBox() {
      return this.showBoundingBox;
   }

   public float getIntegrity() {
      return this.integrity;
   }

   public long getSeed() {
      return this.seed;
   }
}