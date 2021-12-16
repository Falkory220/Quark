package vazkii.quark.content.tools.item;

import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vazkii.arl.util.ItemNBTHelper;
import vazkii.quark.api.ITrowelable;
import vazkii.quark.api.IUsageTickerOverride;
import vazkii.quark.base.item.QuarkItem;
import vazkii.quark.base.module.ModuleLoader;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.content.tools.module.SeedPouchModule;

public class SeedPouchItem extends QuarkItem implements IUsageTickerOverride, ITrowelable {

	public static final String TAG_STORED_ITEM = "storedItem";
	public static final String TAG_COUNT = "itemCount";

	private static final int BAR_COLOR = Mth.color(0.4F, 0.4F, 1.0F);

	public SeedPouchItem(QuarkModule module) {
		super("seed_pouch", module, 
				new Item.Properties()
				.stacksTo(1)
				.durability(SeedPouchModule.maxItems + 1)
				.tab(CreativeModeTab.TAB_TOOLS));
	}

	@Override
	public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack incoming, Slot slot, ClickAction action, Player player, SlotAccess accessor) {
		if(action == ClickAction.SECONDARY) {
			if(!incoming.isEmpty())
				return addItemToMe(player, stack, incoming, slot);
			else return removeItemFromMe(player, stack, slot, accessor);
		}

		return false;
	}

	@Override
	public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
		if(action == ClickAction.SECONDARY) {
			ItemStack incoming = slot.getItem();
			if(!incoming.isEmpty())
				return addItemToMe(player, stack, incoming, slot);
		}

		return false;
	}

	public static boolean addItemToMe(Player player, ItemStack stack, ItemStack incoming, Slot slot) {
		if(slot.mayPickup(player) && slot.mayPlace(stack) && canTakeItem(stack, incoming)) {
			Pair<ItemStack, Integer> contents = getContents(stack);

			if(contents == null) {
				setItemStack(stack, incoming);
				incoming.setCount(0);
			} else {
				int curr = contents.getRight();
				int missing = SeedPouchModule.maxItems - curr;
				int incCount = incoming.getCount();
				int toDrop = Math.min(incCount, missing);

				setCount(stack, curr + toDrop);
				incoming.setCount(incCount - toDrop);
			}

			playInsertSound(player);
			return true;
		}

		return false;
	}

	private static boolean removeItemFromMe(Player player, ItemStack stack, Slot slot, SlotAccess accessor) {
		Pair<ItemStack, Integer> contents = SeedPouchItem.getContents(stack);

		if(contents != null && slot.allowModification(player)) {
			ItemStack held = accessor.get();
			ItemStack seed = contents.getLeft();
			int pouchCount = contents.getRight();

			if(held.isEmpty()) {
				int takeOut = Math.min(seed.getMaxStackSize(), contents.getRight());

				ItemStack result = seed.copy();
				result.setCount(takeOut);
				accessor.set(result);

				SeedPouchItem.setCount(stack, pouchCount - takeOut);

				playRemoveOneSound(player);
				return true;
			}
		}

		return false;
	}

	// vanilla copy
	private static void playRemoveOneSound(Entity p_186343_) {
		p_186343_.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + p_186343_.getLevel().getRandom().nextFloat() * 0.4F);
	}

	private static void playInsertSound(Entity p_186352_) {
		p_186352_.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + p_186352_.getLevel().getRandom().nextFloat() * 0.4F);
	}


	@OnlyIn(Dist.CLIENT)
	public static float itemFraction(ItemStack stack, ClientLevel world, LivingEntity entityIn, int i) {
		if(entityIn instanceof Player) {
			Player player = (Player) entityIn;
			if(player.containerMenu != null) {
				ItemStack held = player.containerMenu.getCarried();

				if(canTakeItem(stack, held))
					return 0F;
			}
		} 

		Pair<ItemStack, Integer> contents = getContents(stack);
		if(contents == null)
			return 0F;

		return (float) contents.getRight() / (float) SeedPouchModule.maxItems;
	}

	public static Pair<ItemStack, Integer> getContents(ItemStack stack) {
		CompoundTag nbt = ItemNBTHelper.getCompound(stack, TAG_STORED_ITEM, true);
		if(nbt == null)
			return null;

		ItemStack contained = ItemStack.of(nbt);
		int count = ItemNBTHelper.getInt(stack, TAG_COUNT, 0);
		return Pair.of(contained, count);
	}

	public static boolean canTakeItem(ItemStack stack, ItemStack incoming) {
		Pair<ItemStack, Integer> contents = getContents(stack);

		if(contents == null)
			return incoming.is(SeedPouchModule.seedPouchHoldableTag);

		return contents.getRight() < SeedPouchModule.maxItems && ItemStack.isSame(incoming, contents.getLeft());
	}

	public static void setItemStack(ItemStack stack, ItemStack target) {
		ItemStack copy = target.copy();
		copy.setCount(1);

		CompoundTag nbt = new CompoundTag();
		copy.save(nbt);

		ItemNBTHelper.setCompound(stack, TAG_STORED_ITEM, nbt);
		setCount(stack, target.getCount());
	}

	public static void setCount(ItemStack stack, int count) {
		if(count <= 0) {
			stack.getTag().remove(TAG_STORED_ITEM);
			stack.setDamageValue(0);

			return;
		}

		ItemNBTHelper.setInt(stack, TAG_COUNT, count);
		stack.setDamageValue(SeedPouchModule.maxItems + 1 - count);
	}

	@Override
	public int getBarColor(ItemStack stack) {
		return BAR_COLOR;
	}

	@Override
	public Component getName(ItemStack stack) {
		Component base = super.getName(stack);

		Pair<ItemStack, Integer> contents = getContents(stack);
		if(contents == null)
			return base;

		MutableComponent comp = base.copy();
		comp.append(new TextComponent(" ("));
		comp.append(contents.getLeft().getHoverName());
		comp.append(new TextComponent(")"));
		return comp;
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		ItemStack stack = context.getItemInHand();
		Pair<ItemStack, Integer> contents = getContents(stack);
		if(contents != null) {
			ItemStack target = contents.getLeft().copy();
			int total = contents.getRight();

			target.setCount(Math.min(target.getMaxStackSize(), total));

			Player player = context.getPlayer();
			if(!player.isShiftKeyDown())
				return placeSeed(context, target, context.getClickedPos(), total);

			else {
				InteractionResult bestRes = InteractionResult.FAIL;

				int range = SeedPouchModule.shiftRange;
				int blocks = range * range;
				int shift = -((int) Math.floor(range / 2));

				for(int i = 0; i < blocks; i++) {
					int x = shift + i % range;
					int z = shift + i / range;

					InteractionResult res = placeSeed(context, target, context.getClickedPos().offset(x, 0, z), total);
					contents = getContents(stack);
					if(contents == null)
						break;
					total = contents.getRight();

					if(!bestRes.consumesAction())
						bestRes = res;
				}

				return bestRes;
			}
		}

		return super.useOn(context);
	}

	private InteractionResult placeSeed(UseOnContext context, ItemStack target, BlockPos pos, int total) {
		InteractionResult res = target.getItem().useOn(new PouchItemUseContext(context, target, pos));
		int diff = res == InteractionResult.CONSUME ? 1 : 0;
		if(diff > 0 && !context.getPlayer().isCreative())
			setCount(context.getItemInHand(), total - diff);

		return res;
	}

	@Override
	public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
		super.fillItemCategory(group, items);

		if(SeedPouchModule.showAllVariantsInCreative && isEnabled() && allowdedIn(group)) {
			List<Item> tagItems = null;

			try {
				tagItems = SeedPouchModule.seedPouchHoldableTag.getValues();
			} catch(IllegalStateException e) { // Tag not bound yet
				return;
			}

			for(Item i : tagItems) {
				if(!ModuleLoader.INSTANCE.isItemEnabled(i))
					continue;

				ItemStack stack = new ItemStack(this);
				setItemStack(stack, new ItemStack(i));
				setCount(stack, SeedPouchModule.maxItems);
				items.add(stack);
			}
		}
	}

	@Override
	public ItemStack getUsageTickerItem(ItemStack stack) {
		Pair<ItemStack, Integer> contents = getContents(stack);
		if(contents != null)
			return contents.getLeft();

		return stack;
	}

	@Override
	public int getUsageTickerCountForItem(ItemStack stack, Predicate<ItemStack> target) {
		Pair<ItemStack, Integer> contents = getContents(stack);
		if(contents != null && target.test(contents.getLeft()))
			return contents.getRight();

		return 0;
	}

	class PouchItemUseContext extends UseOnContext {

		protected PouchItemUseContext(UseOnContext parent, ItemStack stack, BlockPos targetPos) {
			super(parent.getLevel(), parent.getPlayer(), parent.getHand(), stack, 
					new BlockHitResult(parent.getClickLocation(), parent.getClickedFace(), targetPos, parent.isInside()));
		}

	}

}