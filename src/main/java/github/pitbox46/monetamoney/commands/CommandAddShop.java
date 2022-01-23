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
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommandAddShop implements Command<CommandSourceStack> {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final CommandFromHand FROM_HAND = new CommandFromHand();
    private static final CommandAddShop CMD = new CommandAddShop();

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands
                .literal("addshop")
                .requires(commandSource -> commandSource.hasPermission(2))
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
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ItemStack itemStack = ItemArgument.getItem(context, "item").createItemStack(IntegerArgumentType.getInteger(context, "amount"), false);
        Auctioned.addShopListing(Auctioned.auctionedNBT, itemStack, IntegerArgumentType.getInteger(context, "buyPrice"), IntegerArgumentType.getInteger(context, "sellPrice"), IntegerArgumentType.getInteger(context, "dailyStock"));
        return 0;
    }

    public static class CommandFromHand implements Command<CommandSourceStack> {

        @Override
        public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
            if (context.getSource().getPlayerOrException().getMainHandItem().isEmpty())
                throw new SimpleCommandExceptionType(new TextComponent("No item in main hand")).create();
            if (context.getSource().getPlayerOrException().getMainHandItem().getItem().getClass() == Coin.class)
                throw new SimpleCommandExceptionType(new TextComponent("Can't list coins")).create();

            ItemStack itemStack = context.getSource().getPlayerOrException().getMainHandItem();
            Auctioned.addShopListing(Auctioned.auctionedNBT, itemStack, IntegerArgumentType.getInteger(context, "buyPrice"), IntegerArgumentType.getInteger(context, "sellPrice"), IntegerArgumentType.getInteger(context, "dailyStock"));
            return 0;
        }
    }
}
