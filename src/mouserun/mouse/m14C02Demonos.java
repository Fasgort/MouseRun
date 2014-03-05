package mouserun.mouse;

import mouserun.game.*;
import java.util.*;

public class m14C02Demonos extends Mouse {

    //CLASES ANIDADAS
    //=============== 

    /* 
     Las clases definidas sobrecargan los metodos equals y hashcode.   
     Esto es así dado que ambos son usados como claves en estructuras HashMap.
     De no sobrecargarse, no se comportarían adecuadamente.
     */
    /**
     * TDA que permite almacenar dos valores, del mismo o distinto tipo.
     *
     * @param <A> Tipo del atributo first
     * @param <B> Tipo del atributo second
     */
    private class Pair<A, B> {

        public A first;
        public B second;

        public Pair() {
        }

        public Pair(A _first, B _second) {
            first = _first;
            second = _second;
        }

        @Override
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

        @Override
        public int hashCode() {
            if (first instanceof Integer && second instanceof Integer) {
                Integer result = (Integer) first;
                Integer sec = (Integer) second;
                return result * 1000000 + sec;
            }

            return 0;
        }

        @Override
        public String toString() {
            return "X: " + first + " Y: " + second;
        }
    }

    /**
     * Almacena una posición(x,y) y las direcciones accesibles desde la misma.
     * Esto último, solo será válido si el nodo está marcado como explored.
     */
    private class mouseNode {

        public int x;
        public int y;

        public boolean up;
        public boolean down;
        public boolean left;
        public boolean right;

        public boolean explored;
        public int distancia;

        public mouseNode(int _x, int _y, boolean _up, boolean _down, boolean _left, boolean _right) {
            x = _x;
            y = _y;

            up = _up;
            down = _down;
            left = _left;
            right = _right;
            explored = true;
        }

        public mouseNode(Pair<Integer, Integer> pos, boolean _up, boolean _down, boolean _left, boolean _right) {
            this(pos.first, pos.second, _up, _down, _left, _right);
        }

        public mouseNode(int _x, int _y) {
            x = _x;
            y = _y;
            explored = false;
        }

        public mouseNode(Pair<Integer, Integer> pos) {
            this(pos.first, pos.second);
        }

        public Pair<Integer, Integer> getPos() {
            return new Pair(x, y);
        }

        @Override
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

        @Override
        public int hashCode() {
            return x * 10000 + y;
        }

        @Override
        public String toString() {
            return "X: " + x + " Y: " + y;
        }
    }

    //=============== 
    //FIN CLASES ANIDADAS
    private HashMap<Pair<Integer, Integer>, mouseNode> maze; //Contiene los nodos conocidos del laberinto. 
    //Usa una posición (x,y) como clave. Almacenada en un
    //Pair de entero-entero

    private HashMap<Pair<Integer, Integer>, mouseNode> calculados;

    private Pair<Integer, Integer> borderMap;
    private int tamMap = 25;
    private int numExpl = 0;

    private boolean esInaccesible;

    private Stack<Integer> camino;  //Contiene los movimientos a realizar. Bien para llegar a un Cheese,
    //o para llegar a una casilla no explorada.   

    private int moveCount;  //Cuenta los movimientos. Se reinicia al colocar una bomba.
    private int bombsLeft;  //Cuenta las bombas que quedan por poner.

    public m14C02Demonos() {
        super("Demonophobia");

        moveCount = 0;
        bombsLeft = 5;
        camino = new Stack();
        maze = new HashMap();
        calculados = new HashMap();
        borderMap = new Pair(5, 5);

    }

