package github.pitbox46.monetamoney;

import github.pitbox46.monetamoney.blocks.Anchor;
import github.pitbox46.monetamoney.blocks.IOnBreak;
import github.pitbox46.monetamoney.data.*;
import github.pitbox46.monetamoney.items.Coin;
import github.pitbox46.monetamoney.network.PacketHandler;
import github.pitbox46.monetamoney.network.server.SDenyUseItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fmllegacy.network.PacketDistributor;
import net.minecraftforge.fmlserverevents.FMLServerStartingEvent;
import net.minecraftforge.fmlserverevents.FMLServerStoppingEvent;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;

public class ServerEvents {
    public static final Map<String, List<ChunkLoader>> CHUNK_MAP = new HashMap<>();

    /* Helper methods */
    public static void collectAuctionFees() {
        ListTag auctionList = (ListTag) Auctioned.auctionedNBT.get("auction");
        assert auctionList != null;

        Map<String, Integer> itemsByPlayer = auctionList.stream().collect((Supplier<HashMap<String, Integer>>) HashMap::new, (map, inbt) -> map.put(((CompoundTag) inbt).getString("owner"), map.getOrDefault(((CompoundTag) inbt).getString("owner"), 0) + 1), HashMap::putAll);

        auctionList.removeIf(inbt -> {
            CompoundTag nbt = (CompoundTag) inbt;
            long price = calculateDailyListCost(itemsByPlayer.getOrDefault(nbt.getString("owner"), 0));
            boolean flag = Ledger.readBalance(Ledger.jsonFile, nbt.getString("owner")) < price;
            if (!flag) Ledger.addBalance(Ledger.jsonFile, nbt.getString("owner"), -price);
            return flag;
        });
    }

    public static void loadNewChunk(Level world, Team team, ChunkLoader newChunk) {
        ServerLevel serverWorld = world.getServer().getLevel(ResourceKey.create(Registry.DIMENSION_REGISTRY, newChunk.dimensionKey));
        long price = Config.BASE_CHUNKLOADER.get();

        Map<String, Integer> chunksByPlayer = CHUNK_MAP.values().stream().flatMap(Collection::stream).collect((Supplier<HashMap<String, Integer>>) HashMap::new, (map, chunkLoader) -> map.put(chunkLoader.owner, map.getOrDefault(chunkLoader.owner, 0) + 1), HashMap::putAll);

        for (Map.Entry<String, List<ChunkLoader>> entry : CHUNK_MAP.entrySet()) {
            /* Unlisted is all the chunks that don't belong to a team, but belong to an owner. These chunks cannot be loaded */
            if (entry.getKey().equals("unlisted")) continue;

            if (serverWorld != null && team.equals(Teams.getTeam(Teams.jsonFile, entry.getKey())) && newChunk.status == ChunkLoader.Status.ON) {
                price = calculateChunksCost(chunksByPlayer.getOrDefault(newChunk.owner, 0));
            }
        }
        if (newChunk.status == ChunkLoader.Status.ON) {
            long chunkLong = new ChunkPos(newChunk.pos).toLong();
            serverWorld.setChunkForced(ChunkPos.getX(chunkLong), ChunkPos.getZ(chunkLong), newChunk.status == ChunkLoader.Status.ON && team.balance >= price);
            if (serverWorld.getLevel().getForcedChunks().contains(chunkLong)) {
                if (team.balance >= price) {
                    team.balance -= (price);
                    newChunk.status = ChunkLoader.Status.ON;
                    serverWorld.setBlockAndUpdate(newChunk.pos, serverWorld.getBlockState(newChunk.pos).setValue(Anchor.STATUS, Anchor.Status.ON));
                } else {
                    team.balance -= (price + Config.OVERDRAFT_FEE.get());
                    newChunk.status = ChunkLoader.Status.STUCK;
                    serverWorld.setBlockAndUpdate(newChunk.pos, serverWorld.getBlockState(newChunk.pos).setValue(Anchor.STATUS, Anchor.Status.STUCK));
                }
                Teams.updateTeam(Teams.jsonFile, team);
            } else {
                newChunk.status = ChunkLoader.Status.OFF;
                serverWorld.setBlockAndUpdate(newChunk.pos, serverWorld.getBlockState(newChunk.pos).setValue(Anchor.STATUS, Anchor.Status.OFF));
            }
        }
    }

