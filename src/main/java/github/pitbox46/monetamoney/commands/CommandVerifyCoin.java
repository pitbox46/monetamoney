package github.pitbox46.monetamoney.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import github.pitbox46.monetamoney.data.Auctioned;
import github.pitbox46.monetamoney.data.Outstanding;
import github.pitbox46.monetamoney.items.Coin;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommandVerifyCoin implements Command<CommandSource> {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final CommandVerifyCoin CMD = new CommandVerifyCoin();

    public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
        return Commands
                .literal("verify")
                .executes(CMD);
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ItemStack itemStack = context.getSource().asPlayer().getHeldItemMainhand();
        if(itemStack.getItem().getClass() == Coin.class && itemStack.hasTag()) {
            CompoundNBT nbt = itemStack.getOrCreateTag();
            if(nbt.hasUniqueId("uuid") && Outstanding.isValidCoin(Outstanding.jsonFile, nbt.getUniqueId("uuid"))) {
                context.getSource().asPlayer().sendStatusMessage(new TranslationTextComponent("message.monetamoney.validcoin").mergeStyle(TextFormatting.GREEN), false);
                return 0;
            }
        }
        context.getSource().asPlayer().sendStatusMessage(new TranslationTextComponent("message.monetamoney.invalidcoin").mergeStyle(TextFormatting.RED), false);
        return 0;
    }
}
