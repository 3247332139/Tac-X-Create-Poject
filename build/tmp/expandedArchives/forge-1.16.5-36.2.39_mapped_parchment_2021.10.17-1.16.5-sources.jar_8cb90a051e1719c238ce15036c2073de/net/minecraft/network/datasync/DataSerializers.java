package net.minecraft.network.datasync;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Pose;
import net.minecraft.entity.merchant.villager.VillagerData;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.Direction;
import net.minecraft.util.IntIdentityHashBiMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Rotations;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;

/**
 * Registry for {@link EntityDataSerializer}.
 */
public class DataSerializers {
   private static final IntIdentityHashBiMap<IDataSerializer<?>> SERIALIZERS = new IntIdentityHashBiMap<>(16);
   public static final IDataSerializer<Byte> BYTE = new IDataSerializer<Byte>() {
      public void write(PacketBuffer pBuffer, Byte pValue) {
         pBuffer.writeByte(pValue);
      }

      public Byte read(PacketBuffer pBuffer) {
         return pBuffer.readByte();
      }

      public Byte copy(Byte pValue) {
         return pValue;
      }
   };
   public static final IDataSerializer<Integer> INT = new IDataSerializer<Integer>() {
      public void write(PacketBuffer pBuffer, Integer pValue) {
         pBuffer.writeVarInt(pValue);
      }

      public Integer read(PacketBuffer pBuffer) {
         return pBuffer.readVarInt();
      }

      public Integer copy(Integer pValue) {
         return pValue;
      }
   };
   public static final IDataSerializer<Float> FLOAT = new IDataSerializer<Float>() {
      public void write(PacketBuffer pBuffer, Float pValue) {
         pBuffer.writeFloat(pValue);
      }

      public Float read(PacketBuffer pBuffer) {
         return pBuffer.readFloat();
      }

      public Float copy(Float pValue) {
         return pValue;
      }
   };
   public static final IDataSerializer<String> STRING = new IDataSerializer<String>() {
      public void write(PacketBuffer pBuffer, String pValue) {
         pBuffer.writeUtf(pValue);
      }

      public String read(PacketBuffer pBuffer) {
         return pBuffer.readUtf(32767);
      }

      public String copy(String pValue) {
         return pValue;
      }
   };
   public static final IDataSerializer<ITextComponent> COMPONENT = new IDataSerializer<ITextComponent>() {
      public void write(PacketBuffer pBuffer, ITextComponent pValue) {
         pBuffer.writeComponent(pValue);
      }

      public ITextComponent read(PacketBuffer pBuffer) {
         return pBuffer.readComponent();
      }

      public ITextComponent copy(ITextComponent pValue) {
         return pValue;
      }
   };
   public static final IDataSerializer<Optional<ITextComponent>> OPTIONAL_COMPONENT = new IDataSerializer<Optional<ITextComponent>>() {
      public void write(PacketBuffer pBuffer, Optional<ITextComponent> pValue) {
         if (pValue.isPresent()) {
            pBuffer.writeBoolean(true);
            pBuffer.writeComponent(pValue.get());
         } else {
            pBuffer.writeBoolean(false);
         }

      }

      public Optional<ITextComponent> read(PacketBuffer pBuffer) {
         return pBuffer.readBoolean() ? Optional.of(pBuffer.readComponent()) : Optional.empty();
      }

      public Optional<ITextComponent> copy(Optional<ITextComponent> pValue) {
         return pValue;
      }
   };
   public static final IDataSerializer<ItemStack> ITEM_STACK = new IDataSerializer<ItemStack>() {
      public void write(PacketBuffer pBuffer, ItemStack pValue) {
         pBuffer.writeItem(pValue);
      }

      public ItemStack read(PacketBuffer pBuffer) {
         return pBuffer.readItem();
      }

      public ItemStack copy(ItemStack pValue) {
         return pValue.copy();
      }
   };
   public static final IDataSerializer<Optional<BlockState>> BLOCK_STATE = new IDataSerializer<Optional<BlockState>>() {
      public void write(PacketBuffer pBuffer, Optional<BlockState> pValue) {
         if (pValue.isPresent()) {
            pBuffer.writeVarInt(Block.getId(pValue.get()));
         } else {
            pBuffer.writeVarInt(0);
         }

      }

      public Optional<BlockState> read(PacketBuffer pBuffer) {
         int i = pBuffer.readVarInt();
         return i == 0 ? Optional.empty() : Optional.of(Block.stateById(i));
      }

      public Optional<BlockState> copy(Optional<BlockState> pValue) {
         return pValue;
      }
   };
   public static final IDataSerializer<Boolean> BOOLEAN = new IDataSerializer<Boolean>() {
      public void write(PacketBuffer pBuffer, Boolean pValue) {
         pBuffer.writeBoolean(pValue);
      }

      public Boolean read(PacketBuffer pBuffer) {
         return pBuffer.readBoolean();
      }

      public Boolean copy(Boolean pValue) {
         return pValue;
      }
   };
   public static final IDataSerializer<IParticleData> PARTICLE = new IDataSerializer<IParticleData>() {
      public void write(PacketBuffer pBuffer, IParticleData pValue) {
         pBuffer.writeVarInt(Registry.PARTICLE_TYPE.getId(pValue.getType()));
         pValue.writeToNetwork(pBuffer);
      }

      public IParticleData read(PacketBuffer pBuffer) {
         return this.readParticle(pBuffer, Registry.PARTICLE_TYPE.byId(pBuffer.readVarInt()));
      }

      private <T extends IParticleData> T readParticle(PacketBuffer p_200543_1_, ParticleType<T> p_200543_2_) {
         return p_200543_2_.getDeserializer().fromNetwork(p_200543_2_, p_200543_1_);
      }

      public IParticleData copy(IParticleData pValue) {
         return pValue;
      }
   };
   public static final IDataSerializer<Rotations> ROTATIONS = new IDataSerializer<Rotations>() {
      public void write(PacketBuffer pBuffer, Rotations pValue) {
         pBuffer.writeFloat(pValue.getX());
         pBuffer.writeFloat(pValue.getY());
         pBuffer.writeFloat(pValue.getZ());
      }

      public Rotations read(PacketBuffer pBuffer) {
         return new Rotations(pBuffer.readFloat(), pBuffer.readFloat(), pBuffer.readFloat());
      }

      public Rotations copy(Rotations pValue) {
         return pValue;
      }
   };
   public static final IDataSerializer<BlockPos> BLOCK_POS = new IDataSerializer<BlockPos>() {
      public void write(PacketBuffer pBuffer, BlockPos pValue) {
         pBuffer.writeBlockPos(pValue);
      }

      public BlockPos read(PacketBuffer pBuffer) {
         return pBuffer.readBlockPos();
      }

      public BlockPos copy(BlockPos pValue) {
         return pValue;
      }
   };
   public static final IDataSerializer<Optional<BlockPos>> OPTIONAL_BLOCK_POS = new IDataSerializer<Optional<BlockPos>>() {
      public void write(PacketBuffer pBuffer, Optional<BlockPos> pValue) {
         pBuffer.writeBoolean(pValue.isPresent());
         if (pValue.isPresent()) {
            pBuffer.writeBlockPos(pValue.get());
         }

      }

      public Optional<BlockPos> read(PacketBuffer pBuffer) {
         return !pBuffer.readBoolean() ? Optional.empty() : Optional.of(pBuffer.readBlockPos());
      }

      public Optional<BlockPos> copy(Optional<BlockPos> pValue) {
         return pValue;
      }
   };
   public static final IDataSerializer<Direction> DIRECTION = new IDataSerializer<Direction>() {
      public void write(PacketBuffer pBuffer, Direction pValue) {
         pBuffer.writeEnum(pValue);
      }

      public Direction read(PacketBuffer pBuffer) {
         return pBuffer.readEnum(Direction.class);
      }

      public Direction copy(Direction pValue) {
         return pValue;
      }
   };
   public static final IDataSerializer<Optional<UUID>> OPTIONAL_UUID = new IDataSerializer<Optional<UUID>>() {
      public void write(PacketBuffer pBuffer, Optional<UUID> pValue) {
         pBuffer.writeBoolean(pValue.isPresent());
         if (pValue.isPresent()) {
            pBuffer.writeUUID(pValue.get());
         }

      }

      public Optional<UUID> read(PacketBuffer pBuffer) {
         return !pBuffer.readBoolean() ? Optional.empty() : Optional.of(pBuffer.readUUID());
      }

      public Optional<UUID> copy(Optional<UUID> pValue) {
         return pValue;
      }
   };
   public static final IDataSerializer<CompoundNBT> COMPOUND_TAG = new IDataSerializer<CompoundNBT>() {
      public void write(PacketBuffer pBuffer, CompoundNBT pValue) {
         pBuffer.writeNbt(pValue);
      }

      public CompoundNBT read(PacketBuffer pBuffer) {
         return pBuffer.readNbt();
      }

      public CompoundNBT copy(CompoundNBT pValue) {
         return pValue.copy();
      }
   };
   public static final IDataSerializer<VillagerData> VILLAGER_DATA = new IDataSerializer<VillagerData>() {
      public void write(PacketBuffer pBuffer, VillagerData pValue) {
         pBuffer.writeVarInt(Registry.VILLAGER_TYPE.getId(pValue.getType()));
         pBuffer.writeVarInt(Registry.VILLAGER_PROFESSION.getId(pValue.getProfession()));
         pBuffer.writeVarInt(pValue.getLevel());
      }

      public VillagerData read(PacketBuffer pBuffer) {
         return new VillagerData(Registry.VILLAGER_TYPE.byId(pBuffer.readVarInt()), Registry.VILLAGER_PROFESSION.byId(pBuffer.readVarInt()), pBuffer.readVarInt());
      }

      public VillagerData copy(VillagerData pValue) {
         return pValue;
      }
   };
   public static final IDataSerializer<OptionalInt> OPTIONAL_UNSIGNED_INT = new IDataSerializer<OptionalInt>() {
      public void write(PacketBuffer pBuffer, OptionalInt pValue) {
         pBuffer.writeVarInt(pValue.orElse(-1) + 1);
      }

      public OptionalInt read(PacketBuffer pBuffer) {
         int i = pBuffer.readVarInt();
         return i == 0 ? OptionalInt.empty() : OptionalInt.of(i - 1);
      }

      public OptionalInt copy(OptionalInt pValue) {
         return pValue;
      }
   };
   public static final IDataSerializer<Pose> POSE = new IDataSerializer<Pose>() {
      public void write(PacketBuffer pBuffer, Pose pValue) {
         pBuffer.writeEnum(pValue);
      }

      public Pose read(PacketBuffer pBuffer) {
         return pBuffer.readEnum(Pose.class);
      }

      public Pose copy(Pose pValue) {
         return pValue;
      }
   };

