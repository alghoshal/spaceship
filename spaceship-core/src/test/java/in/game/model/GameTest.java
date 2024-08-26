package in.game.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import in.game.factory.SpaceshipFactory;
import in.game.factory.SpaceshipFactoryImpl;
import in.game.game.rules.GameRulesFactory;

public class GameTest {
	private static final Logger log = LoggerFactory.getLogger(GameTest.class);

	SpaceshipGame game;
	String selfId = "self";
	String opponentId = "opponent";
	
	@Before
	public void setUp(){
		User self = new User();
		self.setUserId(selfId);
		
		User opponent = new User();
		opponent.setUserId(opponentId);
		game = new SpaceshipGame(self,opponent);
	}
	
	@Test
	public void testResetBoard(){
		char [][] board = new char[16][16];
		game.resetBoard(board, SalvoImpactType.EMPTY.getImpactValue());
		
		for (char[] cs : board) {
			for (char c : cs) {
				assertEquals(SalvoImpactType.EMPTY.getImpactValue(),c);
			}
		}
	}
	
	@Test
	public void testUpdateBoard(){
		char [][] board = new char[16][16];
		game.resetBoard(board, SalvoImpactType.EMPTY.getImpactValue());
		game.getSelf().setBoard(board);
		
		SpaceshipFactory factory = new SpaceshipFactoryImpl();
		Spaceship winger = factory.createWinger(SpaceshipOrientation.FACING_EAST);
		
		boolean result = game.updateBoard(board, winger.shape, 10, 10, winger);
		assertTrue(result);
		log.debug(game.toString());
	}
	
	@Test
	public void testUpdateBoardNorthOrientation(){
		char [][] board = new char[16][16];
		game.resetBoard(board, SalvoImpactType.EMPTY.getImpactValue());
		game.getSelf().setBoard(board);
		
		SpaceshipFactory factory = new SpaceshipFactoryImpl();
		Spaceship winger = factory.createSClass(SpaceshipOrientation.FACING_NORTH);
		
		boolean result = game.updateBoard(board, winger.shape, 10, 10, winger);
		assertTrue(result);
		log.debug(game.toString());
	}
	
	@Test
	public void testUpdateBoardAlreadyOccupied(){
		char [][] board = new char[16][16];
		game.resetBoard(board, SalvoImpactType.EMPTY.getImpactValue());
		board[10][10] = SalvoImpactType.HIT.getImpactValue();
		game.getSelf().setBoard(board);
		
		SpaceshipFactory factory = new SpaceshipFactoryImpl();
		Spaceship sClass = factory.createSClass(SpaceshipOrientation.FACING_EAST);
		
		boolean result = game.updateBoard(board, sClass.shape, 10, 10, sClass);
		assertFalse(result);
		
		log.debug(game.toString());
		
		for (char[] cs : board) {
			for (char c : cs) {
				assertTrue(SalvoImpactType.EMPTY.getImpactValue()==c||SalvoImpactType.HIT.getImpactValue()== c);
			}
		}
	}
	
	@Test
	public void testUpdateBoardBeyondBoardBoundaries(){
		char [][] board = new char[16][16];
		game.resetBoard(board, SalvoImpactType.EMPTY.getImpactValue());
		game.getSelf().setBoard(board);
		
		SpaceshipFactory factory = new SpaceshipFactoryImpl();
		Spaceship winger = factory.createWinger(SpaceshipOrientation.FACING_EAST);
		
		boolean result = game.updateBoard(board, winger.shape, 12, 12, winger);
		assertFalse(result);
		
		for (char[] cs : board) {
			for (char c : cs) {
				assertEquals(SalvoImpactType.EMPTY.getImpactValue(), c);
			}
		}
	}
	
	@Test
	public void testPlayerTurn(){
		User self = new User();
		self.setUserId(selfId);
		game.setSelf(self);
		
		User opponent = new User();
		opponent.setUserId(opponentId);
		game.setOpponent(opponent);
		

		game.setTurnSelf(true);
		assertEquals(selfId, game.getPlayerTurn());
		
		game.setTurnSelf(false);
		assertEquals(opponentId, game.getPlayerTurn());
	}
	
