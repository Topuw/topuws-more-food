package com.topuw.jarmod;

import com.mojang.math.Transformation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Prediction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.Objects;

import org.joml.Quaternionf;
import org.joml.Vector3f;

public class CuttingBoardBlock extends Block {
	public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
	private static final double ITEM_Y = 0.07;

	public CuttingBoardBlock(BlockBehaviour.Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	protected BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	protected BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return switch (state.getValue(FACING)) {
			case Direction.EAST, Direction.WEST -> Block.box(3, 0, 1, 13, 1, 15);
			default -> Block.box(1, 0, 3, 15, 1, 13);
		};
	}

	@Override
	protected InteractionResult useItemOn(
			ItemStack heldStack,
			BlockState state,
			Level level,
			BlockPos pos,
			Player player,
			InteractionHand hand,
			BlockHitResult hit
	) {
		if (heldStack.isEmpty()) {
			return collectWings(level, pos, player);
		}
		if (!level.mayInteract(player, pos)) {
			return InteractionResult.PASS;
		}

		if (heldStack.is(Items.CHICKEN)) {
			if (level.isClientSide()) {
				return InteractionResult.SUCCESS;
			}
			if (!findBoardItems(level, pos, Items.CHICKEN).isEmpty()
					|| !findBoardItems(level, pos, TopuwSJars.RAW_CHICKEN_WING).isEmpty()) {
				return InteractionResult.PASS;
			}

			addBoardItem(level, pos, new ItemStack(Items.CHICKEN), 0.0, 0.0);
			if (!player.getAbilities().instabuild) {
				heldStack.shrink(1);
			}
			return InteractionResult.SUCCESS;
		}

		if (heldStack.getItem() instanceof KnifeItem) {
			List<Display.ItemDisplay> chicken = findBoardItems(level, pos, Items.CHICKEN);
			if (chicken.isEmpty() || !findBoardItems(level, pos, TopuwSJars.RAW_CHICKEN_WING).isEmpty()) {
				return InteractionResult.PASS;
			}
			if (level.isClientSide()) {
				return InteractionResult.SUCCESS;
			}

			chicken.forEach(Display.ItemDisplay::discard);
			heldStack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND
					? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
			addBoardItem(level, pos, new ItemStack(TopuwSJars.RAW_CHICKEN_WING), -0.13, -0.11);
			addBoardItem(level, pos, new ItemStack(TopuwSJars.RAW_CHICKEN_WING), 0.13, -0.11);
			addBoardItem(level, pos, new ItemStack(TopuwSJars.RAW_CHICKEN_WING), -0.13, 0.11);
			addBoardItem(level, pos, new ItemStack(TopuwSJars.RAW_CHICKEN_WING), 0.13, 0.11);
			return InteractionResult.SUCCESS;
		}

		return InteractionResult.PASS;
	}

	@Override
	protected InteractionResult useWithoutItem(
			BlockState state,
			Level level,
			BlockPos pos,
			Player player,
			BlockHitResult hit
	) {
		return collectWings(level, pos, player);
	}

	private static InteractionResult collectWings(Level level, BlockPos pos, Player player) {
		List<Display.ItemDisplay> chicken = findBoardItems(level, pos, Items.CHICKEN);
		List<Display.ItemDisplay> wings = findBoardItems(level, pos, TopuwSJars.RAW_CHICKEN_WING);
		List<Display.ItemDisplay> boardItems = chicken.isEmpty() ? wings : chicken;
		if (boardItems.isEmpty()) {
			return InteractionResult.PASS;
		}
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}

		ItemStack stack = new ItemStack(chicken.isEmpty() ? TopuwSJars.RAW_CHICKEN_WING : Items.CHICKEN, boardItems.size());
		boardItems.forEach(Display.ItemDisplay::discard);
		if (!player.addItem(stack)) {
			player.drop(stack, false, Prediction.SERVER_ONLY);
		}
		return InteractionResult.SUCCESS;
	}

	private static void addBoardItem(Level level, BlockPos pos, ItemStack stack, double offsetX, double offsetZ) {
		EntityType<?> itemDisplayType = Objects.requireNonNull(BuiltInRegistries.ENTITY_TYPE.getValue(
				ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("minecraft", "item_display"))
		));
		Display.ItemDisplay display = new Display.ItemDisplay(itemDisplayType, level);
		display.setPos(pos.getX() + 0.5 + offsetX, pos.getY() + ITEM_Y, pos.getZ() + 0.5 + offsetZ);
		display.setNoGravity(true);
		display.setBillboardConstraints(Display.BillboardConstraints.FIXED);
		display.setItemTransform(ItemDisplayContext.FIXED);
		display.setItemStack(stack);
		display.setTransformation(new Transformation(
				new Vector3f(), new Quaternionf(), new Vector3f(0.5F), new Quaternionf()
		));
		display.setXRot(-90.0F);
		display.setYRot(0.0F);
		level.addFreshEntity(display);
	}

	private static List<Display.ItemDisplay> findBoardItems(Level level, BlockPos pos, net.minecraft.world.item.Item item) {
		AABB bounds = new AABB(
				pos.getX() - 0.25, pos.getY() - 0.25, pos.getZ() - 0.25,
				pos.getX() + 1.25, pos.getY() + 0.75, pos.getZ() + 1.25
		);
		return level.getEntitiesOfClass(Display.ItemDisplay.class, bounds).stream()
				.filter(display -> display.getItemStack().is(item))
				.filter(display -> display.getX() >= pos.getX() + 0.1 && display.getX() <= pos.getX() + 0.9)
				.filter(display -> display.getZ() >= pos.getZ() + 0.1 && display.getZ() <= pos.getZ() + 0.9)
				.toList();
	}
}
