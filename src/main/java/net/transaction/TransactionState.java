package net.transaction;

import java.awt.Color;

public enum TransactionState {
	DONE(Color.GREEN), PENDING(Color.MAGENTA), PLANNED(Color.YELLOW);

	private Color color;

	private TransactionState(Color color) {
		this.color = color;
	}

	public String getLocalizationName() {
		return "transaction." + name().toLowerCase();
	}

	public Color getColor() {
		return color;
	}

	@Override
	public String toString() {
		return name().toLowerCase();
	}
}
