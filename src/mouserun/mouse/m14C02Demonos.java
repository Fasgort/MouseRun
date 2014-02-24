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
            down = _down;
            left = _left;
            right = _right;
            explored = true;
        }

        public mouseNode(int _x, int _y) {
            x = _x;
            y = _y;
            explored = false;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof mouseNode)) {
                return false;
            }
            mouseNode node = (mouseNode) o;
            return x == node.x && y == node.y;
        }

        public int hashCode() {
            return x * 10000 + y;
        }

        public String toString() {
            return "X: " + x + " Y: " + y;
        }

    }

    private class Pair<A, B> {

        public A first;
        public B second;

        public Pair() {
        }

        public Pair(A _first, B _second) {
            first = _first;
            second = _second;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Pair)) {
                return false;
            }
            Pair key = (Pair) o;
            return first == key.first && second == key.second;
        }

        public int hashCode() {
            if (first instanceof Integer && second instanceof Integer) {
                Integer result = (Integer) first;
                Integer sec = (Integer) second;
                return result * 1000000 + sec;
            }

            return 0;
        }

        public String toString() {
            return "X: " + first + " Y: " + second;
        }
    }

    private HashMap< Pair<Integer, Integer>, mouseNode> maze;
    private Stack<mouseNode> camino;

    public m14C02Demonos() {
        super("Demonophobia");
        maze = new HashMap<>();
        camino = new Stack<>();
    }

    public int move(Grid currentGrid, Cheese cheese) {
        Pair<Integer, Integer> currentPos = new Pair<>(currentGrid.getX(), currentGrid.getY());
        mouseNode currentNode;

        if (maze.containsKey(currentPos)) {
            currentNode = maze.get(currentPos);
            if (!currentNode.explored) {
                currentNode.down = currentGrid.canGoDown();
                currentNode.up = currentGrid.canGoUp();
                currentNode.left = currentGrid.canGoLeft();
                currentNode.right = currentGrid.canGoRight();
            }
        } else {
            currentNode = new mouseNode(
                    currentGrid.getX(), currentGrid.getY(),
                    currentGrid.canGoUp(), currentGrid.canGoDown(),
                    currentGrid.canGoLeft(), currentGrid.canGoRight()
            );

            maze.put(currentPos, currentNode);
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

        if (camino.isEmpty()) {
            Pair<Integer, Integer> cheesePos = new Pair<>(cheese.getX(), cheese.getY());
            if (maze.containsKey(cheesePos)) //  if(false)
            {
                //busca camino

                Queue< mouseNode> q = new LinkedList<>();
                Stack< mouseNode> visitados = new Stack<>();

                q.add(currentNode);
                visitados.add(currentNode);
                HashMap< mouseNode, mouseNode> anterior = new HashMap<>();

                //System.out.println("Entra con: " + currentNode);
                while (!q.isEmpty()) {
                    mouseNode v = q.poll();
                    Pair<Integer, Integer> pos = new Pair<>(v.x, v.y);

                    if (v.x == cheese.getX() && v.y == cheese.getY()) {
                        System.out.println("DONE!");
                        camino.add(maze.get(cheesePos));

                        System.out.println("Anterior de " + v);
                        mouseNode e = anterior.get(v);

                        System.out.println("Anterior: " + e);
                        while (true) {
                            //   System.out.println("BUCLE: " + w.first);
                            if (e == currentNode) {
                                System.out.println("FIN BUCLE");
                                break;
                            }
                            camino.add(e);

                            if (e == null) {
                                camino.clear();
                                break;
                            }

                            System.out.println("GUARDADO: " + e);
                            e = anterior.get(e);
                        }

                        break;
                    }

                    mouseNode w;

                    //UP
                    if (v.up) {
                        if (maze.containsKey(new Pair<>(pos.first, pos.second + 1))) {
                            w = maze.get(new Pair<>(pos.first, pos.second + 1));
                            System.out.println("W: " + w);
                            if (!visitados.contains(w)) {
                                visitados.add(w);
                                q.add(w);
                                anterior.put(w, v);
                            }
                        }
                    }

                    //DOWN
                    if (v.down) {
                        if (maze.containsKey(new Pair<>(pos.first, pos.second - 1))) {
                            w = maze.get(new Pair<>(pos.first, pos.second - 1));
                            System.out.println("W: " + w);
                            if (!visitados.contains(w)) {
                                visitados.add(w);
                                q.add(w);
                                anterior.put(w, v);
                            }
                        }
                    }

                    //LEFT
                    if (v.left) {
                        if (maze.containsKey(new Pair<>(pos.first - 1, pos.second))) {
                            w = maze.get(new Pair<>(pos.first - 1, pos.second));
                            System.out.println("W: " + w);
                            if (!visitados.contains(w)) {
                                visitados.add(w);
                                q.add(w);
                                anterior.put(w, v);
                            }
                        }
                    }
                    //RIGHT
                    if (v.right) {
                        if (maze.containsKey(new Pair<>(pos.first + 1, pos.second))) {
                            w = maze.get(new Pair<>(pos.first + 1, pos.second));
                            if (!visitados.contains(w)) {
                                visitados.add(w);
                                q.add(w);
                                anterior.put(w, v);
                            }
                        }
                    }
                }
            }
        }

        System.out.println("Mi pos: " + currentPos);
        if (!camino.isEmpty()) // if(false)
        {
            mouseNode w = camino.pop();
            Pair<Integer, Integer> target = new Pair<>(w.x, w.y);

            if (target.second - 1 == currentPos.second) {
                return Mouse.UP;
            } else if (target.second + 1 == currentPos.second) {
                return Mouse.DOWN;
            } else if (target.first - 1 == currentPos.first) {
                return Mouse.RIGHT;
            } else {
                return Mouse.LEFT;
            }
        } else {
            return explorar(currentGrid, cheese);
        }
    }

    public void newCheese() {
        camino.clear();
    }

    public void respawned() {
        camino.clear();
    }

    private int explorar(Grid currentGrid, Cheese cheese) {
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
        ArrayList<Integer> possibleMoves = new ArrayList<>();

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
