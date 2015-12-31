package lwjglTest;

/*
 * IMPORTS FROM: 	lwjgl.jar + Native Libraries
 * 					lwjgl_util.jar
 * 					slick-util.jar
 */
import de.jg3d.Edge;
import de.jg3d.Graph;
import static de.jg3d.Main.createAndRun;
import de.jg3d.Node;
import de.jg3d.Vector;
import de.jg3d.util.Importer;
import java.awt.Canvas;
import java.awt.Color;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.LWJGLException;
import org.lwjgl.LWJGLUtil;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Vector3f;

public class Game {

    final static int width = 800, height = 600;
    final static int frameRate = 90;
    boolean[] keys = new boolean[256];
    Camera camera;

    public Graph G;

    public static void main(String[] args) throws LWJGLException {
        File JGLLib = null;
        switch (LWJGLUtil.getPlatform()) {
            case LWJGLUtil.PLATFORM_WINDOWS: {
                JGLLib = new File("./native/windows/");
            }
            break;

            case LWJGLUtil.PLATFORM_LINUX: {
                JGLLib = new File("./native/linux/");
            }
            break;

            case LWJGLUtil.PLATFORM_MACOSX: {
                JGLLib = new File("./native/macosx/");
            }
            break;
        }
        System.setProperty("org.lwjgl.librarypath", JGLLib.getAbsolutePath());

        final MiniMain demo = new MiniMain(new Graph());

        MiniMain.createGraphK(demo.graph, 630);
//        MiniMain.nodeGrid(demo.graph, 25, 24, 33, true, true);
//        Importer.importfile(demo.graph, "cir.txt");
        for (Edge n : demo.graph.getEdges()) {
            n.setWeight(n.getWeight() - 9.0);
        }
        demo.runSimulation();

//        while (!demo.runSimulation()){
//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException ex) {
//            }
//        }
        
        Display.setDisplayMode(new DisplayMode(width, height));
        Display.create();
        Game game = new Game();
        game.G = demo.graph;//createAndRun(new String[]{"cir.txt"});
        game.initGL3();
        long lastFPS = Sys.getTime();
        long fps = 0;
        while (!Display.isCloseRequested()) {

            game.render();
            game.update();
            Display.update();
            Display.sync(frameRate);

            if (Sys.getTime() - lastFPS > 1000) {
                Display.setTitle("FPS: " + fps);
                fps = 0; //reset the FPS counter
                lastFPS += 1000; //add one second
            }
            fps++;
        }
        Display.destroy();
        System.exit(0);
    }
    private boolean selecting = false;

    public Game() {
        camera = new Camera(this);
    }

    void draw_triangle() {
        GL11.glBegin(GL11.GL_TRIANGLES);

        GL11.glColor3f(0.0f, 0.0f, 1.0f);
        GL11.glVertex3f(0.0f, 0.0f, 10f);

        GL11.glColor3f(1.0f, 0.0f, 0.0f);
        GL11.glVertex3f(0.0f, 0f, -10f);

        GL11.glColor3f(0.0f, 1.0f, 0.0f);
        GL11.glVertex3f(0.0f, -10, 0);

        GL11.glEnd();
    }

    public void renderObjs() {
        GL11.glPushMatrix();
        GL11.glScalef(.1f, .1f, .1f);
        Vector p, p2;
        int i = 1;
        for (Node n : G.getNodes()) {
            p = n.getPos();
            drawCube((float) p.getX(), (float) p.getY(), (float) p.getZ(), 1f, selecting ? i : n.hashCode());
            i++;
        }

//        for (Edge e : G.getEdges()) {
//            p = e.getSource().getPos();
//            p2 = e.getDestination().getPos();
//            GL11.glColor3f(0.0f, 1.0f, 0.2f);
//            GL11.glLineWidth(1f);
//            GL11.glBegin(GL11.GL_LINE_STRIP);
//            GL11.glVertex3f((float) p.getX(), (float) p.getY(), (float) p.getZ());
//            GL11.glVertex3f((float) p2.getX(), (float) p2.getY(), (float) p2.getZ());
//            GL11.glEnd();
//        }
        if (nSelect != null) {
            for (Edge e : nSelect.getAdjacencies()) {
                p = e.getSource().getPos();
                p2 = e.getDestination().getPos();
                GL11.glColor3f(0.0f, 1.0f, 0.2f);
                GL11.glLineWidth(1f);
                GL11.glBegin(GL11.GL_LINE_STRIP);
                GL11.glVertex3f((float) p.getX(), (float) p.getY(), (float) p.getZ());
                GL11.glVertex3f((float) p2.getX(), (float) p2.getY(), (float) p2.getZ());
                GL11.glEnd();
            }
        }
        GL11.glPopMatrix();
    }

