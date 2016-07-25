/**
 * Copyright 2015 Q24
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.kahu.hawaii.config;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.*;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.management.MBeanServer;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jolokia.jmx.JolokiaMBeanServerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.jmx.support.MBeanServerFactoryBean;
import org.springframework.jmx.support.RegistrationPolicy;

import io.kahu.hawaii.rest.DefaultResponseManager;
import io.kahu.hawaii.rest.ResponseManager;
import io.kahu.hawaii.service.io.FileChangeListener;
import io.kahu.hawaii.service.io.LocationHelper;
import io.kahu.hawaii.service.io.SiteMapGenerator;
import io.kahu.hawaii.service.mail.DefaultMailSender;
import io.kahu.hawaii.service.mail.FakeMailSender;
import io.kahu.hawaii.service.mail.HawaiiProperties;
import io.kahu.hawaii.service.mail.MailConnection;
import io.kahu.hawaii.service.mail.MailSender;
import io.kahu.hawaii.service.mail.SMTPMailConnection;
import io.kahu.hawaii.util.call.dispatch.ExecutorRepository;
import io.kahu.hawaii.util.call.dispatch.listener.LogCallIdListener;
import io.kahu.hawaii.util.call.configuration.RequestConfigurations;
import io.kahu.hawaii.util.call.dispatch.RequestDispatcher;
import io.kahu.hawaii.util.call.http.response.FileDownload;
import io.kahu.hawaii.util.call.log.CallLoggerImpl;
import io.kahu.hawaii.util.call.log.request.GenericRequestLogger;
import io.kahu.hawaii.util.call.log.request.HttpRequestLogger;
import io.kahu.hawaii.util.call.log.response.FileDownloadResponseLogger;
import io.kahu.hawaii.util.call.log.response.FileResponseLogger;
import io.kahu.hawaii.util.call.log.response.JsonArrayResponseLogger;
import io.kahu.hawaii.util.call.log.response.JsonObjectResponseLogger;
import io.kahu.hawaii.util.call.log.response.ObjectResponseLogger;
import io.kahu.hawaii.util.call.log.response.StreamResponseLogger;
import io.kahu.hawaii.util.call.log.response.StringResponseLogger;
import io.kahu.hawaii.util.encryption.CryptoUtil;
import io.kahu.hawaii.util.exception.ServerException;
import io.kahu.hawaii.util.logger.CoreLoggers;
import io.kahu.hawaii.util.logger.DefaultLogManager;
import io.kahu.hawaii.util.logger.LogManager;
import io.kahu.hawaii.util.logger.LogManagerConfiguration;
import io.kahu.hawaii.util.logger.LoggingConfiguration;
import io.kahu.hawaii.util.logger.LoggingConfigurationMBean;
import io.kahu.hawaii.util.spring.ApplicationContextProvider;

@Configuration
public class KahuConfig {

    final boolean isJunit;

    public KahuConfig() {
        this(false);
    }

    public KahuConfig(final boolean isJunit) {
        this.isJunit = isJunit;
    }

    public boolean isTestContext() {
        return isJunit;
    }

    // *************************************************************************
    // Autowire environment (to lookup property values)
    // *************************************************************************

    @Autowired
    private Environment env;

    // *************************************************************************
    // Hawaii log manager
    // *************************************************************************

    private String[] getStringArrayFromProperty(final String name) {
        String s = env.getProperty(name);
        if (s != null) {
            return s.split("\\s+");
        }
        return new String[0];
    }

    @Bean
    public LoggingConfiguration loggingConfiguration() {
        LoggingConfiguration config = new LoggingConfiguration();

        config.setTracingFields(getStringArrayFromProperty("logging.tracing.fields"));
        config.setSkippedLocationClasses(getStringArrayFromProperty("logging.skipped.location.classes"));

        config.setComplexityThreshold(env.getProperty("logging.complexity.threshold", int.class, config.getComplexityThreshold()));
        config.setMaxInfoRequestBodySize(env.getProperty("logging.max.request.bodysize.in.info", int.class, config.getMaxInfoResponseBodySize()));
        config.setMaxDebugRequestBodySize(env.getProperty("logging.max.request.bodysize.in.debug", int.class, config.getMaxDebugResponseBodySize()));
        config.setMaxInfoResponseBodySize(env.getProperty("logging.max.response.bodysize.in.info", int.class, config.getMaxInfoResponseBodySize()));
        config.setMaxDebugResponseBodySize(env.getProperty("logging.max.response.bodysize.in.debug", int.class, config.getMaxDebugResponseBodySize()));
        config.setMaxOutInfoRequestBodySize(env.getProperty("logging.max.request.bodysize.out.info", int.class, config.getMaxOutInfoResponseBodySize()));
        config.setMaxOutDebugRequestBodySize(env.getProperty("logging.max.request.bodysize.out.debug", int.class, config.getMaxOutDebugResponseBodySize()));
        config.setMaxOutInfoResponseBodySize(env.getProperty("logging.max.response.bodysize.out.info", int.class, config.getMaxOutInfoResponseBodySize()));
        config.setMaxOutDebugResponseBodySize(env.getProperty("logging.max.response.bodysize.out.debug", int.class, config.getMaxOutDebugResponseBodySize()));

        config.setUrlFields(getStringArrayFromProperty("logging.pwdmask.url.fields"));
        config.setParameterFields(getStringArrayFromProperty("logging.pwdmask.parameter.fields"));
        config.setHeaderFields(getStringArrayFromProperty("logging.pwdmask.header.fields"));
        config.setBodyFields(getStringArrayFromProperty("logging.pwdmask.body.fields"));
        config.setPasswordParameters(getStringArrayFromProperty("logging.pwdmask.password.parameters"));
        config.setBodyPasswordPatterns(getStringArrayFromProperty("logging.pwdmask.password.patterns"));
        config.setBodyPasswordFields(getStringArrayFromProperty("logging.pwdmask.password.fields"));
        return config;
    }

    @Bean
    public LogManagerConfiguration logManagerConfiguration() {
        LogManagerConfiguration logManagerConfiguration = new LogManagerConfiguration(loggingConfiguration());

        String[] loggers = getStringArrayFromProperty("logging.log.complete.call");
        for (String logger : loggers) {
            logManagerConfiguration.getOrCreateLoggingConfiguration(logger).enableCompleteCallLogging();
        }
        return logManagerConfiguration;
    }

    @Bean
    public LogManager logManager() {
        return new DefaultLogManager(logManagerConfiguration());
    }

    @Bean
    public LoggingConfigurationMBean loggingConfigurationMBean() {
        return new LoggingConfigurationMBean(logManager(), logManagerConfiguration());
    }

    // *************************************************************************
    // Hawaii file change listener
    // *************************************************************************

    @Bean(destroyMethod = "stop")
    @Scope(SCOPE_PROTOTYPE)
    public FileChangeListener fileChangeListener() {
        long maxFileLength = env.getProperty("uploads.maxfilelength", Long.class);
        return new FileChangeListener(maxFileLength, logManager());
    }

    // *************************************************************************
    // Hawaii location helper
    // *************************************************************************

    @Bean
    public LocationHelper locationHelper() {
        LocationHelper locationHelper = new LocationHelper();
        String csvHome = env.getProperty("locationhelper.csvhome.directory");
        if (csvHome != null && ! csvHome.isEmpty()) {
            locationHelper.setHawaiiCsvHome(csvHome);
        }
        return locationHelper;
    }

    // *************************************************************************
    // Hawaii mail sender setup
    // *************************************************************************

    @Bean
    public HawaiiProperties hawaiiMailProperties() throws ServerException {
        Properties properties = new Properties();
        properties.put("mail.transport.protocol", env.getProperty("mail.transport.protocol"));
        properties.put("mail.smtp.starttls.enable", env.getProperty("mail.smtp.starttls.enable"));
        properties.put("mail.smtp.host", env.getProperty("mail.smtp.host"));
        properties.put("mail.smtp.socketFactory.port", env.getProperty("mail.smtp.socketFactory.port"));
        properties.put("mail.smtp.socketFactory.class", env.getProperty("mail.smtp.socketFactory.class"));
        properties.put("mail.smtp.socketFactory.fallback", env.getProperty("mail.smtp.socketFactory.fallback"));
        properties.put("mail.smtp.auth", env.getProperty("mail.smtp.auth"));
        properties.put("mail.user", CryptoUtil.decrypt(env.getProperty("mail.user")));
        properties.put("mail.password", CryptoUtil.decrypt(env.getProperty("mail.password")));
        properties.put("mail.from", CryptoUtil.decrypt(env.getProperty("mail.from")));
        properties.put("mail.smtp.timeout", env.getProperty("mail.smtp.timeout"));
        properties.put("mail.smtp.connectiontimeout", env.getProperty("mail.smtp.connectiontimeout"));
        return new HawaiiProperties(properties);
    }

    @Bean
    public MailConnection mailConnection() throws ServerException {
        return new SMTPMailConnection(hawaiiMailProperties());
    }

    @Bean
    public MailSender mailSender() throws ServerException {
        String impl = env.getProperty("mail.impl");
        if ("default".equalsIgnoreCase(impl)) {
            return new DefaultMailSender(hawaiiMailProperties(), mailConnection());
        } else if ("fake".equalsIgnoreCase(impl)) {
            logManager().info(CoreLoggers.SERVER, "Container configured to fake mail sending");
            return new FakeMailSender(logManager());
        } else {
            throw new IllegalStateException("mail.impl property contains invalid value: " + impl);
        }
    }

    // *************************************************************************
    // Application context provider - used to interact with application context
    // in non Spring-managed instances (e.g. shop state machine)
    // *************************************************************************

    @Bean
    public ApplicationContextProvider applicationContextProvder() {
        return new ApplicationContextProvider();
    }

    // *************************************************************************
    // Sitemap generator
    // *************************************************************************

    @Bean
    public SiteMapGenerator siteMapGenerator() {
        String sitemapUrlDomain = env.getProperty("sitemap.url.domain");
        String sitemapSkipPages = env.getProperty("sitemap.skip.pages");
        return new SiteMapGenerator(sitemapUrlDomain, sitemapSkipPages);
    }

    // *************************************************************************
    // Hawaii repsonse manager
    // *************************************************************************

    @Bean
    public ResponseManager responseManager() {
        String loggingContextTxId = env.getProperty("logging.context.transactionId");
        return new DefaultResponseManager(logManager(), loggingContextTxId);
    }

    @Bean
    public MBeanServer jolokiaServer() {
        return JolokiaMBeanServerUtil.getJolokiaMBeanServer();
    }

    @Bean
    public MBeanServerFactoryBean mbeanServer() {
        MBeanServerFactoryBean mbeanServer = new MBeanServerFactoryBean();
        mbeanServer.setLocateExistingServerIfPossible(true);
        return mbeanServer;
    }

    @Bean
    public MBeanExporter mbeanExporter() throws ServerException {
        MBeanExporter mbeanExporter = new MBeanExporter();
        mbeanExporter.setServer(jolokiaServer());
        mbeanExporter.setRegistrationPolicy(RegistrationPolicy.IGNORE_EXISTING);
        Map<String, Object> beans = getMBeans();
        mbeanExporter.setBeans(beans);
        return mbeanExporter;
    }

    public Map<String, Object> getMBeans() throws ServerException {
        Map<String, Object> beans = new HashMap<String, Object>();
        beans.put("Hawaii:name=HawaiiMailProperties", hawaiiMailProperties());
        beans.put("Hawaii:name=RequestConfigurations", requestCongfigurations());
        return beans;
    }

    @Bean
    public RequestConfigurations requestCongfigurations() {
        return new RequestConfigurations();
    }

    @Bean(destroyMethod = "stop")
    public ExecutorRepository executorServiceRepository() throws IOException, JSONException {
        String config = env.getProperty("dispatcher.configuration.file");
        File configFile = new File(locationHelper().getHawaiiServerHome(), config);
        return new ExecutorRepository(configFile, logManager(), requestCongfigurations());
    }

    @Bean
    public RequestDispatcher requestDispatcher() throws IOException, JSONException {
        // TODO Inject HttpClientFactory
        return new RequestDispatcher(executorServiceRepository(), logManager(), new LogCallIdListener());
    }

    @Bean
    public CallLoggerImpl<Object> genericObjectCallLogger() {
        return new CallLoggerImpl<>(logManager(), new GenericRequestLogger(), new ObjectResponseLogger());
    }

    @Bean
    public CallLoggerImpl<String> httpStringCallLogger() {
        return new CallLoggerImpl<>(logManager(), new HttpRequestLogger(), new StringResponseLogger());
    }

    @Bean
    public CallLoggerImpl<JSONObject> httpJsonObjectCallLogger() {
        return new CallLoggerImpl<>(logManager(), new HttpRequestLogger(), new JsonObjectResponseLogger());
    }

    @Bean
    public CallLoggerImpl<JSONArray> httpJsonArrayCallLogger() {
        return new CallLoggerImpl<>(logManager(), new HttpRequestLogger(), new JsonArrayResponseLogger());
    }

    @Bean
    public CallLoggerImpl<File> httpFileCallLogger() {
        return new CallLoggerImpl<>(logManager(), new HttpRequestLogger(), new FileResponseLogger());
    }

    @Bean
    public CallLoggerImpl<OutputStream> httpStreamCallLogger() {
        return new CallLoggerImpl<>(logManager(), new HttpRequestLogger(), new StreamResponseLogger());
    }

    @Bean
    public CallLoggerImpl<FileDownload> fileDownloadCallLogger() {
        return new CallLoggerImpl<>(logManager(), new HttpRequestLogger(), new FileDownloadResponseLogger());
    }

    public void setEnv(Environment env) {
        this.env = env;
    }
}
