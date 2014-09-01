/**
 * A custom OpenGL renderer, that gets registered to the {@link AndARRenderer}.
 * It allows you to draw non Augmented Reality stuff, and setup the OpenGL
 * environment.
 * @author tobi
 * 
 * Modified by: Tom Nevala, 2014
 */

package uni.oulu.mentor;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.opengl.GLU;

import edu.dhbw.andar.AndARRenderer;
import edu.dhbw.andar.interfaces.OpenGLRenderer;
import edu.dhbw.andar.util.GraphicsUtil;

public class CustomRenderer implements OpenGLRenderer {
	
	/**
	 * Matrices
	 */
	
	public static int[] viewport = new int[16];
	public static float[] modelViewMatrix = new float[16];
	public static float[] projectionMatrix = new float[16];
	public static float[] pointInPlane = new float[16];
	public static float camera_zoom = 1.0f;
	//public static float camera_zoom = 12.5f;
	public float[] bounds = new float[4];
	
	/**
	 * Light definitions
	 */	
	
	private float[] ambientlight1 = {.3f, .3f, .3f, 1f};
	private float[] diffuselight1 = {.7f, .7f, .7f, 1f};
	private float[] specularlight1 = {0.6f, 0.6f, 0.6f, 1f};
	private float[] lightposition1 = {20.0f,-40.0f,100.0f,1f};
	
	private FloatBuffer lightPositionBuffer1 =  GraphicsUtil.makeFloatBuffer(lightposition1);
	private FloatBuffer specularLightBuffer1 = GraphicsUtil.makeFloatBuffer(specularlight1);
	private FloatBuffer diffuseLightBuffer1 = GraphicsUtil.makeFloatBuffer(diffuselight1);
	private FloatBuffer ambientLightBuffer1 = GraphicsUtil.makeFloatBuffer(ambientlight1);
	
	
	
	/**
	 * Do non Augmented Reality stuff here. Will be called once after all AR objects have
	 * been drawn. The transformation matrices may have to be reset.
	 */
	public final void draw(GL10 gl) {
		 //GLU.gluUnProject(SomeSurface.mouse[0], viewport[3] - SomeSurface.mouse[1], camera_zoom, modelViewMatrix, 0, projectionMatrix, 0, viewport, 0, pointInPlane, 0);  
		 //pointInPlane[0] *= -camera_zoom;  
		 //pointInPlane[1] *= -camera_zoom; 		
	}


	/**
	 * Directly called before each object is drawn. Used to setup lighting and
	 * other OpenGL specific things.
	 */
	public final void setupEnv(GL10 gl) {
		gl.glEnable(GL10.GL_LIGHTING);
		gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_AMBIENT, ambientLightBuffer1);
		gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_DIFFUSE, diffuseLightBuffer1);
		gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_SPECULAR, specularLightBuffer1);
		gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_POSITION, lightPositionBuffer1);
		gl.glEnable(GL10.GL_LIGHT1);
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	    gl.glDisable(GL10.GL_TEXTURE_2D);
		initGL(gl);
	}
	
	/**
	 * Called once when the OpenGL Surface was created.
	 */
	public final void initGL(GL10 gl) {
		gl.glDisable(GL10.GL_COLOR_MATERIAL);
		gl.glEnable(GL10.GL_CULL_FACE);
		gl.glShadeModel(GL10.GL_SMOOTH);
		gl.glDisable(GL10.GL_COLOR_MATERIAL);
		gl.glEnable(GL10.GL_LIGHTING);
		gl.glEnable(GL10.GL_CULL_FACE);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glEnable(GL10.GL_NORMALIZE);
		if(gl instanceof GL11) {
			gl.glLoadIdentity();
			/* Idea from http://ssjskipp.blogspot.fi/2011/08/opengl-android-and-gluunproject.html */
			((GL11)gl).glGetIntegerv(GL11.GL_VIEWPORT, viewport, 0);
			((GL11)gl).glGetFloatv(GL11.GL_MODELVIEW_MATRIX, modelViewMatrix, 0);
			((GL11)gl).glGetFloatv(GL11.GL_PROJECTION_MATRIX, projectionMatrix, 0);
			//viewport[3] is height, gets x and y of top left corner of screen
			GLU.gluUnProject(0, viewport[3], camera_zoom, modelViewMatrix, 0, projectionMatrix, 0, viewport, 0, pointInPlane, 0);
			bounds[0] = pointInPlane[0] * -camera_zoom;
			bounds[1] = pointInPlane[1] * -camera_zoom;
			//viewport[2] is width, gets x and y of bottom right corner of screen
			GLU.gluUnProject(viewport[2], 0, camera_zoom, modelViewMatrix, 0, projectionMatrix, 0, viewport, 0, pointInPlane, 0);
			bounds[2] = pointInPlane[0] * -camera_zoom;
			bounds[3] = pointInPlane[1] * -camera_zoom;
		}
	}
}