   public static void registerSerializer(IDataSerializer<?> pSerializer) {
      if (SERIALIZERS.add(pSerializer) >= 256) throw new RuntimeException("Vanilla DataSerializer ID limit exceeded");
   }

   @Nullable
   public static IDataSerializer<?> getSerializer(int pId) {
      return net.minecraftforge.common.ForgeHooks.getSerializer(pId, SERIALIZERS);
   }

   public static int getSerializedId(IDataSerializer<?> pSerializer) {
      return net.minecraftforge.common.ForgeHooks.getSerializerId(pSerializer, SERIALIZERS);
   }

   static {
      registerSerializer(BYTE);
      registerSerializer(INT);
      registerSerializer(FLOAT);
      registerSerializer(STRING);
      registerSerializer(COMPONENT);
      registerSerializer(OPTIONAL_COMPONENT);
      registerSerializer(ITEM_STACK);
      registerSerializer(BOOLEAN);
      registerSerializer(ROTATIONS);
      registerSerializer(BLOCK_POS);
      registerSerializer(OPTIONAL_BLOCK_POS);
      registerSerializer(DIRECTION);
      registerSerializer(OPTIONAL_UUID);
      registerSerializer(BLOCK_STATE);
      registerSerializer(COMPOUND_TAG);
      registerSerializer(PARTICLE);
      registerSerializer(VILLAGER_DATA);
      registerSerializer(OPTIONAL_UNSIGNED_INT);
      registerSerializer(POSE);
   }
}
