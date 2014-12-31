package mediamatrix.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class IOUtilities {

    public IOUtilities() {
    }

    public static byte[] download(URL url) throws IOException {
        return download(url.toString(), 60000, 60000);
    }

    public static byte[] download(String url, int connectTimeout, int readTimeout) throws IOException {
        URLConnection con = new URL(url).openConnection();
        con.setConnectTimeout(connectTimeout);
        con.setReadTimeout(readTimeout);
        InputStream in = con.getInputStream();
        byte[] content;
        try {
            content = toBytes(in);
        } finally {
            in.close();
        }
        return content;
    }

    public static File saveAsTempFile(byte[] buff, String suffix) throws IOException {
        File temp = File.createTempFile("temp", suffix);
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(temp));
        bos.write(buff, 0, buff.length);
        bos.close();
        return temp;
    }

    private static byte[] toBytes(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        copy(in, out);
        return out.toByteArray();
    }

    private static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buff = new byte[256];
        int len = in.read(buff);
        while (len != -1) {
            out.write(buff, 0, len);
            len = in.read(buff);
        }
    }

    public static String readString(File file, String encode) throws IOException {
        final InputStreamReader in = new InputStreamReader(new FileInputStream(file), Charset.forName(encode));
        final StringWriter sw = new StringWriter();
        char[] buffer = new char[4096];
        for (int n = in.read(buffer); n > -1; n = in.read(buffer)) {
            sw.write(buffer, 0, n);
        }
        in.close();
        return sw.toString();
    }

    public static String readString(InputStream input, String encode) throws IOException {
        final InputStreamReader in = new InputStreamReader(input, Charset.forName(encode));
        final StringWriter sw = new StringWriter();
        char[] buffer = new char[4096];
        for (int n = in.read(buffer); n > -1; n = in.read(buffer)) {
            sw.write(buffer, 0, n);
        }
        in.close();
        return sw.toString();
    }

    public static byte[] readAllBytes(File file) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        byte[] buff = new byte[4026];
        for (int n = bis.read(buff); n > -1; n = bis.read(buff)) {
            bos.write(buff, 0, n);
        }
        bis.close();
        return bos.toByteArray();
    }

    public static byte[] readAllBytes(InputStream in) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        BufferedInputStream bis = new BufferedInputStream(in);
        byte[] buff = new byte[4026];
        for (int n = bis.read(buff); n > -1; n = bis.read(buff)) {
            bos.write(buff, 0, n);
        }
        bis.close();
        return bos.toByteArray();
    }

    public static File changeSuffix(File file, String suffix) {
        File dir = file.getParentFile();
        String fileName = file.getName();
        int slash = fileName.lastIndexOf(File.separatorChar);
        int dot = fileName.lastIndexOf('.');
        String newFileName = fileName.substring(slash + 1, dot) + suffix;
        return new File(dir.getAbsolutePath() + File.separator + newFileName);
    }

    public static File[] findRecursively(File directory) {
        return findRecursively(directory, new ArrayList<File>());
    }

    private static File[] findRecursively(File directory, List<File> result) {
        File[] files = directory.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                findRecursively(files[i], result);
            } else {
                result.add(files[i]);
            }
        }
        return result.toArray(new File[result.size()]);
    }
}
