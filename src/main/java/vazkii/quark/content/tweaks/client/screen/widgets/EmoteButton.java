package vazkii.quark.content.tweaks.client.screen.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import vazkii.quark.base.handler.MiscUtil;
import vazkii.quark.content.tweaks.client.emote.EmoteDescriptor;
import vazkii.quark.content.tweaks.module.EmotesModule;

public class EmoteButton extends TranslucentButton {

	public final EmoteDescriptor desc;

	public EmoteButton(int x, int y, EmoteDescriptor desc, OnPress onPress) {
		super(x, y, EmotesModule.EMOTE_BUTTON_WIDTH - 1, EmotesModule.EMOTE_BUTTON_WIDTH - 1, new TextComponent(""), onPress);
		this.desc = desc;
	}

	@Override
	public void renderButton(PoseStack matrix, int mouseX, int mouseY, float partial) {
		super.renderButton(matrix, mouseX, mouseY, partial);

		if(visible) {
			Minecraft mc = Minecraft.getInstance();
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.setShaderTexture(0, desc.texture);
			blit(matrix, x + 4, y + 4, 0, 0, 16, 16, 16, 16);

			ResourceLocation tierTexture = desc.getTierTexture();
			if(tierTexture != null) {
				RenderSystem.setShaderTexture(0, tierTexture);
				blit(matrix, x + 4, y + 4, 0, 0, 16, 16, 16, 16);
			}
			
			boolean hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
			if(hovered) {
				String name = desc.getLocalizedName();
				
				RenderSystem.setShaderTexture(0, MiscUtil.GENERAL_ICONS);
				int w = mc.font.width(name);
				int left = x - w;
				int top = y - 8;
				
				RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
				blit(matrix, left, top, 242, 9, 5, 17, 256, 256);
				for(int i = 0; i < w; i++)
					blit(matrix, left + i + 5, top, 248, 9, 1, 17, 256, 256);
				blit(matrix, left + w + 5, top, 250, 9, 6, 17, 256, 256);

				mc.font.draw(matrix, name, left + 5, top + 3, 0);
			}
		}
	}
	
}
