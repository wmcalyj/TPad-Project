package com.example.hapticebook.data;

public enum HapticFilterEnum {
	NONE, ORIGINAL, WOODCUT, NOISE, CANNY, WALLPAPER1, WALLPAPER2, WALLPAPER3;

	public boolean isWallPaper() {
		switch (this) {
		case WALLPAPER1:
		case WALLPAPER2:
		case WALLPAPER3:
			return true;
		default:
			return false;
		}
	}

}
