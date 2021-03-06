package com.ldtteam.structures.lib;

import com.google.common.base.Functions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Sets;
import com.ldtteam.structures.client.BlueprintBlockAccess;
import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.blocks.interfaces.IAnchorBlock;
import com.ldtteam.structurize.util.BlockInfo;
import com.ldtteam.structures.blueprints.v1.Blueprint;
import com.ldtteam.structures.client.BlueprintBlockInfoTransformHandler;
import com.ldtteam.structures.client.BlueprintEntityInfoTransformHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Utility functions for blueprints.
 */
public final class BlueprintUtils
{
    private static final Cache<Blueprint, Map<BlockPos, BlockInfo>> blueprintBlockInfoCache = CacheBuilder.newBuilder().maximumSize(50).build();

    private static final Set<String> blackListedTileEntityIds = Sets.newHashSet();
    private static final Set<String> blackListedEntityIds = Sets.newHashSet();

    private BlueprintUtils()
    {
        throw new IllegalArgumentException("Utils class");
    }

    /**
     * Get the tileEntity from a certain position.
     *
     * @param blueprint the blueprint they are in.
     * @param pos      the position they are at.
     * @param access   the world access to assign them to.
     * @return the tileEntity or null.
     */
    public static TileEntity getTileEntityFromPos(final Blueprint blueprint, final BlockPos pos, final BlueprintBlockAccess access)
    {
        final BlockInfo blockInfo = getBlockInfoFromPos(blueprint, pos);
        if (blockInfo.getTileEntityData() != null)
        {
            return TileEntity.create(access, blockInfo.getTileEntityData());
        }
        return null;
    }

    public static BlockInfo getBlockInfoFromPos(final Blueprint blueprint, final BlockPos pos)
    {
        try
        {
            return BlueprintBlockInfoTransformHandler.getInstance().Transform(Optional.ofNullable(blueprintBlockInfoCache
                                                                                                    .get(blueprint,
                                                                                                      () -> blueprint.getBlockInfoAsList().stream()
                                                                                                              .collect(Collectors.toMap(BlockInfo::getPos, Functions.identity())))
                                                                                                    .get(pos))
                                                                                .orElse(new BlockInfo(pos, Blocks.AIR.getDefaultState(), null)));
        }
        catch (ExecutionException e)
        {
            Log.getLogger().warn(e);
        }

        return new BlockInfo(pos, Blocks.AIR.getDefaultState(), null);
    }

    public static BlockPos getPrimaryBlockOffset(@NotNull final Blueprint blueprint)
    {
        return blueprint.getBlockInfoAsList().stream()
                 .filter(blockInfo -> blockInfo.getState().getBlock() instanceof IAnchorBlock)
                 .findFirst()
                 .map(blockInfo -> BlueprintBlockInfoTransformHandler.getInstance().Transform(blockInfo))
                 .map(BlockInfo::getPos)
                 .orElse(new BlockPos(blueprint.getSizeX() / 2, 0, blueprint.getSizeZ() / 2));
    }

    /**
     * Creates a list of tileentities located in the blueprint, placed inside that blueprints block access world.
     *
     * @param blueprint    The blueprint whos tileentities need to be instantiated.
     * @param blockAccess The blueprint world.
     * @return A list of tileentities in the blueprint.
     */
    @NotNull
    public static List<TileEntity> instantiateTileEntities(@NotNull final Blueprint blueprint, @NotNull final BlueprintBlockAccess blockAccess)
    {
        return blueprint.getBlockInfoAsList().stream()
                 .map(blockInfo -> BlueprintBlockInfoTransformHandler.getInstance().Transform(blockInfo))
                 .filter(blockInfo -> blockInfo.getTileEntityData() != null)
                 .map(blockInfo -> constructTileEntity(blockInfo, blockAccess))
                 .filter(Objects::nonNull)
                 .collect(
                   Collectors.toList());
    }

    /**
     * Creates a list of entities located in the blueprint, placed inside that blueprints block access world.
     *
     * @param blueprint    The blueprint whos entities need to be instantiated.
     * @param blockAccess The blueprints world.
     * @return A list of entities in the blueprint
     */
    @NotNull
    public static List<Entity> instantiateEntities(@NotNull final Blueprint blueprint, @NotNull final BlueprintBlockAccess blockAccess)
    {
        return blueprint.getEntitiesAsList().stream()
                 .map(entityInfo -> BlueprintEntityInfoTransformHandler.getInstance().Transform(entityInfo))
                 .map(entityInfo -> constructEntity(entityInfo, blockAccess))
                 .filter(Objects::nonNull)
                 .collect(Collectors.toList());
    }

    @Nullable
    private static TileEntity constructTileEntity(@NotNull final BlockInfo info, @NotNull final BlueprintBlockAccess blockAccess)
    {
        if (info.getTileEntityData() == null)
            return null;

        final String entityId = info.getTileEntityData().getString("id");

        //We know that this is going to fail.
        //Fail fast.
        if (blackListedTileEntityIds.contains(entityId))
            return null;

        try
        {
            final NBTTagCompound compound = info.getTileEntityData().copy();
            compound.setInteger("x", info.getPos().getX());
            compound.setInteger("y", info.getPos().getY());
            compound.setInteger("z", info.getPos().getZ());

            final TileEntity entity = TileEntity.create(blockAccess, compound);
            entity.setWorld(blockAccess);

            return entity;
        }
        catch (Exception ex)
        {
            Structurize.getLogger().error("Could not create tile entity: " + entityId + " with nbt: " + info.toString(), ex);
            blackListedTileEntityIds.add(entityId);
            return null;
        }
    }

    @Nullable
    private static Entity constructEntity(@Nullable final NBTTagCompound info, @NotNull final BlueprintBlockAccess blockAccess)
    {
        if (info == null)
            return null;

        final String entityId = info.getString("id");

        //We know that this is going to fail.
        //Fail fast.
        if (blackListedEntityIds.contains(entityId))
            return null;

        try
        {
            final NBTTagCompound compound = info.copy();
            compound.setUniqueId("UUID", UUID.randomUUID());

            return EntityList.createEntityFromNBT(compound, blockAccess);
        }
        catch (final Exception ex)
        {
            Structurize.getLogger().error("Could not create entity: " + entityId + " with nbt: " + info.toString(), ex);
            blackListedEntityIds.add(entityId);
            return null;
        }
    }
}
