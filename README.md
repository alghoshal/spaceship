# Spaceship Game
A game of battle between fleets of spaceships owned by two (or more) opponents. 
The game is written in Java-8 using Spring Boot and other Spring components. 
All the game endpoints are accessible via REST Api's.

## Requirements & Environment
	Java-8, Maven, Spring Boot-1.3.8, Spring Cloud, Eureka, Service, Web, Actuator  

## To Build
	Download/ Clone all 3 projects: spaceship-core, spaceship-registry and spaceship-user. 
	
	To build go to the individual folders in a terminal/ IDE & run mvn install
	E.g. 
	cd spaceship-registry
	mvn install

	Build spaceship-core first since the other two project depend on it	

## To Run

	1. In one terminal/ IDE start the registry application from the spaceship-registry
	mvn spring-boot:run
	
	2.1. In a second terminal start the 1st player's instance (Defender)
	mvn spring-boot:run -Dserver.port=6000 -Dspring.application.instance_id=defender -Dmanagement.port=6001
	
	2.2 In a third terminal start the 2nd player's instance (Challenger)
	mvn spring-boot:run -Dserver.port=9000 -Dspring.application.instance_id=challenger -Dmanagement.port=9001
	
	& so on for the 3rd, 4th...Nth player's instances. 
	Just remember to assign unique server.port, management.port and instance_id values to each player
	
## Various Service Endpoints

	* [/spaceship/user/game/{gameId}/fire],methods=[PUT],consumes=[application/json],produces=[application/json]
	* [/spaceship/user/game/{gameId}],methods=[GET],produces=[application/json]
	* [/spaceship/user/game/{gameId}/auto],methods=[POST]
	* [/spaceship/user/game/new],methods=[POST],consumes=[application/json],produces=[text/html]
	* [/ping],methods=[GET]
	* [/ping-2],methods=[PUT]
	* [/spaceship/protocol/game/new],methods=[POST],consumes=[application/json],produces=[application/json]
	* [/spaceship/protocol/game/{gameId}],methods=[PUT],consumes=[application/json],produces=[application/json]
	* [/spaceship/protocol/game/{gameId}/fire-back],methods=[PUT],consumes=[application/json],produces=[application/json]

 ## Sample API calls from using curl
 
 		#Scenario-1:
		curl -i -H "Content-Type: application/json" -X POST -d '{"user_id": "gamer-1","full_name": "Gamer Opponent","spaceship_protocol": {"hostname": "127.0.0.1","port": 9001}}' http://localhost:9000/spaceship/protocol/game/new
		# Special char in name
		curl -i -H "Content-Type: application/json" -X POST -d '{"user_id": "gamer-1","full_name": "GäMer @pponent","spaceship_protocol": {"hostname": "127.0.0.1","port": 9001}}' http://localhost:9000/spaceship/protocol/game/new
		
		#Scenario-2:
		curl -i -H "Content-Type: application/json" -X PUT -d '{"salvo": ["0x0", "8x4", "DxA", "AxA", "7xF"]}' http://localhost:6000/spaceship/protocol/game/23
		
		#Scenario-3:
		curl -i -H "Content-Type: application/json" -X PUT -d '{"salvo": ["0x0", "8x4", "DxA", "AxA", "7xF"]}' http://localhost:6000/spaceship/user/game/33/fire
		
		#Scenario-4:
		curl -i  http://localhost:6000/spaceship/user/game/abf-c
		
		#Scenario-5:
		curl -i -H "Content-Type: application/json" -X POST http://localhost:6000/spaceship/user/game/lmn/auto
		curl -i -X POST http://localhost:6000/spaceship/user/game/lmn/auto
		
		#Scenario-7: (~Case 1)
		
		curl -i -H "Content-Type: application/json" -X POST -d '{"user_id": "gamer-2","full_name": "gamer Opponent","rules": "6-shot","spaceship_protocol": {"hostname": "127.0.0.1","port": 9000}}' http://localhost:6000/spaceship/user/game/new
		curl -i -H "Content-Type: application/json" -X POST -d '{"user_id": "gamer-2","full_name": "gamer Opponent","rules": "super-charge","spaceship_protocol": {"hostname": "127.0.0.1","port": 9000}}' http://localhost:6000/spaceship/user/game/new
		curl -i -H "Content-Type: application/json" -X POST -d '{"user_id": "gamer-2","full_name": "gamer Opponent","rules": "desperation","spaceship_protocol": {"hostname": "127.0.0.1","port": 9000}}' http://localhost:6000/spaceship/user/game/new
		
		#Scenario-8:
		curl -i -H "Content-Type: application/json" -X POST -d '{"user_id": "gamer-2","full_name": "gamer Opponent","spaceship_protocol": {"hostname": "127.0.0.1","port": 9000}}' http://localhost:6000/spaceship/user/game/new
		
		# Ping GET
		curl -i -H "Content-Type: application/json" -X GET http://localhost:9000/ping
		
		# Ping PUT
		curl -i -H "Content-Type: application/json" -X PUT -d '{"user_id": "x1"}' http://localhost:9000/ping-2
		
		#XML:
		curl -i -H "Content-Type: text/xml" -X PUT -d '<dd><salvo>hi</salvo></dd>' http://localhost:9000/spaceship/protocol/game/23
		# HTTP/1.1 415 Unsupported Media Type
		 	