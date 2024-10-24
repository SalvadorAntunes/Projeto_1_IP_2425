/**
 * @author Salvador Antunes 71352
 */

import java.util.Scanner;


// Constants representing the game outputs
final String GAME_OVER_MSG = "The game is over";
final String WIN_MSG = "You won the game!";
final String LOSE_MSG = "You lost the game!";
final String QUIT_MID_GAME = "The game was not over yet!";
final String QUIT_MSG = "Goodbye: ";
final String INVALID_COM_MSG = "Invalid command";
final String PLAYER_MOV = "Player: level %d, room %d, treasures %d\n";
final String ENEMY_L1_MOV = "Level 1 enemy: %s, room %d\n";
final String ENEMY_L2_MOV = "Level 2 enemy: %s, room %d\n";
final String ENEMY_Z_MSG = "zigzagger";
final String ENEMY_L_MSG = "looper";

//Constants representing the game inputs
final String QUIT_COM = "quit";
final String RIGHT_COM = "right";
final String LEFT_COM = "left";
final char ENTRY = 'P';
final char EXIT = 'E';
final char TREASURE = 'T';
final char STAIRS = 'S';
final char ENEMY_Z = 'Z';
final char ENEMY_L = 'L';

//Constants representing the game statistics
final int ENEMY_STATS = 3;
final int PLAYER_STATS = 2;
final int EXIT_STATS = 2;
final int ROOM = 0;
final int LEVEL = 1;
final int ENEMY_TYPE = 2;
final int LOOPER = 1;
final int ZIGZAGGER = 2;


//Global variables to control the game status
int totalTreasures;
int treasureCount;
char[] level1;
char[] level2;
int[] playerInfo;
int[] lvl1EnemyInfo;
int[] lvl2EnemyInfo;
int[] exitInfo;
int zigzaggerStepsL1;
int zigzaggerStepsL2;


//Initializes the game state by setting the dungeon layout, and the player's initial position.
void initState(char[] layoutDungeonLevel1, char[] layoutDungeonLevel2){
    level1 = layoutDungeonLevel1;
    level2 = layoutDungeonLevel2;
    playerInfo = initPlayerState();
    treasureCount = 0;
    totalTreasures = countTreasures();
    exitInfo = locateExit();
    lvl1EnemyInfo = buildEnemy(level1);
    lvl1EnemyInfo[LEVEL] = 1;
    lvl2EnemyInfo = buildEnemy(level2);
    lvl2EnemyInfo[LEVEL] = 2;
    zigzaggerStepsL1 = 1;
    zigzaggerStepsL2 = 1;
}

//Checks if the player has won the game by collecting all treasures and reaching the exit.
//@return if the player won.
boolean didThePlayerWin(){
    return treasureCount == totalTreasures &&
            playerInfo[ROOM] == exitInfo[ROOM] &&
            playerInfo[LEVEL] == exitInfo[LEVEL];
}

//Check if the player lost the game by going to the same room as the enemy in his level.
//@return if the player lost.
boolean didThePlayerLose(){
    return (playerInfo[LEVEL] == 1
            && lvl1EnemyInfo[ROOM] == playerInfo[ROOM])
            ||
            (playerInfo[LEVEL] == 2
                    && lvl2EnemyInfo[ROOM] == playerInfo[ROOM]);
}

//Checks if the game is already over either by winning or losing.
//@return if the player is already over.
boolean isTheGameOver(){
    return didThePlayerWin()
            ||
            didThePlayerLose();
}


//Processes the player's movements and updates the enemies' position
//and prints the game's current status.
//@param direction: direction where the player will move.
//@param steps: how many rooms the player will move in the wished direction.
void readPlayerMovement(String direction, int steps){
    updatePlayerPosition(direction, steps);
    updateEnemiesPosition();
    if (!isTheGameOver()){
        printGameStatus();
    }
}

//After receiving the input, updates the player position.
//@param direction: direction where the player will move.
//@param steps: how many rooms the player will move in the wished direction.
void updatePlayerPosition(String direction, int steps){
    if (direction.equals(RIGHT_COM))
        playerInfo[ROOM] += steps;
    else
        playerInfo[ROOM] -= steps;

    //Ensures player stays within room boundaries
    if (playerInfo[ROOM] < 1)
        playerInfo[ROOM] = 1;
    else if (playerInfo[LEVEL] == 1 && playerInfo[ROOM] > level1.length)
        playerInfo[ROOM] = level1.length;
    else if (playerInfo[LEVEL] == 2 && playerInfo[ROOM] > level2.length)
        playerInfo[ROOM] = level2.length;
    addTreasure(); //Checks if player collected treasure
    usingTheStairs(); //Checks if player used the stairs
}

