package com.ldtteam.structurize.blocks.cactus;

import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blocks.AbstractBlockDoor;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

import java.util.Locale;

public class BlockCactusDoor extends AbstractBlockDoor<BlockCactusDoor>
{

    public BlockCactusDoor(final Block block)
    {
        super(Material.WOOD, block);
        setRegistryName(Constants.MOD_ID.toLowerCase() + ":" + "blockcactusdoor");
        setTranslationKey(Constants.MOD_ID.toLowerCase(Locale.ENGLISH) + "." + "blockcactusdoor");
        setSoundType(SoundType.WOOD);
        setLightOpacity(0);
    }

}
