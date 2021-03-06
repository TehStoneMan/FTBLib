package com.feed_the_beast.ftbl.cmd;

import com.feed_the_beast.ftbl.api.cmd.CommandLM;
import com.latmod.lib.util.LMStringUtils;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nonnull;

public class CmdSetItemName extends CommandLM
{
    public CmdSetItemName()
    {
        super("set_item_name");
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 0;
    }

    @Nonnull
    @Override
    public String getCommandUsage(@Nonnull ICommandSender ics)
    {
        return '/' + commandName + " <name...>";
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender ics, @Nonnull String[] args) throws CommandException
    {
        checkArgs(args, 1, "<player>");
        EntityPlayerMP ep = getCommandSenderAsPlayer(ics);

        if(ep.inventory.getCurrentItem() != null)
        {
            ep.inventory.getCurrentItem().setStackDisplayName(LMStringUtils.unsplit(args, " "));
            ep.openContainer.detectAndSendChanges();
            ics.addChatMessage(new TextComponentString("Item name set to '" + ep.inventory.getCurrentItem().getDisplayName() + "'!"));
        }
    }
}