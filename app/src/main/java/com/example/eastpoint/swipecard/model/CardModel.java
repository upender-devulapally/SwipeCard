/**
 * AndTinder v0.1 for Android
 *
 * @Author: Enrique López Mañas <eenriquelopez@gmail.com>
 * http://www.lopez-manas.com
 *
 * TAndTinder is a native library for Android that provide a
 * Tinder card like effect. A card can be constructed using an
 * image and displayed with animation effects, dismiss-to-like
 * and dismiss-to-unlike, and use different sorting mechanisms.
 *
 * AndTinder is compatible with API Level 13 and upwards
 *
 * @copyright: Enrique López Mañas
 * @license: Apache License 2.0
 */

package com.example.eastpoint.swipecard.model;

import android.app.Activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;

import com.example.eastpoint.swipecard.CardFlipActivity;
import com.example.eastpoint.swipecard.R;

public class CardModel {

	private String   title;
	private String   description;
	private int   position;
	private Drawable cardImageDrawable;
	private Drawable cardLikeImageDrawable;
	private Drawable cardDislikeImageDrawable;

	private OnCardDismissedListener mOnCardDismissedListener = null;

	private OnClickListener mOnClickListener = null;

	/**
	 * A handler object, used for deferring UI operations.
	 */
	private Handler mHandler = new Handler();

	/**
	 * Whether or not we're showing the back of the card (otherwise showing the front).
	 */
	private boolean mShowingBack = false;

	/*public void flipCard() {
		if (mShowingBack) {
			getFragmentManager().popBackStack();
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
				.addToBackStack(null);

						// Commit the transaction.
				//.commit();

		// Defer an invalidation of the options menu (on modern devices, the action bar). This
		// can't be done immediately because the transaction may not yet be committed. Commits
		// are asynchronous in that they are posted to the main thread's message loop.
		*//*mHandler.post(new Runnable() {
			@Override
			public void run() {
				invalidateOptionsMenu();
			}
		});*//*
	}

	@Override
	public void onBackStackChanged() {
		mShowingBack = (getFragmentManager().getBackStackEntryCount() > 0);

		// When the back stack changes, invalidate the options menu (action bar).
		invalidateOptionsMenu();
	}*/

	public interface OnCardDismissedListener {
		void onLike();
		void onDislike();
	}

	public interface OnClickListener {
		void OnClickListener();
	}

	public CardModel() {
		this(null, null,0, (Drawable)null);
	}

	public CardModel(String title, String description,int position, Drawable cardImage) {
		this.title = title;
		this.description = description;
		this.position = position;
		this.cardImageDrawable = cardImage;
	}

	public CardModel(String title, String description, Bitmap cardImage) {
		this.title = title;
		this.description = description;
		this.cardImageDrawable = new BitmapDrawable(null, cardImage);
	}

	public String getCardTitle() {
		return title;
	}

	public void setCardTitle(String title) {
		this.title = title;
	}

	public int getPosition() {
		return position;
	}

	public void setCardTitle(int position) {
		this.position = position;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Drawable getCardImageDrawable() {
		return cardImageDrawable;
	}

	public void setCardImageDrawable(Drawable cardImageDrawable) {
		this.cardImageDrawable = cardImageDrawable;
	}

	public Drawable getCardLikeImageDrawable() {
		return cardLikeImageDrawable;
	}

	public void setCardLikeImageDrawable(Drawable cardLikeImageDrawable) {
		this.cardLikeImageDrawable = cardLikeImageDrawable;
	}

	public Drawable getCardDislikeImageDrawable() {
		return cardDislikeImageDrawable;
	}

	public void setCardDislikeImageDrawable(Drawable cardDislikeImageDrawable) {
		this.cardDislikeImageDrawable = cardDislikeImageDrawable;
	}

	public void setOnCardDismissedListener( OnCardDismissedListener listener ) {
		this.mOnCardDismissedListener = listener;
	}

	public OnCardDismissedListener getOnCardDismissedListener() {
		return this.mOnCardDismissedListener;
	}


	public void setOnClickListener( OnClickListener listener ) {
		this.mOnClickListener = listener;
	}

	public OnClickListener getOnClickListener() {
		return this.mOnClickListener;
	}
}
