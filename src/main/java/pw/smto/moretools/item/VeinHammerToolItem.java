package pw.smto.moretools.item;

import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.utils.PolymerClientDecoded;
import eu.pb4.polymer.core.api.utils.PolymerKeepModel;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import pw.smto.moretools.MoreTools;
import pw.smto.moretools.util.BlockBoxUtils;

import java.util.ArrayList;
import java.util.List;

public class VeinHammerToolItem extends BaseToolItem implements PolymerItem, PolymerKeepModel, PolymerClientDecoded {
    private final PolymerModelData model;
    private final int range;

    public VeinHammerToolItem(PickaxeItem base, int range) {
        super(base, BlockTags.PICKAXE_MINEABLE);
        this.model = PolymerResourcePackUtils.requestModel(base, Identifier.of(MoreTools.MOD_ID,
                "item/" + Registries.ITEM.getId(base).getPath().replace("pickaxe", "vein_hammer")));
        this.range = range;
    }

    public VeinHammerToolItem(PickaxeItem base) {
        this(base, 3);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        if (MoreTools.PLAYERS_WITH_CLIENT.contains(player)) {
            return this;
        }
        return this.model.item();
    }

    @Override
    public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return this.model.value();
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.moretools.vein_hammer.tooltip").formatted(Formatting.GOLD));
    }

    @Override
    public List<BlockPos> getAffectedArea(@Nullable World world, BlockPos pos, BlockState state, @Nullable Direction d, @Nullable Block target) {
        int range = 3;
        var targetState = target.getDefaultState();
        boolean useVanillaDirections = true;
        if (targetState.isIn(MoreTools.BlockTags.VEIN_HAMMER_APPLICABLE)) {
            range = this.range;
            useVanillaDirections = false;
        }

        List<BlockPos> result;
        if (useVanillaDirections) {
            result = BlockBoxUtils.getBlockCluster(target, pos, world, range, BlockBoxUtils.DirectionSets.CARDINAL);
        } else result = BlockBoxUtils.getBlockCluster(target, pos, world, range, BlockBoxUtils.DirectionSets.EXTENDED);

        var list = new ArrayList<BlockPos>();
        for (BlockPos blockPos : result) {
            if (world.getBlockState(blockPos).isIn(BlockTags.PICKAXE_MINEABLE)) {
                list.add(blockPos);
            }
        }
        return list;
    }

    public void doToolPower(BlockState state, BlockPos pos, Direction d, ServerPlayerEntity player, World world) {
        Block toFind = state.getBlock();
        List<BlockPos> selection = getAffectedArea(world, pos, state, d, toFind);
        BlockState blockBoxSelection;
        for (BlockPos blockBoxSelectionPos : selection) {
            blockBoxSelection = world.getBlockState(blockBoxSelectionPos);
            if (!blockBoxSelectionPos.equals(pos)) {
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
