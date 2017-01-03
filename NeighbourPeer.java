package diff_match_patch;



public class NeighbourPeer {
	private Zone zone;
	private String ipAddress;
	private Zone neighbourTo;

	public NeighbourPeer(Zone zone, Zone neighbourTo, String ipAddress) {
		this.zone = zone;
		this.ipAddress = ipAddress;
		this.neighbourTo = neighbourTo;
	}

	Zone getZone() {
		return zone;
	}

	String getIPAddress() {
		return ipAddress;
	}

	Zone getNeighbourTo() {
		return neighbourTo;
	}

	void setIPAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	void setZone(Zone zone) {
		this.zone = zone;
	}

}
