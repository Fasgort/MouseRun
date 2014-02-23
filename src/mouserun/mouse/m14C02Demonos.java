package mouserun.mouse;

import mouserun.game.*;
import java.util.*;

public class m14C02Demonos extends Mouse {

    private int countMove = 0;
    private int bombsLeft = 5;

    private int lastMove = 0;
    private int nextMove = 0;
    private int distanciaX;
    private int direccionX;
    private int distanciaY;
    private int direccionY;

    private class mouseNode {

        public int x;
        public int y;

        public boolean up;
        public boolean down;
        public boolean left;
        public boolean right;

        public boolean explored;

        public mouseNode(int _x, int _y, boolean _up, boolean _down, boolean _left, boolean _right) {
            x = _x;
            y = _y;

            up = _up;
            down = _up;
            left = _left;
            right = _right;
            explored = true;
        }

        public mouseNode(int _x, int _y) {
            x = _x;
            y = _y;
            explored = false;
        }
    }

    private class Pair<A, B>{

        public A first;
        public B second;

        public Pair(){}
        
        public Pair(A _first, B _second) 
        {
            first = _first;
            second = _second;
        } 
        
        public boolean equals(Object o) 
        {
            if (this == o) return true;
            if (!(o instanceof Pair)) return false;
            Pair key = (Pair) o;
            return first == key.first && second == key.second;
        }
        
        public int hashCode() 
        {
            if(first instanceof Integer && second instanceof Integer)
            {
                Integer result = (Integer) first;
                Integer sec = (Integer) second;
                return result * 1000000 + sec;
            }
            
            return 0;
        }
    }

    private Map<Pair<Integer, Integer>, mouseNode> maze;
    private List<Integer> camino;
    private int caminoIndex = 0;

    public m14C02Demonos() {
        super("Demonophobia");
        maze = new HashMap<Pair<Integer, Integer>, mouseNode>();
        camino = new ArrayList();
    }

