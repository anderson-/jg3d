package de.jg3d;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;

public class Edge {

    public static final Stroke DEFAULT_STROKE = new BasicStroke(1);

    private double weight;
    private Node destination;
    private Node source;
    private Color color;
    private String label;
    private Stroke stroke = DEFAULT_STROKE;

    public Edge(Node source, Node destination, double weight) {
        this.destination = destination;
        this.source = source;
        this.weight = weight;
        this.color = Color.green;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public Node getDestination() {
        return destination;
    }

    public Node getSource() {
        return source;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double w) {
        this.weight = w;
    }

    public void setColor(Color c) {
        this.color = c;
    }

    public Stroke getStroke() {
        return stroke;
    }

    public void setStroke(Stroke stroke) {
        this.stroke = stroke;
    }

    public double getLength() {
        return source.getPos().distance(destination.getPos());
    }

    public int getAlpha() {
        double v = (source.getPos().getZ() + destination.getPos().getZ()) / 2;
        int alpha = (int) (127 - (v));
        if (alpha < 5) {
            alpha = 5;
        } else if (alpha > 250) {
            alpha = 250;
        }
        alpha = (v < -100) ? 0 : alpha;
        return alpha;
    }

    public Color getColor() {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), getAlpha());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(source.getName()).append("]--(").append(weight).append(")-->[")
                .append(destination.getName()).append(']');
        return sb.toString();
    }

}
