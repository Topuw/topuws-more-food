package com.topuw.jarmod;

import com.topuw.jarmod.mixin.CauldronInteractionDispatcherAccessor;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.cauldron.CauldronInteractions;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.food.Foods;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

public class TopuwSJars implements ModInitializer {
	public static final String MOD_ID = "topuws-jars";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final CauldronInteraction.Dispatcher JAM_CAULDRON_INTERACTIONS = new CauldronInteraction.Dispatcher();

	public static final Item JAR = registerItem("jar", JarItem::new, new Item.Properties().stacksTo(16));
	public static final Item WATER_JAR = registerItem("water_jar", Item::new, new Item.Properties()
			.stacksTo(16)
			.component(DataComponents.CONSUMABLE, Consumables.DEFAULT_DRINK)
			.usingConvertsTo(JAR));
	public static final Item HONEY_JAR = registerItem("honey_jar", Item::new, new Item.Properties()
			.stacksTo(16)
			.food(Foods.HONEY_BOTTLE, Consumables.HONEY_BOTTLE)
			.usingConvertsTo(JAR));

	public static final FoodProperties JAM_FOOD = new FoodProperties.Builder()
			.nutrition(6)
			.saturationModifier(0.8F)
			.alwaysEdible()
			.build();

	public static final Item JAM_JAR = registerItem("jam_jar", Item::new, new Item.Properties()
			.stacksTo(16)
			.food(JAM_FOOD, Consumables.HONEY_BOTTLE)
			.usingConvertsTo(JAR));

	public static final FoodProperties JAM_BREAD_FOOD = new FoodProperties.Builder()
			.nutrition(8)
			.saturationModifier(0.8F)
			.build();

	public static final Item JAM_BREAD = registerItem("jam_bread", Item::new, new Item.Properties()
			.food(JAM_BREAD_FOOD, Consumables.DEFAULT_FOOD));

	public static final FoodProperties FRIED_EGG_FOOD = new FoodProperties.Builder()
			.nutrition(5)
			.saturationModifier(0.6F)
			.build();

	public static final Item FRIED_EGG = registerItem("fried_egg", Item::new, new Item.Properties()
			.food(FRIED_EGG_FOOD, Consumables.DEFAULT_FOOD));

	public static final Item RAW_CHICKEN_WING = registerItem("raw_chicken_wing", Item::new, new Item.Properties()
			.food(new FoodProperties.Builder().nutrition(1).saturationModifier(0.1F).build(), Consumables.DEFAULT_FOOD));
	public static final Item COOKED_CHICKEN_WING = registerItem("cooked_chicken_wing", Item::new, new Item.Properties()
			.food(new FoodProperties.Builder().nutrition(3).saturationModifier(0.3F).build(), Consumables.DEFAULT_FOOD));

	public static final Item WOODEN_KNIFE = registerItem("wooden_knife", KnifeItem::new, new Item.Properties().durability(59));
	public static final Item STONE_KNIFE = registerItem("stone_knife", KnifeItem::new, new Item.Properties().durability(131));
	public static final Item COPPER_KNIFE = registerItem("copper_knife", KnifeItem::new, new Item.Properties().durability(190));
	public static final Item IRON_KNIFE = registerItem("iron_knife", KnifeItem::new, new Item.Properties().durability(250));
	public static final Item GOLDEN_KNIFE = registerItem("golden_knife", KnifeItem::new, new Item.Properties().durability(32));
	public static final Item DIAMOND_KNIFE = registerItem("diamond_knife", KnifeItem::new, new Item.Properties().durability(1561));
	public static final Item NETHERITE_KNIFE = registerItem("netherite_knife", KnifeItem::new, new Item.Properties().durability(2031));

