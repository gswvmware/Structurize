package com.ldtteam.structurize.network.messages;

import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.network.PacketUtils;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Class handling the Server UUID Message.
 */
public class ServerUUIDMessage extends AbstractMessage<ServerUUIDMessage, IMessage>
{
    private UUID serverUUID;

    /**
     * Empty constructor used when registering the message.
     */
    public ServerUUIDMessage()
    {
        super();
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        serverUUID = PacketUtils.readUUID(buf);
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        PacketUtils.writeUUID(buf, Manager.getServerUUID());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Set the server UUID.
     *
     * @param message Message
     * @param ctx     Context
     */
    @Override
    protected void messageOnClientThread(final ServerUUIDMessage message, final MessageContext ctx)
    {
        Manager.setServerUUID(message.serverUUID);
    }
}
