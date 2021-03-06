package com.feed_the_beast.ftbl.api_impl;

import com.feed_the_beast.ftbl.FTBLibStats;
import com.feed_the_beast.ftbl.api.IForgePlayer;
import com.feed_the_beast.ftbl.api.IForgeTeam;
import com.feed_the_beast.ftbl.api.config.ConfigGroup;
import com.feed_the_beast.ftbl.api.events.player.AttachPlayerCapabilitiesEvent;
import com.feed_the_beast.ftbl.api.events.player.ForgePlayerDeathEvent;
import com.feed_the_beast.ftbl.api.events.player.ForgePlayerInfoEvent;
import com.feed_the_beast.ftbl.api.events.player.ForgePlayerLoggedInEvent;
import com.feed_the_beast.ftbl.api.events.player.ForgePlayerLoggedOutEvent;
import com.feed_the_beast.ftbl.api.events.player.ForgePlayerSettingsEvent;
import com.feed_the_beast.ftbl.api.security.EnumPrivacyLevel;
import com.feed_the_beast.ftbl.net.MessageReload;
import com.feed_the_beast.ftbl.util.FTBLib;
import com.feed_the_beast.ftbl.util.ReloadType;
import com.latmod.lib.util.LMNBTUtils;
import com.latmod.lib.util.LMStringUtils;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.stats.StatisticsManagerServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.common.util.FakePlayer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by LatvianModder on 09.02.2016.
 */
public class ForgePlayer implements Comparable<ForgePlayer>, IForgePlayer
{
    final CapabilityDispatcher capabilities;
    private String teamID;
    private GameProfile gameProfile;
    private StatisticsManagerServer statsManager;
    private EntityPlayerMP entityPlayer;
    private NBTTagCompound playerNBT;

    public ForgePlayer(GameProfile p)
    {
        setProfile(p);

        AttachPlayerCapabilitiesEvent event = new AttachPlayerCapabilitiesEvent(this);
        MinecraftForge.EVENT_BUS.post(event);
        capabilities = !event.getCapabilities().isEmpty() ? new CapabilityDispatcher(event.getCapabilities(), null) : null;
    }

    ForgePlayer(EntityPlayerMP ep)
    {
        this(ep.getGameProfile());
        entityPlayer = ep;
    }

    @Override
    public final String getTeamID()
    {
        return teamID;
    }

    public final void setTeamID(String id)
    {
        teamID = (id == null || id.isEmpty()) ? null : id;
    }

    public final boolean isMemberOf(ForgeTeam team)
    {
        return teamID != null && team != null && team.getName().equals(teamID);
    }

    @Override
    @Nullable
    public final ForgeTeam getTeam()
    {
        return teamID != null ? FTBLibAPI_Impl.get().getWorld().getTeam(teamID) : null;
    }

    @Override
    public final boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
    {
        return capabilities != null && capabilities.hasCapability(capability, facing);
    }

    @Override
    public final <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
    {
        return capabilities == null ? null : capabilities.getCapability(capability, facing);
    }

    @Override
    @Nonnull
    public final GameProfile getProfile()
    {
        if(gameProfile == null)
        {
            throw new NullPointerException("GameProfile is null!");
        }

        return gameProfile;
    }

    public final void setProfile(GameProfile p)
    {
        if(p != null)
        {
            gameProfile = new GameProfile(p.getId(), p.getName());
        }
    }

    public final String getStringUUID()
    {
        return LMStringUtils.fromUUID(getProfile().getId());
    }

    @Override
    public final int compareTo(@Nonnull ForgePlayer o)
    {
        return getProfile().getName().compareToIgnoreCase(o.getProfile().getName());
    }

    @Override
    public final String toString()
    {
        return gameProfile.getName();
    }

    @Override
    public final int hashCode()
    {
        return gameProfile.getId().hashCode();
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
        else if(o instanceof UUID)
        {
            return gameProfile.getId().equals(o);
        }
        else if(o instanceof IForgePlayer)
        {
            return equalsPlayer((IForgePlayer) o);
        }
        return equalsPlayer(getWorld().getPlayer(o));
    }

    @Override
    public boolean equalsPlayer(IForgePlayer p)
    {
        return p == this || (p != null && gameProfile.getId().equals(p.getProfile().getId()));
    }

    public Map<EntityEquipmentSlot, ItemStack> getArmor()
    {
        Map<EntityEquipmentSlot, ItemStack> map = new HashMap<>();

        EntityPlayerMP ep = getPlayer();

        if(ep != null)
        {
            for(EntityEquipmentSlot e : EntityEquipmentSlot.values())
            {
                ItemStack is = ep.getItemStackFromSlot(e);

                if(is != null)
                {
                    map.put(e, is.copy());
                }
            }
        }

        return map;
    }

