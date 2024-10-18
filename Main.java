/**
 * @author Salvador Antunes 71352
 */

import java.util.Scanner;

final String GAME_OVER_MSG = "The game is over";
final String WIN_MSG = "You won the game!";
final String LOSE_MSG = "You lost the game!";
final String QUIT_MID_GAME = "The game was not over yet!";
final String QUIT_MSG = "Goodbye: ";
final String INVALID_COM_MSG = "Invalid command";
final String QUIT_COM = "quit";
final String PLAYER_MOV = "Player: level %d, room %d, treasures %d";
final String ENEMY_L1_MOV = "Level 1 enemy: %s, room %d";
final String ENEMY_L2_MOV = "Level 2 enemy: %s, room %d";
final String ENEMY_Z_MSG = "zigzagger";
final String ENEMY_L_MSG = "looper";
final char ENTRY = 'P';
final char EXIT = 'E';
final char TREASURE = 'T';
final char STAIRS = 'S';
final char ENEMY_Z = 'Z';
final char ENEMY_L = 'L';
final int LOOPER = 1;
final int ZIGZAGGER = 2;
final int ENEMY_STATS = 3;
final int PLAYER_STATS = 2;
final int EXIT_STATS = 2;
final int ROOM = 0;
final int LEVEL = 1;
final int ENEMY_TYPE = 2;

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


//Game initial state.
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

//Checks if the player won the game by checking if he got all the treasures and if he's at the exit.
boolean didThePlayerWin(){
    return treasureCount == totalTreasures &&
            playerInfo[ROOM] == exitInfo[ROOM] &&
            playerInfo[LEVEL] == exitInfo[LEVEL];
}

//Check if the player lost the game by checking if he's in the same room as the enemy in his level.
boolean didThePlayerLose(){
    return (playerInfo[LEVEL] == 1
            && lvl1EnemyInfo[ROOM] == playerInfo[ROOM])
            ||
            (playerInfo[LEVEL] == 2
                    && lvl2EnemyInfo[ROOM] == playerInfo[ROOM]);
}

//Just checks if the game is already over.
boolean isTheGameOver(){
    return didThePlayerWin()
            ||
            didThePlayerLose();
}

//Finishes the game and prints the final game result.
void quitGame(){
    if (isTheGameOver()) {
        if (didThePlayerLose())
            System.out.println(QUIT_MSG + LOSE_MSG);
        else if (didThePlayerWin())
            System.out.println(QUIT_MSG + WIN_MSG);
    }
    else{
        System.out.println(QUIT_MID_GAME);
    }
    System.exit(0);
}

//Once the game is over, prints the game's result.
void printGameResult(){
    if (didThePlayerLose())
        System.out.println(LOSE_MSG);
    else if (didThePlayerWin())
        System.out.println(WIN_MSG);
}


//After every player movement, prints the current status of the game.
//It the player's current position and how many treasure he already has and the two enemy's position.
void printGameStatus(){
    String lvl1Enemy;
    String lvl2Enemy;
    if (lvl1EnemyInfo[ENEMY_TYPE] == 1)
        lvl1Enemy = ENEMY_L_MSG;
    else
        lvl1Enemy = ENEMY_Z_MSG;
    if (lvl2EnemyInfo[ENEMY_TYPE] == 1)
        lvl2Enemy = ENEMY_L_MSG;
    else
        lvl2Enemy = ENEMY_Z_MSG;
    System.out.printf(PLAYER_MOV,
            playerInfo[LEVEL], playerInfo[ROOM], treasureCount);
    System.out.println();
    System.out.printf(ENEMY_L1_MOV, lvl1Enemy, lvl1EnemyInfo[ROOM]);
    System.out.println();
    System.out.printf(ENEMY_L2_MOV, lvl2Enemy, lvl2EnemyInfo[ROOM]);
    System.out.println();
}

//After receiving the input, updates the player position and checks if he got another treasure or if used the stairs.
//@param direction: direction where the player will move.
//@param steps: how many rooms the player will move in the wished direction.
void updatePlayerPosition(String direction, int steps){
    if (direction.equals("right"))
        playerInfo[ROOM] += steps;
    else
        playerInfo[ROOM] -= steps;
    if (playerInfo[ROOM] < 1)
        playerInfo[ROOM] = 1;
    else if (playerInfo[LEVEL] == 1 && playerInfo[ROOM] > level1.length)
        playerInfo[ROOM] = level1.length;
    else if (playerInfo[LEVEL] == 2 && playerInfo[ROOM] > level2.length)
        playerInfo[ROOM] = level2.length;
    addTreasure();
    usingTheStairs();
}

//Makes the player use the stairs to go to another level.
void usingTheStairs(){
    int stairsLevel1 = 0;
    int stairsLevel2 = 0;
    for (int i = 0; i < level1.length; i++)
        if (level1[i] == STAIRS)
            stairsLevel1 = i + 1;
    for (int i = 0; i < level2.length; i++)
        if (level2[i] == STAIRS)
            stairsLevel2 = i + 1;
    if (canThePlayerGoUpstairs(stairsLevel1)){
        playerInfo[LEVEL] = 2;
        playerInfo[ROOM] = stairsLevel2;
    } else if (canThePlayerGoDownStairs(stairsLevel2)) {
        playerInfo[LEVEL] = 1;
        playerInfo[ROOM] = stairsLevel1;
    }
}

