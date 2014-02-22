package mouserun.mouse;

import mouserun.game.*;
import java.util.*;

public class m14C02Demonophobia extends Mouse {

    private int lastMove = 0;
    private int nextMove = 0;
    private int countMove = 0;
    private int bombsLeft = 5;
    private int distanciaX;
    private int direccionX;
    private int distanciaY;
    private int direccionY;
    

    public m14C02Demonophobia() {
        super("Demonophobia");
    }

    public int move(Grid currentGrid, Cheese cheese) {

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

 1.- Es posible obtener la altura y anchura del mapa, por lo que podemos crear una estructura de datos que consuma
 sólo la memoria mínima necesaria para guardar la totalidad del mapa. Los métodos Maze.getHeight() y Maze.getWidth() nos permite obtener
 estos datos.

 2.- La posición de las casillas se conoce mediante coordenadas "x" e "y", al igual que el queso, por lo que es posible realizar un algoritmo de búsqueda
 que trate de disminuir la distancia entre el queso y el ratón.




 */
