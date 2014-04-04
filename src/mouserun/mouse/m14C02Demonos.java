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
                return result * 1000 + sec;
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

    private Stack<Integer> camino;
    //Contiene los movimientos a realizar. Bien para llegar a un Cheese,
    //o para llegar a una casilla no explorada.

    private int moveCount;  //Cuenta los movimientos. Se reinicia al colocar una bomba.
    private int bombsLeft;  //Cuenta las bombas que quedan por poner.

    private boolean informativeSearch;

    //CONSTRUCTOR
    //===========
    public m14C02Demonos() {
        super("Demonophobia");

        moveCount = 0;
        bombsLeft = 5;
        informativeSearch = false;
        camino = new Stack<>();
        maze = new HashMap<>();
    }

    @Override
    public int move(Grid currentGrid, Cheese cheese) {
        //Creamos un Pair, con la posición actual y una referancia a un mouseNode
        Pair<Integer, Integer> currentPos = new Pair<>(currentGrid.getX(), currentGrid.getY());
        mouseNode currentNode;

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

            if (maze.containsKey(target)) {
                informativeSearch = true;  
                //Si sabemos donde está el objetivo,
                //usamos A* (puede fallar, esto se considerará
                //más adelante)
            } else {
                informativeSearch = false;
                //Exploramos con profundidadLimitada
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
    }

    @Override
    public void respawned() {
        camino.clear();
    }

    void getCamino(mouseNode rootNode, Pair<Integer, Integer> target) {
        List<Pair<Integer, mouseNode>> candidatos = new ArrayList<>(); //Guarda la profundidad del nodo y el nodo
        HashMap<Pair<Integer, Integer>, mouseNode> anteriores = new HashMap<>();
        mouseNode targetNode = null;

        //Llamadas a búsquedas
        if (informativeSearch) {            
            busquedaAStar(rootNode, target, anteriores);
            targetNode = maze.get(target); //El nodo objetivo es el mismo queso.
        } else {         
            //Comenzamos con límite 5. Si no hay casillas sin explorar,
            //incrementamos dicho límite en 5 unidades.
            
            int limite = 5;
            targetNode = null;
            while(targetNode == null) {
                targetNode = busquedaProfundidadLimitada(rootNode, target, anteriores, limite);
                limite += 5;
            }
        }
        
        //Si A* seleccionado, pero target inaccesible, empleamos la búsqueda de exploración.
        if (informativeSearch && !anteriores.containsKey(target)) {
            //Conocemos la posición del queso, pero es inaccesible.
            //El A* no llega a él.
            
            int limite = 5;
            targetNode = null;
            while(targetNode == null) {
                targetNode = busquedaProfundidadLimitada(rootNode, target, anteriores, limite);
                limite += 5;
            }
        }
        
        //Finalmente calculamos el camino al nodo objetivo          
        mouseNode curNode = anteriores.get(targetNode.getPos());       
        camino.add(getDirection(curNode.getPos(), targetNode.getPos()));
        
        while (curNode != rootNode) {           
            Pair<Integer, Integer> targetPos = curNode.getPos();
            curNode = anteriores.get(curNode.getPos());
            camino.add(getDirection(curNode.getPos(), targetPos));
        }
        
        
    }
    
    /**
     * Emplea la búsqueda A* para hallar un camino entre el nodo rootNode
     * y la posición target.
     * 
     * @param rootNode nodo inicial.
     * @param target posición objetivo.
     * @param anteriores almacena el nodo antecesor a una posición dada.
     */
    void busquedaAStar(mouseNode rootNode, Pair<Integer, Integer> target, HashMap<Pair<Integer, Integer>, mouseNode> anteriores) {
        List<Pair<Integer, mouseNode>> abiertos = new LinkedList<>();
        HashMap<Pair<Integer,Integer>, mouseNode> cerrados = new HashMap<>();
        
        abiertos.add( new Pair<>(0, rootNode));
        
        while(!abiertos.isEmpty()) {
            int min = 9999;   
            int minIndex = 0;
            
            for(int i = 0; i < abiertos.size(); i++){
                Pair<Integer, mouseNode> w = abiertos.get(i);
                if(w.second.getPos() == target){
                    minIndex = i;
                    break;
                }
                
                int curValue = w.first + distanciaManhattam(w.second.getPos(), target);
                if(curValue < min) {
                    min = curValue;
                    minIndex = i;
                }
            }
            
           Pair<Integer, mouseNode> v = abiertos.get(minIndex);
           abiertos.remove(v);
           cerrados.put(v.second.getPos(), v.second);
           int nivel = v.first + 1;
           
           if(v.second.x == target.first && v.second.y == target.second) {               
               break;
           }
           
           //DOWN
           if(v.second.down) {
               Pair<Integer, Integer> curPos = v.second.getPos();
               curPos.second--;
               
               if(maze.containsKey(curPos)) {
                   mouseNode w = maze.get(curPos);                       
                   Pair<Integer, mouseNode> insert = new Pair<>(nivel, w);
                    if(!cerrados.containsKey(insert.second.getPos())) {                        
                            abiertos.add(insert);
                            anteriores.put(w.getPos(), v.second);
                        }
               }
           }
           
           //LEFT
           if(v.second.left) {
               Pair<Integer, Integer> curPos = v.second.getPos();
               curPos.first--;
               
               if(maze.containsKey(curPos)) {
                   mouseNode w = maze.get(curPos);                       
                   Pair<Integer, mouseNode> insert = new Pair<>(nivel, w);
                    if(!cerrados.containsKey(insert.second.getPos())) {                        
                            abiertos.add(insert);
                            anteriores.put(w.getPos(), v.second);
                        }
               }
           }
           
           //RIGHT
           if(v.second.right) {
               Pair<Integer, Integer> curPos = v.second.getPos();
               curPos.first++;
               
               if(maze.containsKey(curPos)) {
                   mouseNode w = maze.get(curPos);                       
                   Pair<Integer, mouseNode> insert = new Pair<>(nivel, w);
                    if(!cerrados.containsKey(insert.second.getPos())) {                        
                            abiertos.add(insert);
                            anteriores.put(w.getPos(), v.second);
                        }
               }
           }
           
           //UP
           if(v.second.up) {
               Pair<Integer, Integer> curPos = v.second.getPos();
               curPos.second++;
               
               if(maze.containsKey(curPos)) {
                   mouseNode w = maze.get(curPos);                       
                   Pair<Integer, mouseNode> insert = new Pair<>(nivel, w);
                    if(!cerrados.containsKey(insert.second.getPos())) {                        
                            abiertos.add(insert);
                            anteriores.put(w.getPos(), v.second);
                        }
               }
           }
        }
    }

    /**
     * 
     * Realiza una búsqueda en profundidad limitada. Almacenara los antecesores
     * de cada nodo para poder calcular el camino y manejará una lista de nodos
     * candidatos, no explorados exclusivamente, que se emplearán en el
     * cálculo del nodo a devolver.
     * 
     * @param rootNode Nodo inicial.
     * @param target  Posición objetivo.
     * @param anteriores Almacena el nodo anterior de cada posición.
     * @param limite Limite de la busqueda
     * @return null si no hay casillas objetivo sino, mejor nodo candidato calculada.
     */
    mouseNode busquedaProfundidadLimitada(mouseNode rootNode, Pair<Integer, Integer> target, HashMap<Pair<Integer, Integer>, mouseNode> anteriores, int limite) {
        Stack<Pair<Integer,mouseNode>> abiertos = new Stack<>();        
        HashMap<Pair<Integer,Integer>, mouseNode> cerrados = new HashMap<>();
        List<Pair<Integer,mouseNode>> candidatos = new LinkedList<>();    
        
        abiertos.add(new Pair<>(0, rootNode));
       
        while(!abiertos.isEmpty()) {            
            Pair<Integer, mouseNode> v = abiertos.pop();
            cerrados.put(v.second.getPos(), v.second);
            
            int nivel = v.first + 1;
            
            if(v.second.x == target.first && v.second.y == target.second) {               
                candidatos.add(v);
                break;
           }
            
            if(v.second.explored) {
                //DOWN
                if(v.second.down) {
                    Pair<Integer, Integer> curPos = v.second.getPos();
                    curPos.second--;
                    
                    if(maze.containsKey(curPos)) {
                        mouseNode w = maze.get(curPos);                       
                        Pair<Integer, mouseNode> insert = new Pair<>(nivel, w);
                        if(nivel <= limite && !cerrados.containsKey(insert.second.getPos())) {                            
                            abiertos.add(insert);
                            anteriores.put(w.getPos(), v.second);
                        }
                    } else {
                        mouseNode w = new mouseNode(curPos);                        
                        Pair<Integer, mouseNode> insert = new Pair<>(nivel, w);
                        if(nivel <= limite && !cerrados.containsKey(insert.second.getPos())) {
                            abiertos.add(insert);
                            anteriores.put(w.getPos(), v.second);
                            candidatos.add(insert);
                        }
                    }
                }
                
                //LEFT
                if(v.second.left) {
                    Pair<Integer, Integer> curPos = v.second.getPos();
                    curPos.first--;
                    
                    if(maze.containsKey(curPos)) {
                        mouseNode w = maze.get(curPos);                       
                        Pair<Integer, mouseNode> insert = new Pair<>(nivel, w);
                        if(nivel <= limite && !cerrados.containsKey(insert.second.getPos())) {                            
                            abiertos.add(insert);
                            anteriores.put(w.getPos(), v.second);
                        }
                    } else {
                        mouseNode w = new mouseNode(curPos);                        
                        Pair<Integer, mouseNode> insert = new Pair<>(nivel, w);
                        if(nivel <= limite && !cerrados.containsKey(insert.second.getPos())) {
                            abiertos.add(insert);
                            anteriores.put(w.getPos(), v.second);
                            candidatos.add(insert);
                        }
                    }
                }
            }
            
            //RIGHT
            if(v.second.right) {
                    Pair<Integer, Integer> curPos = v.second.getPos();
                    curPos.first++;
                    
                    if(maze.containsKey(curPos)) {
                        mouseNode w = maze.get(curPos);                       
                        Pair<Integer, mouseNode> insert = new Pair<>(nivel, w);
                        if(nivel <= limite && !cerrados.containsKey(insert.second.getPos())) {                            
                            abiertos.add(insert);
                            anteriores.put(w.getPos(), v.second);
                        }
                    } else {
                        mouseNode w = new mouseNode(curPos);                        
                        Pair<Integer, mouseNode> insert = new Pair<>(nivel, w);
                        if(nivel <= limite && !cerrados.containsKey(insert.second.getPos())) {
                            abiertos.add(insert);
                            anteriores.put(w.getPos(), v.second);
                            candidatos.add(insert);
                        }
                    }
            }
            
            //UP
            if(v.second.up) {
                    Pair<Integer, Integer> curPos = v.second.getPos();
                    curPos.second++;
                    
                    if(maze.containsKey(curPos)) {
                        mouseNode w = maze.get(curPos);                       
                        Pair<Integer, mouseNode> insert = new Pair<>(nivel, w);
                        if(nivel <= limite && !cerrados.containsKey(insert.second.getPos())) {                            
                            abiertos.add(insert);
                            anteriores.put(w.getPos(), v.second);
                        }
                    } else {
                        mouseNode w = new mouseNode(curPos);                        
                        Pair<Integer, mouseNode> insert = new Pair<>(nivel, w);
                        if(nivel <= limite && !cerrados.containsKey(insert.second.getPos())) {
                            abiertos.add(insert);
                            anteriores.put(w.getPos(), v.second);
                            candidatos.add(insert);
                        }
                    }
            }
        }      
        
        int targetIndex = getMinIndex(candidatos, target, rootNode);   
        if(targetIndex == -1) return null;
        return candidatos.get(targetIndex).second;
    }

    int distanciaManhattam(Pair<Integer, Integer> init, Pair<Integer, Integer> target) {
        return (Math.abs(target.first - init.first)) + (Math.abs(target.second - init.second));
    }

    /**
     * Dada una lista de nodos, emplea una función heurística para encontrar el
     * nodo con menor valor y devuelve su índice.
     *
     * @param nodes lista de nodos candidatos
     * @param target posición objetivo
     * @return Devuelve el índice de la lista nodes con menor valor.
     */
    private int getMinIndex(List<Pair<Integer, mouseNode>> nodes, Pair<Integer, Integer> target, mouseNode init) {       
        if(nodes.isEmpty())
            return -1;
        
        int minValue = 99999;
        int minPos = 0;

        for (int i = 0; i < nodes.size(); i++) {             
            if(nodes.get(i).second == init) {                
                continue;
            }
            if (nodes.get(i).second.getPos() == target) {
                return i;
            }

            int curValue = getValue(nodes.get(i), target);            

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
    private int getValue(Pair<Integer, mouseNode> init, Pair<Integer, Integer> target) {

        int distTarget = distanciaManhattam(init.second.getPos(), target);
        int costeCasilla = init.first;

        return costeCasilla * 2 + distTarget;
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
