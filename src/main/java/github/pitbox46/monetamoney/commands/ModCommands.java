package github.pitbox46.monetamoney.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

public class ModCommands {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralCommandNode<CommandSource> cmdTut = dispatcher.register(
                Commands.literal("monetamoney")
                        .then(CommandRemoveShop.register(dispatcher))
                        .then(CommandAddShop.register(dispatcher))
                        .then(CommandListShop.register(dispatcher))
                        .then(CommandLeaveTeam.register(dispatcher))
                        .then(CommandVerifyCoin.register(dispatcher))
        );

        dispatcher.register(Commands.literal("monetamoney").redirect(cmdTut));
    }
}
