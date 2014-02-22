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
    
    public class mouseNode
    {
        public int x;
        public int y;

        public boolean up;
        public boolean down;
        public boolean left;
        public boolean right;

        public mouseNode(int _x, int _y, boolean _up, boolean _down, boolean _left, boolean _right)
        {
            x = _x;
            y = _y;

            up = _up;
            down = _up;
            left = _left;
            right = _right;
        }
    }
    
    public class Pair <A, B> 
    {
        public A first;
        public B second;

        public Pair(A _first, B _second)
        {
            first = _first;
            second = _second;
        }
    }
    
    private Map<Pair<Integer, Integer>, mouseNode> maze;
    

    public m14C02Demonos()
    {
        super("Demonophobia");
        maze = new HashMap< Pair<Integer, Integer>, mouseNode >();
    }

    public int move(Grid currentGrid, Cheese cheese)
    {
        
        Pair<Integer, Integer> currentPos = new Pair<Integer, Integer> (currentGrid.getX(), currentGrid.getY());
        mouseNode currentNode;
       
        if(!maze.containsKey(currentPos))
        {
             currentNode = new mouseNode( 
                                           currentGrid.getX(), currentGrid.getY(), 
                                           currentGrid.canGoUp(), currentGrid.canGoDown(),
                                           currentGrid.canGoLeft(), currentGrid.canGoRight() 
                                         );
        
            maze.put(currentPos, currentNode);
        }
        else
        {
            currentNode = maze.get(currentPos);
        }
        
        if (countMove >= 30 && bombsLeft != 0) {
            countMove = 0;
            bombsLeft--;
            return Mouse.BOMB;
        }

        countMove++;
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