	@Test
	public void testParseCoordinates(){
		Pair<Integer, Integer> coordinate = game.parseCoordinates("0xD");
		log.debug(coordinate.toString());
		assertEquals(0, coordinate.getKey().intValue());
		assertEquals(13, coordinate.getValue().intValue());
		
		coordinate = game.parseCoordinates("ExA");
		log.debug(coordinate.toString());
		assertEquals(14, coordinate.getKey().intValue());
		assertEquals(10, coordinate.getValue().intValue());
	}
	
	@Test
	public void testHandleSalvoFireOnSelfMiss(){
		char [][] board = new char[16][16];
		game.resetBoard(board, SalvoImpactType.EMPTY.getImpactValue());
		game.getSelf().setBoard(board);
		
		SpaceshipFactory factory = new SpaceshipFactoryImpl();
		Spaceship sClass = factory.createSClass(SpaceshipOrientation.FACING_EAST);
		
		boolean result = game.updateBoard(board, sClass.shape, 10, 10, sClass);
		assertTrue(result);
		log.debug(game.printBoard(game.getBoardSelf()));
		
		String [] salvo = {"0x0", "8x4", "DxA", "AxA", "7xF"};
		SalvoImpact salvoImpact = game.handleSalvoFireOnSelf(salvo);
		for (String salvoImpactResults : salvoImpact.getSalvo().values()) {
			assertEquals(SalvoImpactType.MISS.getImpactName(),salvoImpactResults);
		}
		
		assertTrue(game.isActive());
		assertTrue("Actual: "+game.getSelfOnBoardEntitiesAlive(),1==game.getSelfOnBoardEntitiesAlive());
		assertEquals(8, game.hitsPendingShip[SpaceshipType.S_CLASS.ordinal()]);
		log.debug(salvoImpact.toString());
		
	}
	
	@Test
	public void testHandleSalvoFireOnSelfHit(){
		char [][] board = new char[16][16];
		game.resetBoard(board, SalvoImpactType.EMPTY.getImpactValue());
		game.getSelf().setBoard(board);
		
		SpaceshipFactory factory = new SpaceshipFactoryImpl();
		Spaceship sClass = factory.createSClass(SpaceshipOrientation.FACING_EAST);
		
		boolean result = game.updateBoard(board, sClass.shape, 10, 10, sClass);
		assertTrue(result);
		log.debug(game.printBoard(game.getBoardSelf()));
		
		String[] salvo = {"AxB", "8x4", "DxA", "AxA", "7xF"};
		SalvoImpact salvoImpact = game.handleSalvoFireOnSelf(salvo);
		log.debug(game.printBoard(game.getBoardSelf()));
		
		assertEquals(7, game.hitsPendingShip[SpaceshipType.S_CLASS.ordinal()]);
		assertEquals(SalvoImpactType.HIT.getImpactName(), salvoImpact.getSalvo().get("AxB"));
		assertTrue(game.isActive());
		assertTrue("Actual: "+game.getSelfOnBoardEntitiesAlive(),1==game.getSelfOnBoardEntitiesAlive());
		log.debug(salvoImpact.toString());
	}
	
	@Test
	public void testHandleSalvoFireOnSelfKill(){
		char [][] board = new char[16][16];
		game.resetBoard(board, SalvoImpactType.EMPTY.getImpactValue());
		
		// Last ship body on board
		board[10][10] = SalvoImpactType.BODY.getImpactValue();
		game.getSelf().setBoard(board);
		game.shipAtGridLocation[10*16+10]=SpaceshipType.S_CLASS.ordinal();
		game.hitsPendingShip[SpaceshipType.S_CLASS.ordinal()]=1;
		game.setSelfOnBoardEntitiesAlive(1);
		
		String [] salvo = {"AxB", "8x4", "DxA", "AxA", "7xF"};
		SalvoImpact salvoImpact = game.handleSalvoFireOnSelf(salvo);
		log.debug(game.printBoard(game.getBoardSelf()));
		
		assertEquals(SalvoImpactType.KILL.getImpactName(),salvoImpact.getSalvo().get("AxA"));
		
		assertEquals(0, game.hitsPendingShip[SpaceshipType.S_CLASS.ordinal()]);
		assertEquals(0, game.getSelfOnBoardEntitiesAlive());
		assertTrue("Actual: "+game.getSelfOnBoardEntitiesAlive(),0==game.getSelfOnBoardEntitiesAlive());
		log.debug(salvoImpact.toString());
	}
	
