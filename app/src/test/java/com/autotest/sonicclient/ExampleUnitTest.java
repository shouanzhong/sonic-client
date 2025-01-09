package com.autotest.sonicclient;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.autotest.sonicclient.utils.MinioUtil;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.minio.BucketExistsArgs;
import io.minio.DownloadObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.MinioException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    private MinioClient minioClient;
    private String bucketName;

    @Test
    public void addition_isCorrect() {
        String s = "[40,373][272,493]";
        Pattern compile = Pattern.compile("(\\d+)");
        Matcher matcher = compile.matcher(s);
        ArrayList<Integer> aList = new ArrayList<>();
        // 遍历所有匹配项
        while (matcher.find()) {
            String val = matcher.group();
            aList.add(Integer.parseInt(val));
            // 输出匹配到的数字
            System.out.println(val);
        }
        int x = (aList.get(0) + aList.get(2)) / 2;
        int y = (aList.get(1) + aList.get(3)) / 2;
        System.out.printf("x: %s, y: %s%n", x, y);
    }

    public void init() {
        try {
            // Create a minioClient with the MinIO server playground, its access key and secret key.
            String accessKey = "Q3AM3UQ867SPQQA43P2F";
            String secretKey = "zuf+tfteSlswRu7BJ86wekitnifILbZam1KYY3TG";
            String endpoint = "https://play.min.io";
            minioClient =
                    MinioClient.builder()
                            .endpoint(endpoint)
                            .credentials(accessKey, secretKey)
                            .build();

            // Make 'asiatrip' bucket if not exist.
            bucketName = "com.android.test";
            boolean found =
                    minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                // Make a new bucket called 'asiatrip'.
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            } else {
                System.out.println("Bucket 'asiatrip' already exists.");
            }
        } catch (ErrorResponseException e) {
            e.printStackTrace();
        } catch (InsufficientDataException e) {
            e.printStackTrace();
        } catch (InternalException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidResponseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (XmlParserException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test_minIOUtil_Upload() throws Exception {
        String fileNameInServer = "upload_test.zip";
        String fileNameForUpload = "E:\\Download\\amis-master\\node_modules\\node-xlsx\\node_modules\\xlsx\\dist\\jszip.js";
        MinioUtil.builder().setBucketName("com.autotest.sonicclient").build().upload(fileNameForUpload, fileNameInServer);
    }

    @Test
    public void test_minIOUtil_DownLoad() throws Exception {
        String fileNameInServer = "upload_test.zip";
        String fileNameDownload = "jszip.js";
        MinioUtil.builder().setBucketName("com.autotest.sonicclient").build().download(fileNameInServer, fileNameDownload);
    }


    @Test
    public void test_minIO_Upload() {
        try {
            // Upload '/home/user/Photos/asiaphotos.zip' as object name 'asiaphotos-2015.zip' to bucket
            // 'asiatrip'.
            String fileNameInServer = "asiaphotos-2015.zip";
            String fileNameForUpload = "E:\\Download\\amis-master\\node_modules\\node-xlsx\\node_modules\\xlsx\\dist\\jszip.js";
            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileNameInServer)
                            .filename(fileNameForUpload)
                            .build());
            System.out.println(
                    "'/home/user/Photos/asiaphotos.zip' is successfully uploaded as "
                            + "object 'asiaphotos-2015.zip' to bucket 'asiatrip'.");
        } catch (MinioException e) {
            System.out.println("Error occurred: " + e);
            System.out.println("HTTP trace: " + e.httpTrace());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void test_minIO_Download() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        String fileNameInServer = "asiaphotos-2015.zip";
        String localFileName = "fileDownloadFromMinIO.zip";
        minioClient.downloadObject(
                DownloadObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileNameInServer)
                        .filename(localFileName)
                        .build());
    }

    @Test
    public void JsonAndObject() {
        JSONArray objects = new JSONArray();
        objects.add(new Person());
        System.out.println(objects);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("array", objects);
        System.out.println(jsonObject);
    }

    class Person {
        String name="zhangsan";

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "Person{" +
                    "name='" + name + '\'' +
                    '}';
        }
    }

    @Test
    public void test_changeValueInJsonObject() {
        JSONObject suit = new JSONObject();
        JSONArray list = new JSONArray();
//        JSONObject caseInfo = new JSONObject();
//        caseInfo.put("name", "case1");
//        caseInfo.put("logPath", "path1");
//        list.add(caseInfo);
        Person person = new Person();
        list.add(person);
        suit.put("cases", list);
        System.out.println(suit);
        changeName(suit);
        System.out.println(suit);
    }

    private void changeName(JSONObject suit) {
        JSONArray cases = suit.getJSONArray("cases");
        for (int i = 0; i < cases.size(); i++) {
            Person o = cases.getObject(i, Person.class);
            o.setName("path2");
            cases.set(i, o);
        }
    }
}