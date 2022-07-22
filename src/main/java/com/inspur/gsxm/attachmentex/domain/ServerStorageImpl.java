package com.inspur.gsxm.attachmentex.domain;

import com.inspur.edp.svc.document.storage.domain.manager.AbsDocStorageImpl;
import com.inspur.edp.svc.document.storage.entity.GspDocMetadata;
import com.inspur.gsxm.attachmentex.utils.FileUtils;
import io.iec.edp.caf.commons.exception.CAFRuntimeException;
import io.iec.edp.caf.commons.exception.ExceptionLevel;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.InetAddress;
import java.util.Objects;
import java.util.UUID;

/**
 * @author tianjinzan01
 * @description 服务器直存
 * @date 2021-12-31
 */
@Slf4j
public class ServerStorageImpl extends AbsDocStorageImpl {

    /**
     * 基路径获取
     */
    private static final String SOURCE_PATH = FileUtils.getSourcePath();

    /**
     * 路径拼接符.
     */
    private static final String DOC_TAG = File.separator;

    /**
     * 实现文档上传.
     * @param directoryId 文档目录
     * @param metadata 文档元数据
     * @param content 文档内容
     * @param storageId 文档存储配置ID
     * @return 文档存储绝对路径
     */
    @Override
    protected String doUpload(final String directoryId, final GspDocMetadata metadata, final byte[] content, final String storageId, final boolean b) {
        InetAddress address = null;
        try {
            //获取当前ip
            address = InetAddress.getLocalHost();
            //上传文件名
            String fileName = UUID.randomUUID() + "." + metadata.getDocType();
            //返回路径
            String relativePath = DOC_TAG + metadata.getRootId() + DOC_TAG + directoryId + DOC_TAG + fileName;
            //存储路径
            String dirPath = SOURCE_PATH + DOC_TAG + metadata.getRootId() + DOC_TAG + directoryId;
            //创建文件夹
            FileUtils.createDirectoryNio(dirPath);
            //文件存储
            FileUtils.writeFileNio(content, dirPath + DOC_TAG + fileName);
            return relativePath;
        } catch (Exception e) {
            throw new CAFRuntimeException("dfs", "dfs_localstorage_error", " ip:" + Objects.requireNonNull(address).getHostAddress() + " 服务器直存方式上传文件失败", e, ExceptionLevel.Error, true);
        }
    }

    /**
     * 实现删除文档.
     * @param fileId 文档存储绝对路径
     * @param storageId 文档存储配置ID
     * @return 返回删除结果
     */
    @Override
    protected boolean doDelete(final String fileId, final String storageId) {
        InetAddress address = null;
        try {
            //获取当前ip
            address = InetAddress.getLocalHost();
            //实际路径
            String relativePath = SOURCE_PATH + DOC_TAG + fileId;
            return FileUtils.deleteFileNio(relativePath);
        } catch (Exception e) {
            throw new CAFRuntimeException("dfs", "dfs_localstorage_error", " ip:" + Objects.requireNonNull(address).getHostAddress() + "服务器直存方式上传文件失败", e, ExceptionLevel.Error, true);
        }
    }

    /**
     *实现更新文档.
     * @param directoryId 文档目录
     * @param metadata 文档元数据
     * @param content 文档内容
     * @param storageId 文档存储配置ID
     */
    @Override
    protected String doUpdate(final String directoryId, final GspDocMetadata metadata, final byte[] content, final String storageId) {
        return null;
    }

    /**
     * 实现获取文档内容.
     * @param fileId 文档存储绝对路径
     * @param storageId 文档存储配置ID
     * @return 文档内容
     */
    @Override
    protected byte[] doGet(final String fileId, final String storageId, final Boolean aBoolean) {
        InetAddress address = null;
        try {
            //获取当前ip
            address = InetAddress.getLocalHost();
            //实际路径
            String relativePath = SOURCE_PATH + fileId;
            //文件读取
            return FileUtils.inputStream2ByteArray(relativePath);
        } catch (Exception e) {
            throw new CAFRuntimeException("dfs", "dfs_localstorage_error", " ip:" + Objects.requireNonNull(address).getHostAddress() + " 服务器直存方式获取文件失败", e, ExceptionLevel.Error, true);
        }
    }
}
