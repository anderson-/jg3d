package de.jg3d;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

import java.util.LinkedList;
import java.util.List;
import javax.swing.JApplet;
import javax.swing.event.MouseInputListener;

import de.jg3d.util.Importer;
import de.jg3d.util.TpsCounter;
import java.awt.BasicStroke;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;

public class Main extends JApplet implements Runnable, MouseInputListener, KeyListener, MouseWheelListener {

    Node hit = null;
    boolean shiftIsDown = false;
    boolean ctrlIsDown = false;
    private static final long serialVersionUID = 9101840683353633974L;

    private Thread thread;
    private BufferedImage bimg;

    public Graph graph;

    private TpsCounter tick;
    int gGridSize = 10;

    private Vector totalforce = new Vector();

    private boolean showNodes = true;
    private boolean showNodeNames = false;
    private boolean showNodePosition = false;
    private boolean showEdges = true;
    private boolean showEdgeNames = false;
    private boolean showHud = true;
    private boolean showEnergyStatistics = false;
    private boolean showHelp = false;
    private boolean showEdgeWeights = false;
    private boolean showNodeWeights = false;
    private boolean showEdgeLength = false;
    private boolean flatMode = false;
    private double pseudoZoom = 6;

    int gx, gy;

    ArrayList<Integer> test = new ArrayList<>();

    private int threads = 1;
    private ForceWorker[] forceWorkers = new ForceWorker[4];
    private Node WWW;

