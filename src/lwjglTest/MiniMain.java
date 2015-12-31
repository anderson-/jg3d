/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lwjglTest;

import de.jg3d.ForceWorker;
import de.jg3d.Graph;
import de.jg3d.Main;
import de.jg3d.Node;
import de.jg3d.Vector;
import de.jg3d.util.Importer;
import java.util.LinkedList;
import java.util.List;
import de.jg3d.util.TpsCounter;
import java.util.ArrayList;

public class MiniMain {

    public Graph graph;

    private TpsCounter tick;

    private Vector totalforce = new Vector();

    private boolean flatMode = false;

    ArrayList<Integer> test = new ArrayList<>();

    private int threads = 1;
    private ForceWorker[] forceWorkers = new ForceWorker[4];
    private Thread simThread;

    public Graph getGraph() {
        return graph;
    }

    public MiniMain(Graph graph) {
        this.graph = graph;
    }

    public boolean runSimulation() {
        return runSimulation(Long.MAX_VALUE, 0);
    }

    public boolean runSimulation(int sleep) {
        return runSimulation(Long.MAX_VALUE, sleep);
    }
    boolean kill = false;

    public boolean runSimulation(final long simulationTime, final int sleep) {
        tick = new TpsCounter(100);
        if (simThread != null && simThread.isAlive()) {
            return false;
        }
        simThread = new Thread("SimThread") {
            @Override
            public void run() {
                long timeLeft = simulationTime;
                long begin = System.currentTimeMillis();
                int ts = sleep;
                while (System.currentTimeMillis() - begin < timeLeft) {
                    if (kill) {
                        break;
                    }
                    synchronized (graph) {
                        step();
                        if (graph.getKE() < 5) {
//                            break;
                        } else {
                            ts = sleep;
                        }
                    }
                    if (kill) {
                        break;
                    }
                    try {
                        Thread.sleep(ts);
                    } catch (InterruptedException ex) {
                    }
                }
//                System.out.println("END SIM");
            }
        };
        simThread.start();
        return true;
    }

    public void kill() {
        kill = true;
    }

    public static void createGraphK(Graph g, int k) {
        for (int i = 0; i < k; i++) {
            Node n = new Node(new Vector(100.0));
            g.addNode(n);
            for (Node o : g.getNodes()) {
                if (n != o) {
                    g.connect(n, o, 10);
                }
            }
        }
    }

    public static List<List<Node>> nodeGrid(Graph g, int width, int height, double ew,
            boolean vconnect, boolean hconnect) {
        List<List<Node>> grid = new LinkedList<>();
        List<Node> a, b, first;
        grid.add(first = a = nodeLine(g, width, ew));
        for (int i = 1; i < height; i++) {
            if (hconnect) {
                g.connect(a.get(0), a.get(a.size() - 1), ew);
            }
            grid.add(b = nodeLine(g, width, ew));
            if (hconnect) {
                g.connect(b.get(0), b.get(a.size() - 1), ew);
            }
            for (int j = 0; j < a.size(); j++) {
                g.connect(a.get(j), b.get(j), ew);
            }
            a = b;
        }
        if (vconnect) {
            for (int j = 0; j < a.size(); j++) {
                g.connect(a.get(j), first.get(j), ew);
            }
        }
        return grid;
    }

    public static List<Node> nodeLine(Graph g, int cnt, double ew) {
        List<Node> nl = new LinkedList<>();
        Node a, b;
        a = new Node(new Vector(100.0));
        nl.add(a);
        g.addNode(a);
        for (; cnt > 1; cnt--) {
            b = new Node(new Vector(100.0));
            nl.add(b);
            g.addNode(b);
            g.connect(a, b, ew);
            a = b;
        }
        return nl;
    }

    public static void tree(Graph graph, Node node, int depth) {
        if (depth == 0) {
            return;
        }
        if (!graph.getNodes().contains(node)) {
            graph.addNode(node);
        }
        Node root = new Node(new Vector(500));
        Node leaf1 = new Node(new Vector(500));
        Node leaf2 = new Node(new Vector(500));
        Node leaf3 = new Node(new Vector(500));
        graph.addNode(root);
        graph.addNode(leaf1);
        graph.addNode(leaf2);
        graph.addNode(leaf3);
        graph.connect(root, leaf1, 3);
        graph.connect(root, leaf2, 3);
        graph.connect(root, leaf3, 3);
        graph.connect(node, root, 3);
        tree(graph, leaf1, depth - 1);
        tree(graph, leaf2, depth - 1);
        tree(graph, leaf3, depth - 1);
    }

    public void step() {
        tick.tick();

        Vector[] forces = new Vector[graph.getNodes().size()];
        for (int i = 0; i < forces.length; i++) {
            forces[i] = new Vector(0, 0, 0);
        }

        try {
            for (int i = 0; i < threads; i++) {
                forceWorkers[i] = new ForceWorker(graph, i, threads);
                forceWorkers[i].setForces(forces);
                forceWorkers[i].start();
            }

            for (int i = 0; i < threads; i++) {
                forceWorkers[i].join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (flatMode) {
            for (Node node : graph.getNodes()) {
                node.getPos().setZ(0);
            }
        }
        totalforce = graph.affectForces(forces);

        if (tick.get() > 7 && graph.getKE() < 1500) { //refine calculations
            threads = 1;
        }

    }

}
