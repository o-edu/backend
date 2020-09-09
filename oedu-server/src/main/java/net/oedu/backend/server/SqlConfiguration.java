package net.oedu.backend.server;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories("net.oedu.backend.data.repositories")
@EnableTransactionManagement
public class SqlConfiguration {

    private final boolean showSqlStatements = true;
    private final String database = "jdbc:mysql://localhost:3306/oedu";
    private final String username = "oedu";
    private final String password = "oedu";


    @Bean
    public DataSource dataSource() {

        final HikariConfig config = new HikariConfig();

        config.setJdbcUrl(database);
        config.setUsername(username);
        config.setPassword(password);

        config.setAutoCommit(false);

        config.setIdleTimeout(600000); // 10 minutes
        config.setMaxLifetime(1800000); // 30 minutes
        config.setConnectionTimeout(10000); // 10 seconds
        config.setInitializationFailTimeout(30000); // 30 seconds
        config.setValidationTimeout(5000); // 5 seconds

        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);

        return new HikariDataSource(config);
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(final DataSource dataSource) {
        final LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactoryBean.setDataSource(dataSource);
        entityManagerFactoryBean.setPackagesToScan("net.oedu");

        final HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
//    jpaVendorAdapter.setPrepareConnection();
        jpaVendorAdapter.setGenerateDdl(true);
        jpaVendorAdapter.setShowSql(showSqlStatements);
        entityManagerFactoryBean.setJpaVendorAdapter(jpaVendorAdapter);
//        entityManagerFactoryBean.setJpaProperties(new SqlProperties(StandardCharsets.UTF_8));
        return entityManagerFactoryBean;
    }

    @Bean
    public JpaTransactionManager transactionManager(final EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }


}