//Checks if can go from level one to level two.
//@param stairs: room where the stairs are located in level one.
boolean canThePlayerGoUpstairs(int stairs){
    return playerInfo[LEVEL] == 1 && playerInfo[ROOM] == stairs;
}

//Checks if can go from level two to level one.
//@param stairs: room where the stairs are located in level two.
boolean canThePlayerGoDownStairs(int stairs){
    return playerInfo[LEVEL] == 2 && playerInfo[ROOM] == stairs;
}

//Checks if the player is in the same room as a treasure.
//If so the treasure is added to the amount of treasures the players has collected.
//It also replaces the treasure with a blank space on the level so it cannot be collected again.
void addTreasure(){
    if (playerInfo[LEVEL] == 1) {
        for (int i = 0; i < level1.length; i++)
            if (level1[i] == TREASURE && i == playerInfo[ROOM] - 1){
                treasureCount++;
                level1[i] = '.';
            }
    }
    else if (playerInfo[LEVEL] == 2) {
        for (int i = 0; i < level2.length; i++)
            if (level2[i] == TREASURE && i + 1 == playerInfo[ROOM]){
                treasureCount++;
                level2[i] = '.';
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
//@param enemyInfo: Contains every information about the enemy(room, level, and enemy type).
void updateLooperPosition(int[] enemyInfo){
    enemyInfo[ROOM] += 1;
    if (enemyInfo[LEVEL] == 1 && enemyInfo[ROOM] > level1.length)
        enemyInfo[ROOM] = 1;
    if (enemyInfo[LEVEL] == 2 && enemyInfo[ROOM] > level2.length)
        enemyInfo[ROOM] = 1;
}

//Updates the zigzaggers' position updates the number of steps they will move next time the player moves.
//@param enemyInfo: Contains every information about the enemy(room, level, and enemy type).
void updateZigzaggerPosition(int[] enemyInfo){
    if (enemyInfo[LEVEL] == 1) {
        enemyInfo[ROOM] += zigzaggerStepsL1;
        while (enemyInfo[ROOM] > level1.length)
            enemyInfo[ROOM] = enemyInfo[ROOM] - level1.length;
        zigzaggerStepsL1++;
        if (zigzaggerStepsL1 == 6)
            zigzaggerStepsL1 = 1;
    }
    if (enemyInfo[LEVEL] == 2) {
        enemyInfo[ROOM] += zigzaggerStepsL2;
        while (enemyInfo[ROOM] > level2.length)
            enemyInfo[ROOM] = enemyInfo[ROOM] - level2.length;
        zigzaggerStepsL2++;
        if (zigzaggerStepsL2 == 6)
            zigzaggerStepsL2 = 1;
    }
}

//Places the player in its initial position and returns every stat about him (room, level).
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

//Locates the room and the level where the exit is.
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

//Counts every treasure in the entire dungeon.
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

//Checks if the input submitted is valid.
//@param command: input.
boolean isTheCommandValid(String command){
    String[] commandParts = command.split(" ");
    if (commandParts.length!=2)
        return false;
    String direction = commandParts[0];
    if (!direction.equals("right")&&!direction.equals("left"))
        return false;
    String stepsString = commandParts[1];
    for(char c : stepsString.toCharArray())
        if (!Character.isDigit(c))
            return false;
    return true;
}

//Reads the input and separated the direction the amount of steps.
//@param in: Scanner.
void readPlayerMovement(Scanner in){
    String command = in.nextLine();
    if (command.equals(QUIT_COM))
        quitGame();
    else {
        if (isTheCommandValid(command)){
        String[] commandParts = command.split(" ");
        String direction = commandParts[0];
        int steps = Integer.parseInt(commandParts[1]);
        updatePlayerPosition(direction, steps);
        updateEnemiesPosition();
            if (!isTheGameOver()){
                printGameStatus();
                readPlayerMovement(in);
            }
        }
        else {
            System.out.println(INVALID_COM_MSG);
            readPlayerMovement(in);
        }
    }
}

//Reads every input after the finished.
//@param in: Scanner.
void readAfterTheGameFinished(Scanner in){
    String command = in.nextLine();
    if (command.equals(QUIT_COM))
        quitGame();
    else {
        if (isTheCommandValid(command))
            System.out.println(GAME_OVER_MSG);
        else
            System.out.println(INVALID_COM_MSG);
        readAfterTheGameFinished(in);
    }
}

//Reads one of the levels of the dungeon layout
//@param in:Scanner
char[] readScenario(Scanner in){
    String layoutDungeonLevel = in.nextLine();
    return layoutDungeonLevel.toCharArray();
}

void main(){
    Scanner in = new Scanner(System.in);
    char[] layoutDungeonLevel2 = readScenario(in);
    char[] layoutDungeonLevel1 = readScenario(in);
    initState(layoutDungeonLevel1, layoutDungeonLevel2);
    readPlayerMovement(in);
    printGameResult();
    readAfterTheGameFinished(in);
    in.close();
}