package uni.oulu.mentor;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

/* Idea of using a custom LinearLayout from: https://github.com/commonsguy/cw-advandroid/tree/master/Animation/SlidingPanel */
public class SliderImage extends LinearLayout {
	ImageView im;
	boolean panelOn = false;
	int translationX = 400;
	public SliderImage(Context context, AttributeSet attrs) {
		super(context, attrs);
		im = new ImageView(context);
		im.setImageResource(R.drawable.l_arrow);
		int size = context.getResources().getDisplayMetrics().widthPixels;
		im.setX((size-30));
		LayoutParams lpl = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
		lpl.setMargins(0, 0, 0, 0);
		im.setLayoutParams(lpl);
		this.addView(im);
	}
	// this method is used to set amount translation of arrow image as the slider panel opens and closes
	public void setTransX(int translationX) {
		this.translationX = translationX;
	}
	//this method is used to animate arrow move when slider panel is opened
	public void activate() {
		setVisibility(View.VISIBLE);
		ObjectAnimator oAnim = ObjectAnimator.ofFloat(im, "translationX", (getWidth()-30), (getWidth()-translationX-30));
		oAnim.addListener(oAcListener);
		oAnim.setDuration(500);
		oAnim.setInterpolator(new LinearInterpolator());
		oAnim.start();
	}
	//this method is used to animate arrow move when slider panel is closed
	public void deactivate() {
		ObjectAnimator oAnim = ObjectAnimator.ofFloat(im, "translationX", (getWidth()-translationX-30), (getWidth()-30));
		oAnim.addListener(oDeListener);
		oAnim.setDuration(500);
		oAnim.setInterpolator(new LinearInterpolator());
		oAnim.start();
	}
	//AnimatorListener, which reacts to the events happening when animating arrow move when opening the panel
	AnimatorListener oAcListener = new AnimatorListener() {
		public void onAnimationEnd(Animator anim) {
			im.setImageResource(R.drawable.r_arrow);
			panelOn = true;
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
	//AnimatorListener, which reacts to the events happening when animating arrow move when closing the panel
	AnimatorListener oDeListener = new AnimatorListener() {
		public void onAnimationEnd(Animator anim) {
			im.setImageResource(R.drawable.l_arrow);
			panelOn = false;
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
