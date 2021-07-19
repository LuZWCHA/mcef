package com.nowandfuture.mod.utilities;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;

public class RemoteFile extends File {
    private String sum = null;
    private String remotePath = null;
    private String method = "MD5";

    public static RemoteFile createFake(){
        return new RemoteFile(new File(""));
    }

    public void setReal(@Nonnull File real) {
        this.real = real;
    }

    public File getReal() {
        return real;
    }

    @Override
    public String getName() {
        return real.getName();
    }

    @Override
    public String getParent() {
        return real.getParent();
    }

    @Override
    public File getParentFile() {
        return real.getParentFile();
    }

    @Override
    public String getPath() {
        return real.getPath();
    }

    @Override
    public boolean isAbsolute() {
        return real.isAbsolute();
    }

    @Override
    public String getAbsolutePath() {
        return real.getAbsolutePath();
    }

    @Override
    public File getAbsoluteFile() {
        return real.getAbsoluteFile();
    }

    @Override
    public String getCanonicalPath() throws IOException {
        return real.getCanonicalPath();
    }

    @Override
    public File getCanonicalFile() throws IOException {
        return real.getCanonicalFile();
    }

    @Override
    @Deprecated
    public URL toURL() throws MalformedURLException {
        return real.toURL();
    }

    @Override
    public URI toURI() {
        return real.toURI();
    }

    @Override
    public boolean canRead() {
        return real.canRead();
    }

    @Override
    public boolean canWrite() {
        return real.canWrite();
    }

    @Override
    public boolean exists() {
        return real.exists();
    }

    @Override
    public boolean isDirectory() {
        return real.isDirectory();
    }

    @Override
    public boolean isFile() {
        return real.isFile();
    }

    @Override
    public boolean isHidden() {
        return real.isHidden();
    }

    @Override
    public long lastModified() {
        return real.lastModified();
    }

    @Override
    public long length() {
        return real.length();
    }

    @Override
    public boolean createNewFile() throws IOException {
        return real.createNewFile();
    }

    @Override
    public boolean delete() {
        return real.delete();
    }

    @Override
    public void deleteOnExit() {
        real.deleteOnExit();
    }

    @Override
    public String[] list() {
        return real.list();
    }

    @Override
    public String[] list(FilenameFilter filter) {
        return real.list(filter);
    }

    @Override
    public File[] listFiles() {
        return real.listFiles();
    }

    @Override
    public File[] listFiles(FilenameFilter filter) {
        return real.listFiles(filter);
    }

    @Override
    public File[] listFiles(FileFilter filter) {
        return real.listFiles(filter);
    }

    @Override
    public boolean mkdir() {
        return real.mkdir();
    }

    @Override
    public boolean mkdirs() {
        return real.mkdirs();
    }

    @Override
    public boolean renameTo(File dest) {
        return real.renameTo(dest);
    }

    @Override
    public boolean setLastModified(long time) {
        return real.setLastModified(time);
    }

    @Override
    public boolean setReadOnly() {
        return real.setReadOnly();
    }

    @Override
    public boolean setWritable(boolean writable, boolean ownerOnly) {
        return real.setWritable(writable, ownerOnly);
    }

    @Override
    public boolean setWritable(boolean writable) {
        return real.setWritable(writable);
    }

    @Override
    public boolean setReadable(boolean readable, boolean ownerOnly) {
        return real.setReadable(readable, ownerOnly);
    }

    @Override
    public boolean setReadable(boolean readable) {
        return real.setReadable(readable);
    }

    @Override
    public boolean setExecutable(boolean executable, boolean ownerOnly) {
        return real.setExecutable(executable, ownerOnly);
    }

    @Override
    public boolean setExecutable(boolean executable) {
        return real.setExecutable(executable);
    }

    @Override
    public boolean canExecute() {
        return real.canExecute();
    }

    public static File[] listRoots() {
        return File.listRoots();
    }

    @Override
    public long getTotalSpace() {
        return real.getTotalSpace();
    }

    @Override
    public long getFreeSpace() {
        return real.getFreeSpace();
    }

    @Override
    public long getUsableSpace() {
        return real.getUsableSpace();
    }

    public static File createTempFile(String prefix, String suffix, File directory) throws IOException {
        return File.createTempFile(prefix, suffix, directory);
    }

    public static File createTempFile(String prefix, String suffix) throws IOException {
        return File.createTempFile(prefix, suffix);
    }

    @Override
    public int compareTo(File pathname) {
        return real.compareTo(pathname);
    }

    @Override
    public boolean equals(Object obj) {
        return real.equals(obj);
    }

    @Override
    public int hashCode() {
        return real.hashCode();
    }

    @Override
    public String toString() {
        return "RemoteFile{" +
                "remotePath='" + remotePath + '\'' +
                ", method='" + method + '\'' +
                ", real=" + real.toString() +
                '}';
    }

    @Override
    public Path toPath() {
        return real.toPath();
    }

    private File real;

    private RemoteFile(){
        this(new File(""));
    }

    public RemoteFile(File file){
        this(file.getPath());
        setReal(file);
    }

    public String getSum() {
        return sum;
    }

    public void setSum(String sum) {
        this.sum = sum;
    }

    public RemoteFile(String pathname) {
        super(pathname);
    }

    public RemoteFile(String parent, String child) {
        super(parent, child);
    }

    public RemoteFile(File parent, String child) {
        super(parent, child);
    }

    public RemoteFile(URI uri) {
        super(uri);
    }

    public String getRemotePath() {
        return remotePath;
    }

    public void setRemotePath(String remotePath) {
        this.remotePath = remotePath;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
