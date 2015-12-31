package de.jg3d;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class Graph {

    private List<Node> nodes;
    private List<Edge> edges;

    public Graph() {
        nodes = new ArrayList<Node>();
        edges = new ArrayList<Edge>();
    }

    public Node addNode(Node node) {
        nodes.add(node);
        return nodes.get(nodes.size() - 1);
    }

    public void remNode(Node node) {
        for (Node n : nodes) {
            if (n.connectedTo(node)) {
                Edge e = n.getEdgeTo(node);
                disconnect(n, node);
                edges.remove(e);
            }
        }
        nodes.remove(node);
    }

    public void remNodeRec(final Node n) {
        /*
         * new Thread(new Runnable(){ Node nodenow = n; public void run(){
         * for(Edge e : nodenow.getAdjacencies())
         * try{remNode(nodenow=e.getDestination());}catch(Exception ex){} try{
         * Thread.sleep(50); }catch(Exception ex){} } }).start();
         */
    }

    public Node getRandomNode() {
        return nodes.get((int) (Math.random() * nodes.size()));
    }

    public Node getNode(int index) {
        return nodes.get(index);
    }

    public Node getNode(String name) {
        for (Node n : nodes) {
            if (n.getName().equals(name)) {
                return n;
            }
        }
        return null;
    }

    public double getKE() {
        double ke = 0;
        for (Node node : nodes) {
            ke += node.getKE();
        }
        return ke;
    }

    public double getPE() {
        double pe = 0;
        for (Node n1 : nodes) {
            pe += n1.attractiveForce().absoluteValue();
            for (Node n2 : nodes) {
                if (n1 != n2) {
                    pe += n1.repulsiveForce(n2).absoluteValue() / n1.getPos().distance(n2.getPos());
                }
            }
        }
        return pe;
    }

    public Edge directConnect(Node a, Node b, double weight) {
        Edge e = a.getEdgeTo(b);
        if (e == null) {
            e = b.getEdgeTo(a);
            if (e == null) {
                return connectTo(a, b, weight);
            }
        }
        return e;
    }

    public Edge connectTo(Node a, Node b, double weight) {
        if (a.connectedTo(b)) {
            Edge e = a.getEdgeTo(b);
//            System.out.println("ERROR : " + a + " already connected to " + b + " with weight "
//                    + e.getWeight());
            return e;
        } else {
            Edge e = null;
            a.getAdjacencies().add(e = new Edge(a, b, weight));
            edges.add(e);
            return e;
        }
    }

    public Edge[] connect(Node a, Node b, double weight) {
        Edge ea = connectTo(a, b, weight);
        Edge eb = connectTo(b, a, weight);
        return new Edge[]{ea, eb};
    }

    public boolean disconnectFrom(Node a, Node b) {
        if (a.connectedTo(b)) {
            Edge e = a.getEdgeTo(b);
            edges.remove(e);
            a.getAdjacencies().remove(e);
            System.out.println(a + " disconnected from " + b);
            return true;
        } else {
            System.out.println(a + " is not connected to " + b);
            return false;
        }
    }

    public boolean disconnect(Node a, Node b) {
        boolean ret = true;
        ret &= disconnectFrom(a, b);
        ret &= disconnectFrom(b, a);
        return ret;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Node node : nodes) {
            sb.append(node);
            for (Edge edge : node.getAdjacencies()) {
                sb.append(edge);
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public void calcForces(Vector[] forces, int start, int mod) { // actio == reactio
        Vector force;
        for (int i = start; i < forces.length; i += mod) {
            for (int j = i + 1; j < forces.length; j++) {
                force = nodes.get(i).repulsiveForce(nodes.get(j));
                forces[i] = forces[i].add(force);
                forces[j] = forces[j].add(force.multiply(-1));
            }
            forces[i] = forces[i].add(nodes.get(i).attractiveForce());
        }
    }

    public List<Node> findNodesInRange(Vector pos, double radius) {
        List<Node> inrange = new ArrayList<Node>();
        for (Node n : nodes) {
            if (pos.distance(n.getPos()) <= radius) {
                inrange.add(n);
            }
        }
        return inrange;
    }

    public Node hit(Point p) {
        Vector v = new Vector(p.getX(), p.getY(), 0);
        Node closestNode = null;
        double closestDistance = Double.MAX_VALUE;
        double distance;
        for (Node node : nodes) {
            if (node.getType() != 0) {
                continue;
            }
            distance = node.getProjection().distance(v);
            if (distance < closestDistance) {
                closestDistance = distance;
                closestNode = node;
            }
        }
        return closestNode;
    }

    public Node hit(Point p, double maxDistance, Node... ignore) {
        Vector v = new Vector(p.getX(), p.getY(), 0);
        Node closestNode = null;
        double closestDistance = Double.MAX_VALUE;
        double distance;
        hit:
        for (Node a : nodes) {
            for (Node i : ignore) {
                if (a == i) {
                    continue hit;
                }
            }
            distance = a.getProjection().distance(v);
            if (distance < closestDistance && distance < maxDistance) {
                closestDistance = distance;
                closestNode = a;
            }
        }
        return closestNode;
    }

    public Vector affectForces(Vector[] forces) {
        Vector totalforce = new Vector();
        for (int i = 0; i < nodes.size(); i++) {
            nodes.get(i).affect(forces[i]);
            totalforce = totalforce.add(forces[i].abs());
        }
        return totalforce;
    }

    public void clear() {
        nodes.clear();
        edges.clear();
    }

}
