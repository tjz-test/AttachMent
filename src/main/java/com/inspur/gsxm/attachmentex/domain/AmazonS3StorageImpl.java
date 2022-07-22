package com.inspur.gsxm.attachmentex.domain;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.inspur.edp.svc.document.storage.domain.manager.AbsDocStorageImpl;
import com.inspur.edp.svc.document.storage.entity.GspDocMetadata;
import com.inspur.gsxm.attachmentex.cache.AmazonS3Cache;
import com.inspur.gsxm.attachmentex.entity.AmazonParamInfo;
import com.inspur.gsxm.attachmentex.utils.FileUtils;
import io.iec.edp.caf.commons.exception.CAFRuntimeException;
import io.iec.edp.caf.commons.exception.ExceptionLevel;
import lombok.extern.slf4j.Slf4j;
import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.InetAddress;
import java.util.Objects;
import java.util.UUID;

/**
 * @author tianjinzan01
 * @description s3存储
 * @date 2021-11-30
 */

@Slf4j
public class AmazonS3StorageImpl extends AbsDocStorageImpl {

    /**
     * 路径拼接符号
     */
    private static final String DOC_TAG = File.separator;

    /**
     * 注入连接信息
     */
    @Resource(name = "amazonS3Cache")
    private AmazonS3Cache amazonS3Cache;

    /**
     * 创建s3连接
     * @return 返回连接
     */
    private AmazonS3 getAmazonS3(AmazonParamInfo amazonParamInfo) {
        try {
            //地址拼接
            String url = amazonParamInfo.getAmazonS3Protocol() + "://" + amazonParamInfo.getAmazons3ip();
            //s3连接参数配置
            AWSCredentials credentials = new BasicAWSCredentials(amazonParamInfo.getAccessKey(),amazonParamInfo.getSecretKey());
            ClientConfiguration conf = new ClientConfiguration();
            conf.setSignerOverride("AWSS3V4SignerType");
            // 设置AmazonS3使用的最大连接数
            conf.setMaxConnections(amazonParamInfo.getAmazonS3MaxConnections());
            // 设置socket超时时间
            conf.setSocketTimeout(amazonParamInfo.getAmazonS3SocketTimeout());
            // 设置失败请求重试次数
            conf.setMaxErrorRetry(amazonParamInfo.getAmazonS3MaxErrorRetry());
            // 设置协议
            switch (amazonParamInfo.getAmazonS3Protocol()) {
                case "https":
                    conf.setProtocol(Protocol.HTTPS);
                    break;
                case "http":
                    conf.setProtocol(Protocol.HTTP);
                    break;
                default:
                    throw new RuntimeException("未获取到Protocol参数");
            }
            //创建连接
            AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(credentials))
                    .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(url, Regions.US_EAST_1.name()))
                    .withClientConfiguration(conf)
                    .build();
            //检查桶是否存在，不存在则创建创建
            if (!s3Client.doesBucketExistV2(amazonParamInfo.getBucketName())) {
                CreateBucketRequest request = new CreateBucketRequest(amazonParamInfo.getBucketName());
                s3Client.createBucket(request);
            }
            return s3Client;
        } catch(Exception e) {
            throw new CAFRuntimeException("dfs", "dfs_amazons3_error", "amazons3连接失败", e, ExceptionLevel.Error, true);
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
        InetAddress address = null;
        try (InputStream inputStream = new ByteArrayInputStream(content)) {
            address = InetAddress.getLocalHost();
            //s3连接参数获取
            AmazonParamInfo amazonParamInfo = amazonS3Cache.getAmazonS3Conf();
            //创建amazons3连接
            AmazonS3 s3Client = getAmazonS3(amazonParamInfo);
            //上传文件名
            String fileName = UUID.randomUUID() + "." + metadata.getDocType();
            //返回存储路径
            String relativePath = DOC_TAG + metadata.getRootId() + DOC_TAG + directoryId + DOC_TAG + fileName;
            //获取文件流md5哈希
            String requestEtag = FileUtils.getMd5OfFile(content);
            //上传参数
            ObjectMetadata metaRequest = new ObjectMetadata();
            metaRequest.setContentLength(inputStream.available());
            //上传
            PutObjectResult result =s3Client.putObject(amazonParamInfo.getBucketName(), fileName, inputStream, metaRequest);
            //验证文件完整性
            if (!requestEtag.equals(result.getETag())) {
                throw new RuntimeException("上传文件不完整");
            }
            return relativePath;
        } catch (Exception e) {
            throw new CAFRuntimeException("dfs", "dfs_amazons3_error", " ip:" + Objects.requireNonNull(address).getHostAddress() + " amazons3方式上传文件失败", e, ExceptionLevel.Error, true);
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
        InetAddress address = null;
        try {
            address = InetAddress.getLocalHost();
            //s3连接参数获取
            AmazonParamInfo amazonParamInfo = amazonS3Cache.getAmazonS3Conf();
            //创建amazons3连接
            AmazonS3 s3Client = getAmazonS3(amazonParamInfo);
            //删除
            String fileName = fileId.substring(fileId.lastIndexOf(DOC_TAG) + 1);
            s3Client.deleteObject(new DeleteObjectRequest(amazonParamInfo.getBucketName(), fileName));
            return true;
        } catch (Exception e) {
            throw new CAFRuntimeException("dfs", "dfs_amazons3_error", " ip:" + Objects.requireNonNull(address).getHostAddress() + "amazons3方式删除文件失败", e, ExceptionLevel.Error, true);
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
        InetAddress address = null;
        try {
            address = InetAddress.getLocalHost();
            //s3连接参数获取
            AmazonParamInfo amazonParamInfo = amazonS3Cache.getAmazonS3Conf();
            //创建amazons3连接
            AmazonS3 s3Client = getAmazonS3(amazonParamInfo);
            //文件名获取
            String fileName = fileId.substring(fileId.lastIndexOf(DOC_TAG) + 1);
            //获取文件
            S3Object fileObject = s3Client.getObject(amazonParamInfo.getBucketName(),fileName);
            //文件流处理
            try (BufferedInputStream bis = new BufferedInputStream(fileObject.getObjectContent()); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                int date;
                while ((date = bis.read()) != -1) {
                    bos.write(date);
                }
                return bos.toByteArray();
            }
        } catch (Exception e) {
            throw new CAFRuntimeException("dfs", "dfs_amazons3_error", " ip:" + Objects.requireNonNull(address).getHostAddress() + "amazons3方式获取文件失败", e, ExceptionLevel.Error, true);
        }
    }

    /**
     * 判断图片是否完整
     * @param bytes 文件流
     * @return 是否图片
     */
    private boolean isImg(byte[] bytes) {
        try {
            BufferedImage bufImg = ImageIO.read(new ByteArrayInputStream(bytes));
            return bufImg == null;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
