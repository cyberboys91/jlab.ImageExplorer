package jlab.ImageExplorer.Activity;

import java.io.File;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import jlab.ImageExplorer.R;
import android.view.MenuItem;
import android.content.Intent;
import android.view.ViewGroup;
import android.widget.Gallery;
import android.view.MotionEvent;
import com.bumptech.glide.Glide;
import jlab.ImageExplorer.Utils;
import android.widget.AdapterView;
import android.view.LayoutInflater;
import jlab.ImageExplorer.Interfaces;
import android.content.DialogInterface;
import androidx.appcompat.widget.Toolbar;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import jlab.ImageExplorer.Resource.Resource;
import jlab.ImageExplorer.Resource.Directory;
import jlab.ImageExplorer.View.ZoomImageView;
import android.view.animation.AnimationUtils;
import jlab.ImageExplorer.Resource.LocalFile;
import jlab.ImageExplorer.db.FavoriteDetails;
import jlab.ImageExplorer.Resource.RemoteFile;
import androidx.appcompat.app.AppCompatActivity;
import jlab.ImageExplorer.Resource.FileResource;
import com.google.android.material.appbar.AppBarLayout;
import jlab.ImageExplorer.Resource.LocalDirectory;
import jlab.ImageExplorer.Resource.RemoteDirectory;
import jlab.ImageExplorer.View.ResourceDetailsAdapter;
import static jlab.ImageExplorer.Utils.specialDirectories;
import jlab.ImageExplorer.Resource.LocalStorageDirectories;
import jlab.ImageExplorer.Activity.Fragment.ActionFragment;
import jlab.ImageExplorer.Activity.Fragment.DeleteFragment;
import jlab.ImageExplorer.Activity.Fragment.RenameFragment;
import jlab.ImageExplorer.Activity.Fragment.DetailsFragment;
import jlab.ImageExplorer.View.ResourceDetailsAdapter.OnGetSetViewListener;

/*
 * Created by Javier on 25/08/2018.
 */

