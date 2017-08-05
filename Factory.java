import java.util.*;

public class Factory {
	int factoryId, owner, production, cyborgCount, waitTime, target, stops;
	ArrayList<int[]> links = new ArrayList<int[]>();
	public Factory(int fId, int o, int p, int cCount, int wTime) {
		factoryId = fId;
		owner = o;
		production = p;
		cyborgCount = cCount;
		waitTime = wTime;
		stops = 0;
	}
	
	public void setFactoryTarget(int targetF) {
		target = targetF;
	}
	
	
	public void findLinks(){
		for (int[] link : Player.links) {
			if (link[0] == factoryId) {
				int[] tempLink = {link[1], link[2]};
				links.add(tempLink);
			} else if (link[1] == factoryId) {
				int[] tempLink = {link[0], link[2]};
				links.add(tempLink);
			}
		}
	}
	
	public int distanceToFactory(int factoryId){
		for (int i = 0; i < links.size(); i++){
			int[] link = links.get(i);
			if (link[0] == factoryId){
				return link[1];
			}
		}
		return 100;
	}
	
	public int myClosestFactoryDistance() {
		int d = 100;
		for (Factory f : Player.factories) {
			if (f.factoryId != factoryId && f.owner == 1) {
				int distance = distanceToFactory(f.factoryId);
				if (distance < d) d = distance;
			}
		}
		return d;
	}
	
	public int availableCyborgs(Brain b) {
		int cyborgs = cyborgCount;
		if (owner == 1) {
			for (Troop t : b.enemyTroops) {
				if (t.arrival == factoryId && t.distance < 5) cyborgs -= t.cyborgCount;
			}
			for (Troop t : b.myTroops) {
				if (t.arrival == factoryId && t.distance < 3) cyborgs += t.cyborgCount;
			}
		} else {
			for (Troop t : b.enemyTroops) {
				if (t.arrival == factoryId && t.distance < 5) cyborgs += t.cyborgCount;
			}
			for (Troop t : b.myTroops) {
				if (t.arrival == factoryId && t.distance < 3) cyborgs -= t.cyborgCount;
			}
		}
		return cyborgs;
	}
}
