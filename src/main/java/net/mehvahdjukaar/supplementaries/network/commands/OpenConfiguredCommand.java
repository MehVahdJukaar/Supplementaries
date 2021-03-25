package net.mehvahdjukaar.supplementaries.network.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.network.OpenConfigsPacket;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.network.PacketDistributor;

public class OpenConfiguredCommand implements Command<CommandSource> {
    private static final OpenConfiguredCommand CMD = new OpenConfiguredCommand();

    public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
        return Commands.literal("configured")
                .requires(cs -> cs.hasPermission(0))
                .executes(CMD);
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        if(ModList.get().getModContainerById(Supplementaries.MOD_ID).get()
                .getCustomExtension(ExtensionPoint.CONFIGGUIFACTORY).isPresent()) {

            if(context.getSource().getEntity() instanceof ServerPlayerEntity){
                NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() ->
                                (ServerPlayerEntity) context.getSource().getEntity()), new OpenConfigsPacket());
            }
        }
        else {
            context.getSource().sendSuccess(new TranslationTextComponent("message.supplementaries.command.configs"), false);
        }
        return 0;
    }
}
