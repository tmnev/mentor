/**
 * An example of an AR object being drawn on a marker.
 * @author tobi
 *
 * Modified by: Tom Nevala, 2014
 */

package uni.oulu.mentor;

import java.nio.FloatBuffer;
import javax.microedition.khronos.opengles.GL10;
import android.os.SystemClock;
import edu.dhbw.andar.ARObject;
import edu.dhbw.andar.pub.SimpleBox;
import edu.dhbw.andar.util.GraphicsUtil;

public class CustomObject extends ARObject {
	/**
	 * Just a box, imported from the AndAR project.
	 */
	private SimpleBox box = new SimpleBox();
	private FloatBuffer mat_flash;
	private FloatBuffer mat_ambient;
	private FloatBuffer mat_flash_shiny;
	private FloatBuffer mat_diffuse;
	public volatile int mIndicator = 0;
	public volatile int speedUpColor = 1;
	public volatile int moreExamplesColor = 2;
	public volatile int slowDownColor = 3;
	public volatile int repeatColor = 4;
	public volatile int questionColor = 5;
	public volatile int answerColor = 6;
	public float speedUpGreenVal = 0.8f;
	public float speedUpRedVal = 0.0f;
	public float speedUpBlueVal = 0.0f;
	public float slowDownGreenVal = 0.0f;
	public float slowDownRedVal = 0.8f;
	public float slowDownBlueVal = 0.0f;
	public float moreExamplesGreenVal = 0.8f;
	public float moreExamplesRedVal = 0.8f;
	public float moreExamplesBlueVal = 0.0f;
	public float repeatGreenVal = 0.8f;
	public float repeatRedVal = 0.0f;
	public float repeatBlueVal = 0.8f;
	public float questionGreenVal = 0.0f;
	public float questionRedVal = 0.0f;
	public float questionBlueVal = 0.8f;
	public float answerGreenVal = 0.0f;
	public float answerRedVal = 0.8f;
	public float answerBlueVal = 0.8f;
	public boolean flaring = false;
	public boolean collapsed = false;
	public int flareTime; //5s by default
	public int timePassed;
	public long startMillis = 0;
	public int alpha = 80;
	public float alphaVal = 0.8f;
	/* Basic constructor */
	public CustomObject(String name, String patternName,
			double markerWidth, double[] markerCenter) {
		super(name, patternName, markerWidth, markerCenter);
		float   mat_ambientf[]     = {0.2f, 0.2f, 0.2f, 0.8f};
		float   mat_flashf[]       = {0.2f, 0.2f, 0.2f, 0.8f};
		float   mat_diffusef[]       = {0.2f, 0.2f, 0.2f, 0.8f};
		float   mat_flash_shinyf[] = {50.0f};

		mat_ambient = GraphicsUtil.makeFloatBuffer(mat_ambientf);
		mat_flash = GraphicsUtil.makeFloatBuffer(mat_flashf);
		mat_flash_shiny = GraphicsUtil.makeFloatBuffer(mat_flash_shinyf);
		mat_diffuse = GraphicsUtil.makeFloatBuffer(mat_diffusef);
		flareTime = 5000;
	}
	/* Constructor for adding a custom color at initialization phase */
	public CustomObject(String name, String patternName,
			double markerWidth, double[] markerCenter, float[] customColor) {
		super(name, patternName, markerWidth, markerCenter);
		float   mat_flash_shinyf[] = {50.0f};

		mat_ambient = GraphicsUtil.makeFloatBuffer(customColor);
		mat_flash = GraphicsUtil.makeFloatBuffer(customColor);
		mat_flash_shiny = GraphicsUtil.makeFloatBuffer(mat_flash_shinyf);
		mat_diffuse = GraphicsUtil.makeFloatBuffer(customColor);
		
	}
	// this method is used for setting the flaretime
	public void setFlareTime(int flareTime) {
		this.flareTime = flareTime;
	}
	// this method is used for changing the message type, mIndicator indicates the message type
	public void setIndicator(int indicator) {
		this.mIndicator = indicator;
		startMillis = SystemClock.uptimeMillis();
		flaring = true;
	}
	// returns the message type
	public int getIndicator() {
		return this.mIndicator;
	}
	// sets the default color values for message types
	public void setDefaultColors() {
		this.speedUpColor = 1;
		this.moreExamplesColor = 2;
		this.slowDownColor = 3;
		this.repeatColor = 4;
		this.questionColor = 5;
		this.answerColor = 6;
	}
	/* these methods are used for changing the color values for message types */
	public void setSpeedUpColor(int speedUpColor) {
		setDefaultColors();
		this.speedUpColor = speedUpColor;
	}
	public void setSlowDownColor(int slowDownColor) {
		setDefaultColors();
		this.slowDownColor = slowDownColor;
	}
	public void setMoreExamplesColor(int moreExamplesColor) {
		setDefaultColors();
		this.moreExamplesColor = moreExamplesColor;
	}
	public void setRepeatColor(int repeatColor) {
		setDefaultColors();
		this.repeatColor = repeatColor;
	}
	public void setQuestionColor(int questionColor) {
		setDefaultColors();
		this.questionColor = questionColor;
	}
	public void setAnswerColor(int answerColor) {
		setDefaultColors();
		this.answerColor = answerColor;
	}
	
