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
        //Para cada camino generado, los nodos tendran un peso,
        //basado en el nivel en que se encuentran 

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
    private HashMap<Pair<Integer, Integer>, mouseNode> maze;
    //Contiene los nodos conocidos del laberinto. 
    //Usa una posición (x,y) como clave.
    //Almacenada en un Pair de entero-entero

    private HashMap<Pair<Integer, Integer>, mouseNode> calculados;

    private Pair<Integer, Integer> borderMap;
    //Almacena los límites conocidos del mapa.
    //Se actualiza mediante la exploración y la
    //generación de Cheeese.

    private int tamMap;
    //Almacena el tamaño del mapa, basado en borderMap

    private Stack<Integer> camino;
    //Contiene los movimientos a realizar. Bien para llegar a un Cheese,
    //o para llegar a una casilla no explorada.   

    private List<mouseNode> noExploradasArea;
    //Contiene las casillas no exploradas que bordean 
    //la casilla donde está el Cheese.

    private int moveCount;  //Cuenta los movimientos. Se reinicia al colocar una bomba.
    private int bombsLeft;  //Cuenta las bombas que quedan por poner.

    //CONSTRUCTOR
    //===========
    public m14C02Demonos() {
        super("Demonophobia");

        moveCount = 0;
        bombsLeft = 5;
        camino = new Stack<>();
        noExploradasArea = new ArrayList<>();
        maze = new HashMap<>();
        calculados = new HashMap<>();
        borderMap = new Pair<>(5, 5);
        tamMap = borderMap.first * borderMap.second;
    }

    @Override
    public int move(Grid currentGrid, Cheese cheese) {
        //Creamos un Pair, con la posición actual y una referancia a un mouseNode
        Pair<Integer, Integer> currentPos = new Pair<>(currentGrid.getX(), currentGrid.getY());
        mouseNode currentNode;

        //Actualizamos los bordes del mapa, con la posición y las coordenadas del Cheese
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

        //Buscamos en maze la posición actual. Si está, currentNode será el nodo almacenado
        //en caso contrario, se crea un nuevo nodo y se almacena.
        if (maze.containsKey(currentPos)) {
            currentNode = maze.get(currentPos);
        } else {
            currentNode = new mouseNode(
                    currentPos,
                    currentGrid.canGoUp(), currentGrid.canGoDown(),
                    currentGrid.canGoLeft(), currentGrid.canGoRight()
            );

            maze.put(currentPos, currentNode);
        }

        //En caso de que nos encontremos en la casilla del cheese,
        //abandonamos la casilla y volvemos a ella
        if (cheese.getX() == currentNode.x && cheese.getY() == currentNode.y && camino.isEmpty()) {
            if (currentGrid.canGoUp()) {
                camino.add(Mouse.DOWN);
                camino.add(Mouse.UP);
            } else {
                if (currentGrid.canGoDown()) {
                    camino.add(Mouse.UP);
                    camino.add(Mouse.DOWN);
                } else {
                    if (currentGrid.canGoLeft()) {
                        camino.add(Mouse.RIGHT);
                        camino.add(Mouse.LEFT);
                    } else {
                        if (currentGrid.canGoRight()) {
                            camino.add(Mouse.LEFT);
                            camino.add(Mouse.RIGHT);
                        }
                    }
                }
            }
        }

        //Comprobamos si quedan bombas
        if (bombsLeft > 0) {
            int exitCount = 0;
            //Almacena la cantidad de direcciones por las que
            //se puede avanzar, desde el nodo actual.

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

            //Según el número de movimientos y el número de salidas, se decide
            //si colocar, o no, una bomba.
            if (moveCount > 30 && exitCount > 3) {
                moveCount = 0;
                bombsLeft--;
                return Mouse.BOMB;
            } else {
                if (moveCount > 100 && exitCount > 2) {
                    moveCount = 0;
                    bombsLeft--;
                    return Mouse.BOMB;
                } else {
                    moveCount++;
                }
            }
        }

        //Si no hay ningún camino, generamos uno.
        if (camino.isEmpty()) {
            Pair<Integer, Integer> target = new Pair<>(cheese.getX(), cheese.getY());

            if (noExploradasArea.isEmpty()) {
                getArea(target);
                //Obtenemos el area de casillas no exploradas
                //que rodean al Cheese.
            }

            getCamino(currentNode, target);
            //Obtenemos un camino al Cheese
            //o a una casilla no explorada.
        }

        return camino.pop();
    }

    @Override
    public void newCheese() {
        camino.clear();
        noExploradasArea.clear();
        calculados.clear();
    }

    @Override
    public void respawned() {
        camino.clear();
        noExploradasArea.clear();
        calculados.clear();
    }

    /**
     * Emplea una búsqueda en anchura, a partir de target, y almacena las
     * casillas no exploradas que lo rodean en noExploradasArea.
     *
     * @param target Posición objetivo.
     */
    private void getArea(Pair<Integer, Integer> target) {
        Queue<mouseNode> q = new LinkedList<>();
        List<mouseNode> visitados = new ArrayList<>();
        mouseNode w;

        w = new mouseNode(target);
        q.add(w);
        visitados.add(w);
        noExploradasArea.add(w);

        //Expandiremos nodos, hasta que todas las casillas que encontremos
        //sean casillas exploradas, o nos salgamos de los límites conocidos
        while (!q.isEmpty()) {
            mouseNode v = q.poll();
            mouseNode noExplorado;
            Pair<Integer, Integer> targetPos;

            //UP
            targetPos = v.getPos();
            if (v.y + 1 <= borderMap.second) {
                targetPos.second++;

                if (!maze.containsKey(targetPos)) {
                    noExplorado = new mouseNode(targetPos);
                    if (!visitados.contains(noExplorado)) {
                        noExploradasArea.add(noExplorado);
                        visitados.add(noExplorado);
                    }
                }
            }

            //DOWN
            targetPos = v.getPos();
            if (v.y - 1 >= 0) {
                targetPos.second++;

                if (!maze.containsKey(targetPos)) {
                    noExplorado = new mouseNode(targetPos);
                    if (!visitados.contains(noExplorado)) {
                        noExploradasArea.add(noExplorado);
                        visitados.add(noExplorado);
                    }
                }
            }

            //LEFT
            targetPos = v.getPos();
            if (v.x - 1 >= 0) {
                targetPos.first--;

                if (!maze.containsKey(targetPos)) {
                    noExplorado = new mouseNode(targetPos);
                    if (!visitados.contains(noExplorado)) {
                        noExploradasArea.add(noExplorado);
                        visitados.add(noExplorado);
                    }
                }
            }

            //RIGHT
            targetPos = v.getPos();
            if (v.x + 1 <= borderMap.first) {
                targetPos.first++;

                if (!maze.containsKey(targetPos)) {
                    noExplorado = new mouseNode(targetPos);
                    if (!visitados.contains(noExplorado)) {
                        noExploradasArea.add(noExplorado);
                        visitados.add(noExplorado);
                    }
                }
            }
        }
    }

    /**
     * Empleamos una búsqueda en anchura, para obtener el camino a target, o a
     * una casilla no explorada.
     *
     * @param rootNode Nodo inicial, del que parte la búsqueda
     * @param target Posición objetivo
     */
    private void getCamino(mouseNode rootNode, Pair<Integer, Integer> target) {
        List<mouseNode> noExploradas = new ArrayList<>();
        List<mouseNode> area = new ArrayList<>();
        //Almacena las casillas de noExploradasArea que son accesibles

        //Obtenemos un nuevo HasMap, que contiene el nodo anterior al dado.
        //De esta manera obtenemos el camino.
        HashMap<Pair<Integer, Integer>, mouseNode> anteriores = getAnteriores(rootNode, target, noExploradas, area);

        mouseNode targetNode;
        mouseNode w;

        //Comprobamos si tenemos camino directo al target, en caso contrario
        //elegimos una casilla no explorada de noExploradas o area.
        if (maze.containsKey(target) && anteriores.containsKey(target)) {
            targetNode = maze.get(target);
        } else {
            int i;
            if (!area.isEmpty()) {
                i = getMinIndex(area, target);
                targetNode = area.get(i);
            } else {
                i = getMinIndex(noExploradas, target);
                targetNode = noExploradas.get(i);
            }
        }

        //Finalmente obtenemos el camino al targetNode obtenido.
        w = anteriores.get(targetNode.getPos());
        camino.add(getDirection(w.getPos(), targetNode.getPos()));

        while (w != rootNode) {
            Pair<Integer, Integer> targetPos = w.getPos();
            w = anteriores.get(w.getPos());
            camino.add(getDirection(w.getPos(), targetPos));
        }
    }

    /**
     * Obtiene los predecesores de los nodos, para poder calcular el camino
     * posteriormente. Realiza una búsqueda en anchura.
     *
     * @param rootNode nodo inicial
     * @param target posición objetivo
     * @param noExploradas lista de nodos no explorados accesibles
     * @param area lista de nodos no explorados de noExploradasArea, accesibles
     * @return Devuelve un HashMap de Pair<Integer, Integer> y mouseNode. Este
     * contiene el nodo anterior a la posición pasada como clave.
     */
    private HashMap<Pair<Integer, Integer>, mouseNode> getAnteriores(mouseNode rootNode, Pair<Integer, Integer> target, List<mouseNode> noExploradas, List<mouseNode> area) {
        HashMap<Pair<Integer, Integer>, mouseNode> anteriores = new HashMap<>();

        Queue<mouseNode> q = new LinkedList<>();
        List<mouseNode> visitados = new ArrayList<>();

        q.add(rootNode);
        visitados.add(rootNode);
        calculados.put(rootNode.getPos(), rootNode);
        rootNode.distancia = 0;

        while (!q.isEmpty()) {
            mouseNode v = q.poll();

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
                    notExplored = new mouseNode(targetPos);
                    visitados.add(notExplored);
                    anteriores.put(notExplored.getPos(), v);
                    noExploradas.add(notExplored);
                    notExplored.distancia = v.distancia + 1;

                    if (noExploradasArea.contains(notExplored)) {
                        area.add(notExplored);
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
                    notExplored = new mouseNode(targetPos);
                    visitados.add(notExplored);
                    anteriores.put(notExplored.getPos(), v);
                    noExploradas.add(notExplored);
                    notExplored.distancia = v.distancia + 1;

                    if (noExploradasArea.contains(notExplored)) {
                        area.add(notExplored);
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
                    notExplored = new mouseNode(targetPos);
                    visitados.add(notExplored);
                    anteriores.put(notExplored.getPos(), v);
                    noExploradas.add(notExplored);
                    notExplored.distancia = v.distancia + 1;

                    if (noExploradasArea.contains(notExplored)) {
                        area.add(notExplored);
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
                    notExplored = new mouseNode(targetPos);
                    visitados.add(notExplored);
                    anteriores.put(notExplored.getPos(), v);
                    noExploradas.add(notExplored);
                    notExplored.distancia = v.distancia + 1;

                    if (noExploradasArea.contains(notExplored)) {
                        area.add(notExplored);
                    }
                }
            }
        }

        return anteriores;
    }

    /**
     * Dada una lista de nodos, emplea una función heurística para encontrar el
     * nodo con menor valor y devuelve su índice.
     *
     * @param nodes lista de nodos candidatos
     * @param target posición objetivo
     * @return Devuelve el índice de la lista nodes con menor valor.
     */
    private int getMinIndex(List<mouseNode> nodes, Pair<Integer, Integer> target) {
        double minValue = 9e99;
        int minPos = 0;

        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.get(i).getPos() == target) {
                return i;
            }

            double curValue = getValue(nodes.get(i), target);

            if (curValue < minValue) {
                minPos = i;
                minValue = curValue;
            }
        }

        return minPos;
    }

    /**
     * El nodo de entrada es evaluado, respecto a target, mediante una función
     * heurística y se devuelve el resultado
     *
     * @param init nodo a calcular
     * @param target posición objetivo
     * @return Valor de la función heurística.
     */
    private double getValue(mouseNode init, Pair<Integer, Integer> target) {

        double percentMapExplored = maze.size() / tamMap;
        double distQueso = Math.sqrt(
                (target.first - init.getPos().first) * (target.first - init.getPos().first)
                + (target.second - init.getPos().second) * (target.second - init.getPos().second)
        );
        int costeCasilla = init.distancia;

        if (costeCasilla <= 3) {
            distQueso = distQueso * 0.1 * costeCasilla;
        }

        return (1 - percentMapExplored) * distQueso * 2 + percentMapExplored * costeCasilla;
    }

    /**
     * Dadas dos posiciones, devuelve la dirección a seguir por el ratón para
     * llegar de una a otra.
     *
     * @param init posición inicial
     * @param target posición destino
     * @return Movimiento para ir de init a target.
     */
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
