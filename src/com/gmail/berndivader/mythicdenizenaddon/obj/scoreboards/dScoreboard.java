package com.gmail.berndivader.mythicdenizenaddon.obj.scoreboards;

import org.bukkit.scoreboard.Scoreboard;

import net.aufdemrand.denizen.utilities.ScoreboardHelper;
import net.aufdemrand.denizencore.objects.Adjustable;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Fetchable;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.tags.Attribute;
import net.aufdemrand.denizencore.tags.TagContext;

public class dScoreboard implements dObject, Adjustable {
	private String prefix;
	private String id;
	private Scoreboard sb;

	public dScoreboard(Scoreboard board, String id) {
		if (board==null) return;
		this.id = id;
		this.sb = board;
	}
	
    public static boolean matches(String string) {
        return valueOf(string) != null;
    }
    
	public static dScoreboard valueOf(String name) {
		return valueOf(name, null);
	}
	
	@Override
	public void adjust(Mechanism m) {
		Element val = m.getValue();
	}

	@Override
	public String getAttribute(Attribute a) {
		if (a==null) return null;
		if (a.matches("id")) {
			return new Element(this.id).getAttribute(a.fulfill(1));
		}
    	System.err.println("sbext");
		return new Element(identify()).getAttribute(a);
	}

	@Override
	public void applyProperty(Mechanism mech) {
	}

	@Override
	public String debug() {
		return prefix+"='<A>"+identify()+"<G>'";
	}

	@Override
	public String getObjectType() {
		return "Scoreboard";
	}

	@Override
	public String getPrefix() {
		return this.prefix;
	}

	@Override
	public String identify() {
		return "scoreboard@" + this.id;
	}

	@Override
	public String identifySimple() {
		return identify();
	}

	@Override
	public boolean isUnique() {
		return true;
	}

	@Override
	public dObject setPrefix(String string) {
		this.prefix = string;
		return this;
	}

    @Fetchable("scoreboard")
    public static dScoreboard valueOf(String string, TagContext context) {
        if (string == null) return null;
        String id = string.replace("scoreboard@", "");
        return new dScoreboard(ScoreboardHelper.getScoreboard(id), id);
    }
}