	/**
	 * Everything drawn here will be drawn directly onto the marker,
	 * as the corresponding translation matrix will already be applied.
	 */
	@Override
	public final void draw(GL10 gl) {
		super.draw(gl);
		//enable alpha
		/* Idea from http://stackoverflow.com/questions/11174991/android-opengl-es-1-0-alpha */
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		//change alpha of this box in the function of time
		if(mIndicator != 0 && flaring) {
			if(alpha <= 100 && collapsed == false) {
				if(alpha == 100)
					collapsed = true;
				else
					alpha=alpha+5;
			}
			else if(alpha >= 60 && collapsed == true) {
				if(alpha == 60)
					collapsed = false;
				else
					alpha=alpha-5;
			}
			float alphaF = (float)alpha;
			alphaVal = alphaF / 100;
			long passedTime = SystemClock.uptimeMillis();
			if(startMillis != 0) {
				passedTime = passedTime - startMillis;
			}
			if((int)passedTime >= flareTime) {
				alphaVal = 80;
				flaring = false;
				mIndicator = 0;
			}
			if(speedUpColor != 1) {
				if(speedUpColor == 2) {
					speedUpRedVal = 0.8f;
					speedUpGreenVal = 0.8f;
					speedUpBlueVal = 0.0f;
				}
				else if(speedUpColor == 3) {
					speedUpRedVal = 0.8f;
					speedUpGreenVal = 0.0f;
					speedUpBlueVal = 0.0f;
				}
				else if(speedUpColor == 4) {
					speedUpRedVal = 0.0f;
					speedUpGreenVal = 0.8f;
					speedUpBlueVal = 0.8f;					
				}
				else if(speedUpColor == 5) {
					speedUpRedVal = 0.0f;
					speedUpGreenVal = 0.0f;
					speedUpBlueVal = 0.8f;					
				}
				else if(speedUpColor == 6) {
					speedUpRedVal = 0.8f;
					speedUpGreenVal = 0.0f;
					speedUpBlueVal = 0.8f;					
				}
			}
			else {
				speedUpRedVal = 0.0f;
				speedUpGreenVal = 0.8f;
				speedUpBlueVal = 0.0f;
			}
			if(slowDownColor != 1) {
				if(slowDownColor == 2) {
					slowDownRedVal = 0.8f;
					slowDownGreenVal = 0.8f;
					slowDownBlueVal = 0.0f;
				}
				else if(slowDownColor == 3) {
					slowDownRedVal = 0.8f;
					slowDownGreenVal = 0.0f;
					slowDownBlueVal = 0.0f;
				}
				else if(slowDownColor == 4) {
					slowDownRedVal = 0.0f;
					slowDownGreenVal = 0.8f;
					slowDownBlueVal = 0.8f;					
				}
				else if(slowDownColor == 5) {
					slowDownRedVal = 0.0f;
					slowDownGreenVal = 0.0f;
					slowDownBlueVal = 0.8f;					
				}
				else if(slowDownColor == 6) {
					slowDownRedVal = 0.8f;
					slowDownGreenVal = 0.0f;
					slowDownBlueVal = 0.8f;					
				}
			}
			else {
				slowDownRedVal = 0.8f;
				slowDownGreenVal = 0.0f;
				slowDownBlueVal = 0.0f;
			}
			if(moreExamplesColor != 1) {
				if(moreExamplesColor == 2) {
					moreExamplesRedVal = 0.8f;
					moreExamplesGreenVal = 0.8f;
					moreExamplesBlueVal = 0.0f;
				}
				else if(moreExamplesColor == 3) {
					moreExamplesRedVal = 0.8f;
					moreExamplesGreenVal = 0.0f;
					moreExamplesBlueVal = 0.0f;
				}
				else if(moreExamplesColor == 4) {
					moreExamplesRedVal = 0.0f;
					moreExamplesGreenVal = 0.8f;
					moreExamplesBlueVal = 0.8f;					
				}
				else if(moreExamplesColor == 5) {
					moreExamplesRedVal = 0.0f;
					moreExamplesGreenVal = 0.0f;
					moreExamplesBlueVal = 0.8f;					
				}
				else if(moreExamplesColor == 6) {
					moreExamplesRedVal = 0.8f;
					moreExamplesGreenVal = 0.0f;
					moreExamplesBlueVal = 0.8f;					
				}
			}
			else {
				moreExamplesRedVal = 0.8f;
				moreExamplesGreenVal = 0.8f;
				moreExamplesBlueVal = 0.0f;
			}
			if(repeatColor != 1) {
				if(repeatColor == 2) {
					repeatRedVal = 0.8f;
					repeatGreenVal = 0.8f;
					repeatBlueVal = 0.0f;
				}
				else if(repeatColor == 3) {
					repeatRedVal = 0.8f;
					repeatGreenVal = 0.0f;
					repeatBlueVal = 0.0f;
				}
				else if(repeatColor == 4) {
					repeatRedVal = 0.0f;
					repeatGreenVal = 0.8f;
					repeatBlueVal = 0.8f;					
				}
				else if(repeatColor == 5) {
					repeatRedVal = 0.0f;
					repeatGreenVal = 0.0f;
					repeatBlueVal = 0.8f;					
				}
				else if(repeatColor == 6) {
					repeatRedVal = 0.8f;
					repeatGreenVal = 0.0f;
					repeatBlueVal = 0.8f;					
				}
			}
			else {
				repeatRedVal = 0.0f;
				repeatGreenVal = 0.8f;
				repeatBlueVal = 0.8f;
			}
			if(questionColor != 1) {
				if(questionColor == 2) {
					questionRedVal = 0.8f;
					questionGreenVal = 0.8f;
					questionBlueVal = 0.0f;
				}
				else if(questionColor == 3) {
					questionRedVal = 0.8f;
					questionGreenVal = 0.0f;
					questionBlueVal = 0.0f;
				}
				else if(questionColor == 4) {
					questionRedVal = 0.0f;
					questionGreenVal = 0.8f;
					questionBlueVal = 0.8f;					
				}
				else if(questionColor == 5) {
					questionRedVal = 0.0f;
					questionGreenVal = 0.0f;
					questionBlueVal = 0.8f;					
				}
				else if(questionColor == 6) {
					questionRedVal = 0.8f;
					questionGreenVal = 0.0f;
					questionBlueVal = 0.8f;					
				}
			}
			else {
				questionRedVal = 0.0f;
				questionGreenVal = 0.0f;
				questionBlueVal = 0.8f;
			}
			if(answerColor != 1) {
				if(answerColor == 2) {
					answerRedVal = 0.8f;
					answerGreenVal = 0.8f;
					answerBlueVal = 0.0f;
				}
				else if(answerColor == 3) {
					answerRedVal = 0.8f;
					answerGreenVal = 0.0f;
					answerBlueVal = 0.0f;
				}
				else if(answerColor == 4) {
					answerRedVal = 0.0f;
					answerGreenVal = 0.8f;
					answerBlueVal = 0.8f;					
				}
				else if(answerColor == 5) {
					answerRedVal = 0.0f;
					answerGreenVal = 0.0f;
					answerBlueVal = 0.8f;					
				}
				else if(answerColor == 6) {
					answerRedVal = 0.8f;
					answerGreenVal = 0.0f;
					answerBlueVal = 0.8f;					
				}
			}
			else {
				answerRedVal = 0.8f;
				answerGreenVal = 0.0f;
				answerBlueVal = 0.8f;
			}
		}
		/* these if condition-clases are used for changing the colors, depending on message type and color set */
		if(mIndicator == 1 && flaring) {
			//speed up feedback received, make it green
			float   custom_mat_ambientf[]     = {speedUpRedVal, speedUpGreenVal, speedUpBlueVal, alphaVal};
			float   custom_mat_flashf[]       = {speedUpRedVal, speedUpGreenVal, speedUpBlueVal, alphaVal};
			float   custom_mat_diffusef[]       = {speedUpRedVal, speedUpGreenVal, speedUpBlueVal, alphaVal};
			float   custom_mat_flash_shinyf[] = {50.0f};
			mat_ambient = GraphicsUtil.makeFloatBuffer(custom_mat_ambientf);
			mat_flash = GraphicsUtil.makeFloatBuffer(custom_mat_flashf);
			mat_flash_shiny = GraphicsUtil.makeFloatBuffer(custom_mat_flash_shinyf);
			mat_diffuse = GraphicsUtil.makeFloatBuffer(custom_mat_diffusef);
		}
		else if(mIndicator == 2 && flaring) {
	    	//more examples feedback received, make it yellow
			float   custom_mat_ambientf[]     = {moreExamplesRedVal, moreExamplesGreenVal, moreExamplesBlueVal, alphaVal};
			float   custom_mat_flashf[]       = {moreExamplesRedVal, moreExamplesGreenVal, moreExamplesBlueVal, alphaVal};
			float   custom_mat_diffusef[]       = {moreExamplesRedVal, moreExamplesGreenVal, moreExamplesBlueVal, alphaVal};
			float   custom_mat_flash_shinyf[] = {50.0f};
			mat_ambient = GraphicsUtil.makeFloatBuffer(custom_mat_ambientf);
			mat_flash = GraphicsUtil.makeFloatBuffer(custom_mat_flashf);
			mat_flash_shiny = GraphicsUtil.makeFloatBuffer(custom_mat_flash_shinyf);
			mat_diffuse = GraphicsUtil.makeFloatBuffer(custom_mat_diffusef);			
		}
		else if(mIndicator == 3 && flaring) {
			//slow down feedback received, make it red
			float   custom_mat_ambientf[]     = {slowDownRedVal, slowDownGreenVal, slowDownBlueVal, alphaVal};
			float   custom_mat_flashf[]       = {slowDownRedVal, slowDownGreenVal, slowDownBlueVal, alphaVal};
			float   custom_mat_diffusef[]       = {slowDownRedVal, slowDownGreenVal, slowDownBlueVal, alphaVal};
			float   custom_mat_flash_shinyf[] = {50.0f};
			mat_ambient = GraphicsUtil.makeFloatBuffer(custom_mat_ambientf);
			mat_flash = GraphicsUtil.makeFloatBuffer(custom_mat_flashf);
			mat_flash_shiny = GraphicsUtil.makeFloatBuffer(custom_mat_flash_shinyf);
			mat_diffuse = GraphicsUtil.makeFloatBuffer(custom_mat_diffusef);			
		}
		else if(mIndicator == 4 && flaring) {
	    	//repeat feedback received, make it cyan
			float   custom_mat_ambientf[]     = {repeatRedVal, repeatGreenVal, repeatBlueVal, alphaVal};
			float   custom_mat_flashf[]       = {repeatRedVal, repeatGreenVal, repeatBlueVal, alphaVal};
			float   custom_mat_diffusef[]       = {repeatRedVal, repeatGreenVal, repeatBlueVal, alphaVal};
			float   custom_mat_flash_shinyf[] = {50.0f};
			mat_ambient = GraphicsUtil.makeFloatBuffer(custom_mat_ambientf);
			mat_flash = GraphicsUtil.makeFloatBuffer(custom_mat_flashf);
			mat_flash_shiny = GraphicsUtil.makeFloatBuffer(custom_mat_flash_shinyf);
			mat_diffuse = GraphicsUtil.makeFloatBuffer(custom_mat_diffusef);			
		}
		else if(mIndicator == 5 && flaring) {
	    	//question received, make it blue
			float   custom_mat_ambientf[]     = {questionRedVal, questionGreenVal, questionBlueVal, alphaVal};
			float   custom_mat_flashf[]       = {questionRedVal, questionGreenVal, questionBlueVal, alphaVal};
			float   custom_mat_diffusef[]       = {questionRedVal, questionGreenVal, questionBlueVal, alphaVal};
			float   custom_mat_flash_shinyf[] = {50.0f};
			mat_ambient = GraphicsUtil.makeFloatBuffer(custom_mat_ambientf);
			mat_flash = GraphicsUtil.makeFloatBuffer(custom_mat_flashf);
			mat_flash_shiny = GraphicsUtil.makeFloatBuffer(custom_mat_flash_shinyf);
			mat_diffuse = GraphicsUtil.makeFloatBuffer(custom_mat_diffusef);			
		}
		else if(mIndicator == 6 && flaring) {
	    	//answer received, make it magenta
			float   custom_mat_ambientf[]     = {answerRedVal, answerGreenVal, answerBlueVal, alphaVal};
			float   custom_mat_flashf[]       = {answerRedVal, answerGreenVal, answerBlueVal, alphaVal};
			float   custom_mat_diffusef[]       = {answerRedVal, answerGreenVal, answerBlueVal, alphaVal};
			float   custom_mat_flash_shinyf[] = {50.0f};
			mat_ambient = GraphicsUtil.makeFloatBuffer(custom_mat_ambientf);
			mat_flash = GraphicsUtil.makeFloatBuffer(custom_mat_flashf);
			mat_flash_shiny = GraphicsUtil.makeFloatBuffer(custom_mat_flash_shinyf);
			mat_diffuse = GraphicsUtil.makeFloatBuffer(custom_mat_diffusef);			
		}
		else {
			//nothing received, make it dark gray
			float   custom_mat_ambientf[]     = {0.2f, 0.2f, 0.2f, 0.8f};
			float   custom_mat_flashf[]       = {0.2f, 0.2f, 0.2f, 0.8f};
			float   custom_mat_diffusef[]       = {0.2f, 0.2f, 0.2f, 0.8f};
			float   custom_mat_flash_shinyf[] = {50.0f};
			mat_ambient = GraphicsUtil.makeFloatBuffer(custom_mat_ambientf);
			mat_flash = GraphicsUtil.makeFloatBuffer(custom_mat_flashf);
			mat_flash_shiny = GraphicsUtil.makeFloatBuffer(custom_mat_flash_shinyf);
			mat_diffuse = GraphicsUtil.makeFloatBuffer(custom_mat_diffusef);
		}
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SPECULAR,mat_flash);
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SHININESS, mat_flash_shiny);	
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, mat_diffuse);	
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, mat_ambient);
		
		/* finalize the setting of color by calling glColor4f */
		if(mIndicator == 1 && flaring) {
			//speed up feedback received, make it green by default
			gl.glColor4f(speedUpRedVal, speedUpGreenVal, speedUpBlueVal, alphaVal);
			
		}
		else if(mIndicator == 2 && flaring) {
	    	//more examples feedback received, make it yellow by default
			gl.glColor4f(moreExamplesRedVal, moreExamplesGreenVal, moreExamplesBlueVal, alphaVal);
		}
		else if(mIndicator == 3 && flaring) {
			//slow down feedback received, make it red by default
			gl.glColor4f(slowDownRedVal, slowDownGreenVal, slowDownBlueVal, alphaVal);
		}
		else if(mIndicator == 4 && flaring) {
			//repeat feedback received, make it cyan by default
			gl.glColor4f(repeatRedVal, repeatGreenVal, repeatBlueVal, alphaVal);
		}
		else if(mIndicator == 5 && flaring) {
			//question received, make it blue by default
			gl.glColor4f(questionRedVal, questionGreenVal, questionBlueVal, alphaVal);
		}
		else if(mIndicator == 6 && flaring) {
			//answer received, make it magenta by default
			gl.glColor4f(answerRedVal, answerGreenVal, answerBlueVal, alphaVal);
		}
		else {
			//nothing received, make it dark gray
			gl.glColor4f(0.2f, 0.2f, 0.2f, 0.8f);
		}

	    gl.glTranslatef( 0.0f, 0.0f, 12.5f );
	   
	    //draw the box
	    box.draw(gl);
	    
	}
	
	@Override
	public void init(GL10 gl) {
		
	}
}
