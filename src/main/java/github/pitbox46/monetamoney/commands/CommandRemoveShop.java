package github.pitbox46.monetamoney.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import github.pitbox46.monetamoney.data.Auctioned;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommandRemoveShop implements Command<CommandSourceStack> {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final CommandRemoveShop CMD = new CommandRemoveShop();

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands
                .literal("removeshop")
                .requires(commandSource -> commandSource.hasPermission(2))
                .then(Commands.argument("index", IntegerArgumentType.integer(0))
                        .executes(CMD)
                );
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        int index = IntegerArgumentType.getInteger(context, "index");
        ListTag shop = (ListTag) Auctioned.auctionedNBT.get("shop");
        if (shop == null)
            throw new SimpleCommandExceptionType(new TextComponent("Uh-oh, the shop doesn't exist!")).create();
        if (shop.size() < index + 1)
            throw new SimpleCommandExceptionType(new TextComponent("Index is out of bounds")).create();
        Tag item = shop.remove(index);
        try {
            context.getSource().getPlayerOrException().displayClientMessage(new TextComponent("Removed: " + ((CompoundTag) item).getString("id")), false);
        } catch (CommandSyntaxException e) {
            LOGGER.info("Removed: " + ((CompoundTag) item).getString("id"));
        }
        return 0;
    }
}
