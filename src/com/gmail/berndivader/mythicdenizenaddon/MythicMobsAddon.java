package com.gmail.berndivader.mythicdenizenaddon;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

import com.gmail.berndivader.mythicdenizenaddon.cmds.ActiveMobSkillCast;
import com.gmail.berndivader.mythicdenizenaddon.cmds.CreateMythicSpawner;
import com.gmail.berndivader.mythicdenizenaddon.cmds.GetMythicItems;
import com.gmail.berndivader.mythicdenizenaddon.cmds.GetMythicSkills;
import com.gmail.berndivader.mythicdenizenaddon.cmds.MythicMobSkillCast;
import com.gmail.berndivader.mythicdenizenaddon.cmds.MythicMobsSpawn;
import com.gmail.berndivader.mythicdenizenaddon.cmds.PlayerSkillCast;
import com.gmail.berndivader.mythicdenizenaddon.cmds.SendSignal;
import com.gmail.berndivader.mythicdenizenaddon.cmds.TransformMythicMob;
import com.gmail.berndivader.mythicdenizenaddon.cmds.TransformToMythicMob;
import com.gmail.berndivader.mythicdenizenaddon.cmds.TriggerSkill;
import com.gmail.berndivader.mythicdenizenaddon.events.DenizenConditionEvent;
import com.gmail.berndivader.mythicdenizenaddon.events.DenizenEntityTargeterEvent;
import com.gmail.berndivader.mythicdenizenaddon.events.DenizenLocationTargeterEvent;
import com.gmail.berndivader.mythicdenizenaddon.events.DenizenMythicMobDeathEvent;
import com.gmail.berndivader.mythicdenizenaddon.events.DenizenMythicMobSpawnEvent;
import com.gmail.berndivader.mythicdenizenaddon.events.DenizenSkillEvent;
import com.gmail.berndivader.mythicdenizenaddon.events.DenizenTargetConditionEvent;
import com.gmail.berndivader.mythicdenizenaddon.events.MythicMobsDropEvent;
import com.gmail.berndivader.mythicdenizenaddon.obj.ActivePlayer;
import com.gmail.berndivader.mythicdenizenaddon.obj.dActiveMob;
import com.gmail.berndivader.mythicdenizenaddon.obj.dEntityExt;
import com.gmail.berndivader.mythicdenizenaddon.obj.dListExt;
import com.gmail.berndivader.mythicdenizenaddon.obj.dLocationExt;
import com.gmail.berndivader.mythicdenizenaddon.obj.dMythicItem;
import com.gmail.berndivader.mythicdenizenaddon.obj.dMythicMeta;
import com.gmail.berndivader.mythicdenizenaddon.obj.dMythicMob;
import com.gmail.berndivader.mythicdenizenaddon.obj.dMythicSkill;
import com.gmail.berndivader.mythicdenizenaddon.obj.dMythicSpawner;
import com.gmail.berndivader.mythicdenizenaddon.obj.dWorldExt;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.adapters.AbstractLocation;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import io.lumine.xikage.mythicmobs.skills.AbstractSkill;
import io.lumine.xikage.mythicmobs.skills.SkillCaster;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import io.lumine.xikage.mythicmobs.skills.SkillTargeter;
import io.lumine.xikage.mythicmobs.skills.SkillTrigger;
import io.lumine.xikage.mythicmobs.skills.targeters.ConsoleTargeter;
import io.lumine.xikage.mythicmobs.skills.targeters.IEntitySelector;
import io.lumine.xikage.mythicmobs.skills.targeters.ILocationSelector;
import io.lumine.xikage.mythicmobs.skills.targeters.MTOrigin;
import io.lumine.xikage.mythicmobs.skills.targeters.MTTriggerLocation;
import io.lumine.xikage.mythicmobs.spawning.spawners.MythicSpawner;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dWorld;
import net.aufdemrand.denizencore.objects.dList;

public class MythicMobsAddon extends Support {
	
	public static MythicMobs mythicmobs=MythicMobs.inst();
	