    public Graph getGraph() {
        return graph;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // System.out.println(e);
        switch (e.getKeyCode()) {
            case KeyEvent.VK_SHIFT:
                if (!shiftIsDown) {
                    shiftIsDown = true;
                }
                break;
            case KeyEvent.VK_CONTROL:
                if (!ctrlIsDown) {
                    ctrlIsDown = true;
                }
                break;
            case KeyEvent.VK_UP:
                gy -= 10;
                for (Node n : graph.getNodes()) {
//                    n.getPos().setY(n.getPos().getY() + -10);
//                    n.setSelfForceY(-.2);
                }
                break;
            case KeyEvent.VK_DOWN:
                gy += 10;
                for (Node n : graph.getNodes()) {
//                    n.getPos().setY(n.getPos().getY() + 10);
//                    n.setSelfForceY(.2);
                }
                break;
            case KeyEvent.VK_RIGHT:
                gx += 10;
                for (Node n : graph.getNodes()) {

//                    n.getPos().setX(n.getPos().getX() + 10);
//                    n.setSelfForceX(.2);
                }
                break;
            case KeyEvent.VK_LEFT:
                gx -= 10;
                for (Node n : graph.getNodes()) {
//                    n.getPos().setX(n.getPos().getX() + -10);
//                    n.setSelfForceX(-.2);
                }
                break;
            case KeyEvent.VK_PAGE_UP:
                for (Node n : graph.getNodes()) {
                    n.getPos().setZ(n.getPos().getZ() + 10);
//                    n.setSelfForceZ(.2);
                }
                break;
            case KeyEvent.VK_PAGE_DOWN:
                for (Node n : graph.getNodes()) {
                    n.getPos().setZ(n.getPos().getZ() + -10);
//                    n.setSelfForceZ(-.2);
                }
                break;
            case KeyEvent.VK_HOME:
                graph.getNode(0).setPos(new Vector(0, 0, 0));
                graph.getNode(0).setFixed(true);
                graph.getNode(0).setSelfForce(new Vector(0, 0, 0));
                break;
        }

        switch (e.getKeyChar()) {
            case '?':
                showHelp = !showHelp;
                break;
            case '1':
                threads = 1;
                break;
            case '2':
                threads = 2;
                break;
            case '3':
                threads = 3;
                break;
            case '4':
                threads = 4;
                break;
            case '7':
                generateGuide(new GuideGenerator() {
                    @Override
                    public boolean contains(int x, int y, int z) {
                        return x == 0;
                    }
                }, 5, gGridSize);
                break;
            case '8':
                generateGuide(new GuideGenerator() {
                    @Override
                    public boolean contains(int x, int y, int z) {
                        return y == 0;
                    }
                }, 5, gGridSize);
                break;
            case '9':
                generateGuide(new GuideGenerator() {
                    @Override
                    public boolean contains(int x, int y, int z) {
                        return z == 0;
                    }
                }, 5, gGridSize);
                break;
            case '0':
                createAxis(graph, 30);
                break;
            case '-': // pseudo-unzoom
                pseudoZoom = (pseudoZoom - 1 >= 1) ? pseudoZoom - 1 : pseudoZoom;
                for (Node n : graph.getNodes()) {
                    n.setDiameter(8 * pseudoZoom / 2);
                }
                break;
            case '=':
            case '+': // pseudo-zoom
                pseudoZoom = (pseudoZoom + 1 <= 30) ? pseudoZoom + 1 : pseudoZoom;
                for (Node n : graph.getNodes()) {
                    n.setDiameter(8 * pseudoZoom / 2);
                }
                break;
            case 'i': // invert all fixings
                for (Node n : graph.getNodes()) {
                    n.setFixed(!n.isFixed());
                }
                break;
            case 'f': // fix all nodes
                for (Node n : graph.getNodes()) {
                    n.setFixed(true);
                }
                break;
            case 'u': // unfix all nodes
                for (Node n : graph.getNodes()) {
                    n.setFixed(false);
                }
                break;
            case 'r': // reduce all edge weights
                for (Edge n : graph.getEdges()) {
                    n.setWeight(n.getWeight() - 0.5);
                }
                break;
            case 't': // enhance all edge weights
                for (Edge n : graph.getEdges()) {
                    n.setWeight(n.getWeight() + 0.5);
                }
                break;
            case 'h':
                showHud = !showHud;
                break;
            case 'E':
                showEnergyStatistics = !showEnergyStatistics;
                break;
            case 'n':
                showNodes = !showNodes;
                break;
            case 'l':
                showNodeNames = !showNodeNames;
                break;
            case 'P':
                showNodePosition = !showNodePosition;
                break;
            case 'm':
                showNodeWeights = !showNodeWeights;
                break;
            case 'e':
                showEdges = !showEdges;
                break;
            case 'b':
                showEdgeNames = !showEdgeNames;
                break;
            case 'd':
                showEdgeWeights = !showEdgeWeights;
                break;
            case 'L':
                showEdgeLength = !showEdgeLength;
                break;
            case 'F':
                flatMode = !flatMode;
                break;
            case 'q':
                for (Node n : graph.getNodes()) {
//                    if (!n.isFixed()) {
                    n.getPos().rotateX(0.05);
//                    }
                }
                test.add(1);
                break;
            case 'w':
                for (Node n : graph.getNodes()) {
//                    if (!n.isFixed()) {
                    n.getPos().rotateX(-0.05);
//                    }
                }
                test.add(2);
                break;
            case 'a':
                for (Node n : graph.getNodes()) {
//                    if (!n.isFixed()) {
                    n.getPos().rotateY(0.05);
//                    }
                }
                test.add(3);
                break;
            case 's':
                for (Node n : graph.getNodes()) {
//                    if (!n.isFixed()) {
                    n.getPos().rotateY(-0.05);
//                    }
                }
                test.add(4);
                break;
            case 'y':
                for (Node n : graph.getNodes()) {
//                    if (!n.isFixed()) {
                    n.getPos().rotateZ(0.05);
//                    }
                }
                test.add(5);
                break;
            case 'x':
                for (Node n : graph.getNodes()) {
//                    if (!n.isFixed()) {
                    n.getPos().rotateZ(-0.05);
//                    }
                }
                test.add(6);
                break;
            case '#':
                for (int i = test.size() - 1; i >= 0; i--) {
                    switch (test.get(i)) {
                        case 1:
                            for (Node n : graph.getNodes()) {
                                n.getPos().rotateX(-0.05);
                            }
                            break;
                        case 2:
                            for (Node n : graph.getNodes()) {
                                n.getPos().rotateX(0.05);
                            }
                            break;
                        case 3:
                            for (Node n : graph.getNodes()) {
                                n.getPos().rotateY(-0.05);
                            }
                            break;
                        case 4:
                            for (Node n : graph.getNodes()) {
                                n.getPos().rotateY(0.05);
                            }
                            break;
                        case 5:
                            for (Node n : graph.getNodes()) {
                                n.getPos().rotateZ(-0.05);
                            }
                            break;
                        case 6:
                            for (Node n : graph.getNodes()) {
                                n.getPos().rotateZ(0.05);
                            }
                            break;
                    }
                }
                test.clear();
                break;
            case '\\':
                if (WWW == null) {
                    WWW = graph.getNode(23);
                }
                runAlgo(graph, WWW);
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        for (Node n : graph.getNodes()) {
            n.setSelfForceX(0);
            n.setSelfForceY(0);
            n.setSelfForceZ(0);
        }
        if (e.getKeyCode() == KeyEvent.VK_SHIFT && shiftIsDown) {
            shiftIsDown = false;
            System.out.println("shift released");
        }
        if (e.getKeyCode() == KeyEvent.VK_CONTROL && ctrlIsDown) {
            ctrlIsDown = false;
            System.out.println("ctrl released");
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (!shiftIsDown) {
            hit.setPos(new Vector((e.getX() - 400) / pseudoZoom, (e.getY() - 300) / pseudoZoom, 0));
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (ctrlIsDown) {
            if (hit != null) {
                Node tmp = new Node(new Vector((e.getX() - 400) / pseudoZoom, (e.getY() - 300) / pseudoZoom, 0));
                if (hit.getPos().distance(tmp.getPos()) > 20) {//gridSize??
                    graph.addNode(tmp);
                    graph.connect(hit, tmp, 10);
                    hit = tmp;
                }
            } else {
                hit = graph.hit(e.getPoint());
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            if (shiftIsDown) {
                graph.remNode(graph.hit(e.getPoint()));
            } else {
                graph.addNode(new Node(new Vector((e.getX() - 400) / pseudoZoom, (e.getY() - 300) / pseudoZoom, 0)));
            }
        }
        if (e.getClickCount() == 3) {
            if (shiftIsDown) {
                graph.remNodeRec(graph.hit(e.getPoint()));
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        hit = graph.hit(e.getPoint());
        if (hit != null && !shiftIsDown) {
            hit.setFixed(true);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1 && !shiftIsDown) {
            {
                Node tmp = hit;
                hit = graph.hit(e.getPoint(), 30, tmp);
                if (tmp != null && hit != null && tmp != hit && hit.getType() == 2) {
                    tmp.getPos().setPos(hit.getPos().getX(), hit.getPos().getY(), hit.getPos().getZ());
                    tmp.setFixed(true);
                }
                if (tmp != null && hit != null && hit.getType() != 2) {
                    tmp.setFixed(false);
                }
            }
            if (hit != null && hit.getType() != 2) {
                hit.setFixed(false);
            }
            hit = null;
        } else if (e.getButton() == MouseEvent.BUTTON1 && shiftIsDown) {
            if (hit != null) {
                Node tmp = hit;
                hit = graph.hit(e.getPoint());
                if (tmp != hit) {
                    if (!tmp.connectedTo(hit)) {
                        graph.connect(tmp, hit, 10);
                    } else {
                        graph.disconnect(tmp, hit);
                    }
                }
                hit = tmp;
            } else {
                hit = null;
            }
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent we) {
        double wr = we.getWheelRotation() * .5;
        pseudoZoom = (pseudoZoom + wr > 0.2) ? pseudoZoom + wr : pseudoZoom;
        for (Node n : graph.getNodes()) {
            n.setDiameter(8 * pseudoZoom / 2);
        }
    }

    @Override
    public void init() {
        setBackground(Color.black);
        setFocusable(true); // VERY IMPORTANT for making the keylistener work on linux!!!!
        tick = new TpsCounter(100);

        graph = new Graph();

        //examples:
        // grid:
        // nodeGrid(graph,4,4,8,false,false);
        // nodeGrid(graph,7,7,8,false,false);
        // tube:
        // nodeGrid(graph,13,13,8,true,false);
        // torus:
        //nodeGrid(graph, 12, 18, 14, true, true);
    }

    public static void connectNodesToNearestNeighbours(Graph g, double radius) {
        for (Node n : g.getNodes()) {
            connectNodeToNearestNeighbours(g, n, radius);
        }
    }

    public static void connectNodeToNearestNeighbours(Graph g, Node n, double radius) {
        List<Node> inrange = g.findNodesInRange(n.getPos(), radius);
        for (Node m : inrange) {
            if (n != m) {
                g.connect(n, m, 5);
            }
        }
    }

    public static void connectNodeGrids(Graph g, List<List<Node>> a, List<List<Node>> b, double ew) {
        for (int i = 0; i < a.size(); i++) {
            for (int j = 0; j < a.get(0).size(); j++) {
                b.get(i).get(j).getPos().setPos(a.get(i).get(j).getPos().getX(),
                        a.get(i).get(j).getPos().getY() + 10, a.get(i).get(j).getPos().getZ());
                g.connect(a.get(i).get(j), b.get(i).get(j), ew);
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

    public void transform() {
        for (Node node : graph.getNodes()) {
            node.getPos().rotateX(0.008);
            node.getPos().rotateY(0.01);
            node.getPos().rotateZ(0.02);
        }
    }

    public void clear() {
        graph.clear();
    }

    public void project() {
        for (Node node : graph.getNodes()) {
            node.project(800, 600, pseudoZoom);
        }
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

    public static void createAxis(Graph graph, int size) {
        for (Node n : new ArrayList<Node>(graph.getNodes())) {
            if (n.getType() == 3) {
                graph.remNode(n);
            }
        }
        Node O = new Node(new Vector(0, 0, 0));
        O.setType(3);
        O.setDiameter(5);
        O.setFixed(true);
        O.setWeight(0);
        O.setColor(Color.gray);
        graph.addNode(O);

        Edge c[];

        java.awt.Stroke s = new BasicStroke(4);

        Node X = new Node(new Vector(size, 0, 0));
        X.setType(3);
        X.setDiameter(5);
        X.setFixed(true);
        X.setWeight(0);
        X.setColor(Color.red);
        graph.addNode(X);
        c = graph.connect(O, X, 0);
        c[0].setColor(Color.red);
        c[1].setColor(Color.red);
        c[0].setStroke(s);
        c[1].setStroke(s);

        Node Y = new Node(new Vector(0, size, 0));
        Y.setType(3);
        Y.setDiameter(5);
        Y.setFixed(true);
        Y.setWeight(0);
        Y.setColor(Color.green);
        graph.addNode(Y);
        c = graph.connect(O, Y, 0);
        c[0].setColor(Color.green);
        c[1].setColor(Color.green);
        c[0].setStroke(s);
        c[1].setStroke(s);

        Node Z = new Node(new Vector(0, 0, size));
        Z.setType(3);
        Z.setDiameter(5);
        Z.setFixed(true);
        Z.setWeight(0);
        Z.setColor(Color.blue);
        graph.addNode(Z);
        c = graph.connect(O, Z, 0);
        c[0].setColor(Color.blue);
        c[1].setColor(Color.blue);
        c[0].setStroke(s);
        c[1].setStroke(s);
    }

    public void generateGuide(GuideGenerator guideGen, int size, int d) {
        /*synchronized (graph)*/ {
            for (Node n : new ArrayList<Node>(graph.getNodes())) {
                if (n.getType() == 2) {
                    graph.remNode(n);
                }
            }
            for (int x = -size; x <= size; x++) {
                for (int y = -size; y <= size; y++) {
                    for (int z = -size; z <= size; z++) {
                        if (guideGen.contains(x, y, z)) {
                            Node node = new Node(new Vector(x * d, y * d, z * d));
                            node.setType(2);
                            node.setColor(Color.red);
                            graph.addNode(node);
                            node.setDiameter(5);
                            node.setFixed(true);
                            node.setWeight(0);
                        }
                    }
                }
            }
            for (Node n : new ArrayList<Node>(graph.getNodes())) {
                if (n.getType() == 2) {
                    for (int i : test) {
                        switch (i) {
                            case 1:
                                n.getPos().rotateX(0.05);
                                break;
                            case 2:
                                n.getPos().rotateX(-0.05);
                                break;
                            case 3:
                                n.getPos().rotateY(0.05);
                                break;
                            case 4:
                                n.getPos().rotateY(-0.05);
                                break;
                            case 5:
                                n.getPos().rotateZ(0.05);
                                break;
                            case 6:
                                n.getPos().rotateZ(-0.05);
                                break;
                        }
                    }
                }
            }

        }
    }

    Ellipse2D.Double e = new Ellipse2D.Double();

    public void drawScene(int w, int h, Graphics2D g2) {
        g2.translate(gx, gy);
        if (showEdges) {
            for (Node node : graph.getNodes()) {
                for (Edge edge : node.getAdjacencies()) {
                    g2.setColor(edge.getColor());
                    g2.setStroke(edge.getStroke());
                    g2.drawLine((int) node.getProjection().getX(), (int) node.getProjection()
                            .getY(), (int) edge.getDestination().getProjection().getX(), (int) edge
                            .getDestination().getProjection().getY());
//                    g2.drawLine((int) node.getProjection().getX()+2, (int) node.getProjection()
//                            .getY()+2, (int) edge.getDestination().getProjection().getX()+2, (int) edge
//                            .getDestination().getProjection().getY()+2);
//                    int x = (int)((node.getProjection().getX() + edge.getDestination().getProjection().getX())/2);
//                    int y = (int)((node.getProjection().getY() + edge.getDestination().getProjection().getY())/2);
//                            
//                    g2.drawRect(x-10,y-10,gGridSize,gGridSize);
                    if (showEdgeWeights) {
                        g2.drawString("" + edge.getWeight(), (int) (edge.getSource()
                                .getProjection().getX() + edge.getDestination().getProjection()
                                .getX()) / 2, (int) (edge.getSource().getProjection().getY() + edge
                                .getDestination().getProjection().getY()) / 2);
                    }
                    if (showEdgeLength) {
                        g2.drawString("" + (int) edge.getSource().getPos().distance(edge.getDestination().getPos()), (int) (edge.getSource()
                                .getProjection().getX() + edge.getDestination().getProjection()
                                .getX()) / 2, (int) (edge.getSource().getProjection().getY() + edge
                                .getDestination().getProjection().getY()) / 2);
                    }

                    if (showEdgeNames && edge.getLabel() != null) {
                        g2.drawString("" + edge.getLabel(), (int) (edge.getSource().getProjection()
                                .getX() + edge.getDestination().getProjection().getX()) / 2,
                                (int) (edge.getSource().getProjection().getY() + edge.getDestination()
                                .getProjection().getY()) / 2);
                    }
                }
            }
        }

        if (showNodes) {
            for (Node node : graph.getNodes()) {
//                if (node == hit) {
//                    node.setColor(Color.green);
//                } else if (node.getAdjacencies().size() <= 1) {
//                    node.setColor(Color.red);
//                } else if (node.getAdjacencies().size() <= 2) {
//                    node.setColor(Color.yellow);
//                } else if (node.getAdjacencies().size() <= 3) {
//                    node.setColor(Color.white);
//                } else if (node.getAdjacencies().size() <= 4) {
//                    node.setColor(Color.cyan);
//                } else if (node.getAdjacencies().size() <= 5) {
//                    node.setColor(Color.blue);
//                } else if (node.getAdjacencies().size() <= 6) {
//                    node.setColor(Color.magenta);
//                } else if (node.getAdjacencies().size() <= 7) {
//                    node.setColor(Color.pink);
//                }
                g2.setColor(node.getColor());
                if (node == hit) {
                    g2.setColor(Color.magenta);
                }

                e.setFrame(node.getProjection().getX() - node.getRadius(), node
                        .getProjection().getY()
                        - node.getRadius(), node.getDiameter(), node.getDiameter());

                g2.fill(e);
                if (showNodeWeights) {
                    g2.drawString("" + node.getWeight(), (int) node.getProjection().getX(),
                            (int) node.getProjection().getY());
                }
                if (showNodeNames) {
                    g2.drawString("" + node.getName(), (int) node.getProjection().getX(),
                            (int) node.getProjection().getY());
                }
                if (showNodePosition) {
                    g2.drawString("" + node.getPos(), (int) node.getProjection().getX(),
                            (int) node.getProjection().getY());
                }
                if (node.isFixed()) {
                    g2.setColor(new Color(0, 0, 250, node.getAlpha()));
//                    g2.draw(e);
                }
            }
        }

        g2.translate(-gx, -gy);
        if (showHud) {
            drawTextBlock(
                    g2,
                    "FPS : " + tick + ((threads > 1) ? " using " + threads + " threads" : "") + "\n"
                    + "Nodes : " + graph.getNodes().size() + "\n"
                    + "Edges : " + graph.getEdges().size() + "\n"
                    + "Force : " + totalforce + " (" + totalforce.sum() + ")" + "\n", 10, 10, Color.LIGHT_GRAY);
        }
        if (showEnergyStatistics) {
            drawTextBlock(
                    g2,
                    "Kinetic Energy : " + (int) graph.getKE() + "\n"
                    + "Potential Energy : " + (int) graph.getPE() + "\n"
                    + "Total Energy : " + (int) (graph.getPE() + graph.getKE()) + "\n", 10, 70, Color.LIGHT_GRAY);
        }
        if (showHelp) {
            drawTextBlock(g2, "Help:\n" + "Left mouse and drag:\n"
                    + "  move node near mouse (unfix)\n" + "Right mouse and drag:\n"
                    + "  move node near mouse and fix\n" + "Doubleclick:\n" + "  create new node\n"
                    + "Shift and doubleclick:\n" + "  delete node near mouse\n"
                    + "Drag and drop one node on another:\n" + "  connect them to each other\n"
                    + "Shift and drag/drop one node on another:\n" + "  disconnect them\n"
                    + "Ctrl and mouse-move\n" + "  create a line of nodes\n\n"
                    + "X-Rotation: q / w\n" + "Y-Rotation: a / s\n" + "Z-Rotation: y / x\n"
                    + "Pseudozoom: + / -\n" + "Toggle nodes: n\n" + "Toggle node names: l\n"
                    + "Toggle node weights: m\n" + "Toggle node positions: P\n"
                    + "Toggle edges: e\n" + "Toggle edges length: L\n" + "Toggle edge names: b\n"
                    + "Toggle edge weights: d\n" + "Toggle hud: h\n" + "Toogle help: ?\n"
                    + "Fix all nodes: f\n" + "Unfix all nodes: u\n" + "Invert node fixations: i\n"
                    + "Decrease edge weights: r\n" + "Enhance edge weights: t\n"
                    + "Toggle system energy statatistcs: E\n"
                    + "Toggle flat mode: F\n"
                    + "", 10, 100,
                    new Color(255, 0, 0, 127));
        }
    }

    private static void drawTextBlock(Graphics2D g2, String txt, int x, int y, Color c) {
        g2.setColor(c);
        String[] lines = txt.split("\n");
        for (String line : lines) {
            g2.drawString(line, x, y += 15);
        }
    }

    public Graphics2D createGraphics2D(int w, int h) {
        if (bimg == null || bimg.getWidth() != w || bimg.getHeight() != h) {
            bimg = (BufferedImage) createImage(w, h);
        }
        Graphics2D g2 = bimg.createGraphics();
        g2.setBackground(getBackground());
        g2.clearRect(0, 0, w, h);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        return g2;
    }

    @Override
    public synchronized void paint(Graphics g) {
        /*synchronized (graph)*/ {
            Dimension d = getSize();
            step();
            project();
            Graphics2D g2 = createGraphics2D(d.width, d.height);
            drawScene(d.width, d.height, g2);
            g2.dispose();
            g.drawImage(bimg, 0, 0, this);
        }
    }

    @Override
    public void start() {
        thread = new Thread(this);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }

    @Override
    public synchronized void stop() {
        thread = null;
    }

    @Override
    public void run() {
        Thread me = Thread.currentThread();
        while (thread == me) {
            repaint();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                break;
            }
        }
        thread = null;
    }

    public static Main createFrame() {
        final Main demo = new Main();
        demo.init();
        //Importer.importfile(demo.graph, argv[0]);
        //nodeGrid(demo.graph, 15, 14, 13, true, true);
        demo.addMouseListener(demo);
        demo.addMouseMotionListener(demo);
        demo.addKeyListener(demo);
        demo.addMouseWheelListener(demo);
        demo.start();
        return demo;
    }

    public static void main(String argv[]) {
        final Main demo = new Main();
        demo.init();
        if (argv.length > 0) { //if we got a file, let's try to load it
            Importer.importfile(demo.graph, argv[0]);
        } else { //or show a simple node-grid
            nodeGrid(demo.graph, 15, 14, 13, true, true);
        }
        Frame f = new Frame("jG3D (press ? for help)");
        f.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
                demo.start();
            }

            @Override
            public void windowIconified(WindowEvent e) {
                demo.stop();
            }
        });

        demo.addMouseListener(demo);
        demo.addMouseMotionListener(demo);
        demo.addKeyListener(demo);
        demo.addMouseWheelListener(demo);
        f.add(demo);
        f.pack();
        f.setSize(new Dimension(800, 600));
        f.setVisible(true);
        demo.start();

    }

    public void runAlgo(Graph graph, Node node) {
        if (!node.isFixed()) {
            node.setFixed(true);
            node.setColor(Color.orange);
        } else {
            node.setColor(Color.yellow);
        }

        Edge min = null;
        for (Edge e : node.getAdjacencies()) {
            if (min == null || e.getLength() < min.getLength()) {
                if (!e.getSource().isFixed() || !e.getDestination().isFixed()) {
                    min = e;
                }
            }
        }

        if (min != null) {
            Node n = min.getSource();
            if (n == node) {
                n = min.getDestination();
            }
            int[] p = getClosestLatticeNode((int) n.getPos().getX(), (int) n.getPos().getY(), (int) n.getPos().getZ(), gGridSize);
            n.setPos(new Vector(p[0], p[1], p[2]));
            n.setFixed(true);
            n.setColor(Color.pink);
            //non-static
            WWW = n;
        } else {
            WWW = graph.getRandomNode();
        }
    }

    public static int[] getClosestLatticeNode(int x, int y, int z, int d) {
        x = x/d * d;
        y = y/d * d;
        z = z/d * d;
        System.out.println(x + " " + y + " " + z);
        return new int[]{x, y, z};
    }

    public interface GuideGenerator {

        public boolean contains(int x, int y, int z);

    }

}
