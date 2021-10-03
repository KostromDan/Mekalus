package net.coderbot.iris.pipeline;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;

public class HandRendering {
	public static final HandRendering INSTANCE = new HandRendering();

	private final Minecraft minecraft = Minecraft.getInstance();

	private GameRenderer gameRenderer;
	private RenderBuffers renderBuffers;
	private PoseStack poseStack;
	private float tickDelta;
	private Camera camera;

	private boolean rendering;

	private boolean canRender;

	public void prepareForRendering(RenderBuffers renderBuffers, PoseStack poseStack, float tickDelta, Camera camera, GameRenderer gameRenderer) {
		this.canRender = !(camera.isDetached() || !(camera.getEntity() instanceof Player) || minecraft.options.hideGui || (camera.getEntity() instanceof LivingEntity && ((LivingEntity)camera.getEntity()).isSleeping()) || minecraft.gameMode.getPlayerMode() == GameType.SPECTATOR);

		this.gameRenderer = gameRenderer;
		this.renderBuffers = renderBuffers;
		this.poseStack = poseStack;
		this.tickDelta = tickDelta;
		this.camera = camera;
	}

	public void render() {
		if(!canRender) return;

		rendering = true;

		RenderSystem.clear(256, Minecraft.ON_OSX);

		poseStack.pushPose();

        final PoseStack.Pose pose = poseStack.last();

		gameRenderer.resetProjectionMatrix(gameRenderer.getProjectionMatrix(camera, tickDelta, false));

        pose.pose().setIdentity();
        pose.normal().setIdentity();

		if(minecraft.options.bobView) {
			final float g = minecraft.player.walkDist - minecraft.player.walkDistO;
			final float h = -(minecraft.player.walkDist + g * tickDelta);
			final float i = Mth.lerp(tickDelta, minecraft.player.oBob, minecraft.player.bob);

			poseStack.translate((double)(Mth.sin(h * 3.1415927f) * i * 0.5f), (double)(-Math.abs(Mth.cos(h * 3.1415927f) * i)), 0.0);

			poseStack.mulPose(Vector3f.ZP.rotationDegrees(Mth.sin(h * 3.1415927f) * i * 3.0f));
			poseStack.mulPose(Vector3f.XP.rotationDegrees(Math.abs(Mth.cos(h * 3.1415927f - 0.2f) * i) * 5.0f));
		}

		minecraft.getItemInHandRenderer().renderHandsWithItems(tickDelta, poseStack, renderBuffers.bufferSource(), minecraft.player, minecraft.getEntityRenderDispatcher().getPackedLightCoords(camera.getEntity(), tickDelta));


		poseStack.popPose();

		rendering = false;
	}

	public boolean isRendering() {
		return rendering;
	}
}
