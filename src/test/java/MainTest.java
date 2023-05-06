import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainTest {
    public static void main(String[] args) throws ZipException {
        ZipFile zipFile = new ZipFile("D:\\Jetbrain Projects\\Practical_Projects\\mc-backup-filter\\src\\backups\\2023\\5\\1 - 副本.zip");
        System.out.println(zipFile.getFileHeaders());
        removeDirFromZipArchive(
                "D:\\Jetbrain Projects\\Practical_Projects\\mc-backup-filter\\src\\backups\\2023\\5\\1 - 副本.zip",
                "world/DIM64/");
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