	public static final Block JAM_CAULDRON = registerBlock("jam_cauldron", properties ->
			new LayeredCauldronBlock(
					(Biome.Precipitation) null,
					JAM_CAULDRON_INTERACTIONS,
					properties
			), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER_CAULDRON));

	public static final Block CUTTING_BOARD = registerBlock(
			"cutting_board",
			CuttingBoardBlock::new,
			BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion()
	);
	public static final Item CUTTING_BOARD_ITEM = registerBlockItem("cutting_board", CUTTING_BOARD);

	// Alias fields for compatibility
	public static final Item NORMAL_JAR_ITEM = JAR;
	public static final Item WATER_JAR_ITEM = WATER_JAR;

	public static final ResourceKey<CreativeModeTab> JARS_TAB_KEY = ResourceKey.create(
			Registries.CREATIVE_MODE_TAB,
			Identifier.fromNamespaceAndPath(MOD_ID, "jars")
	);

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Topuw's Food Mod!");

		Registry.register(
				BuiltInRegistries.CREATIVE_MODE_TAB,
				JARS_TAB_KEY,
				FabricCreativeModeTab.builder()
						.title(Component.translatable("itemGroup.topuws-jars.jars"))
						.icon(() -> new ItemStack(JAR))
						.displayItems((displayParameters, output) -> {
							output.accept(JAR);
							output.accept(WATER_JAR);
							output.accept(HONEY_JAR);
							output.accept(JAM_JAR);
							output.accept(JAM_BREAD);
							output.accept(FRIED_EGG);
							output.accept(CUTTING_BOARD_ITEM);
							output.accept(RAW_CHICKEN_WING);
							output.accept(COOKED_CHICKEN_WING);
							output.accept(WOODEN_KNIFE);
							output.accept(STONE_KNIFE);
							output.accept(COPPER_KNIFE);
							output.accept(IRON_KNIFE);
							output.accept(GOLDEN_KNIFE);
							output.accept(DIAMOND_KNIFE);
							output.accept(NETHERITE_KNIFE);
						})
						.build()
		);

		setupCauldronInteractions();

		// Allow Water Jar to be caught when fishing
		LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
			if (source.isBuiltin()) {
				if (BuiltInLootTables.FISHING_JUNK.equals(key)) {
					tableBuilder.modifyPools(poolBuilder -> poolBuilder.add(LootItem.lootTableItem(WATER_JAR).setWeight(15)));
				} else if (BuiltInLootTables.FISHING_FISH.equals(key)) {
					tableBuilder.modifyPools(poolBuilder -> poolBuilder.add(LootItem.lootTableItem(WATER_JAR).setWeight(5)));
				}
			}
		});
	}

	private static void addCauldronInteraction(CauldronInteraction.Dispatcher dispatcher, Item item, CauldronInteraction interaction) {
		((CauldronInteractionDispatcherAccessor) dispatcher).topuwsJars$put(item, interaction);
	}

	private static void setupCauldronInteractions() {
		// Fill empty cauldron with Water Jar
		addCauldronInteraction(CauldronInteractions.EMPTY, WATER_JAR, (state, level, pos, player, hand, itemInHand) -> {
			if (!level.isClientSide()) {
				player.setItemInHand(hand, ItemUtils.createFilledResult(itemInHand, player, new ItemStack(JAR)));
				player.awardStat(Stats.USE_CAULDRON);
				player.awardStat(Stats.ITEM_USED.get(WATER_JAR));
				level.setBlockAndUpdate(pos, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 1));
				level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
				level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
			}
			return InteractionResult.SUCCESS;
		});

		// Add Water Jar to existing water cauldron
		addCauldronInteraction(CauldronInteractions.WATER, WATER_JAR, (state, level, pos, player, hand, itemInHand) -> {
			int currentLevel = state.getValue(LayeredCauldronBlock.LEVEL);
			if (currentLevel < 3) {
				if (!level.isClientSide()) {
					player.setItemInHand(hand, ItemUtils.createFilledResult(itemInHand, player, new ItemStack(JAR)));
					player.awardStat(Stats.USE_CAULDRON);
					player.awardStat(Stats.ITEM_USED.get(WATER_JAR));
					level.setBlockAndUpdate(pos, state.setValue(LayeredCauldronBlock.LEVEL, currentLevel + 1));
					level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
					level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
				}
				return InteractionResult.SUCCESS;
			}
			return InteractionResult.PASS;
		});

		// Fill empty cauldron with Jam Jar
		addCauldronInteraction(CauldronInteractions.EMPTY, JAM_JAR, (state, level, pos, player, hand, itemInHand) -> {
			if (!level.isClientSide()) {
				player.setItemInHand(hand, ItemUtils.createFilledResult(itemInHand, player, new ItemStack(JAR)));
				player.awardStat(Stats.USE_CAULDRON);
				player.awardStat(Stats.ITEM_USED.get(JAM_JAR));
				level.setBlockAndUpdate(pos, JAM_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 1));
				level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
				level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
			}
			return InteractionResult.SUCCESS;
		});

		// Add Jam Jar to existing jam cauldron
		addCauldronInteraction(JAM_CAULDRON_INTERACTIONS, JAM_JAR, (state, level, pos, player, hand, itemInHand) -> {
			int currentLevel = state.getValue(LayeredCauldronBlock.LEVEL);
			if (currentLevel < 3) {
				if (!level.isClientSide()) {
					player.setItemInHand(hand, ItemUtils.createFilledResult(itemInHand, player, new ItemStack(JAR)));
					player.awardStat(Stats.USE_CAULDRON);
					player.awardStat(Stats.ITEM_USED.get(JAM_JAR));
					level.setBlockAndUpdate(pos, state.setValue(LayeredCauldronBlock.LEVEL, currentLevel + 1));
					level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
					level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
				}
				return InteractionResult.SUCCESS;
			}
			return InteractionResult.PASS;
		});

		// Collect water using Jar from Water Cauldron
		addCauldronInteraction(CauldronInteractions.WATER, JAR, (state, level, pos, player, hand, itemInHand) -> {
			if (!level.isClientSide()) {
				Item usedItem = itemInHand.getItem();
				player.setItemInHand(hand, ItemUtils.createFilledResult(itemInHand, player, new ItemStack(WATER_JAR)));
				player.awardStat(Stats.USE_CAULDRON);
				player.awardStat(Stats.ITEM_USED.get(usedItem));
				LayeredCauldronBlock.lowerFillLevel(state, level, pos);
				level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
				level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
			}
			return InteractionResult.SUCCESS;
		});

		// Put sweet berries into water cauldron to turn it into jam
		addCauldronInteraction(CauldronInteractions.WATER, Items.SWEET_BERRIES, (state, level, pos, player, hand, itemInHand) -> {
			if (!level.isClientSide()) {
				itemInHand.consume(1, player);
				player.awardStat(Stats.USE_CAULDRON);
				player.awardStat(Stats.ITEM_USED.get(Items.SWEET_BERRIES));

				int currentLevel = state.getValue(LayeredCauldronBlock.LEVEL);
				BlockState jamState = JAM_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, currentLevel);
				level.setBlockAndUpdate(pos, jamState);
				level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 1.0F);
				level.gameEvent(null, GameEvent.BLOCK_CHANGE, pos);
			}
			return InteractionResult.SUCCESS;
		});

		// Add sweet berries to jam cauldron to increase fill level (if not full)
		addCauldronInteraction(JAM_CAULDRON_INTERACTIONS, Items.SWEET_BERRIES, (state, level, pos, player, hand, itemInHand) -> {
			int currentLevel = state.getValue(LayeredCauldronBlock.LEVEL);
			if (currentLevel < 3) {
				if (!level.isClientSide()) {
					itemInHand.consume(1, player);
					player.awardStat(Stats.USE_CAULDRON);
					player.awardStat(Stats.ITEM_USED.get(Items.SWEET_BERRIES));
					BlockState higherState = state.setValue(LayeredCauldronBlock.LEVEL, currentLevel + 1);
					level.setBlockAndUpdate(pos, higherState);
					level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 1.0F);
					level.gameEvent(null, GameEvent.BLOCK_CHANGE, pos);
				}
				return InteractionResult.SUCCESS;
			}
			return InteractionResult.PASS;
		});

		// Collect jam using Jar
		addCauldronInteraction(JAM_CAULDRON_INTERACTIONS, JAR, (state, level, pos, player, hand, itemInHand) -> {
			if (!level.isClientSide()) {
				Item usedItem = itemInHand.getItem();
				player.setItemInHand(hand, ItemUtils.createFilledResult(itemInHand, player, new ItemStack(JAM_JAR)));
				player.awardStat(Stats.USE_CAULDRON);
				player.awardStat(Stats.ITEM_USED.get(usedItem));
				LayeredCauldronBlock.lowerFillLevel(state, level, pos);
				level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
				level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
			}
			return InteractionResult.SUCCESS;
		});

		// Dip bread into jam cauldron to get Jam Bread
		addCauldronInteraction(JAM_CAULDRON_INTERACTIONS, Items.BREAD, (state, level, pos, player, hand, itemInHand) -> {
			if (!level.isClientSide()) {
				player.setItemInHand(hand, ItemUtils.createFilledResult(itemInHand, player, new ItemStack(JAM_BREAD)));
				player.awardStat(Stats.USE_CAULDRON);
				player.awardStat(Stats.ITEM_USED.get(Items.BREAD));
				LayeredCauldronBlock.lowerFillLevel(state, level, pos);
				level.playSound(null, pos, SoundEvents.HONEY_DRINK.value(), SoundSource.BLOCKS, 1.0F, 1.0F);
				level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
			}
			return InteractionResult.SUCCESS;
		});
	}

	private static <T extends Item> T registerItem(String name, Function<Item.Properties, T> factory, Item.Properties properties) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, name));
		T item = factory.apply(properties.setId(key));
		return Registry.register(BuiltInRegistries.ITEM, key, item);
	}

	private static <T extends Block> T registerBlock(String name, Function<BlockBehaviour.Properties, T> factory, BlockBehaviour.Properties properties) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MOD_ID, name));
		T block = factory.apply(properties.setId(key));
		return Registry.register(BuiltInRegistries.BLOCK, key, block);
	}

	private static BlockItem registerBlockItem(String name, Block block) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, name));
		return Registry.register(BuiltInRegistries.ITEM, key, new BlockItem(block, new Item.Properties().setId(key)));
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	public static class JarItem extends Item {
		public JarItem(Properties properties) {
			super(properties);
		}

		@Override
		public InteractionResult use(Level level, Player player, InteractionHand hand) {
			ItemStack itemStack = player.getItemInHand(hand);
			BlockHitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
			if (hitResult.getType() == HitResult.Type.MISS) {
				return InteractionResult.PASS;
			}
			if (hitResult.getType() == HitResult.Type.BLOCK) {
				BlockPos blockPos = hitResult.getBlockPos();
				if (!level.mayInteract(player, blockPos)) {
					return InteractionResult.PASS;
				}
				if (level.getFluidState(blockPos).is(FluidTags.WATER)) {
					level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.BOTTLE_FILL, SoundSource.NEUTRAL, 1.0F, 1.0F);
					level.gameEvent(player, GameEvent.FLUID_PICKUP, blockPos);
					ItemStack filledStack = ItemUtils.createFilledResult(itemStack, player, new ItemStack(WATER_JAR));
					return InteractionResult.SUCCESS.heldItemTransformedTo(filledStack);
				}
			}
			return InteractionResult.PASS;
		}

		@Override
		public InteractionResult useOn(UseOnContext context) {
			Level level = context.getLevel();
			BlockPos pos = context.getClickedPos();
			BlockState state = level.getBlockState(pos);
			if (state.hasProperty(BeehiveBlock.HONEY_LEVEL)) {
				int honey = state.getValue(BeehiveBlock.HONEY_LEVEL);
				if (honey >= 5) {
					Player player = context.getPlayer();
					level.playSound(player, player != null ? player.getX() : pos.getX(), player != null ? player.getY() : pos.getY(), player != null ? player.getZ() : pos.getZ(), SoundEvents.BOTTLE_FILL, SoundSource.NEUTRAL, 1.0F, 1.0F);
					level.gameEvent(player, GameEvent.FLUID_PICKUP, pos);
					level.setBlockAndUpdate(pos, state.setValue(BeehiveBlock.HONEY_LEVEL, 0));
					if (!CampfireBlock.isSmokeyPos(level, pos)) {
						if (level.getBlockEntity(pos) instanceof BeehiveBlockEntity beehiveBlockEntity) {
							beehiveBlockEntity.emptyAllLivingFromHive(player, state, BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY);
						}
					}
					if (player != null) {
						ItemStack itemStack = context.getItemInHand();
						ItemStack honeyJar = ItemUtils.createFilledResult(itemStack, player, new ItemStack(TopuwSJars.HONEY_JAR));
						player.setItemInHand(context.getHand(), honeyJar);
					}
					return InteractionResult.SUCCESS;
				}
			}
			return super.useOn(context);
		}
	}
}
