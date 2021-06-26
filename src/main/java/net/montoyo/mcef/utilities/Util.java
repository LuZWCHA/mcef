package net.montoyo.mcef.utilities;

import net.montoyo.mcef.MCEF;
import net.montoyo.mcef.remote.Mirror;
import net.montoyo.mcef.remote.MirrorManager;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import javax.swing.*;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Util {

    private static final DummyProgressListener DPH = new DummyProgressListener();
    private static final String HEX = "0123456789abcdef";
    public static SSLSocketFactory SSL_SOCKET_FACTORY;

    /**
     * Clamps d between min and max.
     *
     * @param d   The value to clamp.
     * @param min The minimum.
     * @param max The maximum.
     * @return The clamped value.
     */
    public static double clamp(double d, double min, double max) {
        if (d < min)
            return min;
        else if (d > max)
            return max;
        else
            return d;
    }

    /**
     * Extracts a ZIP archive into a folder.
     *
     * @param zip The ZIP archive file to extract.
     * @param out The output directory for the ZIP content.
     * @return true if the extraction was successful.
     */
    public static boolean extract(File zip, File out) {
        ZipInputStream zis;

        try {
            zis = new ZipInputStream(new FileInputStream(zip));
        } catch (FileNotFoundException e) {
            Log.error("Couldn't extract %s: File not found.", zip.getName());
            e.printStackTrace();
            return false;
        }

        try {
            ZipEntry ze;
            while ((ze = zis.getNextEntry()) != null) {
                if (ze.isDirectory())
                    continue;

                File dst = new File(out, ze.getName());
                delete(dst);
                mkdirs(dst);

                FileOutputStream fos = new FileOutputStream(dst);
                byte[] data = new byte[65536];
                int read;

                while ((read = zis.read(data)) > 0)
                    fos.write(data, 0, read);

                close(fos);
            }

            return true;
        } catch (FileNotFoundException e) {
            Log.error("Couldn't extract a file from %s. Maybe you're missing some permissions?", zip.getName());
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            Log.error("IOException while extracting %s.", zip.getName());
            e.printStackTrace();
            return false;
        } finally {
            close(zis);
        }
    }

    /**
     * Returns the SHA-1 checksum of a file.
     *
     * @param fle The file to be hashed.
     * @return The hash of the file or null if an error occurred.
     */
    public static String hash(File fle) {
        FileInputStream fis;

        try {
            fis = new FileInputStream(fle);
        } catch (FileNotFoundException e) {
            Log.error("Couldn't hash %s: File not found.", fle.getName());
            e.printStackTrace();
            return null;
        }

        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            sha.reset();

            int read = 0;
            byte buffer[] = new byte[65536];

            while ((read = fis.read(buffer)) > 0)
                sha.update(buffer, 0, read);

            byte digest[] = sha.digest();
            String hash = "";

            for (int i = 0; i < digest.length; i++) {
                int b = digest[i] & 0xFF;
                int left = b >>> 4;
                int right = b & 0x0F;

                hash += HEX.charAt(left);
                hash += HEX.charAt(right);
            }

            return hash;
        } catch (IOException e) {
            Log.error("IOException while hashing file %s", fle.getName());
            e.printStackTrace();
            return null;
        } catch (NoSuchAlgorithmException e) {
            Log.error("Holy crap this shouldn't happen. SHA-1 not found!!!!");
            e.printStackTrace();
            return null;
        } finally {
            close(fis);
        }
    }

    /**
     * Downloads a remote resource.
     *
     * @param res  The filename of the resource relative to the mirror root.
     * @param dst  The destination file.
     * @param gzip Also extract the content using GZipInputStream.
     * @param ph   The progress handler. May be null.
     * @return true if the download was successful.
     */
    public static boolean download(String res, File dst, boolean gzip, IProgressListener ph) {
        String err = "Couldn't download " + dst.getName() + "!";

        ph = secure(ph);
        ph.onTaskChanged("Downloading " + dst.getName());

        SizedInputStream sis = openStream(res, err);
        if (sis == null)
            return false;

        InputStream is;
        if (gzip) {
            try {
                is = new GZIPInputStream(sis);
            } catch (IOException e) {
                Log.error("Couldn't create GZIPInputStream: IOException.");
                ph.onError(dst.getName(), new Throwable("Couldn't create GZIPInputStream: IOException."));
                e.printStackTrace();
                close(sis);
                return false;
            }
        } else
            is = sis;

        delete(dst);
        mkdirs(dst);

        FileOutputStream fos;
        try {
            fos = new FileOutputStream(dst);
        } catch (FileNotFoundException e) {
            Log.error("%s Couldn't open the destination file. Maybe you're missing rights.", err);
            ph.onError(dst.getName(), new Throwable(String.format("%s Couldn't open the destination file. Maybe you're missing rights.", err)));
            e.printStackTrace();
            close(is);
            return false;
        }

        int read;
        byte[] data = new byte[65536];
        double total = (double) sis.getContentLength();
        double cur = .0d;

        try {
            while ((read = is.read(data)) > 0) {
                fos.write(data, 0, read);

                cur += (double) sis.resetLengthCounter();
                ph.onProgressed(cur / total * 100.d);
            }
            ph.onProgressEnd();
            return true;
        } catch (IOException e) {
            Log.error("%s IOException while downloading.", err);
            ph.onError(dst.getName(), new Throwable(String.format("%s IOException while downloading.", err)));
            e.printStackTrace();
            return false;
        } finally {
            close(is);
            close(fos);
        }
    }

    /**
     * Same as {@link #download(String, File, boolean, IProgressListener) download}, but with gzip set to false.
     *
     * @param res
     * @param dst
     * @param ph
     * @return
     */
    public static boolean download(String res, File dst, IProgressListener ph) {
        return download(res, dst, false, ph);
    }

    /**
     * Convenience function. Secures a progress listener.
     * If pl is null, then a dummy empty progress listener will be returned.
     *
     * @param pl The progress handler to secure.
     * @return A progress handler that is never null.
     * @see IProgressListener
     */
    public static IProgressListener secure(IProgressListener pl) {
        return (pl == null) ? DPH : pl;
    }

    /**
     * Renames a file using a string.
     *
     * @param src  The file to rename.
     * @param name The new name of the file.
     * @return the new file or null if it failed.
     */
    public static File rename(File src, String name) {
        File ret = new File(src.getParentFile(), name);

        if (src.renameTo(ret))
            return ret;
        else
            return null;
    }

    /**
     * Makes sure that the directory in which the file is exists.
     * If this one doesn't exist, i'll be created.
     *
     * @param f The file.
     */
    public static void mkdirs(File f) {
        File p = f.getParentFile();
        if (!p.exists())
            p.mkdirs();
    }

    /**
     * Tries to delete a file in an advanced way.
     * Does a warning in log if it couldn't delete it neither rename it.
     *
     * @param f The file to be deleted.
     * @see #delete(File)
     */
    public static void delete(String f) {
        delete(new File(f));
    }

    /**
     * Tries to delete a file in an advanced way.
     * Does a warning in log if it couldn't delete it neither rename it.
     *
     * @param f The file to be deleted.
     * @see #delete(String)
     */
    public static void delete(File f) {
        if (!f.exists() || f.delete())
            return;

        File mv = new File(f.getParentFile(), "deleteme" + ((int) (Math.random() * 100000.d)));
        if (f.renameTo(mv)) {
            if (!mv.delete())
                mv.deleteOnExit();

            return;
        }

        Log.warning("Couldn't delete file! If there's any problems, please try to remove it yourself. Path: %s", f.getAbsolutePath());
    }

    /**
     * Tries to open an InputStream to the following remote resource.
     * Automatically handles broken mirrors and other errors.
     *
     * @param res The resource filename relative to the root of the mirror.
     * @param err An error string in case it fails.
     * @return The opened input stream.
     */
    public static SizedInputStream openStream(String res, String err) {
        do {
            HttpURLConnection conn;

            try {
                Mirror m = MirrorManager.INSTANCE.getCurrent();
                conn = m.getResource(res);

                if (conn instanceof HttpsURLConnection && m.usesLetsEncryptCertificate() && SSL_SOCKET_FACTORY != null)
                    ((HttpsURLConnection) conn).setSSLSocketFactory(SSL_SOCKET_FACTORY);
            } catch (MalformedURLException e) {
                Log.error("%s Is the mirror list broken?", err);
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                Log.error("%s Is your antivirus or firewall blocking the connection?", err);
                e.printStackTrace();
                return null;
            }

            try {
                long len = -1;
                boolean failed = true;

                //Java 6 support
                try {
                    Method m = HttpURLConnection.class.getMethod("getContentLengthLong");
                    len = (Long) m.invoke(conn);
                    failed = false;
                } catch (NoSuchMethodException me) {
                } catch (IllegalAccessException ae) {
                } catch (InvocationTargetException te) {
                    if (te.getTargetException() instanceof IOException)
                        throw (IOException) te.getTargetException();
                }

                if (failed)
                    len = (long) conn.getContentLength();

                return new SizedInputStream(conn.getInputStream(), len);
            } catch (IOException e) {
                int rc;

                try {
                    rc = conn.getResponseCode();
                } catch (IOException ie) {
                    Log.error("%s Couldn't even get the HTTP response code!", err);
                    ie.printStackTrace();

                    return null;
                }

                Log.error("%s HTTP response is %d; trying with another mirror.", err, rc);
            }
        } while (MirrorManager.INSTANCE.markCurrentMirrorAsBroken());

        Log.error("%s All mirrors seems broken.", err);
        return null;
    }

    /**
     * Calls "close" on the specified object without throwing any exceptions.
     * This is usefull with input and output streams.
     *
     * @param o The object to call close on.
     */
    public static void close(Object o) {
        try {
            o.getClass().getMethod("close").invoke(o);
        } catch (Throwable t) {
        }
    }

    /**
     * Same as {@link Files#isSameFile(Path, Path)} but if an {@link IOException} is thrown,
     * return false.
     *
     * @param p1 Path 1
     * @param p2 Path 2
     * @return true if the paths are the same, false if they are not or if an exception is thrown during the comparison
     */
    public static boolean isSameFile(Path p1, Path p2) {
        try {
            return Files.isSameFile(p1, p2);
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Same as {@link System#getenv(String)}, but if no such environment variable is
     * defined, will return an empty string instead of null.
     *
     * @param name Name of the environment variable to get
     * @return The value of this environment variable (may be empty but never null)
     */
    public static String getenv(String name) {
        String ret = System.getenv(name);
        return ret == null ? "" : ret;
    }


    public static boolean addPath2JavaLibPath4WinJava78(String path) {
        return addPath2JavaLibPath(path, "usr_paths");
    }

    public static boolean addPath2JavaLibPath4LinuxJava78(String path, String mcDataFile) {
        final String LINUX_WIKI = "https://montoyo.net/wdwiki/Linux";
        //LinuxPatch.doPatch(resourceArray); //Not needed, from what I experienced...

        FileSystem fs = FileSystems.getDefault();
        Path here = fs.getPath(mcDataFile);
        String[] libPath = Util.getenv("LD_LIBRARY_PATH").split(":");

        if (Arrays.stream(libPath).filter(s -> !s.isEmpty()).map(fs::getPath).noneMatch(p -> Util.isSameFile(p, here))) {
            Log.error("On Linux, you *HAVE* to add the .minecraft folder to LD_LIBRARY_PATH in order for MCEF to work.");
            Log.error("You can do this by running the following command and then starting Minecraft within the same terminal:");
            Log.error("export \"LD_LIBRARY_PATH=$LD_LIBRARY_PATH:%s\"", path);
            Log.error("");
            Log.error("Since this has not been done yet, MCEF will now enter virtual mode and WILL NOT WORK.");
            Log.error("For more info, please read %s", LINUX_WIKI);
            Log.error("Please don't post a GitHub issue for this.");

            int ans = JOptionPane.showConfirmDialog(null, "A bug on Linux requires you to add the Minecraft folder to LD_LIBRARY_PATH.\nThis has not been done, so MCEF will not work for now.\nWould you like to open the wiki page?",
                    "MCEF Linux", JOptionPane.YES_NO_OPTION);

            if (ans == JOptionPane.YES_OPTION) {
                try {
                    Runtime.getRuntime().exec("xdg-open " + LINUX_WIKI);
                } catch (IOException ex) {
                    Log.errorEx("Could not open wiki page", ex);
                    JOptionPane.showMessageDialog(null, "Couldn't automatically open the wiki page. The link is:\n" + LINUX_WIKI, "MCEF Linux", JOptionPane.ERROR_MESSAGE);
                }
            }

            return false;
        }
        return true;

    }


    public static boolean addPath2JavaLibPath(String path, String fieldName) {
        if(fieldName == null)
            return false;

        try {
            Field pathsField = ClassLoader.class.getDeclaredField(fieldName);
            pathsField.setAccessible(true);

            String[] paths = (String[]) pathsField.get(null);
            String[] newList = new String[paths.length + 1];

            System.arraycopy(paths, 0, newList, 1, paths.length);
            newList[0] = path.replace('/', File.separatorChar);
            pathsField.set(null, newList);

            return true;
        } catch (Exception e) {
            Log.error("Failed to do it! Entering virtual mode...");
            e.printStackTrace();
        }

        return false;
    }

    /**
     * @param path add to a new Property to JVM, so that other Application can get the path
     * @return success or not
     */
    public static boolean addPath2JcefLibPath(String path){
        try {
            System.setProperty("jcef.library.path", path);
        }catch (Exception exception) {
            exception.printStackTrace();
            return false;
        }

        return path.equals(System.getProperty("jcef.library.path"));
    }

}
