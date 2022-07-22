package com.inspur.gsxm.attachmentex.utils;

import com.inspur.edp.ubcc.param.api.ParamDataService;
import io.iec.edp.caf.commons.exception.CAFRuntimeException;
import io.iec.edp.caf.commons.exception.ExceptionLevel;
import io.iec.edp.caf.commons.utils.SpringBeanUtils;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author tianjinzan01
 * @description 文件存储工具类
 * @date 2021-12-31
 */
public final class FileUtils {

    private FileUtils() { }

    /**
     * 缓冲大小.
     */
    private static final int HC_LONG = 1024;

    /**
     * LINUX SPLIT.
     */
    private static final String DOC_PATH_SPLIT_LINUX = "\\";

    /**
     * WINDOW SPLIT.
     */
    private static final String DOC_PATH_SPLIT_WINDOW = "//";

    /**
     * SPLIT.
     */
    private static final String DOC_PATH_SPLIT = "/";

    /**
     * 基路径获取.
     * @return 返回基础路径
     */
    public static String getSourcePath() {
        return SpringBeanUtils.getBean(ParamDataService.class)
               .getValueAsString("1a5f1585-9497-43cc-9291-5497fe6b106d",
                        "d5ea21ea-76ee-c530-a31e-cee8c6aa00a7",
                        "sourcePath",
                        "");
    }

    /**
     * 创建文件夹a.
     * @param dirPath path
     */
    public static void createDirectoryNio(final String dirPath) {
        String filePath = dirPath;
        if (filePath.contains(DOC_PATH_SPLIT_LINUX)) {
            filePath = filePath.replace(DOC_PATH_SPLIT_LINUX, DOC_PATH_SPLIT);
        }
        if (filePath.contains(DOC_PATH_SPLIT_WINDOW)) {
            filePath = filePath.replace(DOC_PATH_SPLIT_WINDOW, DOC_PATH_SPLIT);
        }
        try {
            //文件夹创建
            Files.createDirectories(Paths.get(filePath));
        } catch (Exception e) {
            throw new CAFRuntimeException("dfs", "", "服务器直存方式(文件夹创建)失败", e, ExceptionLevel.Error, true);
        }
    }

    /**
     * 删除文件.
     * @param fildPath 文件名
     * @return 返回删除结果
     */
    public static boolean deleteFileNio(final String fildPath) {
        String filePath = fildPath;
        if (filePath.contains(DOC_PATH_SPLIT_LINUX)) {
            filePath = filePath.replace(DOC_PATH_SPLIT_LINUX, DOC_PATH_SPLIT);
        }
        if (filePath.contains(DOC_PATH_SPLIT_WINDOW)) {
            filePath = filePath.replace(DOC_PATH_SPLIT_WINDOW, DOC_PATH_SPLIT);
        }
        try {
            Path path = Paths.get(filePath);
            //删除
            return Files.deleteIfExists(path);
        } catch (Exception e) {
            throw new CAFRuntimeException("dfs", "", "服务器直存方式(删除文件)失败", e, ExceptionLevel.Error, true);
        }
    }

    /**
     * write file nio.
     * @param fileName 文件名
     * @param content 内容
     */
    public static void writeFileNio(final byte[] content, final String fileName) {
        try (FileOutputStream fos = new FileOutputStream(fileName); FileChannel channel = fos.getChannel()) {
            ByteBuffer buffer = ByteBuffer.wrap(content);
            while (buffer.hasRemaining()) {
                channel.write(buffer);
            }
        } catch (Exception e) {
            throw new CAFRuntimeException("dfs", "", "服务器直存方式(文件写入)失败", e, ExceptionLevel.Error, true);
        }
    }

    /**
     * 文件读取.
     * @param fildPath 路径
     * @return 返回byte类型
     */
    public static byte[] inputStream2ByteArray(final String fildPath) {
        String filePath = fildPath;
        if (filePath.contains(DOC_PATH_SPLIT_LINUX)) {
            filePath = filePath.replace(DOC_PATH_SPLIT_LINUX, DOC_PATH_SPLIT);
        }
        if (filePath.contains(DOC_PATH_SPLIT_WINDOW)) {
            filePath = filePath.replace(DOC_PATH_SPLIT_WINDOW, DOC_PATH_SPLIT);
        }
        try (InputStream in = new FileInputStream(filePath);
             BufferedInputStream bis = new BufferedInputStream(in);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            int date;
            while ((date = bis.read()) != -1) {
                bos.write(date);
            }
            return bos.toByteArray();
        } catch (Exception e) {
            throw new CAFRuntimeException("dfs", "", "服务器直存方式(文件读取)失败", e, ExceptionLevel.Error, true);
        }
    }