    public int move(Grid currentGrid, Cheese cheese) {
        if (caminoIndex == camino.size()) {
            camino.clear();
            caminoIndex = 0;
        }

        Pair<Integer, Integer> currentPos = new Pair<Integer, Integer>(currentGrid.getX(), currentGrid.getY());
        mouseNode currentNode;

        
        if (!maze.containsKey(currentPos)) {
            currentNode = new mouseNode(
                    currentGrid.getX(), currentGrid.getY(),
                    currentGrid.canGoUp(), currentGrid.canGoDown(),
                    currentGrid.canGoLeft(), currentGrid.canGoRight()
            );
            maze.put(currentPos, currentNode);
        } else {
            currentNode = maze.get(currentPos);
            if (!currentNode.explored) {
                currentNode.down = currentGrid.canGoDown();
                currentNode.up = currentGrid.canGoUp();
                currentNode.left = currentGrid.canGoLeft();
                currentNode.right = currentGrid.canGoRight();
            }
        }

        if (bombsLeft != 0) {
            if (countMove >= 30) {
                countMove = 0;
                bombsLeft--;
                return Mouse.BOMB;
            } else {
                countMove++;
            }
        }

        if (camino.size() == 0) {
            Pair<Integer, Integer> cheesePos = new Pair<Integer, Integer>(cheese.getX(), cheese.getY());

            if (maze.containsKey(cheesePos)) {
                //busca camino
                List< Pair<mouseNode, Integer>> abiertos = new ArrayList<>();
                List< Pair<mouseNode, Integer>> cerrados = new ArrayList<>();
                Pair<Integer, Integer> initPos = new Pair<>(currentNode.x, currentNode.y);

                abiertos.add(new Pair<mouseNode, Integer>(currentNode, 0));

                while (true) {
                    if (abiertos.size() == 0) {
                        break;
                    }

                    int minIndex = getMinIndex(abiertos, initPos);
                    cerrados.add(abiertos.get(minIndex));
                    abiertos.clear();
                    mouseNode lastNode = cerrados.get(cerrados.size() - 1).first;

                    if (lastNode.x == cheesePos.first && lastNode.y == cheesePos.second) {
                        cerrados.remove(0);
                        for (int i = 0; i < cerrados.size(); i++) {
                            camino.add(cerrados.get(i).second);
                        }

                        break;
                    } else {
                        Pair<mouseNode, Integer> insert = new Pair<>();
                        Pair<Integer, Integer> lastPos = new Pair<>(lastNode.x, lastNode.y);

                        if (lastNode.down) {
                            insert.first = maze.get(new Pair<Integer, Integer>(lastPos.first, lastPos.second - 1));
                            insert.second = Mouse.DOWN;
                            abiertos.add(insert);
                        }
                        if (lastNode.up) {
                            insert.first = maze.get(new Pair<Integer, Integer>(lastPos.first, lastPos.second + 1));
                            insert.second = Mouse.UP;
                            abiertos.add(insert);
                        }
                        if (lastNode.left) {
                            insert.first = maze.get(new Pair<Integer, Integer>(lastPos.first - 1, lastPos.second));
                            insert.second = Mouse.LEFT;
                            abiertos.add(insert);
                        }
                        if (lastNode.right) {
                            insert.first = maze.get(new Pair<Integer, Integer>(lastPos.first + 1, lastPos.second));
                            insert.second = Mouse.RIGHT;
                            abiertos.add(insert);
                        }
                    }
                }
            } else {
                //explorar
                if (currentGrid.getX() - cheese.getX() >= 0) {
                    direccionX = 3;
                } else {
                    direccionX = 4;
                }
                distanciaX = Math.abs(currentGrid.getX() - cheese.getX());
                if (currentGrid.getY() - cheese.getY() >= 0) {
                    direccionY = 2;
                } else {
                    direccionY = 1;
                }
                distanciaY = Math.abs(currentGrid.getY() - cheese.getY());

                switch (lastMove) {
                    case 1:
                        lastMove = 2;
                        break;
                    case 2:
                        lastMove = 1;
                        break;
                    case 3:
                        lastMove = 4;
                        break;
                    case 4:
                        lastMove = 3;
                        break;
                }

                if (distanciaX >= distanciaY) {
                    switch (direccionX) {
                        case 3:
                            if (currentGrid.canGoLeft() && lastMove != 3) {
                                lastMove = 3;
                                return Mouse.LEFT;
                            }
                            break;
                        case 4:
                            if (currentGrid.canGoRight() && lastMove != 4) {
                                lastMove = 4;
                                return Mouse.RIGHT;
                            }
                            break;
                    }
                    switch (direccionY) {
                        case 1:
                            if (currentGrid.canGoUp() && lastMove != 1) {
                                lastMove = 1;
                                return Mouse.UP;
                            }
                            break;
                        case 2:
                            if (currentGrid.canGoDown() && lastMove != 2) {
                                lastMove = 2;
                                return Mouse.DOWN;
                            }
                            break;
                    }
                } else {
                    switch (direccionY) {
                        case 1:
                            if (currentGrid.canGoUp() && lastMove != 1) {
                                lastMove = 1;
                                return Mouse.UP;
                            }
                            break;
                        case 2:
                            if (currentGrid.canGoDown() && lastMove != 2) {
                                lastMove = 2;
                                return Mouse.DOWN;
                            }
                            break;
                    }
                    switch (direccionX) {
                        case 3:
                            if (currentGrid.canGoLeft() && lastMove != 3) {
                                lastMove = 3;
                                return Mouse.LEFT;
                            }
                            break;
                        case 4:
                            if (currentGrid.canGoRight() && lastMove != 4) {
                                lastMove = 4;
                                return Mouse.RIGHT;
                            }
                            break;
                    }
                }

                Random random = new Random();
                ArrayList<Integer> possibleMoves = new ArrayList<Integer>();

                if (currentGrid.canGoUp()) {
                    possibleMoves.add(Mouse.UP);
                }
                if (currentGrid.canGoDown()) {
                    possibleMoves.add(Mouse.DOWN);
                }
                if (currentGrid.canGoLeft()) {
                    possibleMoves.add(Mouse.LEFT);
                }
                if (currentGrid.canGoRight()) {
                    possibleMoves.add(Mouse.RIGHT);
                }

                if (possibleMoves.size() == 1) {
                    lastMove = possibleMoves.get(0);
                    return possibleMoves.get(0);
                } else {
                    do {
                        nextMove = possibleMoves.get(random.nextInt(possibleMoves.size()));
                    } while (nextMove == lastMove);
                    lastMove = nextMove;
                    return lastMove;
                }
            }
        }

        return camino.get(caminoIndex++);
    }

    public double getDistance(Pair<Integer, Integer> a, Pair<Integer, Integer> b) {
        return Math.sqrt(
                (b.first - a.first) * (b.first - a.first)
                + (b.second - a.second) * (b.second - a.second)
        );
    }

    public int getMinIndex(List< Pair<mouseNode, Integer>> abiertos, Pair<Integer, Integer> target) {
        double minDist = 9e99;
        int minIndex = 0;

        for (int i = 0; i < abiertos.size(); i++) {
            Pair<Integer, Integer> curPos = new Pair<>(abiertos.get(i).first.x, abiertos.get(i).first.y);
            double curDist = getDistance(curPos, target);

            if (curDist < minDist) {
                minDist = curDist;
                minIndex = i;
            }
        }

        return minIndex;
    }

    public void newCheese() {
        System.out.println("Cheese!");
        camino.clear();
        caminoIndex = 0;
    }

    public void respawned() {
        camino.clear();
        caminoIndex = 0;
    }
}