//Locates the stairs and handles player movement between levels.
void usingTheStairs(){
    int stairsLevel1 = 0;
    int stairsLevel2 = 0;
    for (int i = 0; i < level1.length; i++)
        if (level1[i] == STAIRS)
            stairsLevel1 = i + 1;
    for (int i = 0; i < level2.length; i++)
        if (level2[i] == STAIRS)
            stairsLevel2 = i + 1;

    //Moves player between levels
    if (canThePlayerGoUpstairs(stairsLevel1)){
        playerInfo[LEVEL] = 2;
        playerInfo[ROOM] = stairsLevel2;
    }
    else if (canThePlayerGoDownStairs(stairsLevel2)) {
        playerInfo[LEVEL] = 1;
        playerInfo[ROOM] = stairsLevel1;
    }
}

//Checks if the player can go from level one to level two.
//@param stairs: room where the stairs are located in level one.
//@return if the player will move to level two.
boolean canThePlayerGoUpstairs(int stairs){
    return playerInfo[LEVEL] == 1 && playerInfo[ROOM] == stairs;
}

//Checks if the player can go from level two to level one.
//@param stairs: room where the stairs are located in level two.
//@return if the player will move to level one.
boolean canThePlayerGoDownStairs(int stairs){
    return playerInfo[LEVEL] == 2 && playerInfo[ROOM] == stairs;
}

//Adds a treasure to the player's collection if they are in a treasure room.
void addTreasure(){
    if (playerInfo[LEVEL] == 1) {
        for (int i = 0; i < level1.length; i++)
            if (level1[i] == TREASURE && i == playerInfo[ROOM] - 1){
                treasureCount++;
                level1[i] = '.'; //Removes treasure after collection
            }
    }
    else if (playerInfo[LEVEL] == 2) {
        for (int i = 0; i < level2.length; i++)
            if (level2[i] == TREASURE && i + 1 == playerInfo[ROOM]){
                treasureCount++;
                level2[i] = '.'; //Removes treasure after collection
            }
    }
}

//Identifies the enemies' and update their position in both levels.
void updateEnemiesPosition(){
    if (lvl1EnemyInfo[ENEMY_TYPE] == LOOPER)
        updateLooperPosition(lvl1EnemyInfo);
    else
        updateZigzaggerPosition(lvl1EnemyInfo);
    if (lvl2EnemyInfo[ENEMY_TYPE] == LOOPER)
        updateLooperPosition(lvl2EnemyInfo);
    else
        updateZigzaggerPosition(lvl2EnemyInfo);
}

//Updates the loppers' position.
//Looper moves one step to the right in each turn.
//@param enemyInfo: Contains every information about the enemy(room, level, and enemy type).
void updateLooperPosition(int[] enemyInfo){
    enemyInfo[ROOM] += 1;

    //If enemy moves beyond the last room, he goes to the first room
    if (enemyInfo[LEVEL] == 1 && enemyInfo[ROOM] > level1.length)
        enemyInfo[ROOM] = 1;
    else if (enemyInfo[LEVEL] == 2 && enemyInfo[ROOM] > level2.length)
        enemyInfo[ROOM] = 1;
}

//Updates the zigzaggers' position.
//Zigzagger moves in an increasing step pattern (1, 2, 3, 4, 5).
//@param enemyInfo: Contains every information about the enemy(room, level, and enemy type).
void updateZigzaggerPosition(int[] enemyInfo){
    if (enemyInfo[LEVEL] == 1) {
        enemyInfo[ROOM] += zigzaggerStepsL1;

        //If enemy moves beyond the last room, he goes to the first room
        while (enemyInfo[ROOM] > level1.length)
            enemyInfo[ROOM] = enemyInfo[ROOM] - level1.length;
        zigzaggerStepsL1++; //Increases step size for the next movement
        if (zigzaggerStepsL1 == 6)
            zigzaggerStepsL1 = 1; //Resets steps after reaching 5
    } else if (enemyInfo[LEVEL] == 2) {
        enemyInfo[ROOM] += zigzaggerStepsL2;

        //If enemy moves beyond the last room, he goes to the first room
        while (enemyInfo[ROOM] > level2.length)
            enemyInfo[ROOM] = enemyInfo[ROOM] - level2.length;
        zigzaggerStepsL2++; //Increases step size for the next movement
        if (zigzaggerStepsL2 == 6)
            zigzaggerStepsL2 = 1; //Reset steps after reaching 5
    }
}

//Initializes the player's state, including starting level and room.
//@return the array with the player's information.
int[] initPlayerState(){
    int[] initialPlayerStatus = new int[PLAYER_STATS];
    for (int i = 0; i < level1.length; i++){
        if (level1[i] == ENTRY){
            initialPlayerStatus[ROOM] = i + 1;
            initialPlayerStatus[LEVEL] = 1;
        }
    }
    for (int i = 0; i < level2.length; i++){
        if (level2[i] == ENTRY){
            initialPlayerStatus[ROOM] = i + 1;
            initialPlayerStatus[LEVEL] = 2;
        }
    }
    return initialPlayerStatus;
}

