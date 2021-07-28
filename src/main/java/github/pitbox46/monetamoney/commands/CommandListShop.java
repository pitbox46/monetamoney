package github.pitbox46.monetamoney.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import github.pitbox46.monetamoney.data.Auctioned;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.ItemArgument;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommandListShop implements Command<CommandSource> {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final CommandListShop CMD = new CommandListShop();

    public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
        return Commands
                .literal("listshop")
                .requires(commandSource -> commandSource.hasPermissionLevel(2))
                .executes(CMD);
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        IFormattableTextComponent text = new StringTextComponent("");
        int i = 0;
        for(INBT inbt: ((ListNBT) Auctioned.auctionedNBT.get("shop"))) {
            CompoundNBT compoundNBT = (CompoundNBT) inbt;
            text.appendString(String.format("[%s] ", i));
            text.appendString(compoundNBT.getInt("count") + " ");
            text.appendString(compoundNBT.getString("id"));
            text.appendString("\n");
            i++;
        }
        context.getSource().asPlayer().sendStatusMessage(text, false);
        return 0;
    }
}
