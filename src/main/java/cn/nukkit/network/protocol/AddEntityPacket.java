package cn.nukkit.network.protocol;

import cn.nukkit.entity.Attribute;
import cn.nukkit.entity.custom.EntityDefinition;
import cn.nukkit.entity.custom.EntityManager;
import cn.nukkit.entity.data.EntityMetadata;
import cn.nukkit.entity.item.*;
import cn.nukkit.entity.mob.*;
import cn.nukkit.entity.passive.*;
import cn.nukkit.entity.projectile.*;
import cn.nukkit.entity.weather.EntityLightning;
import cn.nukkit.network.protocol.types.EntityLink;
import cn.nukkit.utils.Binary;
import com.google.common.collect.ImmutableMap;
import lombok.ToString;

/**
 * @author MagicDroidX
 * Nukkit Project
 */
@ToString
public class AddEntityPacket extends DataPacket {

    public static final byte NETWORK_ID = ProtocolInfo.ADD_ENTITY_PACKET;

    public static final ImmutableMap<Integer, String> LEGACY_IDS = ImmutableMap.<Integer, String>builder()
            .put(51, "minecraft:npc")
            .put(63, "minecraft:player")
            .put(EntityWitherSkeleton.NETWORK_ID, "minecraft:wither_skeleton")
            .put(EntityHusk.NETWORK_ID, "minecraft:husk")
            .put(EntityStray.NETWORK_ID, "minecraft:stray")
            .put(EntityWitch.NETWORK_ID, "minecraft:witch")
            .put(EntityZombieVillagerV1.NETWORK_ID, "minecraft:zombie_villager")
            .put(EntityBlaze.NETWORK_ID, "minecraft:blaze")
            .put(EntityMagmaCube.NETWORK_ID, "minecraft:magma_cube")
            .put(EntityGhast.NETWORK_ID, "minecraft:ghast")
            .put(EntityCaveSpider.NETWORK_ID, "minecraft:cave_spider")
            .put(EntitySilverfish.NETWORK_ID, "minecraft:silverfish")
            .put(EntityEnderman.NETWORK_ID, "minecraft:enderman")
            .put(EntitySlime.NETWORK_ID, "minecraft:slime")
            .put(EntityZombiePigman.NETWORK_ID, "minecraft:zombie_pigman")
            .put(EntitySpider.NETWORK_ID, "minecraft:spider")
            .put(EntitySkeleton.NETWORK_ID, "minecraft:skeleton")
            .put(EntityCreeper.NETWORK_ID, "minecraft:creeper")
            .put(EntityZombie.NETWORK_ID, "minecraft:zombie")
            .put(EntitySkeletonHorse.NETWORK_ID, "minecraft:skeleton_horse")
            .put(EntityMule.NETWORK_ID, "minecraft:mule")
            .put(EntityDonkey.NETWORK_ID, "minecraft:donkey")
            .put(EntityDolphin.NETWORK_ID, "minecraft:dolphin")
            .put(EntityTropicalFish.NETWORK_ID, "minecraft:tropicalfish")
            .put(EntityWolf.NETWORK_ID, "minecraft:wolf")
            .put(EntitySquid.NETWORK_ID, "minecraft:squid")
            .put(EntityDrowned.NETWORK_ID, "minecraft:drowned")
            .put(EntitySheep.NETWORK_ID, "minecraft:sheep")
            .put(EntityMooshroom.NETWORK_ID, "minecraft:mooshroom")
            .put(EntityPanda.NETWORK_ID, "minecraft:panda")
            .put(EntitySalmon.NETWORK_ID, "minecraft:salmon")
            .put(EntityPig.NETWORK_ID, "minecraft:pig")
            .put(EntityVillagerV1.NETWORK_ID, "minecraft:villager")
            .put(EntityCod.NETWORK_ID, "minecraft:cod")
            .put(EntityPufferfish.NETWORK_ID, "minecraft:pufferfish")
            .put(EntityCow.NETWORK_ID, "minecraft:cow")
            .put(EntityChicken.NETWORK_ID, "minecraft:chicken")
            .put(107, "minecraft:balloon")
            .put(EntityLlama.NETWORK_ID, "minecraft:llama")
            .put(EntityIronGolem.NETWORK_ID, "minecraft:iron_golem")
            .put(EntityRabbit.NETWORK_ID, "minecraft:rabbit")
            .put(EntitySnowGolem.NETWORK_ID, "minecraft:snow_golem")
            .put(EntityBat.NETWORK_ID, "minecraft:bat")
            .put(EntityOcelot.NETWORK_ID, "minecraft:ocelot")
            .put(EntityHorse.NETWORK_ID, "minecraft:horse")
            .put(EntityCat.NETWORK_ID, "minecraft:cat")
            .put(EntityPolarBear.NETWORK_ID, "minecraft:polar_bear")
            .put(EntityZombieHorse.NETWORK_ID, "minecraft:zombie_horse")
            .put(EntityTurtle.NETWORK_ID, "minecraft:turtle")
            .put(EntityParrot.NETWORK_ID, "minecraft:parrot")
            .put(EntityGuardian.NETWORK_ID, "minecraft:guardian")
            .put(EntityElderGuardian.NETWORK_ID, "minecraft:elder_guardian")
            .put(EntityVindicator.NETWORK_ID, "minecraft:vindicator")
            .put(EntityWither.NETWORK_ID, "minecraft:wither")
            .put(EntityEnderDragon.NETWORK_ID, "minecraft:ender_dragon")
            .put(EntityShulker.NETWORK_ID, "minecraft:shulker")
            .put(EntityEndermite.NETWORK_ID, "minecraft:endermite")
            .put(EntityMinecartEmpty.NETWORK_ID, "minecraft:minecart")
            .put(EntityMinecartHopper.NETWORK_ID, "minecraft:hopper_minecart")
            .put(EntityMinecartTNT.NETWORK_ID, "minecraft:tnt_minecart")
            .put(EntityMinecartChest.NETWORK_ID, "minecraft:chest_minecart")
            .put(100, "minecraft:command_block_minecart")
            .put(EntityArmorStand.NETWORK_ID, "minecraft:armor_stand")
            .put(EntityItem.NETWORK_ID, "minecraft:item")
            .put(EntityPrimedTNT.NETWORK_ID, "minecraft:tnt")
            .put(EntityFallingBlock.NETWORK_ID, "minecraft:falling_block")
            .put(EntityExpBottle.NETWORK_ID, "minecraft:xp_bottle")
            .put(EntityXPOrb.NETWORK_ID, "minecraft:xp_orb")
            .put(70, "minecraft:eye_of_ender_signal")
            .put(EntityEndCrystal.NETWORK_ID, "minecraft:ender_crystal")
            .put(76, "minecraft:shulker_bullet")
            .put(EntityFishingHook.NETWORK_ID, "minecraft:fishing_hook")
            .put(79, "minecraft:dragon_fireball")
            .put(EntityArrow.NETWORK_ID, "minecraft:arrow")
            .put(EntitySnowball.NETWORK_ID, "minecraft:snowball")
            .put(EntityEgg.NETWORK_ID, "minecraft:egg")
            .put(EntityPainting.NETWORK_ID, "minecraft:painting")
            .put(EntityThrownTrident.NETWORK_ID, "minecraft:thrown_trident")
            .put(85, "minecraft:fireball")
            .put(EntityPotion.NETWORK_ID, "minecraft:splash_potion")
            .put(EntityEnderPearl.NETWORK_ID, "minecraft:ender_pearl")
            .put(88, "minecraft:leash_knot")
            .put(89, "minecraft:wither_skull")
            .put(91, "minecraft:wither_skull_dangerous")
            .put(EntityBoat.NETWORK_ID, "minecraft:boat")
            .put(EntityLightning.NETWORK_ID, "minecraft:lightning_bolt")
            .put(94, "minecraft:small_fireball")
            .put(102, "minecraft:llama_spit")
            .put(EntityAreaEffectCloud.NETWORK_ID, "minecraft:area_effect_cloud")
            .put(EntityPotionLingering.NETWORK_ID, "minecraft:lingering_potion")
            .put(EntityFirework.NETWORK_ID, "minecraft:fireworks_rocket")
            .put(103, "minecraft:evocation_fang")
            .put(104, "minecraft:evocation_illager")
            .put(EntityVex.NETWORK_ID, "minecraft:vex")
            .put(56, "minecraft:agent")
            .put(106, "minecraft:ice_bomb")
            .put(EntityPhantom.NETWORK_ID, "minecraft:phantom")
            .put(62, "minecraft:tripod_camera")
            .put(EntityPillager.NETWORK_ID, "minecraft:pillager")
            .put(EntityWanderingTrader.NETWORK_ID, "minecraft:wandering_trader")
            .put(EntityRavager.NETWORK_ID, "minecraft:ravager")
            .put(EntityVillager.NETWORK_ID, "minecraft:villager_v2")
            .put(EntityZombieVillager.NETWORK_ID, "minecraft:zombie_villager_v2")
            .put(EntityFox.NETWORK_ID, "minecraft:fox")
            .put(EntityBee.NETWORK_ID, "minecraft:bee")
            .put(EntityPiglin.NETWORK_ID, "minecraft:piglin")
            .put(EntityHoglin.NETWORK_ID, "minecraft:hoglin")
            .put(EntityStrider.NETWORK_ID, "minecraft:strider")
            .put(EntityZoglin.NETWORK_ID, "minecraft:zoglin")
            .put(EntityPiglinBrute.NETWORK_ID, "minecraft:piglin_brute")
            .put(EntityGoat.NETWORK_ID, "minecraft:goat")
            .put(EntityGlowSquid.NETWORK_ID, "minecraft:glow_squid")
            .put(EntityAxolotl.NETWORK_ID, "minecraft:axolotl")
            .put(EntityWarden.NETWORK_ID, "minecraft:warden")
            .put(EntityFrog.NETWORK_ID, "minecraft:frog")
            .put(EntityTadpole.NETWORK_ID, "minecraft:tadpole")
            .put(EntityAllay.NETWORK_ID, "minecraft:allay")
            .put(EntityChestBoat.NETWORK_ID, "minecraft:chest_boat")
            .put(EntityCamel.NETWORK_ID, "minecraft:camel")
            .put(EntitySniffer.NETWORK_ID, "minecraft:sniffer")
            .put(EntityBreeze.NETWORK_ID, "minecraft:breeze")
            .put(141, "minecraft:breeze_wind_charge_projectile")
            .put(EntityArmadillo.NETWORK_ID, "minecraft:armadillo")
            .put(143, "minecraft:wind_charge_projectile")
            .put(EntityBogged.NETWORK_ID, "minecraft:bogged")
            .put(145, "minecraft:ominous_item_spawner")
            .put(EntityCreaking.NETWORK_ID, "minecraft:creaking")
            .build();

