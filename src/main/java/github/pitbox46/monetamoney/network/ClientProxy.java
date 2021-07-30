package github.pitbox46.monetamoney.network;

import github.pitbox46.monetamoney.data.Auctioned;
import github.pitbox46.monetamoney.network.server.*;
import github.pitbox46.monetamoney.screen.AnchorScreen;
import github.pitbox46.monetamoney.screen.IStatusable;
import github.pitbox46.monetamoney.screen.vault.*;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClientProxy extends CommonProxy {
    //List fee
    public static long fee;
    public static long dailyFee;

    public static long personalBalance = 0;
    public static long teamBalance = 0;

    public ClientProxy() {
        super();
        MinecraftForge.EVENT_BUS.register(this);
    }
}
