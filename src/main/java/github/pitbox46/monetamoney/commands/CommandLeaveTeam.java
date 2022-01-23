package github.pitbox46.monetamoney.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import github.pitbox46.monetamoney.ServerEvents;
import github.pitbox46.monetamoney.data.Team;
import github.pitbox46.monetamoney.data.Teams;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

public class CommandLeaveTeam implements Command<CommandSourceStack> {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final CommandLeaveTeam CMD = new CommandLeaveTeam();

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("leaveteam").executes(CMD);
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String player = context.getSource().getPlayerOrException().getGameProfile().getName();
        Team team = Teams.getPlayersTeam(Teams.jsonFile, player);
        if (!team.isNull()) {
            team.members.remove(context.getSource().getPlayerOrException().getGameProfile().getName());
            Teams.updateTeam(Teams.jsonFile, team);

            ServerEvents.CHUNK_MAP.putIfAbsent("unlisted", new ArrayList<>());
            ServerEvents.movePlayerChunkToTeam(team.toString(), "unlisted", player);

            context.getSource().getPlayerOrException().displayClientMessage(new TextComponent("Successfully removed from team"), false);
        } else {
            throw new SimpleCommandExceptionType(new TextComponent("Not in a team")).create();
        }
        return 0;
    }
}
