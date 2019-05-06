package com.infoclinika.mssharing.autoimporter.service;

import com.infoclinika.mssharing.autoimporter.service.api.internal.UploadService;
import com.infoclinika.mssharing.autoimporter.service.exception.ApplicationConfigException;
import com.infoclinika.mssharing.autoimporter.service.exception.MonitorException;
import com.infoclinika.mssharing.clients.common.web.api.exception.JsonConvertException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * author Ruslan Duboveckij
 */
public class ApplicationConsole {
    /* Config file sample from path - System.getProperty("user.home") +
    File.separator + configFolder(uploader.properties) + File.separator + "config.json"
    (C:\Users\ruslan.duboveckij\chorus-uploader\config.json)
    {
      "username" : "pavel.kaplin@gmail.com",
      "password" : "pwd",
      "contexts" : [ {
        "name" : "Second config",
        "folder" : "C:\\UploadFolderTestMany2",
        "started" : false,
        "labels" : "Test labels 2",
        "instrumentId" : 2,
        "specieId" : 14,
        "created" : 1382540535081
      }, {
        "name" : "First config",
        "folder" : "C:\\UploadFolderTestMany1",
        "started" : false,
        "labels" : "Test labels 1",
        "instrumentId" : 3,
        "specieId" : 4,
        "created" : 1382540535081
      }, {
        "name" : "Second config3",
        "folder" : "C:\\UploadFolderTestMany3",
        "started" : false,
        "labels" : "Test labels 2",
        "instrumentId" : 2,
        "specieId" : 14,
        "created" : 1382540535081
      } ]
    }*/

    public static void main(String[] args) throws Exception {
        ClassPathXmlApplicationContext context =
            new ClassPathXmlApplicationContext("classpath:autoimporter-context.xml");
        UploadService uploadService = context.getBean(UploadService.class);
        try {
            startApp(uploadService);
        } catch (JsonConvertException e) {
            e.printStackTrace();
        } catch (MonitorException e) {
            e.printStackTrace();
        } catch (ApplicationConfigException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    public static void startApp(UploadService uploadService) {
        uploadService.readAuthorization();
    }
}
