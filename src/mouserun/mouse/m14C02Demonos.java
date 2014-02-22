package mouserun.mouse;

import mouserun.game.*;
import java.util.*;

public class m14C02Demonos extends Mouse {

    private int lastMove = 0;
    private int nextMove = 0;
    private int countMove = 0;
    private int bombsLeft = 5;
    private int distanciaX;
    private int direccionX;
    private int distanciaY;
    private int direccionY;

    public class mouseNode {

        public int x;
        public int y;

        public boolean up;
        public boolean down;
        public boolean left;
        public boolean right;

        public mouseNode(int _x, int _y, boolean _up, boolean _down, boolean _left, boolean _right) {
            x = _x;
            y = _y;

            up = _up;
            down = _up;
            left = _left;
            right = _right;
        }
    }

    public class Pair<A, B> {

        public A first;
        public B second;

        public Pair(A _first, B _second) {
            first = _first;
            second = _second;
        }
    }

    private Map<Pair<Integer, Integer>, mouseNode> maze;

    public m14C02Demonos() {
        super("Demonophobia");
        maze = new HashMap<Pair<Integer, Integer>, mouseNode>();
    }

    public int move(Grid currentGrid, Cheese cheese) {

        Pair<Integer, Integer> currentPos = new Pair<Integer, Integer>(currentGrid.getX(), currentGrid.getY());
        mouseNode currentNode;

        if (!maze.containsKey(currentPos)) {

            currentNode = new mouseNode(currentGrid.getX(), currentGrid.getY(),
                    currentGrid.canGoUp(), currentGrid.canGoDown(),
                    currentGrid.canGoLeft(), currentGrid.canGoRight());
            maze.put(currentPos, currentNode);

        } else {
            currentNode = maze.get(currentPos);
        }

        if (bombsLeft != 0) {
            if (countMove >= 30) {
                countMove = 0;
                bombsLeft--;
                return Mouse.BOMB;
            }
            countMove++;
        }
        
        if(maze.containsKey(new Pair<Integer, Integer> (cheese.getX(), cheese.getY()))){
           //Algoritmo de busqueda del queso
            return 1;
        }
        else{
            //Algoritmo de exploración
            return 1;
        }
    }

    public void newCheese() {

        lastMove = 0;

    }

    public void respawned() {

        lastMove = 0;

    }

}

/* Cosas importantes:

 1.- La posición de las casillas se conoce mediante coordenadas "x" e "y", al igual que el queso, por lo que es posible realizar un algoritmo de búsqueda
 que trate de disminuir la distancia entre el queso y el ratón.

 */
