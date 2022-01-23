package github.pitbox46.monetamoney.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import github.pitbox46.monetamoney.data.Auctioned;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommandListShop implements Command<CommandSourceStack> {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final CommandListShop CMD = new CommandListShop();

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands
                .literal("listshop")
                .requires(commandSource -> commandSource.hasPermission(2))
                .executes(CMD);
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        MutableComponent text = new TextComponent("");
        int i = 0;
        for (Tag inbt : ((ListTag) Auctioned.auctionedNBT.get("shop"))) {
            CompoundTag compoundNBT = (CompoundTag) inbt;
            text.append(String.format("[%s] ", i));
            text.append(compoundNBT.getInt("count") + " ");
            text.append(compoundNBT.getString("id"));
            text.append("\n");
            i++;
        }
        context.getSource().getPlayerOrException().displayClientMessage(text, false);
        return 0;
    }
}