    @Override
    public boolean canInteract(@Nullable IForgePlayer owner, EnumPrivacyLevel level)
    {
        if(level == EnumPrivacyLevel.PUBLIC || owner == null)
        {
            return true;
        }
        else if(owner.equalsPlayer(this))
        {
            return true;
        }
        else if(level == EnumPrivacyLevel.PRIVATE)
        {
            return false;
        }
        else if(level == EnumPrivacyLevel.TEAM)
        {
            IForgeTeam team = owner.getTeam();

            if(team != null)
            {
                if(team.getStatus(this).isAlly())
                {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean isOnline()
    {
        return getPlayer() != null;
    }

    @Override
    public EntityPlayerMP getPlayer()
    {
        return entityPlayer;
    }

    @Override
    public final ForgeWorld getWorld()
    {
        return FTBLibAPI_Impl.get().getWorld();
    }

    @Override
    public boolean isFake()
    {
        return getPlayer() instanceof FakePlayer;
    }

    @Override
    public boolean isMemberOf(IForgeTeam team)
    {
        return teamID != null && team != null && team.getName().equals(teamID);
    }

    @Override
    public boolean isOP()
    {
        return FTBLib.isOP(getProfile());
    }

    public void getInfo(IForgePlayer owner, List<ITextComponent> info)
    {
        long ms = System.currentTimeMillis();
        IForgeTeam team = getTeam();

        if(team != null)
        {
            ITextComponent c = new TextComponentString("[" + team.getTitle() + "]");
            c.getStyle().setColor(team.getColor().getTextFormatting()).setUnderlined(true);
            info.add(c);

            if(team.getDesc() != null)
            {
                c = new TextComponentString(team.getDesc());
                c.getStyle().setColor(TextFormatting.GRAY).setItalic(true);
                info.add(c);
            }

            info.add(null);
        }

        StatisticsManagerServer stats = stats();

        if(!owner.isOnline())
        {
            long lastSeen = FTBLibStats.getLastSeen(stats, false);

            if(lastSeen > 0L)
            {
                info.add(new TextComponentTranslation(FTBLibStats.LAST_SEEN.statId).appendText(": " + LMStringUtils.getTimeString(ms - lastSeen)));
            }
        }

        long firstJoined = FTBLibStats.getFirstJoined(stats);

        if(firstJoined > 0L)
        {
            info.add(new TextComponentTranslation(FTBLibStats.FIRST_JOINED.statId).appendText(": " + LMStringUtils.getTimeString(ms - firstJoined)));
        }

        if(stats.readStat(StatList.DEATHS) > 0)
        {
            info.add(new TextComponentTranslation(StatList.DEATHS.statId).appendText(": " + stats.readStat(StatList.DEATHS)));
        }

        if(stats.readStat(StatList.PLAY_ONE_MINUTE) > 0L)
        {
            ITextComponent c = StatList.PLAY_ONE_MINUTE.getStatName();
            c.getStyle().setColor(null);
            info.add(c.appendSibling(new TextComponentString(": " + LMStringUtils.getTimeString(stats.readStat(StatList.PLAY_ONE_MINUTE) * 50L))));
        }

        MinecraftForge.EVENT_BUS.post(new ForgePlayerInfoEvent(this, info, ms));
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        setTeamID(tag.getString("TeamID"));

        if(capabilities != null)
        {
            capabilities.deserializeNBT(tag.getCompoundTag("Caps"));
        }
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        NBTTagCompound tag = new NBTTagCompound();

        if(getTeam() != null)
        {
            tag.setString("TeamID", getTeamID());
        }

        if(capabilities != null)
        {
            tag.setTag("Caps", capabilities.serializeNBT());
        }

        return tag;
    }

    public void onLoggedIn(EntityPlayerMP ep, boolean firstLogin)
    {
        entityPlayer = ep;
        playerNBT = null;
        statsManager = null;
        FTBLibStats.updateLastSeen(stats());
        new MessageReload(ReloadType.LOGIN).sendTo(entityPlayer);
        MinecraftForge.EVENT_BUS.post(new ForgePlayerLoggedInEvent(this, firstLogin));
    }

    public void onLoggedOut()
    {
        FTBLibStats.updateLastSeen(stats());
        MinecraftForge.EVENT_BUS.post(new ForgePlayerLoggedOutEvent(this));
        entityPlayer = null;
        playerNBT = null;
        statsManager = null;
    }

    public void onDeath()
    {
        if(isOnline())
        {
            FTBLibStats.updateLastSeen(stats());
            MinecraftForge.EVENT_BUS.post(new ForgePlayerDeathEvent(this));
            statsManager = null;
        }
    }

    @Override
    @Nonnull
    public StatisticsManagerServer stats()
    {
        if(statsManager == null)
        {
            statsManager = FTBLib.getServer().getPlayerList().getPlayerStatsFile(entityPlayer == null ? new FakePlayer(FTBLib.getServerWorld(), getProfile()) : entityPlayer);
        }

        return statsManager;
    }

    public void getSettings(ConfigGroup group)
    {
        MinecraftForge.EVENT_BUS.post(new ForgePlayerSettingsEvent(this, group));
    }

    @Nullable
    public NBTTagCompound getPlayerNBT()
    {
        if(isOnline())
        {
            return null;
        }

        if(playerNBT == null)
        {
            try
            {
                playerNBT = LMNBTUtils.readTag(new File(FTBLib.folderWorld, "playerdata/" + getProfile().getId() + ".dat"));
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }
        }

        return playerNBT;
    }
}