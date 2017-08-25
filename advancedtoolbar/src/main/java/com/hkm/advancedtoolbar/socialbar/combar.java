package com.hkm.advancedtoolbar.socialbar;

import android.annotation.TargetApi;
import android.net.Uri;
import android.support.v4.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.support.annotation.LayoutRes;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.hkm.advancedtoolbar.R;

import java.util.List;


/**
 * Created by hesk on 24/7/15.
 */
public class combar extends FrameLayout implements View.OnClickListener {
    private static final String TAG = combar.class.getSimpleName();
    private Context context;
    private List<ResolveInfo> resolveInfoList;
    private String contextExcerpt = "nothing in here";
    protected String shareLink = "";
    private String title = "New discovery";
    private FragmentManager fragmentManager;

    public combar(Context context) {
        super(context);
        init(context);
    }

    public combar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public combar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context cont) {
        View rootView = getView(cont, R.layout.socialbar);
        context = cont;
        resolveInfoList = getSharingAppList();
        rootView.findViewById(Hg.facebook.Id()).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                shareByFacebook();
            }
        });
        rootView.findViewById(Hg.message.Id()).setOnClickListener(this);
        rootView.findViewById(Hg.whatsapp.Id()).setOnClickListener(this);
        rootView.findViewById(Hg.pintrest.Id()).setOnClickListener(this);
        rootView.findViewById(Hg.twitter.Id()).setOnClickListener(this);
        rootView.findViewById(R.id.social_bar_mail).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                shareByEmail();
            }
        });
    }

    public combar connectAlert(FragmentManager fragmentm) {
        this.fragmentManager = fragmentm;
        return this;
    }

    public static combar with(Context h) {
        return new combar(h);
    }

    private String template;

    public combar setFormat(String template) {
        this.template = template;
        return this;
    }

    public combar setShareContent(String title, String except, String link) {
        this.title = title;
        shareLink = link;
        if (template == null) {
            contextExcerpt = "I just read an article about " + title + ", check it out @ " + link;
        } else {

            contextExcerpt = String.format(template, title, except, link);
        }
        return this;
    }


    protected View getView(final Context m, final @LayoutRes int layout) {
        return LayoutInflater.from(m).inflate(layout, this);
    }

    @Override
    public void onClick(View v) {
        try {
            Hg instance_icon = Hg.reverseId(v.getId());
            int app_location = getPackageNameIndex(instance_icon);
            if (app_location > -1) {
                share(app_location);
            } else {
                Log.e(TAG, String.format("the application is not installed; app_location = %d", app_location));
                instance_icon.alert(fragmentManager, context);
            }

        } catch (Exception e) {
            Log.e(TAG, "failed to process share request", e);
        }
    }

    private void shareByFacebook() {
        try {
            Intent intent1 = new Intent();
            //intent1.setClassName("com.facebook.katana", "com.facebook.katana.activity.composer.ImplicitShareIntentHandler");
            intent1.setPackage("com.facebook.katana");
            intent1.setAction("android.intent.action.SEND");
            intent1.setType("text/plain");
            intent1.putExtra("android.intent.extra.TEXT", shareLink);
            context.startActivity(intent1);
        } catch (Exception e) {
            Log.w(TAG, "failed to launch facebook, fallback to sharing via web", e);
            // If we failed (not native FB app installed), try share through SEND
            Intent intent = new Intent(Intent.ACTION_SEND);
            String sharerUrl = "https://www.facebook.com/sharer/sharer.php?u=" + shareLink;
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(sharerUrl));
            context.startActivity(intent);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void share(final int position_app) {
        ActivityInfo activity = resolveInfoList.get(position_app).activityInfo;
        ComponentName nameComponent = new ComponentName(activity.applicationInfo.packageName, activity.name);

        final Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, contextExcerpt);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, title);
        Intent newIntent = (Intent) shareIntent.clone();
        //| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        newIntent.setComponent(nameComponent);
        newIntent.setPackage(activity.packageName);

        context.startActivity(newIntent);
    }

    protected void shareByEmail() {
        Intent mailIntent = new Intent(Intent.ACTION_SENDTO);
        mailIntent.setData(Uri.parse("mailto:"));
        mailIntent.putExtra(Intent.EXTRA_SUBJECT, title);
        mailIntent.putExtra(Intent.EXTRA_TEXT, contextExcerpt);
        context.startActivity(Intent.createChooser(mailIntent, title));
    }

    private int getPackageNameIndex(final Hg compareHg) {
        for (int idx = 0; idx< resolveInfoList.size(); idx++) {
            ResolveInfo resolveInfo = resolveInfoList.get(idx);
            if (resolveInfo.activityInfo.applicationInfo.packageName.contains(compareHg.getPackageName())) {
                return idx;
            }
        }

        return -1;
    }

    private List<ResolveInfo> getSharingAppList() {

        final PackageManager pm = context.getPackageManager();
        final Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "action shared");
        return pm.queryIntentActivities(shareIntent, 0);
    }
}
