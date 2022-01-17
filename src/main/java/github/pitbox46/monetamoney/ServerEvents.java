package github.pitbox46.monetamoney;

import github.pitbox46.monetamoney.blocks.Anchor;
import github.pitbox46.monetamoney.blocks.IOnBreak;
import github.pitbox46.monetamoney.data.*;
import github.pitbox46.monetamoney.items.Coin;
import github.pitbox46.monetamoney.network.PacketHandler;
import github.pitbox46.monetamoney.network.server.SDenyUseItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.FolderName;
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
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;

public class ServerEvents {
    public static final Map<String, List<ChunkLoader>> CHUNK_MAP = new HashMap<>();

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        Path modFolder = event.getServer().func_240776_a_(new FolderName("monetamoney"));
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
        if(event.getSide() == LogicalSide.SERVER && event.getPlayer().getHeldItem(event.getHand()).getItem() instanceof BlockItem && ((BlockItem) event.getPlayer().getHeldItem(event.getHand()).getItem()).getBlock().getClass() == Anchor.class) {
            BlockPos pos = event.getPos().offset(event.getHitVec().getFace());
            for(List<ChunkLoader> values: ServerEvents.CHUNK_MAP.values()) {
                if(values.stream().anyMatch(chunkLoader -> event.getWorld().getDimensionKey().getLocation().equals(chunkLoader.dimensionKey) && new ChunkPos(chunkLoader.pos).equals(new ChunkPos(pos)))) {
                    event.setUseItem(Event.Result.DENY);
                    PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) event.getPlayer()), new SDenyUseItem(event.getHand(), event.getPlayer().getHeldItem(event.getHand())));
                    return;
                }
            }
        }
    }

    @SubscribeEvent
    public void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if(event.getEntity() instanceof ServerPlayerEntity && event.getPlacedBlock().getBlock().getClass() == Anchor.class) {
            Team team = Teams.getPlayersTeam(Teams.jsonFile, ((PlayerEntity) event.getEntity()).getGameProfile().getName());
            String teamKey = team.isNull() ? "unlisted" : team.toString();
            ServerEvents.CHUNK_MAP.putIfAbsent(teamKey, new ArrayList<>());
            ServerEvents.CHUNK_MAP.get(teamKey).add(new ChunkLoader(event.getEntity().world.getDimensionKey().getLocation(), event.getPos(), ((PlayerEntity) event.getEntity()).getGameProfile().getName(), ChunkLoader.Status.OFF));
        }
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if(event.getState().getBlock() instanceof IOnBreak) {
            ((IOnBreak) event.getState().getBlock()).onBlockBreak((World) event.getWorld(), event.getPos());
        }
    }

    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event) {
        if(event.getEntityLiving() instanceof PlayerEntity && event.getSource().getTrueSource() instanceof PlayerEntity) {
            PlayerEntity dead = (PlayerEntity) event.getEntityLiving();
            PlayerEntity attacker = (PlayerEntity) event.getSource().getTrueSource();
            for(int i = 0; i < dead.inventory.getSizeInventory(); i++) {
                if(dead.inventory.getStackInSlot(i).getItem() instanceof Coin) {
                    ItemStack coin = dead.inventory.getStackInSlot(i);
                    UUID uuid = coin.getOrCreateTag().getUniqueId("uuid");
                    if(Outstanding.isValidCoin(Outstanding.jsonFile, uuid)) {
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
        if(event.getPlayer() != null && (Config.RECIPES_ARE_ADVANCEMENTS.get() || event.getAdvancement().getParent() == null || !event.getAdvancement().getParent().getId().equals(new ResourceLocation("minecraft:recipes/root")))) {
            Ledger.addBalance(Ledger.jsonFile, event.getPlayer().getGameProfile().getName(), Config.ADVANCEMENT_REWARD.get());
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.WorldTickEvent event) {
        if(event.phase == TickEvent.Phase.END && event.side == LogicalSide.SERVER) {
            if(Ledger.getLastRewardTime(Ledger.jsonFile, "previous_pay") + (1000 * 60 * 60 * 24) <= System.currentTimeMillis()) {
                Ledger.updateLastTime(Ledger.jsonFile, "previous_pay");
                loadAndPayChunks(event.world);
                collectAuctionFees();
            }
            if(Ledger.getLastRewardTime(Ledger.jsonFile, "previous_restock") + (Config.RESTOCK_TIME.get() * 60 * 1000) <= System.currentTimeMillis()) {
                Ledger.updateLastTime(Ledger.jsonFile, "previous_restock");
                Auctioned.restockShop();
            }
        }
    }

    /* Helper methods */
    public static void collectAuctionFees() {
        ListNBT auctionList = (ListNBT) Auctioned.auctionedNBT.get("auction");
        assert auctionList != null;

        Map<String,Integer> itemsByPlayer = auctionList.stream().collect((Supplier<HashMap<String, Integer>>) HashMap::new, (map, inbt) -> map.put(((CompoundNBT) inbt).getString("owner"), map.getOrDefault(((CompoundNBT) inbt).getString("owner"), 0) + 1), HashMap::putAll);

        auctionList.removeIf(inbt -> {
            CompoundNBT nbt = (CompoundNBT) inbt;
            long price = calculateDailyListCost(itemsByPlayer.getOrDefault(nbt.getString("owner"), 0));
            boolean flag = Ledger.readBalance(Ledger.jsonFile, nbt.getString("owner")) < price;
            if(!flag) Ledger.addBalance(Ledger.jsonFile, nbt.getString("owner"), -price);
            return flag;
        });
    }

    public static void loadNewChunk(World world, Team team, ChunkLoader newChunk) {
        ServerWorld serverWorld = world.getServer().getWorld(RegistryKey.getOrCreateKey(Registry.WORLD_KEY, newChunk.dimensionKey));
        long price = Config.BASE_CHUNKLOADER.get();

        Map<String,Integer> chunksByPlayer = CHUNK_MAP.values().stream().flatMap(Collection::stream).collect((Supplier<HashMap<String, Integer>>) HashMap::new, (map, chunkLoader) -> map.put(chunkLoader.owner, map.getOrDefault(chunkLoader.owner, 0) + 1), HashMap::putAll);

        for(Map.Entry<String,List<ChunkLoader>> entry: CHUNK_MAP.entrySet()) {
            /* Unlisted is all the chunks that don't belong to a team, but belong to an owner. These chunks cannot be loaded */
            if(entry.getKey().equals("unlisted")) continue;

            if(serverWorld != null && team.equals(Teams.getTeam(Teams.jsonFile, entry.getKey())) && newChunk.status == ChunkLoader.Status.ON) {
                price = calculateChunksCost(chunksByPlayer.getOrDefault(newChunk.owner, 0));
            }
        }
        if(newChunk.status == ChunkLoader.Status.ON) {
            long chunkLong = new ChunkPos(newChunk.pos).asLong();
            serverWorld.forceChunk(ChunkPos.getX(chunkLong), ChunkPos.getZ(chunkLong), newChunk.status == ChunkLoader.Status.ON && team.balance >= price);
            if (serverWorld.getWorld().getForcedChunks().contains(chunkLong)) {
                if(team.balance >= price) {
                    team.balance -= (price);
                    newChunk.status = ChunkLoader.Status.ON;
                    serverWorld.setBlockState(newChunk.pos, serverWorld.getBlockState(newChunk.pos).with(Anchor.STATUS, Anchor.Status.ON));
                } else {
                    team.balance -= (price + Config.OVERDRAFT_FEE.get());
                    newChunk.status = ChunkLoader.Status.STUCK;
                    serverWorld.setBlockState(newChunk.pos, serverWorld.getBlockState(newChunk.pos).with(Anchor.STATUS, Anchor.Status.STUCK));
                }
                Teams.updateTeam(Teams.jsonFile, team);
            } else {
                newChunk.status = ChunkLoader.Status.OFF;
                serverWorld.setBlockState(newChunk.pos, serverWorld.getBlockState(newChunk.pos).with(Anchor.STATUS, Anchor.Status.OFF));
            }
        }
    }

    public static void loadAndPayChunks(World world) {
        Map<String,Integer> chunksByPlayer = CHUNK_MAP.values().stream().flatMap(Collection::stream).collect((Supplier<HashMap<String, Integer>>) HashMap::new, (map, chunkLoader) -> map.put(chunkLoader.owner, map.getOrDefault(chunkLoader.owner, 0) + 1), HashMap::putAll);

        for(Map.Entry<String,List<ChunkLoader>> entry: CHUNK_MAP.entrySet()) {
            /* Unlisted is all the chunks that don't belong to a team, but belong to an owner. These chunks cannot be loaded */
            if(entry.getKey().equals("unlisted")) continue;

            Iterator<ChunkLoader> iterator = entry.getValue().iterator();
            while(iterator.hasNext()) {
                ChunkLoader chunkLoader = iterator.next();
                ServerWorld serverWorld = world.getServer().getWorld(RegistryKey.getOrCreateKey(Registry.WORLD_KEY, chunkLoader.dimensionKey));
                if(serverWorld != null) {
                    //Should repair any issues with syncing between loaders list and the world
                    if(serverWorld.getBlockState(chunkLoader.pos).getBlock().getClass() != Anchor.class) {
                        iterator.remove();
                        chunksByPlayer.put(chunkLoader.owner, chunksByPlayer.get(chunkLoader.owner) - 1);
                        continue;
                    }
                    Team team = Teams.getTeam(Teams.jsonFile, entry.getKey());
                    long price = calculateChunksCost(chunksByPlayer.getOrDefault(chunkLoader.owner, 0));

                    long chunkLong = new ChunkPos(chunkLoader.pos).asLong();
                    serverWorld.forceChunk(ChunkPos.getX(chunkLong), ChunkPos.getZ(chunkLong), chunkLoader.status == ChunkLoader.Status.ON && team.balance >= price);
                    if (serverWorld.getWorld().getForcedChunks().contains(chunkLong)) {
                        if(team.balance >= price) {
                            team.balance -= (price);
                            chunkLoader.status = ChunkLoader.Status.ON;
                            serverWorld.setBlockState(chunkLoader.pos, serverWorld.getBlockState(chunkLoader.pos).with(Anchor.STATUS, Anchor.Status.ON));
                        } else {
                            team.balance -= (price + Config.OVERDRAFT_FEE.get());
                            chunkLoader.status = ChunkLoader.Status.STUCK;
                            serverWorld.setBlockState(chunkLoader.pos, serverWorld.getBlockState(chunkLoader.pos).with(Anchor.STATUS, Anchor.Status.STUCK));
                        }
                        Teams.updateTeam(Teams.jsonFile, team);
                    } else {
                        chunkLoader.status = ChunkLoader.Status.OFF;
                        serverWorld.setBlockState(chunkLoader.pos, serverWorld.getBlockState(chunkLoader.pos).with(Anchor.STATUS, Anchor.Status.OFF));
                    }
                }
            }
        }
    }

    public static void movePlayerChunkToTeam(String currentTeam, String newTeam, String player) {
        if(ServerEvents.CHUNK_MAP.containsKey(currentTeam)) {
            Iterator<ChunkLoader> iterator = ServerEvents.CHUNK_MAP.get(currentTeam).iterator();
            while(iterator.hasNext()) {
                ChunkLoader chunkLoader = iterator.next();
                if(chunkLoader.owner.equals(player)) {
                    ServerEvents.CHUNK_MAP.get(newTeam).add(chunkLoader);
                    iterator.remove();
                }
            }
        }
    }

    public static void moveAllChunksToTeam(String currentTeam, String newTeam) {
        if(ServerEvents.CHUNK_MAP.containsKey(currentTeam)) {
            Iterator<ChunkLoader> iterator = ServerEvents.CHUNK_MAP.get(currentTeam).iterator();
            while(iterator.hasNext()) {
                ChunkLoader chunkLoader = iterator.next();
                ServerEvents.CHUNK_MAP.get(newTeam).add(chunkLoader);
                iterator.remove();
            }
        }
    }

    public static void newPlayer(PlayerEntity entity) {
        if(Ledger.newPlayer(Ledger.jsonFile, entity.getGameProfile().getName(), Config.INITIAL_BAL.get())) {
            entity.sendStatusMessage(new TranslationTextComponent("message.monetamoney.newplayer", Config.INITIAL_BAL.get()), false);
        }
    }

    public static void rewardCheck(PlayerEntity entity) {
        if(System.currentTimeMillis() - Ledger.readLastReward(Ledger.jsonFile, entity.getGameProfile().getName()) > 86400000) {
            Ledger.updateLastReward(Ledger.jsonFile, entity.getGameProfile().getName());
            Ledger.addBalance(Ledger.jsonFile, entity.getGameProfile().getName(), Config.DAILY_REWARD.get());
            entity.sendStatusMessage(new TranslationTextComponent("message.monetamoney.login_reward", Config.DAILY_REWARD.get()), false);
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
}
