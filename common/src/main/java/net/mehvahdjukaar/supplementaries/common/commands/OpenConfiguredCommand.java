package net.mehvahdjukaar.supplementaries.common.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundOpenConfigsPacket;
import net.mehvahdjukaar.supplementaries.common.network.ModNetwork;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class OpenConfiguredCommand implements Command<CommandSourceStack> {

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandBuildContext dispatcher) {
        return Commands.literal("configs")
                .requires(cs -> cs.hasPermission(0))
                .executes(new OpenConfiguredCommand());
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        if (CommonConfigs.CONFIG_HOLDER.hasConfigScreen()) {
            if (context.getSource().getEntity() instanceof ServerPlayer serverPlayer) {
                NetworkHelper.sendToClientPlayer(serverPlayer, new ClientBoundOpenConfigsPacket());
            }
        } else {
            context.getSource().sendSuccess(()->Component.translatable("message.supplementaries.command.configs"), false);
        }
        return 0;
    }
}
