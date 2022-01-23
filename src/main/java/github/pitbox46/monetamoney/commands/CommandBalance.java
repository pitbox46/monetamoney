package github.pitbox46.monetamoney.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import github.pitbox46.monetamoney.data.Ledger;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.TranslatableComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommandBalance {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final Command<CommandSourceStack> SET = context -> {
        String player = GameProfileArgument.getGameProfiles(context, "player").iterator().next().getName();
        Ledger.setBalance(Ledger.jsonFile, player, IntegerArgumentType.getInteger(context, "amount"));
        return 0;
    };
    private static final Command<CommandSourceStack> ADD = context -> {
        String player = GameProfileArgument.getGameProfiles(context, "player").iterator().next().getName();
        Ledger.addBalance(Ledger.jsonFile, player, IntegerArgumentType.getInteger(context, "amount"));
        return 0;
    };
    private static final Command<CommandSourceStack> GET = context -> {
        String player = GameProfileArgument.getGameProfiles(context, "player").iterator().next().getName();
        long bal = Ledger.readBalance(Ledger.jsonFile, player);
        context.getSource().getPlayerOrException().displayClientMessage(new TranslatableComponent("message.monetamoney.balance", player, bal), false);
        return 0;
    };


    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands
                .literal("balance")
                .requires(commandSource -> commandSource.hasPermission(2))
                .then(Commands.argument("player", GameProfileArgument.gameProfile())
                        .then(Commands.literal("set")
                                .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                        .executes(SET)))
                        .then(Commands.literal("add")
                                .then(Commands.argument("amount", IntegerArgumentType.integer())
                                        .executes(ADD)))
                        .then(Commands.literal("get")
                                .executes(GET))
                );
    }
}
