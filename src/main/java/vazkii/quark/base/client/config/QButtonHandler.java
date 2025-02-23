package vazkii.quark.base.client.config;

import java.util.List;

import com.google.common.collect.ImmutableSet;

import net.minecraft.Util;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.loading.FMLPaths;
import vazkii.quark.base.Quark;
import vazkii.quark.base.client.config.screen.widgets.QButton;
import vazkii.quark.base.handler.GeneralConfig;

@EventBusSubscriber(modid = Quark.MOD_ID, value = Dist.CLIENT)
public class QButtonHandler {

	@SubscribeEvent
	public static void onGuiInit(ScreenEvent.InitScreenEvent event) {
		Screen gui = event.getScreen();

		if(GeneralConfig.enableQButton && (gui instanceof TitleScreen || gui instanceof PauseScreen)) {
			ImmutableSet<String> targets = GeneralConfig.qButtonOnRight 
					? ImmutableSet.of(I18n.get("fml.menu.modoptions"), I18n.get("menu.online"))
							: ImmutableSet.of(I18n.get("menu.options"), I18n.get("fml.menu.mods"));

			List<GuiEventListener> listeners = event.getListenersList();
			for(GuiEventListener b : listeners)
				if(b instanceof AbstractWidget) {
					AbstractWidget abs = (AbstractWidget) b;
					if(targets.contains(abs.getMessage().getString())) {
						Button qButton = new QButton(abs.x + (GeneralConfig.qButtonOnRight ? 103 : -24), abs.y);
						event.addListener(qButton);
						return;
					}
				}
		}
	}

	public static void openFile() {
		Util.getPlatform().openFile(FMLPaths.CONFIGDIR.get().toFile());
	}

}
