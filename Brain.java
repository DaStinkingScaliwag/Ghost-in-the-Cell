import java.util.*;
public class Brain {
	//where all the thinking happens
	
	public Brain() {
		
	}
	ArrayList<Integer> hit;

	ArrayList<Factory> myFactories = new ArrayList<Factory>();
	ArrayList<Factory> neutralFactories  = new ArrayList<Factory>();
	ArrayList<Factory> enemyFactories  = new ArrayList<Factory>();
	ArrayList<Factory> notMyFactories = new ArrayList<Factory>();
	
	public void setFactories() { //creates list of factories with respected owners
		for (Factory f : Player.factories) {
			switch (f.owner){
			case 1:
				myFactories.add(f);
				break;
			case -1:
				enemyFactories.add(f);
				notMyFactories.add(f);
				break;
			default:
				neutralFactories.add(f);
				notMyFactories.add(f);
				break;
			}
		}
	}
	
	ArrayList<Troop> myTroops = new ArrayList<Troop>();
	ArrayList<Troop> enemyTroops = new ArrayList<Troop>();
	
	public void setTroops() { //creates list of troops with respected owners
		for (Troop t : Player.troops){
			if (t.owner == 1) {
				myTroops.add(t);
			} else {
				enemyTroops.add(t);
			}
		}
	}
	
	ArrayList<Bomb> myBombs = new ArrayList<Bomb>();
	ArrayList<Bomb> enemyBombs = new ArrayList<Bomb>();
	public void setBombs(){
		for (Bomb b : Player.bombs){
			if (b.owner == 1){
				myBombs.add(b);
			}else{
				enemyBombs.add(b);
			}
		}
	}
	
	public Factory nearestEnemy(Factory factory, boolean enemy){ // if enemy, target opponent, false then neutral as well.
		ArrayList<Factory> targets;
		if (enemy) {
			targets = enemyFactories;
		}else if (factory.owner == 1 ){
			targets = notMyFactories;
		} else {
			targets = myFactories;
		}
		int closestDistance = 100;
		Factory closestFactory = null;
		for (Factory f : targets) {
			if (factory.distanceToFactory(f.factoryId) < closestDistance) {
				closestDistance = factory.distanceToFactory(f.factoryId);
				closestFactory = f;
			}
		}
		return closestFactory;
	}
	

