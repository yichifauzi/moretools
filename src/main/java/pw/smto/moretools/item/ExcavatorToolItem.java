package pw.smto.moretools.item;

import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.block.BlockState;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import pw.smto.moretools.util.BlockBoxUtils;
import pw.smto.moretools.MoreTools;

public class ExcavatorToolItem extends BaseToolItem implements PolymerItem {
    private final PolymerModelData model;

    public ExcavatorToolItem(ShovelItem base) {
        super(base, BlockTags.SHOVEL_MINEABLE);
        this.model = PolymerResourcePackUtils.requestModel(base, Identifier.of(MoreTools.MOD_ID,
                "item/" + Registries.ITEM.getId(base).getPath().replace("shovel", "excavator")));
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return this.model.item();
    }

    @Override
    public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return this.model.value();
    }

    @Override
    public String getGimmickText() {
        return "Allows breaking blocks in a 3x3 radius.";
    }

    @Override
    public void doToolPower(BlockState state, BlockPos pos, Direction d, ServerPlayerEntity player, World world) {
        BlockBoxUtils.IterableBlockBox selection = BlockBoxUtils.getSurroundingBlocks(pos, d, 1);
        BlockState blockBoxSelection;
        for (BlockPos blockBoxSelectionPos : selection) {
            blockBoxSelection = world.getBlockState(blockBoxSelectionPos);
            if (!blockBoxSelectionPos.equals(pos)) {
                if (blockBoxSelection.isIn(BlockTags.SHOVEL_MINEABLE))
                {
                    blockBoxSelection.getBlock().onBreak(world, pos, blockBoxSelection, player);
                    boolean bl = world.breakBlock(blockBoxSelectionPos, false);
                    if (bl) {
                        blockBoxSelection.getBlock().onBroken(world, pos, blockBoxSelection);
                    }
                    if (!player.isCreative()) {
                        ItemStack itemStack = player.getMainHandStack();
                        ItemStack itemStack2 = itemStack.copy();
                        boolean bl2 = player.canHarvest(blockBoxSelection);
                        itemStack.postMine(world, blockBoxSelection, pos, player);
                        if (bl && bl2) {
                            blockBoxSelection.getBlock().afterBreak(world, player, pos, blockBoxSelection, world.getBlockEntity(blockBoxSelectionPos), itemStack2);
                        }
                    }
                }
            }
        }
    }
}
