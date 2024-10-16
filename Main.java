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
final String LEFT_COM = "left";
final String RIGHT_COM = "right";
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
final char ENEMYZ = 'Z';
final char ENEMYL = 'L';
final int LOOPER = 1;
final int ZIGZAGGER = 2;
final int ENEMY_STATS = 3;
final int ENEMY_TYPE = 0;
final int ENEMY_ROOM = 1;
final int ENEMY_LEVEL = 2;
final int PLAYER_STATS = 2;
final int PLAYER_ROOM = 0;
final int PLAYER_LEVEL = 1;
final int EXIT_STATS = 2;
final int EXIT_ROOM = 0;
final int EXIT_LEVEL = 1;
final int ENEMYL_L_STEPS = 1;
final int[] ENEMY_Z_STEPS = {1,2,3,4,5};

int totalTreasures;
int treasureCount;
char[] level1;
char[] level2;
int[] playerInfo;
int[] lvl1EnemyInfo;
int[] lvl2EnemyInfo;
int[] exitInfo;
int zigzaggerSteps;

void initState(char[] lvl1, char[] lvl2){
    level1 = lvl1;
    level2 = lvl2;
    playerInfo = initPlayerState();
    treasureCount = 0;
    totalTreasures = countTreasures();
    exitInfo = locateExit();
    lvl1EnemyInfo = buildEnemy(level1);
    lvl1EnemyInfo[ENEMY_LEVEL] = 1;
    lvl2EnemyInfo = buildEnemy(level2);
    lvl2EnemyInfo[ENEMY_LEVEL] = 2;
    zigzaggerSteps = 0;
}

boolean didThePlayerWin(){
    return treasureCount == totalTreasures &&
            playerInfo[PLAYER_ROOM] == exitInfo[EXIT_ROOM] &&
            playerInfo[PLAYER_LEVEL] == exitInfo[EXIT_LEVEL];
}

boolean isTheGameOver(){
    return didThePlayerWin()
            ||
            (playerInfo[PLAYER_LEVEL] == 1
                    && lvl1EnemyInfo[ENEMY_ROOM] == playerInfo[PLAYER_ROOM])
            ||
            (playerInfo[PLAYER_LEVEL] == 2
                    && lvl2EnemyInfo[ENEMY_ROOM] == playerInfo[PLAYER_ROOM]);
}

void quitGame(){
    if (isTheGameOver()) {
        if (didThePlayerWin())
            System.out.println(QUIT_MSG + WIN_MSG);
        else
            System.out.println(QUIT_MSG + LOSE_MSG);
    }
    else{
        System.out.println(QUIT_MID_GAME);
    }
    System.exit(0);
}

void printGameResult(){
    if (didThePlayerWin())
        System.out.println(WIN_MSG);
    else
        System.out.println(LOSE_MSG);
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
            playerInfo[PLAYER_LEVEL], playerInfo[PLAYER_ROOM], treasureCount);
    System.out.println();
    System.out.printf(ENEMY_L1_MOV, lvl1Enemy, lvl1EnemyInfo[ENEMY_ROOM]);
    System.out.println();
    System.out.printf(ENEMY_L2_MOV, lvl2Enemy, lvl2EnemyInfo[ENEMY_ROOM]);
    System.out.println();
}

void updatePlayerPosition(String direction, int steps){
    if (direction.equals(RIGHT_COM))
        playerInfo[PLAYER_ROOM] += steps;
    else
        playerInfo[PLAYER_ROOM] -= steps;
    if (playerInfo[PLAYER_ROOM] < 0)
        playerInfo[PLAYER_ROOM] = 0;
    else if (playerInfo[PLAYER_LEVEL] == 1 && playerInfo[PLAYER_ROOM] > level1.length)
        playerInfo[PLAYER_ROOM] = level1.length;
    else if (playerInfo[PLAYER_LEVEL] == 2 && playerInfo[PLAYER_ROOM] > level2.length)
        playerInfo[PLAYER_ROOM] = level2.length;
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
        playerInfo[PLAYER_LEVEL] = 2;
        playerInfo[PLAYER_ROOM] = stairsLevel2;
    } else if (canThePlayerGoDownStairs(stairsLevel2)) {
        playerInfo[PLAYER_LEVEL] = 1;
        playerInfo[PLAYER_ROOM] = stairsLevel1;
    }
}

boolean canThePlayerGoUpstairs(int stairs){
    return playerInfo[PLAYER_LEVEL] == 1 && playerInfo[PLAYER_ROOM] == stairs;
}

