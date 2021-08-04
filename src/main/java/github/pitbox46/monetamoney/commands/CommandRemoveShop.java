package github.pitbox46.monetamoney.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import github.pitbox46.monetamoney.data.Auctioned;
import github.pitbox46.monetamoney.data.Ledger;
import github.pitbox46.monetamoney.data.Outstanding;
import github.pitbox46.monetamoney.setup.Registration;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Iterator;
import java.util.Objects;

public class CommandRemoveShop implements Command<CommandSource> {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final CommandRemoveShop CMD = new CommandRemoveShop();

    public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
        return Commands
                .literal("removeshop")
                .requires(commandSource -> commandSource.hasPermissionLevel(2))
                .then(Commands.argument("index", IntegerArgumentType.integer(0))
                        .executes(CMD)
                );
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        int index = IntegerArgumentType.getInteger(context, "index");
        ListNBT shop = (ListNBT) Auctioned.auctionedNBT.get("shop");
        if (shop == null)
            throw new SimpleCommandExceptionType(new StringTextComponent("Uh-oh, the shop doesn't exist!")).create();
        if (shop.size() < index + 1)
            throw new SimpleCommandExceptionType(new StringTextComponent("Index is out of bounds")).create();
        INBT item = shop.remove(index);
        try {
            context.getSource().asPlayer().sendStatusMessage(new StringTextComponent("Removed: " + ((CompoundNBT) item).getString("id")), false);
        } catch (CommandSyntaxException e) {
            LOGGER.info("Removed: " + ((CompoundNBT) item).getString("id"));
        }
        return 0;
    }
}
