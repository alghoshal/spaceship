package in.game.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Representation of the Spaceship game
 * 
 * @author aghoshal
 */
public class SpaceshipGame extends BoardGame{
	private static final Logger log = LoggerFactory.getLogger(SpaceshipGame.class);
	
	final static char[] digits = {
	        '0' , '1' , '2' , '3' , '4' , '5' ,
	        '6' , '7' , '8' , '9' , 'A' , 'B' ,
	        'C' , 'D' , 'E' , 'F'};
	
	int[] shipAtGridLocation;
	int[] hitsPendingShip;

	int maxDistinctAttempts = 50;
	public SpaceshipGame(){}
	
	public SpaceshipGame(User self, User opponent, int gridSizeX, int gridSizeY){
		super(self, opponent, gridSizeX, gridSizeY);
		resetBoard(this.getBoardSelf(), SalvoImpactType.EMPTY.getImpactValue());
		resetBoard(this.getBoardOpponent(), SalvoImpactType.EMPTY.getImpactValue());
		
		this.hitsPendingShip = new int[SpaceshipType.values().length];
		this.shipAtGridLocation = new int[gridSizeX*gridSizeY];
	}

	public SpaceshipGame(User player1, User player2){
		this(player1,player2,16,16);
	}
	
	public boolean updateBoard(char[][] board,char[][] valuesToAssign, int indexX, int indexY, OnBoardEntity spaceship){
		int boardSizeX = board.length;
		int boardSizeY = board[0].length;
		
		int valuesToAssignX = valuesToAssign.length;
		int valuesToAssignY = valuesToAssign[0].length;
		
		if((valuesToAssignX+indexX)>boardSizeX 
				||(valuesToAssignY+indexY>boardSizeY)){
			log.error("Update value beyond grid: "+indexX+", "+indexY+" | "+valuesToAssign);
			return false;
		}
		
		int i,j;
		
		// Test if assignment is valid
		// TODO: Improve range/ index based!
		for (i=0;i<valuesToAssignX;i++) {
			for(j=0;j<valuesToAssignY;j++){
				if(board[i+indexX][j+indexY]!=SalvoImpactType.EMPTY.getImpactValue()){
					log.debug("Board location not empty: "+(i+indexX)+", "+(j+indexY));
					return false;
				}
			}
		}
		
		int updatedX, updatedY;
		// Update
		for (i=0;i<valuesToAssignX;i++) {
			for(j=0;j<valuesToAssignY;j++){
				updatedX = i+indexX;
				updatedY=j+indexY;
				board[updatedX][updatedY]=valuesToAssign[i][j];
				shipAtGridLocation[updatedX*boardSizeX+updatedY] = spaceship.getEntityId();
				if(SalvoImpactType.BODY.getImpactValue()==valuesToAssign[i][j])	hitsPendingShip[spaceship.getEntityId()]++;
			}
		}
		selfOnBoardEntitiesAlive++;
		
		log.debug(valuesToAssign+" Updated at location: "+indexX+", "+indexY);
		return true;
 	}

	/**
	 * Handles salvo
	 * @param salvo fire on self
	 * @return
	 */
	public SalvoImpact handleSalvoFireOnSelf(String[] salvo) {
		SalvoImpact impact = new SalvoImpact();
		Map<String, String> salvoResults = new HashMap<String, String>();
		int salvoX, salvoY;
		char[][] boardSelf = this.self.getBoard();
		selfOnBoardEntityDestroyed=false;
		for (String inputSalvo : salvo) {
			Pair<Integer, Integer> coordinates = parseCoordinates(inputSalvo);
			salvoX = coordinates.getKey();
			salvoY = coordinates.getValue();
			// Hit
			if(SalvoImpactType.BODY.getImpactValue()==
					boardSelf[salvoX][salvoY]){
				// Update hit
				boardSelf[salvoX][salvoY] = SalvoImpactType.HIT.getImpactValue();
				
				// Update hits counter for ship
				hitsPendingShip[shipAtGridLocation[salvoX*boardSelf.length+salvoY]]--;

				salvoResults.put(inputSalvo, SalvoImpactType.HIT.getImpactName());
				
				// Update ships alive
				if(hitsPendingShip[shipAtGridLocation[salvoX*boardSelf.length+salvoY]]==0){
					log.debug(salvoX+", "+salvoY+" kill");
					this.selfOnBoardEntitiesAlive--;
					this.selfOnBoardEntityDestroyed=true;
					// Update kill
					salvoResults.put(inputSalvo, SalvoImpactType.KILL.getImpactName());
				}else {
					log.debug(salvoX+", "+salvoY+" hit");
				}
			}
			else {
				log.debug(salvoX+", "+salvoY+" miss");
				// Update miss
				boardSelf[salvoX][salvoY] = SalvoImpactType.MISS.getImpactValue();
				salvoResults.put(inputSalvo,SalvoImpactType.MISS.getImpactName());
			}
		}
		
		impact.setSalvo(salvoResults);
		return impact;
	}
	