boolean canThePlayerGoDownStairs(int stairs){
    return playerInfo[PLAYER_LEVEL] == 2 && playerInfo[PLAYER_ROOM] == stairs;
}

void addTreasure(){
    if (playerInfo[PLAYER_LEVEL] == 1) {
        for (int i = 0; i < level1.length; i++)
            if (level1[i] == TREASURE && i == playerInfo[PLAYER_ROOM] - 1)
                treasureCount++;
    }
    else if (playerInfo[PLAYER_LEVEL] == 2) {
        for (int i = 0; i < level2.length; i++)
            if (level2[i] == TREASURE && i + 1 == playerInfo[PLAYER_ROOM])
                treasureCount++;
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
    enemyInfo[ENEMY_ROOM] += ENEMYL_L_STEPS;
    if (enemyInfo[ENEMY_LEVEL] == 1 && enemyInfo[ENEMY_ROOM] > level1.length)
        enemyInfo[ENEMY_ROOM] = 1;
    if (enemyInfo[ENEMY_LEVEL] == 2 && enemyInfo[ENEMY_ROOM] > level2.length)
        enemyInfo[ENEMY_ROOM] = 1;
}

void updateZigzaggerPosition(int[] enemyInfo){
    enemyInfo[ENEMY_ROOM] += ENEMY_Z_STEPS[zigzaggerSteps];
    if (enemyInfo[ENEMY_LEVEL] == 1 && enemyInfo[ENEMY_ROOM] > level1.length)
        enemyInfo[ENEMY_ROOM] = enemyInfo[ENEMY_ROOM] - level1.length;
    if (enemyInfo[ENEMY_LEVEL] == 2 && enemyInfo[ENEMY_ROOM] > level2.length)
        enemyInfo[ENEMY_ROOM] = enemyInfo[ENEMY_ROOM] - level2.length;
    zigzaggerSteps++;
    if (zigzaggerSteps >= ENEMY_Z_STEPS.length)
        zigzaggerSteps = 0;
}

int[] initPlayerState(){
    int[] initialPlayerStatus = new int[PLAYER_STATS];
    for (int i = 0; i < level1.length; i++){
        if (level1[i] == ENTRY)
            initialPlayerStatus[PLAYER_ROOM] = i + 1;
    }
    initialPlayerStatus[PLAYER_LEVEL] = 1;
    return initialPlayerStatus;
}

int[] buildEnemy(char[] lvl){
    int[] enemyInfo = new int[ENEMY_STATS];
    for (int i = 0; i < lvl.length; i++) {
        if (lvl[i] == ENEMYL) {
            enemyInfo[ENEMY_TYPE] = LOOPER;
            enemyInfo[ENEMY_ROOM] = i + 1;
        } else if (lvl[i] == ENEMYZ) {
            enemyInfo[ENEMY_TYPE] = ZIGZAGGER;
            enemyInfo[ENEMY_ROOM] = i + 1;
        }
    }
    return enemyInfo;
}

int[] locateExit (){
    int[] exit = new int[EXIT_STATS];
    for (int i = 0; i < level1.length; i++){
        if (level1[i] == EXIT){
            exit[EXIT_ROOM] = i + 1;
            exit[EXIT_LEVEL] = 1;
        }
    }
    for (int i = 0; i < level2.length; i++){
        if (level2[i] == EXIT) {
        exit[EXIT_ROOM] = i + 1;
        exit[EXIT_LEVEL] = 2;
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

boolean isTheCommandValid(String com){
    return com.equals(RIGHT_COM) || com.equals(LEFT_COM);
}

void readPlayerMovement(Scanner in){
    String command = in.next();
    if (command.equals(QUIT_COM))
        quitGame();
    int steps = in.nextInt();
    in.nextLine();
    if (isTheCommandValid(command)){
        updatePlayerPosition(command, steps);
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

void readAfterTheGameFinished(Scanner in){
    String command = in.nextLine();
    if (command.equals(QUIT_COM))
        quitGame();
    else {
        System.out.println(GAME_OVER_MSG);
        readAfterTheGameFinished(in);
    }
}

char[] readScenario(Scanner in){
    String lvl = in.nextLine();
    return lvl.toCharArray();
}

void main(){
    Scanner in = new Scanner(System.in);
    char[] lvl1 = readScenario(in);
    char[] lvl2 = readScenario(in);
    initState(lvl2, lvl1);
    readPlayerMovement(in);
    printGameResult();
    readAfterTheGameFinished(in);
    in.close();
}