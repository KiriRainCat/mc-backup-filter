package pers.kiriraincat;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationObserver;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FileListener extends FileAlterationListenerAdaptor {

    private static String compressedPath;
    private static List<String> deletionTarget;
    private static long deletionDelay;
    private static boolean neatTerminalOutput;

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");


    public FileListener(List<String> deletionTarget, long deletionDelay, boolean neatTerminalOutput) {
        FileListener.deletionTarget = deletionTarget;
        FileListener.deletionDelay = deletionDelay;
        FileListener.neatTerminalOutput = neatTerminalOutput;
    }

    @Override
    public void onStart(FileAlterationObserver observer) {
        super.onStart(observer);
        if (!neatTerminalOutput) {
            System.out.println("开始检测...");
        }
    }

    @Override
    public void onDirectoryCreate(File directory) {
    }

    @Override
    public void onDirectoryChange(File directory) {
    }

    @Override
    public void onDirectoryDelete(File directory) {
    }

    @Override
    public void onFileCreate(File file) {
        compressedPath = file.getAbsolutePath();
        if (compressedPath.endsWith(".zip")) {
            System.out.println(sdf.format(new Date()) + " 发现新备份文件：" + compressedPath);
            if (file.canRead()) {
                try {
                    deleteZipDir(compressedPath);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    public void onFileChange(File file) {
    }

    @Override
    public void onFileDelete(File file) {
    }

    @Override
    public void onStop(FileAlterationObserver observer) {
        super.onStop(observer);
        if (compressedPath == null) {
            if (!neatTerminalOutput) {
                System.out.println("未发现新备份文件");
                System.out.println("========================================");
            }
            return;
        } else {
            System.out.println(sdf.format(new Date()) + " 删除成功");
            System.out.println("========================================");
        }
        compressedPath = null;
    }

    public static void deleteZipDir(String fileDir) throws InterruptedException {
        Thread.sleep(deletionDelay);
        System.out.println(sdf.format(new Date()) + " 正在删除: " + deletionTarget + "...");
        deletionTarget.forEach(dir -> removeDirFromZipArchive(fileDir, dir));
        Thread.sleep(2000);
    }

    public static void removeDirFromZipArchive(String file, String removeDir) {
        try (ZipFile zipFile = new ZipFile(file)) {
            if (!removeDir.endsWith("/")) {
                zipFile.removeFile(removeDir);
                return;
            }
            // 遍历压缩文件中所有的FileHeader, 将指定删除目录下的子文件名保存起来
            List<FileHeader> headersList = zipFile.getFileHeaders();
            ArrayList<String> removeHeaderNames = new ArrayList<>();
            for (FileHeader subHeader : headersList) {
                String subHeaderName = subHeader.getFileName();
                if (subHeaderName.startsWith(removeDir)
                        && !subHeaderName.equals(removeDir)) {
                    removeHeaderNames.add(subHeaderName);
                }
            }
            // 遍历删除指定目录下的所有子文件(所有子文件删除完毕，该目录自动删除)
            for (String headerNameString : removeHeaderNames) {
                zipFile.removeFile(headerNameString);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