	@SuppressWarnings("unchecked")
	public MythicMobsAddon() {
		
		registerObjects(dMythicSpawner.class, dActiveMob.class, dMythicMob.class, dMythicItem.class, dMythicSkill.class, dMythicMeta.class);
		registerProperty(dEntityExt.class, dEntity.class);
		registerProperty(dWorldExt.class, dWorld.class);
		registerProperty(dLocationExt.class, dLocation.class);
		registerProperty(dListExt.class,dList.class);
		
		registerScriptEvents(new DenizenConditionEvent());
		registerScriptEvents(new DenizenSkillEvent());
		registerScriptEvents(new DenizenMythicMobSpawnEvent());
		registerScriptEvents(new DenizenMythicMobDeathEvent());
		registerScriptEvents(new DenizenTargetConditionEvent());
		registerScriptEvents(new DenizenEntityTargeterEvent());
		registerScriptEvents(new DenizenLocationTargeterEvent());
		registerScriptEvents(new MythicMobsDropEvent());

		new MythicMobsSpawn().activate().as("mmspawnmob").withOptions("- mmspawnmob [mobtype:string] [location] (world:string) (level:integer)", 2);
		new ActiveMobSkillCast().activate().as("mmcastmob").withOptions("- mmcastmob [caster:dActiveMob] [target:dEntity||dLocation] [skill:string] (trigger:dEntity) (power:float)",3);
		new PlayerSkillCast().activate().as("mmcastplayer").withOptions("- mmcastplayer [player:dPlayer] [skill:string] [target:dEntity||dLocation] (trigger:dEntity) (repeat:integer) (delay:integer)", 3);
		new MythicMobSkillCast().activate().as("mmskillcast").withOptions("- mmskillcast [caster:dEntity] [skill:string] [target:dEntity||dLocation] (trigger:dEntity) (repeat:integer) (delay:integer)", 3);
		new SendSignal().activate().as("mmsignal").withOptions("- mmsignal [activemob:dActiveMob] [signal:string] (trigger:dEntity)", 2);
		new TriggerSkill().activate().as("mmtrigger").withOptions("- mmtrigger [activemob:dActiveMob] [trigger:string] [entity:dEntity]", 3);
		new CreateMythicSpawner().activate().as("mmcreatespawner").withOptions("- mmcreatespawner [string:uniquename] [location:dLocation] [string:mobtype]", 3);
		new TransformToMythicMob().activate().as("mmapplymythic").withOptions("- mmapplymythic [entity:dEntity] [mobtype:string] [level:integer]", 2);
		new TransformMythicMob().activate().as("mmremovemythic").withOptions("- mmremovemythic [activemob:dActiveMob]", 1);
		new GetMythicItems().activate().as("getmythicitems").withOptions("- getmythicitems (filter:regex) (strict:boolean)",0);
		new GetMythicSkills().activate().as("getmythicskills").withOptions("- getmythicskills (filter:regex) (strict:boolean>",0);
	}

	public static boolean isActiveMob(UUID uuid) {
		return mythicmobs.getMobManager().isActiveMob(uuid);
	}
	
	public static boolean isMythicMob(String type) {
		return mythicmobs.getMobManager().getMythicMob(type)!=null;
	}

	public static boolean isActiveMob(Entity e) {
		return mythicmobs.getMobManager().isActiveMob(BukkitAdapter.adapt(e));
	}

	public static ActiveMob getActiveMob(Entity e) {
		return mythicmobs.getMobManager().getMythicMobInstance(e);
	}
	
	public static boolean removeSelf(ActiveMob am) {
		if (!am.isUsingDamageSkill()) {
			am.setDead();
			am.getEntity().remove();
		} else {
	    	new BukkitRunnable() {
	            ActiveMob ram = am;
	    		public void run() {
	    			if (ram.isDead() || !ram.isUsingDamageSkill()) {
	    				ram.setDead();
	    				ram.getEntity().remove();
	    				this.cancel();
	    			}
	            }
	        }.runTaskTimer(MythicDenizenPlugin.inst(), 1, 1);
		}
		return true;
	}

	public static boolean isDead(Entity e) {
		ActiveMob am;
		if ((am=mythicmobs.getAPIHelper().getMythicMobInstance(e))!=null) {
			return am.isDead();
		}
		return false;
	}