    @Override
    public byte pid() {
        return NETWORK_ID;
    }

    public long entityUniqueId;
    public long entityRuntimeId;
    public int type;
    public String id;
    public float x;
    public float y;
    public float z;
    public float speedX = 0f;
    public float speedY = 0f;
    public float speedZ = 0f;
    public float yaw;
    public float pitch;
    public float headYaw;
    public float bodyYaw = -1;
    public EntityMetadata metadata = new EntityMetadata();
    public Attribute[] attributes = new Attribute[0];
    public EntityLink[] links = new EntityLink[0];

    @Override
    public void decode() {
        this.decodeUnsupported();
    }

    @Override
    public void encode() {
        this.reset();
        this.putEntityUniqueId(this.entityUniqueId);
        this.putEntityRuntimeId(this.entityRuntimeId);
        this.putString(this.getEntityIdentifier());
        this.putVector3f(this.x, this.y, this.z);
        this.putVector3f(this.speedX, this.speedY, this.speedZ);
        this.putLFloat(this.pitch);
        this.putLFloat(this.yaw);
        this.putLFloat(this.headYaw);
        this.putLFloat(this.bodyYaw == -1 ? this.yaw : this.bodyYaw); // For backwards compatibility
        this.putAttributeList(this.attributes);
        this.put(Binary.writeMetadata(this.metadata));
        this.putUnsignedVarInt(0); // Entity properties int
        this.putUnsignedVarInt(0); // Entity properties float
        this.putUnsignedVarInt(this.links.length);
        for (EntityLink link : links) {
            putEntityLink(link);
        }
    }

    private String getEntityIdentifier() {
        if (this.id != null) {
            return this.id;
        }

        String identifier = LEGACY_IDS.get(type);

        if (identifier == null) {
            // Maybe a custom entity
            EntityDefinition definition = EntityManager.get().getDefinition(this.type);
            if (definition != null) {
                identifier = definition.getIdentifier();
            }
        }

        if (identifier == null) {
            throw new IllegalStateException("Unknown entity with network id " + this.type);
        }
        return identifier;
    }
}
