package github.pitbox46.monetamoney.network.client;

import github.pitbox46.monetamoney.ServerEvents;
import github.pitbox46.monetamoney.blocks.Anchor;
import github.pitbox46.monetamoney.blocks.Vault;
import github.pitbox46.monetamoney.data.Team;
import github.pitbox46.monetamoney.data.Teams;
import github.pitbox46.monetamoney.network.IPacket;
import github.pitbox46.monetamoney.network.PacketHandler;
import github.pitbox46.monetamoney.network.server.SGuiStatusMessage;
import github.pitbox46.monetamoney.network.server.SOpenTeamsPage;
import github.pitbox46.monetamoney.network.server.SSyncFeesPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Function;

public class CTeamButton implements IPacket {
    public BlockPos pos;
    public Button button;

    public CTeamButton() {}

    public CTeamButton(BlockPos pos, Button button) {
        this.pos = pos;
        this.button = button;
    }

    @Override
    public void readPacketData(PacketBuffer buf) {
        this.pos = buf.readBlockPos();
        this.button = buf.readEnumValue(Button.class);
    }

    @Override
    public void writePacketData(PacketBuffer buf) {
        buf.writeBlockPos(this.pos);
        buf.writeEnumValue(this.button);
    }

    @Override
    public void processPacket(NetworkEvent.Context ctx) {
        if(ctx.getSender() != null &&ctx.getSender().getEntityWorld().getBlockState(this.pos).getBlock().getClass() == Vault.class) {
            Team team = Teams.getTeam(Teams.jsonFile, ctx.getSender().getServerWorld().getDimensionKey().getLocation().toString() + this.pos.toLong());
            String player = ctx.getSender().getGameProfile().getName();
            switch (this.button) {
                case OPENPAGE: {
                    SOpenTeamsPage.Type type;
                    if(Teams.getPlayersTeam(Teams.jsonFile, player).isNull()) {
                        type = SOpenTeamsPage.Type.INNONE;
                    } else if (Teams.getPlayersTeam(Teams.jsonFile, player).equals(team)) {
                        type = SOpenTeamsPage.Type.INSAME;
                    } else {
                        type = SOpenTeamsPage.Type.INDIFFERENT;
                    }
                    PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SOpenTeamsPage(team, type));
                } break;
                case JOIN: {
                    if(!team.isNull() && Teams.getPlayersTeam(Teams.jsonFile, player).isNull() && !team.locked) {
                        team.members.add(player);
                        Teams.updateTeam(Teams.jsonFile, team);

                        ServerEvents.CHUNK_MAP.putIfAbsent(team.toString(), new ArrayList<>());
                        ServerEvents.movePlayerChunkToTeam("unlisted", team.toString(), player);

                        PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SOpenTeamsPage(team, SOpenTeamsPage.Type.INSAME));
                    } else {
                        PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslationTextComponent("message.monetamoney.error")));
                    }
                } break;
                case SWITCH: {
                    if(!team.isNull() && !team.locked) {
                        Team playersTeam = Teams.getPlayersTeam(Teams.jsonFile, player);
                        playersTeam.members.remove(player);
                        if(playersTeam.members.size() == 0) {
                            Teams.removeTeam(Teams.jsonFile, playersTeam.toString());
                        } else {
                            Teams.updateTeam(Teams.jsonFile, playersTeam);
                        }
                        team.members.add(player);
                        Teams.updateTeam(Teams.jsonFile, team);

                        ServerEvents.CHUNK_MAP.putIfAbsent(team.toString(), new ArrayList<>());
                        ServerEvents.movePlayerChunkToTeam(playersTeam.toString(), team.toString(), player);

                        PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SOpenTeamsPage(team, SOpenTeamsPage.Type.INSAME));
                    } else {
                        PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslationTextComponent("message.monetamoney.error")));
                    }
                } break;
                case LEAVE: {
                    if(!team.isNull() && team.equals(Teams.getPlayersTeam(Teams.jsonFile, player))) {
                        team.members.remove(player);
                        Teams.updateTeam(Teams.jsonFile, team);

                        ServerEvents.CHUNK_MAP.putIfAbsent("unlisted", new ArrayList<>());
                        ServerEvents.movePlayerChunkToTeam(team.toString(), "unlisted", player);

                        PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SOpenTeamsPage(team, SOpenTeamsPage.Type.INNONE));
                    } else {
                        PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslationTextComponent("message.monetamoney.error")));
                    }
                } break;
                case CREATE: {
                    if(team.isNull() && Teams.getPlayersTeam(Teams.jsonFile, player).isNull()) {
                        team = new Team(this.pos, ctx.getSender().getServerWorld().getDimensionKey().getLocation(), player, Collections.singletonList(player), 0, false );
                        Teams.newTeam(Teams.jsonFile, team);

                        ServerEvents.CHUNK_MAP.putIfAbsent(team.toString(), new ArrayList<>());
                        ServerEvents.movePlayerChunkToTeam("unlisted", team.toString(), player);

                        PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SOpenTeamsPage(team, SOpenTeamsPage.Type.INSAME));
                    } else {
                        PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslationTextComponent("message.monetamoney.error")));
                    }
                } break;
                case REMOVE: {
                    if(!team.isNull() && !Teams.getPlayersTeam(Teams.jsonFile, player).isNull() && team.leader.equals(Teams.getPlayersTeam(Teams.jsonFile, player).leader)) {
                        Teams.removeTeam(Teams.jsonFile, team.toString());

                        ServerEvents.CHUNK_MAP.putIfAbsent("unlisted", new ArrayList<>());
                        ServerEvents.moveAllChunksToTeam(team.toString(), "unlisted");

                        PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SOpenTeamsPage(Team.NULL_TEAM, SOpenTeamsPage.Type.INNONE));
                    } else {
                        PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslationTextComponent("message.monetamoney.error")));
                    }
                } break;
                case LOCK: {
                    if(!team.isNull() && team.leader.equals(player)) {
                        team.locked = true;
                        PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SOpenTeamsPage(team, SOpenTeamsPage.Type.INSAME));
                    }
                } break;
                case UNLOCK: {
                    if(!team.isNull() && team.leader.equals(player)) {
                        team.locked = false;
                        PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SOpenTeamsPage(team, SOpenTeamsPage.Type.INSAME));
                    }
                } break;
            }
        }
    }

    public static Function<PacketBuffer, CTeamButton> decoder() {
        return pb -> {
            CTeamButton packet = new CTeamButton();
            packet.readPacketData(pb);
            return packet;
        };
    }

    public enum Button {
        OPENPAGE,
        CREATE,
        JOIN,
        REMOVE,
        LEAVE,
        SWITCH,
        LOCK,
        UNLOCK
    }
}