    public void render() {
        initGL3();
        clearGL();
        camera.translatePostion();

        //floor
        GL11.glColor3f(0.4f, 0.4f, 1);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(0, 0);
        GL11.glVertex3f(0, 0, 0);

        GL11.glTexCoord2f(1, 0);
        GL11.glVertex3f(10, 0, 0);

        GL11.glTexCoord2f(1, 1);
        GL11.glVertex3f(10, 0, 10);

        GL11.glTexCoord2f(0, 1);
        GL11.glVertex3f(0, 0, 10);
        GL11.glEnd();
        //etc.....
        renderObjs();

        if (Mouse.isButtonDown(0)) {
            selecting = true;
            initGL3();
            clearGL();
            camera.translatePostion();
            renderObjs();
            selecting = false;
            select(400, 300);
        }

        initGL2();

        GL11.glColor3f(1, 1, 0);
        SimpleText.drawString(Math.log10(G.getKE()) + " ?", 32, 580);

        GL11.glBegin(GL11.GL_POINTS);
        GL11.glColor3f(0.4f, 0.4f, 1);
        GL11.glVertex2f(width / 2, height / 2);

        GL11.glColor3f(1, 1, 1);
        for (int i = 0; i < 360; i += 90) {
            GL11.glVertex2d(width / 2 + Math.sin(i * Math.PI / 180) * 8, height / 2 + Math.cos(i * Math.PI / 180) * 8);
        }
        GL11.glEnd();
//
//        GL11.glMatrixMode(GL11.GL_MODELVIEW);
//        GL11.glPushMatrix();
//
//        GL11.glTranslatef(0.0f, 10.0f, 0.0f);
//
//        draw_triangle();
//
//        GL11.glRotatef(45, 1.0f, 0.0f, 0.0f);
//
//        draw_triangle();
//
//        GL11.glPopMatrix();
    }

