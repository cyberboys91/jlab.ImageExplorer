package jlab.ImageExplorer.Resource;


import jlab.ImageExplorer.R;
import android.provider.MediaStore;

/*
 * Created by Javier on 24/07/2017.
 */

public class VideosLocalDirectory extends FilesLocalDirectory {
    public VideosLocalDirectory(String name) {
        super(name, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, R.color.red);
    }

    @Override
    public boolean isMultiColumn() {
        return true;
    }

    @Override
    protected boolean loadIDs() {
        return true;
    }

    @Override
    public void loadData() {
        openSynchronic(null);
    }
}
