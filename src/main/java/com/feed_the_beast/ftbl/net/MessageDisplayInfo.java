package com.feed_the_beast.ftbl.net;

import com.feed_the_beast.ftbl.api.info.IInfoPage;
import com.feed_the_beast.ftbl.api.info.impl.InfoPage;
import com.feed_the_beast.ftbl.api.net.LMNetworkWrapper;
import com.feed_the_beast.ftbl.api.net.MessageToClient;
import com.feed_the_beast.ftbl.gui.GuiInfo;
import com.google.gson.JsonElement;
import com.latmod.lib.util.LMNetUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MessageDisplayInfo extends MessageToClient<MessageDisplayInfo>
{
    public String infoID;
    public JsonElement json;

    public MessageDisplayInfo()
    {
    }

    public MessageDisplayInfo(IInfoPage page)
    {
        infoID = page.getName();
        json = page.getSerializableElement();
    }

    @Override
    public LMNetworkWrapper getWrapper()
    {
        return FTBLibNetHandler.NET;
    }

    @Override
    public void fromBytes(ByteBuf io)
    {
        infoID = LMNetUtils.readString(io);
        json = LMNetUtils.readJsonElement(io);
    }

    @Override
    public void toBytes(ByteBuf io)
    {
        LMNetUtils.writeString(io, infoID);
        LMNetUtils.writeJsonElement(io, json);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onMessage(MessageDisplayInfo m, Minecraft mc)
    {
        InfoPage page = new InfoPage(m.infoID);
        page.fromJson(m.json);
        new GuiInfo(page).openGui();
    }
}