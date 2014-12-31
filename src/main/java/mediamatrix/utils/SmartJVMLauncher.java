package mediamatrix.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class SmartJVMLauncher {

    public SmartJVMLauncher() {
    }

    public void executeCommand(String cmd, String... args) throws IOException, InterruptedException {
        String realCmd = null;
        if (System.getProperty("os.name").indexOf("Windows") >= 0) {
            realCmd = cmd + ".exe";
        } else {
            realCmd = cmd;
        }
        File[] files = new FileSearch().listFiles(new File(".").getAbsoluteFile().getParent(), realCmd);
        final List<String> argsList = new ArrayList<String>();
        argsList.add(files[0].getAbsolutePath());
        for (String s : args) {
            argsList.add(s);
        }
        final ProcessBuilder aProcessBuilder = new ProcessBuilder(argsList);
        aProcessBuilder.redirectErrorStream(true);
        Process p = aProcessBuilder.start();
        printInputStream(p.getInputStream());
        p.waitFor();
    }

    public void executeCommand(String cmd, List<String> args) throws IOException, InterruptedException {
        String realCmd = null;
        if (System.getProperty("os.name").indexOf("Windows") >= 0) {
            realCmd = cmd + ".exe";
        } else {
            realCmd = cmd;
        }
        File[] files = new FileSearch().listFiles(new File(".").getAbsoluteFile().getParent(), realCmd);
        final List<String> argsList = new ArrayList<String>();
        argsList.add(files[0].getAbsolutePath());
        argsList.addAll(args);
        final ProcessBuilder aProcessBuilder = new ProcessBuilder(argsList);
        aProcessBuilder.redirectErrorStream(true);
        Process p = aProcessBuilder.start();
        printInputStream(p.getInputStream());
        p.waitFor();
    }

    public void execute(String bit, int init, int max, String vmArgs, String className, List<String> args) throws IOException, InterruptedException {
        final List<String> argsList = new ArrayList<String>();
        final List<JVMVersion> list = findJava();
        final String currentDir = new File(".").getAbsoluteFile().getParent();
        for (JVMVersion jvm : list) {
            if (jvm.getCpuArc().equals(bit)) {
                argsList.add(jvm.getPath());
            }
        }
        if (argsList.isEmpty()) {
            argsList.add(list.get(0).getPath());
        }
        argsList.add("-classpath");
        argsList.add(getClasspath(currentDir));
        StringBuffer buff = new StringBuffer();
        buff.append(System.getenv("PATH"));
        if (System.getProperty("os.name").indexOf("Windows") >= 0) {
            buff.append(";" + currentDir);
        } else {
            buff.append(":" + currentDir);
        }
        argsList.add("-Djava.library.path=" + buff.toString());
        argsList.add("-Xms" + init + "m");
        argsList.add("-Xmx" + max + "m");
        if (vmArgs != null) {
            argsList.add(vmArgs);
        }
        argsList.add(className);
        if (args != null) {
            argsList.addAll(args);
        }
        final ProcessBuilder aProcessBuilder = new ProcessBuilder(argsList);
        aProcessBuilder.redirectErrorStream(true);
        Process p = aProcessBuilder.start();
        printInputStream(p.getInputStream());
        p.waitFor();
    }

    private String getClasspath(String classPathTopDir) {
        File[] files = new FileSearch().listFiles(classPathTopDir, "*.jar");
        final StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < files.length; i++) {
            if (buffer.length() == 0) {
                buffer.append(files[i].getAbsolutePath());
            } else {
                if (System.getProperty("os.name").indexOf("Windows") >= 0) {
                    buffer.append(";");
                } else {
                    buffer.append(":");
                }
                buffer.append(files[i].getAbsolutePath());
            }
        }
        return buffer.toString();
    }

    public List<JVMVersion> findJava() throws IOException, InterruptedException {
        final List<JVMVersion> list = new ArrayList<JVMVersion>();
        final Set<String> path = new TreeSet<String>();
        if (System.getenv("JAVA_HOME") != null) {
            path.add(System.getenv("JAVA_HOME"));
        }
        if (System.getProperty("os.name").indexOf("Windows") >= 0) {
            if (System.getenv("ProgramFiles") != null) {
                path.add(System.getenv("ProgramFiles") + "/Java/jre6/");
            }
            if (System.getenv("ProgramW6432") != null) {
                path.add(System.getenv("ProgramW6432") + "/Java/jre6/");
            }
            if (System.getenv("ProgramFiles(x86)") != null) {
                path.add(System.getenv("ProgramFiles(x86)") + "/Java/jre6/");
            }
        } else {
            path.add("/usr/bin/");
        }

        String executable = null;
        if (System.getProperty("os.name").indexOf("Windows") >= 0) {
            executable = "java.exe";
        } else {
            executable = "java";
        }
        for (Iterator<String> it = path.iterator(); it.hasNext();) {
            final File[] files = new FileSearch().listFiles(it.next(), executable);
            for (int j = 0; j < files.length; j++) {
                if (files[j].exists() && files[j].canExecute() && files[j].isFile()) {
                    final ProcessBuilder pb = new ProcessBuilder(files[j].getAbsolutePath(), "-version");
                    final Process p = pb.start();
                    final JVMVersion v = parseInputStream(files[j].getAbsolutePath(), p.getErrorStream());
                    p.waitFor();
                    list.add(v);
                }
            }
        }
        return list;
    }

    private JVMVersion parseInputStream(String path, InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line1 = br.readLine().trim();
        String line2 = br.readLine().trim();
        String line3 = br.readLine().trim();
        final JVMVersion result = new JVMVersion(path);
        result.setMajorVersion(line1.replace("java version", "").replace("\"", "").split("_")[0].trim());
        result.setMinorVersion(line1.replace("java version", "").replace("\"", "").split("_")[1].trim());
        if (line3.contains("64-Bit")) {
            result.setCpuArc("64");
        } else {
            result.setCpuArc("32");
        }
        return result;
    }

    private void printInputStream(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        for (String line = br.readLine(); line != null; line = br.readLine()) {
            System.out.println(line);
        }
    }
}
