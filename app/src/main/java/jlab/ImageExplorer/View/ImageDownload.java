package jlab.ImageExplorer.View;

import android.view.View;
import android.content.Context;
import android.widget.ImageView;
import android.util.AttributeSet;
import android.app.DownloadManager;
import android.graphics.drawable.AnimationDrawable;

import jlab.ImageExplorer.R;
import jlab.ImageExplorer.Resource.FileResource;
import jlab.ImageExplorer.Resource.RemoteFile;
import jlab.ImageExplorer.Service.ResourceDownloaderService;

/*
 * Created by Javier on 25/9/2016.
 */
public class ImageDownload extends ImageView implements View.OnClickListener {
    public FileResource resource;
    public DownloadManager dMgr;

    public ImageDownload(Context context) {
        super(context);
        setOnClickListener(this);
        setBackgroundResource(R.drawable.down_img_seq);
    }

    public ImageDownload(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnClickListener(this);
        setBackgroundResource(R.drawable.down_img_seq);
    }

    public ImageDownload(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOnClickListener(this);
        setBackgroundResource(R.drawable.down_img_seq);
    }

    @Override
    public void onClick(View view) {
        AnimationDrawable animd = (AnimationDrawable) getBackground();
        animd.stop();
        animd.start();
        ResourceDownloaderService.showDownloadDialog((RemoteFile) resource, getContext());
    }
}