    public void drawCube(float x, float y, float z, float scale, int i) {

        int r = (i & 0x000000FF) >> 0;
        int g = (i & 0x0000FF00) >> 8;
        int b = (i & 0x00FF0000) >> 16;

//        byte array[] = ByteBuffer.allocate(4).putInt(color * 1000).array();
//        int a = (array[1] & 0xFF) << 16 | (array[2] & 0xFF) << 8 | (array[3] & 0xFF);
//        System.out.println(color + " " + (color * 1000) + " " + Arrays.toString(array) + " " + a);
        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, z);
        GL11.glScalef(scale, scale, scale);
        GL11.glColor4f(r / 255.0f, g / 255.0f, b / 255.0f, 1.0f);
//        GL11.glColor3f(0.5f, 0.5f, 1.0f);                 // Set The Color To Blue One Time Only
        GL11.glBegin(GL11.GL_QUADS);                        // Draw A Quad
//        GL11.glColor3f(0.0f, 1.0f, 0.0f);             // Set The Color To Green
        GL11.glVertex3f(1.0f, 1.0f, -1.0f);         // Top Right Of The Quad (Top)
        GL11.glVertex3f(-1.0f, 1.0f, -1.0f);         // Top Left Of The Quad (Top)
        GL11.glVertex3f(-1.0f, 1.0f, 1.0f);         // Bottom Left Of The Quad (Top)
        GL11.glVertex3f(1.0f, 1.0f, 1.0f);         // Bottom Right Of The Quad (Top)
//        GL11.glColor3f(1.0f, 0.5f, 0.0f);             // Set The Color To Orange
        GL11.glVertex3f(1.0f, -1.0f, 1.0f);         // Top Right Of The Quad (Bottom)
        GL11.glVertex3f(-1.0f, -1.0f, 1.0f);         // Top Left Of The Quad (Bottom)
        GL11.glVertex3f(-1.0f, -1.0f, -1.0f);         // Bottom Left Of The Quad (Bottom)
        GL11.glVertex3f(1.0f, -1.0f, -1.0f);         // Bottom Right Of The Quad (Bottom)
//        GL11.glColor3f(1.0f, 0.0f, 0.0f);             // Set The Color To Red
        GL11.glVertex3f(1.0f, 1.0f, 1.0f);         // Top Right Of The Quad (Front)
        GL11.glVertex3f(-1.0f, 1.0f, 1.0f);         // Top Left Of The Quad (Front)
        GL11.glVertex3f(-1.0f, -1.0f, 1.0f);         // Bottom Left Of The Quad (Front)
        GL11.glVertex3f(1.0f, -1.0f, 1.0f);         // Bottom Right Of The Quad (Front)
//        GL11.glColor3f(1.0f, 1.0f, 0.0f);             // Set The Color To Yellow
        GL11.glVertex3f(1.0f, -1.0f, -1.0f);         // Bottom Left Of The Quad (Back)
        GL11.glVertex3f(-1.0f, -1.0f, -1.0f);         // Bottom Right Of The Quad (Back)
        GL11.glVertex3f(-1.0f, 1.0f, -1.0f);         // Top Right Of The Quad (Back)
        GL11.glVertex3f(1.0f, 1.0f, -1.0f);         // Top Left Of The Quad (Back)
//        GL11.glColor3f(0.0f, 0.0f, 1.0f);             // Set The Color To Blue
        GL11.glVertex3f(-1.0f, 1.0f, 1.0f);         // Top Right Of The Quad (Left)
        GL11.glVertex3f(-1.0f, 1.0f, -1.0f);         // Top Left Of The Quad (Left)
        GL11.glVertex3f(-1.0f, -1.0f, -1.0f);         // Bottom Left Of The Quad (Left)
        GL11.glVertex3f(-1.0f, -1.0f, 1.0f);         // Bottom Right Of The Quad (Left)
//        GL11.glColor3f(1.0f, 0.0f, 1.0f);             // Set The Color To Violet
        GL11.glVertex3f(1.0f, 1.0f, -1.0f);         // Top Right Of The Quad (Right)
        GL11.glVertex3f(1.0f, 1.0f, 1.0f);         // Top Left Of The Quad (Right)
        GL11.glVertex3f(1.0f, -1.0f, 1.0f);         // Bottom Left Of The Quad (Right)
        GL11.glVertex3f(1.0f, -1.0f, -1.0f);         // Bottom Right Of The Quad (Right)
        GL11.glEnd();                                       // Done Drawing The Quad
        GL11.glPopMatrix();
    }

    public Node nSelect = null;

    private void select(int x, int y) {

        GL11.glFlush();
        GL11.glFinish();
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
        ByteBuffer vpBuffer = ByteBuffer.allocateDirect(8);
        GL11.glReadPixels(x, y, 1, 1, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, vpBuffer);
        byte[] data = new byte[4];
        vpBuffer.get(data);
        int pickedID = data[0] + (data[1] << 8) + (data[2] << 16);
        if (pickedID < 0) {
            pickedID = 256 + pickedID;
        }
        pickedID--;
        if (pickedID > 0 && pickedID < 5000) {
            nSelect = G.getNode(pickedID);
        } else {
//            nSelect = null;
        }
        System.out.println(pickedID);
    }

    public void update() {
        mapKeys();
        camera.update();
        {
            if (nSelect != null) {
                Vector v = nSelect.getPos();
                Vector3f c = camera.vector;
                v.setPos(c.x, c.y, c.z);
                nSelect.setPos(v.multiply(10));
            }
        }
    }

    private void mapKeys() {
        //Update keys
        for (int i = 0; i < keys.length; i++) {
            keys[i] = Keyboard.isKeyDown(i);
        }
    }

    private void initGL3() {
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();

        GLU.gluPerspective((float) 100, width / height, 0.001f, 1000);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);
        GL11.glClearDepth(1.0f);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
    }

    public void initGL2() {
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0, width, 0, height, -1, 1);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
    }

    private void clearGL() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
//        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
        GL11.glLoadIdentity();
    }
}
