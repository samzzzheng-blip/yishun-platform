package com.kiss.yishun.service;

import com.kiss.yishun.config.UploadConfig;
import com.kiss.yishun.dao.GradingJobDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.util.UUID;

/** New intake photos are outside all public upload directories. */
@Service
public class GradingPhotoStore {
    public static final String PREFIX="/api/usr/gradingPhotoFile/";
    @Value("${grading.private-photo-dir:${user.home}/.yishun-private/grading}") private String directory;
    @Autowired private GradingJobDao jobs;
    @Autowired private UploadConfig upload;
    public String store(byte[] bytes,boolean png,String owner) throws IOException {
        Path root=Paths.get(directory);Files.createDirectories(root);
        String name=UUID.randomUUID()+(png?".png":".jpg");
        Files.write(root.resolve(name+".owner"),owner.getBytes(StandardCharsets.UTF_8),StandardOpenOption.CREATE_NEW);
        Files.write(root.resolve(name),bytes,StandardOpenOption.CREATE_NEW);
        // Imports are atomic: never leave a newly imported private image after rollback.
        if(org.springframework.transaction.support.TransactionSynchronizationManager.isSynchronizationActive())
            org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(new org.springframework.transaction.support.TransactionSynchronizationAdapter(){
                @Override public void afterCompletion(int status){if(status!=STATUS_COMMITTED)try{
                    Files.deleteIfExists(root.resolve(name));Files.deleteIfExists(root.resolve(name+".owner"));
                }catch(IOException e){org.slf4j.LoggerFactory.getLogger(GradingPhotoStore.class).warn("Could not clean rolled-back private photo",e);}}
            });
        return PREFIX+name;
    }
    private String name(String path) {
        if(path==null || !path.matches("/api/usr/gradingPhotoFile/[a-f0-9-]{36}\\.(jpg|png)"))
            throw new IllegalArgumentException("照片不存在或无权访问");
        return path.substring(PREFIX.length());
    }
    public void check(String path,String actor) {
        String name=name(path);
        try {
            String owner=new String(Files.readAllBytes(Paths.get(directory,name+".owner")),StandardCharsets.UTF_8);
            if(!owner.equals(actor) && jobs.countPublishedPhoto(path)==0) throw new IllegalArgumentException("照片不存在或无权访问");
        } catch(IOException e) {throw new IllegalArgumentException("照片不存在或无权访问");}
    }
    public byte[] read(String path,String actor) throws IOException {
        check(path,actor);return Files.readAllBytes(Paths.get(directory,name(path)));
    }
    public String publish(String path,String actor) {
        if(!path.startsWith(PREFIX)) return path; // Existing public records remain compatible.
        check(path,actor);
        try {
            Path target=Paths.get(upload.getDiskPreciousDir(),"workflow");Files.createDirectories(target);
            String file=UUID.randomUUID()+ (path.endsWith(".png")?".png":".jpg");
            Files.copy(Paths.get(directory,name(path)),target.resolve(file));
            final Path published=target.resolve(file);
            if(org.springframework.transaction.support.TransactionSynchronizationManager.isSynchronizationActive())
                org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(new org.springframework.transaction.support.TransactionSynchronizationAdapter(){
                    @Override public void afterCompletion(int status){if(status!=STATUS_COMMITTED)try{Files.deleteIfExists(published);}catch(IOException e){org.slf4j.LoggerFactory.getLogger(GradingPhotoStore.class).warn("Could not clean rolled-back grading photo",e);}}
                });
            return "/upload/precious/workflow/"+file;
        } catch(IOException e) {throw new IllegalArgumentException("成品照片发布失败，请重试");}
    }
}
