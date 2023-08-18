package net.app;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import javax.swing.ImageIcon;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class IconAtlas {

	private static final Logger logger = LogManager.getLogger(IconAtlas.class);

	private HashMap<String, BufferedImage> atlas = new HashMap<>();

	public IconAtlas() {
		super();
	}

	public void loadIcons() {
		String indexData = Utils.loadFile("icons/index.json");

		JsonObject index = JsonParser.parseString(indexData).getAsJsonObject();
		JsonArray iconsArray = index.get("icons").getAsJsonArray();
		iconsArray.iterator().forEachRemaining(e -> {
			atlas.put(e.getAsString(), Utils.loadImage("icons/" + e.getAsString() + ".png"));
			logger.info("Loaded icon " + e.getAsString());
		});
	}

	public ImageIcon getIcon(String key, int size) {
		try {
			return new ImageIcon(atlas.get(key).getScaledInstance(size, size, Image.SCALE_SMOOTH));
		} catch (Exception e) {
			System.out.println("Failed to load icon: " + key);
			return null;
		}
	}

	public Image getRawImage(String key, int size) {
		return atlas.get(key).getScaledInstance(size, size, Image.SCALE_SMOOTH);
	}

}