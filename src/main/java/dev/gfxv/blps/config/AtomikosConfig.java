package dev.gfxv.blps.config;

import com.atomikos.icatch.jta.UserTransactionManager;
import com.atomikos.jdbc.AtomikosDataSourceBean;
import jakarta.transaction.SystemException;
import org.postgresql.xa.PGXADataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.jta.JtaTransactionManager;

@Configuration
public class AtomikosConfig {

    @Value("${spring.jta.atomikos.datasource.xa-properties.url}")
    private String dbUrl;

    @Value("${spring.jta.atomikos.datasource.xa-properties.user}")
    private String dbUser;

    @Value("${spring.jta.atomikos.datasource.xa-properties.password}")
    private String dbPassword;

    @Bean
    public AtomikosDataSourceBean dataSource() {
        AtomikosDataSourceBean dataSource = new AtomikosDataSourceBean();
        dataSource.setUniqueResourceName("dbResource");
        dataSource.setXaDataSourceClassName("org.postgresql.xa.PGXADataSource");

        PGXADataSource xaDataSource = new PGXADataSource();
        xaDataSource.setUrl(dbUrl);
        xaDataSource.setUser(dbUser);
        xaDataSource.setPassword(dbPassword);

        dataSource.setXaDataSource(xaDataSource);

        dataSource.setMinPoolSize(5);
        dataSource.setMaxPoolSize(20);
        return dataSource;
    }

    @Bean
    public UserTransactionManager atomikosTransactionManager() throws SystemException {
        UserTransactionManager manager = new UserTransactionManager();
        manager.setForceShutdown(false);
        manager.init();
        return manager;
    }

    @Bean
    public JtaTransactionManager transactionManager(UserTransactionManager atomikosTransactionManager) {
        JtaTransactionManager jtaTransactionManager = new JtaTransactionManager();
        jtaTransactionManager.setTransactionManager(atomikosTransactionManager);
        jtaTransactionManager.setUserTransaction(atomikosTransactionManager);
        return jtaTransactionManager;
    }
}