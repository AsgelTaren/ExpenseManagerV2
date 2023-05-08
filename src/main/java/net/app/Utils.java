package net.app;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import javax.imageio.ImageIO;

public class Utils {

	public static final String loadFile(String loc) {
		try {
			return new String(
					new BufferedInputStream(Thread.currentThread().getContextClassLoader().getResourceAsStream(loc))
							.readAllBytes(),
					"UTF-8");
		} catch (IOException e) {
			return null;
		}
	}

	public static final BufferedImage loadImage(String loc) {
		try {
			return ImageIO.read(Thread.currentThread().getContextClassLoader().getResourceAsStream(loc));
		} catch (IOException e) {
			return null;
		}
	}

	public static final Date parseDate(DateFormat format, String target) {
		try {
			return format.parse(target);
		} catch (ParseException e) {
			return null;
		}
	}
}
