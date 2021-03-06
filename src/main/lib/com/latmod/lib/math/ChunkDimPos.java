package com.latmod.lib.math;

import net.minecraft.util.math.ChunkPos;

import javax.annotation.Nonnull;

/**
 * Created by LatvianModder on 14.03.2016.
 */
public final class ChunkDimPos
{
    public final int posX, posZ, dim;

    public ChunkDimPos(int x, int z, int d)
    {
        posX = x;
        posZ = z;
        dim = d;
    }

    @Override
    public boolean equals(Object o)
    {
        if(o == null)
        {
            return false;
        }
        else if(o == this)
        {
            return true;
        }
        else if(o instanceof ChunkDimPos)
        {
            return equalsChunkDimPos((ChunkDimPos) o);
        }
        return false;
    }

    public boolean equalsChunkDimPos(ChunkDimPos p)
    {
        return p == this || (p != null && p.dim == dim && p.posX == posX && p.posZ == posZ);
    }

    @Nonnull
    @Override
    public String toString()
    {
        return "[" + dim + '@' + posX + ',' + posZ + ']';
    }

    @Override
    public int hashCode()
    {
        return 31 * (31 * posX + posZ) + dim;
    }

    public ChunkPos getChunkPos()
    {
        return new ChunkPos(posX, posZ);
    }
}