    public static void loadAndPayChunks(Level world) {
        Map<String, Integer> chunksByPlayer = CHUNK_MAP.values().stream().flatMap(Collection::stream).collect((Supplier<HashMap<String, Integer>>) HashMap::new, (map, chunkLoader) -> map.put(chunkLoader.owner, map.getOrDefault(chunkLoader.owner, 0) + 1), HashMap::putAll);

        for (Map.Entry<String, List<ChunkLoader>> entry : CHUNK_MAP.entrySet()) {
            /* Unlisted is all the chunks that don't belong to a team, but belong to an owner. These chunks cannot be loaded */
            if (entry.getKey().equals("unlisted")) continue;

            Iterator<ChunkLoader> iterator = entry.getValue().iterator();
            while (iterator.hasNext()) {
                ChunkLoader chunkLoader = iterator.next();
                ServerLevel serverWorld = world.getServer().getLevel(ResourceKey.create(Registry.DIMENSION_REGISTRY, chunkLoader.dimensionKey));
                if (serverWorld != null) {
                    //Should repair any issues with syncing between loaders list and the world
                    if (serverWorld.getBlockState(chunkLoader.pos).getBlock().getClass() != Anchor.class) {
                        iterator.remove();
                        chunksByPlayer.put(chunkLoader.owner, chunksByPlayer.get(chunkLoader.owner) - 1);
                        continue;
                    }
                    Team team = Teams.getTeam(Teams.jsonFile, entry.getKey());
                    long price = calculateChunksCost(chunksByPlayer.getOrDefault(chunkLoader.owner, 0));

                    long chunkLong = new ChunkPos(chunkLoader.pos).toLong();
                    serverWorld.setChunkForced(ChunkPos.getX(chunkLong), ChunkPos.getZ(chunkLong), chunkLoader.status == ChunkLoader.Status.ON && team.balance >= price);
                    if (serverWorld.getLevel().getForcedChunks().contains(chunkLong)) {
                        if (team.balance >= price) {
                            team.balance -= (price);
                            chunkLoader.status = ChunkLoader.Status.ON;
                            serverWorld.setBlockAndUpdate(chunkLoader.pos, serverWorld.getBlockState(chunkLoader.pos).setValue(Anchor.STATUS, Anchor.Status.ON));
                        } else {
                            team.balance -= (price + Config.OVERDRAFT_FEE.get());
                            chunkLoader.status = ChunkLoader.Status.STUCK;
                            serverWorld.setBlockAndUpdate(chunkLoader.pos, serverWorld.getBlockState(chunkLoader.pos).setValue(Anchor.STATUS, Anchor.Status.STUCK));
                        }
                        Teams.updateTeam(Teams.jsonFile, team);
                    } else {
                        chunkLoader.status = ChunkLoader.Status.OFF;
                        serverWorld.setBlockAndUpdate(chunkLoader.pos, serverWorld.getBlockState(chunkLoader.pos).setValue(Anchor.STATUS, Anchor.Status.OFF));
                    }
                }
            }
        }
    }

    public static void movePlayerChunkToTeam(String currentTeam, String newTeam, String player) {
        if (ServerEvents.CHUNK_MAP.containsKey(currentTeam)) {
            Iterator<ChunkLoader> iterator = ServerEvents.CHUNK_MAP.get(currentTeam).iterator();
            while (iterator.hasNext()) {
                ChunkLoader chunkLoader = iterator.next();
                if (chunkLoader.owner.equals(player)) {
                    ServerEvents.CHUNK_MAP.get(newTeam).add(chunkLoader);
                    iterator.remove();
                }
            }
        }
    }

    public static void moveAllChunksToTeam(String currentTeam, String newTeam) {
        if (ServerEvents.CHUNK_MAP.containsKey(currentTeam)) {
            Iterator<ChunkLoader> iterator = ServerEvents.CHUNK_MAP.get(currentTeam).iterator();
            while (iterator.hasNext()) {
                ChunkLoader chunkLoader = iterator.next();
                ServerEvents.CHUNK_MAP.get(newTeam).add(chunkLoader);
                iterator.remove();
            }
        }
    }

    public static void newPlayer(Player entity) {
        if (Ledger.newPlayer(Ledger.jsonFile, entity.getGameProfile().getName(), Config.INITIAL_BAL.get())) {
            entity.displayClientMessage(new TranslatableComponent("message.monetamoney.newplayer", Config.INITIAL_BAL.get()), false);
        }
    }

    public static void rewardCheck(Player entity) {
        if (System.currentTimeMillis() - Ledger.readLastReward(Ledger.jsonFile, entity.getGameProfile().getName()) > 86400000) {
            Ledger.updateLastReward(Ledger.jsonFile, entity.getGameProfile().getName());
            Ledger.addBalance(Ledger.jsonFile, entity.getGameProfile().getName(), Config.DAILY_REWARD.get());
            entity.displayClientMessage(new TranslatableComponent("message.monetamoney.login_reward", Config.DAILY_REWARD.get()), false);
        }
    }

    public static long calculateChunksCost(int chunks) {
        return (long) (Config.BASE_CHUNKLOADER.get() * Math.pow(Config.MULTIPLIER_CHUNKLOADER.get(), Math.max(chunks - 1, 0)));
    }

    public static long calculateDailyListCost(int items) {
        return (long) (Config.DAILY_LIST_FEE.get() * Math.pow(Config.MULTIPILER_DAILY_LIST.get(), Math.max(items - 1, 0)));
    }

    public static long calculateListCost(int items) {
        return (long) (Config.LIST_FEE.get() * Math.pow(Config.MULTIPILER_LIST.get(), Math.max(items - 1, 0)));
    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        Path modFolder = event.getServer().getWorldPath(new LevelResource("monetamoney"));
        Ledger.init(modFolder);
        Outstanding.init(modFolder);
        LoadedChunks.init(modFolder);
        Teams.init(modFolder);
        Auctioned.init(modFolder);
        CHUNK_MAP.putAll(LoadedChunks.read(LoadedChunks.jsonFile));
    }

