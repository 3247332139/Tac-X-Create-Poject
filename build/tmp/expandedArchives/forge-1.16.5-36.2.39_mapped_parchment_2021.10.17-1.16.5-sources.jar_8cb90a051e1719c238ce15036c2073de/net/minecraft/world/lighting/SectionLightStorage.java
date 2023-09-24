package net.minecraft.world.lighting;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import javax.annotation.Nullable;
import net.minecraft.util.Direction;
import net.minecraft.util.SectionDistanceGraph;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.SectionPos;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.IChunkLightProvider;
import net.minecraft.world.chunk.NibbleArray;

public abstract class SectionLightStorage<M extends LightDataMap<M>> extends SectionDistanceGraph {
   protected static final NibbleArray EMPTY_DATA = new NibbleArray();
   private static final Direction[] DIRECTIONS = Direction.values();
   private final LightType layer;
   private final IChunkLightProvider chunkSource;
   /**
    * Section positions with blocks in them that can be affected by lighting. All neighbor sections can spread light
    * into them.
    */
   protected final LongSet dataSectionSet = new LongOpenHashSet();
   protected final LongSet toMarkNoData = new LongOpenHashSet();
   protected final LongSet toMarkData = new LongOpenHashSet();
   protected volatile M visibleSectionData;
   protected final M updatingSectionData;
   protected final LongSet changedSections = new LongOpenHashSet();
   protected final LongSet sectionsAffectedByLightUpdates = new LongOpenHashSet();
   protected final Long2ObjectMap<NibbleArray> queuedSections = Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap<>());
   private final LongSet untrustedSections = new LongOpenHashSet();
   /**
    * Section column positions (section positions with Y=0) that need to be kept even if some of their sections could
    * otherwise be removed.
    */
   private final LongSet columnsToRetainQueuedDataFor = new LongOpenHashSet();
   /** Set of section positions that can be removed, because their light won't affect any blocks. */
   private final LongSet toRemove = new LongOpenHashSet();
   protected volatile boolean hasToRemove;

   protected SectionLightStorage(LightType p_i51291_1_, IChunkLightProvider p_i51291_2_, M p_i51291_3_) {
      super(3, 16, 256);
      this.layer = p_i51291_1_;
      this.chunkSource = p_i51291_2_;
      this.updatingSectionData = p_i51291_3_;
      this.visibleSectionData = p_i51291_3_.copy();
      this.visibleSectionData.disableCache();
   }

   protected boolean storingLightForSection(long pSectionPos) {
      return this.getDataLayer(pSectionPos, true) != null;
   }

   @Nullable
   protected NibbleArray getDataLayer(long pSectionPos, boolean pCached) {
      return this.getDataLayer((M)(pCached ? this.updatingSectionData : this.visibleSectionData), pSectionPos);
   }

   @Nullable
   protected NibbleArray getDataLayer(M pMap, long pSectionPos) {
      return pMap.getLayer(pSectionPos);
   }

   @Nullable
   public NibbleArray getDataLayerData(long pSectionPos) {
      NibbleArray nibblearray = this.queuedSections.get(pSectionPos);
      return nibblearray != null ? nibblearray : this.getDataLayer(pSectionPos, false);
   }

   protected abstract int getLightValue(long pLevelPos);

   protected int getStoredLevel(long pLevelPos) {
      long i = SectionPos.blockToSection(pLevelPos);
      NibbleArray nibblearray = this.getDataLayer(i, true);
      return nibblearray.get(SectionPos.sectionRelative(BlockPos.getX(pLevelPos)), SectionPos.sectionRelative(BlockPos.getY(pLevelPos)), SectionPos.sectionRelative(BlockPos.getZ(pLevelPos)));
   }

   protected void setStoredLevel(long pLevelPos, int pLightLevel) {
      long i = SectionPos.blockToSection(pLevelPos);
      if (this.changedSections.add(i)) {
         this.updatingSectionData.copyDataLayer(i);
      }

      NibbleArray nibblearray = this.getDataLayer(i, true);
      nibblearray.set(SectionPos.sectionRelative(BlockPos.getX(pLevelPos)), SectionPos.sectionRelative(BlockPos.getY(pLevelPos)), SectionPos.sectionRelative(BlockPos.getZ(pLevelPos)), pLightLevel);

      for(int j = -1; j <= 1; ++j) {
         for(int k = -1; k <= 1; ++k) {
            for(int l = -1; l <= 1; ++l) {
               this.sectionsAffectedByLightUpdates.add(SectionPos.blockToSection(BlockPos.offset(pLevelPos, k, l, j)));
            }
         }
      }

   }

   protected int getLevel(long pSectionPos) {
      if (pSectionPos == Long.MAX_VALUE) {
         return 2;
      } else if (this.dataSectionSet.contains(pSectionPos)) {
         return 0;
      } else {
         return !this.toRemove.contains(pSectionPos) && this.updatingSectionData.hasLayer(pSectionPos) ? 1 : 2;
      }
   }

   protected int getLevelFromSource(long pPos) {
      if (this.toMarkNoData.contains(pPos)) {
         return 2;
      } else {
         return !this.dataSectionSet.contains(pPos) && !this.toMarkData.contains(pPos) ? 2 : 0;
      }
   }

   protected void setLevel(long pSectionPos, int pLevel) {
      int i = this.getLevel(pSectionPos);
      if (i != 0 && pLevel == 0) {
         this.dataSectionSet.add(pSectionPos);
         this.toMarkData.remove(pSectionPos);
      }

      if (i == 0 && pLevel != 0) {
         this.dataSectionSet.remove(pSectionPos);
         this.toMarkNoData.remove(pSectionPos);
      }

      if (i >= 2 && pLevel != 2) {
         if (this.toRemove.contains(pSectionPos)) {
            this.toRemove.remove(pSectionPos);
         } else {
            this.updatingSectionData.setLayer(pSectionPos, this.createDataLayer(pSectionPos));
            this.changedSections.add(pSectionPos);
            this.onNodeAdded(pSectionPos);

            for(int j = -1; j <= 1; ++j) {
               for(int k = -1; k <= 1; ++k) {
                  for(int l = -1; l <= 1; ++l) {
                     this.sectionsAffectedByLightUpdates.add(SectionPos.blockToSection(BlockPos.offset(pSectionPos, k, l, j)));
                  }
               }
            }
         }
      }

      if (i != 2 && pLevel >= 2) {
         this.toRemove.add(pSectionPos);
      }

      this.hasToRemove = !this.toRemove.isEmpty();
   }

   protected NibbleArray createDataLayer(long pSectionPos) {
      NibbleArray nibblearray = this.queuedSections.get(pSectionPos);
      return nibblearray != null ? nibblearray : new NibbleArray();
   }

   protected void clearQueuedSectionBlocks(LightEngine<?, ?> pEngine, long pSectionPos) {
      if (pEngine.getQueueSize() < 8192) {
         pEngine.removeIf((p_227469_2_) -> {
            return SectionPos.blockToSection(p_227469_2_) == pSectionPos;
         });
      } else {
         int i = SectionPos.sectionToBlockCoord(SectionPos.x(pSectionPos));
         int j = SectionPos.sectionToBlockCoord(SectionPos.y(pSectionPos));
         int k = SectionPos.sectionToBlockCoord(SectionPos.z(pSectionPos));

         for(int l = 0; l < 16; ++l) {
            for(int i1 = 0; i1 < 16; ++i1) {
               for(int j1 = 0; j1 < 16; ++j1) {
                  long k1 = BlockPos.asLong(i + l, j + i1, k + j1);
                  pEngine.removeFromQueue(k1);
               }
            }
         }

      }
   }

   protected boolean hasInconsistencies() {
      return this.hasToRemove;
   }

   protected void markNewInconsistencies(LightEngine<M, ?> pEngine, boolean pUpdateSkyLight, boolean pUpdateBlockLight) {
      if (this.hasInconsistencies() || !this.queuedSections.isEmpty()) {
         for(long i : this.toRemove) {
            this.clearQueuedSectionBlocks(pEngine, i);
            NibbleArray nibblearray = this.queuedSections.remove(i);
            NibbleArray nibblearray1 = this.updatingSectionData.removeLayer(i);
            if (this.columnsToRetainQueuedDataFor.contains(SectionPos.getZeroNode(i))) {
               if (nibblearray != null) {
                  this.queuedSections.put(i, nibblearray);
               } else if (nibblearray1 != null) {
                  this.queuedSections.put(i, nibblearray1);
               }
            }
         }

         this.updatingSectionData.clearCache();

         for(long k : this.toRemove) {
            this.onNodeRemoved(k);
         }

         this.toRemove.clear();
         this.hasToRemove = false;

         for(Entry<NibbleArray> entry : this.queuedSections.long2ObjectEntrySet()) {
            long j = entry.getLongKey();
            if (this.storingLightForSection(j)) {
               NibbleArray nibblearray2 = entry.getValue();
               if (this.updatingSectionData.getLayer(j) != nibblearray2) {
                  this.clearQueuedSectionBlocks(pEngine, j);
                  this.updatingSectionData.setLayer(j, nibblearray2);
                  this.changedSections.add(j);
               }
            }
         }

         this.updatingSectionData.clearCache();
         if (!pUpdateBlockLight) {
            for(long l : this.queuedSections.keySet()) {
               this.checkEdgesForSection(pEngine, l);
            }
         } else {
            for(long i1 : this.untrustedSections) {
               this.checkEdgesForSection(pEngine, i1);
            }
         }

         this.untrustedSections.clear();
         ObjectIterator<Entry<NibbleArray>> objectiterator = this.queuedSections.long2ObjectEntrySet().iterator();

         while(objectiterator.hasNext()) {
            Entry<NibbleArray> entry1 = objectiterator.next();
            long j1 = entry1.getLongKey();
            if (this.storingLightForSection(j1)) {
               objectiterator.remove();
            }
         }

      }
   }

   private void checkEdgesForSection(LightEngine<M, ?> p_241538_1_, long p_241538_2_) {
      if (this.storingLightForSection(p_241538_2_)) {
         int i = SectionPos.sectionToBlockCoord(SectionPos.x(p_241538_2_));
         int j = SectionPos.sectionToBlockCoord(SectionPos.y(p_241538_2_));
         int k = SectionPos.sectionToBlockCoord(SectionPos.z(p_241538_2_));

         for(Direction direction : DIRECTIONS) {
            long l = SectionPos.offset(p_241538_2_, direction);
            if (!this.queuedSections.containsKey(l) && this.storingLightForSection(l)) {
               for(int i1 = 0; i1 < 16; ++i1) {
                  for(int j1 = 0; j1 < 16; ++j1) {
                     long k1;
                     long l1;
                     switch(direction) {
                     case DOWN:
                        k1 = BlockPos.asLong(i + j1, j, k + i1);
                        l1 = BlockPos.asLong(i + j1, j - 1, k + i1);
                        break;
                     case UP:
                        k1 = BlockPos.asLong(i + j1, j + 16 - 1, k + i1);
                        l1 = BlockPos.asLong(i + j1, j + 16, k + i1);
                        break;
                     case NORTH:
                        k1 = BlockPos.asLong(i + i1, j + j1, k);
                        l1 = BlockPos.asLong(i + i1, j + j1, k - 1);
                        break;
                     case SOUTH:
                        k1 = BlockPos.asLong(i + i1, j + j1, k + 16 - 1);
                        l1 = BlockPos.asLong(i + i1, j + j1, k + 16);
                        break;
                     case WEST:
                        k1 = BlockPos.asLong(i, j + i1, k + j1);
                        l1 = BlockPos.asLong(i - 1, j + i1, k + j1);
                        break;
                     default:
                        k1 = BlockPos.asLong(i + 16 - 1, j + i1, k + j1);
                        l1 = BlockPos.asLong(i + 16, j + i1, k + j1);
                     }

                     p_241538_1_.checkEdge(k1, l1, p_241538_1_.computeLevelFromNeighbor(k1, l1, p_241538_1_.getLevel(k1)), false);
                     p_241538_1_.checkEdge(l1, k1, p_241538_1_.computeLevelFromNeighbor(l1, k1, p_241538_1_.getLevel(l1)), false);
                  }
               }
            }
         }

      }
   }

   protected void onNodeAdded(long pSectionPos) {
   }

   protected void onNodeRemoved(long p_215523_1_) {
   }

   protected void enableLightSources(long p_215526_1_, boolean p_215526_3_) {
   }

   public void retainData(long pSectionColumnPos, boolean pRetain) {
      if (pRetain) {
         this.columnsToRetainQueuedDataFor.add(pSectionColumnPos);
      } else {
         this.columnsToRetainQueuedDataFor.remove(pSectionColumnPos);
      }

   }

   protected void queueSectionData(long pSectionPos, @Nullable NibbleArray pArray, boolean p_215529_4_) {
      if (pArray != null) {
         this.queuedSections.put(pSectionPos, pArray);
         if (!p_215529_4_) {
            this.untrustedSections.add(pSectionPos);
         }
      } else {
         this.queuedSections.remove(pSectionPos);
      }

   }

   protected void updateSectionStatus(long pSectionPos, boolean pIsEmpty) {
      boolean flag = this.dataSectionSet.contains(pSectionPos);
      if (!flag && !pIsEmpty) {
         this.toMarkData.add(pSectionPos);
         this.checkEdge(Long.MAX_VALUE, pSectionPos, 0, true);
      }

      if (flag && pIsEmpty) {
         this.toMarkNoData.add(pSectionPos);
         this.checkEdge(Long.MAX_VALUE, pSectionPos, 2, false);
      }

   }

   protected void runAllUpdates() {
      if (this.hasWork()) {
         this.runUpdates(Integer.MAX_VALUE);
      }

   }

   protected void swapSectionMap() {
      if (!this.changedSections.isEmpty()) {
         M m = this.updatingSectionData.copy();
         m.disableCache();
         this.visibleSectionData = m;
         this.changedSections.clear();
      }

      if (!this.sectionsAffectedByLightUpdates.isEmpty()) {
         LongIterator longiterator = this.sectionsAffectedByLightUpdates.iterator();

         while(longiterator.hasNext()) {
            long i = longiterator.nextLong();
            this.chunkSource.onLightUpdate(this.layer, SectionPos.of(i));
         }

         this.sectionsAffectedByLightUpdates.clear();
      }

   }
}