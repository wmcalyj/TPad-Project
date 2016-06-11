package com.example.hapticebook.data;

public enum HapticFilterEnum {
	// We no longer want noise
	NONE(0), ORIGINAL(1), WOODCUT(2), CANNY(3), WALLPAPER1(4), WALLPAPER2(5), WALLPAPER3(6), WALLPAPER4(7);

	private int index;

	HapticFilterEnum(int index) {
		this.index = index;
	}

	public int getFilterIndex() {
		return this.index;
	}

	public boolean isWallPaper() {
		switch (this) {
		case WALLPAPER1:
		case WALLPAPER2:
		case WALLPAPER3:
		case WALLPAPER4:
			return true;
		default:
			return false;
		}
	}

}
