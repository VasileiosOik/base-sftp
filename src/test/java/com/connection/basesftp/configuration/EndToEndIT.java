package com.connection.basesftp.configuration;

import com.connection.basesftp.Application;
import com.jcraft.jsch.ChannelSftp;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;
import org.springframework.integration.support.MutableMessage;
import org.springframework.messaging.Message;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.awaitility.Awaitility.await;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@TestPropertySource(locations = "classpath:test.properties")
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class})
public class EndToEndIT {

    @Value("${sftp.location}")
    private String sftpuserSftpTestArea;

    @Autowired
    private SftpRemoteFileTemplate xmlTemplate;

    @Test
    public void sendFileToSFTP_shouldBeTransfered() throws IOException {
        Path newFilePath = Files.createTempFile(Paths.get("/home/bill/Documents"), "test-file-", ".xml");
        Files.write(newFilePath, "bill, man\n".getBytes());

        try {

            ChannelSftp.LsEntry[] list = xmlTemplate.list(sftpuserSftpTestArea);
            for (ChannelSftp.LsEntry lsEntry : list) {
                System.out.println(lsEntry);
            }

            if (xmlTemplate.exists(sftpuserSftpTestArea)) {
                System.out.println("delete");
                Stream.of(xmlTemplate.list(sftpuserSftpTestArea)).filter(f -> f.getFilename().endsWith(".csv") || f.getFilename().endsWith(".tmp"))
                        .map(f -> sftpuserSftpTestArea + f.getFilename()).forEach(f -> xmlTemplate.remove(f));
            }

            Message<File> message = new MutableMessage<>(newFilePath.toFile());

            xmlTemplate.send(message, sftpuserSftpTestArea);

            await().atMost(10, TimeUnit.SECONDS).ignoreExceptions().until(() ->
                    Stream.of(xmlTemplate.list(sftpuserSftpTestArea)).noneMatch(lsEntry -> lsEntry.getFilename().equals(newFilePath.getFileName().toString())));
        } finally {
            Files.delete(newFilePath);
        }


    }
}
