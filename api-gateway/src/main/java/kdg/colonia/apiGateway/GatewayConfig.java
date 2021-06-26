package kdg.colonia.apiGateway;
import kdg.colonia.apiGateway.filters.AuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig
{
    @Bean
    public AuthFilter authFilter(){
        return new AuthFilter();
    }
}
