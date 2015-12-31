package de.jg3d;

import static de.jg3d.Main.getClosestLatticeNode;
import java.awt.Color;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Node {

    private Vector position; // position
    private Vector projection; // projection (z=0)

    private double weight = 80; // ToDo
    private double diameter = 20; // todo

    private String name;
    private String label;
    private Color color;
    private List<Edge> adjacencies;
    private int type = 0;

    private static int instanceCounter = 0;

    private Vector velocity;
    private Vector selfforce;

    public static double maxAttraction = 100;
    public static double maxRepulsion = 100;

    private boolean fixed;

    public boolean isFixed() {
        return fixed;
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    private void init(Vector pos, String name, Color color) {
        this.position = pos;
        this.name = name;
        this.color = color;
        velocity = new Vector(0, 0, 0);
        selfforce = new Vector(0, 0, 0);
        adjacencies = new LinkedList<Edge>();
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double w) {
        this.weight = w;
    }

    public double getDiameter() {
        return diameter;
    }

    public void setDiameter(double diameter) {
        this.diameter = diameter;
    }

    public double getRadius() {
        return getDiameter() / 2;
    }

    public double getKE() {
        if (fixed){
            return 0;
        }
        return velocity.absoluteValue() * weight;
    }

    public Node() {
        instanceCounter++;
        init(new Vector(0, 0, 0), Integer.toString(instanceCounter), Color.cyan);
    }

    public Node(Vector p) {
        instanceCounter++;
        init(p, Integer.toString(instanceCounter), Color.cyan);
    }

    public Node(Vector p, String name) {
        instanceCounter++;
        init(p, name, Color.cyan);
    }

    public void setColor(Color c) {
        this.color = c;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getAlpha() {
        int alpha = (int) (127 - position.getZ());
        if (alpha < 5) {
            alpha = 5;
        } else if (alpha > 250) {
            alpha = 250;
        }
        alpha = (position.getZ() < -100) ? 0 : alpha;
        return alpha;
    }

    public Color getColor() {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), getAlpha());
    }

    public void setName(String n) {
        this.name = n;
    }

    public void setPos(Vector p) {
        this.position = p;
    }

    public Vector getPos() {
        return position;
    }

    public List<Edge> getAdjacencies() {
        return adjacencies;
    }

    public String getName() {
        return name;
    }

    public boolean connectedTo(Node node) {
        for (Edge edge : adjacencies) {
            if (edge.getDestination() == node) {
                return true;
            }
        }
        return false;
    }

    public Edge getEdgeTo(Node node) {
        for (Edge edge : adjacencies) {
            if (edge.getDestination() == node) {
                return edge;
            }
        }
        return null;
    }

    public void project(double canvasWidth, double canvasHeight, double pseudoZoom) {
        projection = position.get2D(canvasWidth, canvasHeight, pseudoZoom);
    }

    @Override
    public String toString() {
        return new StringBuilder().append('[').append(name).append(']').toString();
    }

    public Vector getProjection() {
        return projection;
    }

    // repulsive force to node
    // distance einbauen => wtf?
    public Vector repulsiveForce(Node node) {
        Vector force = position.add(node.getPos().invert()); // force = a - b
        // (abs(a-b) =
        // distance!!!!)
        force = force.multiply(1 / Math.pow(force.absoluteValue(), 3)); // normalize
        force = force.multiply(weight * node.getWeight()); // weighting

        if (force.absoluteValue() > maxRepulsion) { // reduce extraterrestrial
            // uberforces
//            System.out.println("repulsive uberforce: " + force.absoluteValue());
            force = force.multiply(maxRepulsion / force.absoluteValue());
        }

//        Vector forceK = position.add(node.getPos().invert()); // force = a - b
//        forceK = forceK.multiply(1 / Math.pow(forceK.absoluteValue(), 3)); // normalize
//        forceK = forceK.multiply((50 - position.add(node.getPos().invert()).absoluteValue()) * k);
//        force.add(forceK);
//        
//        int[] closestLatticeNode = getClosestLatticeNode(this, 50, 50);
//        int[] closestLatticeNode2 = getClosestLatticeNode(node, 50, 50);
//
//        if (Arrays.equals(closestLatticeNode, closestLatticeNode2)) {
//            Vector grid = new Vector(closestLatticeNode);
//
//            Vector n1 = grid.add(position.invert());
//            Vector n2 = grid.add(node.getPos().invert());
//
//            if (n1.absoluteValue() > n2.absoluteValue()) {
//                double d = n1.absoluteValue();
//                n1 = n1.multiply(1 / Math.pow(n1.absoluteValue(), 0.5)); // normalize
//                if (d > 0) {
//                    d = 1 / d;
//                } else {
//                    d = 100;
//                }
//                force.add(n1.multiply(G * d * 1000));
//            }
//        }

        return force;
    }
    double k = 2;
    public static double G = 0;

    // sum of attractive forces to all adjacencies
    public Vector attractiveForce() {
        Vector force = new Vector(0, 0, 0);
        Vector attraction;
        for (Edge edge : adjacencies) {
            attraction = position.add(edge.getDestination().getPos().invert()); // force
//            // =
//            // a
//            // -
//            // b
            attraction = attraction.multiply(1 / Math.pow(attraction.absoluteValue(), 0.5)); // normalize
            force = force.add(attraction.multiply(edge.getWeight()));

//            force = force.add(attraction.multiply(-(50 - position.add(edge.getDestination().getPos().invert()).absoluteValue()) * k));
        }

//        int[] closestLatticeNode = getClosestLatticeNode(this, 50, 50);
//
//        Vector attractionGrid = new Vector(closestLatticeNode).add(position.invert());
//        double d = attractionGrid.absoluteValue();
//        attractionGrid = attractionGrid.multiply(1 / Math.pow(attractionGrid.absoluteValue(), 0.5)); // normalize
//        if (d > 0) {
//            d = 1 / d;
//        } else {
//            d = 100;
//        }
//
//        force = force.add(attractionGrid.multiply(-G * d));

        // if (force.absoluteValue() > maxAttraction) { // reduce
        // extraterrestrial uberforces
        // System.out.println("attractive uberforce: "+force.absoluteValue());
        // force = force.multiply(maxAttraction / force.absoluteValue());
        // }
        return force.invert();
    }

    public void affect(Vector force) {
        if (!fixed) {
            velocity = velocity.add(force.multiply(1 / weight)); // inertia
            Vector friction = velocity.multiply(0.025); // 2.5% friction
            velocity = velocity.add(friction.invert());
            velocity = velocity.add(selfforce);
            position = position.add(velocity);
        }
    }

    public void alterSelfForceX(double d) {
        selfforce.setX(selfforce.getX() + d);
    }

    public void alterSelfForceY(double d) {
        selfforce.setY(selfforce.getY() + d);
    }

    public void alterSelfForceZ(double d) {
        selfforce.setZ(selfforce.getZ() + d);
    }

    public void setSelfForce(Vector v) {
        selfforce = v;
    }

    public void setSelfForceX(double d) {
        selfforce.setX(d);
    }

    public void setSelfForceY(double d) {
        selfforce.setY(d);
    }

    public void setSelfForceZ(double d) {
        selfforce.setZ(d);
    }

}
