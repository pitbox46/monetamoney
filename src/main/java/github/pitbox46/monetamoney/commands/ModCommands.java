package github.pitbox46.monetamoney.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class ModCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> cmdTut = dispatcher.register(
                Commands.literal("monetamoney")
                        .then(CommandRemoveShop.register(dispatcher))
                        .then(CommandAddShop.register(dispatcher))
                        .then(CommandListShop.register(dispatcher))
                        .then(CommandLeaveTeam.register(dispatcher))
                        .then(CommandVerifyCoin.register(dispatcher))
                        .then(CommandBalance.register(dispatcher))
        );

        dispatcher.register(Commands.literal("monetamoney").redirect(cmdTut));
    }
}