	public static boolean hasThreatTable(Entity e) {
		ActiveMob am;
		if ((am=mythicmobs.getAPIHelper().getMythicMobInstance(e))!=null) {
			return am.hasThreatTable();
		}
		return false;
	}

	public static boolean hasMythicSpawner(Entity e) {
		ActiveMob am;
		if ((am=mythicmobs.getAPIHelper().getMythicMobInstance(e))!=null) {
			return am.getSpawner()!=null;
		}
		return false;
	}

	public static Entity getOwner(ActiveMob am) {
		if (am.getOwner().isPresent()) {
			UUID uuid = am.getOwner().get();
			return dEntity.getEntityForID(uuid);
		}
		return null;
	}

	public static Entity getLastAggro(ActiveMob am) {
		if (am.getLastAggroCause() != null) {
			return am.getLastAggroCause().getBukkitEntity();
		};
		return null;
	}

	public static Entity getTopTarget(ActiveMob am) {
		if (am.hasThreatTable()) {
			return am.getThreatTable().getTopThreatHolder().getBukkitEntity();
		} else if (am.hasTarget()) {
			return am.getEntity().getTarget().getBukkitEntity();
		}
		return null;
	}

	public static boolean hasTarget(ActiveMob am) {
		return (am.hasThreatTable() || am.hasTarget())?true:false;
	}

	public static void setCustomName(ActiveMob am, String name) {
		am.getEntity().getBukkitEntity().setCustomName(name);
	}

	public static void setTarget(ActiveMob am, Entity target) {
		if (am.hasThreatTable() && (target instanceof LivingEntity)) {
			double h = am.getThreatTable().getTopTargetThreat();
			mythicmobs.getAPIHelper().addThreat(am.getEntity().getBukkitEntity(), (LivingEntity)target, h+1);
		} else {
			am.setTarget(BukkitAdapter.adapt(target));
		}
	}

	public static dList allActiveMobs(World world) {
		dList ams = new dList();
		for (ActiveMob am : mythicmobs.getMobManager().getActiveMobs()) {
			if (am.getLocation().getWorld().getName().equals(world.getName())) {
				ams.add(new dActiveMob(am).identify());
			}
		}
		return ams;
	}

	public static boolean isMythicSpawner(String uniqueName) {
		return mythicmobs.getSpawnerManager().getSpawnerByName(uniqueName) != null;
	}

	public static MythicSpawner getMythicSpawner(String uniqueName) {
		return MythicMobs.inst().getSpawnerManager().getSpawnerByName(uniqueName);
	}

	public static dList allMythicSpawners(World world) {
		dList mss = new dList();
		for (MythicSpawner ms : MythicMobs.inst().getSpawnerManager().listSpawners) {
			if (ms.getLocation().getWorld().getName().equals(world.getName())) {
				mss.add(new dMythicSpawner(ms).identify());
			}
		}
		return mss;
	}

	public static dList getActiveMobsFromSpawner(MythicSpawner ms) {
		dList ams = new dList();
		for (UUID uuid : ms.getAssociatedMobs()) {
			if (MythicMobs.inst().getMobManager().getActiveMob(uuid).isPresent()) {
				ams.add(new dActiveMob(MythicMobs.inst().getMobManager().getActiveMob(uuid).get()).identify());
			}
		}
		return ams;
	}

	public static void attachMobToSpawner(MythicSpawner ms, dActiveMob activeMob) {
		ActiveMob am = activeMob.getActiveMob();
		am.setSpawner(ms);
		if (!ms.getAssociatedMobs().contains(am.getUniqueId())) ms.trackMob(am);
	}

	public static MythicMob getMythicMob(String uniqueName) {
		return MythicMobs.inst().getMobManager().getMythicMob(uniqueName);
	}

	public static dList getThreatTable(ActiveMob am) {
		if (!am.hasThreatTable()) return null;
		dList tt = new dList();
		Iterator<AbstractEntity> it = am.getThreatTable().getAllThreatTargets().iterator();
		while (it.hasNext()) {
			AbstractEntity ae = it.next();
			tt.add(new dEntity(ae.getBukkitEntity()).identify());
		}
		return tt;
	}

