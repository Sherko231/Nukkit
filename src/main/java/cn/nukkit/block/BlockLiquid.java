package cn.nukkit.block;

import cn.nukkit.entity.Entity;
import cn.nukkit.event.block.BlockFromToEvent;
import cn.nukkit.event.block.LiquidFlowEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;
import cn.nukkit.level.Level;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.protocol.LevelSoundEventPacket;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;

/**
 * @author MagicDroidX
 * Nukkit Project
 */
public abstract class BlockLiquid extends BlockTransparentMeta {

    protected static final byte CAN_FLOW_DOWN = 1;
    protected static final byte CAN_FLOW = 0;
    protected static final byte BLOCKED = -1;
    public int adjacentSources = 0;
    protected Vector3 flowVector = null;
    protected Long2ByteMap flowCostVisited = new Long2ByteOpenHashMap();

    protected BlockLiquid(int meta) {
        super(meta);
    }

    @Override
    public boolean canBeFlowedInto() {
        return true;
    }

    @Override
    protected AxisAlignedBB recalculateBoundingBox() {
        return null;
    }

    @Override
    public Item[] getDrops(Item item) {
        return new Item[0];
    }

    @Override
    public boolean hasEntityCollision() {
        return true;
    }

    @Override
    public boolean isBreakable(Item item) {
        return false;
    }

    @Override
    public boolean canBeReplaced() {
        return true;
    }

    @Override
    public boolean isSolid() {
        return false;
    }

    @Override
    public boolean canHarvestWithHand() {
        return false;
    }

    @Override
    public AxisAlignedBB getBoundingBox() {
        return null;
    }

    @Override
    public boolean breakWhenPushed() {
        return true;
    }

    @Override
    public double getMaxY() {
        return this.y + 1 - getFluidHeightPercent();
    }

    @Override
    protected AxisAlignedBB recalculateCollisionBoundingBox() {
        return this;
    }

    public float getFluidHeightPercent() {
        float d = (float) this.getDamage();
        if (d >= 8) {
            d = 0;
        }

        return (d + 1) / 9f;
    }

    protected int getFlowDecay(Block block) {
        if (block.getId() != this.getId()) {
            Block layer1 = block.getLevelBlock(BlockLayer.WATERLOGGED);
            if (layer1.getId() != this.getId()) {
                return -1;
            } else {
                return layer1.getDamage();
            }
        }
        return block.getDamage();
    }

    protected int getEffectiveFlowDecay(Block block) {
        if (block.getId() != this.getId()) {
            block = block.getLevelBlock(BlockLayer.WATERLOGGED);
            if (block.getId() != this.getId()) {
                return -1;
            }
        }
        int decay = block.getDamage();
        if (decay >= 8) {
            decay = 0;
        }
        return decay;
    }

    public void clearCaches() {
        this.flowVector = null;
        this.flowCostVisited.clear();
    }

    public Vector3 getFlowVector() {
        if (this.flowVector != null) {
            return this.flowVector;
        }
        Vector3 vector = new Vector3(0, 0, 0);
        int decay = this.getEffectiveFlowDecay(this);
        for (int j = 0; j < 4; ++j) {
            int x = (int) this.x;
            int y = (int) this.y;
            int z = (int) this.z;
            switch (j) {
                case 0:
                    --x;
                    break;
                case 1:
                    x++;
                    break;
                case 2:
                    z--;
                    break;
                default:
                    z++;
            }
            FullChunk chunk = this.level.getChunk(x >> 4, z >> 4);
            Block sideBlock = this.level.getBlock(chunk, x, y, z, true);
            int blockDecay = this.getEffectiveFlowDecay(sideBlock);
            if (blockDecay < 0) {
                if (!sideBlock.canBeFlowedInto()) {
                    continue;
                }
                blockDecay = this.getEffectiveFlowDecay(this.level.getBlock(chunk, x, y - 1, z, true));
                if (blockDecay >= 0) {
                    int realDecay = blockDecay - (decay - 8);
                    vector.x += (sideBlock.x - this.x) * realDecay;
                    vector.y += (sideBlock.y - this.y) * realDecay;
                    vector.z += (sideBlock.z - this.z) * realDecay;
                }
            } else {
                int realDecay = blockDecay - decay;
                vector.x += (sideBlock.x - this.x) * realDecay;
                vector.y += (sideBlock.y - this.y) * realDecay;
                vector.z += (sideBlock.z - this.z) * realDecay;
            }
        }
        if (this.getDamage() >= 8) {
            FullChunk guessChunk = getChunk();
            if (!this.canFlowInto(this.level.getBlock(guessChunk, (int) this.x, (int) this.y, (int) this.z - 1, true)) ||
                    !this.canFlowInto(this.level.getBlock(guessChunk, (int) this.x, (int) this.y, (int) this.z + 1, true)) ||
                    !this.canFlowInto(this.level.getBlock(guessChunk, (int) this.x - 1, (int) this.y, (int) this.z, true)) ||
                    !this.canFlowInto(this.level.getBlock(guessChunk, (int) this.x + 1, (int) this.y, (int) this.z, true)) ||
                    !this.canFlowInto(this.level.getBlock(guessChunk, (int) this.x, (int) this.y + 1, (int) this.z - 1, true)) ||
                    !this.canFlowInto(this.level.getBlock(guessChunk, (int) this.x, (int) this.y + 1, (int) this.z + 1, true)) ||
                    !this.canFlowInto(this.level.getBlock(guessChunk, (int) this.x - 1, (int) this.y + 1, (int) this.z, true)) ||
                    !this.canFlowInto(this.level.getBlock(guessChunk, (int) this.x + 1, (int) this.y + 1, (int) this.z, true))) {
                vector = vector.normalize();
                vector.y -= 6;
            }
        }
        return this.flowVector = vector.normalize();
    }

