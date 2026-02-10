package lat.sal.library.library_item_service.config;

import lat.sal.library.library_item_service.LibraryBookingDAO;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class RepositoryConfig {

    @Bean
    public Jdbi jdbi(DataSource dataSource) {
        Jdbi jdbi = Jdbi.create(dataSource);
        jdbi.installPlugin(new SqlObjectPlugin());
        return jdbi;
    }

    @Bean
    public LibraryBookingDAO libraryBookingDAO(Jdbi jdbi) {
        return jdbi.onDemand(LibraryBookingDAO.class);
    }
}