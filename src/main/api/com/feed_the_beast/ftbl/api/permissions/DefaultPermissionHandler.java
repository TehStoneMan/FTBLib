/*
 * Minecraft Forge
 * Copyright (c) 2016.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.feed_the_beast.ftbl.api.permissions;

import com.feed_the_beast.ftbl.api.permissions.context.IContext;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nonnull;

/**
 * Default implementation of PermissionAPI.
 * Returns returns true if it is a singleplayer world or the player is an OP
 *
 * @see PermissionAPI
 */
public enum DefaultPermissionHandler implements IPermissionHandler
{
    INSTANCE;

    @Override
    public boolean hasPermission(@Nonnull GameProfile profile, @Nonnull String permission, boolean defaultForPlayer, @Nonnull IContext context)
    {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        return defaultForPlayer || server == null || !server.isDedicatedServer() || server.getPlayerList().getOppedPlayers().getPermissionLevel(profile) > 0;
    }
}