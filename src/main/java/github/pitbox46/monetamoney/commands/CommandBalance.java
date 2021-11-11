package github.pitbox46.monetamoney.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import github.pitbox46.monetamoney.data.Ledger;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.GameProfileArgument;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommandBalance {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final Command<CommandSource> SET = context -> {
        String player = GameProfileArgument.getGameProfiles(context, "player").iterator().next().getName();
        Ledger.setBalance(Ledger.jsonFile, player, IntegerArgumentType.getInteger(context, "amount"));
        return 0;
    };
    private static final Command<CommandSource> ADD = context -> {
        String player = GameProfileArgument.getGameProfiles(context, "player").iterator().next().getName();
        Ledger.addBalance(Ledger.jsonFile, player, IntegerArgumentType.getInteger(context, "amount"));
        return 0;
    };
    private static final Command<CommandSource> GET = context -> {
        String player = GameProfileArgument.getGameProfiles(context, "player").iterator().next().getName();
        long bal = Ledger.readBalance(Ledger.jsonFile, player);
        context.getSource().asPlayer().sendStatusMessage(new TranslationTextComponent("message.monetamoney.balance", player, bal), false);
        return 0;
    };


    public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
        return Commands
                .literal("balance")
                .requires(commandSource -> commandSource.hasPermissionLevel(2))
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
