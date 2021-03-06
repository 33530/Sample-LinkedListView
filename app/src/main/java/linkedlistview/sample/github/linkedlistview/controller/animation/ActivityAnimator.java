package linkedlistview.sample.github.linkedlistview.controller.animation;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.Transformation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import linkedlistview.sample.github.linkedlistview.model.AnimBounceTask;
import linkedlistview.sample.github.linkedlistview.model.AnimPaddingTask;

/**
 * Created by GensaGames
 * GensaGames
 */

public class ActivityAnimator {

    private static final String TAG = ActivityAnimator.class.getSimpleName();
    private static final long ANIM_RAND_UPPER_DUR = 12000L;
    private static final long ANIM_RAND_LOWER_DUR = 4000L;

    private Random mLocalRandom;
    private Handler mMainThreadHandler;
    private BounceInterpolator mBounceInterpolator;
    private List<AnimBounceTask> listActiveTask;
    private long mUpperDuration;
    private long mLowerDuration;

    public ActivityAnimator() {
        mMainThreadHandler = new Handler(Looper.getMainLooper());
        mBounceInterpolator = new BounceInterpolator();
        listActiveTask = new ArrayList<>();
        mLocalRandom = new Random();
        mUpperDuration = ANIM_RAND_UPPER_DUR;
        mLowerDuration = ANIM_RAND_LOWER_DUR;
    }

    @SuppressWarnings("unused")
    public void setLowerUpperDuration(long upper, long lower) {
        mUpperDuration = upper;
        mLowerDuration = lower;
    }

    private long getRandomDuration() {
        return mLowerDuration + (long) (mLocalRandom.nextDouble() *
                (mUpperDuration - mLowerDuration));
    }

    public void animatePaddingChange(final AnimPaddingTask animPaddingTask) {
        final int padding[] = animPaddingTask.getPadding();
        final View animatedView = animPaddingTask.getAnimatedView();
        final AnimPaddingTask.OnAnimationFrame onAnimationFrame =
                animPaddingTask.getOnAnimationFrame();

        if (padding.length != 4) {
            throw new IndexOutOfBoundsException("Padding requires only/least 4 Fields");
        }
        Animation anim = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                animatedView.setPadding((int) (padding[0] * interpolatedTime), (int) (padding[1] *
                                interpolatedTime), (int) (padding[2] * interpolatedTime),
                        (int) (padding[3] * interpolatedTime));
                onAnimationFrame.onAnimationFrame();
            }
        };
        anim.setDuration(animPaddingTask.getDuration());
        anim.setAnimationListener(animPaddingTask.getAnimationListener());
        animatedView.startAnimation(anim);
    }

    public void loopRandomDelayedAnim(AnimBounceTask bounceTask) {
        if (listActiveTask.contains(bounceTask) || bounceTask == null) {
            Log.e(TAG, AnimBounceTask.class.getSimpleName()
                    + " already work or empty!");
            return;
        }
        listActiveTask.add(bounceTask);
        final int indexOfTask = listActiveTask.indexOf(bounceTask);
        mMainThreadHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                AnimBounceTask task = listActiveTask.get(indexOfTask);
                switch (task.getState()) {
                    case START:
                        playBounceAnimation(task.getView(), task
                                .getMaxTranslationY(), task.getDuration());
                    case STOP:
                        mMainThreadHandler.postDelayed(this, getRandomDuration());
                        break;
                    case RELEASE:
                        listActiveTask.remove(task);
                        break;
                }
            }
        }, getRandomDuration());
    }


    /**
     * ------------------------- *** ----------------------------
     * ------------------------- *** ----------------------------
     */
    private AnimatorSet playBounceAnimation(View view, float maxTranslationY, long duration) {
        if (view == null) {
            Log.e(TAG, AnimBounceTask.class.getSimpleName() + " has empty View!");
            return null;
        }
        float baseTranslation = view.getTranslationY();
        AnimatorSet animatorSet = new AnimatorSet();

        ObjectAnimator animation1 = ObjectAnimator.ofFloat(view,
                "translationY", maxTranslationY);
        animation1.setDuration(duration);
        animation1.setInterpolator(mBounceInterpolator);

        ObjectAnimator animation2 = ObjectAnimator.ofFloat(view,
                "translationY", baseTranslation);
        animation2.setDuration(duration);
        animation2.setInterpolator(mBounceInterpolator);

        animatorSet.play(animation2).after(animation1);
        animatorSet.start();
        return animatorSet;
    }
}