//Identifies the enemy and places him in his initial position.
//@param lvl: dungeon layout of the level the enemy's in.
//@return the array with the enemy's information.
int[] buildEnemy(char[] lvl){
    int[] enemyInfo = new int[ENEMY_STATS];
    for (int i = 0; i < lvl.length; i++) {
        if (lvl[i] == ENEMY_L) {
            enemyInfo[ENEMY_TYPE] = LOOPER;
            enemyInfo[ROOM] = i + 1;
        } else if (lvl[i] == ENEMY_Z) {
            enemyInfo[ENEMY_TYPE] = ZIGZAGGER;
            enemyInfo[ROOM] = i + 1;
        }
    }
    return enemyInfo;
}

//Locates the exit and returns its position and level.
//@return the array with the location of the exit.
int[] locateExit (){
    int[] exit = new int[EXIT_STATS];
    for (int i = 0; i < level1.length; i++){
        if (level1[i] == EXIT){
            exit[ROOM] = i + 1;
            exit[LEVEL] = 1;
        }
    }
    for (int i = 0; i < level2.length; i++){
        if (level2[i] == EXIT) {
            exit[ROOM] = i + 1;
            exit[LEVEL] = 2;
        }
    }
    return exit;
}

//Counts every treasure present in both levels.
//@return the total number of treasures in both levels of the dungeon.
int countTreasures (){
    int treasures = 0;
    for (int i = 0; i < level1.length; i++){
        if (level1[i] == TREASURE)
            treasures++;
    }
    for (int i = 0; i < level2.length; i++){
        if (level2[i] == TREASURE)
            treasures++;
    }
    return treasures;
}


//Handles the quit command and prints the final game result.
void quitGame(){
    if (isTheGameOver()) {
        if (didThePlayerLose())
            System.out.println(QUIT_MSG + LOSE_MSG);
        else if (didThePlayerWin())
            System.out.println(QUIT_MSG + WIN_MSG);
    }
    else
        System.out.println(QUIT_MID_GAME);
}

//Once the game is over, prints the game's result.
void printGameResult(){
    if (didThePlayerLose())
        System.out.println(LOSE_MSG);
    else if (didThePlayerWin())
        System.out.println(WIN_MSG);
}

//After every player movement, prints the current status of the game.
//The player's current position and how many treasure he already has
//and the two enemy's position.
void printGameStatus(){
    String lvl1Enemy;
    String lvl2Enemy;
    if (lvl1EnemyInfo[ENEMY_TYPE] == LOOPER)
        lvl1Enemy = ENEMY_L_MSG;
    else
        lvl1Enemy = ENEMY_Z_MSG;
    if (lvl2EnemyInfo[ENEMY_TYPE] == LOOPER)
        lvl2Enemy = ENEMY_L_MSG;
    else
        lvl2Enemy = ENEMY_Z_MSG;
    System.out.printf(PLAYER_MOV, playerInfo[LEVEL], playerInfo[ROOM], treasureCount);
    System.out.printf(ENEMY_L1_MOV, lvl1Enemy, lvl1EnemyInfo[ROOM]);
    System.out.printf(ENEMY_L2_MOV, lvl2Enemy, lvl2EnemyInfo[ROOM]);
}

//Checks if the input submitted is valid.
//@param command: input.
//@return if the command is valid.
boolean isTheCommandValid(String command){
    return command.equals(RIGHT_COM) || command.equals(LEFT_COM);
}

//Reads the input and separated the direction the amount of steps.
//@param in: Scanner.
void readInput(Scanner in) {
    String command = in.next();
    if (command.equals(QUIT_COM))
        quitGame();
    else if (isTheCommandValid(command)) {
        int steps = in.nextInt();
        if (!isTheGameOver()) {
            readPlayerMovement(command, steps); //Processes the players' movements
            if (isTheGameOver())
                printGameResult(); //Prints the final game result
            readInput(in); //Recursive call to continue reading movements
        } else if (isTheGameOver()){ //Reads inputs once the game is finished
            System.out.println(GAME_OVER_MSG);
            readInput(in); //Recursive call to continue reading commands
            // Keep reading inputs until it's the command to finish te program
        }
    }
    else {
        String rest = in.nextLine();
        System.out.println(INVALID_COM_MSG);
        readInput(in); //Recursive call to continue reading commands
    }
}

//Reads the dungeon layout from input.
//Converts the string layout into a character array representing the dungeon level.
//@param in: Scanner.
char[] readScenario(Scanner in){
    String layoutDungeonLevel = in.nextLine();
    return layoutDungeonLevel.toCharArray();
}

//Main method to start the game, initialize the state, and process player input.
void main(){
    Scanner in = new Scanner(System.in);

    //Reads dungeon layouts for both levels
    char[] layoutDungeonLevel2 = readScenario(in);
    char[] layoutDungeonLevel1 = readScenario(in);

    //Initializes game state with the dungeon layouts
    initState(layoutDungeonLevel1, layoutDungeonLevel2);

    //Starts reading and processing player movements
    readInput(in);

    in.close(); //Closes the scanner
}