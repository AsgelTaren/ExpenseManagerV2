package net.app;

import java.util.HashMap;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class LangAtlas {

	private String prefix;
	private HashMap<String, String> lang;

	public LangAtlas(String prefix) {
		this.prefix = prefix;
	}

	public void loadLang() {
		lang = new HashMap<>();
		String data = Utils.loadFile("langs/" + prefix + ".lang");

		JsonObject langObj = JsonParser.parseString(data).getAsJsonObject();
		langObj.entrySet().forEach(e -> lang.put(e.getKey(), e.getValue().getAsString()));
	}

	public String getText(String key) {
		return lang.get(key);
	}

}