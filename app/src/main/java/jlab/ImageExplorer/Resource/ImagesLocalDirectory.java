package jlab.ImageExplorer.Resource;

import android.provider.MediaStore;

import jlab.ImageExplorer.R;
import jlab.ImageExplorer.Utils;

/*
 * Created by Javier on 24/07/2017.
 */

public class ImagesLocalDirectory extends FilesLocalDirectory {
    public ImagesLocalDirectory(String name) {
        super(name, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, R.color.blue);
    }

    @Override
    public boolean isMultiColumn() {
        return true;
    }

    @Override
    protected void getElems() {
        Utils.getAllImagesLocalFiles(uri, this, onSelectListener);
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
