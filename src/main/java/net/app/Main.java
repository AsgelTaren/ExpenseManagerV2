package net.app;

import com.formdev.flatlaf.FlatDarkLaf;

public class Main {

	public static void main(String[] args) {
		FlatDarkLaf.setup();
		App app = new App();
		app.create();

	}

}