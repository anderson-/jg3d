package de.jg3d.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import de.jg3d.Edge;
import de.jg3d.Graph;
import de.jg3d.Node;
import de.jg3d.Vector;
import java.io.FileNotFoundException;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Importer {
    /*
     * File format is like this:
     *   nodenameA;nodenameB
     *   nodenameA;nodenameC
     *   nodenameB;nodenameC;optional edge-label
     *   nodenameC;nodenameD
     **/

    public static void importContent(Graph g, BufferedReader in) {
        try {
            String str;
            while ((str = in.readLine()) != null) {
                String[] tmp = str.split(";");
                Node n = g.getNode(tmp[0].trim());
                if (n == null) {
                    n = new Node(new Vector(100));
                    n.setName(tmp[0].trim());
                    g.addNode(n);
                }
                Node m = g.getNode(tmp[1].trim());
                if (m == null) {
                    m = new Node(new Vector(100));
                    m.setName(tmp[1].trim());
                    g.addNode(m);
                }
                g.connect(n, m, 10);
                if (tmp.length > 2) {
                    Edge e = n.getEdgeTo(m);
                    e.setLabel(tmp[2].trim());
                }
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void importContent(Graph g, String content) {
        importContent(g, new BufferedReader(new StringReader(content)));
    }
    
    public static void importfile(Graph g, String filename) {
        try {
            importContent(g, new BufferedReader(new FileReader(filename)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}
