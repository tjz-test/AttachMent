package com.inspur.gsxm.attachmentex.domain;

import com.emc.ecs.nfsclient.nfs.NfsCreateMode;
import com.emc.ecs.nfsclient.nfs.NfsSetAttributes;
import com.emc.ecs.nfsclient.nfs.io.Nfs3File;
import com.emc.ecs.nfsclient.nfs.io.NfsFileInputStream;
import com.emc.ecs.nfsclient.nfs.io.NfsFileOutputStream;
import com.emc.ecs.nfsclient.nfs.nfs3.Nfs3;
import com.emc.ecs.nfsclient.rpc.CredentialUnix;
import com.inspur.edp.svc.document.storage.domain.manager.AbsDocStorageImpl;
import com.inspur.edp.svc.document.storage.entity.GspDocMetadata;
import com.inspur.gsxm.attachmentex.cache.NfsCache;
import com.inspur.gsxm.attachmentex.entity.NfsParamInfo;
import io.iec.edp.caf.commons.exception.CAFRuntimeException;
import io.iec.edp.caf.commons.exception.ExceptionLevel;
import lombok.extern.slf4j.Slf4j;
import javax.annotation.Resource;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.util.UUID;

/**
 * @author tianjinzan01
 * @description nfs存储扩展
 * @date 2021-12-04
 */
@Slf4j
public class NfsStorageImpl extends AbsDocStorageImpl {

    @Resource(name = "nfsCache")
    private NfsCache nfsCache;

    private final NfsSetAttributes nfsSetAttr;

    private Nfs3 nfsClient;

    public NfsStorageImpl() {
        this.nfsSetAttr = new NfsSetAttributes();
    }

    /**
     * 创建连接
     */
    private void createClient() {
        try {
            NfsParamInfo nfsParamInfo = nfsCache.getNfsParamInfo();
            this.nfsSetAttr.setMode(510L);
            this.nfsClient = new Nfs3(nfsParamInfo.getNfsIp(), nfsParamInfo.getNfsRoot(), new CredentialUnix(nfsParamInfo.getNfsUid(), nfsParamInfo.getNfsGid(), null), 3);
        } catch (Exception e) {
            throw new CAFRuntimeException("dfs", "", "nfs创建连接失败", e, ExceptionLevel.Error, true);
        }
    }

    /**
     * 创建目录
     * @param dirPath 路径
     */
    private void createDirectory(String dirPath) {
        if (dirPath.contains("\\")) {
            dirPath = dirPath.replace("\\", "/");
        }
        if (dirPath.startsWith("/")) {
            dirPath = dirPath.substring(1);
        }
        String[] foldList = dirPath.split("/");
        StringBuilder path = new StringBuilder();
        path.append("/");
        try {
            for (String fold : foldList) {
                path.append(fold);
                Nfs3File filePath = new Nfs3File(this.nfsClient, String.valueOf(path));
                if (!filePath.exists()) {
                    filePath.mkdir(this.nfsSetAttr);
                }
                path.append("/");
            }
        } catch (Exception e) {
            throw new CAFRuntimeException("dfs", "", "nfs创建目录失败", e, ExceptionLevel.Error, true);
        }
    }

    /**
     * 创建文件
     * @param rootPath 绝对路径
     * @param fileName 文件名
     * @param fileContent 文件内容
     */
    private void createFile(String rootPath, String fileName, byte[] fileContent) {
        try {
            createDirectory(rootPath);
            Nfs3File desFile = new Nfs3File(this.nfsClient, rootPath + "/" + fileName);
            desFile.create(NfsCreateMode.GUARDED, this.nfsSetAttr, null);
            NfsFileOutputStream outputStream = new NfsFileOutputStream(desFile);
            outputStream.write(fileContent);
            outputStream.close();
        } catch (Exception e) {
            throw new CAFRuntimeException("dfs", "", "nfs创建文件失败", e, ExceptionLevel.Error, true);
        }
    }

    /**
     * 实现文档上传
     * @param directoryId 文档目录
     * @param metadata 文档元数据
     * @param content 文档内容
     * @param storageId 文档存储配置ID
     * @return 文档存储绝对路径
     */
    @Override
    public String doUpload(String directoryId, GspDocMetadata metadata, byte[] content, String storageId, boolean handleDoc) {
        try{
            createClient();
            String relativePath = "/" + metadata.getRootId() + "/" + directoryId;
            String extension = metadata.getDocType();
            String key = UUID.randomUUID() + "." + extension;
            createFile(relativePath, key, content);
            log.error("nfs上传文档成功，文档绝对路径" + relativePath + "/" + key);
            return relativePath + "/" + key;
        }catch (Exception e){
            throw new CAFRuntimeException("dfs", "", "nfs方式上传文件失败", e, ExceptionLevel.Error, true);
        }
    }

    /**
     * 实现删除文档
     * @param fileId 文档存储绝对路径
     * @param storageId 文档存储配置ID
     * @return 返回删除结果
     */
    @Override
    public boolean doDelete(String fileId, String storageId) {
        try{
            createClient();
            Nfs3File desFile = new Nfs3File(this.nfsClient, fileId);
            if (desFile.exists()) {
                desFile.delete();
            }
            log.error("nfs删除文档成功，文档绝对路径" + fileId);
            return true;
        }catch(Exception e){
            throw new CAFRuntimeException("dfs", "", "nfs方式删除文件失败", e, ExceptionLevel.Error, true);
        }
    }

    /**
     *实现更新文档
     * @param directoryId 文档目录
     * @param metadata 文档元数据
     * @param content 文档内容
     * @param storageId 文档存储配置ID
     */
    @Override
    public String doUpdate(String directoryId, GspDocMetadata metadata, byte[] content, String storageId) {
        return null;
    }

    /**
     * 实现获取文档内容
     * @param fileId 文档存储绝对路径
     * @param storageId 文档存储配置ID
     * @return 文档内容
     */
    @Override
    public byte[] doGet(String fileId, String storageId, Boolean isView) {
        try{
            createClient();
            Nfs3File file = new Nfs3File(this.nfsClient, fileId);
            NfsFileInputStream inputStream = new NfsFileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(inputStream);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            int date;
            while ((date = bis.read()) != -1) {
                bos.write(date);
            }
            log.error("文档预览成功");
            return bos.toByteArray();
        }catch(Exception e){
            throw new CAFRuntimeException("dfs", "", "nfs方式获取文件失败", e, ExceptionLevel.Error, true);
        }
    }
}