	ArrayList<String> out;
	public void think() {
		out = new ArrayList<String>();
		if (Player.turns == 1){
			firstTurn();
		}else{
			//"WAIT" or "MOVE {source} {destination} {cyborgs}" or "BOMB {source} {destination} or "INC {source}"
			for (Factory factory : myFactories) {
				hit = new ArrayList<Integer>();
				boolean madeMove = true;
				do{
					int availableCyborgs = factory.availableCyborgs(this);
					madeMove = false;
					boolean wait = factory.production < 3 && availableCyborgs-factory.cyborgCount > 0; // help is coming

					if (availableCyborgs > 0 && factory.cyborgCount>0 && !(wait)) {
						if (availableCyborgs > 9 && factory.cyborgCount > 9 && factory.production < 3) { //Inc
							out.add("INC " + Integer.toString(factory.factoryId) + "; ");
							availableCyborgs -= 10;
							factory.cyborgCount -= 10;
							madeMove = true;
						} else {
							ArrayList<Factory> tempFactories = new ArrayList<Factory>();
							for (Factory f : Player.factories){
								if (f.factoryId != factory.factoryId) tempFactories.add(f);
							}
							Collections.sort(tempFactories, new Comparator<Factory>() {
								public int compare(Factory f1, Factory f2) {
									return f1.distanceToFactory(factory.factoryId) - f2.distanceToFactory(factory.factoryId);
								}
							});
							for (Factory possibleDest : tempFactories) {
								availableCyborgs = factory.availableCyborgs(this);
								if (availableCyborgs > 0 && factory.cyborgCount > 0){
									if (possibleDest.owner == 1) {
										//Defend if closest and needed
										madeMove = defend(factory, possibleDest);
									} else {
										//Attack
										madeMove = attack(factory, possibleDest);
									}
								}
							}
						}
					} else if ((wait) && availableCyborgs > 9 && factory.cyborgCount>9) {
					    out.add("INC " + Integer.toString(factory.factoryId) + "; ");
						availableCyborgs -= 10;
						factory.cyborgCount -= 10;
						madeMove = true;
					}
					   
				} while (madeMove);
				
				//TODO sort enemyFactories, if best.production > 1 bomb it
				for (Factory f : enemyFactories) {
					boolean alreadyBombed = false;
					for (Bomb b : myBombs) {
						if (b.arrival == f.factoryId) {
							alreadyBombed = true;
						}
					}
					alreadyBombed = alreadyBombed && factory.distanceToFactory(f.factoryId) >= f.waitTime;
					boolean notHit = !hit.contains(f.factoryId);
					if (!(alreadyBombed) && Player.bombsRemaining > 0 && notHit && f.production>1) {
						out.add("BOMB "+Integer.toString(factory.factoryId)+" "+Integer.toString(f.factoryId)+"; ");
						Player.bombsRemaining--;
						myBombs.add(new Bomb(1, factory.factoryId, f.factoryId, factory.distanceToFactory(f.factoryId)));
					}
				}
			}
		}
		out.add("WAIT");
		for (String s: out){
			System.out.print(s);
		}
		System.out.println();
	}
	public boolean defend(Factory departure, Factory arrival){
		//figure out if needed
		int myCyborgs = arrival.availableCyborgs(this);
		if (myCyborgs < 0 || arrival.production < 1) {
			//cleared for defense
			int sending = Math.min(departure.cyborgCount, departure.availableCyborgs(this));
			//now find if closest
			if (departure.distanceToFactory(arrival.factoryId) == arrival.myClosestFactoryDistance()) {
				out.add("MOVE "+Integer.toString(departure.factoryId) +" "+Integer.toString(arrival.factoryId)+" "+Integer.toString(sending)+"; ");
				myTroops.add(new Troop(1, departure.factoryId, arrival.factoryId, sending, departure.distanceToFactory(arrival.factoryId)));
				System.err.println("Defending directly from "+Integer.toString(departure.factoryId) +" to "+Integer.toString(arrival.factoryId)+" with "+Integer.toString(sending));
				hit.add(arrival.factoryId);
			} else {
				Factory eventual = arrival;
				arrival = nextClosest(departure, arrival);
				out.add("MOVE "+Integer.toString(departure.factoryId) +" "+Integer.toString(arrival.factoryId)+" "+Integer.toString(sending)+"; ");
				myTroops.add(new Troop(1, departure.factoryId, arrival.factoryId, sending, departure.distanceToFactory(arrival.factoryId)));
				System.err.println("Moving to defend from "+Integer.toString(departure.factoryId) +" to "+Integer.toString(arrival.factoryId)+" with "+Integer.toString(sending)+" for "+Integer.toString(eventual.factoryId));
			}
			departure.cyborgCount -= sending;
			return true;
		}
		return false;
	}
	
	public boolean attack(Factory departure, Factory arrival){
		int sending = Math.min(departure.cyborgCount, departure.availableCyborgs(this));
		if (departure.distanceToFactory(arrival.factoryId) == arrival.myClosestFactoryDistance()) {
			int availableCyborgs = Math.max(arrival.cyborgCount, arrival.availableCyborgs(this));
			if (availableCyborgs < sending) {
				out.add("MOVE "+Integer.toString(departure.factoryId) +" "+Integer.toString(arrival.factoryId)+" "+Integer.toString(sending)+"; ");
				myTroops.add(new Troop(1, departure.factoryId, arrival.factoryId, sending, departure.distanceToFactory(arrival.factoryId)));
				System.err.println("Attacking directly from "+Integer.toString(departure.factoryId) +" to "+Integer.toString(arrival.factoryId)+" with "+Integer.toString(sending));
				hit.add(arrival.factoryId);
			} else {
				return false;
			}
		} else {
			Factory eventual = arrival;
			departure.stops = 1;
			arrival = nextClosest(departure, arrival);
			out.add("MOVE "+Integer.toString(departure.factoryId) +" "+Integer.toString(arrival.factoryId)+" "+Integer.toString(sending)+"; ");
			myTroops.add(new Troop(1, departure.factoryId, arrival.factoryId, sending, departure.distanceToFactory(arrival.factoryId)));
			System.err.println("Moving to attack from "+Integer.toString(departure.factoryId) +" to "+Integer.toString(arrival.factoryId)+" with "+Integer.toString(sending)+" for "+ Integer.toString(eventual.factoryId));
		}
		departure.cyborgCount -= sending;
		return true;
	}
	public Factory nextClosest(Factory departure, Factory arrival) {
		ArrayList<Factory> tempMyFactories = new ArrayList<Factory>();
		for (Factory f : myFactories){
			if (f.factoryId != departure.factoryId && f.factoryId != arrival.factoryId) tempMyFactories.add(f);
		}
		Collections.sort(tempMyFactories, new Comparator<Factory>() {
			public int compare(Factory f1, Factory f2) {
				return f1.distanceToFactory(arrival.factoryId) - f2.distanceToFactory(arrival.factoryId);
			}
		});
		ArrayList<Factory> options = new ArrayList<Factory>();
		for (Factory f : tempMyFactories) {
			boolean closerToArrival = f.distanceToFactory(arrival.factoryId) < departure.distanceToFactory(arrival.factoryId);
			boolean closer = departure.distanceToFactory(f.factoryId) < departure.distanceToFactory(arrival.factoryId);
			if (closerToArrival && closer) options.add(f);
		}
	    Factory bestStop = arrival;
	    int bestDist = 100;
	    for (Factory f : options) {
	        int distance = f.distanceToFactory(arrival.factoryId) + departure.distanceToFactory(arrival.factoryId);
	        if (distance <= bestDist) {
	            bestDist = distance;
	            bestStop = f;
	        }
		}
	    //time to check to make sure a stop shouldn't be made to get to the stop!
	    if (departure.stops == 2 || (arrival == bestStop || departure.distanceToFactory(bestStop.factoryId) == bestStop.myClosestFactoryDistance())) {
	    	return bestStop;
	    }else{
	    	if (departure.stops == 1) {
	    		departure.stops = 2;
	    	}
	    	return nextClosest(departure, bestStop);
	    }
	}
	
