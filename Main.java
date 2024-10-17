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

boolean didThePlayerWin(){
    return treasureCount == totalTreasures &&
            playerInfo[ROOM] == exitInfo[ROOM] &&
            playerInfo[LEVEL] == exitInfo[LEVEL];
}

boolean didThePlayerLose(){
    return (playerInfo[LEVEL] == 1
            && lvl1EnemyInfo[ROOM] == playerInfo[ROOM])
            ||
            (playerInfo[LEVEL] == 2
                    && lvl2EnemyInfo[ROOM] == playerInfo[ROOM]);
}

boolean isTheGameOver(){
    return didThePlayerWin()
            ||
            didThePlayerLose();
}

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

void printGameResult(){
    if (didThePlayerLose())
        System.out.println(LOSE_MSG);
    else if (didThePlayerWin())
        System.out.println(WIN_MSG);
}

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

boolean canThePlayerGoUpstairs(int stairs){
    return playerInfo[LEVEL] == 1 && playerInfo[ROOM] == stairs;
}

boolean canThePlayerGoDownStairs(int stairs){
    return playerInfo[LEVEL] == 2 && playerInfo[ROOM] == stairs;
}

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

void updateLooperPosition(int[] enemyInfo){
    enemyInfo[ROOM] += 1;
    if (enemyInfo[LEVEL] == 1 && enemyInfo[ROOM] > level1.length)
        enemyInfo[ROOM] = 1;
    if (enemyInfo[LEVEL] == 2 && enemyInfo[ROOM] > level2.length)
        enemyInfo[ROOM] = 1;
}

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