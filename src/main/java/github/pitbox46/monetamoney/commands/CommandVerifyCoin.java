package github.pitbox46.monetamoney.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import github.pitbox46.monetamoney.data.Outstanding;
import github.pitbox46.monetamoney.items.Coin;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommandVerifyCoin implements Command<CommandSourceStack> {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final CommandVerifyCoin CMD = new CommandVerifyCoin();

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands
                .literal("verify")
                .executes(CMD);
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ItemStack itemStack = context.getSource().getPlayerOrException().getMainHandItem();
        if (itemStack.getItem().getClass() == Coin.class && itemStack.hasTag()) {
            CompoundTag nbt = itemStack.getOrCreateTag();
            if (nbt.hasUUID("uuid") && Outstanding.isValidCoin(Outstanding.jsonFile, nbt.getUUID("uuid"))) {
                context.getSource().getPlayerOrException().displayClientMessage(new TranslatableComponent("message.monetamoney.validcoin").withStyle(ChatFormatting.GREEN), false);
                return 0;
            }
        }
        context.getSource().getPlayerOrException().displayClientMessage(new TranslatableComponent("message.monetamoney.invalidcoin").withStyle(ChatFormatting.RED), false);
        return 0;
    }
}
