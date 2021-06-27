package youyihj.zenutils.api.cotx.tile;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import youyihj.zenutils.ZenUtils;
import youyihj.zenutils.impl.util.InternalUtils;

/**
 * @author youyihj
 */
@Mod.EventBusSubscriber
public class TileEntityRegistryHandler {
    @SubscribeEvent
    public static void handle(RegistryEvent.Register<Block> event) {
        if (InternalUtils.isContentTweakerInstalled()) {
            GameRegistry.registerTileEntity(TileEntityContent.class, new ResourceLocation(ZenUtils.MODID, "tile_entity"));
        }
    }
}
