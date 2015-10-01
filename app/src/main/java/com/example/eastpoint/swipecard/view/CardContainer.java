package com.example.eastpoint.swipecard.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ListAdapter;

import com.example.eastpoint.swipecard.CardFlipActivity;
import com.example.eastpoint.swipecard.R;
import com.example.eastpoint.swipecard.model.CardModel;
import com.example.eastpoint.swipecard.model.Orientations.Orientation;

import java.util.Random;

public class CardContainer extends AdapterView<ListAdapter> {
    public static final int INVALID_POINTER_ID = -1;
    private int mActivePointerId = INVALID_POINTER_ID;
    private static final double DISORDERED_MAX_ROTATION_RADIANS = Math.PI / 64;
    private int mNumberOfCards = -1;
    private final DataSetObserver mDataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
            clearStack();
            ensureFull();
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
            clearStack();
        }
    };
    private final Random mRandom = new Random();
    private final Rect boundsRect = new Rect();
    private final Rect childRect = new Rect();
    private final Matrix mMatrix = new Matrix();


    //TODO: determine max dynamically based on device speed
    private int mMaxVisible = 10;
    private GestureDetector mGestureDetector;
    private int mFlingSlop;
    private Orientation mOrientation;
    private ListAdapter mListAdapter;
    private float mLastTouchX;
    private float mLastTouchY;
    private View mTopCard;
    private int mTouchSlop;
    private int mGravity;
    private int mNextAdapterPosition;
    private boolean mDragging;

    public CardContainer(Context context) {
        super(context);

        setOrientation(Orientation.Ordered);
        setGravity(Gravity.CENTER);
        init();

    }

    public CardContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        initFromXml(attrs);
        init();
    }


    public CardContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initFromXml(attrs);
        init();
    }

    private void init() {
        ViewConfiguration viewConfiguration = ViewConfiguration.get(getContext());

        mFlingSlop = viewConfiguration.getScaledMinimumFlingVelocity();
        mTouchSlop = viewConfiguration.getScaledTouchSlop();
        mGestureDetector = new GestureDetector(getContext(), new GestureListener());
    }

    private void initFromXml(AttributeSet attr) {
        TypedArray a = getContext().obtainStyledAttributes(attr,
                R.styleable.CardContainer);

        setGravity(a.getInteger(R.styleable.CardContainer_android_gravity, Gravity.CENTER));
        int orientation = a.getInteger(R.styleable.CardContainer_orientation, 1);
        setOrientation(Orientation.fromIndex(orientation));

        a.recycle();
    }



    @Override
    public ListAdapter getAdapter() {
        return mListAdapter;
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        if (mListAdapter != null)
            mListAdapter.unregisterDataSetObserver(mDataSetObserver);

        clearStack();
        mTopCard = null;
        mListAdapter = adapter;
        mNextAdapterPosition = 0;
        adapter.registerDataSetObserver(mDataSetObserver);

        ensureFull();

        if (getChildCount() != 0) {
            mTopCard = getChildAt(getChildCount() - 1);
            mTopCard.setLayerType(LAYER_TYPE_HARDWARE, null);
        }
        mNumberOfCards = getAdapter().getCount();
        requestLayout();
    }

    private void ensureFull() {
        while (mNextAdapterPosition < mListAdapter.getCount() && getChildCount() < mMaxVisible) {
            View view = mListAdapter.getView(mNextAdapterPosition, null, this);
            view.setLayerType(LAYER_TYPE_SOFTWARE, null);

            addViewInLayout(view, 0, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
                    mListAdapter.getItemViewType(mNextAdapterPosition)), false);

            requestLayout();

            mNextAdapterPosition += 1;
        }
    }

    private void clearStack() {
        removeAllViewsInLayout();
        mNextAdapterPosition = 0;
        mTopCard = null;
    }

    public Orientation getOrientation() {
        return mOrientation;
    }

    public void setOrientation(Orientation orientation) {
        if (orientation == null)
            throw new NullPointerException("Orientation may not be null");
        if(mOrientation != orientation) {
            this.mOrientation = orientation;
            this.mOrientation = Orientation.Ordered;
            if(orientation == Orientation.Disordered) {
                for (int i = 0; i < getChildCount(); i++) {
                    View child = getChildAt(i);
                    child.setRotation(getDisorderedRotation());
                }
            }
            else {
                for (int i = 0; i < getChildCount(); i++) {
                    View child = getChildAt(i);
                    child.setRotation(0);
                }
            }
            requestLayout();
        }

    }

    private float getDisorderedRotation() {
        return (float) Math.toDegrees(mRandom.nextGaussian() * DISORDERED_MAX_ROTATION_RADIANS);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int requestedWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        int requestedHeight = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
        int childWidth, childHeight;


            childWidth = requestedWidth;
            childHeight = requestedHeight;


        int childWidthMeasureSpec, childHeightMeasureSpec;
        childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.AT_MOST);
        childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.AT_MOST);

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            assert child != null;
            child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        }
    }
    int top=0;
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        Log.d("Layout", "Child count: " + getChildCount());
        for (int i = 0; i < getChildCount(); i++) {
            Log.d("Layout", "Current index: " + i+" top: "+childRect.top);
            top = top+50;
            boundsRect.set(0, 0, getWidth(), getHeight());
            View view = getChildAt(i);
            int w, h;
            w = view.getMeasuredWidth();
            h = view.getMeasuredHeight();

            Gravity.apply(mGravity, w, h, boundsRect, childRect);

            view.layout(childRect.left, childRect.top + top, childRect.right, childRect.bottom);
            Log.d("Layout", "Top: " + view.getTop());
        }
    }


    @Override
    public View getSelectedView() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSelection(int position) {
        throw new UnsupportedOperationException();
    }

    public int getGravity() {
        return mGravity;
    }

    public void setGravity(int gravity) {
        mGravity = gravity;
    }

    public static class LayoutParams extends ViewGroup.LayoutParams {

        int viewType;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(int w, int h, int viewType) {
            super(w, h);
            this.viewType = viewType;
        }
    }

    private class GestureListener extends SimpleOnGestureListener{
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Log.d("Fling", "Fling with " + velocityX + ", " + velocityY);
            final View topCard = mTopCard;
            float dx = e2.getX() - e1.getX();
            float dy = e2.getY() - e1.getY();

            float targetX = topCard.getX();
            float targetY = topCard.getY();
            long duration = 0;

            if (Math.abs(dx) > mTouchSlop &&
                    Math.abs(velocityX) > Math.abs(velocityY) &&
                    Math.abs(velocityX) > mFlingSlop * 3) {


                boundsRect.set(0 - topCard.getWidth() - 100, 0 - topCard.getHeight() - 100, getWidth() + 100, getHeight() + 100);

                while (boundsRect.contains((int) targetX, (int) targetY)) {
                    targetX += velocityX / 10;
                    targetY += velocityY / 10;
                    duration += 100;
                }

                duration = Math.min(500, duration);

                mTopCard = getChildAt(getChildCount() - 2);
                CardModel cardModel = (CardModel)getAdapter().getItem(getChildCount() - 1);

                if(mTopCard != null)
                    mTopCard.setLayerType(LAYER_TYPE_HARDWARE, null);

                if (cardModel.getOnCardDismissedListener() != null) {
                    if ( targetX > 0 ) {
                        cardModel.getOnCardDismissedListener().onLike();
                    } else {
                        cardModel.getOnCardDismissedListener().onDislike();
                    }
                }

                topCard.animate()
                        .setDuration(duration)
                        .alpha(.75f)
                        .setInterpolator(new LinearInterpolator())
                        .x(targetX)
                        .y(targetY)
                        .rotation(Math.copySign(45, velocityX))
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                removeViewInLayout(topCard);
                                ensureFull();
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {
                                onAnimationEnd(animation);
                            }
                        });
                return true;
            }else if(Math.abs(dy) > mTouchSlop &&
                    Math.abs(velocityY) > Math.abs(velocityX) &&
                    Math.abs(velocityY) > mFlingSlop * 3){
                Log.d("Naresh","Scroll vertically");

                boundsRect.set(0 - topCard.getWidth() - 100, 0 - topCard.getHeight() - 100, getWidth() + 100, getHeight() + 100);

                while (boundsRect.contains((int) targetX, (int) targetY)) {
                    targetX += velocityX / 10;
                    targetY += velocityY / 10;
                    duration += 100;
                }

                duration = Math.min(500, duration);

                mTopCard = getChildAt(getChildCount() - 2);
                CardModel cardModel = (CardModel)getAdapter().getItem(getChildCount() - 1);

                if(mTopCard != null)
                    mTopCard.setLayerType(LAYER_TYPE_HARDWARE, null);

                topCard.animate()
                        .setDuration(duration)
                        .alpha(.75f)
                        .setInterpolator(new LinearInterpolator())
                        .x(targetX)
                        .y(targetY)
                        .rotation(Math.copySign(45, velocityX))
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                removeViewInLayout(topCard);
                                ensureFull();
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {
                                onAnimationEnd(animation);
                            }
                        });
                return true;


            } else
                return false;
        }

        @Override
        public void onLongPress(MotionEvent event) {
            Log.d("Naresh Test", "onLongPress: he..he...");
            // CardModel cardModel = (CardModel)getAdapter().getItem(getChildCount() - 1);
            // cardModel.flipCard();
        }
    }

    /**
     * A handler object, used for deferring UI operations.
     */
    private Handler mHandler = new Handler();

    /**
     * Whether or not we're showing the back of the card (otherwise showing the front).
     */
    private boolean mShowingBack = false;
    /*private void flipCard() {
        if (mShowingBack) {
           // getFragmentManager().popBackStack();
            return;
        }

        // Flip to the back.

        mShowingBack = true;

        // Create and commit a new fragment transaction that adds the fragment for the back of
        // the card, uses custom animations, and is part of the fragment manager's back stack.

        getFragmentManager()
                .beginTransaction()

                        // Replace the default fragment animations with animator resources representing
                        // rotations when switching to the back of the card, as well as animator
                        // resources representing rotations when flipping back to the front (e.g. when
                        // the system Back button is pressed).
                .setCustomAnimations(
                        R.animator.card_flip_right_in, R.animator.card_flip_right_out,
                        R.animator.card_flip_left_in, R.animator.card_flip_left_out)

                        // Replace any fragments currently in the container view with a fragment
                        // representing the next page (indicated by the just-incremented currentPage
                        // variable).
                .replace(R.id.container, new CardFlipActivity.CardBackFragment())

                        // Add this transaction to the back stack, allowing users to press Back
                        // to get to the front of the card.
                .addToBackStack(null)

                        // Commit the transaction.
                .commit();

        // Defer an invalidation of the options menu (on modern devices, the action bar). This
        // can't be done immediately because the transaction may not yet be committed. Commits
        // are asynchronous in that they are posted to the main thread's message loop.
//            mHandler.post(new Runnable() {
//                @Override
//                public void run() {
//                    invalidateOptionsMenu();
//                }
//            });
    }*/

    //@Override
    public void onBackStackChanged() {
       // mShowingBack = (getFragmentManager().getBackStackEntryCount() > 0);
    }
}
