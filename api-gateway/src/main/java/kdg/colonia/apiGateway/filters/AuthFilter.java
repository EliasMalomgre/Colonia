package kdg.colonia.apiGateway.filters;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.rmi.activation.ActivationDesc;

@Slf4j
public class AuthFilter extends ZuulFilter
{
    @Value("${zuul.routes.auth.url}")
    private String authUrl;

    @Override
    public String filterType()
    {
        return "pre";
    }

    @Override
    public int filterOrder()
    {
        return 10;
    }

    @Override
    public boolean shouldFilter()
    {
        RequestContext ctx = RequestContext.getCurrentContext();

        if ((ctx.get("proxy") != null) && (ctx.get("proxy").equals("games")||ctx.get("proxy").equals("chat"))) {
            log.info(ctx.getRouteHost().getPath()+" ->route needs authentication.");
            return true;
        }
        return false;
    }

    @Override
    public Object run() throws ZuulException
    {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        String jwtToken=request.getHeader("Authorization");
        String userId=request.getHeader("UserId");
        log.info("token: "+jwtToken);

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.set("Authorization",jwtToken);

        HttpEntity<?> httpEntity = new HttpEntity<>(requestHeaders);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Boolean> response = restTemplate.exchange(authUrl+"/authForUser?userId="+userId, HttpMethod.GET, httpEntity, Boolean.class);
        if(response.getBody()!=null&&response.getBody()){
            //Authorized request. Continue as normal.
            log.info("Proceeding with request: "+ctx.getRouteHost());
        }
        else{
            //Undo request and return UNAUTHORIZED code.
            log.warn("Unauthorized request: "+ctx.getRouteHost());
            ctx.unset();
            ctx.setResponseStatusCode(HttpStatus.UNAUTHORIZED.value());
        }
        return null;
    }
}