	/**
	 * 
	 * @param salvoResponse
	 * @return
	 */
	public SalvoImpact captureSalvoImpactOnOpponent(SalvoImpact salvoResponse) {
		int salvoX, salvoY;
		Map<String, String> salvo = salvoResponse.getSalvo();
		char[][] boardOpponent = this.opponent.getBoard();
		opponentOnBoardEntityDestroyed = false;
		for (String inputSalvo : salvo.keySet()) {
			Pair<Integer, Integer> coordinates = parseCoordinates(inputSalvo);
			salvoX = coordinates.getKey();
			salvoY = coordinates.getValue();
			
			String salvoImpactValue = salvo.get(inputSalvo);
			SalvoImpactType salvoImpact = SalvoImpactType.valueOf(salvoImpactValue.toUpperCase());
			boardOpponent[salvoX][salvoY] = salvoImpact.impactValue;
			log.debug(salvoImpact.impactValue+" impact on opponent :"+inputSalvo);
			if(SalvoImpactType.KILL.equals(salvoImpact)){
				opponentOnBoardEntityDestroyed=true;
				opponentOnBoardEntitiesAlive--;
			}
		}
		return salvoResponse;
	}
	
	/**
	 * Updates all salvos to {@link SalvoImpactType#MISS}.
	 * 
	 * @param setUpSalvoImpactForError
	 * @param salvo
	 * @return
	 */
	public SalvoImpact updateAllSalvosAsMisses(SalvoImpact salvoImpact, String[] salvo) {
		Map<String, String> salvoResults = new HashMap<String, String>();
		
		for (String shot : salvo) {
			salvoResults.put(shot, SalvoImpactType.MISS.getImpactName());
		}
		salvoImpact.setSalvo(salvoResults);
		return salvoImpact;
	}
	
	/**
	 * Returns the set of salvos to fire
	 * 
	 * @return
	 */
	public String[] getSalvoSet(){
		return getSalvoSet(getGameRules().getNumberOfShots(this));
	}
	
	/**
	 * Fetches a set of salvo of size=count
	 * @param count
	 * @return
	 */
	public String[] getSalvoSet(int count){
		
		String [] salvo = new String[count];
		int i=0;
		Random r = new Random();
		int boardSizeX =opponent.getBoard().length;
		int boardSizeY = opponent.getBoard()[0].length;
		char[][] boardOpponent = opponent.getBoard();
		Set<String> setOfSalvo = new HashSet<String>();
		int attemptCount = 0;
		while(i<count){
			int coordX = r.nextInt(boardSizeX);
			int coordY = r.nextInt(boardSizeY);
			String newCoordHex = digits[coordX]+"x"+digits[coordY];
			if(!setOfSalvo.contains(newCoordHex)){
				if(SalvoImpactType.EMPTY.impactValue==boardOpponent[coordX][coordY]
						||(attemptCount > maxDistinctAttempts
								&& SalvoImpactType.MISS.impactValue==boardOpponent[coordX][coordY])){
					// Initial maxDistinctAttempts to pick salvos
					salvo[i] = newCoordHex; 
					setOfSalvo.add(newCoordHex);
					i++;
				}
			}
			 attemptCount++;
		}
		return salvo;
		
	}

	public int getMaxDistinctAttempts() {
		return maxDistinctAttempts;
	}

	public void setMaxDistinctAttempts(int maxDistinctAttempts) {
		this.maxDistinctAttempts = maxDistinctAttempts;
	}
}