    /**
     * md5加密.
     * @param content 内容
     * @return 加密结果
     */
    public static String getMd5OfFile(final byte[] content) {
        try (InputStream stream = new ByteArrayInputStream(content)) {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buf = new byte[HC_LONG];
            int len;
            while ((len = stream.read(buf)) > 0) {
                md.update(buf, 0, len);
            }
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte bite : digest) {
                sb.append(String.format("%02x", bite & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new CAFRuntimeException("dfs", "", "S3上传文件流加密失败", e, ExceptionLevel.Error, true);
        }
    }

    /**
     * nio文件权限设置
     * @param path 文件路径
     */
    private static void setPermission(Path path){
        // 检查操作系统是否支持posix。
        boolean supportPosix = FileSystems.getDefault().supportedFileAttributeViews().contains("posix");
        if (!supportPosix) {
            return;
        }
        // 将要设置的权限：用户，组，以及三组读/写/执行的权限
        String owner = "testowner";
        String group = "testgroup";
        PosixFilePermission[] permissions = {
                PosixFilePermission.OWNER_READ,
                PosixFilePermission.OWNER_WRITE,
                PosixFilePermission.OWNER_EXECUTE,
                PosixFilePermission.GROUP_READ,
                PosixFilePermission.GROUP_WRITE,
                PosixFilePermission.GROUP_EXECUTE,
                PosixFilePermission.OTHERS_READ,
                PosixFilePermission.OTHERS_WRITE,
                PosixFilePermission.OTHERS_EXECUTE,
        };
        try {
            changePermission(path, owner, group, permissions);
        } catch (Exception e) {
            throw new CAFRuntimeException("dfs", "", "服务器直存方式(设置文件夹权限)失败", e, ExceptionLevel.Error, true);
        }
    }

    /**
     * nio修改用户，组，以及三组读/写/执行的权限
     * @param path 路径
     * @param owner 属主
     * @param group 属组
     * @param permissions 权限参数
     */
    private static void changePermission(Path path, String owner, String group, PosixFilePermission... permissions) {
        try {
            // 设置permission，相当于linux命令chmod
            Set<PosixFilePermission> perms = new HashSet<>();
            Collections.addAll(perms, permissions);
            Files.setPosixFilePermissions(path, perms);
            // 设置用户和组，相当于linux命令chown
            // 要保证用户和组存在，否则lookupService会抛UserPrincipalNotFoundException
            UserPrincipalLookupService lookupService = FileSystems.getDefault().getUserPrincipalLookupService();
            GroupPrincipal groupPrincipal = lookupService.lookupPrincipalByGroupName(group);
            UserPrincipal userPrincipal = lookupService.lookupPrincipalByName(owner);
            PosixFileAttributeView view = Files.getFileAttributeView(path, PosixFileAttributeView.class);
            view.setGroup(groupPrincipal);
            view.setOwner(userPrincipal);
        } catch (Exception e) {
            throw new CAFRuntimeException("dfs", "", "服务器直存方式(属组或属主不存在)失败", e, ExceptionLevel.Error, true);
        }
    }

    /**
     * 文件夹属组、属主检测
     * @param path 文件夹路径
     * @param group 属组
     * @param owner 属主
     * @return 结果
     * @throws IOException error
     */
    private boolean checkFileAttribute(Path path, String group, String owner) throws IOException {
        boolean res = false;
        GroupPrincipal fileGroup = Files.readAttributes(path, PosixFileAttributes.class).group();
        UserPrincipal fileOwner = Files.readAttributes(path, PosixFileAttributes.class).owner();
        if (group.equals(fileGroup.toString()) && owner.equals(fileOwner.toString())) {
            res = true;
        }
        return res;
    }

}
