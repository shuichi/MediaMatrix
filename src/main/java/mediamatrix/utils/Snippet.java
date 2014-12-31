package mediamatrix.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Snippet {

    public static void main(String[] args) throws Exception {
        String path = null;
        Map<String, String> env = System.getenv();
        if (env.containsKey("Path")) {
            path = env.get("Path");
        } else if (env.containsKey("PATH")) {
            path = env.get("PATH");
        } else if (env.containsKey("path")) {
            path = env.get("path");
        }

        String cmd = "ffmpeg";
        if (System.getProperty("os.name").indexOf("Windows") >= 0) {
            cmd = cmd + ".exe";
        } else {
            cmd = cmd;
        }

        String[] dirs = path.split(File.pathSeparator);
        List<String> lists = new ArrayList<>();
        for (String dir : dirs) {
            lists.add(dir);
        }
        lists.add("/usr/local/bin");
        lists.add("/opt/local/bin");
        for (String dir : lists) {
            File target = new File(dir, cmd);
            if (target.exists()) {
                System.out.println(target);
            }
        }
    }
}
