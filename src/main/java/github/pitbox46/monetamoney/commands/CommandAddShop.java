package github.pitbox46.monetamoney.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import github.pitbox46.monetamoney.data.Auctioned;
import github.pitbox46.monetamoney.items.Coin;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.ItemArgument;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.text.StringTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommandAddShop implements Command<CommandSource> {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final CommandFromHand FROM_HAND = new CommandFromHand();
    private static final CommandAddShop CMD = new CommandAddShop();

    public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
        return Commands
                .literal("addshop")
                .requires(commandSource -> commandSource.hasPermissionLevel(2))
                .then(Commands.argument("dailyStock", IntegerArgumentType.integer(0))
                        .then(Commands.argument("buyPrice", IntegerArgumentType.integer(0))
                                .then(Commands.argument("sellPrice", IntegerArgumentType.integer(0))
                                        .then(Commands.literal("mainhand")
                                                .executes(FROM_HAND))
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(0, 64))
                                                .then(Commands.argument("item", ItemArgument.item())
                                                        .executes(CMD)))
                )));
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ItemStack itemStack = ItemArgument.getItem(context, "item").createStack(IntegerArgumentType.getInteger(context, "amount"), false);
        Auctioned.addShopListing(Auctioned.auctionedNBT, itemStack, IntegerArgumentType.getInteger(context, "buyPrice"), IntegerArgumentType.getInteger(context, "sellPrice"), IntegerArgumentType.getInteger(context, "dailyStock"));
        return 0;
    }

    public static class CommandFromHand implements Command<CommandSource> {

        @Override
        public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
            if(context.getSource().asPlayer().getHeldItemMainhand().isEmpty())
                throw new SimpleCommandExceptionType(new StringTextComponent("No item in main hand")).create();
            if(context.getSource().asPlayer().getHeldItemMainhand().getItem().getClass() == Coin.class)
                throw new SimpleCommandExceptionType(new StringTextComponent("Can't list coins")).create();

            ItemStack itemStack = context.getSource().asPlayer().getHeldItemMainhand();
            Auctioned.addShopListing(Auctioned.auctionedNBT, itemStack, IntegerArgumentType.getInteger(context, "buyPrice"), IntegerArgumentType.getInteger(context, "sellPrice"), IntegerArgumentType.getInteger(context, "dailyStock"));
            return 0;
        }
    }
}
