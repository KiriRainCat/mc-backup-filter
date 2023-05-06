package pers.kiriraincat;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main {
    private static final Properties properties = new Properties();
    private static long deletionDelay;
    private static long watchInterval;

    public static void main(String[] args) throws Exception {
        String worldName;
        try {
            BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream("./settings.properties"));
            properties.load(inputStream);
        } catch (FileNotFoundException e) {
            try (Scanner scanner = new Scanner(System.in)) {
                System.out.println("初次启动请输入参数...如有需要可以前往配置文件再次修改(特指需要删除的路径)");
                System.out.print("请输入backups文件夹的绝对路径: ");
                properties.put("watchPath", scanner.nextLine() + "\\");
                System.out.print("请输入检测到新备份文件后需要等待的时间(为了避免服务器没备份完就开删，单位[s, m, h]): ");
                properties.put("deletionDelay", scanner.nextLine());
                System.out.print("请输入路径扫描间隔(就是检查是否有新文件的间隔，单位[s, m, h]): ");
                properties.put("watchInterval", scanner.nextLine());
                System.out.print("请输入存档世界名称: ");
                worldName = scanner.nextLine();
                properties.put("worldName", worldName);
                System.out.print("请输入需要删除的路径(比如'DIM64/'也就是罗斯128B的文件夹，多个文件或目录使用逗号分隔): ");
                properties.put("deletionTarget", scanner.nextLine());
                properties.put("neatTerminalOutput", "false");
                properties.store(new BufferedOutputStream(Files.newOutputStream(Paths.get("./settings.properties"))), "Stored Settings");
            }
        } finally {
            String watchPath = properties.getProperty("watchPath");

            String tmp = properties.getProperty("deletionDelay");
            long delay = Long.parseLong(tmp.substring(0, tmp.length() - 1));
            if (tmp.endsWith("s")) {
                deletionDelay = delay * 1000;
            } else if (tmp.endsWith("m")) {
                deletionDelay = delay * 1000 * 60;
            }

            tmp = properties.getProperty("watchInterval");
            long interval = Long.parseLong(tmp.substring(0, tmp.length() - 1));
            if (tmp.endsWith("s")) {
                watchInterval = interval * 1000;
            } else if (tmp.endsWith("m")) {
                watchInterval = interval * 1000 * 60;
            }

            worldName = properties.getProperty("worldName");
            String finalWorldName = worldName;
            List<String> deletionTarget = Arrays.stream(properties.getProperty("deletionTarget").split(","))
                    .map(tar -> finalWorldName + "/" + tar)
                    .collect(Collectors.toList());

            boolean neatTerminalOutput = properties.getProperty("neatTerminalOutput").equals("true");

            // 开启检测器
            FileMonitor fileMonitor = new FileMonitor(watchInterval);
            fileMonitor.monitor(watchPath, new FileListener(deletionTarget, deletionDelay, neatTerminalOutput));
            fileMonitor.start();
        }
    }
}