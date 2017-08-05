import java.util.*;

public class Player {
	/*
	 * personal best score: #9 Silver league score 24.11
	 */
	static Factory[] factories;
	static ArrayList<Troop> troops;
	static  ArrayList<Bomb> bombs;
	static ArrayList<int[]> links;
	static int bombsRemaining = 2;
	static int turns = 0;
	static int factoryCount;
	public static void main(String args[]) {

		Scanner in = new Scanner(System.in);
        factoryCount = in.nextInt(); // the number of factories
        int linkCount = in.nextInt(); // the number of links between factories
        
        links = new ArrayList<int[]>();
        for (int i = 0; i < linkCount; i++) {
            int factory1 = in.nextInt(); 
            int factory2 = in.nextInt();
            int distance = in.nextInt();
            int[] link = new int[3];
            link[0] = factory1;
            link[1] = factory2;
            link[2] = distance;
            links.add(link);
        }

        // game loop
        while (true) {
        	turns++;
        	factories = new Factory[factoryCount];
        	troops = new ArrayList<Troop>();
        	bombs = new ArrayList<Bomb>();
        	
            int entityCount = in.nextInt(); // the number of entities (e.g. factories and troops)
            for (int i = 0; i < entityCount; i++) {
                int entityId = in.nextInt();
                String entityType = in.next();
                int owner = in.nextInt();
                int arg2 = in.nextInt();
                int arg3 = in.nextInt();
                int arg4 = in.nextInt();
                int arg5 = in.nextInt();

                if (entityType.equals("FACTORY")) {
                	int cyborgCount = arg2;
                	int production = arg3;
                	int waitTime = arg4;
                	//ignore arg5, useless
                	
                	factories[entityId] = new Factory(entityId, owner, production, cyborgCount, waitTime);
                	factories[entityId].findLinks();
                } else if (entityType.equals("TROOP")) {
                	int departure = arg2;
                	int arrival = arg3;
                	int cyborgCount = arg4;
                	int distance = arg5;
                	
                	troops.add(new Troop(owner, departure, arrival, cyborgCount, distance));
                } else {
                	int departure = arg2;
                	int arrival = arg3; //will be -1 if mine
                	int distance = arg4;
                	//ignore arg5, useless
                	
                	bombs.add(new Bomb(owner, departure, arrival, distance));
                }
                
            }
            
            Brain currentBrain = new Brain();
            currentBrain.setFactories();
            currentBrain.setTroops();
            currentBrain.setBombs();
            currentBrain.think();
        }
    }
}