    @SubscribeEvent
    public void onServerStopping(FMLServerStoppingEvent event) {
        Auctioned.write(Auctioned.auctionedFile, Auctioned.auctionedNBT);
        LoadedChunks.write(LoadedChunks.jsonFile, CHUNK_MAP);
    }

    @SubscribeEvent
    public void onWorldSave(WorldEvent.Save event) {
        Auctioned.write(Auctioned.auctionedFile, Auctioned.auctionedNBT);
        LoadedChunks.write(LoadedChunks.jsonFile, CHUNK_MAP);
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        newPlayer(event.getPlayer());
        rewardCheck(event.getPlayer());
    }

    @SubscribeEvent
    public void onItemUse(PlayerInteractEvent.RightClickBlock event) {
        if (event.getSide() == LogicalSide.SERVER && event.getPlayer().getItemInHand(event.getHand()).getItem() instanceof BlockItem && ((BlockItem) event.getPlayer().getItemInHand(event.getHand()).getItem()).getBlock().getClass() == Anchor.class) {
            BlockPos pos = event.getPos().relative(event.getHitVec().getDirection());
            for (List<ChunkLoader> values : ServerEvents.CHUNK_MAP.values()) {
                if (values.stream().anyMatch(chunkLoader -> event.getWorld().dimension().location().equals(chunkLoader.dimensionKey) && new ChunkPos(chunkLoader.pos).equals(new ChunkPos(pos)))) {
                    event.setUseItem(Event.Result.DENY);
                    PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) event.getPlayer()), new SDenyUseItem(event.getHand(), event.getPlayer().getItemInHand(event.getHand())));
                    return;
                }
            }
        }
    }

    @SubscribeEvent
    public void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof ServerPlayer && event.getPlacedBlock().getBlock().getClass() == Anchor.class) {
            Team team = Teams.getPlayersTeam(Teams.jsonFile, ((Player) event.getEntity()).getGameProfile().getName());
            String teamKey = team.isNull() ? "unlisted" : team.toString();
            ServerEvents.CHUNK_MAP.putIfAbsent(teamKey, new ArrayList<>());
            ServerEvents.CHUNK_MAP.get(teamKey).add(new ChunkLoader(event.getEntity().level.dimension().location(), event.getPos(), ((Player) event.getEntity()).getGameProfile().getName(), ChunkLoader.Status.OFF));
        }
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getState().getBlock() instanceof IOnBreak) {
            ((IOnBreak) event.getState().getBlock()).onBlockBreak((Level) event.getWorld(), event.getPos());
        }
    }

    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntityLiving() instanceof Player && event.getSource().getEntity() instanceof Player) {
            Player dead = (Player) event.getEntityLiving();
            Player attacker = (Player) event.getSource().getEntity();
            for (int i = 0; i < dead.getInventory().getContainerSize(); i++) {
                if (dead.getInventory().getItem(i).getItem() instanceof Coin) {
                    ItemStack coin = dead.getInventory().getItem(i);
                    UUID uuid = coin.getOrCreateTag().getUUID("uuid");
                    if (Outstanding.isValidCoin(Outstanding.jsonFile, uuid)) {
                        return;
                    }
                }
            }
            long balance = Ledger.readBalance(Ledger.jsonFile, dead.getGameProfile().getName());
            Ledger.addBalance(Ledger.jsonFile, attacker.getGameProfile().getName(), (long) (balance * Config.KILL_MONEY.get()));
            Ledger.addBalance(Ledger.jsonFile, dead.getGameProfile().getName(), -(long) (balance * Config.KILL_MONEY.get()));
        }
    }

    @SubscribeEvent
    public void onAdvancement(AdvancementEvent event) {
        if (event.getPlayer() != null && (Config.RECIPES_ARE_ADVANCEMENTS.get() || event.getAdvancement().getParent() == null || !event.getAdvancement().getParent().getId().equals(new ResourceLocation("minecraft:recipes/root")))) {
            Ledger.addBalance(Ledger.jsonFile, event.getPlayer().getGameProfile().getName(), Config.ADVANCEMENT_REWARD.get());
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.side == LogicalSide.SERVER) {
            if (Ledger.getLastRewardTime(Ledger.jsonFile, "previous_pay") + (1000 * 60 * 60 * 24) <= System.currentTimeMillis()) {
                Ledger.updateLastTime(Ledger.jsonFile, "previous_pay");
                loadAndPayChunks(event.world);
                collectAuctionFees();
            }
            if (Ledger.getLastRewardTime(Ledger.jsonFile, "previous_restock") + (Config.RESTOCK_TIME.get() * 60 * 1000) <= System.currentTimeMillis()) {
                Ledger.updateLastTime(Ledger.jsonFile, "previous_restock");
                Auctioned.restockShop();
            }
        }
    }
}
