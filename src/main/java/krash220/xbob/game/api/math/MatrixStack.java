/*
 * Crosshair Bobbing - original work by Krash220 (https://github.com/Krash220/CrosshairBobbingMod),
 * licensed under the GNU General Public License v3.0 (see LICENSE).
 *
 * MODIFIED by DEHunter58711 in 2026 for the UNOFFICIAL NeoForge / Minecraft 26.3 port
 * (GPL-3.0 section 5a). This file is distributed under GPL-3.0 as well.
 * Changes: rewritten for Minecraft 26.3: the HUD now uses a 2D Matrix3x2fStack (GuiGraphicsExtractor.pose()), so this wrapper supports a JOML Matrix4f backend (camera math) and a Matrix3x2fStack backend (HUD).
 */
package krash220.xbob.game.api.math;

import org.joml.Matrix3x2fStack;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector4f;

public class MatrixStack {

    /** 3D backend (camera / projection math). Null when wrapping the GUI pose. */
    public final Matrix4fStack mat;
    /** 2D backend (HUD pose in 26.x). Null when used for 3D math. */
    private final Matrix3x2fStack gui;

    public MatrixStack() {
        this.mat = new Matrix4fStack(16);
        this.gui = null;
    }

    public MatrixStack(Matrix3x2fStack gui) {
        this.mat = null;
        this.gui = gui;
    }

    public void push() {
        if (gui != null) gui.pushMatrix(); else mat.pushMatrix();
    }

    public void pop() {
        if (gui != null) gui.popMatrix(); else mat.popMatrix();
    }

    public void translate(double x, double y, double z) {
        if (gui != null) gui.translate((float) x, (float) y); // z has no meaning on the 2D HUD
        else mat.translate((float) x, (float) y, (float) z);
    }

    public void rotate(float angle, float x, float y, float z) {
        if (gui != null) {
            // Only rotations about the screen normal (Z) are possible on the 2D HUD; that is all MainMod uses.
            if (x == 0f && y == 0f && z != 0f) {
                gui.rotate((float) Math.toRadians(angle) * Math.signum(z));
            }
        } else {
            float len = (float) Math.sqrt(x * x + y * y + z * z);
            if (len > 0f) {
                mat.rotate((float) Math.toRadians(angle), x / len, y / len, z / len);
            }
        }
    }

    public void scale(float x, float y, float z) {
        if (gui != null) gui.scale(x, y); else mat.scale(x, y, z);
    }

    public void identity() {
        if (gui != null) gui.identity(); else mat.identity();
    }

    public void mul(Matrix4f other) {
        mat.mul(other);
    }

    public float[] multiplyVector(float x, float y, float z, float w) {
        Vector4f vec = new Vector4f(x, y, z, w);

        vec.mul(this.mat);

        return new float[] {vec.x(), vec.y(), vec.z(), vec.w()};
    }
}