	public static double getThreatValueOf(ActiveMob am, dEntity dentity) {
		AbstractEntity ae = BukkitAdapter.adapt(dentity.getBukkitEntity());
		if (am.hasThreatTable() && am.getThreatTable().getAllThreatTargets().contains(ae)) {
			return am.getThreatTable().getThreat(ae);
		}
		return 0;
	}

	public static void modThreatOfEntity(ActiveMob am, dEntity dentity, double amount, String action) {
		AbstractEntity ae = BukkitAdapter.adapt(dentity.getBukkitEntity());
		if (action.equals("incthreat")) {
			am.getThreatTable().threatGain(ae, amount);
		} else {
			am.getThreatTable().threatLoss(ae, amount);
		}
		return;
	}

	public static void removeThreatOfEntity(ActiveMob am, dEntity dentity) {
		AbstractEntity ae = BukkitAdapter.adapt(dentity.getBukkitEntity());
		am.getThreatTable().getAllThreatTargets().remove(ae);
	}

	public static dList getTargetsFor(Entity bukkitEntity, String targeter) {
		SkillTargeter st = getSkillTargeter(targeter);
		return getTargetsForTargeter(bukkitEntity,null,st);
	}
	
	public static dList getTargetsFor(Location bukkitLocation, String targeter) {
		SkillTargeter st = getSkillTargeter(targeter);
		return getTargetsForTargeter(null,bukkitLocation,st);
	}
	
	private static SkillTargeter getSkillTargeter(String targeterName) {
	    Optional<SkillTargeter> maybeTargeter = Optional.empty();
		targeterName = targeterName.startsWith("@")?targeterName:"@"+targeterName;
		maybeTargeter = Optional.of(AbstractSkill.parseSkillTargeter(targeterName));
		if (!maybeTargeter.isPresent()) return null;
        SkillTargeter targeter = maybeTargeter.get();
        return targeter;
	}
	
	private static dList getTargetsForTargeter(Entity entity,Location l1,SkillTargeter targeter) {
		dList targetList = new dList();
		SkillCaster caster=null;
		AbstractEntity trigger=null;
		AbstractLocation location=BukkitAdapter.adapt(l1);
		if (entity!=null) {
			caster=MythicMobs.inst().getAPIHelper().isMythicMob(entity)?MythicMobs.inst().getAPIHelper().getMythicMobInstance(entity):new ActivePlayer(entity);
			trigger=caster.getEntity();
			location=caster.getLocation();
		}
		SkillMetadata data = new SkillMetadata(SkillTrigger.API,caster,trigger,location,null,null,1.0f);
        if (targeter instanceof IEntitySelector) {
            data.setEntityTargets(((IEntitySelector)targeter).getEntities(data));
            ((IEntitySelector)targeter).filter(data, false);
            for (AbstractEntity ae:data.getEntityTargets()) {
            	targetList.add(new dEntity(ae.getBukkitEntity()).identify());
            }
        }
        if (targeter instanceof ILocationSelector) {
            data.setLocationTargets(((ILocationSelector)targeter).getLocations(data));
            ((ILocationSelector)targeter).filter(data);
            for (AbstractLocation al:data.getLocationTargets()) {
            	Location l = BukkitAdapter.adapt(al);
            	targetList.add(new dLocation(l).identify());
            }
        } else if (targeter instanceof MTOrigin) {
            data.setLocationTargets(((MTOrigin)targeter).getLocation(data.getOrigin()));
            for (AbstractLocation al : data.getLocationTargets()) {
            	Location l = BukkitAdapter.adapt(al);
            	targetList.add(new dLocation(l).identify());
            }
        } else if (targeter instanceof MTTriggerLocation) {
            HashSet<AbstractLocation> lTargets = new HashSet<AbstractLocation>();
            lTargets.add(data.getTrigger().getLocation());
            data.setLocationTargets(lTargets);
            for (AbstractLocation al : data.getLocationTargets()) {
            	Location l = BukkitAdapter.adapt(al);
            	targetList.add(new dLocation(l).identify());
            }
        }
        if (targeter instanceof ConsoleTargeter) {
            data.setEntityTargets(null);
            data.setLocationTargets(null);
        }
        return targetList;
	}
}
