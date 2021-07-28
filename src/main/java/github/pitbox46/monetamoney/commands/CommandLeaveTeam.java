package github.pitbox46.monetamoney.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import github.pitbox46.monetamoney.ServerEvents;
import github.pitbox46.monetamoney.data.Auctioned;
import github.pitbox46.monetamoney.data.Team;
import github.pitbox46.monetamoney.data.Teams;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.text.StringTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

public class CommandLeaveTeam implements Command<CommandSource> {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final CommandLeaveTeam CMD = new CommandLeaveTeam();

    public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
        return Commands.literal("leaveteam").executes(CMD);
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        String player = context.getSource().asPlayer().getGameProfile().getName();
        Team team = Teams.getPlayersTeam(Teams.jsonFile, player);
        if(!team.isNull()) {
            team.members.remove(context.getSource().asPlayer().getGameProfile().getName());
            Teams.updateTeam(Teams.jsonFile, team);

            ServerEvents.CHUNK_MAP.putIfAbsent("unlisted", new ArrayList<>());
            ServerEvents.movePlayerChunkToTeam(team.toString(), "unlisted", player);

            context.getSource().asPlayer().sendStatusMessage(new StringTextComponent("Successfully removed from team"), false);
        } else {
            throw new SimpleCommandExceptionType(new StringTextComponent("Not in a team")).create();
        }
        return 0;
    }
}