    @Override
    public int move(Grid currentGrid, Cheese cheese) {
        Pair<Integer, Integer> currentPos = new Pair(currentGrid.getX(), currentGrid.getY());
        mouseNode currentNode;
        if (cheese.getX() > borderMap.first) {
            borderMap.first = cheese.getX();
            tamMap = (borderMap.first + 1) * (borderMap.second + 1);
        }
        if (cheese.getY() > borderMap.second) {
            borderMap.second = cheese.getY();
            tamMap = (borderMap.first + 1) * (borderMap.second + 1);
        }
        if (currentPos.first > borderMap.first) {
            borderMap.first = currentPos.first;
            tamMap = (borderMap.first + 1) * (borderMap.second + 1);
        }
        if (currentPos.second > borderMap.second) {
            borderMap.second = currentPos.second;
            tamMap = (borderMap.first + 1) * (borderMap.second + 1);
        }

        System.out.println("X: " + borderMap.first + "; Y: " + borderMap.second + ";");
        System.out.println("Casillas totales: " + tamMap);
        System.out.println("Casillas exploradas: " + numExpl);

        if (maze.containsKey(currentPos)) {
            currentNode = maze.get(currentPos);
        } else {
            numExpl++;
            currentNode = new mouseNode(
                    currentPos,
                    currentGrid.canGoUp(), currentGrid.canGoDown(),
                    currentGrid.canGoLeft(), currentGrid.canGoRight()
            );

            maze.put(currentPos, currentNode);
        }

        if (bombsLeft > 0) {
            int exitCount = 0;
            if (currentNode.up) {
                exitCount++;
            }
            if (currentNode.down) {
                exitCount++;
            }
            if (currentNode.left) {
                exitCount++;
            }
            if (currentNode.right) {
                exitCount++;
            }

            if (moveCount > 30 && exitCount > 3) {
                moveCount = 0;
                return Mouse.BOMB;
            } else {
                moveCount++;
            }
        }

        if (camino.isEmpty()) {
            getCamino(currentNode, new Pair(cheese.getX(), cheese.getY()));
        }

        return camino.pop();
    }

    @Override
    public void newCheese() {
        camino.clear();
        calculados.clear();
        esInaccesible = false;
    }

    @Override
    public void respawned() {
        camino.clear();
        calculados.clear();
        esInaccesible = false;
    }

    private void getCamino(mouseNode rootNode, Pair<Integer, Integer> target) {
        List<mouseNode> noExploradas = new ArrayList();
        HashMap<Pair<Integer, Integer>, mouseNode> anteriores = getAnteriores(rootNode, target, noExploradas);

        mouseNode targetNode;
        mouseNode w;

        if (maze.containsKey(target) && anteriores.containsKey(target)) {
            targetNode = maze.get(target);
        } else {
            int i = getMinIndex(noExploradas, target);
            targetNode = noExploradas.get(i);
            esInaccesible = true;
        }

        Pair<Integer, Integer> targetPosCalc = targetNode.getPos();

        int countCalc = 0;

        while (countCalc < 4) {
            switch (countCalc) {
                case 0:
                    targetPosCalc.first++;
                    break;
                case 1:
                    targetPosCalc.first -= 2;
                    break;
                case 2:
                    targetPosCalc.first++;
                    targetPosCalc.second++;
                    break;
                case 3:
                    targetPosCalc.second -= 2;
                    break;
            }
            if (!calculados.containsKey(targetPosCalc) && maze.containsKey(targetPosCalc)) {
                if (maze.get(targetPosCalc).explored == true) {
                    esInaccesible = false;
                }
            }
            countCalc++;
        }

        w = anteriores.get(targetNode.getPos());
        camino.add(getDirection(w.getPos(), targetNode.getPos()));

        while (true) {
            if (w == rootNode) {
                break;
            }

            Pair<Integer, Integer> targetPos = w.getPos();
            w = anteriores.get(w.getPos());
            camino.add(getDirection(w.getPos(), targetPos));
        }
    }

