package com.hkm.advancedtoolbar.V5;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
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
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.hkm.advancedtoolbar.R;
import com.hkm.advancedtoolbar.Util.TitleStorage;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by hesk on 28/10/15.
 */
public class BeastBar {
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({SINGLELINE, MULTILINE})
    public @interface ToolbarTitleMode {}
    public static final int SINGLELINE = 1;
    public static final int MULTILINE = 2;

    private Context mContext;
    private Toolbar container;
    private TextView mtv;
    private ImageView mImage;
    private FrameLayout startBarButtonContainer, endBarButtonContainer, centerContainer;
    private TextView startBarButtonLabel, endBarButtonLabel;
    private ImageView startBarButtonImageView, endBarButtonImageView;
    private Animation
            main_logo_in;
    private Animation main_logo_out;
    private Animation back_in;
    private Animation back_out;
    private Animation back_in_from_right;
    private Animation back_out_to_right;
    private boolean isCompanyLogoShown, isTitleShown, isSearchButtonShown;
    private Builder setup;
    private int leftSide = 0, rightSide = 0;
    private TitleStorage mTitle;

    public static class Builder {
        private @ToolbarTitleMode int title_line_config = SINGLELINE;
        private int
                ic_company, ic_search, ic_back, ic_background,
                tb_textsize = 0, tb_title_color = 0,
                animation_duration_logo = -1,
                animation_duration = -1;
        private Drawable companyIconDrawable = null;
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

        public Builder setCompanyIconDrawable(Drawable companyIconDrawable) {
            this.companyIconDrawable = companyIconDrawable;
            this.ic_company = 0;
            return this;
        }

        public Builder companyIcon(@DrawableRes final int res) {
            this.ic_company = res;
            this.companyIconDrawable = null;
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

        public Builder setTitleLine(@ToolbarTitleMode int lines) {
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
        actionbar.setDisplayHomeAsUpEnabled(false);
        original.setBackgroundResource(beastbuilder.ic_background);

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
        isSearchButtonShown = false;
        View v = LayoutInflater.from(mContext).inflate(R.layout.beastbar, null, false);
        startBarButtonContainer = (FrameLayout) v.findViewById(R.id.left_bar_button_container);
        endBarButtonContainer = (FrameLayout) v.findViewById(R.id.right_bar_button_container);
        centerContainer = v.findViewById(R.id.centerContainer);
        startBarButtonLabel = (TextView) v.findViewById(R.id.left_bar_button_label);
        endBarButtonLabel = (TextView) v.findViewById(R.id.right_bar_button_label);
        mtv = (TextView) v.findViewById(R.id.ios_actionbar_title);
        mImage = (ImageView) v.findViewById(R.id.logo_k);
        endBarButtonImageView = (ImageView) v.findViewById(R.id.right_bar_button_image_button);
        startBarButtonImageView = (ImageView) v.findViewById(R.id.left_bar_button_image_view);
        this.container.addView(v);
        main_logo_in = AnimationUtils.loadAnimation(mContext, animaionset.slideLogo.getInAnimation());
        main_logo_out = AnimationUtils.loadAnimation(mContext, animaionset.slideLogo.getOutAnimation());
        Animation title_in = AnimationUtils.loadAnimation(mContext, animaionset.slideLogo.getInAnimation());
        Animation title_out = AnimationUtils.loadAnimation(mContext, animaionset.slideLogo.getOutAnimation());
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
        startBarButtonContainer.setVisibility(View.INVISIBLE);
        switch (setup.title_line_config) {
            case SINGLELINE:
                mtv.setSingleLine(true);
                break;
            default:
                mtv.setSingleLine(false);
                mtv.setMaxLines(setup.title_line_config);
                break;
        }

        if (setup.tb_textsize > 0) {
            mtv.setTextSize(TypedValue.COMPLEX_UNIT_PX, mContext.getResources().getDimensionPixelSize(setup.tb_textsize));
        }
        mtv.requestLayout();

        if (setup.ic_search != 0) {
            setEndBarButtonIcon(setup.ic_search);
            isSearchButtonShown = true;
        }

        if (setup.companyIconDrawable != null) {
            setLogoDrawable(setup.companyIconDrawable);
        } else if (setup.ic_company != 0) {
            setLogoDrawableResource(setup.ic_company);
        }

        if (setup.title_default != null) {
            mtv.setVisibility(View.VISIBLE);
            mImage.setVisibility(View.GONE);
            mtv.setText(setup.title_default);
            isCompanyLogoShown = false;
            isTitleShown = true;
        }

        if (setup.ic_back != 0) {
            setStartBarButtonIcon(setup.ic_back);
        }

        startBarButtonContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                leftSide = startBarButtonContainer.getWidth();
                removeLayoutListener(startBarButtonContainer, this);
            }
        });

        endBarButtonContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                rightSide = endBarButtonContainer.getWidth();
                removeLayoutListener(endBarButtonContainer, this);
            }
        });

        if (!isSearchButtonShown) {
            displayRightFirstIcon(false, false);
        }

        if (setup.save_title_navigation) {
            mTitle = new TitleStorage();
        }
    }

    public void displayRightFirstIcon(boolean b, boolean withAnimation) {
        final displayManagement dm = new displayManagement(b, withAnimation, endBarButtonContainer);
    }


    private void removeLayoutListener(View layout, ViewTreeObserver.OnGlobalLayoutListener lb) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            layout.getViewTreeObserver().removeOnGlobalLayoutListener(lb);
        } else {
            layout.getViewTreeObserver().removeGlobalOnLayoutListener(lb);
        }
        mtv.setMaxWidth(size.x - leftSide - rightSide);
    }

    public ImageView getLogoImageView() {
        return mImage;
    }

    public void setLogoDrawableResource(@DrawableRes int resourceId) {
        mImage.setImageResource(resourceId);
        mtv.setVisibility(View.INVISIBLE);
        isCompanyLogoShown = true;
        isTitleShown = false;
    }

    public void setLogoDrawable(Drawable resource) {
        mImage.setImageDrawable(resource);
        mtv.setVisibility(View.INVISIBLE);
        isCompanyLogoShown = true;
        isTitleShown = false;
    }

    public void setLogoOnClickListener(View.OnClickListener onClickListener) {
        mImage.setClickable(true);
        mImage.setOnClickListener(onClickListener);
    }

    public BeastBar setFindIconFunc(@Nullable final onButtonPressListener func) {
        if (func == null) {
            if (isSearchButtonShown) {
                isSearchButtonShown = false;
                mayCancelAnimation(endBarButtonContainer);
                back_out_to_right.setAnimationListener(new ListenerAnimation() {
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        endBarButtonContainer.setVisibility(View.INVISIBLE);
                    }
                });
                endBarButtonContainer.startAnimation(back_out_to_right);
                endBarButtonContainer.setOnClickListener(null);
            }
        } else {
            if (!isSearchButtonShown) {
                isSearchButtonShown = true;
                mayCancelAnimation(endBarButtonContainer);
                back_in_from_right.setAnimationListener(new ListenerAnimation() {
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        endBarButtonContainer.setVisibility(View.VISIBLE);

                    }
                });
                endBarButtonContainer.startAnimation(back_in_from_right);
            }
            endBarButtonContainer.setOnClickListener(new View.OnClickListener() {
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

    public boolean isCompanyLogoShown() {
        return isCompanyLogoShown;
    }

    public boolean isTitleShown() {
        return isTitleShown;
    }

    public boolean isSearchButtonShown() {
        return isSearchButtonShown;
    }

    public BeastBar hideStartBarButton() {
        startBarButtonContainer.setVisibility(View.GONE);
        return this;
    }

    public BeastBar showStartBarButton() {
        startBarButtonContainer.setVisibility(View.VISIBLE);
        return this;
    }

    public BeastBar hideEndBarButton() {
        endBarButtonContainer.setVisibility(View.GONE);
        return this;
    }

    public BeastBar showEndBarButton() {
        endBarButtonContainer.setVisibility(View.VISIBLE);
        return this;
    }

    public @NonNull FrameLayout getStartBarButtonContainer() {
        return startBarButtonContainer;
    }

    public TextView getStartBarButtonLabel() {
        return startBarButtonLabel;
    }

    public BeastBar setStartBarButtonText(String buttonText) {
        startBarButtonLabel.setText(buttonText);
        return this;
    }

    public ImageView getStartBarButtonImageView() {
        return startBarButtonImageView;
    }

    public BeastBar setStartBarButtonIcon(@DrawableRes int resId) {
        startBarButtonImageView.setImageResource(resId);
        return this;
    }

    public BeastBar setStartBarButtonIcon(Drawable drawable) {
        startBarButtonImageView.setImageDrawable(drawable);
        return this;
    }

    public BeastBar setStartBarButtonOnClickListener(@Nullable View.OnClickListener onClickListener) {
        mayCancelAnimation(startBarButtonImageView);
        startBarButtonContainer.setOnClickListener(onClickListener);
        return this;
    }

    public TextView getTitleLabel() {
        return mtv;
    }

    public @NonNull FrameLayout getEndBarButtonContainer() {
        return endBarButtonContainer;
    }

    @Deprecated
    public FrameLayout getRightBarButtonContainer() {
        return endBarButtonContainer;
    }

    public TextView getEndBarButtonLabel() {
        return endBarButtonLabel;
    }

    @Deprecated
    public TextView getRightBarButtonLabel() {
        return getEndBarButtonLabel();
    }

    public BeastBar setEndBarButtonText(String buttonText) {
        endBarButtonLabel.setText(buttonText);
        return this;
    }

    public ImageView getEndBarButtonImageView() {
        return endBarButtonImageView;
    }

    public BeastBar setEndBarButtonIcon(@DrawableRes int resId) {
        endBarButtonImageView.setImageResource(resId);
        return this;
    }

    public BeastBar setEndBarButtonIcon(Drawable drawable) {
        endBarButtonImageView.setImageDrawable(drawable);
        return this;
    }

    public BeastBar setEndBarButtonOnClickListener(@Nullable View.OnClickListener onClickListener) {
        endBarButtonContainer.setOnClickListener(onClickListener);
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

    public FrameLayout getCenterContainer() {
        return centerContainer;
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


    public final void onSaveInstaceState(@NonNull Bundle out) {
        if (mTitle != null) mTitle.onStateInstaceState(out);
        out.putBoolean(TitleStorage.IS_LOGOSHOWN, isCompanyLogoShown);
        out.putBoolean(TitleStorage.IS_SEARCHSHOWN, isSearchButtonShown);
        out.putBoolean(TitleStorage.IS_TITLESHOWN, isTitleShown);
    }


    public final void onRestoreInstanceState(@Nullable Bundle input) {
        if (input == null) return;
        if (mTitle != null) mTitle.onRestoreInstanceState(input);
        isTitleShown = input.getBoolean(TitleStorage.IS_TITLESHOWN, isTitleShown);
        isCompanyLogoShown = input.getBoolean(TitleStorage.IS_LOGOSHOWN, isCompanyLogoShown);
        isSearchButtonShown = input.getBoolean(TitleStorage.IS_SEARCHSHOWN, isSearchButtonShown);

        /**
         * there we can only take care of two features in this library
         */
        if (isTitleShown && mTitle != null) setActionTitle(mTitle.getCurrentTitle());

        if (isCompanyLogoShown) showMainLogo();
    }
}
