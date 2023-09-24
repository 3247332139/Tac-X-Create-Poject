package net.minecraft.world.chunk.storage;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.BiomeContainer;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.chunk.NibbleArray;

public class ChunkLoaderUtil {
   public static ChunkLoaderUtil.AnvilConverterData load(CompoundNBT pChunkData) {
      int i = pChunkData.getInt("xPos");
      int j = pChunkData.getInt("zPos");
      ChunkLoaderUtil.AnvilConverterData chunkloaderutil$anvilconverterdata = new ChunkLoaderUtil.AnvilConverterData(i, j);
      chunkloaderutil$anvilconverterdata.blocks = pChunkData.getByteArray("Blocks");
      chunkloaderutil$anvilconverterdata.data = new NibbleArrayReader(pChunkData.getByteArray("Data"), 7);
      chunkloaderutil$anvilconverterdata.skyLight = new NibbleArrayReader(pChunkData.getByteArray("SkyLight"), 7);
      chunkloaderutil$anvilconverterdata.blockLight = new NibbleArrayReader(pChunkData.getByteArray("BlockLight"), 7);
      chunkloaderutil$anvilconverterdata.heightmap = pChunkData.getByteArray("HeightMap");
      chunkloaderutil$anvilconverterdata.terrainPopulated = pChunkData.getBoolean("TerrainPopulated");
      chunkloaderutil$anvilconverterdata.entities = pChunkData.getList("Entities", 10);
      chunkloaderutil$anvilconverterdata.blockEntities = pChunkData.getList("TileEntities", 10);
      chunkloaderutil$anvilconverterdata.blockTicks = pChunkData.getList("TileTicks", 10);

      try {
         chunkloaderutil$anvilconverterdata.lastUpdated = pChunkData.getLong("LastUpdate");
      } catch (ClassCastException classcastexception) {
         chunkloaderutil$anvilconverterdata.lastUpdated = (long)pChunkData.getInt("LastUpdate");
      }

      return chunkloaderutil$anvilconverterdata;
   }

   public static void convertToAnvilFormat(DynamicRegistries.Impl pRegistryHolder, ChunkLoaderUtil.AnvilConverterData pOldChunk, CompoundNBT pNewChunkData, BiomeProvider pBiomeSource) {
      pNewChunkData.putInt("xPos", pOldChunk.x);
      pNewChunkData.putInt("zPos", pOldChunk.z);
      pNewChunkData.putLong("LastUpdate", pOldChunk.lastUpdated);
      int[] aint = new int[pOldChunk.heightmap.length];

      for(int i = 0; i < pOldChunk.heightmap.length; ++i) {
         aint[i] = pOldChunk.heightmap[i];
      }

      pNewChunkData.putIntArray("HeightMap", aint);
      pNewChunkData.putBoolean("TerrainPopulated", pOldChunk.terrainPopulated);
      ListNBT listnbt = new ListNBT();

      for(int j = 0; j < 8; ++j) {
         boolean flag = true;

         for(int k = 0; k < 16 && flag; ++k) {
            for(int l = 0; l < 16 && flag; ++l) {
               for(int i1 = 0; i1 < 16; ++i1) {
                  int j1 = k << 11 | i1 << 7 | l + (j << 4);
                  int k1 = pOldChunk.blocks[j1];
                  if (k1 != 0) {
                     flag = false;
                     break;
                  }
               }
            }
         }

         if (!flag) {
            byte[] abyte = new byte[4096];
            NibbleArray nibblearray = new NibbleArray();
            NibbleArray nibblearray1 = new NibbleArray();
            NibbleArray nibblearray2 = new NibbleArray();

            for(int l2 = 0; l2 < 16; ++l2) {
               for(int l1 = 0; l1 < 16; ++l1) {
                  for(int i2 = 0; i2 < 16; ++i2) {
                     int j2 = l2 << 11 | i2 << 7 | l1 + (j << 4);
                     int k2 = pOldChunk.blocks[j2];
                     abyte[l1 << 8 | i2 << 4 | l2] = (byte)(k2 & 255);
                     nibblearray.set(l2, l1, i2, pOldChunk.data.get(l2, l1 + (j << 4), i2));
                     nibblearray1.set(l2, l1, i2, pOldChunk.skyLight.get(l2, l1 + (j << 4), i2));
                     nibblearray2.set(l2, l1, i2, pOldChunk.blockLight.get(l2, l1 + (j << 4), i2));
                  }
               }
            }

            CompoundNBT compoundnbt = new CompoundNBT();
            compoundnbt.putByte("Y", (byte)(j & 255));
            compoundnbt.putByteArray("Blocks", abyte);
            compoundnbt.putByteArray("Data", nibblearray.getData());
            compoundnbt.putByteArray("SkyLight", nibblearray1.getData());
            compoundnbt.putByteArray("BlockLight", nibblearray2.getData());
            listnbt.add(compoundnbt);
         }
      }

      pNewChunkData.put("Sections", listnbt);
      pNewChunkData.putIntArray("Biomes", (new BiomeContainer(pRegistryHolder.registryOrThrow(Registry.BIOME_REGISTRY), new ChunkPos(pOldChunk.x, pOldChunk.z), pBiomeSource)).writeBiomes());
      pNewChunkData.put("Entities", pOldChunk.entities);
      pNewChunkData.put("TileEntities", pOldChunk.blockEntities);
      if (pOldChunk.blockTicks != null) {
         pNewChunkData.put("TileTicks", pOldChunk.blockTicks);
      }

      pNewChunkData.putBoolean("convertedFromAlphaFormat", true);
   }

   public static class AnvilConverterData {
      public long lastUpdated;
      public boolean terrainPopulated;
      public byte[] heightmap;
      public NibbleArrayReader blockLight;
      public NibbleArrayReader skyLight;
      public NibbleArrayReader data;
      public byte[] blocks;
      public ListNBT entities;
      public ListNBT blockEntities;
      public ListNBT blockTicks;
      public final int x;
      public final int z;

      public AnvilConverterData(int pX, int pZ) {
         this.x = pX;
         this.z = pZ;
      }
   }
}