    private HashMap<Pair<Integer, Integer>, mouseNode> getAnteriores(mouseNode rootNode, Pair<Integer, Integer> target, List<mouseNode> noExploradas) {
        HashMap<Pair<Integer, Integer>, mouseNode> anteriores = new HashMap();

        Queue<mouseNode> q = new LinkedList();
        List<mouseNode> visitados = new ArrayList();
        boolean targetExplored = maze.containsKey(target);
        boolean insertarNoExploradas = true;

        q.add(rootNode);
        visitados.add(rootNode);
        calculados.put(rootNode.getPos(), rootNode);
        rootNode.distancia = 0;

        while (!q.isEmpty()) {
            mouseNode v = q.poll();

            /*
             if (!noExploradas.isEmpty()) {
             insertarNoExploradas = false;
             if (!targetExplored || esInaccesible == true) {
             break;
             }
             }
             */
            
            if (v.getPos() == target) {
                break;
            }

            mouseNode w;
            mouseNode notExplored;
            Pair<Integer, Integer> targetPos;

            //UP
            if (v.up) {
                targetPos = v.getPos();
                targetPos.second += 1;

                if (maze.containsKey(targetPos)) {
                    w = maze.get(targetPos);

                    if (!visitados.contains(w)) {
                        visitados.add(w);
                        q.add(w);
                        calculados.put(w.getPos(), w);
                        anteriores.put(w.getPos(), v);
                        w.distancia = v.distancia + 1;
                    }
                } else {
                    if (insertarNoExploradas) {
                        notExplored = new mouseNode(targetPos);
                        visitados.add(notExplored);
                        anteriores.put(notExplored.getPos(), v);
                        noExploradas.add(notExplored);
                        notExplored.distancia = v.distancia + 1;
                    }
                }
            }

            //DOWN
            if (v.down) {
                targetPos = v.getPos();
                targetPos.second -= 1;

                if (maze.containsKey(targetPos)) {
                    w = maze.get(targetPos);

                    if (!visitados.contains(w)) {
                        visitados.add(w);
                        q.add(w);
                        calculados.put(w.getPos(), w);
                        anteriores.put(w.getPos(), v);
                        w.distancia = v.distancia + 1;
                    }
                } else {
                    if (insertarNoExploradas) {
                        notExplored = new mouseNode(targetPos);
                        visitados.add(notExplored);
                        anteriores.put(notExplored.getPos(), v);
                        noExploradas.add(notExplored);
                        notExplored.distancia = v.distancia + 1;
                    }
                }
            }

            //LEFT                  
            if (v.left) {
                targetPos = v.getPos();
                targetPos.first -= 1;

                if (maze.containsKey(targetPos)) {
                    w = maze.get(targetPos);

                    if (!visitados.contains(w)) {
                        visitados.add(w);
                        q.add(w);
                        calculados.put(w.getPos(), w);
                        anteriores.put(w.getPos(), v);
                        w.distancia = v.distancia + 1;
                    }
                } else {
                    if (insertarNoExploradas) {
                        notExplored = new mouseNode(targetPos);
                        visitados.add(notExplored);
                        anteriores.put(notExplored.getPos(), v);
                        noExploradas.add(notExplored);
                        notExplored.distancia = v.distancia + 1;
                    }
                }
            }
            //RIGHT                   
            if (v.right) {
                targetPos = v.getPos();
                targetPos.first += 1;

                if (maze.containsKey(targetPos)) {
                    w = maze.get(targetPos);

                    if (!visitados.contains(w)) {
                        visitados.add(w);
                        q.add(w);
                        calculados.put(w.getPos(), w);
                        anteriores.put(w.getPos(), v);
                        w.distancia = v.distancia + 1;
                    }
                } else {
                    if (insertarNoExploradas) {
                        notExplored = new mouseNode(targetPos);
                        visitados.add(notExplored);
                        anteriores.put(notExplored.getPos(), v);
                        noExploradas.add(notExplored);
                        notExplored.distancia = v.distancia + 1;
                    }
                }
            }
        }

        return anteriores;
    }

    private int getMinIndex(List<mouseNode> nodes, Pair<Integer, Integer> target) {
        double minValue = 9e99;
        int minPos = 0;

        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.get(i).getPos() == target) {
                return i;
            }

            double curValue = getValue(nodes.get(i).getPos(), target);

            if (curValue < minValue) {
                minPos = i;
                minValue = curValue;
            }
        }

        return minPos;
    }

    private double getValue(Pair<Integer, Integer> init, Pair<Integer, Integer> target) {

        double percentMapExplored = numExpl / tamMap;
        double distQueso = Math.sqrt(
         (target.first - init.first) * (target.first - init.first)
         + (target.second - init.second) * (target.second - init.second)
         );
        int costeCasilla = maze.get(init.hashCode()).distancia;

        return (1-percentMapExplored)*distQueso + percentMapExplored*costeCasilla;
    }

    private int getDirection(Pair<Integer, Integer> init, Pair<Integer, Integer> target) {
        if (target.second - 1 == init.second) {
            return Mouse.UP;
        } else if (target.second + 1 == init.second) {
            return Mouse.DOWN;
        } else if (target.first - 1 == init.first) {
            return Mouse.RIGHT;
        } else {
            return Mouse.LEFT;
        }
    }
}
