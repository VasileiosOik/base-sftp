package com.connection.basesftp.configuration;

import com.jcraft.jsch.ChannelSftp.LsEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.integration.annotation.*;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.file.filters.ChainFileListFilter;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.handler.advice.ExpressionEvaluatingRequestHandlerAdvice;
import org.springframework.integration.sftp.filters.SftpPersistentAcceptOnceFileListFilter;
import org.springframework.integration.sftp.filters.SftpSimplePatternFileListFilter;
import org.springframework.integration.sftp.inbound.SftpStreamingMessageSource;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;
import org.springframework.integration.transformer.StreamTransformer;

import java.io.File;
import java.io.InputStream;

@Configuration
public class SftpConfiguration {


    private static final String STREAM = "stream";
    private static final String DATA = "data";

    @Value("${sftp.host}")
    private String sftpHostName;

    @Value("${sftp.user}")
    private String sftpUserName;

    @Value("${sftp.password}")
    private String sftpPassword;

    @Value("${sftp.port}")
    private int sftpPort;

    private final SftpRedisConfiguration sftpRedisConfiguration;

    @Autowired
    public SftpConfiguration(SftpRedisConfiguration sftpRedisConfiguration) {
        this.sftpRedisConfiguration = sftpRedisConfiguration;
    }


    @Bean
    public SessionFactory<LsEntry> sftpSessionFactory() {
        DefaultSftpSessionFactory factory = new DefaultSftpSessionFactory();
        factory.setHost(sftpHostName);
        factory.setPort(sftpPort);
        factory.setUser(sftpUserName);
        factory.setPassword(sftpPassword);
        factory.setAllowUnknownKeys(true);
        return new CachingSessionFactory<>(factory);
    }


    @Bean
    @InboundChannelAdapter(channel = STREAM, poller = @Poller(fixedDelay = "5000"))
    public MessageSource<InputStream> sftpMessageSource() {
        SftpStreamingMessageSource messageSource = new SftpStreamingMessageSource(xmlTemplate());
        messageSource.setRemoteDirectory("/sftpuser/sftp-test-area/");
        ChainFileListFilter<LsEntry> chainFileListFilter = new ChainFileListFilter<>();
        chainFileListFilter.addFilters(new SftpSimplePatternFileListFilter("*.xml"),
                new SftpPersistentAcceptOnceFileListFilter(sftpRedisConfiguration.redisMetadataStore(), "sftp-file-"));
        messageSource.setFilter(chainFileListFilter);
        messageSource.setMaxFetchSize(1);
        return messageSource;
    }

    @Bean
    public SftpRemoteFileTemplate xmlTemplate() {
        SftpRemoteFileTemplate sftpRemoteFileTemplate = new SftpRemoteFileTemplate(sftpSessionFactory());
        sftpRemoteFileTemplate.setRemoteDirectoryExpression(new LiteralExpression("/"));
        return sftpRemoteFileTemplate;
    }

    @Bean
    @Transformer(inputChannel = STREAM, outputChannel = DATA)
    public org.springframework.integration.transformer.Transformer transformer() {
        return new StreamTransformer();
    }

    @Bean
    public ExpressionEvaluatingRequestHandlerAdvice after() {
        System.out.println("advice");
        ExpressionEvaluatingRequestHandlerAdvice advice = new ExpressionEvaluatingRequestHandlerAdvice();
        advice.setOnSuccessExpressionString(ifFileExists() + thenDelete() + otherwiseNothing());
        advice.setPropagateEvaluationFailures(true);
        return advice;
    }

    private String otherwiseNothing() {
        System.out.println("no action");
        return ": true";
    }

    private String thenDelete() {
        System.out.println("diagrafw");
        return "@xmlTemplate.remove(headers['file_remoteDirectory'] + headers['file_remoteFile'])";
    }

    private String ifFileExists() {
        System.out.println("uparxei??????????");
        return "@xmlTemplate.exists(headers['file_remoteDirectory'] + headers['file_remoteFile']) ?";
    }

    @MessagingGateway
    public interface MyGateway {

        @Gateway(requestChannel = "toSftpChannel")
        void sendToSftp(File file);

    }
}