	@Test
	public void testCaptureImpactOfSalvoOnOpponent(){
		char [][] boardSelf = new char[16][16];
		game.resetBoard(boardSelf, SalvoImpactType.EMPTY.getImpactValue());
		game.getSelf().setBoard(boardSelf);
		
		String opponentId="2";
		User opponent = new User();
		opponent.setUserId(opponentId);
		game.setOpponent(opponent);
		
		char [][] boardOpponent = new char[16][16];
		game.resetBoard(boardOpponent, SalvoImpactType.EMPTY.getImpactValue());
		game.getOpponent().setBoard(boardOpponent);
		
		SpaceshipFactory factory = new SpaceshipFactoryImpl();
		Spaceship sClass = factory.createSClass(SpaceshipOrientation.FACING_EAST);
		
		boolean result = game.updateBoard(boardSelf, sClass.shape, 10, 10, sClass);
		assertTrue(result);
		log.debug(game.printBoard(game.getBoardSelf()));
		
		SalvoImpact salvoResponseOpponent = new SalvoImpact();
		Map<String, String> salvo = new HashMap<String, String>();
		salvo.put("0x0", "miss");
		salvo.put("AxB", "hit");
		salvo.put("7x8", "miss");
		
		salvoResponseOpponent.setSalvo(salvo);
		SalvoImpact impactResult = game.captureSalvoImpactOnOpponent(salvoResponseOpponent);
		assertNotNull(impactResult);
		char[][] opponentBoard = game.getBoardOpponent();
		
		assertEquals(SalvoImpactType.MISS.getImpactValue(),opponentBoard[0][0]);
		assertEquals(SalvoImpactType.EMPTY.getImpactValue(),opponentBoard[1][1]);
		assertEquals(SalvoImpactType.HIT.getImpactValue(),opponentBoard[10][11]);
		assertEquals(SalvoImpactType.MISS.getImpactValue(),opponentBoard[7][8]);
	}

	@Test
	public void testGetSalvoSet(){
		char [][] boardSelf = new char[16][16];
		game.resetBoard(boardSelf, SalvoImpactType.EMPTY.getImpactValue());
		User self = game.getSelf();
		
		String opponentId="2";
		User opponent = new User();
		opponent.setUserId(opponentId);
		game = new SpaceshipGame(self, opponent);
		
		int count = 2;
		String[] salvo = game.getSalvoSet(count);
		assertEquals(count, salvo.length);
		log.debug(salvo.toString());
	}
	
	@Test
	public void testGetUniqueSalvoSet(){
		char [][] boardSelf = new char[16][16];
		game.resetBoard(boardSelf, SalvoImpactType.EMPTY.getImpactValue());
		User self = game.getSelf();
		
		String opponentId="2";
		User opponent = new User();
		opponent.setUserId(opponentId);
		game = new SpaceshipGame(self, opponent);
		
		// Large no so prob. of collision is high
		int largeNo = 255; 
		String[] salvo = game.getSalvoSet(largeNo);
		Set<String> setOfSalvo = new HashSet<String>();
		setOfSalvo.addAll(Arrays.asList(salvo));
		assertEquals(setOfSalvo.size(), salvo.length);
	}
	
	@Test
	public void testGetSalvoSetUsingStandardRule(){
		char [][] boardSelf = new char[16][16];
		game.resetBoard(boardSelf, SalvoImpactType.EMPTY.getImpactValue());
		User self = game.getSelf();
		
		String opponentId="2";
		User opponent = new User();
		opponent.setUserId(opponentId);
		game = new SpaceshipGame(self, opponent);
		game.setGameRules(GameRulesFactory.fetchStandardRule());
		
		int count = 2;
		game.setSelfOnBoardEntitiesAlive(count);
		String[] salvo = game.getSalvoSet();
		assertEquals(count, salvo.length);
		log.debug(salvo.toString());
	}
	
	@Test
	public void testUpdateAllSalvosAsMisses(){
		SpaceshipGame game = new SpaceshipGame();
		SalvoImpact salvoImpact = new SalvoImpact();
		String salvo[] = {"0x0","AxA"};
		salvoImpact = game.updateAllSalvosAsMisses(
				salvoImpact, salvo);
		
		Map<String, String> salvoImpacts = salvoImpact.getSalvo();

		assertNotNull(salvoImpact);
		assertEquals(salvo.length, salvoImpacts.size()); 
		for (String aSalvoImpact : salvoImpacts.values()) {
			assertEquals(SalvoImpactType.MISS.getImpactName(), aSalvoImpact);
		}

		
	}
	
}
