package org.moon.figura.lua.api;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.luaj.vm2.LuaError;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFieldDoc;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaMethodOverload;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.math.vector.FiguraVec2;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.utils.FiguraIdentifier;
import org.moon.figura.utils.LuaUtils;

import java.util.UUID;

@LuaWhitelist
@LuaTypeDoc(
        name = "RendererAPI",
        value = "renderer"
)
public class RendererAPI {

    private final UUID owner;

    public Float shadowRadius, fov;

    @LuaWhitelist
    @LuaFieldDoc("renderer.render_fire")
    public boolean renderFire = true;
    @LuaWhitelist
    @LuaFieldDoc("renderer.render_vehicle")
    public boolean renderVehicle = true;
    @LuaWhitelist
    @LuaFieldDoc("renderer.render_crosshair")
    public boolean renderCrosshair = true;
    @LuaWhitelist
    @LuaFieldDoc("renderer.force_paperdoll")
    public boolean forcePaperdoll;
    @LuaWhitelist
    @LuaFieldDoc("renderer.render_hud")
    public boolean renderHUD = true;

    public FiguraVec3 cameraPos;
    public FiguraVec3 cameraPivot;
    public FiguraVec3 cameraOffsetPivot;
    public FiguraVec3 cameraRot;
    public FiguraVec3 cameraOffsetRot;
    public ResourceLocation postShader;
    public FiguraVec2 crosshairOffset;
    public FiguraVec3 outlineColor;
    public ResourceLocation fireLayer1, fireLayer2;
    public Boolean renderLeftArm, renderRightArm;

    public RendererAPI(Avatar owner) {
        this.owner = owner.owner;
    }

    private static boolean checkCameraOwner(UUID entity) {
        Entity e = Minecraft.getInstance().getCameraEntity();
        return e != null && e.getUUID().equals(entity);
    }

