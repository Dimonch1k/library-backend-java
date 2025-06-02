package org.library;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.sql.DataSource;

@Configuration
@EnableWebMvc
@EnableTransactionManagement
//@EnableJpaRepositories("org.*")
//@ComponentScan(basePackages = {"org.*"})
//@EntityScan("org.*")
public class AppConfig
{
  @Bean
  public LocalContainerEntityManagerFactoryBean entityManagerFactory(
    DataSource dataSource )
  {
    LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
    em.setDataSource( dataSource );
    em.setPackagesToScan( "org.*" );
    JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
    em.setJpaVendorAdapter( vendorAdapter );
    return em;
  }
}