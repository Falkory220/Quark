package vazkii.quark.base.module.config;

import javax.annotation.Nonnull;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

/**
 * @author WireSegal
 * Created at 1:23 PM on 8/24/19.
 */
public class FlagLootCondition implements LootItemCondition {

    private final ConfigFlagManager manager;
    private final String flag;

    public FlagLootCondition(ConfigFlagManager manager, String flag) {
        this.manager = manager;
        this.flag = flag;
    }

    @Override
    public boolean test(LootContext lootContext) {
        return manager.getFlag(flag);
    }
    

	@Nonnull
    @Override
	public LootItemConditionType getType() {
		return ConfigFlagManager.flagConditionType;
	}

    
    public static class FlagSerializer implements Serializer<FlagLootCondition> {
    	
        private final ConfigFlagManager manager;

        public FlagSerializer(ConfigFlagManager manager) {
            this.manager = manager;
        }

        @Override
        public void serialize(@Nonnull JsonObject json, @Nonnull FlagLootCondition value, @Nonnull JsonSerializationContext context) {
            json.addProperty("flag", value.flag);
        }

        @Nonnull
        @Override
        public FlagLootCondition deserialize(@Nonnull JsonObject json, @Nonnull JsonDeserializationContext context) {
        	String flag = json.getAsJsonPrimitive("flag").getAsString();
            return new FlagLootCondition(manager, flag);
        }
    }

}