public class ImageViewActivity extends AppCompatActivity implements View.OnTouchListener, OnGetSetViewListener,
        AdapterView.OnItemSelectedListener, AdapterView.OnItemClickListener{

    private FileResource resource;
    private Toolbar toolbar;
    private AppBarLayout barImage;
    private Gallery gallery;
    private int currentIndex = 0;
    private Directory directory;
    private LayoutInflater mlInflater;
    private ResourceDetailsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);
        this.barImage = (AppBarLayout) findViewById(R.id.ablImageBar);
        this.gallery = (Gallery) findViewById(R.id.gallery);
        this.mlInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        Utils.currentActivity = this;
        Utils.viewForSnack = gallery;
        Uri uri = savedInstanceState != null && savedInstanceState.containsKey(Utils.RESOURCE_PATH_KEY)
                ? Uri.parse(savedInstanceState.getString(Utils.RESOURCE_PATH_KEY))
                : getIntent().getData();
        loadResource(uri);
        this.toolbar = (Toolbar) findViewById(R.id.toolbar);
        this.toolbar.setTitleTextAppearance(this, R.style.ToolBarApparence);
        this.toolbar.setTitle(resource.getName());
        setSupportActionBar(toolbar);
        loadDirectory(savedInstanceState != null && savedInstanceState.containsKey(Utils.DIRECTORY_KEY)
                ? savedInstanceState : getIntent().getExtras());
        adapter = new ResourceDetailsAdapter();
        adapter.addAll(directory.getContent());
        adapter.setonGetSetViewListener(this);
        gallery.setOnItemSelectedListener(this);
        gallery.setOnItemClickListener(this);
        gallery.setAdapter(adapter);
        gallery.setSelection(currentIndex, true);
    }

    private void loadDirectory(Bundle bundle) {
        String name = bundle != null ? bundle.getString(Utils.DIRECTORY_KEY) : "";
        currentIndex = bundle != null ? bundle.getInt(Utils.INDEX_CURRENT_KEY) : 0;
        directory = getSpecialDirectory(name);
        if (directory == null)
            loadParentDirectoryFromResource(name);
    }

    private void loadParentDirectoryFromResource(String name) {
        if (resource.isRemote()) {
            directory = new RemoteDirectory(name, "", "");
            directory.getContent().add(resource);
            currentIndex = 0;
        } else {
            String path = resource.getRelUrl().substring(0, resource.getRelUrl().length() - resource.getName().length());
            LocalDirectory auxDir = new LocalDirectory(name, path, "", false, 0);
            directory = new LocalDirectory(name, path, "", false, 0);
            auxDir.openSynchronic(null);
            int index = 0;
            for (int i = 0; i < auxDir.getCountElements(); i++) {
                Resource current = auxDir.getResource(i);
                boolean add = !current.isDir() && ((FileResource) current).isImage();
                if (add) {
                    directory.getContent().add(current);
                    index++;
                }
                if (Utils.isEqual(current.getRelUrl(), resource.getRelUrl())) {
                    if (!add) {
                        directory.getContent().add(current);
                        currentIndex = index;
                        index++;
                    } else
                        currentIndex = index - 1;
                }
            }
            if(index == 0) {
                directory.getContent().add(resource);
                currentIndex = 0;
            }
        }
    }

    private Directory getSpecialDirectory(String name) {
        Directory result = null;
        if (name != null) {
            specialDirectories = new LocalStorageDirectories();
            specialDirectories.openSynchronic(null);
            if (name.equals(getString(R.string.downloads_folder)))
                result = specialDirectories.getDownloadDirectory();
            else if (name.equals(getString(R.string.camera_folder)))
                result = specialDirectories.getCameraDirectory();
            else if (name.equals(getString(R.string.favorite_folder)))
                result = specialDirectories.getFavoritesDirectory();
            else if (name.equals(getString(R.string.albums_folder)))
                result = specialDirectories.getAlbumsDirectory();
            else if (name.equals(getString(R.string.all_images)))
                result = specialDirectories.getImagesDirectory();
            if (result != null)
                result.openSynchronic(null);
        }
        return result;
    }

    private void loadResource(Uri uri) {
        String name = FileResource.getNameFromUrl(uri.getPath());
        if (uri.getScheme().equals(Utils.FILE_SCHEME)) {
            File file = new File(uri.getPath());
            this.resource = new LocalFile(name, uri.getPath(), "", "", file.length(),
                    file.lastModified(), file.isHidden());
        } else
            this.resource = new RemoteFile(name, uri.toString(), 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_options_image, menu);
        final MenuItem mnfavorite = menu.getItem(0);
        if (!resource.isRemote() && resource.isImage() && resource.getFavoriteStateLoad()) {
            mnfavorite.setIcon(resource.isFavorite()
                    ? R.drawable.img_favorite_checked
                    : R.drawable.img_favorite_not_checked);
            mnfavorite.setTitle(resource.isFavorite()
                    ? R.string.remove_of_favorite_folder
                    : R.string.add_to_favorite_folder);
        } else if (!resource.isRemote() && resource.isImage()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final boolean isFavorite = Utils.isFavorite(resource);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mnfavorite.setIcon(isFavorite
                                    ? R.drawable.img_favorite_checked
                                    : R.drawable.img_favorite_not_checked);
                            mnfavorite.setTitle(resource.isFavorite()
                                    ? R.string.remove_of_favorite_folder
                                    : R.string.add_to_favorite_folder);
                        }
                    });
                }
            }).start();
        } else {
            menu.removeItem(R.id.mnShare);
            menu.removeItem(R.id.mnDelete);
            menu.removeItem(R.id.mnFavorite);
            menu.removeItem(R.id.mnRename);
            menu.removeItem(R.id.mnSetImageAs);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mnFavorite:
                if(resource.isImage()) {
                    if (!resource.getFavoriteStateLoad())
                        Utils.isFavorite(resource);

                    if (resource.isFavorite())
                        resource.setIsFavorite(false, Utils.deleteFavoriteData(resource.getIdFavorite()));
                    else
                        resource.setIsFavorite(true, Utils.saveFavoriteData(new FavoriteDetails(resource.getRelUrl(),
                                resource.getComment(), resource.getParentName(), resource.mSize, resource.getModificationDate())));

                    item.setIcon(resource.isFavorite()
                            ? R.drawable.img_favorite_checked
                            : R.drawable.img_favorite_not_checked);
                    item.setTitle(resource.isFavorite()
                            ? R.string.remove_of_favorite_folder
                            : R.string.add_to_favorite_folder);
                }
                break;
            case R.id.mnShare:
                //Share
                try {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType(resource.getMimeType());
                    intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(resource.getAbsUrl()));
                    startActivity(Intent.createChooser(intent, getString(R.string.share)));
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                }
                break;
            case R.id.mnSetImageAs:
                //Set image as
                try {
                    Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
                    intent.putExtra(resource.getExtension(), resource.getMimeType());
                    intent.setDataAndType(Uri.parse(resource.getAbsUrl()), resource.getMimeType());
                    startActivity(Intent.createChooser(intent, getString(R.string.set_image_as)));
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                }
                break;
            case R.id.mnRename:
                //Rename
                try {
                    ActionFragment rename = new RenameFragment();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(Utils.RESOURCE_FOR_DETAILS_KEY, resource);
                    rename.setArguments(bundle);
                    ActionFragment.setOnRefreshListener(new Interfaces.IRefreshListener() {
                        @Override
                        public void refresh() {
                            toolbar.setTitle(resource.getName());
                        }
                    });
                    rename.show(getFragmentManager(), "jlab.Rename");
                } catch (Exception exp) {
                    exp.printStackTrace();
                }
                break;
            case R.id.mnDelete:
                //Delete
                if (!DeleteFragment.isRunning())
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.question)
                            .setMessage(String.format("%s %s: \"%s\"?", getString(R.string.delete_begin_question),
                                    resource.isDir()
                                            ? getString(R.string.the_folder)
                                            : getString(R.string.the_file),
                                    resource.getName()))
                            .setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    DeleteFragment deleteFragment = new DeleteFragment();
                                    deleteFragment.setFinishListener(new Interfaces.IFinishListener() {
                                        @Override
                                        public void refresh(boolean successfully) {
                                            try {
                                                if (successfully) {
                                                    if (directory.getCountElements() == 1)
                                                        finish();
                                                    else {
                                                        directory.getContent().remove(currentIndex);
                                                        if (currentIndex == directory.getCountElements())
                                                            currentIndex = 0;
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                adapter.clear();
                                                                adapter.addAll(directory.getContent());
                                                                gallery.setAdapter(adapter);
                                                                gallery.setSelection(currentIndex, true);
                                                            }
                                                        });
                                                    }
                                                }
                                            } catch (Exception ignored) {
                                                ignored.printStackTrace();
                                            }
                                        }
                                    });
                                    Bundle args = new Bundle();
                                    args.putSerializable(Utils.RESOURCE_FOR_DELETE, resource);
                                    deleteFragment.setArguments(args);
                                    deleteFragment.show(getFragmentManager(), "jlab.Delete");
                                }
                            })
                            .setNegativeButton(R.string.cancel, null)
                            .create().show();
                else
                    Utils.showSnackBar(R.string.wait_deleting);
                break;
            case R.id.mnRateApp:
                try {
                    Utils.rateApp();
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                }
                break;
            case R.id.mnAbout:
                Utils.showAboutDialog();
                break;
            case R.id.mnDetails:
                //Details
                try {
                    DetailsFragment details = new DetailsFragment();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(Utils.RESOURCE_FOR_DETAILS_KEY, resource);
                    details.setArguments(bundle);
                    details.show(getFragmentManager(), "jlab.Details");
                } catch (Exception exp) {
                    exp.printStackTrace();
                }
                break;
            case R.id.mnClose:
                finish();
                break;
        }
        return true;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Utils.DIRECTORY_KEY, directory.getName());
        outState.putInt(Utils.INDEX_CURRENT_KEY, currentIndex);
        outState.putString(Utils.RESOURCE_PATH_KEY, resource.getAbsUrl());
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return gallery.onTouchEvent(motionEvent);
    }

    @Override
    public View getView(ViewGroup parent, int position, boolean isDir) {
        return mlInflater.inflate(R.layout.image_view, parent, false);
    }

    @Override
    public void setView(View view, Resource resource, int position) {
        view.findViewById(R.id.ivImageContent).setOnTouchListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        try {
            resource = (FileResource) directory.getResource(i);
            currentIndex = i;
            if (resource.isImage()) {
                if (view != null) {
                    Glide.with(getBaseContext()).load(resource.getRelUrl()).error(R.drawable.img_broken_image)
                            .into((ZoomImageView) view.findViewById(R.id.ivImageContent));
                }
                toolbar.setTitle(resource.getName());
                invalidateOptionsMenu();
            } else
                ((ZoomImageView) view.findViewById(R.id.ivImageContent)).setImageResource(R.drawable.img_broken_image);
        } catch (Exception ignored) {
            ignored.printStackTrace();
            if (view != null)
                ((ZoomImageView) view.findViewById(R.id.ivImageContent)).setImageResource(R.drawable.img_broken_image);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        barImage.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.alpha_in_out));
    }
}