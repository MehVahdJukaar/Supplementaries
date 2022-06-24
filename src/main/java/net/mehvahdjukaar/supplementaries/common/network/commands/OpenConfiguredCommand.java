package net.mehvahdjukaar.supplementaries.common.network.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.common.network.OpenConfigsPacket;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.PacketDistributor;

public class OpenConfiguredCommand implements Command<CommandSourceStack> {
    private static final OpenConfiguredCommand CMD = new OpenConfiguredCommand();

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("configured")
                .requires(cs -> cs.hasPermission(0))
                .executes(CMD);
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        if (ModList.get().getModContainerById(Supplementaries.MOD_ID).get()
                .getCustomExtension(ConfigGuiHandler.ConfigGuiFactory.class).isPresent()) {

            if (context.getSource().getEntity() instanceof ServerPlayer) {
                NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() ->
                        (ServerPlayer) context.getSource().getEntity()), new OpenConfigsPacket());
            }
        } else {
            context.getSource().sendSuccess(Component.translatable("message.supplementaries.command.configs"), false);
        }
        return 0;
    }
}