	public void firstTurn(){
		hit = new ArrayList<Integer>();
		Factory factory = myFactories.get(0);
		Factory enemy = enemyFactories.get(0);
		ArrayList<Factory> tempFactories = new ArrayList<Factory>();
		for (Factory f : Player.factories){
			if (f.factoryId != factory.factoryId && f.factoryId != enemy.factoryId) tempFactories.add(f);
		}
		Collections.sort(tempFactories, new Comparator<Factory>() {
			public int compare(Factory f1, Factory f2) {
				return f1.distanceToFactory(enemy.factoryId) - f2.distanceToFactory(enemy.factoryId);
			}
		});
		tempFactories.add(0, enemy);
		for (Factory f: tempFactories) {
			boolean enemyWorthBombing = f.factoryId == enemy.factoryId && enemy.production>1;
			boolean neutralWorthBombing = f.production>1 && enemy.distanceToFactory(f.factoryId)<factory.distanceToFactory(f.factoryId);
			if (enemyWorthBombing || neutralWorthBombing){
				out.add("BOMB "+Integer.toString(factory.factoryId)+" "+Integer.toString(f.factoryId)+"; ");
				hit.add(f.factoryId);
				Player.bombsRemaining--;
				break;
			}
		}
		//sort on production, then by distance
		tempFactories = new ArrayList<Factory>();
		ArrayList<Integer> factoryNbs = new ArrayList<Integer>();
		for (Factory f : Player.factories){
			boolean closerOrEqual = f.distanceToFactory(factory.factoryId) <= f.distanceToFactory(enemy.factoryId);
			if (f.factoryId != factory.factoryId && (!hit.contains(f.factoryId)) && closerOrEqual && f.factoryId != enemy.factoryId){
				tempFactories.add(f);
				factoryNbs.add(f.factoryId);
			}
		}
		Random generator = new Random();
		ArrayList<Factory> best = new ArrayList<Factory>();
		int bestScore = 0;
		for (int i = 0; i < 1000; i++){//randomly chooses + scores
			int cyborgs = factory.cyborgCount;
			int score;
			ArrayList<Factory> possibility = new ArrayList<Factory>();
			ArrayList<Integer> used = new ArrayList<Integer>();
			do {
				score = 0;
				int index = Math.min(generator.nextInt(Math.round(tempFactories.size()/2))+Math.round(tempFactories.size()/2), tempFactories.size()-1);
				if (!used.contains(index)) {
					Factory p = tempFactories.get(index);
					if (cyborgs - p.cyborgCount - 1 >= 0) {
						possibility.add(p);
						score += p.production;
					} else {
						break;
					}
				}
			}while (cyborgs >= 0);
			if (score > bestScore) {
				best = possibility;
				bestScore = score;
			}
		}
		for (Factory f : best) {
			if (factory.cyborgCount < 1) break;
			int sending = Math.min(factory.cyborgCount, f.cyborgCount + 1);
			factory.cyborgCount -= sending;
			hit.add(f.factoryId);
			out.add("MOVE "+Integer.toString(factory.factoryId)+" "+Integer.toString(f.factoryId)+" "+Integer.toString(sending)+"; ");
		}
	}
}
