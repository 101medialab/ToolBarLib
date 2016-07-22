package com.hkm.advancedtoolbar.V5;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hkm.advancedtoolbar.R;
import com.hkm.advancedtoolbar.Util.TitleStorage;

/**
 * Created by hesk on 28/10/15.
 */
public class BeastBar {
    public static int SINGLELINE = 1, MULTILINE = 2;
    private String text_title;
    private Context mContext;
    private Toolbar container;
    private TextView mtv;
    private ImageView mImage;
    private RelativeLayout mBackground;
    private LinearLayout mRightContainer, mLeftContainer;
    private ImageButton mSearchButton, mTopLeftButton;
    private Runnable mFindFunction;
    private Animation
            main_logo_in,
            main_logo_out,
            title_in,
            title_out,
            back_in,
            back_out,
            back_in_from_right,
            back_out_to_right;
    private boolean isCompanyLogoShown, isTitleShown, isBackButtonShown, isSearchButtonShown;
    private Builder setup;
    private int leftSide = 0, rightSide = 0;
    private TitleStorage mTitle;

    public static class Builder {
        private int
                ic_company, ic_search, ic_back, ic_background,
                tb_textsize = 0, tb_title_color = 0, title_line_config = 1,
                animation_duration_logo = -1,
                animation_duration = -1;
        private Typeface typeface;
        private String title_default;
        private boolean enable_logo_anim = true;
        private boolean save_title_navigation = false;

        public Builder() {

        }

        public Builder setFontFace(@NonNull Context mContext, @NonNull final String fontNameInFontFolder) {
            typeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/" + fontNameInFontFolder);
            return this;
        }

        public Builder companyIcon(@DrawableRes final int res) {
            this.ic_company = res;
            return this;
        }

        public Builder enableLogoAnimation(boolean res) {
            this.enable_logo_anim = res;
            return this;
        }

        public Builder search(@DrawableRes final int res) {
            this.ic_search = res;
            return this;
        }

        public Builder back(@DrawableRes final int res) {
            this.ic_back = res;
            return this;
        }

        public Builder background(@DrawableRes final int res) {
            this.ic_background = res;
            return this;
        }

        public Builder setToolBarTitleSize(@DimenRes final int res) {
            this.tb_textsize = res;
            return this;
        }

        public Builder setToolBarTitleColor(@ColorRes final int res) {
            this.tb_title_color = res;
            return this;
        }

        public Builder defaultTitle(String title) {
            this.title_default = title;
            return this;
        }

        public Builder setTitleLine(int lines) {
            this.title_line_config = lines;
            return this;
        }

        public Builder setAnimationDuration(int millisec_time) {
            this.animation_duration = millisec_time;
            return this;
        }

        public Builder setLogoAnimationDuration(int millisec_time) {
            this.animation_duration_logo = millisec_time;
            return this;
        }

        public Builder enableTitleHistory(boolean save) {
            this.save_title_navigation = save;
            return this;
        }

    }

    private onButtonPressListener mButtonBack;

    public interface onButtonPressListener {
        /**
         * @param previousTitleSteps the previous title to be found in the history or otherwise it is nothing
         * @return true to allow the main logo to show
         */
        boolean onBackPress(final int previousTitleSteps);

        void onSearchPress();
    }

    private Point size = new Point();

    public static BeastBar withToolbar(AppCompatActivity res, Toolbar original, final Builder beastbuilder) {
        Display display = res.getWindowManager().getDefaultDisplay();
        res.setSupportActionBar(original);
        ActionBar actionbar = res.getSupportActionBar();
        actionbar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionbar.setDisplayShowHomeEnabled(false);
        actionbar.setDefaultDisplayHomeAsUpEnabled(false);
        original.setBackgroundResource(beastbuilder.ic_background);
        View homeIcon = res.findViewById(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? android.R.id.home : android.R.id.home);
        // ((View) homeIcon.getParent()).setLayoutParams(new LinearLayout.LayoutParams(0, 0));
        //  ((View) homeIcon).setVisibility(View.GONE);
        final BeastBar bb = new BeastBar(res);
        bb.setToolBar(original);
        bb.setup = beastbuilder;
        display.getSize(bb.size);
        bb.init();

        return bb;
    }

    public BeastBar(Context res) {
        this.mContext = res;
    }

    private BeastBar setToolBar(Toolbar tb) {
        this.container = tb;
        return this;
    }