    @LuaWhitelist
    @LuaMethodDoc("renderer.should_render_fire")
    public boolean shouldRenderFire() {
        return renderFire;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Boolean.class,
                    argumentNames = "renderFire"
            ),
            value = "renderer.set_render_fire")
    public RendererAPI setRenderFire(boolean renderFire) {
        this.renderFire = renderFire;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc("renderer.should_render_vehicle")
    public boolean shouldRenderVehicle() {
        return renderVehicle;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Boolean.class,
                    argumentNames = "renderVehicle"
            ),
            value = "renderer.set_render_vehicle")
    public RendererAPI setRenderVehicle(boolean renderVehicle) {
        this.renderVehicle = renderVehicle;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc("renderer.should_render_crosshair")
    public boolean shouldRenderCrosshair() {
        return renderCrosshair;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Boolean.class,
                    argumentNames = "renderCrosshair"
            ),
            value = "renderer.set_render_crosshair")
    public RendererAPI setRenderCrosshair(boolean renderCrosshair) {
        this.renderCrosshair = renderCrosshair;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc("renderer.should_force_paperdoll")
    public boolean shouldForcePaperdoll() {
        return forcePaperdoll;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Boolean.class,
                    argumentNames = "forcePaperdoll"
            ),
            value = "renderer.set_force_paperdoll")
    public RendererAPI setForcePaperdoll(boolean forcePaperdoll) {
        this.forcePaperdoll = forcePaperdoll;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc("renderer.should_render_hud")
    public boolean shouldRenderHUD() {
        return renderHUD;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Boolean.class,
                    argumentNames = "renderHUD"
            ),
            value = "renderer.set_render_hud")
    public RendererAPI setRenderHUD(boolean renderHUD) {
        this.renderHUD = renderHUD;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload,
                    @LuaMethodOverload(
                            argumentTypes = Float.class,
                            argumentNames = "radius"
                    )
            },
            aliases = "shadowRadius",
            value = "renderer.set_shadow_radius"
    )
    public RendererAPI setShadowRadius(Float shadowRadius) {
        this.shadowRadius = shadowRadius == null ? null : Mth.clamp(shadowRadius, 0f, 12f);
        return this;
    }

    @LuaWhitelist
    public RendererAPI shadowRadius(Float shadowRadius) {
        return setShadowRadius(shadowRadius);
    }

    @LuaWhitelist
    @LuaMethodDoc("renderer.get_shadow_radius")
    public Float getShadowRadius() {
        return this.shadowRadius;
    }

    @LuaWhitelist
    @LuaMethodDoc("renderer.is_first_person")
    public boolean isFirstPerson() {
        return checkCameraOwner(this.owner) && Minecraft.getInstance().options.getCameraType().isFirstPerson();
    }

    @LuaWhitelist
    @LuaMethodDoc("renderer.is_camera_backwards")
    public boolean isCameraBackwards() {
        return checkCameraOwner(this.owner) && Minecraft.getInstance().options.getCameraType().isMirrored();
    }

    @LuaWhitelist
    @LuaMethodDoc("renderer.get_camera_pos")
    public FiguraVec3 getCameraPos() {
        return this.cameraPos;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "pos"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            aliases = "cameraPos",
            value = "renderer.set_camera_pos"
    )
    public RendererAPI setCameraPos(Object x, Double y, Double z) {
        this.cameraPos = x == null ? null : LuaUtils.parseVec3("setCameraPos", x, y, z);
        return this;
    }

    @LuaWhitelist
    public RendererAPI cameraPos(Object x, Double y, Double z) {
        return setCameraPos(x, y, z);
    }

    @LuaWhitelist
    @LuaMethodDoc("renderer.get_camera_pivot")
    public FiguraVec3 getCameraPivot() {
        return this.cameraPivot;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "pivot"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            aliases = "cameraPivot",
            value = "renderer.set_camera_pivot"
    )
    public RendererAPI setCameraPivot(Object x, Double y, Double z) {
        this.cameraPivot = x == null ? null : LuaUtils.parseVec3("setCameraPivot", x, y, z);
        return this;
    }

    @LuaWhitelist
    public RendererAPI cameraPivot(Object x, Double y, Double z) {
        return setCameraPivot(x, y, z);
    }

    @LuaWhitelist
    @LuaMethodDoc("renderer.get_camera_offset_pivot")
    public FiguraVec3 getCameraOffsetPivot() {
        return this.cameraOffsetPivot;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "pivot"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            aliases = "offsetCameraPivot",
            value = "renderer.set_offset_camera_pivot"
    )
    public RendererAPI setOffsetCameraPivot(Object x, Double y, Double z) {
        this.cameraOffsetPivot = x == null ? null : LuaUtils.parseVec3("setOffsetCameraPivot", x, y, z);
        return this;
    }

    @LuaWhitelist
    public RendererAPI offsetCameraPivot(Object x, Double y, Double z) {
        return setOffsetCameraPivot(x, y, z);
    }

    @LuaWhitelist
    @LuaMethodDoc("renderer.get_camera_rot")
    public FiguraVec3 getCameraRot() {
        return this.cameraRot;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "rot"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            aliases = "cameraRot",
            value = "renderer.set_camera_rot"
    )
    public RendererAPI setCameraRot(Object x, Double y, Double z) {
        this.cameraRot = x == null ? null : LuaUtils.parseVec3("setCameraRot", x, y, z);
        return this;
    }

    @LuaWhitelist
    public RendererAPI cameraRot(Object x, Double y, Double z) {
        return setCameraRot(x, y, z);
    }

    @LuaWhitelist
    @LuaMethodDoc("renderer.get_camera_offset_rot")
    public FiguraVec3 getCameraOffsetRot() {
        return this.cameraOffsetRot;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "rot"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            aliases = "offsetCameraRot",
            value = "renderer.set_offset_camera_rot"
    )
    public RendererAPI setOffsetCameraRot(Object x, Double y, Double z) {
        this.cameraOffsetRot = x == null ? null : LuaUtils.parseVec3("setOffsetCameraRot", x, y, z);
        return this;
    }

    @LuaWhitelist
    public RendererAPI offsetCameraRot(Object x, Double y, Double z) {
        return setOffsetCameraRot(x, y, z);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "effect"
            ),
            aliases = "postEffect",
            value = "renderer.set_post_effect"
    )
    public RendererAPI setPostEffect(String effect) {
        this.postShader = effect == null ? null : new ResourceLocation("shaders/post/" + FiguraIdentifier.formatPath(effect) + ".json");
        return this;
    }

    @LuaWhitelist
    public RendererAPI postEffect(String effect) {
        return setPostEffect(effect);
    }

    @LuaWhitelist
    @LuaMethodDoc("renderer.get_fov")
    public Float getFOV() {
        return this.fov;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload,
                    @LuaMethodOverload(
                            argumentTypes = Float.class,
                            argumentNames = "fov"
                    )
            },
            aliases = "fov",
            value = "renderer.set_fov"
    )
    public RendererAPI setFOV(Float fov) {
        this.fov = fov;
        return this;
    }

    @LuaWhitelist
    public RendererAPI fov(Float fov) {
        return setFOV(fov);
    }

    @LuaWhitelist
    @LuaMethodDoc("renderer.get_crosshair_offset")
    public FiguraVec2 getCrosshairOffset() {
        return this.crosshairOffset;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec2.class,
                            argumentNames = "vec"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Integer.class, Integer.class},
                            argumentNames = {"x", "y"}
                    )
            },
            aliases = "crosshairOffset",
            value = "renderer.set_crosshair_offset")
    public RendererAPI setCrosshairOffset(Object x, Double y) {
        this.crosshairOffset = x == null ? null : LuaUtils.parseVec2("setCrosshairOffset", x, y);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc("renderer.get_outline_color")
    public FiguraVec3 getOutlineColor() {
        return outlineColor;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "rgb"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"r", "g", "b"}
                    )
            },
            aliases = "outlineColor",
            value = "renderer.set_outline_color"
    )
    public RendererAPI setOutlineColor(Object r, Double g, Double b) {
        outlineColor = r == null ? null : LuaUtils.parseVec3("setOutlineColor", r, g, b);
        return this;
    }

    @LuaWhitelist
    public RendererAPI outlineColor(Object r, Double g, Double b) {
        return setOutlineColor(r, g, b);
    }

    @LuaWhitelist
    public RendererAPI crosshairOffset(Object x, Double y) {
        return setCrosshairOffset(x, y);
    }

    @LuaWhitelist
    @LuaMethodDoc("renderer.get_primary_fire_texture")
    public String getPrimaryFireTexture() {
        return fireLayer1.toString();
    }

    @LuaWhitelist
    @LuaMethodDoc("renderer.get_secondary_fire_texture")
    public String getSecondaryFireTexture() {
        return fireLayer2.toString();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "id"
            ),
            aliases = "primaryFireTexture",
            value = "renderer.set_primary_fire_texture"
    )
    public RendererAPI setPrimaryFireTexture(String id) {
        if (id == null) {
            fireLayer1 = null;
            return this;
        }

        try {
            fireLayer1 = new ResourceLocation(id);
            if (fireLayer1.getPath().startsWith("textures/"))
                fireLayer1 = new ResourceLocation(fireLayer1.getNamespace(), fireLayer1.getPath().substring("textures/".length()));
            return this;
        } catch (Exception e) {
            throw new LuaError(e.getMessage());
        }
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "id"
            ),
            aliases = "secondaryFireTexture",
            value = "renderer.set_secondary_fire_texture"
    )
    public RendererAPI setSecondaryFireTexture(String id) {
        if (id == null) {
            fireLayer2 = null;
            return this;
        }

        try {
            fireLayer2 = new ResourceLocation(id);
            if (fireLayer2.getPath().startsWith("textures/"))
                fireLayer2 = new ResourceLocation(fireLayer2.getNamespace(), fireLayer2.getPath().substring("textures/".length()));
            return this;
        } catch (Exception e) {
            throw new LuaError(e.getMessage());
        }
    }

    @LuaWhitelist
    public RendererAPI primaryFireTexture(String id) {
        return setPrimaryFireTexture(id);
    }

    @LuaWhitelist
    public RendererAPI secondaryFireTexture(String id) {
        return setSecondaryFireTexture(id);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Boolean.class,
                    argumentNames = "bool"
            ),
            aliases = "renderLeftArm",
            value = "renderer.set_render_left_arm"
    )
    public RendererAPI setRenderLeftArm(Boolean bool) {
        this.renderLeftArm = bool;
        return this;
    }

    @LuaWhitelist
    public RendererAPI renderLeftArm(Boolean bool) {
        return setRenderLeftArm(bool);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Boolean.class,
                    argumentNames = "bool"
            ),
            aliases = "renderRightArm",
            value = "renderer.set_render_right_arm"
    )
    public RendererAPI setRenderRightArm(Boolean bool) {
        this.renderRightArm = bool;
        return this;
    }

    @LuaWhitelist
    public RendererAPI renderRightArm(Boolean bool) {
        return setRenderRightArm(bool);
    }

    @LuaWhitelist
    public Object __index(String arg) {
        if (arg == null) return null;
        return switch (arg) {
            case "renderFire" -> renderFire;
            case "renderVehicle" -> renderVehicle;
            case "renderCrosshair" -> renderCrosshair;
            case "forcePaperdoll" -> forcePaperdoll;
            case "renderHUD" -> renderHUD;
            default -> null;
        };
    }

    @LuaWhitelist
    public void __newindex(@LuaNotNil String key, boolean value) {
        switch (key) {
            case "renderFire" -> renderFire = value;
            case "renderVehicle" -> renderVehicle = value;
            case "renderCrosshair" -> renderCrosshair = value;
            case "forcePaperdoll" -> forcePaperdoll = value;
            case "renderHUD" -> renderHUD = value;
            default -> throw new LuaError("Cannot assign value on key \"" + key + "\"");
        }
    }

    @Override
    public String toString() {
        return "RendererAPI";
    }
}