    @Override
    public void addVelocityToEntity(Entity entity, Vector3 vector) {
        if (entity.canBeMovedByCurrents()) {
            Vector3 flow = this.getFlowVector();
            vector.x += flow.x;
            vector.y += flow.y;
            vector.z += flow.z;
        }
    }

    public int getFlowDecayPerBlock() {
        return 1;
    }

    @Override
    public int onUpdate(int type) {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            this.checkForHarden();
            if (this.usesWaterLogging() && this.getLayer().ordinal() > LAYER_NORMAL.ordinal()) {
                Block layer0 = this.level.getBlock(this, LAYER_NORMAL, true);
                if (layer0.getId() == Block.AIR) {
                    this.level.setBlock(this, LAYER_WATERLOGGED, Block.get(Block.AIR), false, false);
                    this.level.setBlock(this, LAYER_NORMAL, this, false, false);
                } else if (layer0.getWaterloggingType() == WaterloggingType.NO_WATERLOGGING || ((layer0.getWaterloggingType() == WaterloggingType.WHEN_PLACED_IN_WATER) && (this.getDamage() > 0))) {
                    this.level.setBlock(this, LAYER_WATERLOGGED, Block.get(Block.AIR), true, true);
                }
            }
            this.level.scheduleUpdate(this, this.tickRate());
            return 0;
        } else if (type == Level.BLOCK_UPDATE_SCHEDULED) {
            int decay = this.getFlowDecay(this);
            int multiplier = this.getFlowDecayPerBlock();
            FullChunk guessChunk = getChunk();
            if (decay > 0) {
                int smallestFlowDecay = -100;
                this.adjacentSources = 0;
                smallestFlowDecay = this.getSmallestFlowDecay(this.level.getBlock(guessChunk, (int) this.x, (int) this.y, (int) this.z - 1, true), smallestFlowDecay);
                smallestFlowDecay = this.getSmallestFlowDecay(this.level.getBlock(guessChunk, (int) this.x, (int) this.y, (int) this.z + 1, true), smallestFlowDecay);
                smallestFlowDecay = this.getSmallestFlowDecay(this.level.getBlock(guessChunk, (int) this.x - 1, (int) this.y, (int) this.z, true), smallestFlowDecay);
                smallestFlowDecay = this.getSmallestFlowDecay(this.level.getBlock(guessChunk, (int) this.x + 1, (int) this.y, (int) this.z, true), smallestFlowDecay);
                int newDecay = smallestFlowDecay + multiplier;
                if (newDecay >= 8 || smallestFlowDecay < 0) {
                    newDecay = -1;
                }
                int topFlowDecay = this.getFlowDecay(this.level.getBlock(guessChunk, (int) this.x, (int) this.y + 1, (int) this.z, true));
                if (topFlowDecay >= 0) {
                    newDecay = topFlowDecay | 0x08;
                }
                if (this.adjacentSources >= 2 && this instanceof BlockWater) {
                    Block bottomBlock = this.level.getBlock(guessChunk, (int) this.x, (int) this.y - 1, (int) this.z, true);
                    if (bottomBlock.isSolid()) {
                        newDecay = 0;
                    } else if (bottomBlock instanceof BlockWater && bottomBlock.getDamage() == 0) {
                        newDecay = 0;
                    } else {
                        bottomBlock = bottomBlock.getLevelBlock(BlockLayer.WATERLOGGED);
                        if (bottomBlock instanceof BlockWater && bottomBlock.getDamage() == 0) {
                            newDecay = 0;
                        }
                    }
                }
                if (newDecay != decay) {
                    decay = newDecay;
                    boolean decayed = decay < 0;
                    Block to;
                    if (decayed) {
                        to = Block.get(BlockID.AIR);
                    } else {
                        to = getBlock(decay);
                    }
                    BlockFromToEvent event = new BlockFromToEvent(this, to);
                    level.getServer().getPluginManager().callEvent(event);
                    if (!event.isCancelled()) {
                        this.level.setBlock(this, this.getLayer(), event.getTo(), true, true);
                        if (!decayed) {
                            this.level.scheduleUpdate(this, this.tickRate());
                        }
                    }
                }
            }
            if (decay >= 0) {
                Block bottomBlock = this.level.getBlock(guessChunk, (int) this.x, (int) this.y - 1, (int) this.z, true);
                this.flowIntoBlock(bottomBlock, decay | 0x08);
                if (decay == 0 || !(this.usesWaterLogging()? bottomBlock.canWaterloggingFlowInto(): bottomBlock.canBeFlowedInto())) {
                    int adjacentDecay;
                    if (decay >= 8) {
                        adjacentDecay = 1;
                    } else {
                        adjacentDecay = decay + multiplier;
                    }
                    if (adjacentDecay < 8) {
                        boolean[] flags = this.getOptimalFlowDirections();
                        if (flags[0]) {
                            this.flowIntoBlock(this.level.getBlock(guessChunk, (int) this.x - 1, (int) this.y, (int) this.z, true), adjacentDecay);
                        }
                        if (flags[1]) {
                            this.flowIntoBlock(this.level.getBlock(guessChunk, (int) this.x + 1, (int) this.y, (int) this.z, true), adjacentDecay);
                        }
                        if (flags[2]) {
                            this.flowIntoBlock(this.level.getBlock(guessChunk, (int) this.x, (int) this.y, (int) this.z - 1, true), adjacentDecay);
                        }
                        if (flags[3]) {
                            this.flowIntoBlock(this.level.getBlock(guessChunk, (int) this.x, (int) this.y, (int) this.z + 1, true), adjacentDecay);
                        }
                    }
                }
                this.checkForHarden();
            }
        }
        return 0;
    }

    protected void flowIntoBlock(Block block, int newFlowDecay) {
        if (!(block instanceof BlockLiquid) && this.canFlowInto(block)) {
            if (this.usesWaterLogging()) {
                Block waterlogged = block.getLevelBlock(LAYER_WATERLOGGED);
                if (waterlogged instanceof BlockLiquid) {
                    return;
                }

                if (block.getWaterloggingType() == WaterloggingType.FLOW_INTO_BLOCK) {
                    block = waterlogged;
                }
            }

            LiquidFlowEvent event = new LiquidFlowEvent(block, this, newFlowDecay);
            level.getServer().getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                if (block.getLayer() == BlockLayer.NORMAL && block.getId() != 0) {
                    this.level.useBreakOn(block, block.getId() == COBWEB ? Item.get(Item.WOODEN_SWORD) : null);
                }
                this.level.setBlock(block, block.getLayer(), getBlock(newFlowDecay), true, true);
                this.level.scheduleUpdate(block, this.tickRate());
            }
        }
    }

    protected int calculateFlowCost(int blockX, int blockY, int blockZ, int accumulatedCost, int maxCost, int originOpposite, int lastOpposite) {
        int cost = 1000;
        for (int j = 0; j < 4; ++j) {
            if (j == originOpposite || j == lastOpposite) {
                continue;
            }
            int x = blockX;
            int z = blockZ;
            if (j == 0) {
                --x;
            } else if (j == 1) {
                ++x;
            } else if (j == 2) {
                --z;
            } else if (j == 3) {
                ++z;
            }
            long hash = Level.blockHash(x, blockY, z, this.level.getDimensionData());
            byte status;
            if (this.flowCostVisited.containsKey(hash)) {
                status = this.flowCostVisited.get(hash);
            } else {
                FullChunk chunk = this.level.getChunk(x >> 4, z >> 4);
                Block blockSide = this.level.getBlock(chunk, x, blockY, z, true);
                if (!this.canFlowInto(blockSide)) {
                    this.flowCostVisited.put(hash, BLOCKED);
                    status = BLOCKED;
                } else if (usesWaterLogging()?
                        this.level.getBlock(x, blockY - 1, z).canWaterloggingFlowInto() :
                        this.level.getBlock(chunk, x, blockY - 1, z, true).canBeFlowedInto()) {
                    this.flowCostVisited.put(hash, CAN_FLOW_DOWN);
                    status = CAN_FLOW_DOWN;
                } else {
                    this.flowCostVisited.put(hash, CAN_FLOW);
                    status = CAN_FLOW;
                }
            }
            if (status == BLOCKED) {
                continue;
            } else if (status == CAN_FLOW_DOWN) {
                return accumulatedCost;
            }
            if (accumulatedCost >= maxCost) {
                continue;
            }
            int realCost = this.calculateFlowCost(x, blockY, z, accumulatedCost + 1, maxCost, originOpposite, j ^ 0x01);
            if (realCost < cost) {
                cost = realCost;
            }
        }
        return cost;
    }

    @Override
    public double getHardness() {
        return 100d;
    }

    @Override
    public double getResistance() {
        return 500;
    }

    protected boolean[] getOptimalFlowDirections() {
        int[] flowCost = {
                1000,
                1000,
                1000,
                1000
        };
        int maxCost = 4 / this.getFlowDecayPerBlock();
        for (int j = 0; j < 4; ++j) {
            int x = (int) this.x;
            int y = (int) this.y;
            int z = (int) this.z;
            if (j == 0) {
                --x;
            } else if (j == 1) {
                ++x;
            } else if (j == 2) {
                --z;
            } else {
                ++z;
            }
            FullChunk chunk = this.level.getChunk(x >> 4, z >> 4);
            Block block = this.level.getBlock(chunk, x, y, z, true);
            if (!this.canFlowInto(block)) {
                this.flowCostVisited.put(Level.blockHash(x, y, z, this.level.getDimensionData()), BLOCKED);
            } else if (usesWaterLogging()?
                    this.level.getBlock(x, y - 1, z).canWaterloggingFlowInto():
                    this.level.getBlock(chunk, x, y - 1, z, true).canBeFlowedInto()) {
                this.flowCostVisited.put(Level.blockHash(x, y, z, this.level.getDimensionData()), CAN_FLOW_DOWN);
                flowCost[j] = maxCost = 0;
            } else if (maxCost > 0) {
                this.flowCostVisited.put(Level.blockHash(x, y, z, this.level.getDimensionData()), CAN_FLOW);
                flowCost[j] = this.calculateFlowCost(x, y, z, 1, maxCost, j ^ 0x01, j ^ 0x01);
                maxCost = Math.min(maxCost, flowCost[j]);
            }
        }
        this.flowCostVisited.clear();
        double minCost = Double.MAX_VALUE;
        for (int i = 0; i < 4; i++) {
            double d = flowCost[i];
            if (d < minCost) {
                minCost = d;
            }
        }
        boolean[] isOptimalFlowDirection = new boolean[4];
        for (int i = 0; i < 4; ++i) {
            isOptimalFlowDirection[i] = (flowCost[i] == minCost);
        }
        return isOptimalFlowDirection;
    }

    private int getSmallestFlowDecay(Block block, int decay) {
        int blockDecay = this.getFlowDecay(block);
        if (blockDecay < 0) {
            return decay;
        } else if (blockDecay == 0) {
            ++this.adjacentSources;
        } else if (blockDecay >= 8) {
            blockDecay = 0;
        }
        return (decay >= 0 && blockDecay >= decay) ? decay : blockDecay;
    }

    protected void checkForHarden() {
    }

    public abstract BlockLiquid getBlock(int meta);

    @Override
    public boolean canPassThrough() {
        return true;
    }

    @Override
    public void onEntityCollide(Entity entity) {
        entity.resetFallDistance();
    }

    protected boolean liquidCollide(Block cause, Block result) {
        BlockFromToEvent event = new BlockFromToEvent(this, result);
        this.level.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }
        this.level.setBlock(this, event.getTo(), true, true);
        this.level.setBlock(this, Block.LAYER_WATERLOGGED, Block.get(Block.AIR), true, true);
        this.getLevel().addLevelSoundEvent(this.add(0.5, 0.5, 0.5), LevelSoundEventPacket.SOUND_FIZZ);
        return true;
    }

    protected boolean canFlowInto(Block block) {
        if (this.usesWaterLogging()) {
            if (block.canWaterloggingFlowInto()) {
                Block blockLayer1 = block.getLevelBlock(BlockLayer.WATERLOGGED);
                return !(block instanceof BlockLiquid && block.getDamage() == 0) && !(blockLayer1 instanceof BlockLiquid && blockLayer1.getDamage() == 0);
            }
        }
        return block.canBeFlowedInto() && !(block instanceof BlockLiquid && block.getDamage() == 0);
    }

    @Override
    public Item toItem() {
        return new ItemBlock(Block.get(BlockID.AIR));
    }

    public boolean usesWaterLogging() {
        return false;
    }
}