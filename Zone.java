package diff_match_patch;


public class Zone {

	Point top;
	Point bottom;

	public Zone(int topX, int topY, int bottomX, int bottomY) {
		top = new Point(topX, topY);
		bottom = new Point(bottomX, bottomY);
	}

	Point getTop() {
		return top;
	}

	Point getBottom() {
		return bottom;
	}

	void setZone(int x1, int y1, int x2, int y2) {
		top.setxy(x1, y1);
		bottom.setxy(x2, y2);
	}

}
