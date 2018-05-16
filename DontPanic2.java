import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class DontPanic2 {
    public static final int INT_TICS_BETWEEN_BOTS = 3;

    private static List<Rebro> ways;
    private static List<Elevator> elevator;
    private static List<Rebro> currentWay = new ArrayList<>();

    private static int[][] floorElevatorMap;
    private static int[][] grafMap;


    private static int cloneCounter;
    private static int additionalElevCounter;

    private static int exitFloor; // floor on which the exit is found
    private static int exitPos; // position of the exit on its floor

    private static void initGraf(int nbFloors, int width) {
        ways.add(new Rebro(0, 0, 0));
        int wId = 1;
        for (int i = 0; i < nbFloors - 1; i++) {
            for (int j = 0; j < width; j++) {
                if (floorElevatorMap[i][j] != -1) {

                    if (floorElevatorMap[i + 1][j] != -1 && elevator.get(floorElevatorMap[i + 1][j]).stationar) {
                        grafMap[(floorElevatorMap[i][j])][floorElevatorMap[i + 1][j]] = wId;
                        ways.add(new Rebro(wId, floorElevatorMap[i][j], floorElevatorMap[i + 1][j]));
                        wId++;

                    } else {
                        for (int k = j; k < width; k++) {
                            if (floorElevatorMap[i + 1][k] != -1) {
                                grafMap[(floorElevatorMap[i][j])][floorElevatorMap[i + 1][k]] = wId;
                                ways.add(new Rebro(wId, floorElevatorMap[i][j], floorElevatorMap[i + 1][k]));
                                wId++;
                                if (elevator.get(floorElevatorMap[i + 1][k]).stationar) {
                                    break;
                                }
                            }
                        }

                        for (int k = j; k > 0; k--) {
                            if (floorElevatorMap[i + 1][k] != -1) {
                                grafMap[(floorElevatorMap[i][j])][floorElevatorMap[i + 1][k]] = wId;
                                ways.add(new Rebro(wId, floorElevatorMap[i][j], floorElevatorMap[i + 1][k]));
                                wId++;
                                if (elevator.get(floorElevatorMap[i + 1][k]).stationar) {
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

    }


    private static void findPathAlgo(int nbRounds, int dir) {

        boolean EOS = false;
        boolean backFlag = false;

        int backRebroEndId = -1;
        int lastWayEndId = 0, startDir = dir, endDir = 0;
        int sumWayWeight = 0;
        int gotSomeWayFlag = 0;

        currentWay.add(0, ways.get(0));
        endDir = currentWay.get(0).getEndDirAndWightInTics(startDir);
        startDir = endDir;

        int k;
        while (!EOS) {
            lastWayEndId = currentWay.get(currentWay.size() - 1).endVerID;
            gotSomeWayFlag = 0;
            for (Elevator ele : elevator) {
                k = ele.id;
                if (grafMap[lastWayEndId][k] != -1 && !backFlag && (ele.stationar || additionalElevCounter > 0)) {
                    endDir = ways.get(grafMap[lastWayEndId][k]).getEndDirAndWightInTics(startDir);

                    if ((sumWayWeight + ways.get(grafMap[lastWayEndId][k]).getWeight(endDir)) <= nbRounds && cloneCounter != 0) {
                        currentWay.add(ways.get(grafMap[lastWayEndId][k]));
                        additionalElevCounter -= (ele.stationar ? 0 : 1);
                        startDir = endDir;
                        sumWayWeight += currentWay.get(currentWay.size() - 1).getWeight(startDir);
                        gotSomeWayFlag = 1;
                        if (elevator.get(k).floor == exitFloor && elevator.get(k).pos == exitPos) {
                            sumWayWeight += currentWay.get(currentWay.size() - 1).getWeight(startDir);
                            EOS = true;
                        }
                        break;
                    }
                }

                if (backRebroEndId == ele.id && backFlag) {
                    backFlag = false;
                }
            }

            if (gotSomeWayFlag == 0) {

                backRebroEndId = currentWay.get(currentWay.size() - 1).endVerID;
                additionalElevCounter += (elevator.get(backRebroEndId).stationar ? 0 : 1);
                backFlag = true;
                sumWayWeight -= currentWay.get(currentWay.size() - 1).getWeight(currentWay.get(currentWay.size() - 1).endCulcDir);
                currentWay.remove(currentWay.size() - 1);
                startDir = currentWay.get(currentWay.size() - 1).getEndDirAndWightInTics(currentWay.get(currentWay.size() - 1).startCulcDir);
            }
        }
    }


    public static void main(String args[]) {

        int ticsCounter = 0;
        int wayTimer = 1;
        String outMsg = "";
        boolean setElFlag = false;
        int iter = 1;

        Scanner in = new Scanner(System.in);
        int nbFloors = in.nextInt(); // number of floors
        nbFloors++;  //aditional floor for graf

        int width = in.nextInt(); // width of the area

        int nbRounds = in.nextInt(); // maximum number of rounds
        exitFloor = in.nextInt(); // floor on which the exit is found
        exitFloor++;
        exitPos = in.nextInt(); // position of the exit on its floor

        int nbTotalClones = in.nextInt(); // number of generated clones
        cloneCounter = nbTotalClones;

        int nbAdditionalElevators = in.nextInt(); // number of additional elevators that you can build
        additionalElevCounter = nbAdditionalElevators;

        int nbElevators = in.nextInt(); // number of elevators
        nbElevators += 2;  //start and end points as elevators

        elevator = new ArrayList<>();
        ways = new ArrayList<>();


        floorElevatorMap = new int[nbFloors][width];
        for (int i = 0; i < nbFloors; i++) {
            for (int j = 0; j < width; j++) {
                floorElevatorMap[i][j] = -1;
            }
        }

        elevator.add(new Elevator(0, 0, width, true)); //set start position late(at first tic)

        elevator.add(new Elevator(1, exitFloor, exitPos, true)); //exit position
        floorElevatorMap[exitFloor][exitPos] = 1;


        for (int i = 2; i < nbElevators; i++) {
            int elevatorFloor = in.nextInt(); // floor on which this elevator is found
            int elevatorPos = in.nextInt(); // position of the elevator on its floor

            elevator.add(new Elevator(i, elevatorFloor + 1, elevatorPos, true));
            floorElevatorMap[elevatorFloor + 1][elevatorPos] = i;
        }

        int ec = elevator.size() - 1; // aditional elevator id
        for (int i = 2; i < nbFloors; i++) {
            for (int j = 0; j < width; j++) {
                if (floorElevatorMap[i][j] != -1) {
                    for (int k = i; k >= 1; k--) {
                        if (floorElevatorMap[k][j] == -1) {
                            ec++;
                            elevator.add(new Elevator(ec, k, j, false));
                            floorElevatorMap[k][j] = ec;
                        }
                    }
                }
            }
        }


        grafMap = new int[ec + 1][ec + 1];
        for (int i = 0; i <= ec; i++) {
            for (int j = 0; j <= ec; j++) {
                grafMap[i][j] = -1;
            }
        }


        // game loop
        while (true) {

            ticsCounter++;
            wayTimer--;
            outMsg = "";

            int cloneFloor = in.nextInt(); // floor of the leading clone
            int clonePos = in.nextInt(); // position of the leading clone on its floor
            String direction = in.next(); // direction of the leading clone: LEFT or RIGHT
            int intDirection = direction.equals("LEFT") ? -1 : 1;


            if (ticsCounter == 1) {
                elevator.get(0).pos = clonePos;
                floorElevatorMap[0][clonePos] = 0;

                initGraf(nbFloors, width);
                findPathAlgo(nbRounds, intDirection);
            }


            if (wayTimer == 0 && iter < currentWay.size()) {
                if (currentWay.get(iter - 1).endCulcDir != currentWay.get(iter).endCulcDir) {
                    outMsg = "BLOCK";
                }

                if (setElFlag) {
                    outMsg = "ELEVATOR";
                    wayTimer += 4;
                    setElFlag = false;
                } else {
                    wayTimer = currentWay.get(iter).getWeight(currentWay.get(iter).endCulcDir);

                    if (!elevator.get(currentWay.get(iter).endVerID).stationar) {
                        wayTimer -= 4;
                        setElFlag = true;
                    }

                    if (wayTimer == 0) {
                        outMsg = "ELEVATOR";
                        wayTimer += 4;
                        setElFlag = false;
                    }

                    iter++;
                }
            } else {
                outMsg = "WAIT";
            }
            System.out.println(outMsg.equals("") ? "WAIT" : outMsg);
        }
    }

    private static class Elevator {
        int id;
        int floor;
        int pos;
        boolean stationar;

        public Elevator(int id, int floor, int pos, boolean stationar) {
            this.id = id;
            this.floor = floor;
            this.pos = pos;
            this.stationar = stationar;
        }
    }

    private static class Rebro {
        int id;
        int startVerID, endVerID;
        int leftDirStartWiegth = -1, rightDirStartWiegth = -1;
        int endCulcDir = 0;
        int startCulcDir = 0;

        public Rebro(int wId, int startVerID, int endVerID) {
            this.id = wId;
            this.startVerID = startVerID;
            this.endVerID = endVerID;
        }

        public int getEndDirAndWightInTics(int startDirection) {  //direction: 1 to rigth; -1 to left
            int w;
            startCulcDir = startDirection;
            int newDir = startDirection;

            w = startDirection * (elevator.get(endVerID).pos - elevator.get(startVerID).pos);            //pos delta between elevators;
            newDir *= (w >= 0 ? 1 : -1);

            if (w < 0) {
                w = -w + INT_TICS_BETWEEN_BOTS;                                                             //block leader;
                cloneCounter--;
            }

/*
            int firstP = Math.min(elevator.get(endVerID).pos, elevator.get(startVerID).pos);
            int secondP = Math.max(elevator.get(endVerID).pos, elevator.get(startVerID).pos);
            int floor = elevator.get(endVerID).floor;
            for (Elevator ele : elevator) {
                w += ((floor == ele.floor && ele.pos > firstP && ele.pos < secondP && ele.stationar) ? 100 : 0);
            }
*/


//            w += (floor > exitFloor ? 100 : 0);


            w += elevator.get(endVerID).stationar ? 0 : INT_TICS_BETWEEN_BOTS;                           //convert leader to elevator

            w += 1; // upway

            switch (newDir) {
                case 1:
                    rightDirStartWiegth = w;
                    break;
                case -1:
                    leftDirStartWiegth = w;
                    break;
                default:
            }

            endCulcDir = newDir;

            return newDir;
        }

        public int getWeight(int dir) {
            return dir == 1 ? rightDirStartWiegth : leftDirStartWiegth;
        }

    }

}