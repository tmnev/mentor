package uni.oulu.mentor;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.Animator.AnimatorListener;
import android.content.Context;
import android.widget.LinearLayout;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

/* Idea of using a custom LinearLayout for panel from: https://github.com/commonsguy/cw-advandroid/tree/master/Animation/SlidingPanel */
public class SliderPanel extends LinearLayout {
	public boolean panelOpen = false;
	boolean firstTime = true;
	int size;
	int translationX = 400;
	int screenSizeX = 0;
	int screenType = 0;
	public SliderPanel(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	// this method is used to set amount translation of slider panel as it opens and closes
	public void setTransX(int translationX) {
		this.translationX = translationX;
	}
	// this method is used to get that is the panel open or closed
	public boolean getPanelOpen() {
		return this.panelOpen;
	}
	//this method is used to animate slider panel open
	public void activate() {
		ObjectAnimator oAnim = null;
		panelOpen = true;
		setVisibility(View.VISIBLE);
		if(firstTime == true) {
			oAnim = ObjectAnimator.ofFloat(this, "translationX", translationX, 0);
			firstTime = false;
		}
		else
			oAnim = ObjectAnimator.ofFloat(this, "translationX", getWidth(), 0);
		oAnim.addListener(oAcListener);
		oAnim.setDuration(500);
		oAnim.setInterpolator(new LinearInterpolator());
		oAnim.start();
	}
	//this method is used to animate slider panel close
	public void deactivate() {
		panelOpen = false;
		ObjectAnimator oAnim = ObjectAnimator.ofFloat(this, "translationX", 0, getWidth());
		oAnim.addListener(oDeListener);
		oAnim.setDuration(500);
		oAnim.setInterpolator(new LinearInterpolator());
		oAnim.start();
	}
	//AnimatorListener, which reacts to the events happening when animating opening the panel
	AnimatorListener oAcListener = new AnimatorListener() {
		public void onAnimationEnd(Animator anim) {
		}
		public void onAnimationStart(Animator anim) {
			//do something
		}
		public void onAnimationRepeat(Animator anim) {
			//do something
		}
		public void onAnimationCancel(Animator anim) {
			//do something
		}		
	};
	//AnimatorListener, which reacts to the events happening when animating closing the panel
	AnimatorListener oDeListener = new AnimatorListener() {
		public void onAnimationEnd(Animator anim) {
			setVisibility(View.GONE);
		}
		public void onAnimationStart(Animator anim) {
			//do something
		}
		public void onAnimationRepeat(Animator anim) {
			//do something
		}
		public void onAnimationCancel(Animator anim) {
			//do something
		}		
	};
}
