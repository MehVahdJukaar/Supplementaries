package net.mehvahdjukaar.supplementaries.reg.fabric;

import com.mojang.brigadier.arguments.ArgumentType;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.commands.arguments.fabric.NoteBlockInstrumentArgument;
import net.mehvahdjukaar.supplementaries.common.commands.arguments.fabric.SourceArgument;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;

public class ModCommandsImpl {

    public static void registerArguments() {
        registerArgument("source", SourceArgument.class, SingletonArgumentInfo.contextFree(SourceArgument::new));
        registerArgument("note_block_instrument", NoteBlockInstrumentArgument.class, SingletonArgumentInfo.contextFree(NoteBlockInstrumentArgument::new));
    }

    public static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>> void registerArgument(
            String name, Class<? extends A> clazz, ArgumentTypeInfo<A, T> serializer) {
        ArgumentTypeRegistry.registerArgumentType(Supplementaries.res(name), clazz, serializer);
    }
}
