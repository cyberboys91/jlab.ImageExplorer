package jlab.ImageExplorer.Resource;

/*
 * Created by Javier on 23/12/2017.
 */

public class RemoteFile extends FileResource {
    public RemoteFile(String name, String relUrl, long size) {
        super(name, relUrl, null, size);
        this.isRemote = true;
    }
}