    private void animationTitle() {
        if (!isTitleShown) {
            isTitleShown = true;
            if (setup.enable_logo_anim) {
                mayCancelAnimation(mtv);
                main_logo_in.setAnimationListener(new ListenerAnimation() {
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mtv.setVisibility(View.VISIBLE);
                    }
                });
                mtv.startAnimation(main_logo_in);
            } else {
                mtv.setVisibility(View.VISIBLE);
            }
        }
        if (isCompanyLogoShown) {
            isCompanyLogoShown = false;
            if (setup.enable_logo_anim) {
                mayCancelAnimation(mImage);
                main_logo_out.setAnimationListener(new ListenerAnimation() {
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mImage.setVisibility(View.INVISIBLE);
                    }
                });
                mImage.startAnimation(main_logo_out);
            } else {
                mImage.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void init() {
        isBackButtonShown = false;
        isSearchButtonShown = false;
        View v = LayoutInflater.from(mContext).inflate(R.layout.beastbar, null, false);
        mLeftContainer = (LinearLayout) v.findViewById(R.id.left_container);
        mBackground = (RelativeLayout) v.findViewById(R.id.ios_background);
        mRightContainer = (LinearLayout) v.findViewById(R.id.right_container);
        mtv = (TextView) v.findViewById(R.id.ios_actionbar_title);
        mImage = (ImageView) v.findViewById(R.id.logo_k);
        mSearchButton = (ImageButton) v.findViewById(R.id.ios_find_icon);
        mTopLeftButton = (ImageButton) v.findViewById(R.id.ios_back_button);
        this.container.addView(v);
        main_logo_in = AnimationUtils.loadAnimation(mContext, animaionset.slideLogo.getInAnimation());
        main_logo_out = AnimationUtils.loadAnimation(mContext, animaionset.slideLogo.getOutAnimation());
        title_in = AnimationUtils.loadAnimation(mContext, animaionset.slideLogo.getInAnimation());
        title_out = AnimationUtils.loadAnimation(mContext, animaionset.slideLogo.getOutAnimation());
        back_in = AnimationUtils.loadAnimation(mContext, animaionset.slideText.getInAnimation());
        back_out = AnimationUtils.loadAnimation(mContext, animaionset.slideText.getOutAnimation());
        back_in_from_right = AnimationUtils.loadAnimation(mContext, R.anim.back_in_from_right);
        back_out_to_right = AnimationUtils.loadAnimation(mContext, R.anim.back_out_to_right);
        if (setup.animation_duration > -1) {
            title_in.setDuration(setup.animation_duration);
            title_out.setDuration(setup.animation_duration);
            back_in.setDuration(setup.animation_duration);
            back_out.setDuration(setup.animation_duration);
            back_in_from_right.setDuration(setup.animation_duration);
            back_out_to_right.setDuration(setup.animation_duration);
            main_logo_in.setDuration(setup.animation_duration);
            main_logo_out.setDuration(setup.animation_duration);
        }
        if (setup.animation_duration_logo > -1) {
            main_logo_in.setDuration(setup.animation_duration_logo);
            main_logo_out.setDuration(setup.animation_duration_logo);
        }
        if (setup.tb_title_color != 0) {
            final int color_title = ContextCompat.getColor(mContext, setup.tb_title_color);
            mtv.setTextColor(color_title);
        }
        if (setup.typeface != null) {
            mtv.setTypeface(setup.typeface);
        }
        mTopLeftButton.setVisibility(View.INVISIBLE);
        if (setup.title_line_config == SINGLELINE) {
            mtv.setSingleLine(true);
        } else {
            mtv.setSingleLine(false);
            mtv.setMaxLines(setup.title_line_config);
        }

        if (setup.tb_textsize > 0) {
            mtv.setTextSize(TypedValue.COMPLEX_UNIT_PX, mContext.getResources().getDimensionPixelSize(setup.tb_textsize));
        }
        mtv.requestLayout();

        if (setup.ic_search != 0) {
            mSearchButton.setImageResource(setup.ic_search);
            isSearchButtonShown = true;
        }

        if (setup.ic_company != 0) {
            mImage.setImageResource(setup.ic_company);
            mtv.setVisibility(View.INVISIBLE);
            isCompanyLogoShown = true;
            isTitleShown = false;
        }

        if (setup.title_default != null) {
            mtv.setVisibility(View.VISIBLE);
            mImage.setVisibility(View.GONE);
            mtv.setText(setup.title_default);
            isCompanyLogoShown = false;
            isTitleShown = true;
        }

        if (setup.ic_back != 0) {
            mTopLeftButton.setImageResource(setup.ic_back);
        }

        mLeftContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                leftSide = mLeftContainer.getWidth();
                removeLayoutListener(mLeftContainer, this);
            }
        });

        mRightContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                rightSide = mRightContainer.getWidth();
                removeLayoutListener(mRightContainer, this);
            }
        });

        if (!isSearchButtonShown) {
            displayRightFirstIcon(false, false);
        }

        if (setup.save_title_navigation) {
            mTitle = new TitleStorage();
        }
        // mBackground.setBackgroundResource(setup.ic_background);
    }

    public void displayRightFirstIcon(boolean b, boolean withAnimation) {
        final displayManagement dm = new displayManagement(b, withAnimation, mSearchButton);
    }

    private void removeLayoutListener(View layout, ViewTreeObserver.OnGlobalLayoutListener lb) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            layout.getViewTreeObserver().removeOnGlobalLayoutListener(lb);
        } else {
            layout.getViewTreeObserver().removeGlobalOnLayoutListener(lb);
        }
        mtv.setMaxWidth(size.x - leftSide - rightSide);
    }

    public BeastBar setFindIconFunc(@Nullable final onButtonPressListener func) {
        if (func == null) {
            if (isSearchButtonShown) {
                isSearchButtonShown = false;
                mayCancelAnimation(mSearchButton);
                back_out_to_right.setAnimationListener(new ListenerAnimation() {
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mSearchButton.setVisibility(View.INVISIBLE);
                    }
                });
                mSearchButton.startAnimation(back_out_to_right);
                mSearchButton.setOnClickListener(null);
            }
        } else {
            if (!isSearchButtonShown) {
                isSearchButtonShown = true;
                mayCancelAnimation(mSearchButton);
                back_in_from_right.setAnimationListener(new ListenerAnimation() {
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mSearchButton.setVisibility(View.VISIBLE);

                    }
                });
                mSearchButton.startAnimation(back_in_from_right);
            }
            mSearchButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    func.onSearchPress();
                }
            });
        }
        return this;
    }


    private static void mayCancelAnimation(View anything) {
        if (anything.getAnimation() != null) {
            anything.getAnimation().cancel();
        }
    }


    public BeastBar setActionTitle(final String title) {
        mtv.setText(title);
        if (setup.save_title_navigation) {
            mTitle.saveTitle(title);
        }
        animationTitle();
        return this;
    }

    /**
     * @return the total length of the history
     */
    public int titlePopBack() {
        if (setup.save_title_navigation && mTitle.getHistorySteps() > 1) {
            final String history_title = mTitle.popback();
            mtv.setText(history_title);
            animationTitle();
            return mTitle.getHistorySteps();
        }
        return -1;
    }

    public void resetTitleHistory() {
        if (setup.save_title_navigation) {
            mTitle.reset();
        }
    }

    public BeastBar showMainLogo() {
        if (!isCompanyLogoShown) {
            isCompanyLogoShown = true;
            if (setup.enable_logo_anim) {
                mayCancelAnimation(mImage);
                main_logo_in.setAnimationListener(new ListenerAnimation() {
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mImage.setVisibility(View.VISIBLE);
                    }
                });
                mImage.startAnimation(main_logo_in);
            } else {
                mImage.setVisibility(View.VISIBLE);
            }
        }
        if (isTitleShown) {
            isTitleShown = false;
            if (setup.enable_logo_anim) {
                mayCancelAnimation(mtv);
                main_logo_out.setAnimationListener(new ListenerAnimation() {
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mtv.setVisibility(View.INVISIBLE);
                    }
                });
                mtv.startAnimation(main_logo_out);
            } else {
                mtv.setVisibility(View.INVISIBLE);
            }
        }
        return this;
    }

    /* search button visibility */
    public void showSearchButton() {
        mSearchButton.setVisibility(View.VISIBLE);
    }

    public void hideSearchButton() {
        mSearchButton.setVisibility(View.GONE);
    }

    public boolean isBackButtonShown() {
        return isBackButtonShown;
    }

    public boolean isCompanyLogoShown() {
        return isCompanyLogoShown;
    }

    public boolean isTitleShown() {
        return isTitleShown;
    }

    public boolean isSearchButtonShown() {
        return isSearchButtonShown;
    }

    public BeastBar setBackIconFunc(@Nullable final onButtonPressListener func) {
        if (func == null) {
            if (isBackButtonShown) {
                isBackButtonShown = false;
                mayCancelAnimation(mTopLeftButton);
                back_out.setAnimationListener(new ListenerAnimation() {
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mTopLeftButton.setVisibility(View.INVISIBLE);

                    }
                });
                mTopLeftButton.startAnimation(back_out);
                mTopLeftButton.setOnClickListener(null);
            }
        } else {
            if (!isBackButtonShown) {
                isBackButtonShown = true;
                mayCancelAnimation(mTopLeftButton);
                back_in.setAnimationListener(new ListenerAnimation() {
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mTopLeftButton.setVisibility(View.VISIBLE);

                    }
                });
                mTopLeftButton.startAnimation(back_in);
            }
            mTopLeftButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    func.onBackPress(titlePopBack());
                }
            });
        }
        return this;
    }

    private class enhancedAnimation extends ListenerAnimation {
        private View target;


        public enhancedAnimation(View target) {
            this.target = target;
        }


        @Override
        public void onAnimationEnd(Animation animation) {
            target.setVisibility(View.GONE);
        }


    }

    private class displayManagement {
        private boolean shown, withanimation;
        private View display_target;

        public displayManagement(
                final boolean shown,
                final boolean withanimation,
                final View target) {
            this.shown = shown;
            this.withanimation = withanimation;
            this.display_target = target;
            init();
        }

        private void init() {
            if (!withanimation) {
                if (shown) {
                    if (display_target.getVisibility() == View.GONE) {
                        display_target.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (display_target.getVisibility() == View.VISIBLE) {
                        display_target.setVisibility(View.GONE);
                    }
                }
            } else {
                if (shown) {
                    if (display_target.getVisibility() == View.GONE) {
                        display_target.setVisibility(View.VISIBLE);
                        mayCancelAnimation(display_target);
                        display_target.startAnimation(main_logo_in);
                    }
                } else {
                    if (display_target.getVisibility() == View.VISIBLE) {
                        mayCancelAnimation(display_target);
                        display_target.getAnimation().setAnimationListener(new enhancedAnimation(display_target));
                        display_target.startAnimation(main_logo_out);
                    }
                }
            }
        }
    }


    private class ListenerAnimation implements Animation.AnimationListener {
        /**
         * <p>Notifies the end of the animation. This callback is not invoked
         * for animations with repeat count set to INFINITE.</p>
         *
         * @param animation The animation which reached its end.
         */
        @Override
        public void onAnimationEnd(Animation animation) {

        }

        /**
         * <p>Notifies the repetition of the animation.</p>
         *
         * @param animation The animation which was repeated.
         */
        @Override
        public void onAnimationRepeat(Animation animation) {

        }

        /**
         * <p>Notifies the start of the animation.</p>
         *
         * @param animation The started animation.
         */
        @Override
        public void onAnimationStart(Animation animation) {

        }
    }

    public TitleStorage getTitleStorageInstance() {
        return mTitle;
    }


    public final void onStateInstaceState(Bundle out) {
        if (mTitle != null) mTitle.onStateInstaceState(out);
        out.putBoolean(TitleStorage.IS_LOGOSHOWN, isCompanyLogoShown);
        out.putBoolean(TitleStorage.IS_SEARCHSHOWN, isSearchButtonShown);
        out.putBoolean(TitleStorage.IS_TITLESHOWN, isTitleShown);
        out.putBoolean(TitleStorage.IS_BACKSHOWN, isBackButtonShown);
    }


    public final void onRestoreInstanceState(@Nullable Bundle input) {
        if (input == null) return;
        if (mTitle != null) mTitle.onRestoreInstanceState(input);
        isTitleShown = input.getBoolean(TitleStorage.IS_TITLESHOWN, isTitleShown);
        isCompanyLogoShown = input.getBoolean(TitleStorage.IS_LOGOSHOWN, isCompanyLogoShown);
        isSearchButtonShown = input.getBoolean(TitleStorage.IS_SEARCHSHOWN, isSearchButtonShown);
        isBackButtonShown = input.getBoolean(TitleStorage.IS_BACKSHOWN, isBackButtonShown);

        /**
         * there we can only take care of two features in this library
         */
        if (isTitleShown && mTitle != null) setActionTitle(mTitle.getCurrentTitle());

        if (isCompanyLogoShown) showMainLogo();


    }
}
